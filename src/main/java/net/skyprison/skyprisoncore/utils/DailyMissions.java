package net.skyprison.skyprisoncore.utils;

import com.Zrips.CMI.CMI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DailyMissions {

    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public DailyMissions(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }


    //Mission Type - Specific Type in type - Mission Title - Amount to reach - Current amount
    private ArrayList<String> getMissions(List<String> ranks) {
        ArrayList<String> missions = new ArrayList<>();
        missions.add("secrets-any-Find Secrets");

        missions.add("sponge-any-Find Sponges");

        missions.add("fish-any-Go Fishing");
        missions.add("fish-pufferfish-Fish Pufferfish");
        missions.add("fish-cod-Fish Cod");
        missions.add("fish-salmon-Fish Salmon");
        missions.add("fish-tropical_fish-Fish Tropical Fish");

        missions.add("money-any-Make Money");

        missions.add("break-any-Break Blocks");
        missions.add("break-birch_log-Chop Birch");

        missions.add("bomb-any-Use Bombs");

        missions.add("parkour-any-Do /parkour");

        if(ranks.contains("desert")) {
            missions.add("harvest-any-Harvest Crops");
            missions.add("harvest-cactus-Harvest Cactus");
            missions.add("harvest-bamboo-Harvest Bamboo");
        }

        if(ranks.contains("nether")) {
            missions.add("harvest-pumpkin-Harvest Pumpkin");
            //missions.add("harvest-nether_wart-Harvest Nether Wart");

            missions.add("kill-any-Kill Monsters");
            missions.add("kill-zombie-Kill Zombies");
            missions.add("kill-skeleton-Kill Skeletons");
        }

        if(ranks.contains("snow")) {
            missions.add("Slaughter-any-Slaughter Animals");
            missions.add("slaughter-cow-Slaughter Cows");
            missions.add("slaughter-pig-Slaughter Pigs");
            missions.add("harvest-sugar_cane-Harvest Sugar Cane");
        }

        Collections.shuffle(missions);

        ArrayList<String> twoMissions = new ArrayList<>();
        twoMissions.add(missions.get(0));
        twoMissions.add(missions.get(1));

        ArrayList<String> missionsToGive = new ArrayList<>();

        for(String mission : twoMissions) {
            Random randInt = new Random();
            String[] missionSplit = mission.split("-");
            switch(missionSplit[0].toLowerCase()) {
                case "secrets": // SecretFound IMPLEMENTED
                    missionsToGive.add(mission  + ":" + (randInt.nextInt(3) + 2));
                    break;
                case "fish": // PlayerFish IMPLEMENTED
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(16) + 16));
                            break;
                        case "pufferfish":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(5) + 5));
                            break;
                        case "cod":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(10) + 5));
                            break;
                        case "salmon":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(10) + 5));
                            break;
                        case "tropical_fish":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(5) + 5));
                            break;
                    }
                    break;
                case "kill": // Entity Death IMPLEMENTED
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(15) + 15));
                            break;
                        case "zombie":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(15) + 15));
                            break;
                        case "skeleton":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(15) + 15));
                            break;
                    }
                    break;
                case "break": // Block Break IMPLEMENTED
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(50) + 75));
                            break;
                        case "birch_log":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(50) + 25));
                            break;
                    }
                    break;
                case "harvest": // Block Break IMPLEMENTED
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(80) + 80));
                            break;
                        case "cactus":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(64) + 64));
                            break;
                        case "pumpkin":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(64) + 64));
                            break;
                        case "nether_wart":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(64) + 64));
                            break;
                        case "sugar_cane":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(64) + 64));
                            break;
                        case "bamboo":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(64) + 64));
                            break;
                    }
                    break;
                case "slaughter": // EntityDeath IMPLEMENTED
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(15) + 15));
                            break;
                        case "pig":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(15) + 15));
                            break;
                        case "cow":
                            missionsToGive.add(mission  + ":" + (randInt.nextInt(15) + 15));
                            break;
                    }
                    break;
                case "money": // ShopPostTransaction IMPLEMENTED
                    missionsToGive.add(mission  + ":" + (randInt.nextInt(1500) + 500));
                    break;
                case "sponge": // SpongeFound IMPLEMENTED
                    missionsToGive.add(mission  + ":" + (randInt.nextInt(1) + 2));
                    break;
                case "bomb": // BlockPlace IMPLEMENTED
                    missionsToGive.add(mission  + ":" + (randInt.nextInt(5) + 5));
                    break;
                case "parkour": // parkourFinish IMPLEMENTED
                    missionsToGive.add(mission  + ":" + (randInt.nextInt(5) + 3));

            }
        }

        return missionsToGive;
    }

    public void setPlayerMissions(Player player) {
        ArrayList<String> missions = new ArrayList<>();
        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT type, amount, needed, completed FROM daily_missions WHERE user_id = '" + player.getUniqueId() + "'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getInt(4) == 0) {
                    missions.add(rs.getString(1) + ":" + rs.getInt(3) + "_" + rs.getInt(2));
                } else {
                    missions.add(rs.getString(1) + ":" + rs.getInt(3) + "_" + rs.getInt(2) + ":completed");
                }
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (missions.isEmpty()) {
            List<String> ranks = new ArrayList<>();
            CMI.getInstance().getPlayerManager().getUser(player).getRank().getPreviousRanks().forEach(i -> ranks.add(i.getName()));

            missions = getMissions(ranks);

            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String currDate = formatter.format(date);

            for (String mission : missions) {
                String[] mSplit = mission.split(":");
                String sql = "INSERT INTO daily_missions (user_id, date, type, amount, needed, completed) VALUES (?, ?, ?, ?, ?, ?) ";
                List<Object> params = new ArrayList<>() {{
                    add(player.getUniqueId().toString());
                    add(currDate);
                    add(mSplit[0]);
                    add(0);
                    add(mSplit[1]);
                    add(0);
                }};
                hook.sqlUpdate(sql, params);
            }
        }
        plugin.missions.put(player.getUniqueId(), missions);
    }

    private String getFullMission(OfflinePlayer player, String mission) {
        String pMission = "";
        for(String fMission : getFullMissions(player)) {
            if(fMission.split(":")[0].equalsIgnoreCase(mission)) {
                pMission = fMission;
            }
        }
        return pMission;
    }

    public ArrayList<String> getFullMissions(OfflinePlayer player) {
        if(plugin.missions.containsKey(player.getUniqueId())) {
            return plugin.missions.get(player.getUniqueId());
        } else {
            return new ArrayList<>();
        }
    }

    public boolean isCompleted(OfflinePlayer player, String mission) {
        return getFullMission(player, mission).contains(":completed");
    }
    
    public ArrayList<String> getMissions(OfflinePlayer player) {
        if(plugin.missions.containsKey(player.getUniqueId())) {
            ArrayList<String> missionType = new ArrayList<>();
            for(String mission : plugin.missions.get(player.getUniqueId())) {
                missionType.add(mission.split(":")[0]);
            }
            return missionType;
        } else {
            return new ArrayList<>();
        }
    }

    public Integer getMissionNeeded(OfflinePlayer player, String mission) {
        int needed;

        if(!mission.contains(":")) {
            mission = getFullMission(player, mission);
        }

        String stats = mission.split(":")[1];

        if(stats.contains("_")) {
            needed = Integer.parseInt(stats.split("_")[0]);
        } else {
            needed = Integer.parseInt(stats);
        }

        return needed;
    }


    public Integer getMissionAmount(OfflinePlayer player, String mission) {
        int amount = 0;

        if(!mission.contains(":")) {
            mission = getFullMission(player, mission);
        }

        String stats = mission.split(":")[1];

        if(stats.contains("_")) {
            amount = Integer.parseInt(stats.split("_")[1]);
        }

        return amount;
    }


    public void updatePlayerMission(Player player, String mission) {
        updateMission(player, getFullMission(player, mission), 1);
    }

    public void updatePlayerMission(Player player, String mission, Integer amount) {
        updateMission(player, getFullMission(player, mission), amount);
    }

    private void updateMission(Player player, String mission, Integer incAmount) {
        String[] mSplit = mission.split(":");

        int needed;
        int amount = 0;
        byte completed = 0;

        if(mSplit[1].contains("_")) {
            needed = Integer.parseInt(mSplit[1].split("_")[0]);
            amount = Integer.parseInt(mSplit[1].split("_")[1]);
        } else {
            needed = Integer.parseInt(mSplit[1]);
        }
        ArrayList<String> missions = getFullMissions(player);
        amount += incAmount;
        String uMission = mSplit[0] + ":" + needed + "_" + amount;

        if(amount >= needed) {
            completed = 1;
            Random randInt = new Random();
            int reward = randInt.nextInt(25) + 25;
            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), reward, "Daily Mission", mSplit[0] + ":" + needed);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

            uMission = uMission + ":completed";

            String sql = "UPDATE users SET missions_completed = missions_completed + ? WHERE user_id = ?";
            List<Object> params = new ArrayList<>() {{
                add(1);
                add(player.getUniqueId());
            }};
            hook.sqlUpdate(sql, params);
        }

        missions.set(missions.indexOf(mission), uMission);
        plugin.missions.put(player.getUniqueId(), missions);

        String sql = "UPDATE daily_missions SET amount = ?, completed = ? WHERE user_id = ? AND type = ?";
        int finalCompleted = completed;
        int finalAmount = amount;
        List<Object> params = new ArrayList<>() {{
            add(finalAmount);
            add(finalCompleted);
            add(player.getUniqueId().toString());
            add(mSplit[0]);
        }};
        hook.sqlUpdate(sql, params);
    }
}
