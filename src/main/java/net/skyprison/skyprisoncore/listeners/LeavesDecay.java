package net.skyprison.skyprisoncore.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

public class LeavesDecay implements Listener {
    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if(event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("world_prison")) {
            if(event.getBlock().getType() == Material.BIRCH_LEAVES) {
                if (Math.random() < 0.025) {
                    ItemStack apple = new ItemStack(Material.APPLE, 1);
                    event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), apple);
                }
            }
        }
    }
}
