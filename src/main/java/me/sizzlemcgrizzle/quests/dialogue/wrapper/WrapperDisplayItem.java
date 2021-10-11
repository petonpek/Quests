package me.sizzlemcgrizzle.quests.dialogue.wrapper;

import de.craftlancer.core.Utils;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class WrapperDisplayItem {
    public static final String DISPLAY_ITEM_METADATA = "adminshopDisplayItem";

    private QuestEntityWrapper wrapper;
    private Item item;
    private ItemStack displayItem;
    private Location location;

    public WrapperDisplayItem(QuestEntityWrapper wrapper) {
        this.wrapper = wrapper;

        refresh();
    }

    public void tick() {
        if (displayItem == null
                || displayItem.getType().isAir()
                || location == null
                || !Utils.isChunkLoaded(location))
            return;

        if (item == null || !item.isValid()) {
            item = location.getWorld().dropItem(location, displayItem);
            item.setInvulnerable(true);
            item.setMetadata(DISPLAY_ITEM_METADATA, new FixedMetadataValue(QuestsPlugin.getInstance(), 0));
        }

        item.setVelocity(new Vector().zero());
    }

    public void remove() {
        if (item == null)
            return;

        item.remove();
    }

    public void refresh() {
        remove();

        if (wrapper.getLocation() != null)
            this.location = wrapper.getLocation().add(
                    wrapper.getXOffset(),
                    wrapper.getYOffset(),
                    wrapper.getZOffset()
            );

        if (wrapper.getDisplayItem() != null && !wrapper.getDisplayItem().getType().isAir())
            this.displayItem = new ItemBuilder(wrapper.getDisplayItem())
                    .setDisplayName(ChatColor.DARK_GRAY + "AdminShopDisplayItem")
                    .setAmount(1)
                    .build();

        tick();
    }
}
