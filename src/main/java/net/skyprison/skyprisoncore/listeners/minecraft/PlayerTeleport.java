package net.skyprison.skyprisoncore.listeners.minecraft;

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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Comparator;

public class PlayerTeleport implements Listener {
    private final SkyPrisonCore plugin;

    public PlayerTeleport(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    private void enableFlight(Player player, boolean fromFlight) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(true), 1L);
        if (!fromFlight) {
            player.sendMessage(Component.text("You can fly now!", NamedTextColor.AQUA, TextDecoration.BOLD));
        }
    }
    private void disableFlight(Player player) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> player.setAllowFlight(false), 1L);
        player.sendMessage(Component.text("You can no longer fly!", NamedTextColor.AQUA, TextDecoration.BOLD));
    }
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PvPManager pvpAPI = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        if(pvpAPI == null) return;

        Player player = event.getPlayer();
        PlayerHandler playerHandler = pvpAPI.getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);

        Location toLoc = event.getTo();
        Location fromLoc = event.getFrom();

        GameMode mode = player.getGameMode();

        if(pvpPlayer.isInCombat() || event.isCancelled() || !mode.equals(GameMode.SURVIVAL)) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionsTo = container.get(BukkitAdapter.adapt(toLoc.getWorld()));
        RegionManager regionsFrom = container.get(BukkitAdapter.adapt(fromLoc.getWorld()));

        if(regionsTo == null || regionsFrom == null) return;

        final ApplicableRegionSet to = regionsTo.getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                toLoc.getBlockY(), toLoc.getBlockZ()));
        final ApplicableRegionSet from = regionsFrom.getApplicableRegions(BlockVector3.at(fromLoc.getBlockX(),
                fromLoc.getBlockY(), fromLoc.getBlockZ()));

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionQuery query = container.createQuery();

        boolean toFlight = query.testState(BukkitAdapter.adapt(toLoc), localPlayer, ClaimUtils.FLY);
        boolean fromFlight = query.testState(BukkitAdapter.adapt(fromLoc), localPlayer, ClaimUtils.FLY);

        if (toFlight) {
            enableFlight(player, fromFlight);
            return;
        } else if (fromFlight) {
            disableFlight(player);
            return;
        }

        ProtectedRegion toRegion = to.getRegions().stream().max(Comparator.comparingInt(ProtectedRegion::getPriority)).orElse(null);
        ProtectedRegion fromRegion = from.getRegions().stream().max(Comparator.comparingInt(ProtectedRegion::getPriority)).orElse(null);

        if(fromRegion == null) return;

        String fromId = fromRegion.getId();
        fromFlight = fromId.contains("fly") && !fromId.contains("nofly") && !fromId.contains("no-fly");
        if(toRegion != null) {
            String toId = toRegion.getId();
            toFlight = toId.contains("fly") && !toId.contains("nofly") && !toId.contains("no-fly");

            if (toFlight) {
                enableFlight(player, fromFlight);
            } else if (fromFlight) {
                disableFlight(player);
            }
        } else {
            if (fromFlight) {
                disableFlight(player);
            }
        }
    }
}
