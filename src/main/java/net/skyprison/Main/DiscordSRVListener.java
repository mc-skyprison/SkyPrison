package net.skyprison.Main;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class DiscordSRVListener {

	public void commandRun(String msg, String perm, String channelName, ChatColor color1, ChatColor color2, ChatColor color3, ChatColor color4) {
		String[] fullmsg = msg.split(" ");
		ArrayList message = new ArrayList();
		for(int i = 3; i < fullmsg.length; i++) {
			message.add(fullmsg[i]);
		}
		String[] name = channelName.split("-");
		String fullMsg = color1 + "" + ChatColor.BOLD + "("
				+ color2 + ChatColor.BOLD + name[0].toUpperCase()
				+ color1 + ChatColor.BOLD + ") "
				+ color3 + fullmsg[1] + ChatColor.WHITE + ": " + color4 + String.join(" ", message);
		for (Player online : Bukkit.getServer().getOnlinePlayers()) {
			if (online.hasPermission(perm)) {
				online.sendMessage(fullMsg);
			}
		}
		TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(channelName);
		channel.sendMessage("**" + fullmsg[1] + "**: " + String.join(" ", message)).queue();
		Bukkit.getConsoleSender().sendMessage(fullMsg);
	}



	@Subscribe
	public void discordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {
		if(event.getChannel().getName().equalsIgnoreCase("staff-chat")) {
			event.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					commandRun(event.getProcessedMessage(),
							"cmi.command.staffmsg",
							"staff-chat",
							ChatColor.YELLOW,
							ChatColor.DARK_GRAY,
							ChatColor.DARK_RED,
							ChatColor.AQUA);
				}
			}.runTask(SkyPrisonMain.getInstance());
			event.getMessage().delete().queue();

		} else if(event.getChannel().getName().equalsIgnoreCase("guard-chat")) {
			event.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					commandRun(event.getProcessedMessage(),
							"skyprisoncore.guard.guardchat",
							"guard-chat",
							ChatColor.DARK_GRAY,
							ChatColor.GRAY,
							ChatColor.RED,
							ChatColor.DARK_AQUA);
				}
			}.runTask(SkyPrisonMain.getInstance());
			event.getMessage().delete().queue();

		} else if(event.getChannel().getName().equalsIgnoreCase("build-chat")) {
			event.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					commandRun(event.getProcessedMessage(),
							"skyprisoncore.builder.buildchat",
							"build-chat",
							ChatColor.GREEN,
							ChatColor.GRAY,
							ChatColor.RED,
							ChatColor.GREEN);
				}
			}.runTask(SkyPrisonMain.getInstance());
			event.getMessage().delete().queue();

		} else if(event.getChannel().getName().equalsIgnoreCase("admin-chat")) {
			event.setCancelled(true);
			new BukkitRunnable() {
				@Override
				public void run() {
					commandRun(event.getProcessedMessage(),
							"mcmmo.chat.adminchat",
							"admin-chat",
							ChatColor.YELLOW,
							ChatColor.GRAY,
							ChatColor.DARK_RED,
							ChatColor.RED);
				}
			}.runTask(SkyPrisonMain.getInstance());
			event.getMessage().delete().queue();

		}
	}
}
