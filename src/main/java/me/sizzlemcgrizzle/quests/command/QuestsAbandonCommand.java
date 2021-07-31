package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class QuestsAbandonCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public QuestsAbandonCommand(QuestsPlugin plugin) {
        super("", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        Optional<Quest> optional = plugin.getQuests().stream().filter(q -> q.getProgress().containsKey(((Player) sender).getUniqueId())).findFirst();
        
        if (!optional.isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You are not currently in a quest.");
            return null;
        }
        
        optional.get().getProgress().remove(((Player) sender).getUniqueId());
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Quest abandoned.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
