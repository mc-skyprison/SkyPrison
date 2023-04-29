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
import net.skyprison.skyprisoncore.SkyPrisonCore;
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
            if (event.getBlock().getWorld().getName().equalsIgnoreCase("world_prison") && player.isOp()) {
                ItemStack bomb = event.getItemInHand();
                Block block = event.getBlockPlaced();
                if (bomb.getType().equals(Material.PLAYER_HEAD)) {
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
                            if (!bombType.equalsIgnoreCase("nuke")) {
                                Location loc = block.getLocation();
                                List<Block> blocks = new ArrayList<>();
                                int radius = 0;
                                switch (bombType) {
                                    case "small":
                                        radius = 1;
                                        break;
                                    case "medium":
                                        radius = 2;
                                        break;
                                    case "large":
                                        radius = 3;
                                        break;
                                    case "massive":
                                        radius = 4;
                                        break;
                                }
                                for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
                                    for (int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; y++) {
                                        for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++) {
                                            blocks.add(loc.getWorld().getBlockAt(x, y, z));
                                        }
                                    }
                                }
                                Location disLoc = new Location(block.getWorld(), loc.getX()+0.5, loc.getY()+1, loc.getZ()+0.5);
                                TextDisplay ent = (TextDisplay) block.getWorld().spawnEntity(disLoc, EntityType.TEXT_DISPLAY);
                                ent.setBillboard(Display.Billboard.CENTER);
                                String name = bombType.substring(0, 1).toUpperCase() + bombType.substring(1);
                                ent.text(Component.text(plugin.colourMessage("&e" + name + " Bomb\n&c▊▊▊")));
                                ent.setDefaultBackground(false);

                                LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
                                RegionQuery query = regionContainer.createQuery();

                                new BukkitRunnable() {
                                    int i = 0;

                                    @Override
                                    public void run() {
                                        i++;
                                        switch(i) {
                                            case 1:
                                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                                ent.text(Component.text(plugin.colourMessage("&e" + name + " Bomb\n&a▊&c▊▊")));
                                                break;
                                            case 2:
                                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                                ent.text(Component.text(plugin.colourMessage("&e" + name + " Bomb\n&a▊▊&c▊")));
                                                break;
                                            case 3:
                                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                                ent.text(Component.text(plugin.colourMessage("&e" + name + " Bomb\n&a▊▊▊")));
                                                break;
                                            case 4:
                                                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                                                player.spawnParticle(Particle.EXPLOSION_NORMAL, block.getLocation(), 20);
                                                ent.remove();
                                                for(Block block1 : blocks) {
                                                    if(!block1.equals(block)) {
                                                        com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(block1.getLocation());
                                                        if (query.testState(toLoc, localPlayer, Flags.BLOCK_BREAK)) {
                                                            block1.breakNaturally();
                                                        }
                                                    } else {
                                                        block1.setType(Material.AIR);
                                                    }
                                                }
                                                this.cancel();
                                                break;

                                        }
                                    }
                                }.runTaskTimer(plugin, 20, 20);
                            } else {
                                // SELECT MINE REGION AND GO BOOM BOOM
                            }
                        } else {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
}
