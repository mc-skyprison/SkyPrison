package net.skyprison.skyprisoncore.listeners.minecraft;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import net.alex9849.arm.AdvancedRegionMarket;
import net.coreprotect.CoreProtect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
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
    private final PlayerParticlesAPI particles;

    public BlockBreak(SkyPrisonCore plugin, PlayerParticlesAPI particles) {
        this.plugin = plugin;
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

        Block block = event.getBlock();
        if(block.getWorld().getName().equalsIgnoreCase("world_free") && block.getType().equals(Material.CHEST)) {
            int mailBox = MailUtils.getMailBox(block);
            if(mailBox != -1) {
                event.setCancelled(true);
                return;
            }
        }
        if(player.getGameMode().equals(GameMode.SURVIVAL)) {
            Location loc = block.getLocation();
            Material bType = block.getType();

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
                        if (plugin.bombLocs.contains(loc)) {
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
                        TokenUtils.addTokens(player.getUniqueId(), tReward, "2000 Blocks Broken", "");
                        player.sendMessage(Component.text("You've mined 2,000 blocks and have received some tokens!", NamedTextColor.GRAY));
                    } else {
                        plugin.blockBreaks.put(player.getUniqueId(), brokeBlocks + 1);
                    }
                }
            } else {
                if (loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                    boolean notCell = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByLocation(loc).isEmpty();
                    if (bType.equals(Material.SUGAR_CANE) && notCell) {
                        if (block.getRelative(BlockFace.DOWN, 1).getType().equals(Material.SUGAR_CANE))
                            event.setCancelled(false);
                    } else if (bType.equals(Material.TALL_GRASS) || bType.equals(Material.SHORT_GRASS) || bType.equals(Material.LARGE_FERN) || bType.equals(Material.FERN)) {
                        List<Location> shinyLocs = plugin.shinyGrass;
                        if (!shinyLocs.isEmpty()) {
                            for (Location shinyLoc : shinyLocs) {
                                if (shinyLoc.equals(loc) || shinyLoc.offset(0, 1, 0).equals(loc) || shinyLoc.offset(0, -1, 0).equals(loc)) {
                                    plugin.shinyGrass.remove(loc);
                                    particles.removeFixedEffectsInRange(shinyLoc, 1);
                                    player.sendMessage(Component.text("Buried amidst the leafy foliage, you discover an unexpected treasure!", NamedTextColor.GRAY, TextDecoration.ITALIC));
                                    item = RandomReward.getRandomReward(plugin);
                                    PlayerManager.giveItems(player, item);
                                    break;
                                }
                            }
                        }
                    } else if (bType.equals(Material.CACTUS)) {
                        if (block.getRelative(BlockFace.DOWN, 1).getType().equals(Material.CACTUS))
                            event.setCancelled(false);
                    } else if (bType.equals(Material.BAMBOO)) {
                        if (block.getRelative(BlockFace.DOWN, 1).getType().equals(Material.BAMBOO))
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
                                    Block nBlock = block.getRelative(BlockFace.UP, i++);
                                    if (nBlock.getType().equals(Material.BIRCH_LOG)) {
                                        blocks.add(nBlock);
                                    } else {
                                        birchDown = false;
                                        i = 0;
                                    }
                                } else {
                                    Block nBlock = block.getRelative(BlockFace.DOWN, i++);
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
                            if(!block.getRelative(BlockFace.DOWN, 1).getType().equals(Material.BIRCH_LOG) && block.getRelative(BlockFace.DOWN, 1).isSolid()) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        block.setType(Material.BIRCH_SAPLING);
                                    }
                                }.runTaskLater(plugin, 5);
                            }
                        }
                    }
                }
            }
            DailyMissions.updatePlayerMissions(player.getUniqueId(), block.getBlockData() instanceof Ageable ? "harvest" : "break", bType);
        }
    }
}
