package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.utils.DailyMissions;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFish implements Listener {
    private final DailyMissions dailyMissions;

    public PlayerFish(DailyMissions dailyMissions) {
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if(event.getCaught() != null && event.getCaught() instanceof Item item && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Material caught = item.getItemStack().getType();
            Player player = event.getPlayer();
            for (String mission : dailyMissions.getMissions(player)) {
                if(!dailyMissions.isCompleted(player, mission)) {
                    String[] missSplit = mission.split("-");
                    if (missSplit[0].equalsIgnoreCase("fish")) {
                        switch (missSplit[1].toLowerCase()) {
                            case "any" -> dailyMissions.updatePlayerMission(player, mission);
                            case "pufferfish" -> {
                                if (caught.equals(Material.PUFFERFISH)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                            }
                            case "salmon" -> {
                                if (caught.equals(Material.SALMON)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                            }
                            case "cod" -> {
                                if (caught.equals(Material.COD)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                            }
                            case "tropical_fish" -> {
                                if (caught.equals(Material.TROPICAL_FISH)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
