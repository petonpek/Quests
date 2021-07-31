package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.command.CommandUtils;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class QuestsCommandHandler extends CommandHandler {
    
    public QuestsCommandHandler(QuestsPlugin plugin) {
        super(plugin);
        
        registerSubCommand("edit", new QuestsEditCommand(plugin));
        registerSubCommand("create", new QuestsCreateCommand(plugin));
        registerSubCommand("setColor", new QuestsSetColorCommand(plugin));
        registerSubCommand("abandon", new QuestsAbandonCommand(plugin));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return super.onTabComplete(sender, cmd, label, CommandUtils.parseArgumentStrings(args));
    }
}
