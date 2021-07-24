package me.sizzlemcgrizzle.quests.menu;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.menu.PagedMenu;
import de.craftlancer.core.util.ItemBuilder;
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
        this.menu = new PagedMenu(plugin, "Quest Steps", true, 6,
                quest.getSteps().stream().map(step -> {
                    ItemBuilder builder = new ItemBuilder(step.getMenuItem());
                    
                    builder.addLore("&8→ &6Left click to edit",
                            "&8→ &6Shift right click to delete");
                    
                    return new MenuItem(builder.build()).addClickAction(click -> step.display(click.getPlayer(), quest), ClickType.LEFT)
                            .addClickAction(click -> {
                                quest.getSteps().remove(step);
                                
                                createMenu();
                                display(click.getPlayer());
                            });
                }).collect(Collectors.toList()), true);
        
        menu.setInfoItem(new MenuItem(new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("&e&lQuest Steps Page")
                .setLore("", "&7Public: " + ((quest.isPublic()) ? "&ayes" : "&cno"),
                        "&7Repeatable: " + ((quest.isRepeatable()) ? "&ayes" : "&cno"),
                        "&7Timeout: &6" + quest.getTimeout(),
                        "", "&8→ &6Left click to add step",
                        "&8→ &6Right click to toggle public",
                        "&8→ &6Shift left click to toggle repeatable",
                        "&8→ &6Shift right click to set timeout").build())
                .addClickAction(click -> selectionMenu.display(click.getPlayer(), quest), ClickType.LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    quest.setPublic(!quest.isPublic());
                    createMenu();
                    display(player);
                }, ClickType.RIGHT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    quest.setRepeatable(!quest.isRepeatable());
                    createMenu();
                    display(player);
                }, ClickType.SHIFT_LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    new LambdaRunnable(() -> player.closeInventory()).runTaskLater(plugin, 1);
                    
                    plugin.getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a positive number of seconds.",
                            d -> d >= 0, d -> {
                        
                        quest.setTimeout(d.intValue());
                        
                        createMenu();
                        display(player);
                    }, () -> display(player)));
                }, ClickType.SHIFT_RIGHT));
        
        menu.setInventoryUpdateHandler(m -> m.set(52, new MenuItem(new ItemBuilder(Material.BARRIER).setDisplayName("&e&lGo back")
                .setLore("", "&8→ &6Click to return to quest step menu").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            QuestsPlugin.getInstance().getQuestMenu().display(player);
        })));
    }
    
    public void display(Player player) {
        if (menu == null) {
            createMenu();
            selectionMenu = new QuestStepsSelectionMenu(plugin, quest);
        }
        
        menu.display(player);
    }
}
