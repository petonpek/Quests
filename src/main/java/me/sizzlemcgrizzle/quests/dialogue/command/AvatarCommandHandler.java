package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.CommandHandler;
import de.craftlancer.core.command.CommandUtils;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AvatarCommandHandler extends CommandHandler {
    public AvatarCommandHandler(QuestsPlugin plugin) {
        super(plugin);
        
        registerSubCommand("add", new AvatarAddCommand(plugin));
        registerSubCommand("remove", new AvatarRemoveCommand(plugin));
        registerSubCommand("addSound", new AvatarAddSoundCommand(plugin));
        registerSubCommand("removeSound", new AvatarRemoveSoundCommand(plugin));
        registerSubCommand("setGender", new AvatarSetGenderCommand(plugin));
        registerSubCommand("conversation", new AvatarConversationCommandHandler(plugin));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return super.onTabComplete(sender, cmd, label, CommandUtils.parseArgumentStrings(args));
    }
}
