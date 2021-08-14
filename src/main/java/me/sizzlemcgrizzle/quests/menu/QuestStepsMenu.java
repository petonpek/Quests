package me.sizzlemcgrizzle.quests.menu;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.menu.PagedMenu;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;

import java.util.stream.Collectors;

public class QuestStepsMenu implements Listener {
    
    private final QuestsPlugin plugin;
    private final Quest quest;
    private PagedMenu menu;
    private QuestStepsSelectionMenu selectionMenu;
    
    public QuestStepsMenu(QuestsPlugin plugin, Quest quest) {
        this.plugin = plugin;
        this.quest = quest;
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    public void createMenu() {
        int[] index = {0};
        this.menu = new PagedMenu(plugin, "Quest Steps", true, 6,
                quest.getSteps().stream().map(step -> {
                    ItemBuilder builder = new ItemBuilder(step.getMenuItem());
                    
                    builder.addLore("&8→ &6Left click to edit",
                            "&8→ &6Right click to add quest step to the left",
                            "&8→ &6Shift right click to delete");
                    
                    int val = index[0]++;
                    
                    return new MenuItem(builder.build()).addClickAction(click -> step.display(click.getPlayer()), ClickType.LEFT)
                            .addClickAction(click -> selectionMenu.display(click.getPlayer(), val), ClickType.RIGHT)
                            .addClickAction(click -> {
                                quest.clearAllProgress();
                                quest.getSteps().remove(step);
                                
                                createMenu();
                                display(click.getPlayer());
                            }, ClickType.SHIFT_RIGHT);
                }).collect(Collectors.toList()), true);
        
        menu.setInfoItem(new MenuItem(new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("&e&lQuest Steps Page")
                .setLore("",
                        "&7Timeout: &6" + quest.getTimeout(),
                        "", "&8→ &6Left click to add step",
                        "&8→ &6Right click to set timeout").build())
                .addClickAction(click -> selectionMenu.display(click.getPlayer(), quest.getSteps().size()), ClickType.LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    if (!player.hasPermission(QuestsPlugin.ADMIN_PERMISSION)) {
                        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Only admins can do this.");
                        return;
                    }
                    
                    new LambdaRunnable(player::closeInventory).runTaskLater(plugin, 1);
                    
                    plugin.getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a positive number of seconds.",
                            d -> d >= 0, d -> {
                        
                        quest.setTimeout(d.intValue());
                        
                        createMenu();
                        display(player);
                    }, () -> display(player)));
                }, ClickType.RIGHT));
        
        menu.addToolbarItem(0, new MenuItem(new ItemBuilder(quest.isRepeatable() ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA)
                .setDisplayName("&e&lSet Repeatable")
                .setLore("", "&7Repeatable: " + ((quest.isRepeatable()) ? "&ayes" : "&cno"),
                        "",
                        "&8→ &6Click to toggle repeatable").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    if (!player.hasPermission(QuestsPlugin.ADMIN_PERMISSION)) {
                        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Only admins can do this.");
                        return;
                    }
                    
                    quest.setRepeatable(!quest.isRepeatable());
                    
                    createMenu();
                    display(click.getPlayer());
                }));
        
        menu.addToolbarItem(1, new MenuItem(new ItemBuilder(quest.startsAutomatically() ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA)
                .setDisplayName("&e&lSet Automatic Start")
                .setLore("", "&7Automatic start: " + ((quest.startsAutomatically()) ? "&ayes" : "&cno"),
                        "",
                        "&8→ &6Click to toggle automatic start").build())
                .addClickAction(click -> {
                    
                    Player player = click.getPlayer();
                    
                    if (!player.hasPermission(QuestsPlugin.ADMIN_PERMISSION)) {
                        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Only admins can do this.");
                        return;
                    }
                    
                    quest.setStartsAutomatically(!quest.startsAutomatically());
                    
                    createMenu();
                    display(click.getPlayer());
                }));
        
        menu.addToolbarItem(2, new MenuItem(new ItemBuilder(quest.isPublic() ? Material.LIME_TERRACOTTA : Material.RED_TERRACOTTA)
                .setDisplayName("&e&lSet Public")
                .setLore("", "&7Public: " + ((quest.isPublic()) ? "&ayes" : "&cno"),
                        "",
                        "&8→ &6Click to toggle public").build())
                .addClickAction(click -> {
                    
                    Player player = click.getPlayer();
                    
                    if (!player.hasPermission(QuestsPlugin.ADMIN_PERMISSION)) {
                        MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Only admins can do this.");
                        return;
                    }
                    
                    quest.setPublic(!quest.isPublic());
                    
                    createMenu();
                    display(click.getPlayer());
                }));
        
        menu.addToolbarItem(8, new MenuItem(new ItemBuilder(Material.BARRIER).setDisplayName("&e&lGo back")
                .setLore("", "&8→ &6Click to return to quest step menu").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    QuestsPlugin.getInstance().getQuestMenu().display(player);
                }));
    }
    
    public void display(Player player) {
        if (menu == null) {
            selectionMenu = new QuestStepsSelectionMenu(plugin, quest);
            createMenu();
        }
        
        menu.display(player);
    }
}
