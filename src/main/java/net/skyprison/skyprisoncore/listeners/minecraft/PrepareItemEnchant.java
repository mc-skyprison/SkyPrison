package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;

public class PrepareItemEnchant implements Listener {
    private final SkyPrisonCore plugin;
    public PrepareItemEnchant(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();
        if(item.hasItemMeta() && !item.getItemMeta().getPersistentDataContainer().isEmpty()) {
            event.setCancelled(true);
        }
    }
}
