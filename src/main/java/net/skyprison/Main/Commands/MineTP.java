package net.skyprison.Main.Commands;

import com.Ben12345rocks.VotingPlugin.Objects.User;
import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class MineTP implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser((Player)sender);
		if (sender instanceof Player) {
			if(args.length < 1) {
				user.sendMessage(ChatColor.RED + "Command Usage: /minetp <warp>");
			} else {
				if(user.getBalance() >= 100) {
					String checkPlaceholder = PlaceholderAPI.setPlaceholders(user.getOfflinePlayer(), "%cmi_user_metaint_minetp%");
					if(checkPlaceholder.isEmpty()) {
						user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "This teleport costs" + ChatColor.YELLOW + " $100 " + ChatColor.GRAY + "to use! Click me again to confirm your teleport!");
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						String command = "cmi usermeta " + user.getName() + " increment minetp +1";
						Bukkit.dispatchCommand(console, command);
					} else {
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						Bukkit.dispatchCommand(console, "cmi warp " + args[0] + " " + user.getName());
						Bukkit.dispatchCommand(console, "cmi money take " + user.getName() + " 100 -s");
						user.sendMessage("[" + ChatColor.GREEN + "Balance" + ChatColor.WHITE + "] " +
								ChatColor.WHITE + "You spent " + ChatColor.YELLOW + "$100" +
								ChatColor.WHITE + ". New Balance: " + ChatColor.YELLOW + user.getFormatedBalance());
					}
				} else {
					user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "Requires" + ChatColor.YELLOW + " $100 " + ChatColor.GRAY + "to use!");
				}
			}
		} else {
			if(args.length < 2) {
				user.sendMessage(ChatColor.RED + "Command Usage: /minetp <warp> <player>");
			} else {
				if(user.getBalance() >= 100) {
					Player player = Bukkit.getPlayer(args[1]);
					String checkPlaceholder = PlaceholderAPI.setPlaceholders(player, "%cmi_user_metaint_minetp%");
					if(checkPlaceholder.isEmpty()) {
						user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "This teleport costs" + ChatColor.YELLOW + " $100 " + ChatColor.GRAY + "to use! Click me again to confirm your teleport!");
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						String command = "cmi usermeta " + user.getName() + " increment minetp +1";
						Bukkit.dispatchCommand(console, command);
					} else {
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						Bukkit.dispatchCommand(console, "cmi warp " + args[0] + " " + user.getName());
						Bukkit.dispatchCommand(console, "cmi money take " + user.getName() + " 100 -s");
						user.sendMessage("[" + ChatColor.GREEN + "Balance" + ChatColor.WHITE + "] " +
								ChatColor.WHITE + "You spent " + ChatColor.YELLOW + "$100" +
								ChatColor.WHITE + ". New Balance: " + ChatColor.YELLOW + user.getFormatedBalance());
					}
				} else {
					user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "Requires" + ChatColor.YELLOW + " $100 " + ChatColor.GRAY + "to use!");
				}
			}
		}
		return true;
	}
}

