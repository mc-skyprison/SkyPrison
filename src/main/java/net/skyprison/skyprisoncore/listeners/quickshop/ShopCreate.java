package net.skyprison.skyprisoncore.listeners.quickshop;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.event.ShopCreateEvent;

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
                Bukkit.getPlayer(event.getCreator()).sendMessage(plugin.colourMessage("&cMinimum price for this item is $" + minItemPrice + "!"));
                event.setCancelled(true);
            }
        }
    }
}
