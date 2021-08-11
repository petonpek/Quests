package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.InventoryUtils;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestProgress;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public abstract class QuestStepItem extends QuestStep {
    
    private ItemStack item;
    
    public QuestStepItem(Quest quest, Location location, ItemStack item) {
        this(quest, location, 1, item);
        
        this.item = item;
    }
    
    public QuestStepItem(Quest quest, Location location, int weight, ItemStack item) {
        super(quest, location, weight);
        
        this.item = item;
    }
    
    public QuestStepItem(Map<String, Object> map) {
        super(map);
        
        this.item = (ItemStack) map.get("itemRequirement");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("itemRequirement", item);
        
        return map;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    /**
     * @return true if item is taken, false otherwise
     */
    public boolean takeItems(Player player) {
        
        if (item == null)
            return true;
        
        if (item.getType().isAir())
            return true;
        
        QuestProgress progress = getQuest().getProgress().get(player.getUniqueId());
        
        if (progress.getCompletedWeight() >= getTotalWeight())
            return true;
        
        if (!InventoryUtils.containsAtLeast(player.getInventory(), item))
            return false;
        
        player.getInventory().removeItem(item);
        return true;
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons(List<MenuItem> defaults) {
        defaults.add(new MenuItem(new ItemBuilder(Material.CHEST).setDisplayName("&e&lSet Item")
                .setLore("", "&7Current item: &6" + ((item == null || item.getType().isAir() ? "none" :
                                "x" + item.getAmount() + " " + (item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name()))),
                        "", "&8→ &6Click to set item in hand").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            
            item = player.getInventory().getItemInMainHand().clone();
            
            createMenu();
            display(player);
        }));
        
        return defaults;
    }
}
