package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class QuestStepNPCInteraction extends QuestStepItem {
    
    private int id;
    
    public QuestStepNPCInteraction(Quest quest, Location location, NPC npc) {
        super(quest, location, new ItemStack(Material.AIR));
        
        this.id = npc.getId();
    }
    
    public QuestStepNPCInteraction(Map<String, Object> map) {
        super(map);
        
        this.id = (int) map.get("id");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("id", id);
        
        return map;
    }
    
    @EventHandler
    public void onEntityInteract(NPCRightClickEvent event) {
        if (event.getNPC().getId() != id)
            return;
        
        if (getQuest().canStartQuest(event.getClicker(), this))
            getQuest().start(event.getClicker());
        
        if (isPlayerOnStep(event.getClicker()))
            if (takeItems(event.getClicker())) {
                onStepAction(event.getClicker(), 1);
                event.setCancelled(true);
            }
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        List<MenuItem> defaults = super.getConfigurationButtons();
        
        defaults.add(new MenuItem(new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("&e&lChange NPC")
                .setLore("", "&8→ &6Click to set NPC").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            
            player.closeInventory();
            MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "Select an npc.");
            
            QuestsPlugin.getInstance().getUserInputManager().getNPCInput(player, npc -> {
                id = npc.getId();
                
                createMenu();
                display(player);
            });
        }));
        
        return defaults;
    }
    
    @Override
    protected Material getMenuMaterial() {
        return Material.ZOMBIE_HEAD;
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lNPC Interaction").build();
    }
}
