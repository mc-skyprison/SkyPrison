package net.skyprison.skyprisoncore.listeners.minecraft;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import net.coreprotect.CoreProtect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.RandomReward;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockBreak implements Listener {
    private final SkyPrisonCore plugin;
    private final DailyMissions dm;
    private final PlayerParticlesAPI particles;

    public BlockBreak(SkyPrisonCore plugin, DailyMissions dm, PlayerParticlesAPI particles) {
        this.plugin = plugin;
        this.dm = dm;
        this.particles = particles;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onblockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        Player player = event.getPlayer();
        Material bType = b.getType();

        if(!event.isCancelled()) {
            if(loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                if(bType.equals(Material.SNOW_BLOCK))
                    event.setDropItems(false);
                if(!player.isOp()) {
                    if(bType.equals(Material.TNT)) {
                        event.setCancelled(true);
                    }
                }
            } else if(loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                if (bType.equals(Material.SNOW_BLOCK)) {
                    event.setDropItems(false);
                    Location cob = loc.add(0.5D, 0.0D, 0.5D);
                    ItemStack snowblock = new ItemStack(Material.SNOW_BLOCK, 1);
                    loc.getWorld().dropItem(cob, snowblock);
                } else if (bType.equals(Material.PLAYER_HEAD) || bType.equals(Material.PLAYER_WALL_HEAD)) {
                    if(plugin.bombLocs.contains(b.getLocation())) {
                        event.setCancelled(true);
                    }
                }
            }

            if (!CoreProtect.getInstance().getAPI().hasPlaced(player.getName(), event.getBlock(), 300, 0) && !loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                int brokeBlocks = plugin.blockBreaks.get(player.getUniqueId());
                if (brokeBlocks >= 2000) {
                    plugin.blockBreaks.put(player.getUniqueId(), 0);
                    Random rand = new Random();
                    int tReward = rand.nextInt(25 - 10 + 1) + 10;
                    plugin.tokens.addTokens(player.getUniqueId(), tReward, "2000 Blocks Broken", "");
                    player.sendMessage(Component.text("You've mined 2,000 blocks and have received some tokens!", NamedTextColor.GRAY));
                } else {
                    plugin.blockBreaks.put(player.getUniqueId(), brokeBlocks + 1);
                }
            }
        } else {
            if(loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
                if (regionManager != null) {
                    ApplicableRegionSet regionList = regionManager.getApplicableRegions(BlockVector3.at(b.getLocation().getX(),
                            b.getLocation().getY(), b.getLocation().getZ()));
                    if (bType.equals(Material.SUGAR_CANE)) {
                        for (ProtectedRegion region : regionList.getRegions()) {
                            if (region.getId().equalsIgnoreCase("snow-island1")) {
                                if (loc.subtract(0, 1, 0).getBlock().getType().equals(Material.SUGAR_CANE))
                                    event.setCancelled(false);
                                break;
                            }
                        }
                    } else if (bType.equals(Material.TALL_GRASS) || bType.equals(Material.GRASS) || bType.equals(Material.LARGE_FERN) || bType.equals(Material.FERN)) {
                        List<Location> shinyLocs = plugin.shinyGrass;
                        if (!shinyLocs.isEmpty()) {
                            for (Location shinyLoc : shinyLocs) {
                                if (shinyLoc.equals(loc) || shinyLoc.offset(0, 1, 0).equals(loc) || shinyLoc.offset(0, -1, 0).equals(loc)) {
                                    plugin.shinyGrass.remove(loc);
                                    particles.removeFixedEffectsInRange(shinyLoc, 1);
                                    player.sendMessage(Component.text("Buried amidst the leafy foliage, you discover an unexpected treasure!", NamedTextColor.GRAY, TextDecoration.ITALIC));
                                    ItemStack item = RandomReward.getRandomReward();
                                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                                    if (user.getInventory().canFit(item)) {
                                        player.getInventory().addItem(item);
                                    } else {
                                        player.getLocation().getWorld().dropItem(player.getLocation(), item);
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (bType.equals(Material.CACTUS)) {
                        for (ProtectedRegion region : regionList.getRegions()) {
                            if (region.getId().equalsIgnoreCase("desert-nofly")) {
                                if (loc.subtract(0, 1, 0).getBlock().getType().equals(Material.CACTUS))
                                    event.setCancelled(false);
                                break;
                            }
                        }
                    } else if (bType.equals(Material.BAMBOO)) {
                        for (ProtectedRegion region : regionList.getRegions()) {
                            if (region.getId().equalsIgnoreCase("desert-nofly")) {
                                if (loc.subtract(0, 1, 0).getBlock().getType().equals(Material.BAMBOO))
                                    event.setCancelled(false);
                                break;
                            }
                        }
                    } else if (bType.equals(Material.BIRCH_LOG)) {
                        for (ProtectedRegion region : regionList.getRegions()) {
                            if (region.getId().equalsIgnoreCase("grass-nofly")) {
                                ArrayList<Material> axes = new ArrayList<>();
                                axes.add(Material.DIAMOND_AXE);
                                axes.add(Material.GOLDEN_AXE);
                                axes.add(Material.IRON_AXE);
                                axes.add(Material.STONE_AXE);
                                axes.add(Material.WOODEN_AXE);
                                axes.add(Material.NETHERITE_AXE);

                                event.setCancelled(false);
                                if (axes.contains(player.getInventory().getItemInMainHand().getType())) {
                                    if (!player.isSneaking()) {
                                        boolean birchDown = true;
                                        int birchDrops = 0;
                                        Location birchLoc;
                                        Location saplingLoc;
                                        int i = 0;
                                        while (birchDown) {
                                            birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i, loc.getBlockZ());
                                            if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                                                birchLoc.getBlock().breakNaturally();
                                                birchDrops++;
                                                i++;
                                            } else {
                                                saplingLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i + 1, loc.getBlockZ());
                                                Location finalSaplingLoc = saplingLoc;
                                                if (birchLoc.getBlock().getType() == Material.GRASS_BLOCK || birchLoc.getBlock().getType() == Material.DIRT) {
                                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> finalSaplingLoc.getBlock().setType(Material.BIRCH_SAPLING), 2L);
                                                }
                                                birchDown = false;
                                            }
                                        }
                                        boolean birchUp = true;
                                        int x = 1;
                                        while (birchUp) {
                                            birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + x, loc.getBlockZ());
                                            if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                                                birchLoc.getBlock().breakNaturally();
                                                birchDrops++;
                                                x++;
                                            } else {
                                                birchUp = false;
                                            }
                                        }

                                        ItemStack item = player.getInventory().getItemInMainHand();
                                        Damageable im = (Damageable) item.getItemMeta();
                                        Material axe = item.getType();
                                        int dmg = im.getDamage();
                                        if (item.containsEnchantment(Enchantment.DURABILITY)) {
                                            int enchantLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
                                            if (birchDrops / enchantLevel + dmg > axe.getMaxDurability()) {
                                                player.getInventory().remove(item);
                                            } else {
                                                im.setDamage(birchDrops / enchantLevel + dmg);
                                                item.setItemMeta(im);
                                            }
                                        } else {
                                            if (birchDrops + dmg > axe.getMaxDurability()) {
                                                player.getInventory().remove(item);
                                            } else {
                                                im.setDamage(birchDrops + dmg);
                                                item.setItemMeta(im);
                                            }
                                        }
                                    } else {
                                        Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                                        if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> loc.getBlock().setType(Material.BIRCH_SAPLING), 2L);
                                        }
                                    }
                                } else {
                                    Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                                    if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> loc.getBlock().setType(Material.BIRCH_SAPLING), 2L);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        for (String mission : dm.getMissions(player)) {
            if(!dm.isCompleted(player, mission)) {
                String[] missSplit = mission.split("-");
                if (missSplit[0].equalsIgnoreCase("break")) {
                    switch (missSplit[1].toLowerCase()) {
                        case "any" -> {
                            if (!(b.getBlockData() instanceof Ageable)) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                        case "birch_log" -> {
                            if (bType.equals(Material.BIRCH_LOG) || bType.equals(Material.BIRCH_WOOD)) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                    }
                } else if (missSplit[0].equalsIgnoreCase("harvest")) {
                    switch (missSplit[1].toLowerCase()) {
                        case "any" -> {
                            if (b.getBlockData() instanceof Ageable) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                        case "cactus" -> {
                            if (bType.equals(Material.CACTUS)) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                        case "sugar_cane" -> {
                            if (bType.equals(Material.SUGAR_CANE)) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                        case "pumpkin" -> {
                            if (bType.equals(Material.PUMPKIN)) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                        case "bamboo" -> {
                            if (bType.equals(Material.BAMBOO)) {
                                dm.updatePlayerMission(player, mission);
                            }
                        }
                    }
                }
            }
        }


    }
}
