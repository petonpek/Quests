package me.sizzlemcgrizzle.quests.dialogue;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.menu.PagedMenu;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.util.UserInputManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AvatarConversation implements ConfigurationSerializable {
    
    private final List<AvatarMessage> messages;
    private final String id;
    private final Map<UUID, Integer> conversing = new HashMap<>();
    private final List<UUID> onCooldown = new ArrayList<>();
    private List<BaseComponent[]> formattedMessages;
    private Consumer<Player> onComplete;
    private PagedMenu menu;
    
    public AvatarConversation(String id) {
        this(id, null);
    }
    
    public AvatarConversation(String id, Consumer<Player> consumer) {
        this.id = id;
        this.messages = new ArrayList<>();
        this.onComplete = consumer;
    }
    
    public AvatarConversation(Map<String, Object> map) {
        this.id = map.get("id").toString();
        this.messages = (List<AvatarMessage>) map.get("messages");
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("id", id);
        map.put("messages", messages);
        
        return map;
    }
    
    public String getId() {
        return id;
    }
    
    public void next(Player player) {
        if (onCooldown.contains(player.getUniqueId()))
            return;
        
        if (messages.size() == 0) {
            complete(player);
            return;
        }
        
        int i = conversing.getOrDefault(player.getUniqueId(), 0);
        
        if (formattedMessages == null || formattedMessages.size() == 0)
            format();
        
        player.spigot().sendMessage(formattedMessages.get(i));
        messages.get(i).getAvatar().playSound(player);
        
        if (i + 1 == messages.size()) {
            complete(player);
            return;
        }
        
        conversing.put(player.getUniqueId(), i + 1);
    }
    
    public Consumer<Player> getOnComplete() {
        return onComplete;
    }
    
    public void setOnComplete(Consumer<Player> onComplete) {
        this.onComplete = onComplete;
    }
    
    public void complete(Player player) {
        if (onComplete != null)
            onComplete.accept(player);
        getOnCooldown().add(player.getUniqueId());
        getConversing().remove(player.getUniqueId());
        
        new LambdaRunnable(() -> getOnCooldown().remove(player.getUniqueId())).runTaskLater(QuestsPlugin.getInstance(), getCooldown());
    }
    
    public List<UUID> getOnCooldown() {
        return onCooldown;
    }
    
    private void format() {
        int counter = 0;
        formattedMessages = new ArrayList<>();
        
        for (AvatarMessage m : messages) {
            List<String> lines = new ArrayList<>(m.getLines());
            
            if (messages.size() > 1)
                lines.set(1, lines.get(1) + ChatColor.DARK_GRAY + " (" + (counter + 1) + "/" + messages.size() + ")");
            
            ComponentBuilder builder = new ComponentBuilder();
            
            for (int i = 0; i < 6; i++) {
                if (i != m.getLastTextLine())
                    builder.append(lines.get(i)).append("\n");
                else if (counter == messages.size() - 1)
                    builder.append(lines.get(i)).append(" ").append(getLastMessageButton()).append("\n");
                else {
                    BaseComponent c = new TextComponent("Next →");
                    
                    c.setColor(ChatColor.GOLD);
                    c.setBold(true);
                    c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/avatar conversation next " + id));
                    c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§bClick to continue...").create()));
                    
                    builder.append(lines.get(i) + " ").append(c).append("\n");
                }
                
            }
            
            formattedMessages.add(builder.create());
            counter++;
        }
    }
    
    /**
     * Similar to the Next -> button, but shown on the very last
     * conversation message. Could be used with GUIs.
     */
    public BaseComponent[] getLastMessageButton() {
        return new BaseComponent[]{new TextComponent("")};
    }
    
    
    public void add(AvatarMessage message) {
        add(messages.size(), message);
    }
    
    public void add(int index, AvatarMessage message) {
        messages.add(index, message);
        
        format();
    }
    
    public void remove(int index) {
        messages.remove(index);
        
        format();
    }
    
    public void remove(AvatarMessage m) {
        messages.remove(m);
        
        format();
    }
    
    public int size() {
        return messages.size();
    }
    
    
    public void display(Player player) {
        if (menu == null)
            createMenu();
        
        menu.display(player);
    }
    
    protected PagedMenu getConversationMenu() {
        return menu;
    }
    
    protected int getCooldown() {
        return 0;
    }
    
    public Map<UUID, Integer> getConversing() {
        return conversing;
    }
    
    protected void createMenu() {
        int[] index = {0};
        this.menu = new PagedMenu(QuestsPlugin.getInstance(), "Conversation Editor", true, 4, messages.stream().map(m -> {
            int val = index[0]++;
            return new MenuItem(new ItemBuilder(Material.PLAYER_HEAD).setDisplayName("&d&lMessage Preview").setLore(m.getLines())
                    .addLore("", "&8→ &6Left click to set message", "&8→ &6Right click to add message to the left", "&8→ &6Shift right click to remove").build())
                    .addClickAction(click -> {
                        Player player = click.getPlayer();
                        
                        QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a message less than 150 characters...",
                                s -> s.length() <= 150,
                                s -> {
                                    m.format(s);
                                    format();
                                    
                                    createMenu();
                                    display(player);
                                }, () -> display(player)));
                    }, ClickType.LEFT)
                    .addClickAction(click -> {
                        Player player = click.getPlayer();
                        player.closeInventory();
                        
                        QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.ObjectInputPrompt<>("&bEnter a valid avatar name to use...",
                                string -> QuestsPlugin.getInstance().getAvatars().stream().filter(a -> a.getName().equalsIgnoreCase(string)).findFirst(),
                                avatar -> {
                                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a message less than 150 characters...",
                                            s -> s.length() <= 150,
                                            s -> {
                                                add(val, new AvatarMessage(avatar, s));
                                                
                                                createMenu();
                                                display(player);
                                            }, () -> display(player)));
                                }, () -> display(player)));
                    }, ClickType.RIGHT)
                    .addClickAction(click -> {
                        Player player = click.getPlayer();
                        
                        remove(m);
                        createMenu();
                        display(player);
                    }, ClickType.SHIFT_RIGHT);
        }).collect(Collectors.toList()), true);
        
        MenuItem infoItem = new MenuItem(new ItemBuilder(Material.DIAMOND_BLOCK).setDisplayName("&6&lConversation Editor")
                .addLore("&8→ &6Left click to add message").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();
                    player.closeInventory();
                    
                    QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.ObjectInputPrompt<>("&bEnter a valid avatar name to use...",
                            string -> QuestsPlugin.getInstance().getAvatars().stream().filter(a -> a.getName().equalsIgnoreCase(string)).findFirst(),
                            avatar -> {
                                QuestsPlugin.getInstance().getUserInputManager().getInput(player, new UserInputManager.StringInputPrompt("&bEnter a message less than 150 characters...",
                                        s -> s.length() <= 150,
                                        s -> {
                                            add(new AvatarMessage(avatar, s));
                                            
                                            createMenu();
                                            display(player);
                                        }, () -> display(player)));
                            }, () -> display(player)));
                }, ClickType.LEFT);
        
        menu.setInfoItem(infoItem);
    }
}
