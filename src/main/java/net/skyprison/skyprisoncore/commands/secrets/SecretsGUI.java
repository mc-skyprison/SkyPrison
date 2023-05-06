package net.skyprison.skyprisoncore.commands.secrets;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SecretsGUI implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook hook;

	public SecretsGUI(SkyPrisonCore plugin, DatabaseHook hook) {
		this.plugin = plugin;
		this.hook = hook;
	}

	private ItemStack decorativeItemStack(Material material, int amount, String name, Player player) {
		ItemStack item = new ItemStack(material, amount);
		if(material.equals(Material.PLAYER_HEAD)) {
			SkullMeta itemmeta = (SkullMeta) item.getItemMeta();
			itemmeta.setDisplayName(name);
			itemmeta.setOwningPlayer(player);
			item.setItemMeta(itemmeta);
		} else {
			ItemMeta itemmeta = item.getItemMeta();
			itemmeta.setDisplayName(name);
			item.setItemMeta(itemmeta);
		}
		return item;
	}

	private ItemStack categoryMainItemStack(Player player, Material material, int amount, String name, String category) {
		File f = new File(plugin.getDataFolder() + File.separator + "secrets.yml");
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);

		if(!category.equalsIgnoreCase("all")) {
			Set<String> secrets = yamlf.getConfigurationSection("inventory." + category).getKeys(false);
			int totalSecrets = 0;

			for(String secretKey : secrets) {
				if(yamlf.isSet("inventory." + category + "." + secretKey + ".id")) {
					totalSecrets += 1;
				}
			}

			ItemStack item = new ItemStack(material, amount);
			ItemMeta itemmeta = item.getItemMeta();
			itemmeta.setDisplayName(name);
			int secretsFound = 0;

			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT secret_name FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					if(rs.getString(1).contains(category)) {
						secretsFound += 1;
					}
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			String lore1 = ChatColor.GRAY +  "Secrets Found: " + secretsFound + "/" + totalSecrets;
			itemmeta.setLore(Collections.singletonList(lore1));
			item.setItemMeta(itemmeta);

			return item;
		} else {
			ItemStack item = new ItemStack(material, amount);
			ItemMeta itemmeta = item.getItemMeta();
			itemmeta.setDisplayName(name);
			item.setItemMeta(itemmeta);
			return item;
		}
	}

	private ItemStack unknownItemStack() {
		String name = ChatColor.RED + "" + ChatColor.BOLD + "???";
		ItemStack item = new ItemStack(Material.BOOK, 1);
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.setDisplayName(name);
		itemmeta.setLore(Collections.singletonList(ChatColor.GRAY + "" + ChatColor.ITALIC + "Find this secret to unlock it!"));
		item.setItemMeta(itemmeta);
		return item;
	}

	private ItemStack rewardItemStack(List<String> lore, String name, String reward) {
		ItemStack item = new ItemStack(Material.CHEST_MINECART, 1);
		NamespacedKey key = new NamespacedKey(plugin, "reward");
		ItemMeta itemmeta = item.getItemMeta();
		itemmeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, reward);
		itemmeta.setDisplayName(name);
		itemmeta.setLore(lore);
		item.setItemMeta(itemmeta);
		return item;
	}


	private ItemStack secretItemStack(Material material, int amount, String name, String line3, Player player, String secretId, String category, String tokenAmount) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta itemmeta = item.getItemMeta();

		itemmeta.setDisplayName(name);
		String line2 = ChatColor.DARK_PURPLE + "Cooldown:";
		String plsName = name.replaceAll("\\s", "");
		String line1;


		int amountFound = 0;
		try {
			Connection conn = hook.getSQLConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "' AND secret_name = '" + secretId + "'");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				amountFound = rs.getInt(1);
			}
			hook.close(ps, rs, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}


		if (amountFound > 0) {
			if (plsName.toLowerCase().contains("parkour")) {
				if (amountFound == 1) {
					line1 = ChatColor.GRAY + "You've done this parkour " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " time";
				} else {
					line1 = ChatColor.GRAY + "You've done this parkour " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " times";
				}
			} else if (plsName.toLowerCase().contains("puzzle")) {
				if (amountFound == 1) {
					line1 = ChatColor.GRAY + "You've completed this puzzle " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " time";
				} else {
					line1 = ChatColor.GRAY + "You've completed this puzzle " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " times";
				}

			} else if (amountFound == 1) {
				line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " time";
			} else {
				line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + amountFound + ChatColor.GRAY + " times";
			}
		} else {
			line1 = ChatColor.GRAY + "You've found this secret " + ChatColor.AQUA + "0" + ChatColor.GRAY + " times";
		}
		line1 = PlaceholderAPI.setPlaceholders(player, line1);
		itemmeta.setLore(Arrays.asList(line1, line2, line3, "", plugin.colourMessage("&bTokens: &7" + tokenAmount)));
		item.setItemMeta(itemmeta);
		return item;
	}

	private String getTokenAmount(String SVSSignID) {
		File SVSFile = new File("plugins/ServerSigns/signs/" + SVSSignID);
		String output = "";
		if (!SVSFile.exists()) {
			output = ChatColor.DARK_RED + "ERROR! Notify Admin!";
		} else {
			YamlConfiguration f = YamlConfiguration.loadConfiguration(SVSFile);
			Set<String> cmds = f.getConfigurationSection("commands").getKeys(false);
			for(String cmd : cmds) {
				String tokenCmd = f.getString("commands." + cmd + ".command");
				if(tokenCmd.startsWith("tokensadd") || tokenCmd.startsWith("/tokensadd")) {
					String[] tokenVal = tokenCmd.split(" ");
					output = tokenVal[2];
				} else if(tokenCmd.startsWith("token add") || tokenCmd.startsWith("/token add")
						|| tokenCmd.startsWith("tokens add") || tokenCmd.startsWith("/tokens add")) {
					String[] tokenVal = tokenCmd.split(" ");
					output = tokenVal[3];
				}
			}
		}
		return output;
	}


	private String getCooldown(String SVSSignID, UUID pUUID) {
		File SVSFile = new File("plugins/ServerSigns/signs/" + SVSSignID);
		String output;
		if (!SVSFile.exists()) {
			output = ChatColor.DARK_RED + "ERROR! Notify Admin!";
		} else {
			YamlConfiguration f = YamlConfiguration.loadConfiguration(SVSFile);
			if (f.getLong("lastUse." + pUUID) > 0L) {
				Long useTime = f.getLong("lastUse." + pUUID);
				Long cooldownLong = useTime / 1000L + f.getLong("cooldown") - System.currentTimeMillis() / 1000L;
				int cooldown = cooldownLong.intValue();
				if (cooldown > 86400) {
					int days = cooldown / 86400;
					int hours = cooldown % 86400 / 3600;
					output = ChatColor.RED + "" + days + " days " + hours + " hrs";
				} else {
					int hours = cooldown / 3600;
					int minutes = cooldown % 3600 / 60;
					if(minutes >= 0) {
						output = ChatColor.RED + "" + hours + " hrs " + minutes + " mins";
					} else {
						output = ChatColor.GREEN + "Available Now!";
					}
				}
			} else {
				output = ChatColor.GREEN + "Available Now!";
			}
		}
		return output;
	}

	public void openGUI(Player player, String guiType) {
		File f = new File(plugin.getDataFolder() + File.separator + "secrets.yml");
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		int slots = yamlf.getInt("inv-slots");
		String guiTitle;
		if(guiType.equalsIgnoreCase("main-menu")) {
			guiTitle = "Main";
		} else if(guiType.equalsIgnoreCase("rewards")) {
			guiTitle = "Rewards";
		} else if(guiType.equalsIgnoreCase("secrets")) {
			guiTitle = "All";
		} else {
			guiTitle = guiType.substring(0, 1).toUpperCase() + guiType.substring(1);
		}

		if(guiType.equalsIgnoreCase("main"))
			guiType = "main-menu";

		if(guiType.equalsIgnoreCase("main-menu")) {
			slots = yamlf.getInt("main-slots");
		} else if(guiType.equalsIgnoreCase("rewards")) {
			slots = yamlf.getInt("reward-slots");
		} else if(guiType.equalsIgnoreCase("secrets")) {
			slots = yamlf.getInt("secrets-slots");
		}
		Inventory rewardInv = Bukkit.createInventory(null, slots, ChatColor.RED + "Secrets - " + guiTitle);
		if(yamlf.contains("inventory." + guiType)) {
			for (int i = 0; i < slots; i++) {
				if (yamlf.isSet("inventory." + guiType + "." + i)) {
					Material material = Material.getMaterial(Objects.requireNonNull(yamlf.getString("inventory." + guiType + "." + i + ".material")));
					ItemStack item;
					int amount = yamlf.getInt("inventory." + guiType + "." + i + ".amount");
					if (yamlf.getBoolean("inventory." + guiType + "." + i + ".meta")) {
						String secretId = yamlf.getString("inventory." + guiType + "." + i + ".id");

						boolean hasFound = false;
						try {
							Connection conn = hook.getSQLConnection();
							PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "' AND secret_name = '" + secretId + "'");
							ResultSet rs = ps.executeQuery();
							while(rs.next()) {
								hasFound = true;
							}
							hook.close(ps, rs, conn);
						} catch (SQLException e) {
							e.printStackTrace();
						}

						if (hasFound) {
							String name = yamlf.getString("inventory." + guiType + "." + i + ".name");
							int secretX = yamlf.getInt("inventory." + guiType + "." + i + ".X");
							int secretY = yamlf.getInt("inventory." + guiType + "." + i + ".Y");
							int secretZ = yamlf.getInt("inventory." + guiType + "." + i + ".Z");
							String secretWorld = yamlf.getString("inventory." + guiType + "." + i + ".world");
							String file = secretWorld + "_" + secretX + "_" + secretY + "_" + secretZ + ".yml";
							item = secretItemStack(material, amount, name, getCooldown(file, player.getUniqueId()), player, secretId, guiType, getTokenAmount(file));
						} else {
							item = unknownItemStack();
						}
						rewardInv.setItem(i, item);
					} else {
						String name = yamlf.getString("inventory." + guiType + "." + i + ".name");
						if(yamlf.isSet("inventory." + guiType + "." + i + ".category-main")) {
							String category = yamlf.getString("inventory." + guiType + "." + i + ".category-main");
							item = categoryMainItemStack(player, material, amount, name, category);
						} else {
							item = decorativeItemStack(material, amount, name, player);
						}
						rewardInv.setItem(i, item);
					}
				} else if(guiType.equalsIgnoreCase("rewards")) {
					HashMap<String, Integer> rewards = new HashMap<>();

					try {
						Connection conn = hook.getSQLConnection();
						PreparedStatement ps = conn.prepareStatement("SELECT reward_name, reward_collected FROM rewards_data WHERE user_id = '" + player.getUniqueId() + "'");
						ResultSet rs = ps.executeQuery();
						while(rs.next()) {
							rewards.put(rs.getString(1), rs.getInt(2));
						}
						hook.close(ps, rs, conn);
					} catch (SQLException e) {
						e.printStackTrace();
					}


					if (!rewards.isEmpty()) {
						for (String reward : rewards.keySet()) {
							if (rewards.get(reward) == 0) {
								File rewardsDataFile = new File(plugin.getDataFolder() + File.separator
										+ "rewardsdata.yml");
								YamlConfiguration rData = YamlConfiguration.loadConfiguration(rewardsDataFile);
								String name = rData.getString(reward + ".name");
								List<String> lore = rData.getStringList(reward + ".lore");
								ItemStack item = rewardItemStack(lore, name, reward);
								rewardInv.setItem(i, item);
							}
						}
					}
				}
			}
			player.openInventory(rewardInv);
		} else {
			player.sendMessage(ChatColor.RED + "Invalid secret category! /secret (category)");
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 1) {
				World pWorld = player.getWorld();
				String guiType = "main-menu";
				RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
				RegionManager regions = container.get(BukkitAdapter.adapt(pWorld));
				ApplicableRegionSet regionList = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
				if (!regionList.getRegions().isEmpty()) {
					if (pWorld.getName().equalsIgnoreCase("world_prison")) {
						if(regionList.getRegions().contains(regions.getRegion("grass-welcome"))) {
							guiType = "grass";
						} else if (regionList.getRegions().contains(regions.getRegion("desert-welcome"))) {
							guiType = "desert";
						} else if (regionList.getRegions().contains(regions.getRegion("nether-welcome"))) {
							guiType = "nether";
						} else if (regionList.getRegions().contains(regions.getRegion("snow-welcome"))) {
							guiType = "snow";
						} else {
							guiType = "prison-other";
						}
					} else if (pWorld.getName().equalsIgnoreCase("world_skycity")) {
						guiType = "skycity";
					}
				}
				openGUI(player, guiType);
			} else if (args.length == 1) {
				openGUI(player, args[0]);
			} else {
				player.sendMessage(ChatColor.RED + "Invalid Usage! /secret (category)");
			}
		} else {
			plugin.tellConsole("This command is only supported in-game!");
		}
		return true;
	}
}
