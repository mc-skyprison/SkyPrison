package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.io.File;
import java.util.regex.Matcher;

public class ChatUtils {
	private final SkyPrisonCore plugin;
	private final DiscordApi discApi;

	public ChatUtils(SkyPrisonCore plugin, DiscordApi discApi) {
		this.plugin = plugin;
		this.discApi = discApi;
	}

	public void chatSendMessage(String[] args, CommandSender sender, String chatId, String discordId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		Player player = (Player) sender;
		if(args.length > 0) {
			StringBuilder cMessage = new StringBuilder();
			for (String arg : args) {
				cMessage.append(arg).append(" ");
			}
			String format = langConf.getString("chat." + chatId + ".format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
			String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage.toString()));
			for (Player online : Bukkit.getServer().getOnlinePlayers()) {
				if (online.hasPermission("skyprisoncore.command." + chatId)) {
					online.sendMessage(plugin.colourMessage(message));
				}
			}
			plugin.tellConsole(plugin.colourMessage(message));

			String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
			String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage.toString()));
			TextChannel channel = discApi.getTextChannelById(discordId).get();
			channel.sendMessage(dMessage);
		} else {
			stickyChatCheck(player, discordId, chatId);
		}
	}

	public void discordChatSend(String msg, String user, String chatId, String discordId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String format = langConf.getString("chat." + chatId + ".format").replaceAll("\\[name\\]", user);
		String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(msg));
		for (Player online : Bukkit.getServer().getOnlinePlayers()) {
			if (online.hasPermission("skyprisoncore.command." + chatId)) {
				online.sendMessage(plugin.colourMessage(message));
			}
		}
		plugin.tellConsole(plugin.colourMessage(message));
		String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", user);
		String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(msg));
		TextChannel channel = discApi.getTextChannelById(discordId).get();
		channel.sendMessage(dMessage);
	}


	public void consoleChatSend(String[] args, String chatId, String discordId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String prefix = langConf.getString("global.plugin-prefix");
		if(args.length > 0) {
			StringBuilder cMessage = new StringBuilder();
			for (String arg : args) {
				cMessage.append(arg).append(" ");
			}
			String format = langConf.getString("chat." + chatId + ".format").replaceAll("\\[name\\]", "Console");
			String message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage.toString()));
			for (Player online : Bukkit.getServer().getOnlinePlayers()) {
				if (online.hasPermission("skyprisoncore.command." + chatId)) {
					online.sendMessage(plugin.colourMessage(message));
				}
			}
			plugin.tellConsole(plugin.colourMessage(message));

			String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", "Console");
			String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(cMessage.toString()));

			TextChannel channel = discApi.getTextChannelById(discordId).get();
			channel.sendMessage(dMessage);
		} else {
			plugin.tellConsole(plugin.colourMessage(prefix + langConf.getString("chat." + chatId + ".wrong-usage")));
		}
	}

	public void stickyChatCheck(Player player, String discordId, String chatId) {
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
		String prefix = langConf.getString("global.plugin-prefix");
		if(plugin.stickyChat.containsKey(player.getUniqueId())) {
			if (plugin.stickyChat.get(player.getUniqueId()).equals(chatId + "-" + discordId)) {
				String stickEnabled = langConf.getString("chat.stickied.disabled").replaceAll("\\[chat\\]", chatId);
				player.sendMessage(plugin.colourMessage(prefix + stickEnabled));
				plugin.stickyChat.remove(player.getUniqueId());
			} else {
				String[] oldSticky = plugin.stickyChat.get(player.getUniqueId()).split("-");
				String stickSwapped = langConf.getString("chat.stickied.swapped").replaceAll("\\[oldchat\\]", oldSticky[0]);
				stickSwapped = stickSwapped.replaceAll("\\[newchat\\]", chatId);
				player.sendMessage(plugin.colourMessage(prefix + stickSwapped));
				plugin.stickyChat.put(player.getUniqueId(), chatId + "-" + discordId);
			}
		} else {
			String stickEnabled = langConf.getString("chat.stickied.enabled").replaceAll("\\[chat\\]", chatId);
			player.sendMessage(plugin.colourMessage(prefix + stickEnabled));
			plugin.stickyChat.put(player.getUniqueId(), chatId + "-" + discordId);
		}
	}
}
