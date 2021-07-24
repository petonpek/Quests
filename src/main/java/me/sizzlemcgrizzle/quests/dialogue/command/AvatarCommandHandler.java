package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.CommandHandler;
import me.sizzlemcgrizzle.quests.QuestsPlugin;

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
}
