package net.skyprison.skyprisoncore.listeners;

import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class ShopPreTransaction implements Listener {

    private final SkyPrisonCore plugin;

    public ShopPreTransaction(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopPreTransaction(ShopPreTransactionEvent event) {
        if(event.getShopAction().equals(ShopManager.ShopAction.SELL_ALL)) {
            Player player = event.getPlayer();
            File f = new File(plugin.getDataFolder() + File.separator + "blocksells.yml");
            FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
            if(yamlf.isConfigurationSection(player.getUniqueId().toString())) {
                String iName = event.getShopItem().getItem().getType().name();
                List<String> blockedSales = yamlf.getStringList(player.getUniqueId() + ".blocked");
                if(Objects.requireNonNull(blockedSales).contains(iName)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
