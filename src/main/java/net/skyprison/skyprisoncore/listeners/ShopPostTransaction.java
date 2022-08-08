package net.skyprison.skyprisoncore.listeners;

import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.brcdev.shopgui.shop.ShopTransactionResult;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
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

    public ShopPostTransaction(DatabaseHook hook) {
        this.hook = hook;
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) {
        if(event.getResult().getResult().equals(ShopTransactionResult.ShopTransactionResultType.SUCCESS)) {
            if (event.getResult().getShopAction() == ShopManager.ShopAction.SELL
                    || event.getResult().getShopAction() == ShopManager.ShopAction.SELL_ALL) {
                Player player = event.getResult().getPlayer();

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
                        List<Object> params = new ArrayList<Object>() {{
                            add(finalRecentId);
                        }};
                        hook.sqlUpdate(sql, params);
                    }
                }

                String sql = "INSERT INTO recent_sells (user_id, recent_item, recent_amount, recent_price) VALUES (?, ?, ?, ?)";
                List<Object> params = new ArrayList<Object>() {{
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
