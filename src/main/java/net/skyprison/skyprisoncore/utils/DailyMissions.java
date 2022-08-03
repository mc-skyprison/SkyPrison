package net.skyprison.skyprisoncore.utils;

import org.bukkit.entity.Player;

import java.util.*;

public class DailyMissions {

    private HashMap<UUID, ArrayList<String>> playerMissions = new HashMap<>();

    //Mission Type - Specific Type in type - Mission Title - Amount to reach - Current amount
    private ArrayList<String> getMissions() {
        ArrayList<String> missions = new ArrayList<>();
        missions.add("secrets-any-Find Secrets");
        missions.add("fish-any-Go Fishing");
        missions.add("fish-pufferfish-Fish Pufferfish");
        missions.add("fish-cod-Fish Cod");
        missions.add("fish-salmon-Fish Salmon");
        missions.add("fish-tropical_fish-Fish Tropical Fish");
        missions.add("casino-basic-Gamble Basic");

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
                case "casino":
                    missionsToGive.add(mission  + "-" + (randInt.nextInt(5) + 3) + "-0");
                    break;
            }
        }

        return missionsToGive;
    }

    public void setPlayerMissions(Player player) {
        playerMissions.put(player.getUniqueId(), getMissions());
    }

    public void updatePlayerMission(Player player, String mission, String updatedMission) {
        ArrayList<String> missions = playerMissions.get(player.getUniqueId());
        ArrayList<String> uMissions = new ArrayList<>();
        for(String currMission : missions) {
            if(currMission.equals(mission)) {
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
