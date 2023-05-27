package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

public class IgnoreTeleport implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public IgnoreTeleport(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if(args.length == 1) {
				ArrayList<String> ignoredPlayers = new ArrayList<>();

				try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT ignore_id FROM teleport_ignore WHERE user_id = '" + player.getUniqueId() + "'")) {
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						ignoredPlayers.add(rs.getString(1));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}


				if(args[0].equalsIgnoreCase("list")) {
					if (!ignoredPlayers.isEmpty()) {
						player.sendMessage(plugin.colourMessage("&e---=== &6Ignoring Teleports &e===---"));
						for(String ignoredPlayer : ignoredPlayers) {
							player.sendMessage(plugin.colourMessage("&e" + Bukkit.getOfflinePlayer(UUID.fromString(ignoredPlayer)).getName()));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cYou havn't teleport ignored anyone!"));
					}
				} else {
					if (CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
						CMIUser ignorePlayer = CMI.getInstance().getPlayerManager().getUser(args[0]);
						OfflinePlayer adminCheck = Bukkit.getOfflinePlayer(ignorePlayer.getUniqueId());
						if (!adminCheck.isOp()) {
							if (ignoredPlayers.contains(ignorePlayer.getUniqueId().toString())) {
								ignoredPlayers.remove(ignorePlayer.getUniqueId().toString());
								player.sendMessage(plugin.colourMessage("&aSuccessfully removed " + ignorePlayer.getName() + " from your ignore list!"));

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM teleport_ignore WHERE user_id = ? AND ignore_id = ?")) {
									ps.setString(1, player.getUniqueId().toString());
									ps.setString(2, ignorePlayer.getUniqueId().toString());
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							} else {
								ignoredPlayers.add(ignorePlayer.getUniqueId().toString());
								player.sendMessage(plugin.colourMessage("&aSuccessfully added " + ignorePlayer.getName() + " to your ignore list!"));

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO teleport_ignore (user_id, ignore_id) VALUES (?, ?)")) {
									ps.setString(1, player.getUniqueId().toString());
									ps.setString(2, ignorePlayer.getUniqueId().toString());
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cYou cannot ignore this player!"));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cPlayer does not exist!"));
					}
				}
			} else {
				player.sendMessage(plugin.colourMessage("&c/ignoretp <player> /list"));
			}
		}
		return true;
	}
}
