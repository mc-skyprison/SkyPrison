package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShopBan implements CommandExecutor {
	private final DatabaseHook hook;
	private final SkyPrisonCore plugin;

	public ShopBan(DatabaseHook hook, SkyPrisonCore plugin) {
		this.hook = hook;
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length > 0) {
				ArrayList<String> bannedUsers = new ArrayList<>();

				try {
					Connection conn = hook.getSQLConnection();
					PreparedStatement ps = conn.prepareStatement("SELECT banned_user FROM shop_banned WHERE user_id = '" + player.getUniqueId() + "'");
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						bannedUsers.add(rs.getString(1));
					}
					hook.close(ps, rs, conn);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				switch(args[0]) {
					case "list":
						if (!bannedUsers.isEmpty()) {
							player.sendMessage(plugin.colourMessage("&e---=== &6ShopBan &e===---"));
							for(String bannedPlayer : bannedUsers) {
								player.sendMessage(plugin.colourMessage("&e" + Bukkit.getOfflinePlayer(UUID.fromString(bannedPlayer)).getName()));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cYou havn't banned anyone from your shops!"));
						}
						break;
					case "add":
						if(args.length > 1) {
							if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
								CMIUser banUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
								if(!banUser.getPlayer().equals(player)) {
									if (!bannedUsers.contains(banUser.getUniqueId().toString())) {
										String sql = "INSERT INTO shop_banned (user_id, banned_user) VALUES (?, ?)";
										List<Object> params = new ArrayList<Object>() {{
											add(player.getUniqueId().toString());
											add(banUser.getUniqueId().toString());
										}};
										hook.sqlUpdate(sql, params);

										player.sendMessage(plugin.colourMessage("&f[&2Market&f] &7" + banUser.getName() + " &acan no longer buy/sell from your shops!"));
									} else {
										player.sendMessage(plugin.colourMessage("&cThat player is already banned from your shops!"));
									}
								} else {
									player.sendMessage(plugin.colourMessage("&cYou can't ban yourself!"));
								}
							} else {
								player.sendMessage(plugin.colourMessage("&c" + args[1] + " has never joined the server before!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cCorrect Usage: /shopban add <player>"));
						}
						break;
					case "remove":
						if(args.length > 1) {
							if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
								CMIUser banUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
								if (bannedUsers.contains(banUser.getUniqueId().toString())) {
									String sql = "DELETE FROM shop_banned WHERE user_id = ? AND banned_user = ?";
									List<Object> params = new ArrayList<Object>() {{
										add(player.getUniqueId().toString());
										add(banUser.getUniqueId().toString());
									}};
									hook.sqlUpdate(sql, params);

									player.sendMessage(plugin.colourMessage("&f[&2Market&f] &7" + banUser.getName() + " &acan now buy/sell from your shops!"));
								} else {
									player.sendMessage(plugin.colourMessage("&cThat player isnt banned from your shops!"));
								}
							} else {
								player.sendMessage(plugin.colourMessage("&c" + args[1] + " has never joined the server before!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cCorrect Usage: /shopban remove <player>"));
						}
						break;
					default:
						player.sendMessage(plugin.colourMessage("&a/shopban list\n/shopban remove <player>\n/shopban add <player>"));
				}
			} else {
				player.sendMessage(plugin.colourMessage("&a/shopban list\n/shopban remove <player>\n/shopban add <player>"));
			}

		}
		return true;
	}
}
