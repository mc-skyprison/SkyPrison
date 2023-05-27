package net.skyprison.skyprisoncore.listeners.quickshop;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.api.event.ShopPurchaseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopPurchase implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public ShopPurchase(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent event) {
        String player = event.getShop().getOwner().toString();
        String bannedPlayer = event.getPurchaser().toString();

        boolean isBanned = false;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT banned_user FROM shop_banned WHERE user_id = ? AND banned_user = ?")) {
            ps.setString(1, player);
            ps.setString(2, bannedPlayer);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                isBanned = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(isBanned) {
            Bukkit.getPlayer(event.getPurchaser()).sendMessage(plugin.colourMessage("&cThis player has banned you from their shops!"));
            event.setCancelled(true);
        }
    }

}
