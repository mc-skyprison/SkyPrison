package net.skyprison.skyprisoncore.listeners;

import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopPreTransaction implements Listener {
    private final DatabaseHook hook;

    public ShopPreTransaction(DatabaseHook hook) {
        this.hook = hook;
    }

    @EventHandler
    public void onShopPreTransaction(ShopPreTransactionEvent event) {
        if(event.getShopAction().equals(ShopManager.ShopAction.SELL_ALL)) {
            Player player = event.getPlayer();

            boolean isBlocked = false;

            String iName = event.getShopItem().getItem().getType().name();
            try {
                Connection conn = hook.getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT block_item FROM block_sells WHERE user_id = '" + player.getUniqueId() + "' AND block_item = '" + iName + "'");
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    isBlocked = true;
                }
                hook.close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(isBlocked) {
                event.setCancelled(true);
            }
        }
    }
}
