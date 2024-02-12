package net.skyprison.skyprisoncore.inventories;

import org.bukkit.inventory.InventoryHolder;

public interface CustomInventory extends InventoryHolder {
    default int page() {
        return 1;
    }
}
