package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocess implements Listener {
    public PlayerCommandPreprocess() {}
    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if(event.isCancelled() && event.getPlayer().getWorld().getName().equalsIgnoreCase("world_prison") && !event.getPlayer().isOp()) {
            event.getPlayer().sendMessage(Component.text("You can't use this command in this area!", NamedTextColor.RED));
        }
    }
}
