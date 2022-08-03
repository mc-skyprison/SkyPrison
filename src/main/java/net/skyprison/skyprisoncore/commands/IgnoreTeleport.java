package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.common.collect.Lists;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IgnoreTeleport implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook hook;

	public IgnoreTeleport(SkyPrisonCore plugin, DatabaseHook hook) {
		this.plugin = plugin;
		this.hook = hook;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length == 1) {
				String ignoredUsers = "";

				try {
					Connection conn = hook.getSQLConnection();
					PreparedStatement ps = conn.prepareStatement("SELECT teleport_ignore FROM users WHERE user_id = '" + player.getUniqueId() + "'");
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						ignoredUsers = rs.getString(1);
						ignoredUsers = ignoredUsers.replace("[", "");
						ignoredUsers = ignoredUsers.replace("]", "");
						ignoredUsers = ignoredUsers.replace(" ", "");
					}
					hook.close(ps, rs, conn);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				ArrayList<String> ignoredPlayers = new ArrayList<>(Arrays.asList(ignoredUsers.split(",")));

				if(args[0].equalsIgnoreCase("list")) {
					if (!ignoredUsers.isEmpty()) {
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
							} else {
								ignoredPlayers.add(ignorePlayer.getUniqueId().toString());
								player.sendMessage(plugin.colourMessage("&aSuccessfully added " + ignorePlayer.getName() + " to your ignore list!"));
							}
							String sql = "UPDATE users SET teleport_ignore = ? WHERE user_id = ?";
							List<Object> params = new ArrayList<Object>() {{
								add(ignoredPlayers);
								add(player.getUniqueId().toString());
							}};
							hook.sqlUpdate(sql, params);
						} else {
							player.sendMessage(plugin.colourMessage("&cYou cannot ignore this player!"));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cPlayer does not exist!"));
					}
				}
			} else {
				player.sendMessage(plugin.colourMessage("&c/ignoretp <player> / list"));
			}
		}
		return true;
	}
}
