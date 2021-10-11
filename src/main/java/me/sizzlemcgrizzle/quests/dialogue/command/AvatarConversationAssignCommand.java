package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.AssignedConversation;
import me.sizzlemcgrizzle.quests.dialogue.wrapper.MythicMobWrapper;
import me.sizzlemcgrizzle.quests.dialogue.wrapper.NPCWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AvatarConversationAssignCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarConversationAssignCommand(QuestsPlugin plugin) {
        super(QuestsPlugin.CREATOR_PERMISSION, plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3)
            return Utils.getMatches(args[2], Arrays.asList("citizens", "mythicmobs"));
        
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
        
        Player player = (Player) sender;
        
        if (args[2].equalsIgnoreCase("citizens")) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "Right click an NPC.");
            plugin.getUserInputManager().getNPCInput(player, npc -> {
                if (plugin.getWrappers().stream()
                        .anyMatch(w -> w instanceof NPCWrapper && ((NPCWrapper) w).getId() == npc.getId()))
                    MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "A conversation is already assigned to this NPC.");
                else {
                    NPCWrapper wrapper = new NPCWrapper(npc.getId(),npc.getEntity().getLocation());
                    plugin.getWrappers().add(wrapper);
                    wrapper.getConversation().display(player);
                }
            });
        }
        
        if (args[2].equalsIgnoreCase("mythicmobs")) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "Right click a mythic mob.");
            plugin.getUserInputManager().getMythicMobInput(player, (mm, active) -> {
                if (plugin.getWrappers().stream()
                        .anyMatch(w -> w instanceof MythicMobWrapper && ((MythicMobWrapper) w).getMob().equals(mm)))
                    MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "A conversation is already assigned to this Mythic Mob.");
                else {
                    MythicMobWrapper wrapper = new MythicMobWrapper(mm, active.getEntity().getBukkitEntity().getLocation());
                    plugin.getWrappers().add(wrapper);
                    wrapper.getConversation().display(player);
                }
            });
        }
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
