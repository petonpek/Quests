package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.blueprints.api.BlueprintPostPasteEvent;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QuestStepBlueprintPlace extends QuestStepItem {
    
    private String type;
    
    public QuestStepBlueprintPlace(Quest quest, Location location, String type) {
        super(quest, location, new ItemStack(Material.AIR));
        
        this.type = type;
    }
    
    public QuestStepBlueprintPlace(Map<String, Object> map) {
        super(map);
        
        this.type = (String) map.get("type");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("type", type);
        
        return map;
    }
    
    @EventHandler
    public void onBlueprintPlace(BlueprintPostPasteEvent event) {
        
        if (!event.getType().equals(type))
            return;
        
        if (isPlayerOnStep(event.getPlayer()))
            if (takeItems(event.getPlayer()))
                onStepAction(event.getPlayer(), 1);
        
        if (getQuest().canStartQuest(event.getPlayer(), this)) {
            getQuest().start(event.getPlayer());
            onStepAction(event.getPlayer(), 1);
        }
    }
    
    @Override
    protected Material getMenuMaterial() {
        return Material.STONE;
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        return Collections.singletonList(new MenuItem(new ItemBuilder(Material.STONE).setCustomModelData(1).setDisplayName("&e&lChange Blueprint Type")
                .setLore("", "&7Blueprint type: &6" + type, "", "&8→ &6Click to set blueprint type").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a blueprint type...",
                            string -> true,
                            string -> {
                                type = string;
                                
                                createMenu();
                                display(player);
                            }, () -> display(player)));
                }));
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lBlueprint Place").setCustomModelData(1).build();
    }
}
