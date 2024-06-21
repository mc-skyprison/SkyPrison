package net.skyprison.skyprisoncore.listeners.plugins;


import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopManager;
import net.alex9849.arm.adapters.WGRegion;
import net.alex9849.arm.events.UnsellRegionEvent;
import net.alex9849.arm.regions.Region;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class AdvancedRegionMarketListeners implements Listener {
    @EventHandler
    public void onUnsellRegion(UnsellRegionEvent event) {
        Region region = event.getRegion();
        World world = region.getRegionworld();
        if(!world.getName().equalsIgnoreCase("world_skycity")) return;

        WGRegion wgRegion = region.getRegion();
        QuickShopAPI qs = QuickShopAPI.getInstance();
        ShopManager shopManager = qs.getShopManager();

        List<Shop> shops = shopManager.getAllShops(region.getOwner()).stream().filter(shop -> {
            Location loc = shop.getLocation();
            return wgRegion.contains(loc.blockX(), loc.blockY(), loc.blockZ());
        }).toList();

        shops.forEach(shopManager::deleteShop);
    }
}
