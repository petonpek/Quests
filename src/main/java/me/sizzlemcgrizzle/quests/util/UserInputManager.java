package me.sizzlemcgrizzle.quests.util;

import de.craftlancer.clfeatures.CLFeatures;
import de.craftlancer.clfeatures.portal.PortalFeatureInstance;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class UserInputManager implements Listener {
    
    private final QuestsPlugin plugin;
    private final Map<UUID, Consumer<NPC>> inputMapNPC = new HashMap<>();
    private final Map<UUID, Consumer<Block>> inputMapBlock = new HashMap<>();
    private final Map<UUID, Consumer<PortalFeatureInstance>> inputMapPortal = new HashMap<>();
    private final Map<UUID, BiConsumer<MythicMob, ActiveMob>> inputMapMythicMob = new HashMap<>();
    
    public UserInputManager(QuestsPlugin plugin) {
        this.plugin = plugin;
        
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onNPCInteract(NPCRightClickEvent event) {
        Optional.ofNullable(inputMapNPC.remove(event.getClicker().getUniqueId())).ifPresent(c -> {
            event.setCancelled(true);
            c.accept(event.getNPC());
        });
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;
        
        if (inputMapPortal.containsKey(event.getPlayer().getUniqueId())) {
            CLFeatures.getInstance().getFeature("portal").getFeatures().stream()
                    .filter(f -> f.getStructure().containsBlock(event.getClickedBlock()))
                    .findFirst()
                    .ifPresent(portalFeatureInstance -> inputMapPortal.remove(event.getPlayer().getUniqueId())
                            .accept((PortalFeatureInstance) portalFeatureInstance));
        }
        
        Optional.ofNullable(inputMapBlock.remove(event.getPlayer().getUniqueId())).ifPresent(c -> {
            c.accept(event.getClickedBlock());
            event.setCancelled(true);
        });
    }
    
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        if (!inputMapMythicMob.containsKey(player.getUniqueId()))
            return;
        
        ActiveMob active = MythicMobs.inst().getAPIHelper().getMythicMobInstance(event.getRightClicked());
        
        if (active == null)
            return;
        
        Optional.ofNullable(inputMapMythicMob.remove(player.getUniqueId())).ifPresent(c -> {
            c.accept(active.getType(), active);
            event.setCancelled(true);
        });
    }
    
    public void getNPCInput(Player player, Consumer<NPC> consumer) {
        inputMapNPC.put(player.getUniqueId(), consumer);
    }
    
    public void getBlockInput(Player player, Consumer<Block> consumer) {
        inputMapBlock.put(player.getUniqueId(), consumer);
    }
    
    public void getPortalInput(Player player, Consumer<PortalFeatureInstance> consumer) {
        inputMapPortal.put(player.getUniqueId(), consumer);
    }
    
    public void getMythicMobInput(Player player, BiConsumer<MythicMob, ActiveMob> consumer) {
        inputMapMythicMob.put(player.getUniqueId(), consumer);
    }
    
    public void getInput(Player player, StringPrompt prompt) {
        new ConversationFactory(plugin)
                .withFirstPrompt(prompt)
                .withLocalEcho(false)
                .withModality(true)
                .buildConversation(player)
                .begin();
    }
    
    /**
     * @param <I> Typically string, given as an input to get an Optional of R.
     *            This is useful to reduce the amount of streams used to
     *            find a certain value.
     */
    public interface ObjectValidation<I, R> {
        Optional<R> call(I i);
    }
    
    public static class StringInputPrompt extends StringPrompt {
        
        private final String prompt;
        private final Consumer<String> onAccept;
        private final Runnable onExit;
        private final Predicate<String> validation;
        
        public StringInputPrompt(String prompt, @Nullable Predicate<String> validation, Consumer<String> onAccept) {
            this(prompt, validation, onAccept, null);
        }
        
        public StringInputPrompt(String prompt, @Nullable Predicate<String> validation, Consumer<String> onAccept, Runnable onExit) {
            this.prompt = ChatColor.translateAlternateColorCodes('&', prompt);
            this.validation = validation;
            this.onAccept = onAccept;
            this.onExit = onExit;
        }
        
        @Override
        public @Nonnull
        String getPromptText(@Nonnull ConversationContext conversationContext) {
            return prompt + " Enter '&&' to exit prompt.";
        }
        
        @Override
        public @Nullable
        Prompt acceptInput(@Nonnull ConversationContext conversationContext, @Nullable String s) {
            Player player = (Player) conversationContext.getForWhom();
            
            if (s == null)
                return this;
            
            s = ChatColor.translateAlternateColorCodes('&', s);
            
            if (s.equals("&&")) {
                player.sendMessage(ChatColor.GREEN + "Successfully exited prompt.");
                if (onExit != null)
                    onExit.run();
            } else {
                if (validation != null)
                    if (!validation.test(s))
                        return this;
                
                player.sendMessage(ChatColor.GREEN + "Successfully saved input.");
                onAccept.accept(s);
            }
            
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    public static class ObjectInputPrompt<R> extends StringPrompt {
        
        private final String prompt;
        private final Consumer<R> onAccept;
        private final Runnable onExit;
        private final ObjectValidation<String, R> validation;
        
        public ObjectInputPrompt(String prompt, ObjectValidation<String, R> validation, Consumer<R> onAccept) {
            this(prompt, validation, onAccept, null);
        }
        
        public ObjectInputPrompt(String prompt, ObjectValidation<String, R> validation, Consumer<R> onAccept, Runnable onExit) {
            this.prompt = ChatColor.translateAlternateColorCodes('&', prompt);
            this.validation = validation;
            this.onAccept = onAccept;
            this.onExit = onExit;
        }
        
        @Override
        public @Nonnull
        String getPromptText(@Nonnull ConversationContext conversationContext) {
            return ChatColor.AQUA + prompt + " Enter '&&' to exit prompt.";
        }
        
        @Override
        public @Nullable
        Prompt acceptInput(@Nonnull ConversationContext conversationContext, @Nullable String s) {
            Player player = (Player) conversationContext.getForWhom();
            if (s == null)
                return this;
            else if (s.equals("&&")) {
                player.sendMessage(ChatColor.GREEN + "Successfully exited prompt.");
                if (onExit != null)
                    onExit.run();
            } else {
                Optional<R> r = validation.call(s);
                
                if (!r.isPresent())
                    return this;
                
                player.sendMessage(ChatColor.GREEN + "Successfully saved input.");
                onAccept.accept(r.get());
            }
            
            return Prompt.END_OF_CONVERSATION;
        }
    }
    
    public static class NumberInputPrompt extends StringPrompt {
        
        private final String prompt;
        private final Consumer<Double> onAccept;
        private final Runnable onExit;
        private final Predicate<Double> validation;
        
        public NumberInputPrompt(String prompt, @Nullable Predicate<Double> validation, Consumer<Double> onAccept) {
            this(prompt, validation, onAccept, null);
        }
        
        public NumberInputPrompt(String prompt, @Nullable Predicate<Double> validation, Consumer<Double> onAccept, @Nullable Runnable onExit) {
            this.prompt = ChatColor.translateAlternateColorCodes('&', prompt);
            this.validation = validation;
            this.onAccept = onAccept;
            this.onExit = onExit;
        }
        
        @Override
        public @Nonnull
        String getPromptText(@Nonnull ConversationContext conversationContext) {
            return prompt + " Enter '&&' to exit prompt.";
        }
        
        @Override
        public @Nullable
        Prompt acceptInput(@Nonnull ConversationContext conversationContext, @Nullable String s) {
            Player player = (Player) conversationContext.getForWhom();
            if (s == null)
                return this;
            else if (s.equals("&&")) {
                player.sendMessage(ChatColor.GREEN + "Successfully exited prompt.");
                if (onExit != null)
                    onExit.run();
            } else {
                double i;
                try {
                    i = Double.parseDouble(s);
                } catch (NumberFormatException e) {
                    return this;
                }
                
                if (validation != null)
                    if (!validation.test(i))
                        return this;
                
                onAccept.accept(i);
            }
            
            return Prompt.END_OF_CONVERSATION;
        }
    }
}
