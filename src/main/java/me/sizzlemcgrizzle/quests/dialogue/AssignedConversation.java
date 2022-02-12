package me.sizzlemcgrizzle.quests.dialogue;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.menu.Menu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AssignedConversation extends AvatarConversation {
    
    private List<String> questNames = new ArrayList<>();
    
    public AssignedConversation(String id) {
        super(id);
        
        setOnComplete(player -> new LambdaRunnable(() -> {
            if (!questNames.isEmpty())
                displayQuestMenu(player);
        }).runTaskLater(QuestsPlugin.getInstance(), 20));
    }
    
    public AssignedConversation(Map<String, Object> map) {
        super(map);
        
        if (map.containsKey("questNames"))
            questNames = ((List<String>) map.get("questNames"));
        else
            questNames = ((List<Quest>) map.get("quests")).stream().map(Quest::getId).collect(Collectors.toList());
        
        setOnComplete(player -> new LambdaRunnable(() -> {
            if (!questNames.isEmpty())
                displayQuestMenu(player);
        }).runTaskLater(QuestsPlugin.getInstance(), 20));
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("questNames", questNames);
        
        return map;
    }
    
    @Override
    protected int getCooldown() {
        return 21;
    }
    
    public boolean removeQuest(Quest quest) {
        return questNames.remove(quest.getId());
    }
    
    public List<Quest> getAvailableQuests(Player player) {
        List<Quest> list = new ArrayList<>();
        
        questNames.forEach(name -> QuestsPlugin.getInstance().getQuest(name).ifPresent(quest -> {
            if (!quest.isPublic())
                return;
            
            if (!quest.playerHasPermission(player))
                return;
            
            list.add(quest);
        }));
        
        return list;
    }
    
    @Override
    protected void createMenu() {
        super.createMenu();
        
        MenuItem item = getConversationMenu().getInfoItem().deepClone(i -> new ItemBuilder(i).setType(Material.EMERALD_BLOCK)
                .addLore("&8→ &6Right click to add quest", "&8→ &6Shift left click to view quests", "&8→ &6Shift right click to remove quest").build());
        
        item.addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    if (questNames.size() == 10) {
                        MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "There are already 10 quests assigned.");
                        return;
                    }
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.ObjectInputPrompt<>("&bEnter a valid quest that is not already assigned...",
                            string -> questNames.contains(string) ? Optional.empty() : QuestsPlugin.getInstance().getQuest(string),
                            quest -> {
                                questNames.add(quest.getId());
                                
                                display(player);
                            }, () -> display(player)));
                }, ClickType.RIGHT)
                .addClickAction(click -> displayQuestMenu(click.getPlayer()), ClickType.SHIFT_LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    if (questNames.size() == 0) {
                        MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "There are no quests assigned.");
                        return;
                    }
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a valid quest...",
                            string -> questNames.contains(string),
                            s -> {
                                questNames.remove(s);
                                
                                display(player);
                            }, () -> display(player)));
                }, ClickType.SHIFT_RIGHT);
        
        getConversationMenu().setInfoItem(item);
    }
    
    public void displayQuestMenu(Player player) {
        List<Quest> availableQuests = getAvailableQuests(player);
        
        Menu menu = new Menu(QuestsPlugin.getInstance(),
                ResourcePackManager.getInstance().isFullyAccepted(player) ?
                        ChatColor.WHITE + TranslateSpaceFont.getSpecificAmount(-8) + getInventoryUnicode(availableQuests.size())
                        : "Quests", 6);
        
        if (!availableQuests.isEmpty()) {
            for (int i = 0; i < Math.min(availableQuests.size(), 5); i++) {
                Quest quest = availableQuests.get(i);
                
                ItemBuilder builder = new ItemBuilder(Material.BOOK)
                        .setCustomModelData(200)
                        .setDisplayName(quest.getColor() + "" + ChatColor.BOLD + "" + quest.getId());
                
                if (quest.getDescription().size() > 0)
                    builder.addLore("");
                builder.addLore(quest.getDescription());
                
                builder.addLore("",
                        quest.canStartQuest(player) ? "&8→ &6Click to start quest" : "&cYou cannot start this quest right now.");
                
                for (int e = 0; e < 4; e++)
                    menu.set(i * 9 + e, new MenuItem(builder.build()).addClickAction(click -> {
                        if (quest.canStartQuest(player)) {
                            quest.start(click.getPlayer());
                            player.closeInventory();
                        } else {
                            MessageUtil.sendMessage(QuestsPlugin.getInstance(), player,
                                    MessageLevel.INFO, "You cannot accept this quest right now. Use /quests abandon if you have an active quest.");
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                        }
                    }));
            }
            
            for (int i = 0; i < Math.min(availableQuests.size() - 5, 5); i++) {
                Quest quest = availableQuests.get(i + 5);
                
                ItemBuilder builder = new ItemBuilder(Material.BOOK)
                        .setCustomModelData(200)
                        .setDisplayName(quest.getColor() + "" + ChatColor.BOLD + "" + quest.getId());
                
                if (quest.getDescription().size() > 0)
                    builder.addLore("");
                builder.addLore(quest.getDescription());
                
                builder.addLore("",
                        quest.canStartQuest(player) ? "&8→ &6Click to start quest" : "&cYou cannot start this quest right now.");
                
                for (int e = 5; e < 9; e++)
                    menu.set(i * 9 + e, new MenuItem(builder.build()).addClickAction(click -> {
                        if (quest.canStartQuest(player)) {
                            quest.start(click.getPlayer());
                            player.closeInventory();
                        } else {
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5F, 1F);
                        }
                    }));
            }
        }
        
        player.openInventory(menu.getInventory());
    }
    
    private String getInventoryUnicode(int size) {
        switch (size) {
            case 0:
                return "\uE320";
            case 1:
                return "\uE321";
            case 2:
                return "\uE322";
            case 3:
                return "\uE323";
            case 4:
                return "\uE324";
            case 5:
                return "\uE325";
            case 6:
                return "\uE326";
            case 7:
                return "\uE327";
            case 8:
                return "\uE328";
            case 9:
                return "\uE329";
            default:
                return "\uE330";
            
        }
    }
    
}
