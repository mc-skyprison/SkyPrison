package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.List;

public class CMIPlayerTeleportRequest implements Listener {

    private SkyPrisonCore plugin;

    public CMIPlayerTeleportRequest(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCMIPlayerTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        File ignoreData = new File(plugin.getDataFolder() + File.separator + "teleportignore.yml");
        FileConfiguration ignoreConf = YamlConfiguration.loadConfiguration(ignoreData);
        Player askedPlayer = event.getWhoAccepts();
        Player askingPlayer = event.getWhoOffers();
        if(ignoreConf.isConfigurationSection(askedPlayer.getUniqueId().toString())) {
            List<?> ignoredPlayers = ignoreConf.getList(askedPlayer.getUniqueId() + ".ignores");
            assert ignoredPlayers != null;
            if(ignoredPlayers.contains(askingPlayer.getUniqueId().toString())) {
                askingPlayer.sendMessage(plugin.colourMessage(askedPlayer.displayName() + "&eis ignoring you!"));
                event.setCancelled(true);
            }
        }
    }
}
