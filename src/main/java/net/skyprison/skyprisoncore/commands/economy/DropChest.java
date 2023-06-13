package net.skyprison.skyprisoncore.commands.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class DropChest implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public DropChest(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}


	public void openGUI(Player player, int page) {
		File f = new File(plugin.getDataFolder() + File.separator + "dropchest.yml");
		FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		Set<String> voidItems = Objects.requireNonNull(yamlf.getConfigurationSection("items")).getKeys(false);
		ArrayList<String> arr = new ArrayList<>();
		ArrayList<Integer> totalPages = new ArrayList<>();
		for(String dropItem : voidItems) {
			if(yamlf.getInt("items." + dropItem + ".page") == page) {
				arr.add(dropItem);
			}
			totalPages.add(yamlf.getInt("items." + dropItem + ".page"));
		}
		Inventory dropChest = Bukkit.createInventory(null, 54, Component.text("Drop Party | Page " + page, NamedTextColor.RED));
		int i = 0;
		for (String dropItem : arr) {
			ItemStack droppedItem = yamlf.getItemStack("items." + dropItem + ".item");
			dropChest.setItem(i, droppedItem);
			i++;
		}
		ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack pageChange = new ItemStack(Material.PAPER);
		ItemMeta itemMeta = pageChange.getItemMeta();
		itemMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN));
		pageChange.setItemMeta(itemMeta);
		for (int b = 45; b < 54; b++) {
			if (page == 0) {
				if(totalPages.size() < 1) {
					dropChest.setItem(b, pane);
				} else {
					if (Collections.max(totalPages).equals(page)) {
						dropChest.setItem(b, pane);
					} else {
						if(b != 52) {
							dropChest.setItem(b, pane);
						} else {
							dropChest.setItem(b, pageChange);
						}
					}
				}
			} else if (Collections.max(totalPages).equals(page)) {
				if(b != 46) {
					dropChest.setItem(b, pane);
				} else {
					itemMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN));
					pageChange.setItemMeta(itemMeta);
					dropChest.setItem(b, pageChange);
				}
			} else {
				if(b != 46 && b != 52) {
					dropChest.setItem(b, pane);
				} else if(b == 46) {
					itemMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN));
					pageChange.setItemMeta(itemMeta);
					dropChest.setItem(b, pageChange);
				} else {
					dropChest.setItem(b, pageChange);
				}
			}
		}
		player.openInventory(dropChest);
	}

	Location randomLocation(Location min, Location max) {
		Location range = new Location(min.getWorld(), Math.abs(max.getX() - min.getX()), min.getY(), Math.abs(max.getZ() - min.getZ()));
		return new Location(min.getWorld(), (Math.random() * range.getX()) + (Math.min(min.getX(), max.getX())), range.getY(), (Math.random() * range.getZ()) + (Math.min(min.getZ(), max.getZ())));
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {
			if (args.length < 1) {
				openGUI(player, 0);
			} else if (args[0].equalsIgnoreCase("drop")) {
				File f = new File(plugin.getDataFolder() + File.separator + "dropchest.yml");
				YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
				Set<String> setList = Objects.requireNonNull(yamlf.getConfigurationSection("items")).getKeys(false);
				Location loc = player.getLocation();
				World world = Bukkit.getWorld("world_prison");
				Location min = new Location(world, -10, 134, 11);
				Location max = new Location(world, 18, 140, -11);
				for (int b = 0; b < setList.size(); b++) {
					int finalB = b;
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
						Location randLoc = randomLocation(min, max);
						loc.getWorld().dropItem(randLoc, Objects.requireNonNull(yamlf.getItemStack("items." + finalB + ".item")));
						Objects.requireNonNull(yamlf.getConfigurationSection("items.")).set(String.valueOf(finalB), null);
						try {
							yamlf.save(f);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}, 20L);
				}
					Bukkit.broadcast(Component.text("[", NamedTextColor.WHITE).append(Component.text("Drop Party", NamedTextColor.LIGHT_PURPLE))
							.append(Component.text("] ", NamedTextColor.WHITE)).append(Component.text("Drop party is now over!", NamedTextColor.YELLOW)));
			}
		}
		return true;
	}
}
