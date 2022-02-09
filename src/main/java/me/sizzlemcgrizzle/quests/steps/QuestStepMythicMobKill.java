package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class QuestStepMythicMobKill extends QuestStep {
    
    private MythicMob mythicMob;
    private String internalName;
    
    public QuestStepMythicMobKill(Quest quest, Location location, MythicMob mob, int totalWeight) {
        super(quest, location, totalWeight);
        
        this.mythicMob = mob;
        this.internalName = mob.getInternalName();
    }
    
    public QuestStepMythicMobKill(Map<String, Object> map) {
        super(map);
        
        this.internalName = (String) map.get("name");
        this.mythicMob = MythicMobs.inst().getMobManager().getMythicMob(internalName);
        
        if (mythicMob == null)
            MessageUtil.sendMessage(QuestsPlugin.getInstance(),
                    Bukkit.getConsoleSender(),
                    MessageLevel.WARNING,
                    "Error: Mythic mob with name \"" + internalName + "\" does not exist in QuestStepMythicMobKill");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("name", internalName);
        
        return map;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!(event.getKiller() instanceof Player player))
            return;
        
        if (mythicMob == null || !event.getMobType().equals(mythicMob))
            return;
        
        if (getQuest().canStartQuest(player, this))
            getQuest().start(player);
        
        if (isPlayerOnStep(player))
            onStepAction(player, 1);
    }
    
    @Override
    protected Material getMenuMaterial() {
        return Material.WITHER_SKELETON_SKULL;
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        List<MenuItem> defaults = super.getConfigurationButtons();
        
        defaults.add(new MenuItem(new ItemBuilder(Material.WITHER_SKELETON_SKULL).setDisplayName("&e&lChange Mythic Mob")
                .setLore("", "&7Mythic mob name: &6" + internalName, "", "&8→ &6Click to set mythic mob").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            
            MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "Select an active mythic mob.");
            QuestsPlugin.getInstance().getUserInputManager().getMythicMobInput(player, (mob, active) -> {
                mythicMob = mob;
                internalName = mob.getInternalName();
                
                createMenu();
                display(player);
            });
        }));
        
        return defaults;
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&eMythic Mob Kills").build();
    }
}
