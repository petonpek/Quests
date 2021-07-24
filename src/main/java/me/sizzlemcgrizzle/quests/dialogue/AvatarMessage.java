package me.sizzlemcgrizzle.quests.dialogue;

import de.craftlancer.core.resourcepack.TranslateSpaceFont;
import me.sizzlemcgrizzle.quests.QuestsPlugin;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvatarMessage implements ConfigurationSerializable {
    
    private static int maxCharPerLine = 50;
    
    //Length should be 6
    private List<String> lines;
    //2 <= x <= 4
    private int lastTextLine = 2;
    private String avatarName;
    private Avatar avatar;
    
    public AvatarMessage(Avatar avatar, String message) {
        this.avatar = avatar;
        format(message);
    }
    
    public AvatarMessage(Map<String, Object> map) {
        this.avatarName = map.get("avatar").toString();
        this.lines = (List<String>) map.get("lines");
        this.lastTextLine = (int) map.getOrDefault("lastTextLine", 2);
    }
    
    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("avatar", avatarName);
        map.put("lines", lines);
        map.put("lastTextLine", lastTextLine);
        
        return map;
    }
    
    public void format(String message) {
        String[] words = message.split(" ");
        String[] lines = new String[6];
        
        lines[0] = TranslateSpaceFont.getSpecificAmount(0) + ChatColor.WHITE + "\uEA01" + TranslateSpaceFont.getSpecificAmount(-48) + getAvatar().getUnicode(); //Set unicode of avatar
        lines[1] = TranslateSpaceFont.getSpecificAmount(58) + ChatColor.WHITE + "" + ChatColor.BOLD + avatar.getName(); //Show name of avatar
        lines[2] = TranslateSpaceFont.getSpecificAmount(58) + ChatColor.GRAY;
        lines[3] = TranslateSpaceFont.getSpecificAmount(58) + ChatColor.GRAY;
        lines[4] = TranslateSpaceFont.getSpecificAmount(58) + ChatColor.GRAY;
        lines[5] = TranslateSpaceFont.getSpecificAmount(58) + ChatColor.GRAY;
        
        int counter = 2;
        //Assign words less than 50 char in total per line.
        while (counter < 5 && words.length > 0) {
            
            int usedWords = 0;
            for (String s : words)
                if (lines[counter].length() + s.length() < maxCharPerLine) {
                    lines[counter] += s + " ";
                    lastTextLine = counter;
                    usedWords++;
                } else
                    break;
            
            words = Arrays.copyOfRange(words, usedWords, words.length);
            counter++;
        }
        
        //Add the rest of the words if there are any
        for (String s : words)
            lines[4] = lines[4] + s;
        
        this.lines = Arrays.asList(lines);
    }
    
    public List<String> getLines() {
        return lines;
    }
    
    public int getLastTextLine() {
        return lastTextLine;
    }
    
    public Avatar getAvatar() {
        if (avatar == null)
            avatar = QuestsPlugin.getInstance().getAvatars().stream().filter(a -> a.getName().equals(avatarName)).findFirst().get();
        
        return avatar;
    }
}
