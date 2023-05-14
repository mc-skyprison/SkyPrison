package net.skyprison.skyprisoncore.listeners.parkour;

import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.event.ParkourFinishEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class ParkourFinish implements Listener {
    private final SkyPrisonCore plugin;
    private final DailyMissions dm;

    public ParkourFinish(SkyPrisonCore plugin, DailyMissions dm) {
        this.plugin = plugin;
        this.dm = dm;
    }

    @EventHandler
    public void onParkourFinish(ParkourFinishEvent event) {
        Player player = event.getPlayer();
        List<String> uncompletedCourses = Parkour.getInstance().getPlayerManager().getUncompletedCourses(player);
        if(uncompletedCourses.isEmpty()) {
            plugin.asConsole("lp user " + player.getName() + " permission set skyprisoncore.tag.17");
        }

        if(!event.getEventName().equalsIgnoreCase("parkour1")) {
            for (String mission : dm.getMissions(player)) {
                if (!dm.isCompleted(player, mission)) {
                    String[] missSplit = mission.split("-");
                    if (missSplit[0].equalsIgnoreCase("parkour")) {
                        dm.updatePlayerMission(player, mission);
                    }
                }
            }
        }
    }
}
