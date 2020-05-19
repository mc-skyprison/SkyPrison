package net.skyprison.Main.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class KillInfo implements CommandExecutor {
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
					.getDataFolder() + "/recentKills.yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileConfiguration kills = YamlConfiguration.loadConfiguration(f);
			if(!kills.isConfigurationSection(player.getUniqueId().toString())) {
				kills.set(player.getUniqueId().toString() + ".pvpdeaths", 0);
				kills.set(player.getUniqueId().toString() + ".pvpkills", 0);
				kills.set(player.getUniqueId().toString() + ".pvpkillstreak", 0);
				try {
					kills.save(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			int deaths = kills.getInt(player.getUniqueId().toString() + ".pvpdeaths");
			int pKills = kills.getInt(player.getUniqueId().toString() + ".pvpkills");
			int streak = kills.getInt(player.getUniqueId().toString() + ".pvpkillstreak");
			Double KSRatio;
			if(deaths == 0 && pKills == 0) {
				KSRatio = 0.0;
			} else {
				KSRatio = round((double) pKills/deaths, 2);
			}
			player.sendMessage(ChatColor.RED + "--= PvP Stats =--" +
					ChatColor.GRAY + "\nPvP Kills: " + ChatColor.RED + pKills +
					ChatColor.GRAY + "\nPvP Deaths: " + ChatColor.RED + deaths +
					ChatColor.GRAY + "\nKill Streak: " + ChatColor.RED + streak +
					ChatColor.GRAY + "\nK/D Ratio: " + ChatColor.RED + KSRatio);
		}
		return true;
	}
}
