package net.skyprison.skyprisoncore.commands.guard;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GuardDuty implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public GuardDuty(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {
			if(!player.isOp()) {
				if(!player.hasPermission("skyprisoncore.guard.onduty")) {
					if(player.hasPermission("skyprisoncore.guard.srguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent add srguard");
					} else if(player.hasPermission("skyprisoncore.guard.guard")) {
						plugin.asConsole("lp user " + player.getName() + " parent add guard");
					} else if(player.hasPermission("skyprisoncore.guard.trguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent add trguard");
					}
					player.sendMessage(Component.text("You are now ON duty!", NamedTextColor.RED));
					BukkitAudiences.create(plugin).players().sendMessage(Component.text("[", NamedTextColor.WHITE).append(Component.text("Guard", NamedTextColor.DARK_AQUA))
							.append(player.displayName().colorIfAbsent(NamedTextColor.BLUE)).append(Component.text(" is now ", NamedTextColor.AQUA))
							.append(Component.text("ON", NamedTextColor.AQUA, TextDecoration.BOLD)).append(Component.text(" duty!", NamedTextColor.AQUA)));
				} else {
					if(player.hasPermission("skyprisoncore.guard.srguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent remove srguard");
					} else if(player.hasPermission("skyprisoncore.guard.guard")) {
						plugin.asConsole("lp user " + player.getName() + " parent remove guard");
					} else if(player.hasPermission("skyprisoncore.guard.trguard")) {
						plugin.asConsole("lp user " + player.getName() + " parent remove trguard");
					}
					plugin.InvGuardGearDelPlyr(player);
					player.sendMessage(Component.text("You are now OFF duty!", NamedTextColor.RED));
					BukkitAudiences.create(plugin).players().sendMessage(Component.text("[", NamedTextColor.WHITE).append(Component.text("Guard", NamedTextColor.DARK_AQUA))
							.append(player.displayName().colorIfAbsent(NamedTextColor.BLUE)).append(Component.text(" is now ", NamedTextColor.AQUA))
							.append(Component.text("OFF", NamedTextColor.AQUA, TextDecoration.BOLD)).append(Component.text(" duty!", NamedTextColor.AQUA)));
				}
			}
		}
		return true;
	}
}
