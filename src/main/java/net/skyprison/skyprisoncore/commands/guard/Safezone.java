package net.skyprison.skyprisoncore.commands.guard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class Safezone implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public Safezone(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public HashMap<UUID, Integer> safezoneViolators = new HashMap<>();

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player guard) {
			if (args.length == 1) {
				Player target = Bukkit.getPlayer(args[0]);
				if (target != null) {
					if (!guard.equals(target)) {
						if (safezoneViolators.containsKey(target.getUniqueId())) {
							int viols = safezoneViolators.get(target.getUniqueId()) + 1;
							int violsLeft = 3 - viols;
							if (viols < 3) {
								target.sendMessage(Component.text("You have received 1 safezone warn(s)! (" + violsLeft + " warn(s) left until jail!)", NamedTextColor.RED));
								guard.sendMessage(Component.text("Target has received 1 safezone warn(s)! (" + violsLeft + " warn(s) left until jail!)", NamedTextColor.RED));
								safezoneViolators.put(target.getUniqueId(), viols);
							} else {
								target.sendMessage(Component.text("You have been jailed for safezoning!", NamedTextColor.RED));
								guard.sendMessage(Component.text("Target has been jailed!", NamedTextColor.RED));
								safezoneViolators.remove(target.getUniqueId());
								plugin.asConsole("jail " + target.getName());
							}
						} else {
						safezoneViolators.put(target.getUniqueId(), 1);
						target.sendMessage(Component.text("You have received a safezone warn! (2 warn(s) left until jail!)", NamedTextColor.RED));
						guard.sendMessage(Component.text("Target has received a safezone warn! (2 warn(s) left until jail!)", NamedTextColor.RED));
						}
					} else
						guard.sendMessage(Component.text("You can't /safezone yourself!", NamedTextColor.RED));
				} else
					guard.sendMessage(Component.text("No such player is online!", NamedTextColor.RED));
			} else
				guard.sendMessage(Component.text("/safezone <player>", NamedTextColor.RED));
		}
		return true;
	}
}
