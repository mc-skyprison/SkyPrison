package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Matcher;

public class ChatUtils {
	private final SkyPrisonCore plugin;
	private final DiscordApi discApi;
	private final String prefix;
	private final FileConfiguration langConf;

	public ChatUtils(SkyPrisonCore plugin, DiscordApi discApi) {
		this.plugin = plugin;
		this.discApi = discApi;
		File lang = new File(plugin.getDataFolder() + File.separator
				+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
		this.langConf = YamlConfiguration.loadConfiguration(lang);
		this.prefix = langConf.getString("global.plugin-prefix");
	}
	public void chatSendMessage(String msg, CommandSender sender, String chatId, String discordId) {
		String senderName = sender instanceof Player player ? player.getName() : "Console";
		String format = Objects.requireNonNull(langConf.getString("chat." + chatId + ".format")).replaceAll("\\[name]", Matcher.quoteReplacement(senderName));
		sendPrivateMessage(msg, chatId, format);
		if(discApi != null && discApi.getTextChannelById(discordId).isPresent()) {
			String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(senderName));
			String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(msg));
			TextChannel channel = discApi.getTextChannelById(discordId).get();
			channel.sendMessage(dMessage);
		}
	}
	public void wrongUsage(CommandSender sender, String chatId) {
		sender.sendMessage(MiniMessage.miniMessage().deserialize(prefix + langConf.getString("chat." + chatId + ".wrong-usage")));

	}
	public void discordChatSend(String msg, String user, String chatId, String discordId) {
		String format = Objects.requireNonNull(langConf.getString("chat." + chatId + ".format")).replaceAll("\\[name]", user);
		sendPrivateMessage(msg, chatId, format);
		if(discApi != null && discApi.getTextChannelById(discordId).isPresent()) {
			String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", user);
			String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(msg));
			TextChannel channel = discApi.getTextChannelById(discordId).get();
			channel.sendMessage(dMessage);
		}
	}
	private void sendPrivateMessage(String msg, String chatId, String format) {
		Audience players = plugin.getServer().filterAudience(audience -> {
			boolean hasPerm = false;
			if(audience instanceof Player player) {
				hasPerm = player.hasPermission("skyprisoncore.command." + chatId);
			}
			return hasPerm;
		});
		Audience receivers = Audience.audience(players, plugin.getServer().getConsoleSender());
		String message = format.replaceAll("\\[message]", Matcher.quoteReplacement(msg));
		receivers.sendMessage(MiniMessage.miniMessage().deserialize(message));
	}
	public void stickyChatCheck(Player player, String chatId, String discordId) {
		if(plugin.stickyChat.containsKey(player.getUniqueId())) {
			if (plugin.stickyChat.get(player.getUniqueId()).equals(chatId + "-" + discordId)) {
				String stickEnabled = Objects.requireNonNull(langConf.getString("chat.stickied.disabled")).replaceAll("\\[chat]", chatId);
				player.sendMessage(MiniMessage.miniMessage().deserialize(prefix + stickEnabled));
				plugin.stickyChat.remove(player.getUniqueId());
			} else {
				String oldSticky = plugin.stickyChat.get(player.getUniqueId()).split("-")[0];
				String stickSwapped = Objects.requireNonNull(langConf.getString("chat.stickied.swapped")).replaceAll("\\[oldchat]", oldSticky);
				stickSwapped = stickSwapped.replaceAll("\\[newchat]", chatId);
				player.sendMessage(MiniMessage.miniMessage().deserialize(prefix + stickSwapped));
				plugin.stickyChat.put(player.getUniqueId(), chatId + "-" + discordId);
			}
		} else {
			String stickEnabled = Objects.requireNonNull(langConf.getString("chat.stickied.enabled")).replaceAll("\\[chat]", chatId);
			player.sendMessage(MiniMessage.miniMessage().deserialize(prefix + stickEnabled));
			plugin.stickyChat.put(player.getUniqueId(), chatId + "-" + discordId);
		}
	}
	public static String formatNumber(double value) {
		DecimalFormat df = new DecimalFormat("###,###,###.##");
		return df.format(value);
	}
	public static String formatDate(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return sdf.format(time);
	}
	public static int timeToTicks(String time) {
		String[] split = time.split(":");
		return (int) ((Integer.parseInt(split[0]) * 1000) + ((Math.rint(Integer.parseInt(split[1]) / 60.0 * 100.0) / 100.0) * 1000));
	}
	public static  String ticksToTime(int ticks) { // 500 -> 24:00
		String time = String.valueOf(ticks / 1000.0);
		String[] split = time.split("\\.");
		int minutes = (Integer.parseInt(split[1]) * 60) % 60;
		int hours = Integer.parseInt(split[0]);
		String sMinutes = String.valueOf(minutes);
		String sHours = String.valueOf(hours);
		if(minutes < 10) {
			sMinutes = "0" + sMinutes;
		}
		if(hours < 10) {
			sHours = "0" + sHours;
		}
		return sHours + ":" + sMinutes;
	}
}
