package net.skyprison.skyprisoncore.listeners.minecraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

public class LeavesDecay implements Listener {
    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if(!world.getName().equalsIgnoreCase("world_prison") || !block.getType().equals(Material.BIRCH_LEAVES) || !(Math.random() < 0.025)) return;

        world.dropItem(loc, new ItemStack(Material.APPLE));
    }
}
