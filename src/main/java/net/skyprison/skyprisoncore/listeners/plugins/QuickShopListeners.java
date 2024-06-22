package net.skyprison.skyprisoncore.listeners.plugins;

import com.ghostchu.quickshop.api.event.ShopCreateEvent;
import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class QuickShopListeners implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public QuickShopListeners(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onShopCreate(ShopCreateEvent event) {
        Shop shop = event.getShop();
        Material item = shop.getItem().getType();
        QUser creator = event.getCreator();
        if(!creator.isRealPlayer() || !plugin.minPrice.containsKey(item)) return;

        double minItemPrice = plugin.minPrice.get(item);
        double setPrice = shop.getPrice();
        Player player = creator.getBukkitPlayer().orElse(null);
        if(player == null || setPrice >= minItemPrice) return;

        player.sendMessage(Component.text("Minimum price for this item is $" + minItemPrice + "!", NamedTextColor.RED));
        event.setCancelled(true, "Minimum price for this item is $" + minItemPrice + "!");
    }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent event) {
        Player player = event.getPurchaser().getBukkitPlayer().orElse(null);
        UUID shopOwner = event.getShop().getOwner().getUniqueId();
        if(player == null || shopOwner == null) return;

        boolean isBanned = false;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT banned_user FROM shop_banned WHERE user_id = ? AND banned_user = ?")) {
            ps.setString(1, shopOwner.toString());
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                isBanned = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(isBanned) {
            player.sendMessage(Component.text("This player has banned you from their shops!", NamedTextColor.RED));
            event.setCancelled(true, player.getName() + " has been banned from this shop!");
        }
    }

    @EventHandler
    public void onShopSuccessPurchase(ShopSuccessPurchaseEvent event) {
        Shop shop = event.getShop();

        UUID purchaser = event.getPurchaser().getUniqueId();
        UUID shopOwner = shop.getOwner().getUniqueId();
        if(purchaser == null || shopOwner == null) return;

        boolean isBuying = shop.isBuying();
        String sender = isBuying ? shopOwner.toString() : purchaser.toString();
        String receiver = isBuying ? purchaser.toString() : shopOwner.toString();

        ItemStack item = shop.getItem();
        item.setAmount(event.getAmount());

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO logs_transactions (sender_id, sender_rank, receiver_id, receiver_rank, amount, item) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, sender);
            ps.setString(2, PlayerManager.getPrisonRank(UUID.fromString(sender)));
            ps.setString(3, receiver);
            ps.setString(4, PlayerManager.getPrisonRank(UUID.fromString(receiver)));
            ps.setDouble(5, event.getBalance());
            ps.setBytes(6, item.serializeAsBytes());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
