package me.sizzlemcgrizzle.quests;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.navigation.NavigationUtil;
import de.craftlancer.core.resourcepack.ResourcePackManager;
import de.craftlancer.core.util.BossBarMessageRegistrable;
import de.craftlancer.core.util.MessageUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.sizzlemcgrizzle.quests.command.QuestsCommandHandler;
import me.sizzlemcgrizzle.quests.dialogue.AssignedConversation;
import me.sizzlemcgrizzle.quests.dialogue.Avatar;
import me.sizzlemcgrizzle.quests.dialogue.AvatarConversation;
import me.sizzlemcgrizzle.quests.dialogue.AvatarMessage;
import me.sizzlemcgrizzle.quests.dialogue.RecommendedConversation;
import me.sizzlemcgrizzle.quests.dialogue.command.AvatarCommandHandler;
import me.sizzlemcgrizzle.quests.menu.QuestsOverviewMenu;
import me.sizzlemcgrizzle.quests.steps.*;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestsPlugin extends JavaPlugin implements Listener, BossBarMessageRegistrable {
    
    public static String CREATOR_PERMISSION = "quests.content";
    public static String ADMIN_PERMISSION = "quests.admin";
    
    private static QuestsPlugin instance;
    
    private List<Quest> quests;
    private List<Avatar> avatars;
    
    private Map<MythicMob, AssignedConversation> mythicMobConversations = new HashMap<>();
    private Map<Integer, AssignedConversation> npcConversations = new HashMap<>();
    
    private QuestsOverviewMenu questMenu;
    private UserInputManager userInputManager;
    private RecommendedConversation recommendedConversation;
    
    public static QuestsPlugin getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        
        ConfigurationSerialization.registerClass(Quest.class);
        ConfigurationSerialization.registerClass(QuestProgress.class);
        ConfigurationSerialization.registerClass(QuestReward.class);
        ConfigurationSerialization.registerClass(QuestStep.class);
        ConfigurationSerialization.registerClass(QuestStepBlockInteract.class);
        ConfigurationSerialization.registerClass(QuestStepNPCInteraction.class);
        ConfigurationSerialization.registerClass(QuestStepMythicMobKill.class);
        ConfigurationSerialization.registerClass(QuestStepWorldGuardAction.class);
        ConfigurationSerialization.registerClass(QuestStepMythicMobInteraction.class);
        ConfigurationSerialization.registerClass(QuestStepAdminShopTrade.class);
        ConfigurationSerialization.registerClass(QuestStepBlueprintPlace.class);
        ConfigurationSerialization.registerClass(QuestStepPortalUse.class);
        ConfigurationSerialization.registerClass(Avatar.class);
        ConfigurationSerialization.registerClass(AvatarMessage.class);
        ConfigurationSerialization.registerClass(AvatarConversation.class);
        ConfigurationSerialization.registerClass(AssignedConversation.class);
        ConfigurationSerialization.registerClass(RecommendedConversation.class);
        ConfigurationSerialization.registerClass(QuestStepConditionalAbandon.class);
        
        MessageUtil.register(this, new TextComponent("§8[§cQuests§8]"));
        
        getCommand("quests").setExecutor(new QuestsCommandHandler(this));
        getCommand("avatar").setExecutor(new AvatarCommandHandler(this));
        
        File questsFile = new File(getDataFolder(), "quests.yml");
        
        if (!questsFile.exists())
            saveResource(questsFile.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(questsFile);
        
        avatars = (List<Avatar>) config.get("avatars", new ArrayList<>());
        quests = (List<Quest>) config.get("quests", new ArrayList<>());
        recommendedConversation = (RecommendedConversation) config.get("recommendedConversation", new RecommendedConversation("recommended"));
        
        if (config.getConfigurationSection("mythicMobConversations") != null)
            mythicMobConversations = config.getConfigurationSection("mythicMobConversations").getValues(false).entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> MythicMobs.inst().getMobManager().getMythicMob(e.getKey()),
                            e -> (AssignedConversation) e.getValue()));
        if (config.getConfigurationSection("npcConversations") != null)
            npcConversations = config.getConfigurationSection("npcConversations").getValues(false).entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> Integer.valueOf(e.getKey()),
                            e -> (AssignedConversation) e.getValue()));
        
        questMenu = new QuestsOverviewMenu(this);
        userInputManager = new UserInputManager(this);
        
        Bukkit.getPluginManager().registerEvents(this, this);
        
        new LambdaRunnable(this::save).runTaskTimer(this, 36000, 36000);

        new LambdaRunnable(() -> quests.forEach(quest ->
                quest.getProgress().forEach((uuid, p) -> {

            Player player = Bukkit.getPlayer(uuid);

            if (player == null)
                return;

            if (quest.getSteps().isEmpty())
                return;

            QuestStep step = quest.getSteps().get(p.getStepID());

            String emoji = step.isShowingCompass() && ResourcePackManager.getInstance().isFullyAccepted(player) ? NavigationUtil.getUnicode(player, step.getLocation()) : "";
            
            MessageUtil.sendBossBarMessage(this,player, emoji + ChatColor.GOLD + " " + step.getCompassDescription(player));
        }))).runTaskTimer(QuestsPlugin.getInstance(), 0, 3);
    }
    
    @Override
    public void onDisable() {
        save();
    }
    
    private void save() {
        File questsFile = new File(getDataFolder(), "quests.yml");
        
        if (!questsFile.exists())
            saveResource(questsFile.getName(), false);
        
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("quests", quests);
        config.set("avatars", avatars);
        config.set("recommendedConversation", recommendedConversation);
        
        config.set("mythicMobConversations", mythicMobConversations.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getInternalName(), Map.Entry::getValue)));
        config.set("npcConversations", npcConversations.entrySet().stream().collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue)));
        
        BukkitRunnable saveTask = new LambdaRunnable(() -> {
            try {
                config.save(questsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        if (isEnabled())
            saveTask.runTaskAsynchronously(this);
        else
            saveTask.run();
    }
    
    public UserInputManager getUserInputManager() {
        return userInputManager;
    }
    
    public RecommendedConversation getRecommendedConversation() {
        return recommendedConversation;
    }
    
    public QuestsOverviewMenu getQuestMenu() {
        return questMenu;
    }
    
    public Optional<Quest> getQuest(String id) {
        return quests.stream().filter(q -> q.getId().equals(id)).findFirst();
    }
    
    public List<Quest> getQuests() {
        return quests;
    }
    
    public List<Avatar> getAvatars() {
        return avatars;
    }
    
    public Optional<Avatar> getAvatar(String name) {
        return avatars.stream().filter(a -> a.getName().equalsIgnoreCase(name)).findFirst();
    }
    
    public Map<MythicMob, AssignedConversation> getMythicMobConversations() {
        return mythicMobConversations;
    }
    
    public Map<Integer, AssignedConversation> getNPCConversations() {
        return npcConversations;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        ActiveMob active = MythicMobs.inst().getAPIHelper().getMythicMobInstance(event.getRightClicked());
        
        if (active == null)
            return;
        
        AssignedConversation convo = mythicMobConversations.get(active.getType());
        
        if (convo == null)
            return;
        
        if (player.hasPermission(QuestsPlugin.ADMIN_PERMISSION) && player.isSneaking())
            convo.display(player);
        else
            convo.next(player);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(NPCRightClickEvent event) {
        Player player = event.getClicker();
        
        AssignedConversation convo = npcConversations.get(event.getNPC().getId());
        
        if (convo == null)
            return;
        
        if (player.hasPermission(QuestsPlugin.ADMIN_PERMISSION) && player.isSneaking())
            convo.display(player);
        else
            convo.next(player);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        new LambdaRunnable(() -> recommendedConversation.next(event.getPlayer())).runTaskLater(this, 20);
    }
    
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        recommendedConversation.getConversing().remove(event.getPlayer().getUniqueId());
    }
    
    @Override
    public String getBossBarId() {
        return "quests";
    }
}
