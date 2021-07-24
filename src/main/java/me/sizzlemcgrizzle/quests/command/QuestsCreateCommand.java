package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class QuestsCreateCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public QuestsCreateCommand(QuestsPlugin plugin) {
        super("quests.admin", plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 2)
            return Collections.singletonList("<name>");
        if (args.length == 3)
            return Collections.singletonList("<permission>");
        
        return Collections.emptyList();
    }
    
    @Override
    protected String execute(CommandSender sender, Command command, String s, String[] args) {
        if (!checkSender(sender)) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You do not have access to this command.");
            return null;
        }
        
        if (args.length < 2) {
            MessageUtil.sendMessage(plugin, sender, MessageLevel.INFO, "You must enter a quest name.");
            return null;
        }
        
        String name, permission = "";
        
        name = args[1];
        
        if (args.length > 2)
            permission = args[2];
        
        plugin.getQuests().add(new Quest(name, permission));
        plugin.getQuestMenu().getPlayerMenus().clear();
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully added new quest. To use this quest, switch visibility to public.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
