package net.skyprison.Main.Commands.RanksPkg;

import net.skyprison.Main.SkyPrisonMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Contraband implements CommandExecutor {
	public Contraband() {
	}

	public boolean InvCheckCont(Player target) {
		for (int n = 0; n < target.getInventory().getSize(); n++) {
			ItemStack i = target.getInventory().getItem(n);
			if (i != null) {
				for (Material cb : SkyPrisonMain.getInstance().contraband) {
					if (i.getType() == cb) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void cbPunish(Player target, int tmremaining) {
		if (SkyPrisonMain.getInstance().cbed.contains(target)) {
			if (tmremaining > 0) {
				Inventory cbSelector = Bukkit.getServer().createInventory(null, 36, ChatColor.DARK_RED + "You've been caught with contraband!");
				for (int i = 0; i < 36; i++) {
					if (i >= 18 && i <= 26) {
						cbSelector.setItem(i, new ItemStack(Material.REDSTONE_BLOCK));
					} else if (i == 11) {
						ItemStack sword = new ItemStack(Material.GOLDEN_SWORD, 1);
						ItemMeta meta = sword.getItemMeta();
						ArrayList<String> lore = new ArrayList<String>();
						lore.add("Select me to:");
						lore.add("-turn in your contraband");
						lore.add("-not get jailed");
						meta.setLore(lore);
						sword.setItemMeta(meta);
						cbSelector.setItem(i, sword);
					} else if (i == 15) {
						ItemStack ironbars = new ItemStack(Material.IRON_BARS, 1);
						ItemMeta meta = ironbars.getItemMeta();
						ArrayList<String> lore = new ArrayList<String>();
						lore.add("Select me to:");
						lore.add("-keep your contraband");
						lore.add("-get jailed for 5min");
						meta.setLore(lore);
						ironbars.setItemMeta(meta);
						cbSelector.setItem(i, ironbars);
					} else if (i >= 9 && i <= 17) {
						cbSelector.setItem(i, new ItemStack(Material.AIR));
					} else {
						cbSelector.setItem(i, new ItemStack(Material.SANDSTONE, 1));
					}
				}
				if (!target.getOpenInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_RED + "You've been caught with contraband!")) {
					target.openInventory(cbSelector);
				}
				for (int i = 0; i <= 10 - tmremaining; i++) {
					int slot = 18 + i;
					target.getOpenInventory().setItem(slot, new ItemStack(Material.GLASS, 1));
				}
				SkyPrisonMain.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(SkyPrisonMain.getInstance(), new Runnable() {
					public void run() {
						int tm = tmremaining - 1;
						cbPunish(target, tm);
					}
				},20L);
			} else {
				target.closeInventory();
				Bukkit.getServer().dispatchCommand(SkyPrisonMain.getInstance().getServer().getConsoleSender(), "jail " + target.getName());
				target.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You did not respond. You have automatically been sent to jail for having contraband. All contraband items will remain in your inventory!");
				SkyPrisonMain.getInstance().cbed.remove(target);
				SkyPrisonMain.getInstance().cbedMap.get(target).sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + target.getName() + ChatColor.LIGHT_PURPLE + " has gone to jail!");
				SkyPrisonMain.getInstance().cbedMap.remove(target);
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player guard = (Player)sender;
			if (args.length < 1) {
				guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.YELLOW + " Use /cb <target> to initiate contraband countdown...");
			} else {
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target != null) {
					if (!SkyPrisonMain.getInstance().cbed.contains(target)) {
						double radius = 20.0D;
						if (target.getLocation().distance(guard.getLocation()) <= radius) {
							if (InvCheckCont(target)) {
								SkyPrisonMain.getInstance().cbed.add(target);
								SkyPrisonMain.getInstance().cbedMap.put(target, guard);
								guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You have caught "+ChatColor.GOLD + target.getName()+ChatColor.RED+" with contraband. Please await their decision...");
								target.sendMessage("\n\n\n[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + guard.getName() + ChatColor.RED + " has caught you with contraband.");
								cbPunish(target, 10);
							} else {
								guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Player does not have contraband!");
							}
						} else {
							guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You are not close enough to the player to execute this command!");
						}
					} else {
						guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Player has already been '/cb'ed!");
					}
				} else {
					guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Player is not online or cannot be /cb'ed...");
				}
			}
		}
		return true;
	}
}
