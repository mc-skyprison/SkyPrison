package net.skyprison.skyprisoncore.listeners.quickshop;

import com.ghostchu.quickshop.api.event.ShopSuccessPurchaseEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class ShopSuccessPurchase implements Listener {
    private final DatabaseHook db;

    public ShopSuccessPurchase(DatabaseHook db) {
        this.db = db;
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
