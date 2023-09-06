package net.skyprison.skyprisoncore.utils;

import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public record Secret(int id, String name, ItemStack displayItem, String category, String type, Integer position, String rewardType, Integer reward, long cooldown) {
    public void setPlayerCooldown(UUID pUUID) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO secrets_userdata (secret_id, user_id, collect_time) VALUES (?, ?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, pUUID.toString());
            ps.setLong(3, System.currentTimeMillis() + cooldown);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isAvailable() {
        long currTime = System.currentTimeMillis();
        return currTime >= cooldown;
    }
}
