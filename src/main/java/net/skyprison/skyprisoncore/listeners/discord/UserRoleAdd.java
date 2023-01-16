package net.skyprison.skyprisoncore.listeners.discord;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


public class UserRoleAdd implements UserRoleAddListener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public UserRoleAdd(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    @Override
    public void onUserRoleAdd(UserRoleAddEvent event) {
        if(event.getRole().getId() == Long.parseLong("799644093742448711")) {
            String pUUID = "";
            try {
                Connection conn = hook.getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE discord_id = '" + event.getUser().getId() + "'");
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    pUUID = rs.getString(1);
                }
                hook.close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!pUUID.isEmpty()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(pUUID));
                plugin.asConsole("lp user " + player.getName() + " permission set deluxetags.tag.serverbooster");
            }
        }
    }
}
