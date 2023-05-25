package net.skyprison.skyprisoncore.inventories;

import org.bukkit.Material;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public interface CustomInventory extends InventoryHolder {
    ClickBehavior defaultClickBehavior();
    List<Object> customClickList();

    int getPage();

    default boolean isCustomClick(Material mat) {
        return customClickList().contains(mat);
    }
    default boolean isCustomClick(int slot) {
        return customClickList().contains(slot);
    }
}
