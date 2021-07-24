package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.Avatar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AvatarRemoveCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarRemoveCommand(QuestsPlugin plugin) {
        super("quests.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return plugin.getAvatars().stream().map(Avatar::getName).collect(Collectors.toList());
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        if (args.length < 2) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter an id.");
            return null;
        }
        
        if (plugin.getAvatars().removeIf(a -> a.getName().equalsIgnoreCase(args[1])))
            MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully added avatar.");
        else
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "The avatar id you entered is invalid.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
