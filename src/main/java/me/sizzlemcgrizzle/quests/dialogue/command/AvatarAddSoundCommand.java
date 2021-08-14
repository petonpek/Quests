package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.Avatar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AvatarAddSoundCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarAddSoundCommand(QuestsPlugin plugin) {
        super(QuestsPlugin.CREATOR_PERMISSION, plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Utils.getMatches(args[1], plugin.getAvatars().stream().map(Avatar::getName).collect(Collectors.toList()));
        if (args.length == 3)
            return Collections.singletonList("<sound>");
        
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
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a sound.");
            return null;
        }
        
        Optional<Avatar> optional = plugin.getAvatar(args[1]);
        
        if (!optional.isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "The avatar you entered is invalid.");
            return null;
        }
        
        optional.get().getSounds().add(args[2]);
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully added sound.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
