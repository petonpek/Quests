package me.sizzlemcgrizzle.quests.dialogue.wrapper;

import me.sizzlemcgrizzle.quests.dialogue.AssignedConversation;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;

public class NPCWrapper extends QuestEntityWrapper{

    private int id;

    public NPCWrapper(int id, Location location) {
        super(location);

        this.id = id;
    }

    public NPCWrapper(int id, Location location, AssignedConversation conversation) {
        super(location, conversation);

        this.id = id;
    }

    public NPCWrapper(Map<String, Object> map) {
        super(map);

        this.id = (int) map.get("id");
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        map.put("id", id);

        return map;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return CitizensAPI.getNPCRegistry().getById(id).getEntity();
    }

    public int getId() {
        return id;
    }
}
