package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class FirstjoinTop implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public FirstjoinTop(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	public static boolean isInteger(String s) {
		return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
		if(s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
			if(i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				else continue;
			}
			if(Character.digit(s.charAt(i),radix) < 0) return false;
		}
		return true;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {

			if (args.length > 0 && !isInteger(args[0])) {
				player.sendMessage(Component.text("Incorrect Usage! /firstjointop (page)", NamedTextColor.RED));
				return true;
			}
			LinkedHashMap<String, Long> firstJoins = new LinkedHashMap<>();
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, first_join FROM users")) {
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					if(rs.getLong(2) != 0) {
						firstJoins.put(rs.getString(1), rs.getLong(2));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			List<Map.Entry<String, Long>> entries =
					new ArrayList<>(firstJoins.entrySet());
			entries.sort(Map.Entry.comparingByValue());
			Map<String, Long> sortedMap = new LinkedHashMap<>();
			for (Map.Entry<String, Long> entry : entries) {
				sortedMap.put(entry.getKey(), entry.getValue());
			}

			boolean playerDone = false;
			ArrayList<String> playerFirstJoin = new ArrayList<>();
			ArrayList<Long> timeFirstJoin = new ArrayList<>();
			for(String playerUUID : sortedMap.keySet()) {
				playerFirstJoin.add(playerUUID);
				timeFirstJoin.add(sortedMap.get(playerUUID));
			}
			int maxNum = 10;
			int minNum = 1;
			int pageNum = 1;

			if(args.length > 0 && isInteger(args[0])) {
				pageNum = Integer.parseInt(args[0]);
				minNum = (maxNum * pageNum) - 9;
				maxNum = maxNum * pageNum;
			}

			Component firstMsg = Component.empty();
			firstMsg = firstMsg.append(Component.text("-------", NamedTextColor.GOLD, TextDecoration.BOLD))
							.append(Component.text("Firstjoin Top (Page " + pageNum + ")", NamedTextColor.YELLOW))
									.append(Component.text("-------", NamedTextColor.GOLD, TextDecoration.BOLD));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			for(int i = minNum; i <= maxNum; i++) {
				if(i == playerFirstJoin.size()) break;
				String playerUUID = playerFirstJoin.get(i - 1);
				Date firstJoinDate = new Date(timeFirstJoin.get(playerFirstJoin.indexOf(playerUUID)));
				OfflinePlayer fPlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
				String firstJoinFormat = sdf.format(firstJoinDate);
				boolean isPlayer = playerUUID.equalsIgnoreCase(player.getUniqueId().toString());
					if (isPlayer) playerDone = true;
					firstMsg = firstMsg.append(Component.text(i + ". ", isPlayer ? NamedTextColor.GREEN : NamedTextColor.GOLD))
							.append(Component.text(Objects.requireNonNull(fPlayer.getName()) + ": ", isPlayer ? NamedTextColor.GREEN : NamedTextColor.YELLOW))
							.append(Component.text(firstJoinFormat, isPlayer ? NamedTextColor.GREEN : NamedTextColor.GOLD));
			}

			if (!playerDone) {
				Date firstJoinDate = new Date(timeFirstJoin.get(playerFirstJoin.indexOf(player.getUniqueId().toString())));
				String firstJoinFormat = sdf.format(firstJoinDate);
				int playerPos = playerFirstJoin.indexOf(player.getUniqueId().toString()) + 1;
				firstMsg = firstMsg.append(Component.text(playerPos + ". " + player.getName() + ": " + firstJoinFormat , NamedTextColor.GREEN));
			}

			player.sendMessage(firstMsg);
		}
		return true;
	}
}
