package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class NextDayTask extends TimerTask {
    private final DatabaseHook db;
    public NextDayTask(DatabaseHook db) {
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
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE dailies SET current_streak = 0 WHERE user_id IN " + SkyPrisonCore.getQuestionMarks(dailyPlayers))) {
                for (int i = 0; i < dailyPlayers.size(); i++) {
                    ps.setString(i + 1, dailyPlayers.get(i));
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        List<DailyMissions.PlayerMission> missions = PlayerManager.getAllMissions();
        for(DailyMissions.PlayerMission mission : missions) {
            if(!mission.date().toLocalDateTime().toLocalDate().equals(LocalDate.now())) {
                missions.forEach(PlayerManager::removePlayerMissions);

                Player player = Bukkit.getPlayer(mission.player());
                if(player != null && player.isOnline()) DailyMissions.giveMissions(player);
                break;
            }
        }
    }
}
