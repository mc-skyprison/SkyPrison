package net.skyprison.skyprisoncore.listeners.plugins;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.NoChance.PvPManager.Events.PlayerTagEvent;
import me.NoChance.PvPManager.Events.PlayerTogglePvPEvent;
import me.NoChance.PvPManager.Events.PlayerUntagEvent;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PvPManagerListeners implements Listener {
    private final SkyPrisonCore plugin;

    public PvPManagerListeners(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        if(!player.getWorld().getName().equalsIgnoreCase("world_prison")) return;

        com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(player.getLocation());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (query.testState(toLoc, localPlayer, ClaimUtils.FLY)) {
            plugin.flyPvP.put(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void onPlayerUntag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(player.getLocation());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (query.testState(toLoc, localPlayer, ClaimUtils.FLY)) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPlayerTogglePvP(PlayerTogglePvPEvent event) {
        if(!(TabAPI.getInstance().getNameTagManager() instanceof UnlimitedNameTagManager nametagManager)) return;

        TabPlayer tPlayer = TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId());
        if(tPlayer == null) return;

        nametagManager.setLine(tPlayer, "belowname", event.getPvPState() ? null : "<red>PVP OFF</red>");
    }
}
