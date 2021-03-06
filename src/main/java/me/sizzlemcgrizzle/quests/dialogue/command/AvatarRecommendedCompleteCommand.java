package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AvatarRecommendedCompleteCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarRecommendedCompleteCommand(QuestsPlugin plugin) {
        super("", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        plugin.getRecommendedConversation().displayQuestMenu((Player) sender);
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
