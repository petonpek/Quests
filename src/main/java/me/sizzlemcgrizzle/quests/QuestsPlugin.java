package me.sizzlemcgrizzle.quests;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.command.QuestsCommandHandler;
import me.sizzlemcgrizzle.quests.dialogue.Avatar;
import me.sizzlemcgrizzle.quests.dialogue.AvatarConversation;
import me.sizzlemcgrizzle.quests.dialogue.AvatarMessage;
import me.sizzlemcgrizzle.quests.dialogue.command.AvatarCommandHandler;
import me.sizzlemcgrizzle.quests.menu.QuestsOverviewMenu;
import me.sizzlemcgrizzle.quests.steps.QuestStep;
import me.sizzlemcgrizzle.quests.steps.QuestStepAdminShopTrade;
import me.sizzlemcgrizzle.quests.steps.QuestStepBlockInteract;
import me.sizzlemcgrizzle.quests.steps.QuestStepMythicMobInteraction;
import me.sizzlemcgrizzle.quests.steps.QuestStepMythicMobKill;
import me.sizzlemcgrizzle.quests.steps.QuestStepNPCInteraction;
import me.sizzlemcgrizzle.quests.steps.QuestStepWorldGuardAction;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestsPlugin extends JavaPlugin {
    private static QuestsPlugin instance;
    
    private List<Quest> quests;
    private List<Avatar> avatars;
    
    private QuestsOverviewMenu questMenu;
    private UserInputManager userInputManager;
    
    public static QuestsPlugin getInstance() {
        return instance;
    }
    
    @Override
    public void onEnable() {
        instance = this;
        
        ConfigurationSerialization.registerClass(Quest.class);
        ConfigurationSerialization.registerClass(QuestProgress.class);
        ConfigurationSerialization.registerClass(QuestStep.class);
        ConfigurationSerialization.registerClass(QuestStepBlockInteract.class);
        ConfigurationSerialization.registerClass(QuestStepNPCInteraction.class);
        ConfigurationSerialization.registerClass(QuestStepMythicMobKill.class);
        ConfigurationSerialization.registerClass(QuestStepWorldGuardAction.class);
        ConfigurationSerialization.registerClass(QuestStepMythicMobInteraction.class);
        ConfigurationSerialization.registerClass(Avatar.class);
        ConfigurationSerialization.registerClass(AvatarMessage.class);
        ConfigurationSerialization.registerClass(AvatarConversation.class);
        ConfigurationSerialization.registerClass(QuestReward.class);
        ConfigurationSerialization.registerClass(QuestStepAdminShopTrade.class);
        
        MessageUtil.register(this, new TextComponent("§8[§eQuests§8]"));
        
        getCommand("quests").setExecutor(new QuestsCommandHandler(this));
        getCommand("avatar").setExecutor(new AvatarCommandHandler(this));
        
        File questsFile = new File(getDataFolder(), "quests.yml");
        
        if (!questsFile.exists())
            saveResource(questsFile.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(questsFile);
        
        avatars = (List<Avatar>) config.get("avatars", new ArrayList<>());
        quests = (List<Quest>) config.get("quests", new ArrayList<>());
        
        questMenu = new QuestsOverviewMenu(this);
        userInputManager = new UserInputManager(this);
    }
    
    @Override
    public void onDisable() {
        File questsFile = new File(getDataFolder(), "quests.yml");
        
        if (!questsFile.exists())
            saveResource(questsFile.getName(), false);
        
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("quests", quests);
        config.set("avatars", avatars);
        
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
    
    public QuestsOverviewMenu getQuestMenu() {
        return questMenu;
    }
    
    public Quest getQuest(String id) {
        return quests.stream().filter(q -> q.getId().equals(id)).findFirst().orElse(null);
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
}
