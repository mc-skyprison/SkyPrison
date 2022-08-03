package net.skyprison.skyprisoncore.listeners;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class PlayerPostRespawn implements Listener {

    private final SkyPrisonCore plugin;

    public PlayerPostRespawn(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
            Location toLoc = player.getLocation();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            final ApplicableRegionSet regionListTo = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                    toLoc.getBlockY(), toLoc.getBlockZ()));
            boolean flyFalse = true;
            for (ProtectedRegion region : regionListTo) {
                if (region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                    flyFalse = false;
                    player.setAllowFlight(true);
                    break;
                }
            }
            if (flyFalse) {
                if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                    player.setAllowFlight(false);
                }
            }
        }
    }
}
