package me.sizzlemcgrizzle.quests.dialogue;

import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Avatar implements ConfigurationSerializable {
    
    private final String name;
    private final String unicode;
    private final List<String> sounds;
    // false -> male, true -> female
    private boolean gender;
    
    public Avatar(String name, String unicode) {
        this.name = name;
        this.unicode = unicode;
        this.sounds = new ArrayList<>();
        this.gender = false;
    }
    
    public Avatar(Map<String, Object> map) {
        this.name = map.get("name").toString();
        this.unicode = map.get("unicode").toString();
        this.sounds = (List<String>) map.getOrDefault("sounds", new ArrayList<>());
        this.gender = (boolean) map.getOrDefault("gender", false);
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("name", name);
        map.put("unicode", unicode);
        map.put("sounds", sounds);
        map.put("gender", gender);
        
        return map;
    }
    
    public String getUnicode() {
        return unicode;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getSounds() {
        return sounds;
    }
    
    public void playSound(Player player) {
        if (!sounds.isEmpty()) {
            String sound = sounds.get((int) (Math.random() * sounds.size()));
            player.playSound(player.getLocation(), sound, 0.5F, 1F);
        } else {
            player.playSound(player.getLocation(),
                    Sound.ENTITY_VILLAGER_YES,
                    0.5F,
                    isMale() ? 0.7F : 1.6F);
        }
    }
    
    public boolean isMale() {
        return !gender;
    }
    
    public void setGender(boolean gender) {
        this.gender = gender;
    }
}
