package net.skyprison.skyprisoncore.listeners.shopguiplus;

import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ShopPostTransaction implements Listener {
    private final DatabaseHook db;
    private final DailyMissions dailyMissions;

    public ShopPostTransaction(DatabaseHook db, DailyMissions dailyMissions) {
        this.db = db;
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) {
        ShopTransactionResult result = event.getResult();
        if(!result.getResult().equals(ShopTransactionResult.ShopTransactionResultType.SUCCESS)) return;

        Player player = result.getPlayer();

        for (String mission : dailyMissions.getMissions(player)) {
            if(!dailyMissions.isCompleted(player, mission)) {
                String[] missSplit = mission.split("-");
                if (missSplit[0].equalsIgnoreCase("money")) {
                    int incAmount = (int) result.getPrice();
                    dailyMissions.updatePlayerMission(player, mission, incAmount);
                }
            }
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO logs_shop (user_id, transaction_type, item, amount, price) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, result.getShopAction().name());
            ps.setString(3, result.getShopItem().getItem().getType().name());
            ps.setInt(4, result.getAmount());
            ps.setDouble(5, result.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
