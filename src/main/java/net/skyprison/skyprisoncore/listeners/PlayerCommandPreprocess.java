package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocess implements Listener {
    private final SkyPrisonCore plugin;

    public PlayerCommandPreprocess(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(event.isCancelled() && event.getPlayer().getWorld().getName().equalsIgnoreCase("world_prison")) {
            event.getPlayer().sendMessage(plugin.colourMessage("&cYou can't use this command in this area!"));
        }
    }
}
