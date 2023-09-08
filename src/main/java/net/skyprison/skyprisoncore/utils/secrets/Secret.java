package net.skyprison.skyprisoncore.utils.secrets;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public record Secret(int id, String name, ItemStack displayItem, String category, String type, String rewardType, Integer reward, String cooldown, boolean deleted) {
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
    public Component getTimeLeft(UUID pUUID) {
        return SecretsUtils.getTimeLeft(id, cooldown, pUUID);
    }
}
