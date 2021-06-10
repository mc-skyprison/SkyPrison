package com.github.drakepork.skyprisoncore.Commands;
import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardDuty implements CommandExecutor {
	private Core plugin;
	@Inject
	public GuardDuty(Core plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if(!player.isOp()) {
				if(!player.hasPermission("skyprisoncore.guard.onduty")) {
					if(player.hasPermission("skyprisoncore.guard.srguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent add srguard");
					} else if(player.hasPermission("skyprisoncore.guard.guard")) {
						plugin.asConsole("lp user " + player.getName() + " parent add guard");
					} else if(player.hasPermission("skyprisoncore.guard.trguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent add trguard");
					}
					player.sendMessage(ChatColor.RED + "You are now ON duty!");
					plugin.asConsole(plugin.colourMessage("ctellraw all <T>&f[&3Guard&f] &9" + player.getDisplayName() + " &bis now &lON &bduty!</T>"));
				} else {
					if(player.hasPermission("skyprisoncore.guard.srguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent remove srguard");
					} else if(player.hasPermission("skyprisoncore.guard.guard")) {
						plugin.asConsole("lp user " + player.getName() + " parent remove guard");
					} else if(player.hasPermission("skyprisoncore.guard.trguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent remove trguard");
					}
					player.sendMessage(ChatColor.RED + "You are now OFF duty!");
					plugin.asConsole(plugin.colourMessage("ctellraw all <T>&f[&3Guard&f] &9" + player.getDisplayName() + " &bis now &lOFF &bduty!</T>"));
				}
			}
		}
		return true;
	}
}
