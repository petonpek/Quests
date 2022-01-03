package me.sizzlemcgrizzle.quests;

import net.minecraft.server.v1_16_R3.Entity;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress implements ConfigurationSerializable {
    
    private int completedWeight = 0;
    private int stepID = 0;
    
    public QuestProgress() {

    }
    
    public QuestProgress(Map<String, Object> map) {
        this.completedWeight = (int) map.get("completedWeight");
        this.stepID = (int) map.get("stepID");
    }
    
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("completedWeight", completedWeight);
        map.put("stepID", stepID);
        
        return map;
    }
    
    public int getStepID() {
        return stepID;
    }
    
    public void setStepID(int stepID) {
        this.stepID = stepID;
    }
    
    public int getCompletedWeight() {
        return completedWeight;
    }
    
    public void setCompletedWeight(int completedWeight) {
        this.completedWeight = completedWeight;
    }
}
