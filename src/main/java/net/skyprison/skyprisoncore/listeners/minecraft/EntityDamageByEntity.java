package net.skyprison.skyprisoncore.listeners.minecraft;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.AbstractMap;
import java.util.Map;

public class EntityDamageByEntity implements Listener {
    private final SkyPrisonCore plugin;

    public EntityDamageByEntity (SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    private boolean damageCheck(Player damager, Player damagee) {
        Location damagerLoc = BukkitAdapter.adapt(damager.getLocation());
        Location damageeLoc = BukkitAdapter.adapt(damagee.getLocation());

        LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer(damager);
        LocalPlayer localDamagee = WorldGuardPlugin.inst().wrapPlayer(damagee);

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (plugin.flyPvP.containsKey(damager.getUniqueId()) && !plugin.flyPvP.containsKey(damagee.getUniqueId())) {
            plugin.flyPvP.remove(damager.getUniqueId());
        } else if (query.testState(damagerLoc, localDamager, ClaimUtils.FLY) && !query.testState(damageeLoc, localDamagee, ClaimUtils.FLY)) {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.flyPvP.remove(damager.getUniqueId()), 1L);
        }

        if (damager.hasPermission("skyprisoncore.guard.onduty") && damagee.hasPermission("skyprisoncore.guard.onduty")) {
            return true;
        } else if (damagee.hasPermission("skyprisoncore.showhit")) {
            Map.Entry<Player, Long> lastHit = plugin.hitcd.get(damager);
            if (plugin.hitcd.get(damager) == null || (lastHit.getKey() == damagee && System.currentTimeMillis() / 1000L - lastHit.getValue() > 5L) || lastHit.getKey() != damagee) {
                damagee.sendMessage(Component.text("You have been hit by " + damager.getName(), NamedTextColor.RED));
                plugin.hitcd.put(damager, new AbstractMap.SimpleEntry<>(damagee, System.currentTimeMillis() / 1000L));
            }
        }
        return false;
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player damagee)) return;
        switch (event.getDamager()) {
            case Player damager -> {
                if(damageCheck(damager, damagee)) event.setCancelled(true);
            }
            case Projectile pArrow -> {
                if(!(pArrow.getShooter() instanceof Player damager)) return;
                if(damageCheck(damager, damagee)) event.setCancelled(true);
            }
            default -> {}
        }
    }
}
