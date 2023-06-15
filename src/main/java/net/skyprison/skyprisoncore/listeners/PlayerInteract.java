package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class PlayerInteract implements Listener {
    private final SkyPrisonCore plugin;

    public PlayerInteract(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().getType().equals(Material.EXPERIENCE_BOTTLE) && Objects.equals(event.getHand(), EquipmentSlot.HAND)) {
            if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                ItemStack expBottle = event.getItem();
                ItemMeta expMeta = expBottle.getItemMeta();
                NamespacedKey key = new NamespacedKey(plugin, "exp-amount");
                if (expMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    event.setCancelled(true);
                    int expToGive = expMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    Player player = event.getPlayer();
                    player.giveExp(expToGive);
                    player.sendMessage(plugin.colourMessage("&2&l+" + plugin.formatNumber(expToGive)) + " XP");
                    if (expBottle.getAmount() - 1 > 0) {
                        expBottle.setAmount(expBottle.getAmount() - 1);
                    } else {
                        player.getInventory().removeItem(expBottle);
                    }
                }
            }
        }
    }
}
