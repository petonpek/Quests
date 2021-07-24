package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.Menu;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestProgress;
import me.sizzlemcgrizzle.quests.QuestReward;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.AvatarConversation;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class QuestStep implements ConfigurationSerializable, Listener {
    
    private final String questName;
    private final QuestReward reward;
    
    private Location location;
    private Quest quest;
    private Menu configurationMenu;
    
    private String compassDescription = "";
    private int totalWeight;
    
    private AvatarConversation conversation;
    
    public QuestStep(Quest quest, Location location) {
        this(quest, location, 1);
    }
    
    public QuestStep(Quest quest, Location location, int weight) {
        this.questName = quest.getId();
        this.location = location;
        this.totalWeight = weight;
        this.reward = new QuestReward();
        this.conversation = new AvatarConversation(UUID.randomUUID().toString(), player -> {
            getQuest().completeStep(player);
            reward.reward(player);
        });
        
        Bukkit.getPluginManager().registerEvents(this, QuestsPlugin.getInstance());
    }
    
    public QuestStep(Map<String, Object> map) {
        this.location = (Location) map.get("location");
        this.questName = (String) map.get("quest");
        this.reward = (QuestReward) map.getOrDefault("reward", new QuestReward());
        this.totalWeight = (int) map.getOrDefault("totalWeight", 1);
        this.compassDescription = (String) map.getOrDefault("compassDescription", "");
        this.conversation = (AvatarConversation) map.get("conversation");
        this.conversation.setOnComplete(player -> {
            getQuest().completeStep(player);
            reward.reward(player);
        });
        
        Bukkit.getPluginManager().registerEvents(this, QuestsPlugin.getInstance());
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("location", location);
        map.put("quest", questName);
        map.put("reward", reward);
        map.put("totalWeight", totalWeight);
        map.put("compassDescription", compassDescription);
        map.put("conversation", conversation);
        
        return map;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public boolean isPlayerOnStep(Player player) {
        if (!getQuest().getProgress().containsKey(player.getUniqueId()))
            return false;
        
        return getQuest().getProgress().get(player.getUniqueId()).getStepID() == getQuest().getSteps().indexOf(this);
    }
    
    public void onStepAction(Player player, int weight) {
        QuestProgress progress = getQuest().getProgress().get(player.getUniqueId());
        
        progress.setCompletedWeight(progress.getCompletedWeight() + weight);
        
        if (progress.getCompletedWeight() >= getTotalWeight())
            conversation.next(player);
    }
    
    public String getCompassDescription() {
        return compassDescription;
    }
    
    public void setCompassDescription(String compassDescription) {
        this.compassDescription = ChatColor.translateAlternateColorCodes('&', compassDescription);
    }
    
    public Quest getQuest() {
        if (quest == null)
            quest = QuestsPlugin.getInstance().getQuest(questName);
        
        return quest;
    }
    
    public int getTotalWeight() {
        return totalWeight;
    }
    
    public void setTotalWeight(int totalWeight) {
        this.totalWeight = totalWeight;
    }
    
    protected abstract Material getMenuMaterial();
    
    protected abstract List<MenuItem> getConfigurationButtons();
    
    public ItemStack getMenuItem() {
        ItemBuilder builder = new ItemBuilder(getMenuMaterial());
        
        builder.addLore("");
        if (location != null)
            builder.addLore("&7Location: &6(" + ((int) getLocation().getX()) + ", " + ((int) getLocation().getY()) + ", " + ((int) getLocation().getZ()) + ")");
        builder.addLore("&7Total weight: &6" + totalWeight, "");
        
        return builder.build();
    }
    
    public void display(Player player, Quest quest) {
        if (configurationMenu == null)
            createMenu(quest);
        
        player.openInventory(configurationMenu.getInventory());
    }
    
    public void createMenu(Quest quest) {
        List<MenuItem> buttons = getConfigurationButtons();
        
        int rows = ((buttons.size() + 6) / 10) + 1;
        
        this.configurationMenu = new Menu(QuestsPlugin.getInstance(), "Step Configuration", rows);
        
        MenuItem locationButton = new MenuItem(new ItemBuilder(Material.STONE_BRICKS).setDisplayName("&e&lChange Location")
                .setLore("", "&7Location: &6(" + ((int) getLocation().getX()) + ", " + ((int) getLocation().getY()) + ", " + ((int) getLocation().getZ()) + ")",
                        "", "&8→ &6Click to set location").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            
            player.closeInventory();
            MessageUtil.sendMessage(QuestsPlugin.getInstance(), player, MessageLevel.INFO, "Right click a block to set step location.");
            
            QuestsPlugin.getInstance().getUserInputManager()
                    .getBlockInput(player, block -> {
                        setLocation(block.getLocation());
                        
                        createMenu(quest);
                        display(player, quest);
                    });
        });
        
        MenuItem weightButton = new MenuItem(new ItemBuilder(Material.IRON_BLOCK).setDisplayName("&e&lSet Total Weight")
                .setLore("", "&7Total weight: &6" + totalWeight, "", "&8→ &6Click to set total weight").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            QuestsPlugin.getInstance().getUserInputManager()
                    .getInput(player, new UserInputManager.NumberInputPrompt("&bEnter a number...", d -> d >= 1, d -> {
                        setTotalWeight(d.intValue());
                        
                        createMenu(quest);
                        display(player, quest);
                    }, () -> display(player, quest)));
        });
        
        MenuItem rewardButton = new MenuItem(new ItemBuilder(Material.EMERALD).setDisplayName("&e&lEdit Reward")
                .setLore("", "&8→ &6Click to edit reward").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            reward.display(player, this);
        });
        
        MenuItem compassButton = new MenuItem(new ItemBuilder(Material.COMPASS).setDisplayName("&e&lSet Direction Description")
                .setLore("", "&7Current information: &6" + compassDescription, "",
                        "&8→ &6Click to set direction description").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            QuestsPlugin.getInstance().getUserInputManager()
                    .getInput(player, new UserInputManager.StringInputPrompt("&bEnter a description...", s -> true, s -> {
                        setCompassDescription(s);
                        
                        createMenu(quest);
                        display(player, quest);
                    }, () -> display(player, quest)));
        });
        
        MenuItem conversationButton = new MenuItem(new ItemBuilder(Material.SPRUCE_SIGN).setDisplayName("&e&lEdit Conversation")
                .setLore("", "&8→ &6Click to edit conversation").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            conversation.display(player, this);
        });
        
        configurationMenu.set(0, locationButton);
        configurationMenu.set(1, weightButton);
        configurationMenu.set(2, rewardButton);
        configurationMenu.set(3, compassButton);
        configurationMenu.set(4, conversationButton);
        
        int slot = 5;
        for (MenuItem item : buttons) {
            configurationMenu.set(slot, item);
            slot++;
        }
        
        int exitSlot = (rows * 9) - 1;
        
        MenuItem exitButton = new MenuItem(new ItemBuilder(Material.BARRIER).setDisplayName("&e&lGo back")
                .setLore("", "&8→ &6Click to return to quest step menu").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            quest.getMenu().display(player);
        });
        configurationMenu.set(exitSlot, exitButton);
    }
    
    public AvatarConversation getConversation() {
        return conversation;
    }
}
