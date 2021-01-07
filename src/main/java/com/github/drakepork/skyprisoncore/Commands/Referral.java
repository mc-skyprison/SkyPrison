package com.github.drakepork.skyprisoncore.Commands;

import com.Ben12345rocks.VotingPlugin.Objects.User;
import com.Ben12345rocks.VotingPlugin.UserManager.UserManager;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Referral implements CommandExecutor {
	private Core plugin;
	@Inject
	public Referral(Core plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			CMIUser player = CMI.getInstance().getPlayerManager().getUser((OfflinePlayer) sender);
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("help")) {
					player.sendMessage(ChatColor.GREEN + "If a player referred you to our server, you can do \n/referral <player> to give them tokens ");
				} else {
					Long playtime = TimeUnit.MILLISECONDS.toHours(player.getTotalPlayTime());
					File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
							.getDataFolder() + "/referrals.yml");
					if (!f.exists()) {
						try {
							f.createNewFile();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					FileConfiguration refer = YamlConfiguration.loadConfiguration(f);
					if(!refer.isConfigurationSection(player.getUniqueId().toString())) {
						if(playtime < 3) {
							if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
								User refTokens = UserManager.getInstance().getVotingPluginUser(args[0]);
								CMIUser reffedPlayer = CMI.getInstance().getPlayerManager().getUser(args[0]);
								if(!player.getLastIp().equalsIgnoreCase(reffedPlayer.getLastIp())) {
									refer.set(player.getUniqueId().toString() + ".reffedPlayer", reffedPlayer.getUniqueId().toString());

									int refs = refer.getInt(reffedPlayer.getUniqueId().toString() + ".refsReceived")+1;
									refer.set(reffedPlayer.getUniqueId().toString() + ".refsReceived", refs);
									ArrayList arr;
									if(refer.isList(reffedPlayer.getUniqueId().toString() + ".reffedBy")) {
										arr = (ArrayList) refer.getList(reffedPlayer.getUniqueId().toString() + ".reffedBy");
									} else {
										arr = new ArrayList();
									}
									arr.add(player.getUniqueId().toString());
									refer.set(reffedPlayer.getUniqueId().toString() + ".reffedBy", arr);
									try {
										refer.save(f);
										if(reffedPlayer.isOnline()) {
											reffedPlayer.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA + " has referred you! You have received " + ChatColor.YELLOW + "150" + ChatColor.DARK_AQUA + " tokens!");
										} else {
											Bukkit.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send " + reffedPlayer.getName() + " " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA + " has referred you! You have received " + ChatColor.YELLOW + "150" + ChatColor.DARK_AQUA + " tokens!");
										}
										player.sendMessage(ChatColor.DARK_AQUA + "You successfully referred " + ChatColor.AQUA + reffedPlayer.getName() + ChatColor.DARK_AQUA + "!");
										Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
											public void run() {
												refTokens.addPoints(150);
											}});
									} catch (IOException e) {
										e.printStackTrace();
									}
								} else {
									player.sendMessage(ChatColor.RED + "/referral <player>");
								}
							} else {
								player.sendMessage(ChatColor.RED + "/referral <player>");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You have played too long to refer anyone!");
						}
					} else {
						Player reffedPlayer = Bukkit.getPlayer(UUID.fromString(
								refer.getString(player.getUniqueId().toString() + ".reffedPlayer")));
						player.sendMessage(ChatColor.RED + "You have already referred the player " + reffedPlayer.getName());
					}
				}
			} else {
				player.sendMessage(ChatColor.RED + "/referral <player>");
			}
		}
		return true;
	}
}
