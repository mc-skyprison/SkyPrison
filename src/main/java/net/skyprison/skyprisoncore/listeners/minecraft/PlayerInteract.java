package net.skyprison.skyprisoncore.listeners.minecraft;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class PlayerInteract implements Listener {
    private final SkyPrisonCore plugin;

    public PlayerInteract(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack clickedItem = event.getItem();
        if(clickedItem != null && clickedItem.hasItemMeta() && Objects.equals(event.getHand(), EquipmentSlot.HAND)
                && !clickedItem.getItemMeta().getPersistentDataContainer().isEmpty()
                && (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            Player player = event.getPlayer();
            ItemMeta clickedMeta = clickedItem.getItemMeta();
            PersistentDataContainer clickedPers = clickedMeta.getPersistentDataContainer();
            NamespacedKey expKey = new NamespacedKey(plugin, "exp-amount");
            NamespacedKey voucherKey = new NamespacedKey(plugin, "voucher");
            NamespacedKey gregKey = new NamespacedKey(plugin, "greg");
            if (clickedPers.has(expKey, PersistentDataType.INTEGER)) {
                event.setCancelled(true);
                int expToGive = clickedPers.get(expKey, PersistentDataType.INTEGER);
                player.giveExp(expToGive);
                player.sendMessage(Component.text("+" + plugin.formatNumber(expToGive) + " XP", NamedTextColor.DARK_GREEN, TextDecoration.BOLD));
                player.getInventory().removeItem(clickedItem.asOne());
            } else if (clickedPers.has(voucherKey, PersistentDataType.STRING)) {
                String voucherType = clickedPers.get(voucherKey, PersistentDataType.STRING);
                if(voucherType != null && voucherType.equalsIgnoreCase("single-use-enderchest")) {
                    event.setCancelled(true);
                    player.getInventory().removeItem(clickedItem.asOne());
                    player.openInventory(player.getEnderChest());
                }
            } else if(clickedItem.getType().equals(Material.SPLASH_POTION) && clickedPers.has(gregKey, PersistentDataType.STRING)) {
                String potionData = clickedPers.get(gregKey, PersistentDataType.STRING);
                if(potionData != null && potionData.contains(";")) {
                    String[] types = potionData.split(";"); // works, onYou
                    if(Boolean.parseBoolean(types[0])) {
                        PotionMeta potionMeta = (PotionMeta) clickedItem.getItemMeta();
                        if (Boolean.parseBoolean(types[1])) {
                            player.addPotionEffect(potionMeta.getCustomEffects().get(0));
                            player.sendMessage(Component.text("The bottle breaks in your hands before you throw it.. Ouch!", NamedTextColor.GRAY, TextDecoration.ITALIC));
                            player.getInventory().setItemInMainHand(null);
                            event.setCancelled(true);
                        }
                    } else {
                        player.sendMessage(Component.text("The potion appears to be defective..", NamedTextColor.GRAY, TextDecoration.ITALIC));
                        player.getInventory().setItemInMainHand(null);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
