package com.github.drakepork.skyprisoncore.Commands.referral;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.DiscordSRV.DiscordSRVListener;
import com.Zrips.CMI.Modules.DiscordSRV.DiscordSRVManager;
import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.realized.tokenmanager.api.TokenManager;
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
					player.sendMessage(ChatColor.GREEN + "If a player referred you to our server, you can do \n/referral <player> to give them some points!");
				} else {
					Long playtime = TimeUnit.MILLISECONDS.toHours(player.getTotalPlayTime());
					File f = new File(plugin.getDataFolder() +  File.separator + "referrals.yml");
					FileConfiguration refer = YamlConfiguration.loadConfiguration(f);
					boolean hasReferred = false;
					if(f.length() != 0) {
						reffedLoop:
						for(String reffedPlayers : refer.getKeys(false)) {
							for(String reffedBy : refer.getStringList(reffedPlayers + ".reffedBy")) {
								String[] split = reffedBy.split(":");
								if(split[0].equalsIgnoreCase(player.getUniqueId().toString())) {
									hasReferred = true;
									break reffedLoop;
								}
							}
						}
					}
					if(!hasReferred) {
						if(playtime < 24) {
							if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
								CMIUser reffedPlayer = CMI.getInstance().getPlayerManager().getUser(args[0]);
								if(!player.getLastIp().equalsIgnoreCase(reffedPlayer.getLastIp())) {
									String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
									if (discordId != null) {
										Member member = DiscordUtil.getMemberById(discordId);
										Role refRole = DiscordUtil.getRole("");
										DiscordUtil.addRoleToMember(member, refRole);
									}
									int refs = refer.getInt(reffedPlayer.getUniqueId().toString() + ".refsReceived")+1;
									refer.set(reffedPlayer.getUniqueId().toString() + ".refsReceived", refs);
									ArrayList arr;
									if(refer.isList(reffedPlayer.getUniqueId().toString() + ".reffedBy")) {
										arr = (ArrayList) refer.getList(reffedPlayer.getUniqueId().toString() + ".reffedBy");
									} else {
										arr = new ArrayList();
									}
									arr.add(player.getUniqueId().toString() + ":" + System.currentTimeMillis());
									refer.set(reffedPlayer.getUniqueId().toString() + ".reffedBy", arr);
									try {
										refer.save(f);
										if(reffedPlayer.isOnline()) {
											reffedPlayer.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA + " has referred you! You have received " + ChatColor.YELLOW + "150" + ChatColor.DARK_AQUA + " tokens!");
										} else {
											Bukkit.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send " + reffedPlayer.getName() + " " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA + " has referred you! You have received " + ChatColor.YELLOW + "150" + ChatColor.DARK_AQUA + " tokens!");
										}
										player.sendMessage(ChatColor.DARK_AQUA + "You successfully referred " + ChatColor.AQUA + reffedPlayer.getName() + ChatColor.DARK_AQUA + "!");
										TokenManager tm = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
										tm.addTokens(player.getPlayer(), 250);
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
						player.sendMessage(ChatColor.RED + "You have already referred someone!");
					}
				}
			} else {
				player.sendMessage(ChatColor.GREEN + "If a player referred you to our server, you can do \n/referral <player> to give them some points!");
			}
		}
		return true;
	}
}
