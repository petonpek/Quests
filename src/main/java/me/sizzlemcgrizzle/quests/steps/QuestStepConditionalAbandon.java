package me.sizzlemcgrizzle.quests.steps;

import de.craftlancer.core.menu.MenuItem;
import de.craftlancer.core.util.ItemBuilder;
import me.sizzlemcgrizzle.quests.Quest;
import me.sizzlemcgrizzle.quests.QuestProgress;
import me.sizzlemcgrizzle.quests.dialogue.AvatarConversation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QuestStepConditionalAbandon extends QuestStepItem implements InstantComplete{

    private AvatarConversation abandonConversation;

    private boolean withTimeout = false;

    public QuestStepConditionalAbandon(Quest quest, Location location, ItemStack item) {
        super(quest, location, item);

        this.abandonConversation = new AvatarConversation(UUID.randomUUID().toString(), player -> {});

        setShowCompass(false);
    }

    public QuestStepConditionalAbandon(Map<String, Object> map) {
        super(map);

        setShowCompass(false);
    }

    @Override
    protected Material getMenuMaterial() {
        return Material.REDSTONE_BLOCK;
    }

    @Override
    public ItemStack getMenuItem() {
        return new ItemBuilder(super.getMenuItem()).setDisplayName("&e&lConditional Abandon").build();
    }

    @Override
    public void onStepAction(Player player, int weight) {
        QuestProgress progress = getQuest().getProgress().get(player.getUniqueId());

        progress.setCompletedWeight(progress.getCompletedWeight() + weight);

        if (progress.getCompletedWeight() >= getTotalWeight())
            if (takeItems(player))
                getConversation().next(player);
            else {
                abandonConversation.next(player);
                getQuest().abandon(player.getUniqueId(),withTimeout);
            }
    }

    @Override
    protected List<MenuItem> getConfigurationButtons() {
        List<MenuItem> list = super.getConfigurationButtons();

        list.remove(3);
        list.remove(1);
        list.remove(0);

        list.add(new MenuItem(new ItemBuilder(Material.DARK_OAK_SIGN).setDisplayName("&e&lEdit Conversation")
                .setLore("", "&8→ &6Click to edit conversation").build()).addClickAction(click -> {
            Player player = click.getPlayer();
            abandonConversation.display(player);
        }));

        list.add(new MenuItem(new ItemBuilder(Material.CLOCK).setDisplayName("&e&lWith Timeout")
                .addLore("", "&7With timeout: " + (withTimeout ? "&eyes" : "&cno"),
                        "", "&8→ &6Click to toggle timeout").build())
                .addClickAction(click -> {
                    Player player = click.getPlayer();

                    withTimeout = !withTimeout;

                    createMenu();
                    display(player);
                }));

        return list;
    }
}
