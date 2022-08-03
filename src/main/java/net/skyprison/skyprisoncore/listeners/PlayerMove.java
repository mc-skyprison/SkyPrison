package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Objects;

public class PlayerMove implements Listener {
    private final SkyPrisonCore plugin;

    public PlayerMove(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PvPManager pvpmanager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        PlayerHandler playerHandler = Objects.requireNonNull(pvpmanager).getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
        if(plugin.teleportMove.containsKey(player.getUniqueId())) {
            Location toLoc = event.getTo();
            Location fromLoc = event.getFrom();
            if(toLoc.getBlockX() != fromLoc.getBlockX() || toLoc.getBlockZ() != fromLoc.getBlockZ()) {
                plugin.getServer().getScheduler().cancelTask(plugin.teleportMove.get(player.getUniqueId()));
                plugin.teleportMove.remove(player.getUniqueId());
                player.sendMessage(plugin.colourMessage("&cTeleport Cancelled!"));
            }
        }
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                Location toLoc = event.getTo();
                Location fromLoc = event.getFrom();
                if(toLoc.getBlockX() != fromLoc.getBlockX() || toLoc.getBlockZ() != fromLoc.getBlockZ()) {
                    boolean toFly = true;
                    boolean fromFly = true;

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
                    final ApplicableRegionSet regionListTo = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                            toLoc.getBlockY(), toLoc.getBlockZ()));
                    final ApplicableRegionSet regionListFrom = regions.getApplicableRegions(BlockVector3.at(fromLoc.getBlockX(),
                            fromLoc.getBlockY(), fromLoc.getBlockZ()));

                    for (int i = 0; i <= toLoc.getBlockY(); i++) {
                        Location blockCheck = new Location(toLoc.getWorld(), toLoc.getBlockX(), toLoc.getBlockY() - i, toLoc.getBlockZ());
                        Block block = blockCheck.getBlock();
                        if (block.isSolid() && !block.getType().equals(Material.BARRIER) && !block.isLiquid()  && !block.isPassable()) {
                            toFly = false;
                            break;
                        }
                    }
                    for (int i = 0; i <= fromLoc.getBlockY(); i++) {
                        Location blockCheck = new Location(fromLoc.getWorld(), fromLoc.getBlockX(), fromLoc.getBlockY() - i, fromLoc.getBlockZ());
                        Block block = blockCheck.getBlock();
                        if (block.isSolid() && !block.getType().equals(Material.BARRIER) && !block.isLiquid() && !block.isPassable()) {
                            fromFly = false;
                            break;
                        }
                    }
                    for (ProtectedRegion region : regionListTo) {
                        if(region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                            toFly = true;
                            break;
                        }
                    }
                    for (ProtectedRegion region : regionListFrom) {
                        if(region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                            fromFly = true;
                            break;
                        }
                    }
                    if (toFly && !fromFly) {
                        if (!pvpPlayer.isInCombat()) {
                            player.setAllowFlight(true);
                            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can fly now!");
                        }
                    } else if (!toFly && fromFly) {
                        player.setAllowFlight(false);
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                    }
                }
                if (pvpPlayer.isInCombat()) {
                    if (toLoc.getBlockY() <= 3) {
                        pvpPlayer.unTag();
                    }
                }
            }
        }
        if (player.hasPermission("skyprisoncore.guard.onduty") && !player.isOp()) {
            ArrayList<String> guardWorlds = (ArrayList<String>) plugin.getConfig().getStringList("guard-worlds");
            boolean inWorld = false;
            for(String guardWorld : Objects.requireNonNull(guardWorlds)) {
                if(player.getWorld().getName().equalsIgnoreCase(guardWorld)) {
                    inWorld = true;
                    break;
                }
            }
            if(!inWorld) {
                if((event.getFrom().getBlockX() != event.getTo().getBlockX()) || (event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Please go off duty!");
                }
            }
        }
    }
}
