package me.sizzlemcgrizzle.quests.dialogue.wrapper;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.sizzlemcgrizzle.quests.dialogue.AssignedConversation;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MythicMobWrapper extends QuestEntityWrapper{

    private MythicMob mob;

    public MythicMobWrapper(MythicMob mob, Location location) {
        super(location);

        this.mob = mob;
    }

    public MythicMobWrapper(MythicMob mob, Location location, AssignedConversation conversation) {
        super(location, conversation);

        this.mob = mob;
    }

    public MythicMobWrapper(Map<String, Object> map) {
        super(map);

        this.mob = MythicMobs.inst().getMobManager().getMythicMob(map.get("name").toString());
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();

        map.put("name", mob.getInternalName());

        return map;
    }

    public MythicMob getMob() {
        return mob;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return MythicMobs.inst().getMobManager().getActiveMobs()
                .stream()
                .filter(m -> m.getType().equals(mob))
                .findFirst()
                .map(activeMob -> activeMob.getEntity().getBukkitEntity()).orElse(null);
    }
}
