package me.sizzlemcgrizzle.quests.steps;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QuestStepWorldGuardAction extends QuestStep {
    
    private String regionName;
    //true -> entry, false -> exit
    private boolean entry = true;
    
    public QuestStepWorldGuardAction(Quest quest, Location location, String regionName) {
        super(quest, location);
        
        this.regionName = regionName;
    }
    
    public QuestStepWorldGuardAction(Map<String, Object> map) {
        super(map);
        
        this.regionName = (String) map.get("regionName");
        this.entry = (boolean) map.getOrDefault("entry", true);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("regionName", regionName);
        map.put("entry", entry);
        
        return map;
    }
    
    @EventHandler
    public void regionEnteredEvent(RegionEnteredEvent event) {
        if (!entry)
            return;
        
        if (!event.getRegionName().equalsIgnoreCase(regionName))
            return;
        
        if (event.getPlayer() == null)
            return;
        
        if (isPlayerOnStep(event.getPlayer()))
            onStepAction(event.getPlayer(), 1);
        
        if (getQuest().canStartQuest(event.getPlayer(), this)) {
            getQuest().start(event.getPlayer());
            onStepAction(event.getPlayer(), 1);
        }
    }
    
    @EventHandler
    public void regionExitEvent(RegionLeftEvent event) {
        if (entry)
            return;
        
        if (!event.getRegionName().equalsIgnoreCase(regionName))
            return;
        
        if (event.getPlayer() == null)
            return;
        
        if (isPlayerOnStep(event.getPlayer()))
            onStepAction(event.getPlayer(), 1);
        
        if (getQuest().canStartQuest(event.getPlayer(), this)) {
            getQuest().start(event.getPlayer());
            onStepAction(event.getPlayer(), 1);
        }
    }
    
    @Override
    protected Material getMenuMaterial() {
        return Material.WOODEN_AXE;
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        return Arrays.asList(new MenuItem(new ItemBuilder(Material.WOODEN_AXE).setDisplayName("&e&lChange World Guard Region")
                        .setLore("", "&7Region name: &6" + regionName, "", "&8→ &6Click to set region").build()).addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a valid world guard region...",
                            string -> WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).hasRegion(string),
                            string -> {
                                regionName = string;
                                
                                createMenu(null);
                                display(player, null);
                            }, () -> display(player, null)));
                }),
                new MenuItem(new ItemBuilder(Material.OAK_DOOR).setDisplayName("&e&lToggle Action")
                        .setLore("", "&7Action: &6" + (entry ? "entry" : "exit"), "", "&8→ &6Click to toggle action").build()).addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    entry = !entry;
                    createMenu(getQuest());
                    display(player, getQuest());
                }));
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lWorld Guard Region Action").build();
    }
}
