package me.sizzlemcgrizzle.quests.dialogue.wrapper;

import de.craftlancer.core.LambdaRunnable;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.AssignedConversation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class QuestEntityWrapper implements ConfigurationSerializable {

    private Location location;
    private AssignedConversation conversation;
    private ItemStack displayItem;
    private WrapperDisplayItem wrapperDisplayItem;

    private double xOffset = 0.0, yOffset = 0.0, zOffset = 0.0;

    public QuestEntityWrapper(Location location) {
        this(location,null);
    }

    public QuestEntityWrapper(Location location, AssignedConversation conversation) {
        this.location = location;
        this.displayItem = new ItemStack(Material.AIR);
        this.wrapperDisplayItem = new WrapperDisplayItem(this);
        this.conversation = conversation == null ? new AssignedConversation(UUID.randomUUID().toString()) : conversation;

        start();
    }

    public QuestEntityWrapper(Map<String, Object> map) {
        this.location = (Location) map.get("location");
        this.conversation = (AssignedConversation) map.get("conversation");
        this.displayItem = (ItemStack) map.get("displayItem");
        this.wrapperDisplayItem = new WrapperDisplayItem(this);

        this.xOffset = (double) map.get("x");
        this.yOffset = (double) map.get("y");
        this.zOffset = (double) map.get("z");

        start();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        if (wrapperDisplayItem != null)
            wrapperDisplayItem.remove();

        Map<String, Object> map = new HashMap<>();

        map.put("location", location);
        map.put("conversation", conversation);
        map.put("displayItem", displayItem.toString());

        map.put("x", xOffset);
        map.put("y", yOffset);
        map.put("z", zOffset);

        return map;
    }

    private void start() {
        new LambdaRunnable(wrapperDisplayItem::tick).runTaskTimer(QuestsPlugin.getInstance(),0, 10);
    }

    @Nullable
    public abstract Entity getEntity();

    public AssignedConversation getConversation() {
        return conversation;
    }

    public Location getLocation() {
        return location == null ? getEntity().getLocation() : location;
    }

    public double getXOffset() {
        return xOffset;
    }

    public double getYOffset() {
        return yOffset;
    }

    public double getZOffset() {
        return zOffset;
    }

    public void setxOffset(double xOffset) {
        this.xOffset = xOffset;
    }

    public void setyOffset(double yOffset) {
        this.yOffset = yOffset;
    }

    public void setzOffset(double zOffset) {
        this.zOffset = zOffset;
    }

    public boolean hasConversation() {
        return conversation != null;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }
}
