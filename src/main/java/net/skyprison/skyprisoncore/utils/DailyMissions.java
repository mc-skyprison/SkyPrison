package net.skyprison.skyprisoncore.utils;

import com.Zrips.CMI.CMI;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.*;

public class DailyMissions {

    private HashMap<UUID, ArrayList<String>> playerMissions = new HashMap<>();

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

        if(ranks.contains("desert")) {
            missions.add("harvest-any-Harvest Crop");
            missions.add("harvest-cactus-Harvest Cactus");
        }

        if(ranks.contains("nether")) {
            missions.add("harvest-pumpkin-Harvest Pumpkin");
            missions.add("harvest-nether_wart-Harvest Nether Wart");
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
                case "secrets":
                    missionsToGive.add(mission  + "-" + (randInt.nextInt(3) + 2) + "-0");
                    break;
                case "fish":
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "pufferfish":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(5) + 5) + "-0");
                            break;
                        case "cod":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(10) + 5) + "-0");
                            break;
                        case "salmon":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(10) + 5) + "-0");
                            break;
                        case "tropical_fish":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(5) + 3) + "-0");
                            break;
                    }
                    break;
                case "kill":
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "zombie":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "skeleton":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                    }
                    break;
                case "break":
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "birch_log":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                    }
                    break;
                case "harvest":
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "cactus":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "pumpkin":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "nether_wart":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "sugar_cane":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                    }
                    break;
                case "slaughter":
                    switch(missionSplit[1].toLowerCase()) {
                        case "any":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "pig":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                        case "cow":
                            missionsToGive.add(mission  + "-" + (randInt.nextInt(15) + 10) + "-0");
                            break;
                    }
                    break;
                case "money":
                    missionsToGive.add(mission  + "-" + (randInt.nextInt(1500) + 500) + "-0");
                    break;
                case "sponge":
                    missionsToGive.add(mission  + "-" + (randInt.nextInt(4) + 1) + "-0");
                    break;
            }
        }

        return missionsToGive;
    }

  /*
        Player player = event.getEntity().getKiller();
        for (String mission : dailyMissions.getPlayerMissions(player)) {
            String[] missSplit = mission.split("-");
            if (missSplit[0].equalsIgnoreCase("kill")) {
                int currAmount = Integer.parseInt(missSplit[4]) + 1;
                String nMission = missSplit[0] + "-" + missSplit[1] + "-" + missSplit[2] + "-" + missSplit[3] + "-" + currAmount;
                switch (missSplit[1].toLowerCase()) {
                    case "any":
                        if(event.getEntity() instanceof Monster) {
                            dailyMissions.updatePlayerMission(player, mission, nMission);
                        }
                        break;
                    case "zombie":
                        if (event.getEntityType().equals(EntityType.ZOMBIE)) {
                            dailyMissions.updatePlayerMission(player, mission, nMission);
                        }
                        break;
                }
            }
        }
    */



    public void setPlayerMissions(Player player) {
        List<String> ranks = new ArrayList<>();
        CMI.getInstance().getPlayerManager().getUser(player).getRank().getPreviousRanks().forEach(i -> ranks.add(i.getName()));
        playerMissions.put(player.getUniqueId(), getMissions(ranks));
    }

    public void updatePlayerMission(Player player, String mission, String updatedMission) {
        ArrayList<String> missions = playerMissions.get(player.getUniqueId());
        ArrayList<String> uMissions = new ArrayList<>();
        for(String currMission : missions) {
            if(currMission.equals(mission)) {
                if (missionComplete(player, updatedMission)) {
                    Random randInt = new Random();
                    int reward = randInt.nextInt(25) + 25;
                    //plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), reward);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                }
                uMissions.add(updatedMission);
            } else {
                uMissions.add(currMission);
            }
        }


        playerMissions.put(player.getUniqueId(), uMissions);
    }

    public boolean missionComplete(Player player, String mission) {
        ArrayList<String> missions = playerMissions.get(player.getUniqueId());
        for(String currMission : missions) {
            if(currMission.equals(mission)) {
                String[] currSplit = currMission.split("-");
                if(currSplit[3].equalsIgnoreCase(currSplit[4])) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }


    public ArrayList<String> getPlayerMissions(Player player) {
        if(playerMissions.get(player.getUniqueId()) != null)
            return playerMissions.get(player.getUniqueId());
        else
            return new ArrayList<>();
    }


}
