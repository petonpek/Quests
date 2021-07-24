package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.clstuff.adminshop.AdminShopTransactionEvent;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuestStepAdminShopTrade extends QuestStep {
    
    private List<Integer> rows;
    
    public QuestStepAdminShopTrade(Quest quest, Location location) {
        super(quest, location);
        
        this.rows = new ArrayList<>();
    }
    
    public QuestStepAdminShopTrade(Map<String, Object> map) {
        super(map);
        
        this.rows = (List<Integer>) map.get("rows");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("rows", rows);
        
        return map;
    }
    
    @EventHandler
    public void onAdminShopTrade(AdminShopTransactionEvent event) {
        if (!event.getShop().getLocation().equals(getLocation()))
            return;
        
        if (!rows.contains(event.getRow()))
            return;
        
        Player player = event.getPlayer();
        
        if (isPlayerOnStep(player))
            onStepAction(player, 1);
        
        if (getQuest().canStartQuest(event.getPlayer(), this)) {
            getQuest().start(event.getPlayer());
            onStepAction(player, 1);
        }
    }
    
    @Override
    protected Material getMenuMaterial() {
        return Material.END_PORTAL_FRAME;
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        return Collections.singletonList(new MenuItem(new ItemBuilder(Material.END_PORTAL_FRAME).setDisplayName("&e&lAdjust rows")
                .setLore("", "&8→ &6Click to set rows").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player,
                            new UserInputManager.ObjectInputPrompt<>("&bEnter which rows should be used, separated by commas, 0-3 (e.g. '0,1,2,3')",
                                    string -> {
                                        List<Integer> list = new ArrayList<>();
                                        
                                        for (String s : string.split(","))
                                            try {
                                                list.add(Integer.parseInt(s));
                                            } catch (NumberFormatException e) {
                                                return Optional.empty();
                                            }
                                        return Optional.of(list);
                                    }, list -> {
                                rows = list;
                                
                                createMenu(getQuest());
                                display(player, getQuest());
                            }));
                }));
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lAdmin Shop Trade").build();
    }
}
