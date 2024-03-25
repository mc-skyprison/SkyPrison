package net.skyprison.skyprisoncore.utils.secrets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class SecretsUtils {
    public static int getFoundAmount(int secretId, UUID playerId) {
        int found = 0;
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(id) FROM secrets_userdata WHERE secret_id = ? AND user_id = ?")) {
            ps.setInt(1, secretId);
            ps.setString(2, playerId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                found = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return found;
    }
    public static List<String> getCategoryNames() {
        List<String> categories = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT name FROM secrets_categories WHERE deleted = 0 ORDER BY category_order ASC")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                categories.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    public static List<String> getTypes() {
        return new ArrayList<>(Arrays.asList("secret", "parkour", "puzzle"));
    }
    public static List<String> getRewardTypes() {
        return new ArrayList<>(Collections.singleton("tokens"));
    }
    public static ItemStack getSign(SkyPrisonCore plugin, int secretId, String secretName, Material material) {
        ItemStack sign = new ItemStack(material);
        sign.editMeta(meta -> {
            meta.displayName(Component.text("Secret Sign", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Secret Name: ", NamedTextColor.GOLD).append(MiniMessage.miniMessage().deserialize(secretName)).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Place sign to add Secret Location", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            NamespacedKey key = new NamespacedKey(plugin, "secret-sign");
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, secretId);
        });
        return sign;
    }
    public static List<Secret> getNotFoundSecrets(String category, UUID pUUID) {
        List<Secret> secrets = getSecretsInCategory(category);
        secrets = secrets.stream().filter(secret -> getFoundAmount(secret.id(), pUUID) == 0).toList();
        return secrets;
    }
    public static List<Secret> getSecretsInCategory(String category) {
        List<Secret> secrets = new ArrayList<>();
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, display_item, type, reward_type, reward, cooldown, max_uses, deleted FROM secrets WHERE category = ?")) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(3));
                String type = rs.getString(4);
                String rewardType = rs.getString(5);
                int reward = rs.getInt(6);
                String cooldown = rs.getString(7);
                int maxUses = rs.getInt(8);
                int deleted = rs.getInt(9);
                secrets.add(new Secret(id, name, displayItem, category, type, rewardType, reward, cooldown, maxUses, deleted == 1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return secrets;
    }
    public static SecretCategory getCategoryFromId(String id) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT description, display_item, permission, permission_message, regions, deleted, reward_type, reward FROM secrets_categories WHERE name = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String description = rs.getString(1);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(2));
                String permission = rs.getString(3);
                String permissionMessage = rs.getString(4);
                String regions = rs.getString(5);
                int deleted = rs.getInt(6);
                String rewardType = rs.getString(7);
                int reward = rs.getInt(8);
                HashMap<String, List<String>> regionMap = new HashMap<>();
                if(regions != null && !regions.isEmpty()) {
                    Arrays.stream(regions.split(";")).forEach(region -> {
                        String[] split = region.split(":"); // region : world
                        List<String> worldRegions = regionMap.getOrDefault(split[1], new ArrayList<>(Collections.singleton(split[0])));
                        regionMap.put(split[1], worldRegions);
                    });
                }
                return new SecretCategory(id, description, displayItem, permission, permissionMessage, regionMap, deleted == 1, rewardType, reward);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Secret getSecretFromId(int id) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT name, display_item, category, type, reward_type, reward, cooldown, max_uses, deleted FROM secrets WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(2));
                String category = rs.getString(3);
                String type = rs.getString(4);
                String rewardType = rs.getString(5);
                int reward = rs.getInt(6);
                String cooldown = rs.getString(7);
                int maxUses = rs.getInt(8);
                int deleted = rs.getInt(9);
                return new Secret(id, name, displayItem, category, type, rewardType, reward, cooldown, maxUses, deleted == 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static long coolInMillis(String cooldown) {
        int days = Integer.parseInt(cooldown.substring(0, cooldown.length() - 1));
        return TimeUnit.DAYS.toMillis(days);
    }
    public static Component formatTime(long collected) {
        Component coolText = null;
        LocalDateTime currTime = LocalDateTime.now();
        LocalDateTime cooldownDate = Instant.ofEpochMilli(collected).atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
        if (currTime.toLocalDate().toEpochDay() < cooldownDate.toLocalDate().toEpochDay()) {
            Duration duration = Duration.between(currTime, cooldownDate);
            List<String> timeStrings = timeToString(duration);
            String timeString = "<#ff5f33>" + String.join(", ", timeStrings) + " <bold>Left</bold>";
            coolText = MiniMessage.miniMessage().deserialize(timeString);
        }
        return coolText;
    }
    @NotNull
    private static List<String> timeToString(Duration duration) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        List<String> timeStrings = new ArrayList<>();
        if (days != 0.0)
            timeStrings.add("<bold>" + days + " Day" + (days > 1 ? "s" : "") + "</bold>");
        if (hours != 0.0)
            timeStrings.add("<bold>" + hours + " Hour" + (hours > 1 ? "s" : "") + "</bold>");
        if (minutes != 0.0 && days == 0.0)
            timeStrings.add("<bold>" + minutes + " Minute" + (minutes > 1 ? "s" : "") + "</bold>");
        if (seconds != 0.0 && days == 0.0 && hours == 0.0)
            timeStrings.add("<bold>" + seconds + " Second" + (seconds > 1 ? "s" : "") + "</bold><");
        return timeStrings;
    }
    public static long getPlayerCooldown(int secretId, UUID pUUID) {
        long collected = 0;
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(collect_time) FROM secrets_userdata WHERE secret_id = ? AND user_id = ?")) {
            ps.setInt(1, secretId);
            ps.setString(2, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                collected = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collected;
    }
    public static Component getTimeLeft(int secretId, String cooldown, UUID pUUID) {
        Component coolText = null;
        long collected = getPlayerCooldown(secretId, pUUID);

        if(collected != 0) {
            collected += coolInMillis(cooldown);
            coolText = formatTime(collected);
        }

        if(coolText == null) {
            coolText = Component.text("Available Now!", TextColor.fromHexString("#a5ff52"), TextDecoration.BOLD);
        }
        return coolText;
    }
}
