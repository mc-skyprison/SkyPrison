package net.skyprison.skyprisoncore.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class PlayerManager {
    public static UUID getPlayerId(String playerName) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE current_name = ?")) {
            ps.setString(1, playerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    return UUID.fromString(rs.getString(1));
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
            }
        } catch (SQLException ignored) {
            return null;
        }
        return null;
    }
}
