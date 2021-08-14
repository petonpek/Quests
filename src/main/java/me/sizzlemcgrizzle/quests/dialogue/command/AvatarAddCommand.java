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

public class AvatarAddCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarAddCommand(QuestsPlugin plugin) {
        super(QuestsPlugin.ADMIN_PERMISSION, plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Collections.singletonList("<name>");
        if (args.length == 3)
            return Collections.singletonList("<unicode>");
        
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
        
        if (args.length < 3) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a unicode.");
            return null;
        }
        
        if (plugin.getAvatars().stream().anyMatch(a -> a.getName().equalsIgnoreCase(args[1]))) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "The avatar id you entered is already in use.");
            return null;
        }
        
        
        plugin.getAvatars().add(new Avatar(args[1], args[2]));
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully added avatar.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
