package me.sizzlemcgrizzle.quests.dialogue;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.Map;

public class RecommendedConversation extends AssignedConversation {
    public RecommendedConversation(String id) {
        super(id);
        
        setOnComplete(null);
    }
    
    public RecommendedConversation(Map<String, Object> map) {
        super(map);
        
        setOnComplete(null);
    }
    
    @Override
    public BaseComponent[] getLastMessageButton() {
        return new ComponentBuilder("Show Me").color(ChatColor.AQUA).bold(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click here to see quests...").color(ChatColor.AQUA).create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/avatar conversation recommended complete"))
                .create();
    }
}
