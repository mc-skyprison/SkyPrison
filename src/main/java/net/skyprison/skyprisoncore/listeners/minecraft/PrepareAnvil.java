package net.skyprison.skyprisoncore.listeners.minecraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import java.util.Arrays;

public class PrepareAnvil implements Listener {
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if(Arrays.stream(event.getInventory().getContents()).anyMatch(item -> item != null && item.hasItemMeta() && !item.getItemMeta().getPersistentDataContainer().isEmpty())) {
            event.setResult(null);
        }
    }
}
