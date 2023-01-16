package net.skyprison.skyprisoncore.listeners.parkour;

import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.event.PlayerFinishCourseEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class PlayerFinishCourse implements Listener {
    private final SkyPrisonCore plugin;

    public PlayerFinishCourse(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCourseCompletion(PlayerFinishCourseEvent event) {
        Player player = event.getPlayer();
        List<String> uncompletedCourses = Parkour.getInstance().getPlayerManager().getUncompletedCourses(player);
        if(uncompletedCourses.isEmpty()) {
            plugin.asConsole("lp user " + player.getName() + " permission set deluxetags.tag.Parkourist");
        }
    }
}
