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
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(f);
			Boolean stuff = false;
			StringBuilder itemBought = new StringBuilder();
			for(int i = 6; i < args.length; i++){
				if(i != 6) {
					itemBought.append(" "+args[i]);
				} else {
					itemBought.append(args[i]);
				}
			}
			int i = 0;
			while (stuff == false) {
				// /donoradd <player> <item-currency> <item-price> <date> <time> <amount of it bought> <item-bought>
				if (!playerPurchases.contains(i + ".item-bought")) {
					playerPurchases.set(i + ".item-price", Double.parseDouble(args[2]));
					playerPurchases.set(i + ".item-currency", args[1]);
					playerPurchases.set(i + ".bought-date", args[3] + " " + args[4]);
					playerPurchases.set(i + ".item-quantity", Integer.parseInt(args[5]));
					playerPurchases.set(i + ".item-bought", String.valueOf(itemBought));
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
