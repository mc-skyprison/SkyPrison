package net.skyprison.skyprisoncore.commands.donations;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
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

public class DonorAdd implements CommandExecutor {
	private final DatabaseHook db;

	public DonorAdd(DatabaseHook db) {
		this.db = db;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (args.length > 6) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);
			if(user != null) {
				Player player = user.getPlayer();
				StringBuilder itemBought = new StringBuilder();
				for (int i = 6; i < args.length; i++) {
					if (i != 6) {
						itemBought.append(" ").append(args[i]);
					} else {
						itemBought.append(args[i]);
					}
				}

				double totalDonor = Double.parseDouble(args[2]);

				try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT price FROM donations WHERE user_id = '" + player.getUniqueId() + "'")) {
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						totalDonor += rs.getDouble(1);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				// /donoradd <player> <item-currency> <item-price> <date> <time> <amount of it bought> <item-bought>
				try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO donations (user_id, item_bought, price, currency, amount, date) VALUES (?, ?, ?, ?, ?, ?)")) {
					ps.setString(1, player.getUniqueId().toString());
					ps.setString(2, String.valueOf(itemBought));
					ps.setDouble(3, Double.parseDouble(args[2]));
					ps.setString(4, args[1]);
					ps.setInt(5, Integer.parseInt(args[5]));
					ps.setString(6, args[3] + " " + args[4]);
					ps.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}

				if (totalDonor >= 10.0) {
					if(!player.hasPermission("group.donor1")) {
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor1");
					} else if (totalDonor >= 50.0) {
						if(!player.hasPermission("group.donor2")) {
							Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor2");
						} else if (totalDonor >= 100.0) {
							if(!player.hasPermission("group.donor3")) {
								Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + args[0] + " parent add donor3");
							}
						}
					}
				}
			}
		}
		return true;
	}
}
