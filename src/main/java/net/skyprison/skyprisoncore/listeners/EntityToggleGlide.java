package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class EntityToggleGlide implements Listener {
    public EntityToggleGlide() {
    }

    @EventHandler
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if(event.isGliding()) {
            Player player = (Player) event.getEntity();
            Location pLoc = player.getLocation();
            boolean canFly = true;

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            if(regions != null) {
                final ApplicableRegionSet regionListFrom = regions.getApplicableRegions(BlockVector3.at(pLoc.getBlockX(),
                        pLoc.getBlockY(), pLoc.getBlockZ()));


                for (int i = 0; i <= pLoc.getBlockY(); i++) {
                    Location blockCheck = new Location(pLoc.getWorld(), pLoc.getBlockX(), pLoc.getBlockY() - i, pLoc.getBlockZ());
                    Block block = blockCheck.getBlock();
                    if (block.isSolid() && !block.getType().equals(Material.BARRIER) && !block.isLiquid() && !block.isPassable()) {
                        canFly = false;
                        break;
                    }
                }

                for (ProtectedRegion region : regionListFrom) {
                    if (region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                        canFly = true;
                        break;
                    }
                }

                if (!canFly) {
                    player.setGliding(false);
                    player.setVelocity(player.getVelocity().multiply(-1));
                }
            }
        }
    }
}
