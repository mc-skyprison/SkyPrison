package net.skyprison.skyprisoncore.commands;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class FirstjoinTop implements CommandExecutor {
	private SkyPrisonCore plugin;

	@Inject
	public FirstjoinTop(SkyPrisonCore plugin) {
		this.plugin = plugin;
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

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			File f = new File(plugin.getDataFolder() + File.separator + "firstjoindata.yml");
			YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
			Set<String> setList = yamlf.getKeys(false);
			LinkedHashMap<String, Long> firstJoins = new LinkedHashMap<>();
			for (String pUUID : setList) {
				Long firstJoinTime = yamlf.getLong(pUUID + ".firstjoin");
				firstJoins.put(pUUID, firstJoinTime);
			}
			List<Map.Entry<String, Long>> entries =
					new ArrayList<>(firstJoins.entrySet());
			Collections.sort(entries, Comparator.comparing(Map.Entry::getValue));
			Map<String, Long> sortedMap = new LinkedHashMap<>();
			for (Map.Entry<String, Long> entry : entries) {
				sortedMap.put(entry.getKey(), entry.getValue());
			}

			Boolean playerDone = false;
			ArrayList<String> playerFirstJoin = new ArrayList();
			ArrayList<Long> timeFirstJoin = new ArrayList();
			for(String playerUUID : sortedMap.keySet()) {
				playerFirstJoin.add(playerUUID);
				timeFirstJoin.add(sortedMap.get(playerUUID));
			}
			int maxNum = 10;
			int minNum = 1;
			int pageNum = 1;

			if(args.length == 1) {
				if(isInteger(args[0])) {
					pageNum = Integer.parseInt(args[0]);
					minNum = (maxNum * pageNum) - 9;
					maxNum = maxNum * pageNum;
				}
			}

			player.sendMessage(plugin.colourMessage("&6&l------- &eFirstjoin top (Page " + pageNum + ") &6&l-------"));
			for(int i = minNum; i <= maxNum; i++) {
				if ((i >= 0) && (i < playerFirstJoin.size())) {
					String playerUUID = playerFirstJoin.get(i - 1);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date firstJoinDate = new Date(timeFirstJoin.get(playerFirstJoin.indexOf(playerUUID)));
					OfflinePlayer fPlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
					String firstJoinFormat = sdf.format(firstJoinDate);
					if (args.length == 1) {
						if (isInteger(args[0])) {
							if (i == maxNum) {
								if (!playerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
									player.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.YELLOW + fPlayer.getName()
											+ ": " + ChatColor.GOLD + firstJoinFormat);
								} else {
									playerDone = true;
									player.sendMessage(ChatColor.GREEN + "" + i + ". " + fPlayer.getName() + ": " + firstJoinFormat);
								}
								if (!playerDone) {
									firstJoinDate = new Date(timeFirstJoin.get(playerFirstJoin.indexOf(player.getUniqueId().toString())));
									firstJoinFormat = sdf.format(firstJoinDate);
									int playerPos = playerFirstJoin.indexOf(player.getUniqueId().toString()) + 1;
									player.sendMessage(ChatColor.GREEN + "" + playerPos + ". " + player.getName() + ": " + firstJoinFormat);
								}
								break;
							} else {
								if (!playerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
									player.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.YELLOW + fPlayer.getName()
											+ ": " + ChatColor.GOLD + firstJoinFormat);
								} else {
									playerDone = true;
									player.sendMessage(ChatColor.GREEN + "" + i + ". " + fPlayer.getName() + ": " + firstJoinFormat);
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "/firstjointop (page)");
							break;
						}
					} else {
						if (i == 10) {
							if (!playerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
								player.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.YELLOW + fPlayer.getName()
										+ ": " + ChatColor.GOLD + firstJoinFormat);
							} else {
								playerDone = true;
								player.sendMessage(ChatColor.GREEN + "" + i + ". " + fPlayer.getName() + ": " + firstJoinFormat);
							}
							if (!playerDone) {
								int playerPos = playerFirstJoin.indexOf(player.getUniqueId().toString()) + 1;
								player.sendMessage(ChatColor.GREEN + "" + playerPos + ". " + player.getName() + ": " + firstJoinFormat);
							}
							break;
						} else {
							if (!playerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
								player.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.YELLOW + fPlayer.getName()
										+ ": " + ChatColor.GOLD + firstJoinFormat);
							} else {
								playerDone = true;
								player.sendMessage(ChatColor.GREEN + "" + i + ". " + fPlayer.getName() + ": " + firstJoinFormat);
							}
						}
					}
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date firstJoinDate = new Date(timeFirstJoin.get(playerFirstJoin.indexOf(player.getUniqueId().toString())));
					String firstJoinFormat = sdf.format(firstJoinDate);
					int playerPos = playerFirstJoin.indexOf(player.getUniqueId().toString()) + 1;
					player.sendMessage(ChatColor.GREEN + "" + playerPos + ". " + player.getName() + ": " + firstJoinFormat);
					break;
				}
			}
		}
		return true;
	}
}
