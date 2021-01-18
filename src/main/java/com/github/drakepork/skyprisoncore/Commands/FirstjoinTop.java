package com.github.drakepork.skyprisoncore.Commands;

import com.github.drakepork.skyprisoncore.Core;
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
	private Core plugin;

	@Inject
	public FirstjoinTop(Core plugin) {
		this.plugin = plugin;
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

			int i = 1;
			player.sendMessage(plugin.colourMessage("&6&l------- &eFirstjoin top &6&l-------"));
			for(String playerUUID : playerFirstJoin) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date firstJoinDate = new Date(timeFirstJoin.get(playerFirstJoin.indexOf(playerUUID)));
				OfflinePlayer fPlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerUUID));
				String firstJoinFormat = sdf.format(firstJoinDate);
				if(args.length == 1) {
					try {
						int pageNum = Integer.parseInt(args[0]);
						if(i <= 10) {

						}
						break;
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED + "/firstjointop (page)");
						break;
					}
				} else {
					if(i == 10) {
						if(!playerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
							player.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.YELLOW + fPlayer.getName()
									+ ": " + ChatColor.GOLD + firstJoinFormat);
						} else {
							playerDone = true;
							player.sendMessage(ChatColor.GREEN + "" + i + ". " + fPlayer.getName() + ": " + firstJoinFormat);
						}
						if(!playerDone) {
							int playerPos = playerFirstJoin.indexOf(player.getUniqueId().toString()) + 1;
							player.sendMessage(ChatColor.GREEN + "" + playerPos + ". " + player.getName() + ": " + firstJoinFormat);
						}
						break;
					} else {
						if(!playerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
							player.sendMessage(ChatColor.GOLD + "" + i + ". " + ChatColor.YELLOW + fPlayer.getName()
									+ ": " + ChatColor.GOLD + firstJoinFormat);
						} else {
							playerDone = true;
							player.sendMessage(ChatColor.GREEN + "" + i + ". " + fPlayer.getName() + ": " + firstJoinFormat);
						}
						i++;
					}
				}
			}
		}
		return true;
	}
}
