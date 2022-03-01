package net.skyprison.skyprisoncore.listeners;

import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;

public class BrewDrink implements Listener {
    private SkyPrisonCore plugin;

    public BrewDrink(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBrewDrink(BrewDrinkEvent event) throws IOException {
        Player player = event.getPlayer();
        File brewData = new File(plugin.getDataFolder() + File.separator + "brewsdrank.yml");
        FileConfiguration brewConf = YamlConfiguration.loadConfiguration(brewData);
        int totalBrews = 0;
        if(brewConf.contains(player.getUniqueId().toString())) {
            totalBrews = brewConf.getInt(player.getUniqueId().toString());
        }
        brewConf.set(player.getUniqueId().toString(), totalBrews + 1);
        brewConf.save(brewData);
    }
}
