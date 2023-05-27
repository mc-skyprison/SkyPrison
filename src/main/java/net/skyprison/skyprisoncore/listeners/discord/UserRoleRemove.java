package net.skyprison.skyprisoncore.listeners.discord;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserRoleRemove implements UserRoleRemoveListener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public UserRoleRemove(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        if(event.getRole().getId() == Long.parseLong("799644093742448711")) {
            String pUUID = "";
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE discord_id = ?")) {
                ps.setLong(1, event.getUser().getId());
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    pUUID = rs.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!pUUID.isEmpty()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(pUUID));
                plugin.asConsole("lp user " + player.getName() + " permission unset deluxetags.tag.serverbooster");
            }
        }
    }
}
