package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Random;

public class PlayerFish implements Listener {
    private final SkyPrisonCore plugin;
    private DailyMissions dailyMissions;

    public PlayerFish(SkyPrisonCore plugin, DailyMissions dailyMissions) {
        this.plugin = plugin;
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if(event.getCaught() != null) {
            Player player = event.getPlayer();

            for (String mission : dailyMissions.getPlayerMissions(player)) {
                String[] missSplit = mission.split("-");
                if (missSplit[0].equalsIgnoreCase("fish")) {
                    int currAmount = Integer.parseInt(missSplit[4]) + 1;
                    String nMission = missSplit[0] + "-" + missSplit[1] + "-" + missSplit[2] + "-" + missSplit[3] + "-" + currAmount;
                    switch (missSplit[1].toLowerCase()) {
                        case "any":
                            dailyMissions.updatePlayerMission(player, mission, nMission);
                            break;
                        case "pufferfish":
                            if (event.getCaught().getType().equals(EntityType.PUFFERFISH)) {
                                dailyMissions.updatePlayerMission(player, mission, nMission);
                            }
                            break;
                    }


                    if (dailyMissions.missionComplete(player, nMission)) {
                        Random randInt = new Random();
                        int reward = randInt.nextInt(25) + 25;
                        plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), reward);
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    }
                }
            }
        }
    }
}
