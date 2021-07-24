package me.sizzlemcgrizzle.quests;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.menu.Menu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.steps.QuestStep;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuestReward implements ConfigurationSerializable {
    
    private List<ItemStack> items;
    private List<String> commands;
    private List<ItemStack> fireworks;
    private Menu menu;
    
    public QuestReward() {
        this.items = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.fireworks = new ArrayList<>();
    }
    
    public QuestReward(Map<String, Object> map) {
        this.items = (List<ItemStack>) map.getOrDefault("items", new ArrayList<>());
        this.commands = (List<String>) map.getOrDefault("commands", new ArrayList<>());
        this.fireworks = (List<ItemStack>) map.getOrDefault("fireworks", new ArrayList<>());
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("items", items);
        map.put("commands", commands);
        map.put("fireworks", fireworks);
        
        return map;
    }
    
    public void display(Player player, QuestStep questStep) {
        if (menu == null)
            createMenu(questStep);
        
        player.openInventory(menu.getInventory());
    }
    
    public void createMenu(QuestStep questStep) {
        this.menu = new Menu(QuestsPlugin.getInstance(), "Reward Editor", 1);
        
        int[] itemIndex = {0};
        MenuItem itemButton = new MenuItem(new ItemBuilder(Material.DIAMOND).setDisplayName("&e&lItem Rewards")
                .setLore(items.stream()
                        .map(itemStack -> ChatColor.GRAY + "" + itemIndex[0]++ + ": " + ChatColor.GOLD + (itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name()))
                        .collect(Collectors.toList()))
                .addLore("", "&8→ &6Left click to add item", "&8→ &6Right click to receive item", "&8→ &6Shift right click to remove an item").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    items.add(player.getInventory().getItemInMainHand().clone());
                    createMenu(questStep);
                    display(player, questStep);
                }, ClickType.LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a valid index to receive the item from.",
                            d -> d >= 0 && d < items.size(),
                            d -> {
                                player.getInventory().addItem(items.get(d.intValue()));
                                display(player, questStep);
                            },
                            () -> display(player, questStep)));
                }, ClickType.RIGHT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a valid index to remove the item from.",
                            d -> d >= 0 && d < items.size(),
                            d -> {
                                items.remove(d.intValue());
                                createMenu(questStep);
                                display(player, questStep);
                            },
                            () -> display(player, questStep)));
                }, ClickType.SHIFT_RIGHT);
        
        int[] commandIndex = {0};
        MenuItem commandsButton = new MenuItem(new ItemBuilder(Material.COMMAND_BLOCK).setDisplayName("&e&lCommand Rewards")
                .setLore(commands.stream().map(c -> ChatColor.GRAY + "" + commandIndex[0]++ + ": " + c).collect(Collectors.toList()))
                .addLore("", "&8→ &6Left click to add command", "&8→ &6Right click to remove command").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a command without a slash.",
                            string -> string.charAt(0) != '/', string -> {
                        
                        commands.add(string);
                        createMenu(questStep);
                        display(player, questStep);
                    }, () -> display(player, questStep)));
                }, ClickType.LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a valid index to remove the command from.",
                            d -> d >= 0 && d < commands.size(),
                            d -> {
                                commands.remove(d.intValue());
                                createMenu(questStep);
                                display(player, questStep);
                            },
                            () -> display(player, questStep)));
                }, ClickType.RIGHT);
        
        int[] fireworkIndex = {0};
        MenuItem fireworkButton = new MenuItem(new ItemBuilder(Material.FIREWORK_ROCKET).setDisplayName("&e&lFirework Rewards")
                .setLore(fireworks.stream()
                        .map(itemStack -> ChatColor.GRAY + "" + fireworkIndex[0]++ + ": " + ChatColor.GOLD + (itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name()))
                        .collect(Collectors.toList()))
                .addLore("", "&8→ &6Left click to add firework", "&8→ &6Right click to receive firework", "&8→ &6Shift right click to remove an firework").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    if (player.getInventory().getItemInMainHand().getType() != Material.FIREWORK_ROCKET) {
                        MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "You must hold a firework.");
                        return;
                    }
                    fireworks.add(player.getInventory().getItemInMainHand().clone());
                    createMenu(questStep);
                    display(player, questStep);
                }, ClickType.LEFT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a valid index to receive the firework from.",
                            d -> d >= 0 && d < fireworks.size(),
                            d -> {
                                player.getInventory().addItem(fireworks.get(d.intValue()));
                                display(player, questStep);
                            },
                            () -> display(player, questStep)));
                }, ClickType.RIGHT)
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a valid index to remove the firework from.",
                            d -> d >= 0 && d < fireworks.size(),
                            d -> {
                                fireworks.remove(d.intValue());
                                createMenu(questStep);
                                display(player, questStep);
                            },
                            () -> display(player, questStep)));
                }, ClickType.SHIFT_RIGHT);
        
        MenuItem exitButton = new MenuItem(new ItemBuilder(Material.BARRIER).setDisplayName("&e&lGo back")
                .setLore("", "&8→ &6Click to return to main menu").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            questStep.display(player, questStep.getQuest());
        });
        
        menu.set(0, itemButton);
        menu.set(1, commandsButton);
        menu.set(2, fireworkButton);
        menu.set(8, exitButton);
    }
    
    public void reward(Player player) {
        items.forEach(item -> player.getInventory().addItem(item).forEach((integer, i) -> player.getWorld().dropItemNaturally(player.getLocation(), i)));
        commands.forEach(c -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), c.replace("%player%", player.getName())));
        for (int i = 0; i < fireworks.size(); i++) {
            ItemStack firework = fireworks.get(i);
            
            new LambdaRunnable(() -> {
                if (!firework.hasItemMeta() || !(firework.getItemMeta() instanceof FireworkMeta))
                    return;
                FireworkMeta meta = (FireworkMeta) firework.getItemMeta();
                
                
                Firework fw = player.getWorld().spawn(player.getEyeLocation(), Firework.class);
                fw.setFireworkMeta(meta);
            }).runTaskLater(QuestsPlugin.getInstance(), i * 5L);
        }
    }
}
