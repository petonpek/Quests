package me.sizzlemcgrizzle.quests.menu;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import de.craftlancer.core.menu.Menu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.steps.QuestStepAdminShopTrade;
import me.sizzlemcgrizzle.quests.steps.QuestStepBlockInteract;
import me.sizzlemcgrizzle.quests.steps.QuestStepBlueprintPlace;
import me.sizzlemcgrizzle.quests.steps.QuestStepMythicMobInteraction;
import me.sizzlemcgrizzle.quests.steps.QuestStepMythicMobKill;
import me.sizzlemcgrizzle.quests.steps.QuestStepNPCInteraction;
import me.sizzlemcgrizzle.quests.steps.QuestStepWorldGuardAction;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QuestStepsSelectionMenu {
    
    private final QuestsPlugin plugin;
    private final Quest quest;
    private Menu menu;
    
    public QuestStepsSelectionMenu(QuestsPlugin plugin, Quest quest) {
        this.plugin = plugin;
        this.quest = quest;
    }
    
    public void display(Player player, int index) {
        createMenu(index);
        
        player.openInventory(menu.getInventory());
    }
    
    public void createMenu(int index) {
        this.menu = new Menu(plugin, "Step Selection", 1);
        
        ItemStack blockInteractionItem = new ItemBuilder(Material.STONE_BRICKS).setDisplayName("&e&lBlock Interaction").build();
        ItemStack entityInteractionItem = new ItemBuilder(Material.ZOMBIE_HEAD).setDisplayName("&e&lNPC Interaction").build();
        ItemStack mythicMobKillItem = new ItemBuilder(Material.WITHER_SKELETON_SKULL).setDisplayName("&e&lMythic Mob Kill").build();
        ItemStack worldGuardEntryItem = new ItemBuilder(Material.WOODEN_AXE).setDisplayName("&e&lWorld Guard Action").build();
        ItemStack mythicMobInteractionItem = new ItemBuilder(Material.SKELETON_SKULL).setDisplayName("&e&lMythic Mob Interaction").build();
        ItemStack adminShopTradeItem = new ItemBuilder(Material.END_PORTAL_FRAME).setDisplayName("&e&lAdmin Shop Trade").build();
        ItemStack blueprintPlaceItem = new ItemBuilder(Material.STONE).setCustomModelData(1).setDisplayName("&e&lBlueprint Placement").build();
        
        MenuItem blockInteractionMenuItem = new MenuItem(blockInteractionItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager()
                    .getBlockInput(player, block -> {
                        quest.getSteps().add(index, new QuestStepBlockInteract(quest, block.getLocation()));
                        quest.getMenu().createMenu();
                        quest.getMenu().display(player);
                    });
        });
        
        MenuItem entityInteractionMenuItem = new MenuItem(entityInteractionItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager().getBlockInput(player, block -> {
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Right click an npc...");
                plugin.getUserInputManager().getNPCInput(player, npc -> {
                    quest.getSteps().add(index, new QuestStepNPCInteraction(quest, block.getLocation(), npc));
                    quest.getMenu().createMenu();
                    quest.getMenu().display(player);
                });
            });
        });
        
        MenuItem mythicMobKillMenuItem = new MenuItem(mythicMobKillItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager().getBlockInput(player, block -> {
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Select an active mythic mob.");
                plugin.getUserInputManager().getMythicMobInput(player, (mob, active) -> {
                    plugin.getUserInputManager().getInput(player, new UserInputManager.NumberInputPrompt("&bEnter the amount of mythic mobs of this type to kill...", null,
                            d -> {
                                quest.getSteps().add(index, new QuestStepMythicMobKill(quest, block.getLocation(), mob, d.intValue()));
                                quest.getMenu().createMenu();
                                quest.getMenu().display(player);
                            }, () -> quest.getMenu().display(player)));
                });
            });
        });
        
        MenuItem worldGuardEntryMenuItem = new MenuItem(worldGuardEntryItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager().getBlockInput(player, block -> {
                plugin.getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a valid world guard region...",
                        string -> WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).hasRegion(string),
                        string -> {
                            quest.getSteps().add(index, new QuestStepWorldGuardAction(quest, block.getLocation(), string));
                            quest.getMenu().createMenu();
                            quest.getMenu().display(player);
                        }, () -> quest.getMenu().display(player)));
            });
        });
        
        MenuItem mythicMobInteractionMenuItem = new MenuItem(mythicMobInteractionItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager().getBlockInput(player, block -> {
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Select an active mythic mob.");
                plugin.getUserInputManager().getMythicMobInput(player, (mob, active) -> {
                    quest.getSteps().add(index, new QuestStepMythicMobInteraction(quest, block.getLocation(), active));
                    quest.getMenu().createMenu();
                    quest.getMenu().display(player);
                });
            });
        });
        
        MenuItem adminShopMenuItem = new MenuItem(adminShopTradeItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager()
                    .getBlockInput(player, block -> {
                        quest.getSteps().add(index, new QuestStepAdminShopTrade(quest, block.getLocation()));
                        quest.getMenu().createMenu();
                        quest.getMenu().display(player);
                    });
        });
        
        MenuItem exitButton = new MenuItem(new ItemBuilder(Material.BARRIER).setDisplayName("&e&lGo back")
                .setLore("", "&8→ &6Click to return to quest step menu").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            quest.getMenu().display(player);
        });
        
        MenuItem blueprintPlaceMenuItem = new MenuItem(blueprintPlaceItem).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "Click a block to set quest step location.");
            plugin.getUserInputManager().getBlockInput(player, block -> {
                plugin.getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a blueprint type region...",
                        string -> true,
                        string -> {
                            quest.getSteps().add(index, new QuestStepBlueprintPlace(quest, block.getLocation(), string));
                            quest.getMenu().createMenu();
                            quest.getMenu().display(player);
                        }, () -> quest.getMenu().display(player)));
            });
        });
        
        menu.set(0, blockInteractionMenuItem);
        menu.set(1, entityInteractionMenuItem);
        menu.set(2, mythicMobKillMenuItem);
        menu.set(3, worldGuardEntryMenuItem);
        menu.set(4, mythicMobInteractionMenuItem);
        menu.set(5, adminShopMenuItem);
        menu.set(6, blueprintPlaceMenuItem);
        menu.set(8, exitButton);
    }
}
