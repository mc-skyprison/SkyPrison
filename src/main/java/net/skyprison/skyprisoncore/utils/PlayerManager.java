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
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (SQLException ignored) {}
        return null;
    }
    public static String getPlayerName(UUID pUUID) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_name FROM users WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ignored) {}
        return null;
    }

    public static long getPlayerDiscord(UUID pUUID) {
        long discordId = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                discordId = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return discordId;
    }
}
