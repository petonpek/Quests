package me.sizzlemcgrizzle.quests.command;

import de.craftlancer.core.Utils;
import de.craftlancer.core.command.CommandUtils;
import de.craftlancer.core.command.SubCommand;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuestsCreateCommand extends SubCommand {
    
    private QuestsPlugin plugin;
    
    public QuestsCreateCommand(QuestsPlugin plugin) {
        super(QuestsPlugin.CREATOR_PERMISSION, plugin, false);
        
        this.plugin = plugin;
    }
    
    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        
        args = CommandUtils.parseArgumentStrings(args);
        
        if (args.length == 2)
            return Collections.singletonList("<name>");
        if (args.length == 3)
            return Utils.getMatches(args[2], Arrays.stream(org.bukkit.ChatColor.values()).map(Enum::name).collect(Collectors.toList()));
        if (args.length == 4)
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
        
        Player player = (Player) sender;
        String name, permission = "";
        ChatColor chatColor = ChatColor.DARK_PURPLE;
        
        name = args[1];
        
        if (args.length > 2)
            chatColor = ChatColor.of(args[2]);
        
        if (args.length > 3)
            permission = args[3];
        
        plugin.getQuests().add(new Quest(name, !player.hasPermission(QuestsPlugin.ADMIN_PERMISSION) && player.hasPermission(QuestsPlugin.CREATOR_PERMISSION), chatColor, permission));
        plugin.getQuestMenu().getPlayerMenus().clear();
        
        MessageUtil.sendMessage(plugin, sender, MessageLevel.SUCCESS, "Successfully added new quest. To use this quest, switch visibility to public.");
        return null;
    }
    
    @Override
    public void help(CommandSender commandSender) {
    
    }
}
