package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.inventories.BlacksmithTrimmer;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.inventories.EndBlacksmithUpgrade;
import net.skyprison.skyprisoncore.inventories.GrassBlacksmithUpgrade;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


public class InventoryClose implements Listener {
    public InventoryClose() {}
    private void returnItems(HumanEntity player, ItemStack... items) {
        PlayerInventory pInv = player.getInventory();
        World world = player.getWorld();
        Location loc = player.getLocation();
        HashMap<Integer, ItemStack> didntFit = pInv.addItem(Arrays.stream(items).filter(Objects::nonNull).toArray(ItemStack[]::new));
        for (ItemStack item : didntFit.values()) {
            world.dropItemNaturally(loc, item).setOwner(player.getUniqueId());
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().getHolder(false) instanceof CustomInventory customInv) {
            if (customInv instanceof GrassBlacksmithUpgrade inv) {
                inv.cancelTimer();
                returnItems(event.getPlayer(), inv.getInventory().getItem(10), inv.getInventory().getItem(16));
            } else if (customInv instanceof BlacksmithTrimmer inv) {
                inv.cancelTimer();
                returnItems(event.getPlayer(), inv.getInventory().getItem(10), inv.getInventory().getItem(11), inv.getInventory().getItem(12));
            } else if (customInv instanceof EndBlacksmithUpgrade inv) {
                inv.cancelTimer();
                returnItems(event.getPlayer(), inv.getInventory().getItem(10), inv.getInventory().getItem(11), inv.getInventory().getItem(12),
                        inv.getInventory().getItem(13), inv.getInventory().getItem(14));
            }
        }
    }
}
