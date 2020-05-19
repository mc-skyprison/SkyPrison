package net.skyprison.Main.Commands.Opme;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.Main.SkyPrisonMain;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class Deop implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			FileConfiguration config = SkyPrisonMain.getInstance().getConfig();
			ArrayList opped = (ArrayList) config.getStringList("opped-access");
			if(opped.contains(((Player) sender).getUniqueId().toString())) {
				if (args.length == 1) {
					String name = args[0];
					OfflinePlayer player = Bukkit.getOfflinePlayer(name);
					CMIUser user = CMI.getInstance().getPlayerManager().getUser(name);
					if(user != null) {
						if (!player.isOp()) {
							sender.sendMessage(ChatColor.RED + player.getName() + " is not opped");
						} else {
							player.setOp(false);
							sender.sendMessage(ChatColor.GRAY + player.getName() + " has been deopped!");
							Logger log = Bukkit.getLogger();
							log.info(ChatColor.RED + player.getName() + " has been deopped!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Player has not been on the server.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "/deop <player>");
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "You do not have permission to execute this command...");
			}
		} else {
			if (args.length == 1) {
				String name = args[0];
				OfflinePlayer player = Bukkit.getOfflinePlayer(name);
				CMIUser user = CMI.getInstance().getPlayerManager().getUser(name);
				if(user != null) {
					if (player.isOp()) {
						sender.sendMessage(ChatColor.RED + player.getName() + " is already opped!");
					} else {
						player.setOp(true);
						sender.sendMessage(ChatColor.GRAY + player.getName() + " has been opped!");
						Logger log = Bukkit.getLogger();
						log.info(ChatColor.RED + player.getName() + " has been opped!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player has not been on the server.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "/op <player>");
			}
		}
		return true;
	}
}