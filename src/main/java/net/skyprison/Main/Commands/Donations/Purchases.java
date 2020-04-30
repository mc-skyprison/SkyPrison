package net.skyprison.Main.Commands.Donations;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Set;

public class Purchases implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
					.getDataFolder() + "/donations/" + player.getUniqueId().toString() + ".yml");
			if(!f.exists()) {
				player.sendMessage("You have not donated!");
			} else {
				FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(f);
				Set setList = playerPurchases.getKeys(false);
				for (int i = 0; i < setList.size(); i++) {
					if (playerPurchases.contains(i + ".item-bought")) {
						String itemBought = playerPurchases.getString(i + ".item-bought");
						String itemPrice = playerPurchases.getString(i + ".item-price");
						String boughtDate = playerPurchases.getString(i + ".bought-date");
						String itemQuantity = playerPurchases.getString(i + ".item-quantity");
						player.sendMessage(itemBought + " " + itemPrice + " " + boughtDate + " " + itemQuantity);
					} else {
						player.sendMessage(ChatColor.RED + "ERROR READING DONATION FILE, CONTACT ADMIN FOR SUPPORT.");
					}
				}
			}
		}
		return true;
	}
}
