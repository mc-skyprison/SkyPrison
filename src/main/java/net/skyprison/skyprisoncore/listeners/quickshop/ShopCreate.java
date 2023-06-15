package net.skyprison.skyprisoncore.listeners.quickshop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.event.ShopCreateEvent;

import java.util.Objects;

public class ShopCreate implements Listener {

    private final SkyPrisonCore plugin;

    public ShopCreate(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopCreate(ShopCreateEvent event) {
        Material itemMaterial = event.getShop().getItem().getType();
        if(plugin.minPrice.containsKey(itemMaterial)) {
            double minItemPrice = plugin.minPrice.get(itemMaterial);
            double setPrice = event.getShop().getPrice();
            if(setPrice < minItemPrice) {
                Objects.requireNonNull(Bukkit.getPlayer(event.getCreator())).sendMessage(Component.text("Minimum price for this item is $" + minItemPrice + "!", NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }
}
