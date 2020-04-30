package net.skyprison.Main.Commands.Donations;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DonorAdd implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 6) {
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
					.getDataFolder() + "/donations/" + Bukkit.getPlayer(args[0]).getUniqueId().toString() + ".yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(f);
			Boolean stuff = false;
			int i = 0;
			while (stuff == false) {
				// /donoradd <player> <item-bought> <item-currency> <item-price> <time> <date> <amount of it bought>
				if (!playerPurchases.contains(i + ".item-bought")) {
					playerPurchases.set(i + ".item-bought", args[1]);
					playerPurchases.set(i + ".item-price", args[2] + args[3]);
					playerPurchases.set(i + ".bought-date", args[4] + " " + args[5]);
					playerPurchases.set(i + ".item-quantity", args[6]);
					try {
						playerPurchases.save(f);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					stuff = true;
				} else {
					i++;
				}
			}
		}
		return true;
	}
}
