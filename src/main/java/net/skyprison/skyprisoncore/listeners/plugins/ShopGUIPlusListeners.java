package net.skyprison.skyprisoncore.listeners.plugins;

import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopGUIPlusListeners implements Listener {
    private final DatabaseHook db;

    public ShopGUIPlusListeners(DatabaseHook db) {
        this.db = db;
    }

    @EventHandler
    public void onShopPreTransaction(ShopPreTransactionEvent event) {
        if(!event.getShopAction().equals(ShopManager.ShopAction.SELL_ALL)) return;

        Player player = event.getPlayer();
        String iName = event.getShopItem().getItem().getType().name();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT block_item FROM block_sells WHERE user_id = ? AND block_item = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, iName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) event.setCancelled(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) {
        ShopTransactionResult result = event.getResult();
        if(!result.getResult().equals(ShopTransactionResult.ShopTransactionResultType.SUCCESS)) return;

        Player player = result.getPlayer();

        DailyMissions.updatePlayerMissions(player.getUniqueId(), "money", "", (int) result.getPrice());

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO logs_shop (user_id, user_rank, transaction_type, item, amount, price) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, PlayerManager.getPrisonRank(player));
            ps.setString(3, result.getShopAction().name());
            ps.setString(4, result.getShopItem().getItem().getType().name());
            ps.setInt(5, result.getAmount());
            ps.setDouble(6, result.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
