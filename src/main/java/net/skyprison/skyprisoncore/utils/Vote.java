package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Vote {
    private static final DatabaseHook db = SkyPrisonCore.db;
    public static int getVoteParty() {
        int votes = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) % 50 FROM votes")) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                votes = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return votes;
    }
    public static int getVoteTokens(UUID pUUID) {
        int tokens = 25;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) FROM votes WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int votes = rs.getInt(1);
                if(votes == 1) {
                    tokens = -1;
                } else if(votes >= 1050) {
                    tokens = 100;
                } else {
                    if (votes <= 150) {
                        tokens += votes / 6;
                    } else {
                        tokens += 25 + (votes - 150) / 18;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tokens;
    }
    public static int getVoteAmount(UUID pUUID) {
        int votes = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) FROM votes WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                votes = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return votes;
    }
    public static void onAllSites(UUID pUUID) {
        long startOfToday = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli();
        long endOfToday = startOfToday + 24*60*60*1000 - 1;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) FROM votes WHERE user_id = ? AND time >= ? AND time <= ?")) {
            ps.setString(1, pUUID.toString());
            ps.setLong(2, startOfToday);
            ps.setLong(3, endOfToday);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int votes = rs.getInt(1);
                if(votes == 6) {
                    Player player = Bukkit.getPlayer(pUUID);
                    Component voteMsg = Component.text("Vote", NamedTextColor.DARK_GREEN, TextDecoration.BOLD).append(Component.text("  » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(" You've voted on 6 sites today, and have therefore received a Vote Key!", NamedTextColor.AQUA));
                    String playerName = player != null ? player.getName() : PlayerManager.getPlayerName(pUUID);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "crate key give " + playerName + " crate_vote 1");
                    if(player != null) {
                        player.sendMessage(voteMsg);
                    } else {
                        Notifications.createNotification("vote-all", null, pUUID.toString(), voteMsg, null, true);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void checkVoteMilestones(UUID pUUID) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) FROM votes WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int votes  = rs.getInt(1);
                Component reward = null;
                String playerName = PlayerManager.getPlayerName(pUUID);
                if(playerName != null) {
                    switch (votes) {
                        case 100 -> {
                            reward = Component.text("White Sheep Disguise", NamedTextColor.WHITE, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.disguise.sheep");
                        }
                        case 200 -> {
                            reward = Component.text("Yellow Sheep Disguise", NamedTextColor.YELLOW, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.yellow");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set ibsdisguises.disguise.sheep.setcolor");
                        }
                        case 300 -> {
                            reward = Component.text("Green Sheep Disguise", NamedTextColor.DARK_GREEN, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.green");
                        }
                        case 400 -> {
                            reward = Component.text("Red Sheep Disguise", NamedTextColor.RED, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.red");
                        }
                        case 500 -> {
                            reward = Component.text("Blue Sheep Disguise & Novice Voter Tag", NamedTextColor.DARK_AQUA, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.blue");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set skyprisoncore.tag.55");
                        }
                        case 750 -> {
                            reward = Component.text("Black Sheep Disguise", NamedTextColor.DARK_GRAY, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.black");
                        }
                        case 1000 -> {
                            reward = Component.text("Pink Sheep Disguise & Top Voter Tag", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.pink");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set skyprisoncore.tag.56");
                        }
                        case 1250 -> {
                            reward = Component.text("Sheep Pet", NamedTextColor.YELLOW, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep");
                        }
                        case 1500 -> {
                            reward = Component.text("Sheep Pet Customization & Elite Voter Tag", NamedTextColor.YELLOW, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set skyprisoncore.tag.87");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep.data.baby");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep.data.color");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep.data.frozen");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep.data.sheared");
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep.data.silent");
                        }
                        case 1750 -> {
                            reward = Component.text("Rainbow Sheep Pet", NamedTextColor.YELLOW, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set pet.type.sheep.data.rainbow");
                        }
                        case 2000 -> {
                            reward = Component.text("Rainbow Sheep Disguise", NamedTextColor.YELLOW, TextDecoration.BOLD);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + playerName + " permission set libsdisguises.options.disguise.sheep.setcolor.yellow");
                        }
                    }
                    if (reward != null) {
                        Component milestoneMsg = Component.text("Vote", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                                .append(Component.text("  » ", NamedTextColor.DARK_GRAY)).append(Component.text(playerName, NamedTextColor.GREEN))
                                .append(Component.text(" has voted ", NamedTextColor.AQUA)).append(Component.text(votes, NamedTextColor.YELLOW))
                                .append(Component.text(" times and received ", NamedTextColor.AQUA)).append(reward);
                        Bukkit.getServer().sendMessage(milestoneMsg);

                        if (Bukkit.getPlayer(pUUID) == null) {
                            Notifications.createNotification("vote-milestone", null, pUUID.toString(), milestoneMsg, null, true);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
