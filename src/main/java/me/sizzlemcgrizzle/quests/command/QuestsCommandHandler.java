package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.command.CommandHandler;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestsCommandHandler extends CommandHandler {
    
    private QuestsPlugin plugin;
    
    public QuestsCommandHandler(QuestsPlugin plugin) {
        super(plugin);
        
        this.plugin = plugin;
        
        registerSubCommand("create", new QuestsCreateCommand(plugin));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        if (sender instanceof Player && (!sender.hasPermission("quests.admin") || args.length == 0))
            plugin.getQuestMenu().display((Player) sender);
        
        return super.onCommand(sender, cmd, label, args);
    }
}
