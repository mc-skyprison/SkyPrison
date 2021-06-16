package net.skyprison.skyprisoncore.Commands.contraband;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Contraband implements CommandExecutor {
	private SkyPrisonCore plugin;
	@Inject
	public Contraband(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}


	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player guard = (Player) sender;
			if(args.length == 1) {
				if(Bukkit.getPlayer(args[0]) != null) {
					Player target = Bukkit.getPlayer(args[0]);
					if(!guard.equals(target)) {
						ArrayList<String> contrabands = new ArrayList<>();
						contrabands.add("EGG");
						contrabands.add("SNOWBALL");
						contrabands.add("FLINT_AND_STEEL");
						contrabands.add("TRIDENT");
						Boolean containsCB = false;
						for (String contraband : contrabands) {
							Material cb = Material.getMaterial(contraband);
							if (target.getInventory().contains(cb)) {
								containsCB = true;
								Timer t = new Timer();
								t.scheduleAtFixedRate(new TimerTask() {
									int i = 0;
									@Override
									public void run() {
										int timeLeft = 5 - i;
										guard.sendMessage(plugin.colourMessage("&f[{#564387}&lContraband&f] {#4dabdd}They have &e&l" + timeLeft + " {#4dabdd}seconds to hand over their contraband!"));
										target.sendMessage(plugin.colourMessage("&f[{#564387}&lContraband&f] {#4dabdd}You have &e&l" + timeLeft + " {#4dabdd}seconds to hand over your contraband!"));
										if(i == 5)
											t.cancel();
										i++;
									}
								}, 0, 1000);
								break;
							}
						}
						if(!containsCB) {
							guard.sendMessage(ChatColor.RED + "Player doesnt have any contrabands!");
						}
					} else {
						guard.sendMessage(ChatColor.RED + "You can't /contraband yourself!");
					}
				} else {
					guard.sendMessage(ChatColor.RED + "/contraband <player>");
				}
			} else {
				guard.sendMessage(ChatColor.RED + "/contraband <player>");
			}
		}
		return true;
	}
}
