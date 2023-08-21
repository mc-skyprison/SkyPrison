package net.skyprison.skyprisoncore.listeners.minecraft;

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
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockPlace implements Listener {
    private final SkyPrisonCore plugin;
    private final DailyMissions dm;
    private final DatabaseHook db;
    public BlockPlace(SkyPrisonCore plugin, DailyMissions dm, DatabaseHook db) {
        this.plugin = plugin;
        this.dm = dm;
        this.db = db;
    }

    public int getMailboxAmount(Player player) {
        int mailBoxes = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) FROM mail_boxes WHERE owner_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mailBoxes = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mailBoxes;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if ((!event.canBuild() || event.isCancelled()) && !player.hasPermission("skyprisoncore.blockjump.bypass")) {
            player.setVelocity(new Vector(0, -0.5, 0));
        } else {
            ItemStack item = event.getItemInHand();
            Block block = event.getBlockPlaced();
            World world = block.getWorld();
            String worldName = world.getName();
            if(block.getType().equals(Material.ENDER_CHEST) && worldName.equalsIgnoreCase("world_prison")) {
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                if (regionManager != null) {
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
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (item.hasItemMeta() && !item.getPersistentDataContainer().isEmpty()) {
                PersistentDataContainer pers = item.getPersistentDataContainer();
                NamespacedKey bombKey = new NamespacedKey(plugin, "bomb-type");
                NamespacedKey mailKey = new NamespacedKey(plugin, "mailbox");
                if (pers.has(bombKey)) {
                    if (worldName.equalsIgnoreCase("world_prison")) {
                        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                        if (regionManager != null) {
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
                                String bombType = item.getPersistentDataContainer().get(bombKey, PersistentDataType.STRING);
                                Location loc = block.getLocation();
                                List<Block> blocks = new ArrayList<>();
                                Random random = new Random();
                                if (bombType != null) {
                                    int radius = switch (bombType) {
                                        case "small" -> 2;
                                        case "medium" -> 3;
                                        case "large" -> 4;
                                        case "massive" -> 5;
                                        case "nuke" -> 6;
                                        default -> 0;
                                    };
                                    if (radius != 6) {
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
                                    String progress = "▊";
                                    Component title = Component.text(name + " Bomb\n", NamedTextColor.YELLOW);
                                    Component bombTitle = title.append(Component.text(progress.repeat(radius), NamedTextColor.RED));
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
                                            if (redAmount >= 0) {
                                                greenAmount++;
                                                Component nBombTitle = title.append(Component.text(progress.repeat(greenAmount), NamedTextColor.GREEN))
                                                        .append(Component.text(progress.repeat(redAmount), NamedTextColor.RED));
                                                ent.text(nBombTitle);
                                                world.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                                            } else {
                                                world.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                                                world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
                                                ent.remove();
                                                double delay = 0.0;
                                                for (Block block1 : blocks) {
                                                    if (!block1.getType().equals(Material.PLAYER_HEAD) && !block1.getType().equals(Material.PLAYER_WALL_HEAD)) {
                                                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                                            com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(block1.getLocation());
                                                            if (query.testState(toLoc, localPlayer, Flags.BLOCK_BREAK)) {
                                                                block1.breakNaturally();
                                                            }
                                                        }, (int) Math.floor(delay));
                                                        delay += 0.004;
                                                    } else {
                                                        if (block1.equals(block)) {
                                                            block1.setType(Material.AIR);
                                                        } else if (!plugin.bombLocs.contains(block1.getLocation())) {
                                                            block1.breakNaturally();
                                                        }
                                                    }
                                                }
                                                plugin.bombLocs.remove(loc);
                                                for (String mission : dm.getMissions(player)) {
                                                    if (mission.startsWith("bomb") && !dm.isCompleted(player, mission)) {
                                                        dm.updatePlayerMission(player, mission);
                                                    }
                                                }
                                                this.cancel();
                                            }
                                        }
                                    }.runTaskTimer(plugin, 20, 20);
                                }
                            } else {
                                player.sendMessage(Component.text("You can only use this in the mines!", NamedTextColor.RED));
                                event.setCancelled(true);
                            }
                        }
                    } else {
                        player.sendMessage(Component.text("You can only use this in the prison mines!", NamedTextColor.RED));
                        event.setCancelled(true);
                    }
                } else if(pers.has(mailKey)) {
                    if(worldName.equalsIgnoreCase("world_free")) {
                        int amount = getMailboxAmount(player);
                        if(player.hasPermission("skyprisoncore.mailboxes.amount." + amount)) {
                            int mailBox = pers.get(mailKey, PersistentDataType.INTEGER);
                            Chest chest = (Chest) block.getBlockData();
                            chest.setType(Chest.Type.SINGLE);
                            block.setBlockData(chest);
                            BlockFace chestFace = chest.getFacing();
                            Location disLoc = block.getLocation().toCenterLocation().add(chestFace.getModX()*0.5, 0.5, chestFace.getModZ()*0.5);
                            if (mailBox == -2) {
                                int mailId = -2;
                                String name = player.getName() + "-" + new Random().nextInt(99999) + 10000;
                                TextDisplay boxDisplay = (TextDisplay) world.spawnEntity(disLoc, EntityType.TEXT_DISPLAY);
                                boxDisplay.text(Component.text("Mailbox " + name, NamedTextColor.YELLOW));
                                boxDisplay.setBillboard(Display.Billboard.CENTER);
                                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO mail_boxes " +
                                        "(name, owner_id, x, y, z, world, is_placed, display_text) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                                    ps.setString(1, name);
                                    ps.setString(2, player.getUniqueId().toString());
                                    ps.setInt(3, block.getX());
                                    ps.setInt(4, block.getY());
                                    ps.setInt(5, block.getZ());
                                    ps.setString(6, worldName);
                                    ps.setInt(7, 1);
                                    ps.setString(8, boxDisplay.getUniqueId().toString());
                                    ps.executeUpdate();
                                    player.sendMessage(Component.text(
                                            "Mailbox has been created!", NamedTextColor.GREEN));

                                    ResultSet rs = ps.getGeneratedKeys();
                                    if (rs.next()) {
                                        mailId = rs.getInt(1);
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                try (Connection conn = db.getConnection(); PreparedStatement ps =
                                        conn.prepareStatement("INSERT INTO mail_boxes_users (mailbox_id, user_id, preferred) VALUES (?, ?, ?)")) {
                                    ps.setInt(1, mailId);
                                    ps.setString(2, player.getUniqueId().toString());
                                    ps.setInt(3, 0);
                                    ps.executeUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                TextDisplay boxDisplay = (TextDisplay) world.spawnEntity(disLoc, EntityType.TEXT_DISPLAY);
                                boxDisplay.text(Component.text("Mailbox " + plugin.getMailBoxName(mailBox), NamedTextColor.YELLOW));
                                boxDisplay.setBillboard(Display.Billboard.CENTER);
                                try (Connection conn = db.getConnection(); PreparedStatement ps =
                                        conn.prepareStatement("UPDATE mail_boxes SET x = ?, y = ?, z = ?, world = ?, is_placed = ?, display_text = ? WHERE id = ?")) {
                                    ps.setInt(1, block.getX());
                                    ps.setInt(2, block.getY());
                                    ps.setInt(3, block.getZ());
                                    ps.setString(4, worldName);
                                    ps.setInt(5, 1);
                                    ps.setString(6, boxDisplay.getUniqueId().toString());
                                    ps.setInt(7, mailBox);
                                    ps.executeUpdate();
                                    player.sendMessage(Component.text("Mailbox location has been changed!", NamedTextColor.GREEN));
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            player.sendMessage(Component.text("You've reached the maximum number of mailboxes you can have!", NamedTextColor.RED));
                            event.setCancelled(true);
                        }
                    } else {
                        player.sendMessage(Component.text("You can only place this in Free!", NamedTextColor.RED));
                        event.setCancelled(true);
                    }
                }
            } else if(worldName.equalsIgnoreCase("world_free") && block.getType().equals(Material.CHEST)) {
                org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
                InventoryHolder holder = chest.getInventory().getHolder();
                if(holder instanceof DoubleChest doubleChest) {
                    Location bLoc = block.getLocation();
                    Location left = doubleChest.getLeftSide().getInventory().getLocation();
                    Location right = doubleChest.getRightSide().getInventory().getLocation();
                    Location loc = bLoc == left ? right : bLoc == right ? left : null;
                    if(loc != null) {
                        Block b = loc.getBlock();
                        if(plugin.getMailBox(b) != -1) {
                            Chest chestData = (Chest) block.getBlockData();
                            chestData.setType(Chest.Type.SINGLE);
                            block.setBlockData(chestData);
                        }
                    }
                }
            }
        }
    }
}
