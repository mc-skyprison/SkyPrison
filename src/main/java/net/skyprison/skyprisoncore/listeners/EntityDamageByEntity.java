package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

    @EventHandler
    public void guardHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damagee = (Player) event.getEntity();

            if (damager.getWorld().getName().equalsIgnoreCase("world_prison")) {
                Material dmgWpn = damager.getInventory().getItemInMainHand().getType();
                if (dmgWpn.equals(Material.WOODEN_AXE) || dmgWpn.equals(Material.STONE_AXE) || dmgWpn.equals(Material.IRON_AXE) || dmgWpn.equals(Material.DIAMOND_AXE) || dmgWpn.equals(Material.NETHERITE_AXE)) {
                    event.setDamage(event.getDamage() - 4);
                }
            }

            com.sk89q.worldedit.util.Location damagerLoc = BukkitAdapter.adapt(damager.getLocation());
            com.sk89q.worldedit.util.Location damageeLoc = BukkitAdapter.adapt(damagee.getLocation());
            LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer(damager);
            LocalPlayer localDamagee = WorldGuardPlugin.inst().wrapPlayer(damagee);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            if (plugin.flyPvP.containsKey(damager.getUniqueId()) && !plugin.flyPvP.containsKey(damagee.getUniqueId())) {
                plugin.flyPvP.remove(damager.getUniqueId());
            } else if (query.testState(damagerLoc, localDamager, plugin.FLY) && !query.testState(damageeLoc, localDamagee, plugin.FLY)) {
                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.flyPvP.remove(damager.getUniqueId()), 1L);
            }
            if (damager.hasPermission("skyprisoncore.guard.onduty") && damagee.hasPermission("skyprisoncore.guard.onduty")) {
                event.setCancelled(true);
            } else if (damagee.hasPermission("skyprisoncore.showhit")) {
                Map.Entry<Player, Long> lasthit = plugin.hitcd.get(damager);
                if (plugin.hitcd.get(damager) == null || (lasthit.getKey() == damagee && System.currentTimeMillis() / 1000L - lasthit.getValue() > 5L) || lasthit.getKey() != damagee) {
                    damagee.sendMessage(ChatColor.RED + "You have been hit by " + damager.getName());
                    plugin.hitcd.put(damager, new AbstractMap.SimpleEntry<>(damagee, System.currentTimeMillis() / 1000L));
                }
            }
        } else if(event.getDamager() instanceof Projectile) {
            Projectile pArrow = (Projectile) event.getDamager();
            if(pArrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) pArrow.getShooter();
                Player damagee = (Player) event.getEntity();

                com.sk89q.worldedit.util.Location damagerLoc = BukkitAdapter.adapt(damager.getLocation());
                com.sk89q.worldedit.util.Location damageeLoc = BukkitAdapter.adapt(damagee.getLocation());
                LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer(damager);
                LocalPlayer localDamagee = WorldGuardPlugin.inst().wrapPlayer(damagee);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                if (plugin.flyPvP.containsKey(damager.getUniqueId()) && !plugin.flyPvP.containsKey(damagee.getUniqueId())) {
                    plugin.flyPvP.remove(damager.getUniqueId());
                } else if (query.testState(damagerLoc, localDamager, plugin.FLY) && !query.testState(damageeLoc, localDamagee, plugin.FLY)) {
                    plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> plugin.flyPvP.remove(damager.getUniqueId()), 1L);
                }
                if (damager.hasPermission("skyprisoncore.guard.onduty") && damagee.hasPermission("skyprisoncore.guard.onduty")) {
                    event.setCancelled(true);
                } else if (damagee.hasPermission("skyprisoncore.showhit")) {
                    Map.Entry<Player, Long> lasthit = plugin.hitcd.get(damager);
                    if (plugin.hitcd.get(damager) == null || (lasthit.getKey() == damagee && System.currentTimeMillis() / 1000L - lasthit.getValue() > 5L) || lasthit.getKey() != damagee) {
                        damagee.sendMessage(ChatColor.RED + "You have been shot by " + damager.getName());
                        plugin.hitcd.put(damager, new AbstractMap.SimpleEntry<>(damagee, System.currentTimeMillis() / 1000L));
                    }
                }
            }
        }
    }
}
