package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsEditCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public QuestsEditCommand(QuestsPlugin plugin) {
        super("quests.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        plugin.getQuestMenu().display((Player) sender);
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
