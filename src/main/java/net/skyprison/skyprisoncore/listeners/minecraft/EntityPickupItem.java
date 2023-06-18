package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupItem implements Listener {
    private final SkyPrisonCore plugin;

    public EntityPickupItem(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
                if (plugin.isGuardGear(event.getItem().getItemStack())) {
                    event.setCancelled(true);
                }
                plugin.InvGuardGearDelPlyr(player);
            }
        }
    }
}
