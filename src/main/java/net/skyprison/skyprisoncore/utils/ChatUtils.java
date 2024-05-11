package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.kyori.adventure.text.format.TextDecoration.State.FALSE;
import static net.skyprison.skyprisoncore.SkyPrisonCore.pluginPrefix;

public class ChatUtils {
	private final SkyPrisonCore plugin;
	private final DiscordApi discApi;

	public final static List<Chats> chats = List.of(
			new Chats("build", "800885673732997121", text("BUILDER", GREEN, BOLD), GRAY, BLUE),
			new Chats("admin", "791054229136605194", text("ADMIN", RED, BOLD), GRAY, GREEN),
			new Chats("guard", "791054021338464266", text("GUARD", BLUE, BOLD), GRAY, DARK_AQUA),
			new Chats("staff", "791054076787163166", text("STAFF", GREEN, BOLD), GRAY, AQUA)
	);

	public record Chats(String chatId, String discordId, Component prefix, TextColor nameColour, TextColor msgColour) {}

	public ChatUtils(SkyPrisonCore plugin, DiscordApi discApi) {
		this.plugin = plugin;
		this.discApi = discApi;
	}

	public void sendPrivateMessage(String msg, String senderName, String chatId) {
		Chats chat = chats.stream().filter(c -> c.chatId().equals(chatId)).findFirst().orElse(null);
		if(chat == null) return;
		Component formattedMsg = text(senderName, chat.nameColour).append(text(" » ", DARK_GRAY)).append(MiniMessage.miniMessage().deserialize(msg)
				.colorIfAbsent(chat.msgColour)).decorationIfAbsent(BOLD, FALSE);
		Audience receivers = plugin.getServer().filterAudience(audience ->
				(audience instanceof Player player && player.hasPermission("skyprisoncore.command." + chatId)) || audience instanceof ConsoleCommandSender);
		receivers.sendMessage(chat.prefix.appendSpace().append(formattedMsg));

		if(discApi != null && discApi.getTextChannelById(chat.discordId).isPresent()) {
			TextChannel channel = discApi.getTextChannelById(chat.discordId).get();
			channel.sendMessage("**" + senderName + "**: " + MiniMessage.miniMessage().stripTags(msg));
		}
	}

	public void stickyChatCheck(Player player, String chatId) {
		if(plugin.stickyChat.containsKey(player.getUniqueId())) {
			if (plugin.stickyChat.get(player.getUniqueId()).equals(chatId)) {
				player.sendMessage(pluginPrefix.append(text("Disabled sticky chat for " + chatId, GREEN)));
				plugin.stickyChat.remove(player.getUniqueId());
			} else {
				String oldSticky = plugin.stickyChat.get(player.getUniqueId());
				player.sendMessage(pluginPrefix.append(text("Swapped sticky chat from " + oldSticky + " to " + chatId, GREEN)));
				plugin.stickyChat.put(player.getUniqueId(), chatId);
			}
		} else {
			player.sendMessage(pluginPrefix.append(text("Enabled sticky chat for " + chatId, GREEN)));
			plugin.stickyChat.put(player.getUniqueId(), chatId);
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

	public static String ticksToTime(int ticks) { // 500 -> 24:00
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
