package net.skyprison.skyprisoncore.utils;

import com.Zrips.CMI.CMI;
import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class DailyMissions {
    private final static List<Mission> missions = new ArrayList<>();
    public static void updatePlayerMissions(UUID player, String type, Object subType) {
        updatePlayerMissions(player, type, subType, 1);
    }
    public static void updatePlayerMissions(UUID player, String type, Object subType, int amount) {
        List<DailyMissions.PlayerMission> missions = PlayerManager.getPlayerMissions(player, false);
        missions.forEach(pMission -> {
            Mission mission = pMission.mission();
            if(mission.type().equals(type) && (mission.subType().contains("any") || mission.subType().contains(subType))) {
                pMission.updateAmount(amount);
            }
        });
    }
    public static void giveMissions(Player player) {
        boolean newMissions = true;
        try(Connection conn = db.getConnection(); PreparedStatement ps =
                conn.prepareStatement("SELECT id, mission, needed, amount, completed, mission_date FROM daily_missions WHERE user_id = ? AND DATE(mission_date) = CURDATE()")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            List<PlayerMission> pMissions = new ArrayList<>();
            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString(1));
                int missionId = rs.getInt(2);
                int needed = rs.getInt(3);
                int amount = rs.getInt(4);
                boolean completed = rs.getBoolean(5);
                Timestamp date = rs.getTimestamp(6);
                Mission mission = missions.stream().filter(m -> m.id() == missionId).findFirst().orElse(null);
                if(mission == null) break;
                pMissions.add(new PlayerMission(id, player.getUniqueId(), mission, amount, needed, completed, date));
            }
            if(pMissions.size() == 2) {
                newMissions = false;
                pMissions.forEach(PlayerManager::addPlayerMissions);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(newMissions) {
            List<String> ranks = new ArrayList<>();
            CMI.getInstance().getPlayerManager().getUser(player).getRank().getPreviousRanks().forEach(i -> ranks.add(i.getName()));
            List<Mission> avail = new ArrayList<>(missions.stream().filter(mission -> ranks.contains(mission.rank())).toList());
            Collections.shuffle(avail);
            List<Mission> toGive = avail.subList(0, 2);
            toGive.forEach(mission -> PlayerManager.addPlayerMissions(new PlayerMission(player.getUniqueId(), mission)));
        }
    }
    public static void loadMissions() {
        missions.add(new Mission(1, "secrets", List.of("any"), "Find Secrets", 2, 5, "default"));
        missions.add(new Mission(2, "sponge", List.of("any"), "Find Sponges", 2, 5, "default"));

        missions.add(new Mission(3, "fish", List.of("any"), "Go Fishing", 16, 32, "default"));
        missions.add(new Mission(4, "fish", List.of(Material.PUFFERFISH), "Fish Pufferfish", 5, 10, "default"));
        missions.add(new Mission(5, "fish", List.of(Material.COD), "Fish Cod", 5, 15, "default"));
        missions.add(new Mission(6, "fish", List.of(Material.SALMON), "Fish Salmon", 5, 15, "default"));
        missions.add(new Mission(7, "fish", List.of(Material.TROPICAL_FISH), "Fish Tropical Fish", 5, 10, "default"));

        missions.add(new Mission(8, "break", List.of("any"), "Break Blocks", 75, 125, "default"));
        missions.add(new Mission(9, "break", MaterialSetTag.BIRCH_LOGS.getValues().stream().toList(), "Chop Birch", 25, 75, "default"));

        missions.add(new Mission(10, "money", List.of("any"), "Make Money", 500, 2000, "default"));
        missions.add(new Mission(11, "bomb", List.of("any"), "Use Bombs", 5, 10, "default"));
        missions.add(new Mission(12, "parkour", List.of("any"), "Do /parkour", 3, 8, "default"));

        missions.add(new Mission(13, "harvest", MaterialSetTag.CROPS.getValues().stream().toList(), "Harvest Crops", 80, 160, "desert"));
        missions.add(new Mission(14, "harvest", List.of(Material.CACTUS), "Harvest Cactus", 64, 128, "desert"));
        missions.add(new Mission(15, "harvest", List.of(Material.BAMBOO), "Harvest Bamboo", 64, 128, "desert"));
        missions.add(new Mission(16, "harvest", List.of(Material.PUMPKIN), "Harvest Pumpkin", 64, 128, "nether"));
        missions.add(new Mission(17, "harvest", List.of(Material.SUGAR_CANE), "Harvest Sugar Cane", 64, 128, "snow"));

        missions.add(new Mission(18, "kill", List.of("any"), "Kill Monsters", 15, 30, "nether"));
        missions.add(new Mission(19, "kill", Arrays.asList(EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER), "Kill Zombies", 15, 30, "nether"));
        missions.add(new Mission(20, "kill", List.of("skeleton"), "Kill Skeletons", 15, 30, "nether"));

        missions.add(new Mission(21, "slaughter", List.of("any"), "Slaughter Animals", 15, 30, "snow"));
        missions.add(new Mission(22, "slaughter", List.of("cow"), "Slaughter Cows", 15, 30, "snow"));
        missions.add(new Mission(23, "slaughter", List.of("pig"), "Slaughter Pigs", 15, 30, "snow"));
    }
    public record Mission(int id, String type, List<?> subType, String displayName, Integer lowest, Integer highest, String rank) {
        public Integer randomNeeded() {
                return (int) (Math.random() * (highest - lowest)) + lowest;
        }
    }
    public static class PlayerMission {
        private final UUID id;
        private final UUID player;
        private final Mission mission;
        private final Integer needed;
        private Integer amount = 0;
        private boolean completed = false;
        private final Timestamp date;
        public PlayerMission(UUID player, Mission mission) {
            Timestamp date = null;
            id = UUID.randomUUID();
            this.player = player;
            this.mission = mission;
            needed = mission.randomNeeded();
            try(Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement("INSERT INTO daily_missions (id, user_id, mission, needed, amount) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, id.toString());
                ps.setString(2, player.toString());
                ps.setInt(3, mission.id);
                ps.setInt(4, needed);
                ps.setInt(5, amount);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        date = rs.getTimestamp("mission_date");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.date = date;
        }
        public PlayerMission(UUID id, UUID player, Mission mission, Integer amount, Integer needed, boolean completed, Timestamp date) {
            this.id = id;
            this.player = player;
            this.mission = mission;
            this.amount = amount;
            this.needed = needed;
            this.completed = completed;
            this.date = date;
        }
        public UUID id() {
            return id;
        }
        public UUID player() {
            return player;
        }
        public Mission mission() {
            return mission;
        }
        public Integer needed() {
            return needed;
        }
        public Integer amount() {
            return amount;
        }
        public boolean completed() {
            return completed;
        }
        public Timestamp date() {
            return date;
        }
        public void updateAmount(Integer amount) {
            if(completed) return;
            this.amount += amount;
            if(this.amount >= needed) {
                completed = true;
                Player oPlayer = Bukkit.getPlayer(player);
                if(oPlayer != null) {
                    Random randInt = new Random();
                    int reward = randInt.nextInt(25) + 25;
                    TokenUtils.addTokens(player, reward, "Daily Mission", mission.displayName + " - " + needed);
                    oPlayer.playSound(oPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
            }
        }
    }
}
