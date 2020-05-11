package net.skyprison.Main.Commands.Donations;

import org.apache.commons.compress.compressors.FileNameUtil;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Purchases implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (args.length < 1){
				Player player = (Player) sender;
				File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
						.getDataFolder() + "/donations/" + player.getUniqueId().toString() + ".yml");
				if (!f.exists()) {
					player.sendMessage("You have not donated!");
				} else {
					FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(f);
					Set setList = playerPurchases.getKeys(false);
					player.sendMessage(ChatColor.YELLOW + "----====" + ChatColor.GOLD + " Purchases " + ChatColor.YELLOW + "====-----");
					double total = 0;
					for (int i = 0; i < setList.size(); i++) {
						if (playerPurchases.contains(i + ".item-bought")) {
							String itemBought = playerPurchases.getString(i + ".item-bought");
							double itemPrice = playerPurchases.getDouble(i + ".item-price");
							String boughtDate = playerPurchases.getString(i + ".bought-date");
							String itemQuantity = playerPurchases.getString(i + ".item-quantity");
							player.sendMessage(itemBought + " $" + itemPrice + " " + boughtDate + " " + itemQuantity + "x");
							total += playerPurchases.getDouble(i + ".item-price");
						} else {
							player.sendMessage(ChatColor.RED + "ERROR READING DONATION FILE, CONTACT ADMIN FOR SUPPORT.");
						}
					}

					player.sendMessage(ChatColor.YELLOW + "Total: " + ChatColor.GOLD + "$" + total);
				}
			} else if(args[0].equalsIgnoreCase("giveranks")) {
				File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
						.getDataFolder() + "/donations");
				File[] donorFiles = f.listFiles();
				ArrayList donorArr = new ArrayList();
				for (File file : donorFiles) {
					String playerUUID = FilenameUtils.removeExtension(file.getName());
					if(!playerUUID.equalsIgnoreCase("test")) {
						double total = 0;
						FileConfiguration playerPurchases = YamlConfiguration.loadConfiguration(file);
						Set setList = playerPurchases.getKeys(false);
						for(int i = 0; i < setList.size(); i++) {
							if(playerPurchases.contains(i + ".item-bought")) {
								total += playerPurchases.getDouble(i + ".item-price");
							} else {
								break;
							}
						}
						OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
						if(total < 20) {
							Bukkit.getPlayer("DrakePork").sendMessage(player.getName());
						}
					}
				}
			}
		}
		return true;
	}
}
