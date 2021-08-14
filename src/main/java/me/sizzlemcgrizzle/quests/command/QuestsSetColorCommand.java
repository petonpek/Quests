package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QuestsSetColorCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public QuestsSetColorCommand(QuestsPlugin plugin) {
        super(QuestsPlugin.CREATOR_PERMISSION, plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        
        if (args.length == 2)
            return Utils.getMatches(args[1], plugin.getQuests().stream()
                    .filter(q -> sender.isOp() || (q.isByContentTeam() && sender.hasPermission(QuestsPlugin.CREATOR_PERMISSION)))
                    .map(Quest::getId).collect(Collectors.toList()));
        if (args.length == 3)
            return Utils.getMatches(args[2], Arrays.stream(org.bukkit.ChatColor.values()).map(Enum::name).collect(Collectors.toList()));
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        if (args.length < 3) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a quest name and color.");
            return null;
        }
        
        Optional<Quest> quest = plugin.getQuest(args[1]);
        ChatColor chatColor = ChatColor.of(args[2]);
        
        if (!quest.isPresent()) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "The quest you entered does not exist.");
            return null;
        }
        
        quest.get().setColor(chatColor);
        
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully set quest color.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
