package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Objects;


public class InventoryClose implements Listener {
    private final SkyPrisonCore plugin;

    public InventoryClose(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        ItemStack checkItem = event.getInventory().getItem(0);
        if(checkItem != null) {
            ItemMeta checkMeta = Objects.requireNonNull(checkItem).getItemMeta();
            PersistentDataContainer checkData = checkMeta.getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "stop-click");
            NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
            if (checkData.has(key, PersistentDataType.INTEGER) && checkData.has(key1, PersistentDataType.STRING)) {
                String guiType = checkData.get(key1, PersistentDataType.STRING);
                if ("blacksmith-gui".equalsIgnoreCase(guiType) && event.getInventory().getItem(13) != null) {
                    ItemStack item = event.getInventory().getItem(13);
                    if(item != null) {
                        Player player = (Player) event.getPlayer();
                        HashMap<Integer, ItemStack> inv = player.getInventory().addItem(item);
                        if (!inv.isEmpty()) {
                            for (ItemStack dItem : inv.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), dItem);
                            }
                        }
                    }
                }
            }
        }
    }
}
