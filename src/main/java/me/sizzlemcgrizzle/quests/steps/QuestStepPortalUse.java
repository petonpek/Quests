package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.clapi.clfeatures.portal.event.PortalTeleportEvent;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class QuestStepPortalUse extends QuestStepItem {
    public QuestStepPortalUse(Quest quest, Location location) {
        super(quest, location, new ItemStack(Material.AIR));
    }
    
    public QuestStepPortalUse(Map<String, Object> map) {
        super(map);
    }
    
    @EventHandler
    public void onBlockInteract(PortalTeleportEvent event) {
        if (!event.getStartingPortal().getInitialBlock().equals(getLocation()))
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
        return Material.CHISELED_QUARTZ_BLOCK;
    }
    
    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lPortal Use").build();
    }
    
    @Override
    protected List<MenuItem> getConfigurationButtons() {
        List<MenuItem> defaults = super.getConfigurationButtons();
        
        defaults.set(0, new MenuItem(new ItemBuilder(Material.CHISELED_QUARTZ_BLOCK).setDisplayName("&e&lSet Portal")
                .addLore("", "&8→ &6Click to set portal").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    
                    player.closeInventory();
                    MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "Right click a portal...");
                    
                    QuestsPlugin.getInstance().getUserInputManager().getPortalInput(player, portal -> {
                        setLocation(portal.getInitialBlock());
                        
                        createMenu();
                        display(player);
                    });
                }));
        
        return defaults;
    }
}
