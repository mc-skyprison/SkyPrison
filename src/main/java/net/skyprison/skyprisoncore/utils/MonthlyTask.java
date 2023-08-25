package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.economy.Tokens;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.TimerTask;
import java.util.UUID;

public class MonthlyTask extends TimerTask {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public MonthlyTask(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void run() {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("""
                SELECT user_id, COUNT(id) as vote_count\s
                FROM votes\s
                WHERE YEAR(FROM_UNIXTIME(time / 1000)) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)
                AND MONTH(FROM_UNIXTIME(time / 1000)) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH)
                GROUP BY user_id\s
                ORDER BY vote_count DESC\s
                LIMIT 3
                """)) {
            ResultSet rs = ps.executeQuery();
            int i = 1;
            String month = YearMonth.now().minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
            Component topVote = Component.text("Top Voters of " + month, TextColor.fromHexString("#d8044b"), TextDecoration.BOLD);
            String topVoter = "";
            while (rs.next()) {
                String playerId = rs.getString(1);
                try {
                    UUID pUUID = UUID.fromString(playerId);
                    String playerName = PlayerManager.getPlayerName(pUUID);
                    if(playerName != null) {
                        if(i == 1) {
                            topVoter = playerName;
                            plugin.asConsole("lp user " + playerName + " permission set skyprisoncore.tag.85");
                        }
                        new Tokens(plugin, db).addTokens(pUUID, 200, "monthly-top-voter", String.valueOf(i));
                        topVote = topVote.appendNewline().append(Component.text(i + ". ", NamedTextColor.GRAY, TextDecoration.BOLD))
                                .append(Component.text(playerName, NamedTextColor.RED, TextDecoration.BOLD))
                                .append(Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                                .append(Component.text(plugin.formatNumber(rs.getInt("vote_count")) + " Votes", TextColor.fromHexString("#cccccc"), TextDecoration.BOLD));
                        i++;
                        Player player = Bukkit.getPlayer(pUUID);
                        Component voterMsg = Component.text("You came ", NamedTextColor.GRAY, TextDecoration.BOLD)
                                .append(Component.text(i == 1 ? "First" : i == 2 ? "Second" : "Third", NamedTextColor.RED, TextDecoration.BOLD))
                                .append(Component.text("in Monthly Voters! You've been awarded ", NamedTextColor.GRAY, TextDecoration.BOLD))
                                .append(Component.text("200 Tokens", NamedTextColor.RED, TextDecoration.BOLD));
                        if(i == 1) {
                            voterMsg = voterMsg.append(Component.text(" and ", NamedTextColor.GRAY, TextDecoration.BOLD))
                                    .append(Component.text("Voter Legend Tag", NamedTextColor.RED, TextDecoration.BOLD))
                                    .append(Component.text("!", NamedTextColor.GRAY, TextDecoration.BOLD));
                        } else {
                            voterMsg = voterMsg.append(Component.text("!", NamedTextColor.GRAY, TextDecoration.BOLD));
                        }
                        if(player != null) {
                            player.sendMessage(voterMsg);
                        } else {
                            plugin.createNotification("vote-monthly-top", null, pUUID.toString(), voterMsg, null, true);
                        }
                    }
                } catch (Exception ignored) {}
            }
            topVote = topVote.appendNewline().appendNewline().append(Component.text("They have been awarded", NamedTextColor.GRAY))
                    .append(Component.text(" 200 Tokens ", NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(" and ", NamedTextColor.GRAY))
                    .append(Component.text(topVoter, NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(" has been awarded ", NamedTextColor.GRAY))
                    .append(Component.text("Voter Legend Tag", NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text("!", NamedTextColor.GRAY));
            plugin.getServer().sendMessage(topVote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
