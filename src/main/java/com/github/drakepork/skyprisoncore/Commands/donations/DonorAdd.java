package com.github.drakepork.skyprisoncore.Commands.donations;

import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class DonorAdd implements CommandExecutor {
	private Core plugin;

	@Inject
	public DonorAdd(Core plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 6) {
			if(Bukkit.getPlayer(args[0]).isValid()) {
				Player player = Bukkit.getPlayer(args[0]);
				File f = new File(plugin.getDataFolder() + File.separator
						+ "donations" + File.separator + player.getUniqueId().toString() + ".yml");
				if (!f.exists()) {
					try {
						f.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(f);
				Boolean stuff = false;
				StringBuilder itemBought = new StringBuilder();
				for (int i = 6; i < args.length; i++) {
					if (i != 6) {
						itemBought.append(" " + args[i]);
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

				Double totalDonations = playerPurchases.getDouble("totalDonationAmount");
				Double newTotalDonations = totalDonations + Double.parseDouble(args[2]);
				playerPurchases.set("totalDonationAmount", newTotalDonations);
				try {
					playerPurchases.save(f);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (newTotalDonations >= 10.0) {
					if(!player.hasPermission("group.donor1")) {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor1");
					} else if (newTotalDonations >= 50.0) {
						if(!player.hasPermission("group.donor2")) {
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor2");
						} else if (newTotalDonations >= 100.0) {
							if(!player.hasPermission("group.donor3")) {
								Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor3");
							}
						}
					}
				}
			}
		}
		return true;
	}
}
