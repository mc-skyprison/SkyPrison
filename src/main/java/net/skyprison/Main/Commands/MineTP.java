package net.skyprison.Main.Commands;

import com.Ben12345rocks.VotingPlugin.Objects.User;
import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;
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
		if (sender instanceof Player) {
			User user = UserManager.getInstance().getVotingPluginUser((Player) sender);
			if(args.length < 1) {
				user.sendMessage(ChatColor.RED + "Command Usage: /minetp <warp>");
			} else {
				if(user.getPoints() >= 25) {
					Player player = (Player)sender;
					String checkPlaceholder = PlaceholderAPI.setPlaceholders(player, "%cmi_user_metaint_minetp%");
					if(checkPlaceholder.isEmpty()) {
						user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "This teleport costs" + ChatColor.YELLOW + " 25 " + ChatColor.GRAY + "tokens to use! Click me again to confirm your teleport!");
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						String command = "cmi usermeta " + user.getPlayerName() + " increment minetp +1";
						Bukkit.dispatchCommand(console, command);
					} else {
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						String command = "cmi warp " + args[0] + " " + user.getPlayerName();
						user.sendMessage("[" + ChatColor.AQUA + "Tokens" + ChatColor.WHITE + "] " + "You used " + ChatColor.YELLOW + "25" + ChatColor.WHITE + " Tokens");
						Bukkit.dispatchCommand(console, command);
						user.removePoints(25);
					}
				} else {
					user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "Requires" + ChatColor.YELLOW + " 25 " + ChatColor.GRAY + " tokens to use!");
				}
			}
		} else {
			User user = UserManager.getInstance().getVotingPluginUser(args[1]);
			if(args.length < 2) {
				user.sendMessage(ChatColor.RED + "Command Usage: /minetp <warp> <player>");
			} else {
				if(user.getPoints() >= 25) {
					Player player = Bukkit.getPlayer(args[1]);
					String checkPlaceholder = PlaceholderAPI.setPlaceholders(player, "%cmi_user_metaint_minetp%");
					if(checkPlaceholder.isEmpty()) {
						user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "This teleport costs" + ChatColor.YELLOW + " 25 " + ChatColor.GRAY + "tokens to use! Click me again to confirm your teleport!");
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						String command = "cmi usermeta " + user.getPlayerName() + " increment minetp +1";
						Bukkit.dispatchCommand(console, command);
					} else {
						ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
						String command = "cmi warp " + args[0] + " " + user.getPlayerName();
						user.sendMessage("[" + ChatColor.AQUA + "Tokens" + ChatColor.WHITE + "] " + "You used " + ChatColor.YELLOW + "25" + ChatColor.WHITE + " Tokens");
						Bukkit.dispatchCommand(console, command);
						user.removePoints(25);
					}
				} else {
					user.sendMessage("["+ChatColor.RED + "MineTeleport" + ChatColor.WHITE + "] " + ChatColor.GRAY + "Requires" + ChatColor.YELLOW + " 25 " + ChatColor.GRAY + " tokens to use!");
				}
			}
		}
		return true;
	}
}

