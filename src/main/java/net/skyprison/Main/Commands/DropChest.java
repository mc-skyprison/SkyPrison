package net.skyprison.Main.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class DropChest implements CommandExecutor {
	Location randomLocation(Location min, Location max) {
		Location range = new Location(min.getWorld(), Math.abs(max.getX() - min.getX()), min.getY(), Math.abs(max.getZ() - min.getZ()));
		return new Location(min.getWorld(), (Math.random() * range.getX()) + (min.getX() <= max.getX() ? min.getX() : max.getX()), range.getY(), (Math.random() * range.getZ()) + (min.getZ() <= max.getZ() ? min.getZ() : max.getZ()));
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			File f = new File("plugins/SkyPrisonCore/dropChest.yml");
			YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
			Inventory dropChest = Bukkit.createInventory(null, 54, ChatColor.RED + "Rewards!");
			Player player = (Player) sender;
			Set setList = yamlf.getConfigurationSection("items").getKeys(false);
			if (args.length < 1) {
				for (int i = 0; i < setList.size(); i++) {
					dropChest.setItem(i, yamlf.getItemStack("items." + i + ".item"));
				}
				player.openInventory(dropChest);
			} else if (args[0].equalsIgnoreCase("drop")) {
				Location loc = player.getLocation();
				World world = Bukkit.getWorld("prison");
				Location min = new Location(world, -10, 134, 11);
				Location max = new Location(world, 18, 140, -11);
				for(int b = 0; b < setList.size(); b++) {
					Location randLoc = randomLocation(min, max);
					loc.getWorld().dropItem(randLoc, yamlf.getItemStack("items." + b + ".item"));
					yamlf.getConfigurationSection("items.").set(String.valueOf(b), null);
					try {
						yamlf.save(f);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return true;
	}
}
