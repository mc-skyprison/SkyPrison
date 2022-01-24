package net.skyprison.skyprisoncore.commands;

import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnchTable implements CommandExecutor {
	private final SkyPrisonCore plugin;
	@Inject
	public EnchTable(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
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
				player.sendMessage(plugin.colourMessage("&cYou can't use /enchtable in this world!"));
				return true;
			}
			player.openEnchanting(loc, true);
		}
		return true;
	}
}
