package net.skyprison.Main.Commands.RanksPkg;

import net.skyprison.Main.SkyPrisonMain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Contraband implements CommandExecutor {
	private SkyPrisonMain plugin;
	public void CommandStuff(SkyPrisonMain plugin) {
		this.plugin = plugin;

	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player guard = (Player)sender;
			if (args.length < 1) {
				guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.YELLOW + " Use /cb <target> to initiate contraband countdown...");
			} else {
				Player target = Bukkit.getServer().getPlayer(args[0]);
				if (target != null) {
					if (!plugin.cbed.contains(target)) {
						double radius = 20.0D;
						if (target.getLocation().distance(guard.getLocation()) <= radius) {
							if (plugin.InvCheckCont(target)) {
								plugin.cbed.add(target);
								plugin.cbedMap.put(target, guard);
								target.sendMessage("\n\n\n[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + guard.getName() + ChatColor.RED + " has caught you with contraband.");
								plugin.cbPunish(target, 10);
							}
							guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Player does not have contraband!");
						} else {

							guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You are not close enough to the player to execute this command!");
						}
					} else {
						guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Player has already been '/cb'ed!");
					}
				} else {
					guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Player is not online or cannot be /cb'ed...");
				}
			}
		}
		return true;
	}
}
