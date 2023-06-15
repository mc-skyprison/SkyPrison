package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnchTable implements CommandExecutor {
	public EnchTable() {}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			Location loc;
			if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
				loc = new Location(Bukkit.getWorld("world_prison"), -121, 150, -175);
			} else if(player.getWorld().getName().equalsIgnoreCase("world_free")) {
				loc = new Location(Bukkit.getWorld("world_free"), -2941, 148, -758);
			} else if(player.getWorld().getName().equalsIgnoreCase("world_free_nether")) {
				loc = new Location(Bukkit.getWorld("world_free_nether"), -17, 116, -52);
			} else if(player.getWorld().getName().equalsIgnoreCase("world_free_end")) {
				loc = new Location(Bukkit.getWorld("world_free_end"), -203, 77, 4);
			} else {
				player.sendMessage(Component.text("You can't use /enchtable in this world!", NamedTextColor.RED));
				return true;
			}
			player.openEnchanting(loc, true);
		}
		return true;
	}
}
