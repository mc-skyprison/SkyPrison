package net.skyprison.skyprisoncore.listeners.shopguiplus;

import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShopPostTransaction implements Listener {
    private final DatabaseHook db;
    private final DailyMissions dailyMissions;

    public ShopPostTransaction(DatabaseHook db, DailyMissions dailyMissions) {
        this.db = db;
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) {
        if(event.getResult().getResult().equals(ShopTransactionResult.ShopTransactionResultType.SUCCESS)) {
            if (event.getResult().getShopAction() == ShopManager.ShopAction.SELL
                    || event.getResult().getShopAction() == ShopManager.ShopAction.SELL_ALL) {
                Player player = event.getResult().getPlayer();

                for (String mission : dailyMissions.getMissions(player)) {
                    if(!dailyMissions.isCompleted(player, mission)) {
                        String[] missSplit = mission.split("-");
                        if (missSplit[0].equalsIgnoreCase("money")) {
                            int incAmount = (int) event.getResult().getPrice();
                            dailyMissions.updatePlayerMission(player, mission, incAmount);
                        }
                    }
                }
                List<String> soldItems = new ArrayList<>();
                int recentId = -1;

                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT recent_item, recent_amount, recent_price, recent_id FROM recent_sells WHERE user_id = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    int first = 0;
                    while (rs.next()) {
                        if (first == 0) {
                            first++;
                            recentId = rs.getInt(4);
                        }
                        soldItems.add(rs.getString(1) + "/" + rs.getInt(2) + "/" + rs.getFloat(3));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                soldItems.add(event.getResult().getShopItem().getItem().getType()
                        + "/" + event.getResult().getAmount()
                        + "/" + event.getResult().getPrice());
                if (soldItems.size() > 5) {
                    if (recentId >= 0) {
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM recent_sells WHERE recent_id = ?")) {
                            ps.setInt(1, recentId);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO recent_sells (user_id, recent_item, recent_amount, recent_price) VALUES (?, ?, ?, ?)")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setString(2, event.getResult().getShopItem().getItem().getType().name());
                    ps.setInt(3, event.getResult().getAmount());
                    ps.setDouble(4, event.getResult().getPrice());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
