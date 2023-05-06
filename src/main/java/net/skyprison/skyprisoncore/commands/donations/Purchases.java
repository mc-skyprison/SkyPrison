package net.skyprison.skyprisoncore.commands.donations;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
			} else if(args.length == 1) {
				if(player.hasPermission("skyprisoncore.command.purchases.others")) {
					if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
						double totalDonor = 0;
						ArrayList<String> donations = new ArrayList<>();
						CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);

						try {
							Connection conn = hook.getSQLConnection();
							PreparedStatement ps = conn.prepareStatement("SELECT item_bought, price, date FROM donations WHERE user_id = '" + user.getUniqueId() + "'");
							ResultSet rs = ps.executeQuery();
							while (rs.next()) {
								totalDonor += rs.getDouble(2);
								donations.add("&3" + rs.getString(1) + " &f&l- &a" + rs.getDouble(2) + " &e" + rs.getString(3));
							}
							hook.close(ps, rs, conn);
						} catch (SQLException e) {
							e.printStackTrace();
						}

						if (!donations.isEmpty()) {
							player.sendMessage(ChatColor.YELLOW + "----==== " + ChatColor.GOLD + user.getName() +  " Purchases " + ChatColor.YELLOW + "====-----");
							for (String donation : donations) {
								player.sendMessage(plugin.colourMessage(donation));
							}
							player.sendMessage(ChatColor.YELLOW + "Total: " + ChatColor.GOLD + "$" + totalDonor);
						} else {
							player.sendMessage(ChatColor.RED + "Player has not donated!");
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cPlayer doesn't exist!"));
					}
				} else {
					player.sendMessage(plugin.colourMessage("§4Error:§c You do not have permission to execute this command..."));
				}
			}
		}
		return true;
	}
}
