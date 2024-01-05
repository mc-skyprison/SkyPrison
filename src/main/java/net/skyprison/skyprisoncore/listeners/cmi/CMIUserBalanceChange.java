package net.skyprison.skyprisoncore.listeners.cmi;

import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CMIUserBalanceChange implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public CMIUserBalanceChange(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }
    @EventHandler
    public void onCMIUserBalanceChange(CMIUserBalanceChangeEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            CMIUser toUser = event.getUser();
            CMIUser fromUser = event.getSource();
            if (toUser != null && fromUser != null && event.getActionType().equalsIgnoreCase("deposit")) {
                double amount = event.getTo() - event.getFrom();
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO logs_transactions (sender_id, sender_rank, receiver_id, receiver_rank, amount) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, fromUser.getUniqueId().toString());
                    ps.setString(2, fromUser.getRank().getName());
                    ps.setString(3, toUser.getUniqueId().toString());
                    ps.setString(4, toUser.getRank().getName());
                    ps.setDouble(5, amount);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
