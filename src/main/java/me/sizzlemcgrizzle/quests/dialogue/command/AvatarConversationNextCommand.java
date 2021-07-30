package me.sizzlemcgrizzle.quests.dialogue.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import me.sizzlemcgrizzle.quests.dialogue.AvatarConversation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AvatarConversationNextCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public AvatarConversationNextCommand(QuestsPlugin plugin) {
        super("", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 3)
            return Collections.singletonList("<id>");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        if (args.length < 3) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a conversation id.");
            return null;
        }
        
        String id = args[2];
        
        Player player = (Player) sender;
        
        plugin.getQuests().stream().filter(quest -> quest.getProgress().containsKey(player.getUniqueId()))
                .findFirst().ifPresent(quest -> {
            AvatarConversation conversation = quest.getSteps().get(quest.getProgress().get(player.getUniqueId()).getStepID()).getConversation();
            
            if (conversation.getId().equals(id))
                conversation.next(player);
        });
        
        plugin.getNPCConversations().values().stream().filter(a -> a.getId().equals(id)).findFirst().ifPresent(a -> a.next(player));
        plugin.getMythicMobConversations().values().stream().filter(a -> a.getId().equals(id)).findFirst().ifPresent(a -> a.next(player));
        
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
