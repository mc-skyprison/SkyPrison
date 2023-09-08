package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.mail.MailBoxSend;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
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
        if(event.isSigning() && plugin.writingMail.containsKey(player.getUniqueId())) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            book.setItemMeta(event.getNewBookMeta());
            book.editMeta(meta -> {
               meta.lore(null);
               meta.displayName(null);
            });
            MailBoxSend inv = plugin.writingMail.get(player.getUniqueId());
            plugin.getServer().getScheduler().runTask(plugin, () -> player.getInventory().setItemInOffHand(inv.getOffHand()));
            inv.sendMail(book);
        }
        BookMeta bookMeta = event.getNewBookMeta();
        List<Component> pages = bookMeta.pages();
        int i = 1;
        for(Component page : pages) {
            String pageString = MiniMessage.miniMessage().serialize(page);
            pageString = pageString.replaceAll("\\\\<", "<");
            Component newPage = plugin.getParsedString(player, "book", pageString);
            bookMeta.page(i, newPage);
            i++;
        }
        event.setNewBookMeta(bookMeta);
    }
}
