package net.skyprison.skyprisoncore.utils;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.party.PartyManager;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.List;

public class DiscordUtil {
	private SkyPrisonCore plugin;

	public DiscordUtil(SkyPrisonCore plugin) {
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

	public void partyRun(DiscordGuildMessagePreProcessEvent event) {
		event.setCancelled(true);
		String msg = event.getMessage().getContentRaw();
		String UserName = event.getAuthor().getName();
		new BukkitRunnable() {
			@Override
			public void run() {
				String[] splitMsg = msg.split(" ", 2);
				String message = "&a(P) &f" + UserName + " &a→ &f" + splitMsg[1];
				Party p;
				if(Bukkit.getPlayer(splitMsg[0]) != null) {
					p = PartyManager.getParty(Bukkit.getPlayer(splitMsg[0]));
				} else {
					p = PartyManager.getParty(splitMsg[0]);
				}

				if(p != null) {
					List pMembers = p.getOnlineMembers();
					for (Object online : pMembers) {
						Player oPlayer = (Player) online;
						oPlayer.sendMessage(plugin.colourMessage(message));
					}
					plugin.tellConsole(plugin.colourMessage(message));
					String dMessage = "(**" + p.getName() + "**) " + UserName + " » " + splitMsg[1];
					TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("party-chats");
					channel.sendMessage(dMessage).queue();
				}
			}
		}.runTask(plugin);
		event.getMessage().delete().queue();
	}

	@Subscribe
	public void discordMessageProcessed(DiscordGuildMessagePreProcessEvent event) {
		switch(event.getChannel().getName().toLowerCase()) {
			case "admin-chat":
				commandRun(event, "skyprisoncore.command.admin", "admin-chat", "chat.admin.format");
				break;
			case "build-chat":
				commandRun(event, "skyprisoncore.command.build", "build-chat", "chat.build.format");
				break;
			case "guard-chat":
				commandRun(event, "skyprisoncore.command.guard", "guard-chat", "chat.guard.format");
				break;
			case "discord-chat":
				commandRun(event, "skyprisoncore.command.discord", "discord-chat", "chat.discord.format");
				break;
			case "staff-chat":
				commandRun(event, "skyprisoncore.command.staff", "staff-chat", "chat.staff.format");
				break;
			case "party-chats":
				partyRun(event);
				break;
		}
	}
}
