package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.PlayerManager;
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
                if (PlayerManager.isGuardGear(event.getItem().getItemStack())) {
                    event.setCancelled(true);
                }
                PlayerManager.checkGuardGear(player);
            }
        }
    }
}
