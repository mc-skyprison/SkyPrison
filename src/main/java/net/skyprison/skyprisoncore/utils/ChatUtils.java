package net.skyprison.skyprisoncore.utils;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.regex.Matcher;

public class ChatUtils {
	private SkyPrisonCore plugin;

	public ChatUtils(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void chatSendMessage(String[] args, CommandSender sender, String chatId, String discordId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		Player player = (Player) sender;
		if(args.length > 0) {
			String cMessage = "";
			for (int i = 0; i < args.length; i++) {
				cMessage = cMessage + args[i] + " ";
			}
			String format = langConf.getString("chat." + chatId + ".format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
			String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
			for (Player online : Bukkit.getServer().getOnlinePlayers()) {
				if (online.hasPermission("skyprisoncore.command." + chatId)) {
					online.sendMessage(plugin.colourMessage(message));
				}
			}
			plugin.tellConsole(plugin.colourMessage(message));

			String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
			String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
			TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(discordId);
			channel.sendMessage(plugin.removeColour(dMessage)).queue();
		} else {
			stickyChatCheck(player, chatId.substring(0, 1).toUpperCase() + chatId.substring(1),  discordId);
		}
	}

	public void consoleChatSend(String[] args, String chatId, String discordId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String prefix = langConf.getString("global.plugin-prefix");
		if(args.length > 0) {
			String cMessage = "";
			for (int i = 0; i < args.length; i++) {
				cMessage = cMessage + args[i] + " ";
			}
			String format = langConf.getString("chat." + chatId + ".format").replaceAll("\\[name\\]", "Console");
			String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
			for (Player online : Bukkit.getServer().getOnlinePlayers()) {
				if (online.hasPermission("skyprisoncore.command." + chatId)) {
					online.sendMessage(plugin.colourMessage(message));
				}
			}
			plugin.tellConsole(plugin.colourMessage(message));

			String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", "Console");
			String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage));
			TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(discordId);
			channel.sendMessage(plugin.removeColour(dMessage)).queue();
		} else {
			plugin.tellConsole(plugin.colourMessage(prefix + langConf.getString("chat." + chatId + ".wrong-usage")));
		}
	}

	public void stickyChatCheck(Player player, String chatName, String discordId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String prefix = langConf.getString("global.plugin-prefix");
		if(plugin.stickyChat.containsKey(player.getUniqueId())) {
			if (plugin.stickyChat.get(player.getUniqueId()).equals(discordId)) {
				String stickEnabled = langConf.getString("chat.stickied.disabled").replaceAll("\\[chat\\]", chatName);
				player.sendMessage(plugin.colourMessage(prefix + stickEnabled));
				plugin.stickyChat.remove(player.getUniqueId());
			} else {
				String[] oldSticky = plugin.stickyChat.get(player.getUniqueId()).split("-");
				String oldChat = oldSticky[0].substring(0, 1).toUpperCase() + oldSticky[0].substring(1);
				String stickSwapped = langConf.getString("chat.stickied.swapped").replaceAll("\\[oldchat\\]", Matcher.quoteReplacement(oldChat));
				stickSwapped = stickSwapped.replaceAll("\\[newchat\\]", chatName);
				player.sendMessage(plugin.colourMessage(prefix + stickSwapped));
				plugin.stickyChat.put(player.getUniqueId(), discordId);
			}
		} else {
			String stickEnabled = langConf.getString("chat.stickied.enabled").replaceAll("\\[chat\\]", chatName);
			player.sendMessage(plugin.colourMessage(prefix + stickEnabled));
			plugin.stickyChat.put(player.getUniqueId(), discordId);
		}
	}
}
