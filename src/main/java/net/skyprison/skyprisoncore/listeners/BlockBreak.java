package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import net.coreprotect.CoreProtect;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Random;

public class BlockBreak implements Listener {
        private SkyPrisonCore plugin;

        public BlockBreak(SkyPrisonCore plugin) {
                this.plugin = plugin;
        }


        @EventHandler
        public void onblockBreak(BlockBreakEvent event) {
                Block b = event.getBlock();
                Location loc = b.getLocation();
                if(!event.isCancelled()) {
                        if(loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                                if(!event.getPlayer().isOp()) {
                                        if(b.getType().equals(Material.TNT)) {
                                                event.setCancelled(true);
                                        }
                                }
                        } else {
                                if (!CoreProtect.getInstance().getAPI().hasPlaced(event.getPlayer().getName(), event.getBlock(), 300, 0)) {
                                        String pUUID = event.getPlayer().getUniqueId().toString();
                                        int brokeBlocks = plugin.blockBreaks.get(pUUID);
                                        if (brokeBlocks >= 2000) {
                                                plugin.blockBreaks.put(pUUID, 0);
                                                Random rand = new Random();
                                                int tReward = rand.nextInt(25 - 10 + 1) + 10;
                                                plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(event.getPlayer()), tReward);
                                                event.getPlayer().sendMessage(ChatColor.GRAY + "You've mined 2,000 blocks and have received some tokens!");
                                        } else {
                                                plugin.blockBreaks.put(pUUID, brokeBlocks + 1);
                                        }
                                }
                                if (b.getType() == Material.SNOW_BLOCK && loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                                        event.setDropItems(false);
                                        Location cob = loc.add(0.5D, 0.0D, 0.5D);
                                        ItemStack snowblock = new ItemStack(Material.SNOW_BLOCK, 1);
                                        loc.getWorld().dropItem(cob, snowblock);
                                } else if (b.getType() == Material.SNOW_BLOCK && loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                                        event.setDropItems(false);
                                } else if (b.getType() == Material.BIRCH_LOG && loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                                        ArrayList<Material> axes = new ArrayList<>();
                                        axes.add(Material.DIAMOND_AXE);
                                        axes.add(Material.GOLDEN_AXE);
                                        axes.add(Material.IRON_AXE);
                                        axes.add(Material.STONE_AXE);
                                        axes.add(Material.WOODEN_AXE);
                                        axes.add(Material.NETHERITE_AXE);
                                        if (axes.contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                                                if (!event.getPlayer().isSneaking()) {
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

                                                        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                                                        Damageable im = (Damageable) item.getItemMeta();
                                                        Material axe = item.getType();
                                                        int dmg = im.getDamage();
                                                        if (item.containsEnchantment(Enchantment.DURABILITY)) {
                                                                int enchantLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
                                                                if (birchDrops / enchantLevel + dmg > axe.getMaxDurability()) {
                                                                        event.getPlayer().getInventory().remove(item);
                                                                } else {
                                                                        im.setDamage(birchDrops / enchantLevel + dmg);
                                                                        item.setItemMeta((ItemMeta) im);
                                                                }
                                                        } else {
                                                                if (birchDrops + dmg > axe.getMaxDurability()) {
                                                                        event.getPlayer().getInventory().remove(item);
                                                                } else {
                                                                        im.setDamage(birchDrops + dmg);
                                                                        item.setItemMeta((ItemMeta) im);
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
                                } else if (b.getType() == Material.WHEAT && loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                                        if (!event.getPlayer().isOp()) {
                                                BlockData bdata = b.getBlockData();
                                                if (bdata instanceof Ageable) {
                                                        Ageable age = (Ageable) bdata;
                                                        if (age.getAge() != age.getMaximumAge()) {
                                                                event.setCancelled(true);
                                                                event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "This wheat isn't ready for harvest..");
                                                        } else {
                                                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> loc.getBlock().setType(Material.WHEAT), 2L);
                                                        }
                                                }
                                        }
                                } else if (b.getType() == Material.BIRCH_SAPLING && loc.getWorld().getName().equalsIgnoreCase("world_prison") && !event.getPlayer().isOp()) {
                                        event.setCancelled(true);
                                }
                        }
                }
        }
}
