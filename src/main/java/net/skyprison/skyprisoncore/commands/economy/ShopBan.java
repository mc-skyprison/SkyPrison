package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import java.util.Objects;
import java.util.UUID;

public class ShopBan implements CommandExecutor {
	private final DatabaseHook db;

	public ShopBan(DatabaseHook db) {
		this.db = db;
	}

	private final Component prefix = Component.text("[", NamedTextColor.WHITE).append(Component.text("Market", NamedTextColor.GREEN).append(Component.text("] ", NamedTextColor.WHITE)));

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
							player.sendMessage(Component.text("---=== ", NamedTextColor.YELLOW).append(Component.text("ShopBan", NamedTextColor.GOLD).append(Component.text(" ===---", NamedTextColor.YELLOW))));
							for (String bannedPlayer : bannedUsers) {
								player.sendMessage(Component.text(Objects.requireNonNull(Bukkit.getOfflinePlayer(UUID.fromString(bannedPlayer)).getName()), NamedTextColor.YELLOW));
							}
						} else {
							player.sendMessage(Component.text("You havn't banned anyone from your shops!", NamedTextColor.RED));
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
											player.sendMessage(prefix.append(Component.text(banUser.getName(), NamedTextColor.GRAY).append(Component.text(" can no longer buy/sell from your shops!", NamedTextColor.GREEN))));
										} catch (SQLException e) {
											e.printStackTrace();
										}
									} else {
										player.sendMessage(Component.text("That player is already banned from your shops!", NamedTextColor.RED));
									}
								} else {
									player.sendMessage(Component.text("You can't ban yourself!", NamedTextColor.RED));
								}
							} else {
								player.sendMessage(Component.text(args[1] + " has never joined the server before!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("Inorrect Usage! /shopban add <player>", NamedTextColor.RED));
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
										player.sendMessage(prefix.append(Component.text(banUser.getName(), NamedTextColor.GRAY).append(Component.text(" can now buy/sell from your shops!", NamedTextColor.GREEN))));
									} catch (SQLException e) {
										e.printStackTrace();
									}
								} else {
									player.sendMessage(Component.text("That player isnt banned from your shops!", NamedTextColor.RED));
								}
							} else {
								player.sendMessage(Component.text("&c" + args[1] + " has never joined the server before!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("Inorrect Usage! /shopban remove <player>", NamedTextColor.RED));
						}
					}
					default ->
							player.sendMessage(Component.text("/shopban list\n/shopban remove <player>\n/shopban add <player>", NamedTextColor.GREEN));
				}
			} else {
				player.sendMessage(Component.text("/shopban list\n/shopban remove <player>\n/shopban add <player>", NamedTextColor.GREEN));
			}

		}
		return true;
	}
}
