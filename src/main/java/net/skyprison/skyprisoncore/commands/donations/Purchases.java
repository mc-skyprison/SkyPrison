package net.skyprison.skyprisoncore.commands.donations;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Purchases implements CommandExecutor {
	private final DatabaseHook hook;
	private final SkyPrisonCore plugin;

	public Purchases(DatabaseHook hook, SkyPrisonCore plugin) {
		this.hook = hook;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 1) {
				double totalDonor = 0;
				ArrayList<String> donations = new ArrayList<>();

				try {
					Connection conn = hook.getSQLConnection();
					PreparedStatement ps = conn.prepareStatement("SELECT item_bought, price, date FROM donations WHERE user_id = '" + player.getUniqueId() + "'");
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						totalDonor += rs.getDouble(2);
						donations.add("&3" + rs.getString(1) + " &f&l- &a" + rs.getDouble(2) + " &e" + rs.getString(3));
					}
					hook.close(ps, rs, conn);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				if (!donations.isEmpty()) {
					player.sendMessage(ChatColor.YELLOW + "----====" + ChatColor.GOLD + " Purchases " + ChatColor.YELLOW + "====-----");
					for(String donation : donations) {
						player.sendMessage(plugin.colourMessage(donation));
					}
					player.sendMessage(ChatColor.YELLOW + "Total: " + ChatColor.GOLD + "$" + totalDonor);
				} else {
					player.sendMessage(ChatColor.RED + "You have not donated!");
				}
			}
		}
		return true;
	}
}
