package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;

public class PlayerQuit implements Listener {

    private SkyPrisonCore plugin;
    private DatabaseHook db;

    public PlayerQuit(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String pUUID = event.getPlayer().getUniqueId().toString();

            File tokenMine = new File(plugin.getDataFolder() + File.separator
                    + "blocksmined.yml");
            FileConfiguration mineConf = YamlConfiguration.loadConfiguration(tokenMine);
            mineConf.set(pUUID, plugin.blockBreaks.get(pUUID));
            plugin.blockBreaks.remove(pUUID);

            File tData = new File(plugin.getDataFolder() + File.separator + "tokensdata.yml");
            FileConfiguration tokenConf = YamlConfiguration.loadConfiguration(tData);
            tokenConf.set("players." + pUUID, plugin.tokensData.get(pUUID));
            plugin.tokensData.remove(pUUID);
            try {
                mineConf.save(tokenMine);
                tokenConf.save(tData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
