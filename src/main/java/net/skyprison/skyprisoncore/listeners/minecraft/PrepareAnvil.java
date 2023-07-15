package net.skyprison.skyprisoncore.listeners.minecraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class PrepareAnvil implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack left = event.getInventory().getFirstItem();
        ItemStack right = event.getInventory().getSecondItem();
        if(left != null && left.hasItemMeta() && !left.getItemMeta().getPersistentDataContainer().isEmpty()) {
            event.setResult(null);
            return;
        }
        if(right != null && right.hasItemMeta() && !right.getItemMeta().getPersistentDataContainer().isEmpty()) {
            event.setResult(null);
        }
    }
}
