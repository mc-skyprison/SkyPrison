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
    private final DatabaseHook hook;
    private DailyMissions dailyMissions;

    public ShopPostTransaction(DatabaseHook hook, DailyMissions dailyMissions) {
        this.hook = hook;
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) {
        if(event.getResult().getResult().equals(ShopTransactionResult.ShopTransactionResultType.SUCCESS)) {
            if (event.getResult().getShopAction() == ShopManager.ShopAction.SELL
                    || event.getResult().getShopAction() == ShopManager.ShopAction.SELL_ALL) {
                Player player = event.getResult().getPlayer();

                for (String mission : dailyMissions.getPlayerMissions(player)) {
                    String[] missSplit = mission.split("-");
                    if (missSplit[0].equalsIgnoreCase("money")) {
                        int currAmount = Integer.parseInt(missSplit[4]) + Integer.parseInt(String.valueOf(event.getResult().getPrice()));
                        String nMission = missSplit[0] + "-" + missSplit[1] + "-" + missSplit[2] + "-" + missSplit[3] + "-" + currAmount;
                        dailyMissions.updatePlayerMission(player, mission, nMission);
                    }
                }
                List<String> soldItems = new ArrayList<>();
                int recentId = -1;

                try {
                    Connection conn = hook.getSQLConnection();
                    PreparedStatement ps = conn.prepareStatement("SELECT recent_item, recent_amount, recent_price, recent_id FROM recent_sells WHERE user_id = '" + player.getUniqueId() + "'");
                    ResultSet rs = ps.executeQuery();
                    int first = 0;
                    while (rs.next()) {
                        if (first == 0) {
                            first++;
                            recentId = rs.getInt(4);
                        }
                        soldItems.add(rs.getString(1) + "/" + rs.getInt(2) + "/" + rs.getFloat(3));
                    }
                    hook.close(ps, rs, conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                soldItems.add(event.getResult().getShopItem().getItem().getType()
                        + "/" + event.getResult().getAmount()
                        + "/" + event.getResult().getPrice());
                if (soldItems.size() > 5) {
                    if (recentId >= 0) {
                        String sql = "DELETE FROM recent_sells WHERE recent_id = ?";
                        int finalRecentId = recentId;
                        List<Object> params = new ArrayList<>() {{
                            add(finalRecentId);
                        }};
                        hook.sqlUpdate(sql, params);
                    }
                }

                String sql = "INSERT INTO recent_sells (user_id, recent_item, recent_amount, recent_price) VALUES (?, ?, ?, ?)";
                List<Object> params = new ArrayList<>() {{
                    add(player.getUniqueId().toString());
                    add(event.getResult().getShopItem().getItem().getType());
                    add(event.getResult().getAmount());
                    add(event.getResult().getPrice());
                }};
                hook.sqlUpdate(sql, params);
            }
        }
    }
}
