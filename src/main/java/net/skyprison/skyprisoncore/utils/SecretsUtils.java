package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class SecretsUtils {
    public static int getFoundAmount(int secretId, String playerId) {
        int found = 0;
        try (Connection sConn = db.getConnection(); PreparedStatement sPs = sConn.prepareStatement(
                "SELECT COUNT(id) FROM secrets WHERE secret_id = ? AND user_id = ?")) {
            sPs.setInt(1, secretId);
            sPs.setString(2, playerId);
            ResultSet set = sPs.executeQuery();
            if (set.next()) {
                found = set.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return found;
    }

    public static Component getCooldownText(long cooldown, long currTime) {
        long timeTill = cooldown - currTime;
        int days = (int) Math.floor(timeTill / (1000.0 * 60.0 * 60.0 * 24.0));
        int hours = (int) Math.floor(timeTill / (1000.0 * 60.0 * 60.0));
        int minutes = (int) Math.round((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
        int seconds = (int) Math.round((timeTill % (1000.0 * 60.0)) / 1000.0);
        TextComponent coolText = Component.empty();
        if (days != 0.0)
            coolText = coolText.append(Component.text(days, NamedTextColor.YELLOW).append(Component.text(" day" +
                    (days > 1 ? "s " : " "), NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
        if (hours != 0.0)
            coolText = coolText.append(Component.text(hours, NamedTextColor.YELLOW).append(Component.text(" hour" +
                    (hours > 1 ? "s " : " "), NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
        if (minutes != 0.0)
            coolText = coolText.append(Component.text(minutes, NamedTextColor.YELLOW).append(Component.text(" min" +
                    (minutes > 1 ? "s " : " "), NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
        if (seconds != 0.0)
            coolText = coolText.append(Component.text(seconds, NamedTextColor.YELLOW).append(Component.text(" sec" +
                    (seconds > 1 ? "s " : " "), NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
        if (coolText.content().isEmpty()) coolText = Component.text("Available Now!", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false);
        return coolText;
    }

    public static Secret getSecretFromId(int id) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT name, display_item, category, type, position, reward_type, reward, cooldown FROM secrets WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(2));
                String sCategory = rs.getString(3);
                String type = rs.getString(4);
                int position = rs.getInt(5);
                String rewardType = rs.getString(6);
                int reward = rs.getInt(7);
                long cooldown = rs.getLong(8);
                return new Secret(id, name, displayItem, sCategory, type, position, rewardType, reward, cooldown);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
