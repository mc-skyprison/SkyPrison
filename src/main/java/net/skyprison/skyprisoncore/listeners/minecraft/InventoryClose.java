package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.inventories.smith.BlacksmithTrimmer;
import net.skyprison.skyprisoncore.inventories.smith.EndBlacksmithUpgrade;
import net.skyprison.skyprisoncore.inventories.smith.GrassBlacksmithUpgrade;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;


public class InventoryClose implements Listener {
    public InventoryClose() {}
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().getHolder(false) instanceof CustomInventory customInv) {
            HumanEntity player = event.getPlayer();
            Inventory invent = customInv.getInventory();
            switch (customInv) {
                case GrassBlacksmithUpgrade inv -> {
                    inv.cancelTimer();
                    PlayerManager.giveItems(player, invent.getItem(10), invent.getItem(16));
                }
                case BlacksmithTrimmer inv -> {
                    inv.cancelTimer();
                    PlayerManager.giveItems(player, invent.getItem(10), invent.getItem(11), invent.getItem(12));
                }
                case EndBlacksmithUpgrade inv -> {
                    inv.cancelTimer();
                    PlayerManager.giveItems(player, invent.getItem(10), invent.getItem(11), invent.getItem(12),
                            invent.getItem(13), invent.getItem(14));
                }
                default -> {
                }
            }
        }
    }
}
