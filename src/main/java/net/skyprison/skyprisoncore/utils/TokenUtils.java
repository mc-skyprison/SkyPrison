package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class TokenUtils {
    private static final Component prefix = Component.text("Tokens", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY));
    public static Component getPrefix() {
        return prefix;
    }
    private static final Map<UUID, Integer> tokensData = new HashMap<>();
    public static Map<UUID, Integer> getTokensData() {
        return tokensData;
    }
    public static void addTokens(UUID pUUID, Integer amount, String source, String sourceData) {
        Player player = Bukkit.getPlayer(pUUID);
        if(player != null && player.isOnline()) {
            int tokens = tokensData.get(pUUID);
            tokens += amount;
            tokensData.put(pUUID, tokens);
            player.sendMessage(prefix.append(Component.text(ChatUtils.formatNumber(amount) + " tokens ", NamedTextColor.AQUA).append(Component.text("has been added to your balance", NamedTextColor.GRAY))));
        } else {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = tokens + ? WHERE user_id = ?")) {
                ps.setInt(1, amount);
                ps.setString(2, pUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        insertData(pUUID, "receive", amount, source, sourceData);
    }
    public static void removeTokens(UUID pUUID, Integer amount, String source, String sourceData) {
        Player player = Bukkit.getPlayer(pUUID);
        if(player != null && player.isOnline()) {
            int tokens = tokensData.get(pUUID);
            tokens -= amount;
            tokensData.put(pUUID, Math.max(tokens, 0));
            player.sendMessage(prefix.append(Component.text(ChatUtils.formatNumber(amount) + " tokens ", NamedTextColor.AQUA)
                    .append(Component.text("was removed from your balance", NamedTextColor.GRAY))));
        } else {
            int tokens = getTokens(pUUID);
            tokens -= amount;
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                ps.setInt(1, Math.max(tokens, 0));
                ps.setString(2, pUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        insertData(pUUID, "remove", amount, source, sourceData);
    }
    public static void setTokens(UUID pUUID, Integer amount, String source, String sourceData) {
        Player player = Bukkit.getPlayer(pUUID);
        if(player != null && player.isOnline()) {
            tokensData.put(pUUID, amount);
            player.sendMessage(prefix.append(Component.text("Your token balance has been set to ", NamedTextColor.GRAY)
                    .append(Component.text(ChatUtils.formatNumber(amount), NamedTextColor.AQUA))));
        } else {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                ps.setInt(1, amount);
                ps.setString(2, pUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        insertData(pUUID, "set", amount, source, sourceData);
    }
    public static int getTokens(UUID pUUID) {
        int tokens = 0;
        if(tokensData.containsKey(pUUID)) {
            tokens = tokensData.get(pUUID);
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = ?")) {
                ps.setString(1, pUUID.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tokens = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return tokens;
    }
    private static void insertData(UUID pUUID, String type, int amount, String source, String sourceData) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO logs_tokens (user_id, user_rank, type, amount, source, source_data) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, pUUID.toString());
            ps.setString(2, PlayerManager.getPrisonRank(pUUID));
            ps.setString(3, type);
            ps.setInt(4, amount);
            ps.setString(5, source);
            ps.setString(6, sourceData);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void sendTokensHelp(CommandSender sender) {
        Component helpMsg = Component.text("");
        helpMsg = helpMsg.append(Component.text("━━━━━━━━━━━━━━━━━|", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH)).append(Component.text(" Tokens ", NamedTextColor.AQUA))
                .append(Component.text("|━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));

        helpMsg = helpMsg.append(Component.text("\n/tokens balance (player)", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Check your own or other players token balance", NamedTextColor.GRAY)))

                .append(Component.text("\n/tokens shop", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Opens the token shop", NamedTextColor.GRAY)))

                .append(Component.text("\n/tokens top", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Displays the top token balances", NamedTextColor.GRAY)));


        if(sender.hasPermission("skyprisoncore.command.tokens.admin")) {
            helpMsg = helpMsg.append(Component.text("\n/tokens add <player> <amount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Adds tokens to the specified player", NamedTextColor.GRAY)))

                    .append(Component.text("\n/tokens remove <player> <amount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Removes tokens from the specified player", NamedTextColor.GRAY)))

                    .append(Component.text("\n/tokens set <player> <amount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Sets the tokens of the specified player to the specified amount", NamedTextColor.GRAY)))

                    .append(Component.text("\n/tokens giveall <amount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Gives tokens of the specified amount to everyone online", NamedTextColor.GRAY)));
        }
        sender.sendMessage(helpMsg);
    }
}
