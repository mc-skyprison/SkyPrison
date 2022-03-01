package net.skyprison.skyprisoncore.listeners;

import com.google.common.collect.Lists;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ShopPostTransaction implements Listener {
    private SkyPrisonCore plugin;

    public ShopPostTransaction(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) throws IOException {
        if(event.getResult().getShopAction() == ShopManager.ShopAction.SELL
                || event.getResult().getShopAction() == ShopManager.ShopAction.SELL_ALL) {
            Player player = event.getResult().getPlayer();
            File f = new File(plugin.getDataFolder() + File.separator + "recentsells.yml");
            FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
            if(yamlf.isConfigurationSection(player.getUniqueId().toString())) {
                ArrayList<String> soldItems = (ArrayList<String>) Objects.requireNonNull(yamlf.getStringList(player.getUniqueId() + ".sold-items"));
                soldItems.add(0, event.getResult().getShopItem().getItem().getType()
                        + "/" + event.getResult().getAmount()
                        + "/" + event.getResult().getPrice());
                if(soldItems.size() > 5) {
                    soldItems.remove(5);
                }
                yamlf.set(player.getUniqueId() + ".sold-items", soldItems);
            } else {
                yamlf.set(player.getUniqueId() + ".sold-items", Lists.newArrayList(event.getResult().getShopItem().getItem().getType()
                        + "/" + event.getResult().getAmount()
                        + "/" + event.getResult().getPrice()));
            }
            yamlf.save(f);
        }
    }
}
