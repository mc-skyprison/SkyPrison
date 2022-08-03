package net.skyprison.skyprisoncore.listeners;

import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopPostTransaction implements Listener {
    private final DatabaseHook hook;

    public ShopPostTransaction(DatabaseHook hook) {
        this.hook = hook;
    }

    @EventHandler
    public void onShopPostTransaction(ShopPostTransactionEvent event) {
        if(event.getResult().getShopAction() == ShopManager.ShopAction.SELL
                || event.getResult().getShopAction() == ShopManager.ShopAction.SELL_ALL) {
            Player player = event.getResult().getPlayer();

            String recentSells = "";

            try {
                Connection conn = hook.getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT recent_sells FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    recentSells = rs.getString(1);
                    recentSells = recentSells.replace("[", "");
                    recentSells = recentSells.replace("]", "");
                    recentSells = recentSells.replace(" ", "");
                }
                hook.close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            List<String> soldItems = new ArrayList<>(Arrays.asList(recentSells.split(",")));

            soldItems.add(0, event.getResult().getShopItem().getItem().getType()
                    + "/" + event.getResult().getAmount()
                    + "/" + event.getResult().getPrice());
            if(soldItems.size() > 5) {
                soldItems.remove(5);
            }

            String sql = "UPDATE users SET recent_sells = ? WHERE user_id = ?";
            List<Object> params = new ArrayList<Object>() {{
                add(soldItems);
                add(player.getUniqueId());
            }};
            hook.sqlUpdate(sql, params);
        }
    }
}
