package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.event.ShopPurchaseEvent;

import java.io.File;
import java.util.ArrayList;

public class ShopPurchase implements Listener {

    private SkyPrisonCore plugin;

    public ShopPurchase(SkyPrisonCore plugin) {
            this.plugin = plugin;
        }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent event) {
        File f = new File(plugin.getDataFolder() + File.separator + "shopban.yml");
        FileConfiguration shopConf = YamlConfiguration.loadConfiguration(f);
        if(shopConf.isConfigurationSection(event.getShop().getOwner().toString())) {
            ArrayList<String> bannedPlayers = (ArrayList<String>) shopConf.getStringList(event.getShop().getOwner() + ".banned-players");
            if(bannedPlayers.contains(event.getPurchaser().toString())) {
                Bukkit.getPlayer(event.getPurchaser()).sendMessage(plugin.colourMessage("&cThis player has banned you from their shops!"));
                event.setCancelled(true);
            }
        }
    }

}
