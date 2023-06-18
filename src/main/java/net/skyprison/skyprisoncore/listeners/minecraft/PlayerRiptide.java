package net.skyprison.skyprisoncore.listeners.minecraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;

public class PlayerRiptide implements Listener {
    @EventHandler
    public void playerRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
            Location loc = player.getLocation();
            player.teleportAsync(loc);
        }
    }
}
