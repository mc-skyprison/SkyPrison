package net.skyprison.skyprisoncore.listeners.minecraft;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.MailBoxSettings;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class PlayerInteract implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public PlayerInteract(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }
    public boolean isMember(Player player, int mailBox) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM mail_boxes_users WHERE mailbox_id = ? AND user_id = ?")) {
            ps.setInt(1, mailBox);
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean isOwner(Player player, int mailBox) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT owner_id FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UUID ownerId = UUID.fromString(rs.getString(1));
                return player.getUniqueId().equals(ownerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(block != null && block.getWorld().getName().equalsIgnoreCase("world_free") && action.equals(Action.LEFT_CLICK_BLOCK)
        && block.getType().equals(Material.CHEST)) {
            int mailBox =  plugin.getMailBox(block);
            if(mailBox != -1) {
                if(isMember(player, mailBox)) {
                    player.openInventory(new MailBoxSettings(plugin, db, mailBox, isOwner(player, mailBox), player).getInventory());
                    return;
                }
            }
        }
        if(item != null && item.hasItemMeta() && Objects.equals(event.getHand(), EquipmentSlot.HAND)
                && !item.getItemMeta().getPersistentDataContainer().isEmpty()) {
            if(action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) {
                ItemMeta clickedMeta = item.getItemMeta();
                PersistentDataContainer clickedPers = clickedMeta.getPersistentDataContainer();
                NamespacedKey expKey = new NamespacedKey(plugin, "exp-amount");
                NamespacedKey voucherKey = new NamespacedKey(plugin, "voucher");
                NamespacedKey gregKey = new NamespacedKey(plugin, "greg");
                if (clickedPers.has(expKey, PersistentDataType.INTEGER)) {
                    event.setCancelled(true);
                    int expToGive = clickedPers.get(expKey, PersistentDataType.INTEGER);
                    player.giveExp(expToGive);
                    player.sendMessage(Component.text("+" + plugin.formatNumber(expToGive) + " XP", NamedTextColor.DARK_GREEN, TextDecoration.BOLD));
                    player.getInventory().removeItem(item.asOne());
                } else if (clickedPers.has(voucherKey, PersistentDataType.STRING)) {
                    String voucherType = clickedPers.get(voucherKey, PersistentDataType.STRING);
                    if (voucherType != null && voucherType.equalsIgnoreCase("single-use-enderchest")) {
                        event.setCancelled(true);
                        player.getInventory().removeItem(item.asOne());
                        player.openInventory(player.getEnderChest());
                    }
                } else if (item.getType().equals(Material.SPLASH_POTION) && clickedPers.has(gregKey, PersistentDataType.STRING)) {
                    String potionData = clickedPers.get(gregKey, PersistentDataType.STRING);
                    if (potionData != null && potionData.contains(";")) {
                        String[] types = potionData.split(";");
                        if (Boolean.parseBoolean(types[0])) {
                            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
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
}
