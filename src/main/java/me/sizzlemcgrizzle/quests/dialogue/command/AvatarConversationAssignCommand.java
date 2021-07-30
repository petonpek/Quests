package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.AssignedConversation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AvatarConversationAssignCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarConversationAssignCommand(QuestsPlugin plugin) {
        super("quests.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3)
            return Utils.getMatches(args[2], Arrays.asList("citizens", "mythicmobs"));
        if (args.length == 4)
            return Collections.singletonList("<id>");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        if (args.length < 3 || (!args[2].equalsIgnoreCase("citizens") && !args[2].equalsIgnoreCase("mythicmobs"))) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter 'citizens' or 'mythic mobs'.'");
            return null;
        }
        
        if (args.length < 4) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter an id.");
            return null;
        }
        
        if (plugin.getNPCConversations().values().stream().anyMatch(c -> c.getId().equalsIgnoreCase(args[3]))
                || plugin.getMythicMobConversations().values().stream().anyMatch(c -> c.getId().equalsIgnoreCase(args[3]))) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "The id you entered already exists.");
            return null;
        }
        
        Player player = (Player) sender;
        
        if (args[2].equalsIgnoreCase("citizens")) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "Right click an NPC.");
            plugin.getUserInputManager().getNPCInput(player, npc -> {
                AssignedConversation convo = new AssignedConversation(args[3],
                        (npc.getEntity() instanceof LivingEntity) ? ((LivingEntity) npc.getEntity()).getEyeLocation() : npc.getEntity().getLocation());
                plugin.getNPCConversations().put(npc.getId(), convo);
                convo.display(player);
            });
        }
        
        if (args[2].equalsIgnoreCase("mythicmobs")) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "Right click a mythic mob.");
            plugin.getUserInputManager().getMythicMobInput(player, (mm, active) -> {
                Entity bukkitEntity = active.getEntity().getBukkitEntity();
                AssignedConversation convo = new AssignedConversation(args[3],
                        bukkitEntity instanceof LivingEntity ? ((LivingEntity) bukkitEntity).getEyeLocation() : bukkitEntity.getLocation());
                plugin.getMythicMobConversations().put(mm, convo);
                convo.display(player);
            });
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
