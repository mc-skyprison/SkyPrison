package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.utils.DailyMissions;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFish implements Listener {
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if(event.getCaught() != null && event.getCaught() instanceof Item item && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Material caught = item.getItemStack().getType();
            Player player = event.getPlayer();
            DailyMissions.updatePlayerMissions(player.getUniqueId(), "fish", caught);
        }
    }
}
