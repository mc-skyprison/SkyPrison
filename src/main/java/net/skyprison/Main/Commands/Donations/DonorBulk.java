package net.skyprison.Main.Commands.Donations;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DonorBulk implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		File csvFile = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
				.getDataFolder() + "/donations/test.csv");

		List<List<String>> records = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				records.add(Arrays.asList(values));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 1; i < records.size(); i++) {
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
					.getDataFolder() + "/donations/" + Bukkit.getOfflinePlayer(String.valueOf(records.get(i).get(5))).getUniqueId().toString() + ".yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String[] dates = records.get(i).get(2).split("T|\\+");
			FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(f);
			int b = 0;
			while(true) {
				if (!playerPurchases.contains(b + ".item-bought")) {
					playerPurchases.set(b + ".item-price", Double.parseDouble(records.get(i).get(7)));
					playerPurchases.set(b + ".item-currency", records.get(i).get(9));
					playerPurchases.set(b + ".bought-date", dates[0] + " " + dates[1]);
					playerPurchases.set(b + ".item-quantity", 1);
					playerPurchases.set(b + ".item-bought", records.get(i).get(15));
					try {
						playerPurchases.save(f);
						int total = records.size()-1;
						System.out.println("Success - " + records.get(i).get(5) + " " + i + "/" + total);
					} catch (final IOException e) {
						e.printStackTrace();
						System.out.println("Failed - " + records.get(i).get(5));
						System.out.println("Success - " + records.get(i).get(5) + " " + i + "/" + records.size());
					}
					break;
				} else {
					b++;
				}
			}
		}
		return true;
	}
}
