package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BlockPlace implements Listener {

    private final SkyPrisonCore plugin;

    public BlockPlace(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if ((!event.canBuild() || event.isCancelled()) && !event.getPlayer().hasPermission("antiblockjump.bypass")) {
            player.setVelocity(new Vector(0, -5, 0));
        } else {
            if (event.getBlock().getWorld().getName().equalsIgnoreCase("world_prison") && !player.isOp()) {
                if (event.getBlock() instanceof Skull) {
                    Skull bomb = (Skull) event.getBlock();
                    NamespacedKey key = new NamespacedKey(plugin, "bomb-type");
                    if (bomb.getPersistentDataContainer().has(key)) {
                        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                        ApplicableRegionSet regionList = regionManager.getApplicableRegions(BlockVector3.at(bomb.getLocation().getX(),
                                bomb.getLocation().getY(), bomb.getLocation().getZ()));
                        ProtectedRegion mineRegion = null;
                        for (ProtectedRegion region : regionList.getRegions()) {
                            if (region.getId().contains("mine") && !region.getId().contains("exit")) {
                                mineRegion = region;
                                break;
                            }
                        }
                        if (mineRegion != null) {
                            player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 1);
                            String bombType = bomb.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                            if (!bombType.equalsIgnoreCase("nuke")) {
                                Location loc = bomb.getLocation();
                                List<Block> blocks = new ArrayList<>();
                                int radius = 0;
                                switch (bombType) {
                                    case "small":
                                        radius = 2;
                                        break;
                                    case "medium":
                                        radius = 4;
                                        break;
                                    case "large":
                                        radius = 6;
                                        break;
                                    case "massive":
                                        radius = 8;
                                        break;
                                }
                                for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
                                    for (int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; y++) {
                                        for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++) {
                                            blocks.add(loc.getWorld().getBlockAt(x, y, z));
                                        }
                                    }
                                }
                                blocks.forEach(player::breakBlock);
                            } else {
                                // SELECT MINE REGION AND GO BOOM BOOM
                            }
                        }
                    }
                }
            }
        }
    }
}
