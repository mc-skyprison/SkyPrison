package net.skyprison.skyprisoncore.commands;

import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class KillInfo implements CommandExecutor {
	private final DatabaseHook db;

	public KillInfo(DatabaseHook db) {
		this.db = db;
	}


	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {
			int deaths = 0;
			int pKills = 0;
			int streak = 0;
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT pvp_deaths, pvp_kills, pvp_killstreak FROM users WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					deaths = rs.getInt(1);
					pKills = rs.getInt(2);
					streak = rs.getInt(3);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			double KSRatio;
			if(deaths == 0 && pKills == 0) {
				KSRatio = 0.0;
			} else if(deaths == 0) {
				KSRatio = round(pKills, 2);
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
