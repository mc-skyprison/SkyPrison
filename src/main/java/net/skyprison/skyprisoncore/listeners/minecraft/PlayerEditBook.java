package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        if(event.isSigning()) {
            Player player = event.getPlayer();
            if(player.hasPermission("skyprisoncore.book.colours")) {
                BookMeta bookMeta = event.getNewBookMeta();
                List<Component> pages = bookMeta.pages();
                int i = 1;
                for(Component page : pages) {
                    String pageString = PlainTextComponentSerializer.plainText().serialize(page);
                    Component serialized = plugin.playerMsgBuilder.deserialize(pageString);
                    bookMeta.page(i, serialized);
                    i++;
                }
                event.setNewBookMeta(bookMeta);
            }
        }
    }
}
