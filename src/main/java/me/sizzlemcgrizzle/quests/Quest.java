package me.sizzlemcgrizzle.quests;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.navigation.NavigationUtil;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import me.sizzlemcgrizzle.quests.menu.QuestStepsMenu;
import me.sizzlemcgrizzle.quests.steps.QuestStep;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Quest implements ConfigurationSerializable {
    
    private final String id;
    private final String permission;
    private final List<String> description;
    private final List<QuestStep> steps;
    private final Map<UUID, Long> completed;
    private final Map<UUID, QuestProgress> progress;
    private ChatColor color;
    private boolean isPublic = false;
    private boolean isRepeatable = false;
    private int timeout = 0;
    private boolean startAutomatically = false;
    
    private QuestStepsMenu menu;
    
    public Quest(String id, ChatColor color, String permission) {
        this.id = id;
        this.permission = permission;
        this.steps = new ArrayList<>();
        this.progress = new HashMap<>();
        this.completed = new HashMap<>();
        this.description = new ArrayList<>();
        this.color = color;
        
        start();
    }
    
    public Quest(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.color = ChatColor.of((String) map.getOrDefault("color", "DARK_PURPLE"));
        this.permission = (String) map.get("permission");
        this.steps = (List<QuestStep>) map.get("steps");
        this.progress = ((Map<String, QuestProgress>) map.get("progress")).entrySet().stream()
                .collect(Collectors.toMap(e -> UUID.fromString(e.getKey()), Map.Entry::getValue));
        this.isPublic = (boolean) map.get("isPublic");
        this.isRepeatable = (boolean) map.get("isRepeatable");
        this.completed = ((Map<String, Long>) map.get("completed")).entrySet().stream()
                .collect(Collectors.toMap(e -> UUID.fromString(e.getKey()), Map.Entry::getValue));
        this.description = (List<String>) map.get("description");
        this.timeout = (int) map.getOrDefault("timeout", 0);
        this.startAutomatically = (boolean) map.getOrDefault("startAutomatically", false);
        
        start();
    }
    
    private void start() {
        new LambdaRunnable(() -> progress.forEach((uuid, p) -> {
            Player player = Bukkit.getPlayer(uuid);
            
            if (player == null)
                return;
            
            if (steps.isEmpty())
                return;
            
            QuestStep step = steps.get(p.getStepID());
            
            String emoji = step.isShowingCompass() && ResourcePackManager.getInstance().isFullyAccepted(player) ? NavigationUtil.getUnicode(player, steps.get(p.getStepID()).getLocation()) : "";
            
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(emoji + ChatColor.GOLD + " " + steps.get(p.getStepID()).getCompassDescription(player)));
        })).runTaskTimer(QuestsPlugin.getInstance(), 0, 3);
    }
    
    public void abandon(UUID uuid, boolean withTimeout) {
        
        steps.get(progress.get(uuid).getStepID()).getConversation().getConversing().remove(uuid);
        
        progress.remove(uuid);
        
        if (withTimeout)
            completed.put(uuid, System.currentTimeMillis());
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("id", id);
        map.put("permission", permission);
        map.put("color", color.getName());
        map.put("steps", steps);
        map.put("progress", progress.entrySet().stream().collect(Collectors.toMap(a -> a.getKey().toString(), Map.Entry::getValue)));
        map.put("isPublic", isPublic);
        map.put("isRepeatable", isRepeatable);
        map.put("completed", completed.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
        map.put("description", description);
        map.put("timeout", timeout);
        map.put("startAutomatically", startAutomatically);
        
        return map;
    }
    
    public String getId() {
        return id;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public void setColor(ChatColor color) {
        this.color = color;
    }
    
    public boolean playerHasPermission(Player player) {
        return permission.equals("") || player.hasPermission(permission);
    }
    
    public List<QuestStep> getSteps() {
        return steps;
    }
    
    public Map<UUID, QuestProgress> getProgress() {
        return progress;
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
    
    public boolean isRepeatable() {
        return isRepeatable;
    }
    
    public void setRepeatable(boolean repeatable) {
        isRepeatable = repeatable;
    }
    
    public boolean hasCompleted(Player player) {
        return completed.containsKey(player.getUniqueId());
    }
    
    public List<String> getDescription() {
        return description;
    }
    
    public boolean startsAutomatically() {
        return startAutomatically;
    }
    
    public void setStartsAutomatically(boolean startAutomatically) {
        this.startAutomatically = startAutomatically;
    }
    
    public void completeStep(Player player) {
        QuestProgress p = progress.get(player.getUniqueId());
        
        int nextStepID = p.getStepID() + 1;
        
        if (nextStepID == steps.size()) {
            progress.remove(player.getUniqueId());
            
            completed.put(player.getUniqueId(), System.currentTimeMillis());
            
            QuestsPlugin.getInstance().getQuestMenu().getPlayerMenus().remove(player.getUniqueId());
            
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + "Quest complete!"));
            return;
        }
        
        p.setCompletedWeight(0);
        p.setStepID(nextStepID);
    }
    
    public boolean canStartQuest(Player player) {
        
        if (QuestsPlugin.getInstance().getQuests().stream().anyMatch(p -> p.getProgress().containsKey(player.getUniqueId())))
            return false;
        
        if (!isPublic())
            return false;
        
        if (!isRepeatable() && completed.containsKey(player.getUniqueId()))
            return false;
        
        if (isRepeatable() && completed.getOrDefault(player.getUniqueId(), 0L) + timeout * 1000L > System.currentTimeMillis())
            return false;
        
        return playerHasPermission(player);
    }
    
    public boolean canStartQuest(Player player, QuestStep step) {
        return startsAutomatically() && steps.get(0).equals(step) && canStartQuest(player);
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    
    public void start(Player player) {
        if (steps.isEmpty())
            return;
        
        progress.put(player.getUniqueId(), new QuestProgress());
        QuestsPlugin.getInstance().getQuestMenu().getPlayerMenus().remove(player.getUniqueId());
    }
    
    public QuestStepsMenu getMenu() {
        if (menu == null)
            menu = new QuestStepsMenu(QuestsPlugin.getInstance(), this);
        
        return menu;
    }
}
