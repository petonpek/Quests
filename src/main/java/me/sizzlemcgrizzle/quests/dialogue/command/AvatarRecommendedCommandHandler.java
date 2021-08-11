package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.SubCommandHandler;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.CommandSender;

public class AvatarRecommendedCommandHandler extends SubCommandHandler {
    public AvatarRecommendedCommandHandler(QuestsPlugin plugin) {
        super("clcore.admin", plugin, false, 2);
        
        registerSubCommand("edit", new AvatarRecommendedEditCommand(plugin));
        registerSubCommand("complete", new AvatarRecommendedCompleteCommand(plugin));
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
