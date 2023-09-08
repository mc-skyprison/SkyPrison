package net.skyprison.skyprisoncore.utils.secrets;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public record Secret(int id, String name, ItemStack displayItem, String category, String type, String rewardType, Integer reward,
                     String cooldown, int maxUses, boolean deleted) {
    public void setPlayerCooldown(UUID pUUID) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO secrets_userdata (secret_id, user_id, collect_time) VALUES (?, ?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, pUUID.toString());
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isAvailable(UUID pUUID) {
        boolean isAvailable = true;
        long collected = SecretsUtils.getPlayerCooldown(id, pUUID);
        if(collected != 0) {
            collected += SecretsUtils.coolInMillis(cooldown);
            isAvailable = SecretsUtils.formatTime(collected) == null;
        }
        return isAvailable;
    }
    public boolean hasUsesLeft(UUID pUUID) {
        if(maxUses == 0) return true;
        boolean hasUsesLeft = true;
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(id) FROM secrets_userdata WHERE secret_id = ? AND user_id = ? ORDER BY collect_time DESC LIMIT 1")) {
            ps.setInt(1, id);
            ps.setString(2, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int found = rs.getInt(1);
                hasUsesLeft = found < maxUses;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return hasUsesLeft;
    }
    public Component getTimeLeft(UUID pUUID) {
        return SecretsUtils.getTimeLeft(id, cooldown, pUUID);
    }
}
