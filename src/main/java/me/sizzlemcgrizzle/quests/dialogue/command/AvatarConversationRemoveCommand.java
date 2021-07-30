package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.AvatarConversation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AvatarConversationRemoveCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarConversationRemoveCommand(QuestsPlugin plugin) {
        super("quests.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3)
            return Utils.getMatches(args[2], Arrays.asList("citizens", "mythicmobs"));
        if ((args[2].equalsIgnoreCase("citizens") || args[2].equalsIgnoreCase("mythicmobs")) && args.length == 4)
            return Utils.getMatches(args[3],
                    (args[2].equalsIgnoreCase("citizens") ? QuestsPlugin.getInstance().getNPCConversations().values()
                            : QuestsPlugin.getInstance().getMythicMobConversations().values()).stream().map(AvatarConversation::getId).collect(Collectors.toList()));
        
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
        
        if (args[2].equalsIgnoreCase("citizens")) {
            QuestsPlugin.getInstance().getNPCConversations().entrySet().removeIf(e -> e.getValue().getId().equalsIgnoreCase(args[3]));
        } else
            QuestsPlugin.getInstance().getMythicMobConversations().entrySet().removeIf(e -> e.getValue().getId().equalsIgnoreCase(args[3]));
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Removed all assigned conversations with the id you entered.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
