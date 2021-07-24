package me.sizzlemcgrizzle.quests.menu;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.menu.PagedMenu;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class QuestsOverviewMenu {
    
    private final QuestsPlugin plugin;
    private final Map<UUID, PagedMenu> playerMenus = new HashMap<>();
    
    public QuestsOverviewMenu(QuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public Map<UUID, PagedMenu> getPlayerMenus() {
        return playerMenus;
    }
    
    public void display(Player player) {
        playerMenus.clear();
        PagedMenu menu = playerMenus.computeIfAbsent(player.getUniqueId(),
                e -> new PagedMenu(plugin, "Quests", true, 5,
                        plugin.getQuests().stream().filter(q -> player.hasPermission("quests.admin") || q.isPublic())
                                .sorted(Comparator.comparingInt(q -> q.getProgress().containsKey(player.getUniqueId()) ? 0 : q.canStartQuest(player) ? 1 : 2)).map(quest -> {
                            
                            ItemBuilder builder = new ItemBuilder(quest.getProgress().containsKey(player.getUniqueId()) ? Material.ENCHANTED_BOOK : quest.canStartQuest(player) ? Material.WRITABLE_BOOK : Material.BOOK)
                                    .setEnchantmentGlow(quest.hasCompleted(player))
                                    .setDisplayName("&5&l" + quest.getId());
                            
                            if (quest.getDescription().size() > 0)
                                builder.addLore("");
                            builder.addLore(quest.getDescription());
                            
                            if (quest.getSteps().size() > 0) {
                                Location loc = quest.getSteps().get(0).getLocation();
                                
                                if (loc != null)
                                    builder.addLore("", "&8→ &6Go to (" + ((int) loc.getX()) + ", " + ((int) loc.getY()) + ", " + ((int) loc.getZ()) + ")");
                            }
                            
                            if (player.hasPermission("quests.admin"))
                                builder.addLore("",
                                        "&c--- Admin Visible Only ---",
                                        "&8→ &6Left click to edit",
                                        "&8→ &6Right click to add description line",
                                        "&8→ &6Shift left click to remove last description line",
                                        "&8→ &6Shift right click to delete quest");
                            
                            MenuItem item = new MenuItem(builder.build());
                            
                            if (player.hasPermission("quests.admin")) {
                                item.addClickAction(click -> {
                                    Player p = click.getPlayer();
                                    
                                    if (p.hasPermission("quests.admin"))
                                        quest.getMenu().display(p);
                                }, ClickType.LEFT)
                                        .addClickAction(click -> {
                                            Player p = click.getPlayer();
                                            
                                            p.closeInventory();
                                            plugin.getUserInputManager().getInput(p, new UserInputManager.StringInputPrompt("&bEnter a line of description...", null,
                                                    string -> {
                                                        quest.getDescription().add(ChatColor.translateAlternateColorCodes('&', string));
                                                        
                                                        playerMenus.remove(p.getUniqueId());
                                                        display(p);
                                                    }, () -> display(p)));
                                        }, ClickType.RIGHT)
                                        .addClickAction(click -> {
                                            Player p = click.getPlayer();
                                            if (quest.getDescription().size() > 0)
                                                quest.getDescription().remove(quest.getDescription().size() - 1);
                                            
                                            playerMenus.remove(p.getUniqueId());
                                            display(p);
                                        }, ClickType.SHIFT_LEFT)
                                        .addClickAction(click -> {
                                            Player p = click.getPlayer();
                                            
                                            p.closeInventory();
                                            plugin.getUserInputManager().getInput(p, new UserInputManager.StringInputPrompt("&bEnter 'confirm' to delete quest.",
                                                    s -> s.equals("confirm"), s -> {
                                                plugin.getQuests().remove(quest);
                                                playerMenus.remove(p.getUniqueId());
                                                display(p);
                                            }, () -> display(p)));
                                        }, ClickType.SHIFT_RIGHT);
                            }
                            
                            return item;
                            
                        }).collect(Collectors.toList())
                        , true));
        
        menu.setInfoItem(new MenuItem(new ItemBuilder(Material.STONE).setDisplayName("&e&lQuest Information").setCustomModelData(5)
                .setLore("", "&7Enchanted books: &6Quest in progress", "&7Written books: &6Completed quest", "&7Writable books: &6Startable quests").build()));
        menu.display(player);
    }
}
