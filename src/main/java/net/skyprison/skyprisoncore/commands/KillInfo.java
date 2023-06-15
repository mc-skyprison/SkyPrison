package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
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

			double KDRatio;
			if(deaths == 0 && pKills == 0) {
				KDRatio = 0.0;
			} else if(deaths == 0) {
				KDRatio = round(pKills, 2);
			} else {
				KDRatio = round((double) pKills/deaths, 2);
			}

			Component killMsg = Component.text("--= PvP Stats =--", NamedTextColor.RED);

			killMsg = killMsg.append(Component.text("\nPvP Kills: ", NamedTextColor.GRAY).append(Component.text(pKills, NamedTextColor.RED)))
					.append(Component.text("\nPvP Deaths: ", NamedTextColor.GRAY).append(Component.text(deaths, NamedTextColor.RED)))
					.append(Component.text("\nKill Streak: ", NamedTextColor.GRAY).append(Component.text(streak, NamedTextColor.RED)))
					.append(Component.text("\nK/D Ratio: ", NamedTextColor.GRAY).append(Component.text(KDRatio, NamedTextColor.RED)));
			player.sendMessage(killMsg);
		}
		return true;
	}
}
