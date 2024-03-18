package net.skyprison.skyprisoncore.listeners.quickshop;

import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ShopCreate implements Listener {

    private final SkyPrisonCore plugin;

    public ShopCreate(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopCreate(ShopCreateEvent event) {
        Shop shop = event.getShop();
        Material item = shop.getItem().getType();
        QUser creator = event.getCreator();
        if(!creator.isRealPlayer() || !plugin.minPrice.containsKey(item)) return;

        double minItemPrice = plugin.minPrice.get(item);
        double setPrice = shop.getPrice();
        Player player = creator.getBukkitPlayer().orElse(null);
        if(player == null || setPrice >= minItemPrice) return;

        player.sendMessage(Component.text("Minimum price for this item is $" + minItemPrice + "!", NamedTextColor.RED));
        event.setCancelled(true, "Minimum price for this item is $" + minItemPrice + "!");
    }
}
