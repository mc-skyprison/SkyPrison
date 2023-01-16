package net.skyprison.skyprisoncore.listeners.cmi;

import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.events.CMIPlayerUnjailEvent;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerUnJail implements Listener {
    @EventHandler
    public void onPlayerUnJail(CMIPlayerUnjailEvent event) {
        CMIUser user = event.getUser();
        user.getPlayer().playSound(user.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

}
