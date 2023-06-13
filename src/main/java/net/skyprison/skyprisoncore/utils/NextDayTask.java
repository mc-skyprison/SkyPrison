package net.skyprison.skyprisoncore.utils;

import com.google.common.base.Functions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.UUID;
import java.util.stream.Collectors;

public class NextDayTask extends TimerTask {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public NextDayTask(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void run() {
        ArrayList<String> dailyPlayers = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate yestererday = LocalDate.now().minusDays(2);
        String yestererDate = yestererday.format(formatter);

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, current_streak FROM dailies WHERE last_collected = ?")) {
            ps.setString(1, yestererDate);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                if(rs.getInt(2) > 0) dailyPlayers.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(!dailyPlayers.isEmpty()) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE dailies SET current_streak = 0 WHERE user_id IN " + plugin.getQuestionMarks(dailyPlayers))) {
                for (int i = 0; i < dailyPlayers.size(); i++) {
                    ps.setString(i + 1, dailyPlayers.get(i));
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        LocalDate yesterday = LocalDate.now();
        String currDate = yesterday.format(formatter);
        ArrayList<Integer> oldMissions = new ArrayList<>();
        ArrayList<UUID> players = new ArrayList<>();
        ArrayList<UUID> oPlayers = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT mission_id, user_id FROM daily_missions WHERE date != ?")) {
            ps.setString(1, currDate);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                oldMissions.add(rs.getInt(1));
                UUID pUUID = UUID.fromString(rs.getString(2));
                if(!players.contains(pUUID)) {
                    players.add(pUUID);
                    if (Bukkit.getOfflinePlayer(pUUID).isOnline()) {
                        oPlayers.add(pUUID);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(!oldMissions.isEmpty()) {
            players.forEach(player -> plugin.missions.remove(player));

            for (UUID pUUID : oPlayers) {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(pUUID);
                if (offline.isOnline()) {
                    Player player = offline.getPlayer();
                    assert player != null;
                    player.sendMessage(Component.text("Your Daily Missions have refreshed!", NamedTextColor.GREEN));
                    plugin.dailyMissions.setPlayerMissions(player);
                }
            }

            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM daily_missions WHERE mission_id IN " + plugin.getQuestionMarks(oldMissions.stream().map(Functions.toStringFunction()).collect(Collectors.toList())))) {
                for (int i = 0; i < oldMissions.size(); i++) {
                    ps.setInt(i + 1, oldMissions.get(i));
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
