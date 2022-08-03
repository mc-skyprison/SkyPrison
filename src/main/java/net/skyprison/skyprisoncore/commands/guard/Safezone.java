package net.skyprison.skyprisoncore.commands.guard;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Safezone implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public Safezone(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public HashMap<UUID, Integer> safezoneViolators = new HashMap<>();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player guard = (Player) sender;
			if (args.length == 1) {
				if (Bukkit.getPlayer(args[0]) != null) {
					Player target = Bukkit.getPlayer(args[0]);
					if (!guard.equals(target)) {
						if (safezoneViolators.containsKey(target.getUniqueId())) {
							int viols = safezoneViolators.get(target.getUniqueId()) + 1;
							int violsLeft = 3 - viols;
							if (viols < 3) {
								target.sendMessage(ChatColor.RED + "You have received 1 safezone warn(s)! (" + violsLeft + " warn(s) left until jail!)");
								guard.sendMessage(ChatColor.RED + "Target has received 1 safezone warn(s)! (" + violsLeft + " warn(s) left until jail!");
								safezoneViolators.put(target.getUniqueId(), viols);
							} else {
								target.sendMessage(ChatColor.RED + "You have been jailed for safezoning!");
								guard.sendMessage(ChatColor.RED + "Target has been jailed!");
								safezoneViolators.remove(target.getUniqueId());
								plugin.asConsole("jail " + target.getName());
							}
						} else {
						safezoneViolators.put(target.getUniqueId(), 1);
						target.sendMessage(ChatColor.RED + "You have received a safezone warn! (2 warn(s) left until jail!)");
						guard.sendMessage(ChatColor.RED + "Target has received a safezone warn! (2 warn(s) left until jail!)");
						}
					} else
						guard.sendMessage(ChatColor.RED + "You can't /safezone yourself!");
				} else
					guard.sendMessage(ChatColor.RED + "No such player is online!");
			} else
				guard.sendMessage(ChatColor.RED + "/safezone <player>");
		}
		return true;
	}
}
