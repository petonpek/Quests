package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.Quest;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class QuestStepBlockInteract extends QuestStepItem {
    public QuestStepBlockInteract(Quest quest, Location location) {
        super(quest, location, new ItemStack(Material.AIR));
    }
    
    public QuestStepBlockInteract(Map<String, Object> map) {
        super(map);
    }
    
    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        if (!event.getClickedBlock().getLocation().equals(getLocation()))
            return;
        
        if (isPlayerOnStep(event.getPlayer()))
            if (takeItems(event.getPlayer()))
                onStepAction(event.getPlayer(), 1);

//        if (getQuest().canStartQuest(event.getPlayer(), this)) {
//            getQuest().start(event.getPlayer());
//            onStepAction(event.getPlayer(), 1);
//        }
    }
    
    @Override
    protected Material getMenuMaterial() {
        return Material.STONE_BRICKS;
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        return super.getConfigurationButtons();
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lBlock Interaction").build();
    }
}
