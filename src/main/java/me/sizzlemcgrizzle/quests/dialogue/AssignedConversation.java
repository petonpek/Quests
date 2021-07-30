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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AssignedConversation extends AvatarConversation {
    
    private List<Quest> quests;
    
    public AssignedConversation(String id, Location location) {
        super(id, location);
        
        setOnComplete(player -> new LambdaRunnable(() -> displayQuestMenu(player)).runTaskLater(QuestsPlugin.getInstance(), 20));
        this.quests = new ArrayList<>();
    }
    
    public AssignedConversation(Map<String, Object> map) {
        super(map);
        
        this.quests = (List<Quest>) map.get("quests");
        
        setOnComplete(player -> new LambdaRunnable(() -> displayQuestMenu(player)).runTaskLater(QuestsPlugin.getInstance(), 20));
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("quests", quests);
        
        return map;
    }
    
    public List<Quest> getQuests() {
        return quests;
    }
    
    @Override
    protected void createMenu() {
        super.createMenu();
        
        MenuItem item = getConversationMenu().getInfoItem();
        
        item.setItem(new ItemBuilder(item.getItem()).setType(Material.EMERALD_BLOCK)
                .addLore("&8→ &6Right click to add quest", "&8→ &6Shift left click to view quests", "&8→ &6Shift right click to remove quest").build());
        
        item.addClickAction(click -> {
            Player player = click.getPlayer();
            
            if (quests.size() == 10) {
                MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "There are already 10 quests assigned.");
                return;
            }
            
            player.closeInventory();
            
            QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.ObjectInputPrompt<>("&bEnter a valid quest...",
                    string -> QuestsPlugin.getInstance().getQuest(string),
                    quest -> {
                        quests.add(quest);
                        
                        display(player);
                    }, () -> display(player)));
        }, ClickType.RIGHT)
                .addClickAction(click -> displayQuestMenu(click.getPlayer()), ClickType.SHIFT_LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    if (quests.size() == 0) {
                        MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "There are no quests assigned.");
                        return;
                    }
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.ObjectInputPrompt<>("&bEnter a valid quest...",
                            string -> quests.stream().filter(q -> q.getId().equalsIgnoreCase(string)).findFirst(),
                            quest -> {
                                quests.remove(quest);
                                
                                display(player);
                            }, () -> display(player)));
                }, ClickType.SHIFT_RIGHT);
        
        getConversationMenu().setInfoItem(item);
    }
    
    public void displayQuestMenu(Player player) {
        List<Quest> availableQuests = quests.stream().filter(q -> q.isPublic() && player.hasPermission(q.getPermission())).collect(Collectors.toList());
        
        Menu menu = new Menu(QuestsPlugin.getInstance(),
                ResourcePackManager.getInstance().isFullyAccepted(player) ?
                        ChatColor.WHITE + TranslateSpaceFont.getSpecificAmount(-8) + getInventoryUnicode(availableQuests.size())
                        : "Quests", 6);
        
        if (!availableQuests.isEmpty()) {
            for (int i = 0; i < Math.min(availableQuests.size(), 10); i++) {
                Quest quest = availableQuests.get(i);
                
                ItemBuilder builder = new ItemBuilder(Material.BOOK)
                        .setCustomModelData(200)
                        .setDisplayName(quest.getColor() + "" + ChatColor.BOLD + "" + quest.getId());
                
                if (quest.getDescription().size() > 0)
                    builder.addLore("");
                builder.addLore(quest.getDescription());
                
                builder.addLore("",
                        quest.canStartQuest(player) ? "&8→ &6Click to start quest" : "&cYou cannot start this quest right now.");
                
                for (int e = (i > 5 ? 5 : 0); e < (i > 5 ? 9 : 4); e++)
                    menu.set(i * 9 + e, new MenuItem(builder.build()).addClickAction(click -> {
                        quest.start(click.getPlayer());
                        player.closeInventory();
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