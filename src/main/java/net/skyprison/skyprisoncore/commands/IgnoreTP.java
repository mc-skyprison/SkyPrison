package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IgnoreTP implements CommandExecutor {
	private final SkyPrisonCore plugin;

	@Inject
	public IgnoreTP(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length == 1) {
				if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
					CMIUser ignorePlayer = CMI.getInstance().getPlayerManager().getUser(args[0]);
					OfflinePlayer adminCheck = Bukkit.getOfflinePlayer(ignorePlayer.getUniqueId());
					if(!adminCheck.isOp()) {
						File ignoreData = new File(plugin.getDataFolder() + File.separator + "teleportignore.yml");
						YamlConfiguration ignoreConf = YamlConfiguration.loadConfiguration(ignoreData);
						if (ignoreConf.isConfigurationSection(player.getUniqueId().toString())) {
							List ignoredPlayers = ignoreConf.getList(player.getUniqueId() + ".ignores");
							if (ignoredPlayers.contains(ignorePlayer.getUniqueId().toString())) {
								ignoredPlayers.remove(ignorePlayer.getUniqueId().toString());
								ignoreConf.set(player.getUniqueId() + ".ignores", ignoredPlayers);
								try {
									ignoreConf.save(ignoreData);
									player.sendMessage(plugin.colourMessage("&aSuccessfully removed " + ignorePlayer.getName() + " from your ignore list!"));
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {
								ignoredPlayers.add(ignorePlayer.getUniqueId().toString());
								ignoreConf.set(player.getUniqueId() + ".ignores", ignoredPlayers);
								try {
									ignoreConf.save(ignoreData);
									player.sendMessage(plugin.colourMessage("&aSuccessfully added " + ignorePlayer.getName() + " from your ignore list!"));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} else {
							List newIgnoreList = Lists.newArrayList(ignorePlayer.getUniqueId().toString());
							ignoreConf.set(player.getUniqueId() + ".ignores", newIgnoreList);
							try {
								ignoreConf.save(ignoreData);
								player.sendMessage(plugin.colourMessage("&aSuccessfully added " + ignorePlayer.getName() + " from your ignore list!"));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cYou cannot ignore this player!"));
					}
				} else {
					player.sendMessage(plugin.colourMessage("&cPlayer does not exist!"));
				}
			} else {
				player.sendMessage(plugin.colourMessage("&c/ignoretp <player>"));
			}
		}
		return true;
	}
}
