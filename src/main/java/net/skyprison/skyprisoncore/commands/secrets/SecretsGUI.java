package net.skyprison.skyprisoncore.commands.secrets;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SecretsGUI implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public SecretsGUI(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	private ItemStack decorativeItemStack(Material material, int amount, String name, Player player) {
		ItemStack item = new ItemStack(material, amount);
		if(material.equals(Material.PLAYER_HEAD)) {
			SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
			itemMeta.displayName(Component.text(name));
			itemMeta.setOwningPlayer(player);
			item.setItemMeta(itemMeta);
		} else {
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.displayName(Component.text(name));
			item.setItemMeta(itemMeta);
		}
		return item;
	}

	private ItemStack categoryMainItemStack(Player player, Material material, int amount, String name, String category) {
		File f = new File(plugin.getDataFolder() + File.separator + "secrets.yml");
		YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);

		if(!category.equalsIgnoreCase("all")) {
			Set<String> secrets = Objects.requireNonNull(yamlf.getConfigurationSection("inventory." + category)).getKeys(false);
			int totalSecrets = 0;

			for(String secretKey : secrets) {
				if(yamlf.isSet("inventory." + category + "." + secretKey + ".id")) {
					totalSecrets += 1;
				}
			}

			ItemStack item = new ItemStack(material, amount);
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.displayName(Component.text(name));
			int secretsFound = 0;

			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT secret_name FROM secrets_data WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					if(rs.getString(1).contains(category)) {
						secretsFound += 1;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			itemMeta.lore(Collections.singletonList(Component.text("Secrets Found: " + secretsFound + "/" + totalSecrets, NamedTextColor.GRAY)));
			item.setItemMeta(itemMeta);

			return item;
		} else {
			ItemStack item = new ItemStack(material, amount);
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.displayName(Component.text(name));
			item.setItemMeta(itemMeta);
			return item;
		}
	}

	private ItemStack unknownItemStack() {
		Component name = Component.text("???", NamedTextColor.RED, TextDecoration.BOLD);
		ItemStack item = new ItemStack(Material.BOOK, 1);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.displayName(name);
		itemMeta.lore(Collections.singletonList(Component.text("Find this secret to unlock it..", NamedTextColor.GRAY)));
		item.setItemMeta(itemMeta);
		return item;
	}

	private ItemStack rewardItemStack(List<Component> lore, String name, String reward) {
		ItemStack item = new ItemStack(Material.CHEST_MINECART, 1);
		NamespacedKey key = new NamespacedKey(plugin, "reward");
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, reward);
		itemMeta.displayName(Component.text(name));
		itemMeta.lore(lore);
		item.setItemMeta(itemMeta);
		return item;
	}


	private ItemStack secretItemStack(Material material, int amount, String name, Component cooldown, Player player, String secretId, int tokenAmount) {
		ItemStack item = new ItemStack(material, amount);
		ItemMeta itemMeta = item.getItemMeta();

		itemMeta.displayName(Component.text(name));
		String plsName = name.replaceAll("\\s", "");


		int amountFound = 0;
		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = ? AND secret_name = ?")) {
			ps.setString(1, player.getUniqueId().toString());
			ps.setString(2, secretId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				amountFound = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		boolean isParkour = plsName.toLowerCase().contains("parkour");
		boolean isPuzzle = plsName.toLowerCase().contains("puzzle");


		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("You've " + (isParkour ? "done this parkour " : isPuzzle ? "completed this puzzle " : "found this secret "), NamedTextColor.GRAY)
				.append(Component.text(amountFound, NamedTextColor.AQUA)).append(Component.text(" time(s)", NamedTextColor.GRAY)));
		lore.add(Component.text("Cooldown: ", NamedTextColor.DARK_PURPLE).append(cooldown));
		lore.add(Component.empty());
		lore.add(Component.text("Tokens: ", NamedTextColor.AQUA).append(Component.text(tokenAmount, NamedTextColor.GRAY)));


		itemMeta.lore(lore);
		item.setItemMeta(itemMeta);
		return item;
	}

	private int getTokenAmount(String SVSSignID) {
		File SVSFile = new File("plugins/ServerSigns/signs/" + SVSSignID);
		int tokenAmount = 0;
		if (SVSFile.exists()) {
			YamlConfiguration f = YamlConfiguration.loadConfiguration(SVSFile);
			Set<String> cmds = Objects.requireNonNull(f.getConfigurationSection("commands")).getKeys(false);
			for(String cmd : cmds) {
				String tokenCmd = f.getString("commands." + cmd + ".command");
				if(tokenCmd != null) {
					if (tokenCmd.startsWith("tokensadd") || tokenCmd.startsWith("/tokensadd")) {
						String[] tokenVal = tokenCmd.split(" ");
						tokenAmount = Integer.parseInt(tokenVal[2]);
					} else if (tokenCmd.startsWith("token add") || tokenCmd.startsWith("/token add")
							|| tokenCmd.startsWith("tokens add") || tokenCmd.startsWith("/tokens add")) {
						String[] tokenVal = tokenCmd.split(" ");
						tokenAmount = Integer.parseInt(tokenVal[3]);
					}
				}
			}
		}
		return tokenAmount;
	}


	private Component getCooldown(String SVSSignID, UUID pUUID) {
		File SVSFile = new File("plugins/ServerSigns/signs/" + SVSSignID);
		Component cooldown = Component.empty();
		if (SVSFile.exists()) {
			YamlConfiguration f = YamlConfiguration.loadConfiguration(SVSFile);
			if (f.getLong("lastUse." + pUUID) > 0L) {
				long useTime = f.getLong("lastUse." + pUUID);
				long cooldownLong = useTime / 1000L + f.getLong("cooldown") - System.currentTimeMillis() / 1000L;
				int cooldownInt = (int) cooldownLong;
				if (cooldownInt > 86400) {
					int days = cooldownInt / 86400;
					int hours = cooldownInt % 86400 / 3600;
					cooldown = Component.text(days + " days " + hours + " hrs", NamedTextColor.RED);
				} else {
					int hours = cooldownInt / 3600;
					int minutes = cooldownInt % 3600 / 60;
					if(minutes >= 0) {
						cooldown = Component.text( hours + " hrs " + minutes + " mins", NamedTextColor.RED);
					} else {
						cooldown = Component.text("Available Now!", NamedTextColor.GREEN);
					}
				}
			} else {
				cooldown = Component.text("Available Now!", NamedTextColor.GREEN);
			}
		}
		return cooldown;
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
		Inventory rewardInv = Bukkit.createInventory(null, slots, Component.text("Secrets - " + guiTitle, NamedTextColor.RED));
		if(yamlf.contains("inventory." + guiType)) {
			for (int i = 0; i < slots; i++) {
				if (yamlf.isSet("inventory." + guiType + "." + i)) {
					Material material = Material.getMaterial(Objects.requireNonNull(yamlf.getString("inventory." + guiType + "." + i + ".material")));
					ItemStack item;
					int amount = yamlf.getInt("inventory." + guiType + "." + i + ".amount");
					if (yamlf.getBoolean("inventory." + guiType + "." + i + ".meta")) {
						String secretId = yamlf.getString("inventory." + guiType + "." + i + ".id");

						boolean hasFound = false;
						try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = ? AND secret_name = ?")) {
							ps.setString(1, player.getUniqueId().toString());
							ps.setString(1, secretId);
							ResultSet rs = ps.executeQuery();
							while(rs.next()) {
								hasFound = true;
							}
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
							item = secretItemStack(material, amount, name, getCooldown(file, player.getUniqueId()), player, secretId, getTokenAmount(file));
						} else {
							item = unknownItemStack();
						}
						rewardInv.setItem(i, item);
					} else {
						String name = yamlf.getString("inventory." + guiType + "." + i + ".name");
						if(yamlf.isSet("inventory." + guiType + "." + i + ".category-main")) {
							String category = yamlf.getString("inventory." + guiType + "." + i + ".category-main");
							assert category != null;
							item = categoryMainItemStack(player, material, amount, name, category);
						} else {
							item = decorativeItemStack(material, amount, name, player);
						}
						rewardInv.setItem(i, item);
					}
				} else if(guiType.equalsIgnoreCase("rewards")) {
					HashMap<String, Integer> rewards = new HashMap<>();

					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT reward_name, reward_collected FROM rewards_data WHERE user_id = ?")) {
						ps.setString(1, player.getUniqueId().toString());
						ResultSet rs = ps.executeQuery();
						while(rs.next()) {
							rewards.put(rs.getString(1), rs.getInt(2));
						}
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
								List<String> stringLore = rData.getStringList(reward + ".lore");
								List<Component> lore = new ArrayList<>();
								stringLore.forEach(sLore -> lore.add(MiniMessage.miniMessage().deserialize(sLore)));
								ItemStack item = rewardItemStack(lore, name, reward);
								rewardInv.setItem(i, item);
							}
						}
					}
				}
			}
			player.openInventory(rewardInv);
		} else {
			player.sendMessage(Component.text("Invalid secret category! /secret (category)", NamedTextColor.RED));
		}
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if (args.length < 1) {
				World pWorld = player.getWorld();
				String guiType = "main-menu";
				RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
				RegionManager regions = container.get(BukkitAdapter.adapt(pWorld));
				if(regions != null) {
					ApplicableRegionSet regionList = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
					if (!regionList.getRegions().isEmpty()) {
						if (pWorld.getName().equalsIgnoreCase("world_prison")) {
							if (regionList.getRegions().contains(regions.getRegion("grass-welcome"))) {
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
				}
			} else if (args.length == 1) {
				openGUI(player, args[0]);
			} else {
				player.sendMessage(Component.text("Incorrect Usage! /secret (category)", NamedTextColor.RED));
			}
		} else {
			plugin.tellConsole(Component.text("This command is only supported in-game!", NamedTextColor.RED));
		}
		return true;
	}
}
