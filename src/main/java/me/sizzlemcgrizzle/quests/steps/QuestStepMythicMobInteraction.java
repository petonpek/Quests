package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class QuestStepMythicMobInteraction extends QuestStepItem {
    
    private MythicMob mythicMob;
    private String internalName;
    
    public QuestStepMythicMobInteraction(Quest quest, Location location, ActiveMob active) {
        super(quest, location, new ItemStack(Material.AIR));
        
        this.mythicMob = active.getType();
        this.internalName = mythicMob.getInternalName();
    }
    
    public QuestStepMythicMobInteraction(Map<String, Object> map) {
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
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        ActiveMob active = MythicMobs.inst().getAPIHelper().getMythicMobInstance(event.getRightClicked());
        
        if (active == null)
            return;
        
        if (mythicMob == null || !active.getType().equals(mythicMob))
            return;
        
        if (getQuest().canStartQuest(event.getPlayer(), this))
            getQuest().start(event.getPlayer());
        
        
        if (isPlayerOnStep(player))
            if (takeItems(player)) {
                onStepAction(player, 1);
                event.setCancelled(true);
            }
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        List<MenuItem> defaults = super.getConfigurationButtons();
        
        
        defaults.add(new MenuItem(new ItemBuilder(Material.SKELETON_SKULL).setDisplayName("&e&lChange Mythic Mob")
                .setLore("", "&7Mythic mob name: &6" + internalName, "", "&8→ &6Click to set mythic mob").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            player.closeInventory();
            
            MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "Select a mythic mob.");
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
    protected Material getMenuMaterial() {
        return Material.SKELETON_SKULL;
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lMythic Mob Interaction").build();
    }
}
