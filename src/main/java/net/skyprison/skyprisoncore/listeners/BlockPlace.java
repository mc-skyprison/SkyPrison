package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

public class BlockPlace implements Listener {

    private SkyPrisonCore plugin;

    public BlockPlace(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(event.getPlayer());
        user.getLastBlockLeave();
        if ((!event.canBuild() || event.isCancelled()) && !event.getPlayer().hasPermission("antiblockjump.bypass"))
            event.getPlayer().setVelocity(new Vector(0, -5, 0));
    }
}
