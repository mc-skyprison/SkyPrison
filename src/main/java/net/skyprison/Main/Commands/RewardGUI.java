package net.skyprison.Main.Commands;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardGUI implements Listener, CommandExecutor {
	private ItemStack customItemStackDos(Material material, int amount, String name) {
		ItemStack item = new ItemStack(material, amount);
		return getItemStackDos(name, item);
	}

	private ItemStack getItemStackDos(String name, ItemStack item) {
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(name);
		item.setItemMeta(itemmeta);
		return item;
	}

	private ItemStack customItemStack(Material material, int amount, String name, String line3, Player player) {
		ItemStack item = new ItemStack(material, amount);
		return getItemStack(name, line3, item, player);
	}

	private ItemStack getItemStack(String name, String line3, ItemStack item, Player player) {
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(name);
		String line2 = ChatColor.DARK_PURPLE + "Cooldown:";
		String combinedName = name.replaceAll("\\s", "");
		String plsName = combinedName;
		String line1;
		String checkPlaceholder = "%cmi_user_metaint_" + plsName.toLowerCase() + "%";
		checkPlaceholder = PlaceholderAPI.setPlaceholders(player, checkPlaceholder);
		if (checkPlaceholder != null && !checkPlaceholder.isEmpty()) {
			if (plsName.toLowerCase().contains("parkour")) {
				if (checkPlaceholder.equals("1")) {
					line1 = ChatColor.GRAY + "You've done this parkour " + ChatColor.AQUA + "%cmi_user_metaint_" + plsName.toLowerCase() + "%" + ChatColor.GRAY + " time";
				} else {
					line1 = ChatColor.GRAY + "You've done this parkour " + ChatColor.AQUA + "%cmi_user_metaint_" + plsName.toLowerCase() + "%" + ChatColor.GRAY + " times";
				}
			} else if (plsName.toLowerCase().contains("puzzle")) {
				if (checkPlaceholder.equals("1")) {
					line1 = ChatColor.GRAY + "You've completed this puzzle " + ChatColor.AQUA + "%cmi_user_metaint_" + plsName.toLowerCase() + "%" + ChatColor.GRAY + " time";
				} else {
					line1 = ChatColor.GRAY + "You've completed this puzzle " + ChatColor.AQUA + "%cmi_user_metaint_" + plsName.toLowerCase() + "%" + ChatColor.GRAY + " times";
				}

			} else if (checkPlaceholder.equals("1")) {
				line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + "%cmi_user_metaint_" + plsName.toLowerCase() + "%" + ChatColor.GRAY + " time";
			} else {
				line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + "%cmi_user_metaint_" + plsName.toLowerCase() + "%" + ChatColor.GRAY + " times";
			}
		} else {
			line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + "0" + ChatColor.GRAY + " times";
		}
		line1 = PlaceholderAPI.setPlaceholders(player, line1);

		itemmeta.setLore(Arrays.asList(new String[] { line1, line2, line3 }));
		item.setItemMeta(itemmeta);
		return item;
	}

	private String GetCooldown(String SVSSignID, UUID puid) {
		File SVSFile = new File("plugins/ServerSigns/signs/" + SVSSignID);
		String output = ChatColor.DARK_RED + "ERROR 1/Notify Admin";
		if (!SVSFile.exists()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ServerSigns File " + ChatColor.DARK_RED + SVSSignID + ChatColor.RED + " DOES NOT EXIST! Please notify redhelmet8 of issue and file name...");
			output = ChatColor.DARK_RED + "ERROR 2/Notify Admin";
		} else {
			YamlConfiguration f = YamlConfiguration.loadConfiguration(SVSFile);
			if (f.getLong("lastUse." + puid) > 0L) {
				Long usetime = Long.valueOf(f.getLong("lastUse." + puid));
				Long cooldownlong = Long.valueOf(usetime.longValue() / 1000L + f.getLong("cooldown") - System.currentTimeMillis() / 1000L);
				int cooldown = cooldownlong.intValue();
				if (cooldown > 86400) {
					int days = cooldown / 86400;
					int hours = cooldown % 86400 / 3600;
					output = ChatColor.RED + "" + days + " days " + hours + " hrs";
				} else {
					int hours = cooldown / 3600;
					int minutes = cooldown % 3600 / 60;
					output = ChatColor.RED + "" + hours + " hrs " + minutes + " mins";
				}
			} else {
				output = ChatColor.GREEN + "Available Now!";
			}
		}
		return output;
	}

	public void openGUI(Player player, int page) {
		Inventory rewardinv;
		File f = new File("plugins/SkyPrisonCore/rewardGUI.yml");
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		if(page == 1) {
			rewardinv = Bukkit.createInventory(null, yamlf.getInt("invslots"), ChatColor.RED + "Free Secrets");
		} else {
			rewardinv = Bukkit.createInventory(null, yamlf.getInt("invslots"), ChatColor.RED + "Prison Secrets");
		}
		for (int i = 0; i < yamlf.getInt("invslots"); i++) {
			if(yamlf.isSet("inventory.page" + page + "." + i)) {
				Material material = Material.getMaterial(yamlf.getString("inventory.page" + page + "." + i + ".material"));
				int amount = yamlf.getInt("inventory.page" + page + "." + i + ".amount");
				if (yamlf.getBoolean("inventory.page" + page + "." + i + ".meta")) {
					String name = yamlf.getString("inventory.page" + page + "." + i + ".name");
					String file = yamlf.getString("inventory.page" + page + "." + i + ".file");
					ItemStack item = customItemStack(material, amount, name, GetCooldown(file, player.getUniqueId()), player);
					rewardinv.setItem(yamlf.getInt("inventory.page" + page + "." + i + ".slot"), item);
				} else {
					String name = yamlf.getString("inventory.page" + page + "." + i + ".name");
					ItemStack item = customItemStackDos(material, amount, name);
					rewardinv.setItem(yamlf.getInt("inventory.page" + page + "." + i + ".slot"), item);
				}
			}
		}
		player.openInventory(rewardinv);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player)sender;
		openGUI(player, 0);
		return true;
	}
}
