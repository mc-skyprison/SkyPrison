package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockPlace implements Listener {

    private final SkyPrisonCore plugin;
    private final DailyMissions dm;

    public BlockPlace(SkyPrisonCore plugin, DailyMissions dm) {
        this.plugin = plugin;
        this.dm = dm;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if ((!event.canBuild() || event.isCancelled()) && !event.getPlayer().hasPermission("antiblockjump.bypass")) {
            player.setVelocity(new Vector(0, -5, 0));
        } else {
            if (event.getBlock().getWorld().getName().equalsIgnoreCase("world_prison")) {
                ItemStack bomb = event.getItemInHand();
                Block block = event.getBlockPlaced();
                if (bomb.getType().equals(Material.PLAYER_HEAD) || bomb.getType().equals(Material.PLAYER_WALL_HEAD)) {
                    NamespacedKey key = new NamespacedKey(plugin, "bomb-type");
                    if (bomb.getPersistentDataContainer().has(key)) {
                        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                        ApplicableRegionSet regionList = regionManager.getApplicableRegions(BlockVector3.at(block.getLocation().getX(),
                                block.getLocation().getY(), block.getLocation().getZ()));
                        ProtectedRegion mineRegion = null;
                        for (ProtectedRegion region : regionList.getRegions()) {
                            if (region.getId().contains("mine") && !region.getId().contains("exit")) {
                                mineRegion = region;
                                break;
                            }
                        }
                        if (mineRegion != null) {
                            String bombType = bomb.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                            Location loc = block.getLocation();
                            List<Block> blocks = new ArrayList<>();
                            Random random = new Random();
                            int radius = switch (bombType) {
                                case "small" -> 2;
                                case "medium" -> 3;
                                case "large" -> 4;
                                case "massive" -> 5;
                                case "nuke" -> 6;
                                default -> 0;
                            };

                            World world = loc.getWorld();
                            if(radius != 6) {
                                int centerX = loc.getBlockX();
                                int centerY = loc.getBlockY();
                                int centerZ = loc.getBlockZ();

                                for (int x = centerX - radius; x <= centerX + radius; x++) {
                                    for (int y = centerY - radius; y <= centerY + radius; y++) {
                                        for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                                            double distance = Math.sqrt(
                                                    Math.pow(x - centerX, 2) +
                                                            Math.pow(y - centerY, 2) +
                                                            Math.pow(z - centerZ, 2)
                                            );

                                            // Add randomness to the edges
                                            double threshold = radius - (radius * 0.2) + (random.nextDouble() * (radius * 0.4));

                                            if (distance <= threshold) {
                                                blocks.add(world.getBlockAt(x, y, z));
                                            }
                                        }
                                    }
                                }
                            } else {
                                BlockVector3 min = mineRegion.getMinimumPoint();
                                BlockVector3 max = mineRegion.getMaximumPoint();

                                for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                                    for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                                            blocks.add(loc.getWorld().getBlockAt(x, y, z));
                                        }
                                    }
                                }
                            }

                            Location disLoc = new Location(block.getWorld(), loc.getX() + 0.5, loc.getY() + 1, loc.getZ() + 0.5);
                            TextDisplay ent = (TextDisplay) block.getWorld().spawnEntity(disLoc, EntityType.TEXT_DISPLAY);
                            ent.setBillboard(Display.Billboard.CENTER);
                            String name = bombType.substring(0, 1).toUpperCase() + bombType.substring(1);
                            String red = "&c▊";
                            String green = "&a▊";
                            String title = "&e" + name + " Bomb\n";
                            TextComponent bombTitle = Component.text(plugin.colourMessage(title + red.repeat(radius)));
                            ent.text(bombTitle);
                            ent.setDefaultBackground(false);

                            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                            RegionQuery query = regionContainer.createQuery();
                            plugin.bombLocs.add(loc);

                            new BukkitRunnable() {
                                int redAmount = radius;
                                int greenAmount = 0;
                                @Override
                                public void run() {
                                    redAmount--;
                                    if(redAmount >= 0) {
                                        greenAmount++;
                                        TextComponent nBombTitle = Component.text(plugin.colourMessage(title + green.repeat(greenAmount) + red.repeat(redAmount)));
                                        ent.text(nBombTitle);
                                        world.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                    } else {
                                        world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                                        world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
                                        ent.remove();
                                        double delay = 0.0;
                                        for(Block block1 : blocks) {
                                            if(!block1.getType().equals(Material.PLAYER_HEAD) && !block1.getType().equals(Material.PLAYER_WALL_HEAD)) {
                                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                                    com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(block1.getLocation());
                                                    if (query.testState(toLoc, localPlayer, Flags.BLOCK_BREAK)) {
                                                        block1.breakNaturally();
                                                    }
                                                }, (int)Math.floor(delay));
                                                delay += 0.004;
                                            } else {
                                                if(block1.equals(block)) {
                                                    block1.setType(Material.AIR);
                                                } else if(!plugin.bombLocs.contains(block1.getLocation())) {
                                                    block1.breakNaturally();
                                                }
                                            }
                                        }
                                        plugin.bombLocs.remove(loc);
                                        for(String mission : dm.getMissions(player)) {
                                            if(mission.startsWith("bomb") && !dm.isCompleted(player, mission)) {
                                                dm.updatePlayerMission(player, mission);
                                            }
                                        }
                                        this.cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 20, 20);
                        } else {
                            player.sendMessage(plugin.colourMessage("&cYou can only use this in the mines!"));
                            event.setCancelled(true);
                        }
                    }
                }
            } else {
                ItemStack bomb = event.getItemInHand();
                if (bomb.getType().equals(Material.PLAYER_HEAD) || bomb.getType().equals(Material.PLAYER_WALL_HEAD)) {
                    NamespacedKey key = new NamespacedKey(plugin, "bomb-type");
                    if (bomb.getPersistentDataContainer().has(key) && !player.isOp()) {
                        player.sendMessage(plugin.colourMessage("&cYou can only use this in the mines!"));
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
