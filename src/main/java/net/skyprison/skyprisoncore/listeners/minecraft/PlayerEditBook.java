package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;

public class PlayerEditBook implements Listener {
    private final SkyPrisonCore plugin;
    public PlayerEditBook(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();
        BookMeta bookMeta = event.getNewBookMeta();
        List<Component> pages = bookMeta.pages();
        int i = 1;
        for(Component page : pages) {
            String pageString = MiniMessage.miniMessage().serialize(page);
            bookMeta.page(i, plugin.getParsedString(player, "book", pageString));
            i++;
        }
        event.setNewBookMeta(bookMeta);
    }
}
