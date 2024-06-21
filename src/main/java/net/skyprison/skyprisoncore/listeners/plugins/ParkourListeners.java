package net.skyprison.skyprisoncore.listeners.plugins;

import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.event.ParkourFinishEvent;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class ParkourListeners implements Listener {
    @EventHandler
    public void onParkourFinish(ParkourFinishEvent event) {
        Player player = event.getPlayer();
        List<String> uncompletedCourses = Parkour.getInstance().getPlayerManager().getUncompletedCourses(player);
        if(uncompletedCourses.isEmpty()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.tag.17");
        }

        if(!event.getEventName().equalsIgnoreCase("parkour1")) {
            DailyMissions.updatePlayerMissions(player.getUniqueId(), "parkour", "");
        }
    }
}
