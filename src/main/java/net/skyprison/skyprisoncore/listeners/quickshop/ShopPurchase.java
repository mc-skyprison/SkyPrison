package net.skyprison.skyprisoncore.listeners.quickshop;

import com.ghostchu.quickshop.api.event.ShopPurchaseEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ShopPurchase implements Listener {
    private final DatabaseHook db;

    public ShopPurchase(DatabaseHook db) {
        this.db = db;
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

}
