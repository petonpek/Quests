package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.SubCommandHandler;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.CommandSender;

public class AvatarConversationCommandHandler extends SubCommandHandler {
    public AvatarConversationCommandHandler(QuestsPlugin plugin) {
        super("quests.admin", plugin, false, 1);
        
        registerSubCommand("next", new AvatarConversationNextCommand(plugin));
        registerSubCommand("assign", new AvatarConversationAssignCommand(plugin));
        registerSubCommand("remove", new AvatarConversationRemoveCommand(plugin));
        registerSubCommand("recommended", new AvatarRecommendedCommandHandler(plugin));
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
