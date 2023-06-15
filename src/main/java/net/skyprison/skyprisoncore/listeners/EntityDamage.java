package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.io.File;


public class EntityDamage implements Listener {

    private final SkyPrisonCore plugin;

    public EntityDamage(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(event.getEntity() instanceof Player player) {
            if(player.getWorld().getName().equalsIgnoreCase("world_skyplots") && event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                event.setCancelled(true);
                File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
                FileConfiguration fData = YamlConfiguration.loadConfiguration(f);
                double x = fData.getDouble(player.getUniqueId() + ".x");
                double y = fData.getDouble(player.getUniqueId() + ".y");
                double z = fData.getDouble(player.getUniqueId() + ".z");
                Location loc = new Location(Bukkit.getWorld("world_skyplots"), x, y, z);
                player.teleport(loc);
                player.sendMessage(plugin.colourMessage("&aSkyPlots &f&l>> &eYou've been teleported to your SkyPlot!"));
            }
        }
    }
}
