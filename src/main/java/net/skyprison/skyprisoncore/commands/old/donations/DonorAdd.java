package net.skyprison.skyprisoncore.commands.old.donations;

import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static net.skyprison.skyprisoncore.utils.PlayerManager.getPlayerId;

public class DonorAdd implements CommandExecutor {
	private final DatabaseHook db;
	public DonorAdd(DatabaseHook db) {
		this.db = db;
	}

	public static void checkTotal(Player player, double total) {
		if (total >= 10.0) {
			if (!player.hasPermission("group.donor1")) {
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " parent add donor1");
			} else if (total >= 50.0) {
				if (!player.hasPermission("group.donor2")) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " parent add donor2");
				} else if (total >= 100.0) {
					if (!player.hasPermission("group.donor3")) {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " parent add donor3");
					}
				}
			}
		}
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (args.length > 6) {
			UUID user = getPlayerId(args[0]);
			if(user != null) {
				StringBuilder itemBought = new StringBuilder();
				for (int i = 6; i < args.length; i++) {
					if (i != 6) {
						itemBought.append(" ").append(args[i]);
					} else {
						itemBought.append(args[i]);
					}
				}

				double totalDonor = Double.parseDouble(args[2]);

				try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT price FROM donations WHERE user_id = ?")) {
					ps.setString(1, user.toString());
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						totalDonor += rs.getDouble(1);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// /donoradd <player> <item-currency> <item-price> <date> <time> <amount of it bought> <item-bought>
				try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO donations (user_id, item_bought, price, currency, amount, date) VALUES (?, ?, ?, ?, ?, ?)")) {
					ps.setString(1, user.toString());
					ps.setString(2, String.valueOf(itemBought));
					ps.setDouble(3, Double.parseDouble(args[2]));
					ps.setString(4, args[1]);
					ps.setInt(5, Integer.parseInt(args[5]));
					ps.setString(6, args[3] + " " + args[4]);
					ps.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}


				Player player = Bukkit.getPlayer(user);
				if(player != null) {
					checkTotal(player, totalDonor);
				} else {
					Notifications.scheduleForOnline(user.toString(), "purchase-total-check", String.valueOf(totalDonor));
				}
			}
		}
		return true;
	}
}
