package com.github.drakepork.skyprisoncore.Listeners;

import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class DiscordSRVListener {
	private Core plugin;

	@Inject
	public DiscordSRVListener(Core plugin) {
		this.plugin = plugin;
	}

	public void commandRun(DiscordGuildMessagePreProcessEvent event, String perm, String channelName, String langFormat) {
		event.setCancelled(true);
		String msg = event.getMessage().getContentRaw();
		String UserName = event.getAuthor().getName();
		new BukkitRunnable() {
			@Override
			public void run() {
				File lang = new File(plugin.getDataFolder() + File.separator
						+ "lang" + File.separator + plugin.getConfig().getString("lang-file"));
				FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

				String message = ChatColor.stripColor(String.join(" ", msg));
				String format = langConf.getString(langFormat).replaceAll("\\[name\\]", ChatColor.stripColor(UserName));
				message = format.replaceAll("\\[message\\]", message);
				for (Player online : Bukkit.getServer().getOnlinePlayers()) {
					if (online.hasPermission(perm)) {
						online.sendMessage(plugin.colourMessage(message));
					}
				}
				plugin.tellConsole(plugin.colourMessage(message));

				String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", ChatColor.stripColor(UserName));
				String dMessage = dFormat.replaceAll("\\[message\\]", ChatColor.stripColor(String.join(" ", msg)));
				TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
				channel.sendMessage(dMessage).queue();
			}
		}.runTask(plugin);
		event.getMessage().delete().queue();
	}

	@Subscribe
	public void discordMessageProcessed(DiscordGuildMessagePreProcessEvent event) {
		switch(event.getChannel().getName().toLowerCase()) {
			case "admin-chat":
				commandRun(event, "royalasylum.chat.admin", "admin-chat", "chat.admin.format");
				break;
			case "build-chat":
				commandRun(event, "royalasylum.chat.build", "build-chat", "chat.build.format");
				break;
			case "guard-chat":
				commandRun(event, "royalasylum.chat.guard", "guard-chat", "chat.guard.format");
				break;
			case "discord-chat":
				commandRun(event, "royalasylum.chat.discord", "discord-chat", "chat.discord.format");
				break;
		}
	}
}