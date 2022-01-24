package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ShopBan implements CommandExecutor {
	private final SkyPrisonCore plugin;
	@Inject
	public ShopBan(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length > 0) {
				File f = new File(plugin.getDataFolder() + File.separator + "shopban.yml");
				YamlConfiguration shopConf = YamlConfiguration.loadConfiguration(f);
				switch(args[0]) {
					case "list":
						if (shopConf.getList(player.getUniqueId() + ".banned-players") != null && !shopConf.getList(player.getUniqueId() + ".banned-players").isEmpty()) {
							ArrayList<String> bannedPlayers = (ArrayList<String>) shopConf.getStringList(player.getUniqueId() + ".banned-players");
							player.sendMessage(plugin.colourMessage("&e---=== &6ShopBan &e===---"));
							for(String bannedPlayer : bannedPlayers) {
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
									ArrayList<String> bannedPlayers = new ArrayList<>();
									if (shopConf.isConfigurationSection(player.getUniqueId().toString())) {
										bannedPlayers = (ArrayList<String>) shopConf.getStringList(player.getUniqueId() + ".banned-players");
									}

									if (!bannedPlayers.contains(banUser.getUniqueId().toString())) {
										bannedPlayers.add(banUser.getUniqueId().toString());
										player.sendMessage(plugin.colourMessage("&f[&2Market&f] &7" + banUser.getName() + " &acan no longer buy/sell from your shops!"));
										shopConf.set(player.getUniqueId() + ".banned-players", bannedPlayers);
										try {
											shopConf.save(f);
										} catch (IOException e) {
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
						break;
					case "remove":
						if(args.length > 1) {
							if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
								CMIUser banUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
								ArrayList<String> bannedPlayers = new ArrayList<>();
								if (shopConf.isConfigurationSection(player.getUniqueId().toString())) {
									bannedPlayers = (ArrayList<String>) shopConf.getStringList(player.getUniqueId() + ".banned-players");
								}

								if (bannedPlayers.contains(banUser.getUniqueId().toString())) {
									bannedPlayers.remove(banUser.getUniqueId().toString());
									player.sendMessage(plugin.colourMessage("&f[&2Market&f] &7" + banUser.getName() + " &acan now buy/sell from your shops!"));
									shopConf.set(player.getUniqueId() + ".banned-players", bannedPlayers);
									try {
										shopConf.save(f);
									} catch (IOException e) {
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
