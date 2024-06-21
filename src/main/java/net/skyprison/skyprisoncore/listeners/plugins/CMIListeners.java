package net.skyprison.skyprisoncore.listeners.plugins;

import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CMIListeners implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public CMIListeners(SkyPrisonCore plugin, DatabaseHook db) {
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

    @EventHandler
    public void onCMIPlayerTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        Player sender = event.getWhoOffers();
        Player receiver = event.getWhoAccepts();

        PlayerManager.Ignore ignoring = PlayerManager.getPlayerIgnore(sender.getUniqueId(), receiver.getUniqueId());
        if(ignoring != null && ignoring.ignoreTeleport()) {
            sender.sendMessage(Component.text("Can't send teleport requests to players you're ignoring!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        PlayerManager.Ignore ignored = PlayerManager.getPlayerIgnore(receiver.getUniqueId(), sender.getUniqueId());
        if(ignored != null && ignored.ignoreTeleport()) {
            sender.sendMessage(receiver.displayName().colorIfAbsent(NamedTextColor.RED).append(Component.text(" is ignoring your teleport requests!", NamedTextColor.RED)));
            event.setCancelled(true);
        }
    }
}
