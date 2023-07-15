package net.skyprison.skyprisoncore.listeners.minecraft;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import net.alex9849.arm.AdvancedRegionMarket;
import net.coreprotect.CoreProtect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.RandomReward;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

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
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.hasItemMeta() && item.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller"))) {
            int durabilityLeft = item.getType().getMaxDurability() - item.getDamage();
            if(durabilityLeft <= 1) {
                if(!player.hasMetadata("treefeller-broken-msg")) {
                    player.setMetadata("treefeller-broken-msg", new FixedMetadataValue(plugin, true));
                    player.sendMessage(Component.text("Your tool has broken! Get it repaired at a blacksmith.", NamedTextColor.GRAY));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.removeMetadata("treefeller-broken-msg", plugin);
                        }
                    }.runTaskLater(plugin, 60);
                }
                event.setCancelled(true);
                return;
            }
        }


        if(player.getGameMode().equals(GameMode.SURVIVAL)) {
            Block b = event.getBlock();
            Location loc = b.getLocation();
            Material bType = b.getType();

            if (!event.isCancelled()) {
                if (loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                    if (bType.equals(Material.SNOW_BLOCK))
                        event.setDropItems(false);
                    if (!player.getGameMode().equals(GameMode.CREATIVE) && bType.equals(Material.TNT)) {
                        event.setCancelled(true);
                    }
                } else if (loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                    if (bType.equals(Material.SNOW_BLOCK)) {
                        event.setDropItems(false);
                        Location cob = loc.add(0.5D, 0.0D, 0.5D);
                        ItemStack snowblock = new ItemStack(Material.SNOW_BLOCK, 1);
                        loc.getWorld().dropItem(cob, snowblock);
                    } else if (bType.equals(Material.PLAYER_HEAD) || bType.equals(Material.PLAYER_WALL_HEAD)) {
                        if (plugin.bombLocs.contains(b.getLocation())) {
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
                if (loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                    boolean notCell = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(b.getLocation()).isEmpty();
                    if (bType.equals(Material.SUGAR_CANE) && notCell) {
                        if (b.getRelative(BlockFace.DOWN, 1).getType().equals(Material.SUGAR_CANE))
                            event.setCancelled(false);
                    } else if (bType.equals(Material.TALL_GRASS) || bType.equals(Material.GRASS) || bType.equals(Material.LARGE_FERN) || bType.equals(Material.FERN)) {
                        List<Location> shinyLocs = plugin.shinyGrass;
                        if (!shinyLocs.isEmpty()) {
                            for (Location shinyLoc : shinyLocs) {
                                if (shinyLoc.equals(loc) || shinyLoc.offset(0, 1, 0).equals(loc) || shinyLoc.offset(0, -1, 0).equals(loc)) {
                                    plugin.shinyGrass.remove(loc);
                                    particles.removeFixedEffectsInRange(shinyLoc, 1);
                                    player.sendMessage(Component.text("Buried amidst the leafy foliage, you discover an unexpected treasure!", NamedTextColor.GRAY, TextDecoration.ITALIC));
                                    item = RandomReward.getRandomReward(plugin);
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
                        if (b.getRelative(BlockFace.DOWN, 1).getType().equals(Material.CACTUS))
                            event.setCancelled(false);
                    } else if (bType.equals(Material.BAMBOO)) {
                        if (b.getRelative(BlockFace.DOWN, 1).getType().equals(Material.BAMBOO))
                            event.setCancelled(false);
                    } else if (bType.equals(Material.BIRCH_LOG)) {
                        if (item.hasItemMeta() && item.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller")) && !player.hasMetadata("treefeller-stop-looping") && !player.isSneaking() && notCell) {
                            event.setCancelled(false);
                            player.setMetadata("treefeller-stop-looping", new FixedMetadataValue(plugin, true));
                            List<Block> blocks = new ArrayList<>();
                            boolean birch = true;
                            boolean birchDown = true;
                            int i = 0;
                            while (birch) {
                                if (birchDown) {
                                    Block nBlock = b.getRelative(BlockFace.UP, i++);
                                    if (nBlock.getType().equals(Material.BIRCH_LOG)) {
                                        blocks.add(nBlock);
                                    } else {
                                        birchDown = false;
                                        i = 0;
                                    }
                                } else {
                                    Block nBlock = b.getRelative(BlockFace.DOWN, i++);
                                    if (nBlock.getType().equals(Material.BIRCH_LOG)) {
                                        blocks.add(nBlock);
                                    } else if(nBlock.isSolid()) {
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                nBlock.getRelative(BlockFace.UP, 1).setType(Material.BIRCH_SAPLING);
                                            }
                                        }.runTaskLater(plugin, 5);
                                        birch = false;
                                    } else {
                                        birch = false;
                                    }
                                }
                            }
                            blocks.forEach(player::breakBlock);
                            int cooldown = item.getPersistentDataContainer().get(new NamespacedKey(plugin, "treefeller-cooldown"), PersistentDataType.INTEGER);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.removeMetadata("treefeller-stop-looping", plugin);
                                }
                            }.runTaskLater(plugin, 20L * cooldown);
                        } else if(notCell) {
                            event.setCancelled(false);
                            if(!b.getRelative(BlockFace.DOWN, 1).getType().equals(Material.BIRCH_LOG) && b.getRelative(BlockFace.DOWN, 1).isSolid()) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        b.setType(Material.BIRCH_SAPLING);
                                    }
                                }.runTaskLater(plugin, 5);
                            }
                        }
                    }
                }
            }

            for (String mission : dm.getMissions(player)) {
                if (!dm.isCompleted(player, mission)) {
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
}
