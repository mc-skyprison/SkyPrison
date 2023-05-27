package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
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
import java.util.ArrayList;
import java.util.UUID;

public class ShopBan implements CommandExecutor {
	private final DatabaseHook db;
	private final SkyPrisonCore plugin;

	public ShopBan(DatabaseHook db, SkyPrisonCore plugin) {
		this.db = db;
		this.plugin = plugin;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if(args.length > 0) {
				ArrayList<String> bannedUsers = new ArrayList<>();

				try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT banned_user FROM shop_banned WHERE user_id = ?")) {
					ps.setString(1, player.getUniqueId().toString());
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						bannedUsers.add(rs.getString(1));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				switch (args[0]) {
					case "list" -> {
						if (!bannedUsers.isEmpty()) {
							player.sendMessage(plugin.colourMessage("&e---=== &6ShopBan &e===---"));
							for (String bannedPlayer : bannedUsers) {
								player.sendMessage(plugin.colourMessage("&e" + Bukkit.getOfflinePlayer(UUID.fromString(bannedPlayer)).getName()));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cYou havn't banned anyone from your shops!"));
						}
					}
					case "add" -> {
						if (args.length > 1) {
							if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
								CMIUser banUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
								if (!banUser.getPlayer().equals(player)) {
									if (!bannedUsers.contains(banUser.getUniqueId().toString())) {
										try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO shop_banned (user_id, banned_user) VALUES (?, ?)")) {
											ps.setString(1, player.getUniqueId().toString());
											ps.setString(2, banUser.getUniqueId().toString());
											ps.executeUpdate();
											player.sendMessage(plugin.colourMessage("&f[&2Market&f] &7" + banUser.getName() + " &acan no longer buy/sell from your shops!"));
										} catch (SQLException e) {
											e.printStackTrace();
										}
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
					}
					case "remove" -> {
						if (args.length > 1) {
							if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
								CMIUser banUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
								if (bannedUsers.contains(banUser.getUniqueId().toString())) {
									try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM shop_banned WHERE user_id = ? AND banned_user = ?")) {
										ps.setString(1, player.getUniqueId().toString());
										ps.setString(2, banUser.getUniqueId().toString());
										ps.executeUpdate();
										player.sendMessage(plugin.colourMessage("&f[&2Market&f] &7" + banUser.getName() + " &acan now buy/sell from your shops!"));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								} else {
									player.sendMessage(plugin.colourMessage("&cThat player isnt banned from your shops!"));
								}
							} else {
								player.sendMessage(plugin.colourMessage("&c" + args[1] + " has never joined the server before!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cCorrect Usage: /shopban remove <player>"));
						}
					}
					default ->
							player.sendMessage(plugin.colourMessage("&a/shopban list\n/shopban remove <player>\n/shopban add <player>"));
				}
			} else {
				player.sendMessage(plugin.colourMessage("&a/shopban list\n/shopban remove <player>\n/shopban add <player>"));
			}

		}
		return true;
	}
}
