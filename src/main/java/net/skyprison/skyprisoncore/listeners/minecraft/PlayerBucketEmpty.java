package net.skyprison.skyprisoncore.listeners.minecraft;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.Objects;

public class PlayerBucketEmpty implements Listener {
    public PlayerBucketEmpty() {
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        if(!event.isCancelled()) {
            Player player = event.getPlayer();
            World pWorld = player.getWorld();
            if (event.getBucket().equals(Material.LAVA_BUCKET)) {
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(pWorld));
                ApplicableRegionSet regionList = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()));
                if (pWorld.getName().equalsIgnoreCase("world_prison")) {
                    if(regionList.getRegions().contains(regions.getRegion("grass-mine"))) {
                        event.setCancelled(true);
                    } else if(regionList.getRegions().contains(regions.getRegion("desert-mine"))) {
                        event.setCancelled(true);
                    } else if(regionList.getRegions().contains(regions.getRegion("nether-mine"))) {
                        event.setCancelled(true);
                    } else if(regionList.getRegions().contains(regions.getRegion("snow-mine"))) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
