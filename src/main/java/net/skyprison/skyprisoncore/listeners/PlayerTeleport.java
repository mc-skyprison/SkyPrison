package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class PlayerTeleport implements Listener {

    private final SkyPrisonCore plugin;

    public PlayerTeleport(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PvPManager pvpmanager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        PlayerHandler playerHandler = Objects.requireNonNull(pvpmanager).getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
        Location toLoc = event.getTo();
        Location fromLoc = event.getFrom();

        if(!pvpPlayer.isInCombat() && !event.isCancelled() && !player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionsTo = container.get(BukkitAdapter.adapt(toLoc.getWorld()));
            RegionManager regionsFrom = container.get(BukkitAdapter.adapt(fromLoc.getWorld()));
            final ApplicableRegionSet regionListTo = Objects.requireNonNull(regionsTo).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                    toLoc.getBlockY(), toLoc.getBlockZ()));
            final ApplicableRegionSet regionListFrom = Objects.requireNonNull(regionsFrom).getApplicableRegions(BlockVector3.at(fromLoc.getBlockX(),
                    fromLoc.getBlockY(), fromLoc.getBlockZ()));

            com.sk89q.worldedit.util.Location fromLocWE = BukkitAdapter.adapt(event.getFrom());
            com.sk89q.worldedit.util.Location toLocWE = BukkitAdapter.adapt(toLoc);
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();

            if(!regionListTo.getRegions().isEmpty() && !regionListFrom.getRegions().isEmpty()) {
                ProtectedRegion toRegion = null;
                for (final ProtectedRegion rg : regionListTo) {
                    if(toRegion == null)
                        toRegion = rg;
                    if(rg.getPriority() > toRegion.getPriority()) {
                        toRegion = rg;
                    }
                }
                ProtectedRegion fromRegion = null;
                for (final ProtectedRegion rg : regionListFrom) {
                    if(fromRegion == null)
                        fromRegion = rg;
                    if(rg.getPriority() > fromRegion.getPriority()) {
                        fromRegion = rg;
                    }
                }
                if (query.testState(toLocWE, localPlayer, SkyPrisonCore.FLY) || (toRegion.getId().contains("fly") && !toRegion.getId().contains("nofly") && !toRegion.getId().contains("no-fly"))) {
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(true), 1L);
                    if (!query.testState(fromLocWE, localPlayer, SkyPrisonCore.FLY)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can fly now!");
                    }
                } else if (query.testState(fromLocWE, localPlayer, SkyPrisonCore.FLY) || (fromRegion.getId().contains("fly") && !fromRegion.getId().contains("nofly") && !fromRegion.getId().contains("no-fly"))) {
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(false), 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                    }
                }
            } else {
                if (query.testState(toLocWE, localPlayer, SkyPrisonCore.FLY)) {
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(true), 1L);
                    if (!query.testState(fromLocWE, localPlayer, SkyPrisonCore.FLY)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can fly now!");
                    }
                } else if (query.testState(fromLocWE, localPlayer, SkyPrisonCore.FLY)) {
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(false), 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                    }
                }
                if(!regionListFrom.getRegions().isEmpty()) {
                    ProtectedRegion fromRegion = null;
                    for (final ProtectedRegion rg : regionListFrom) {
                        if(fromRegion == null)
                            fromRegion = rg;
                        if(rg.getPriority() > fromRegion.getPriority()) {
                            fromRegion = rg;
                        }
                    }
                    if (query.testState(fromLocWE, localPlayer, SkyPrisonCore.FLY) || (fromRegion.getId().contains("fly") && !fromRegion.getId().contains("nofly") && !fromRegion.getId().contains("no-fly"))) {
                        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(false), 1L);
                        if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                        }
                    }
                }
            }
        }
    }
}
