package com.github.drakepork.skyprisoncore.Commands.Chats;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildChat implements CommandExecutor {
    public void tellConsole(String message){
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length > 0) {
                Player player = (Player)sender;
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message = message + args[i] + " ";
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                String fullMessage = "" + ChatColor.GREEN + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "BUILD" + ChatColor.GREEN + ChatColor.BOLD + ") " + ChatColor.RED + "" + player.getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + message;
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("skyprisoncore.builder.buildchat")) {
                        online.sendMessage(fullMessage);
                    }
                }
                TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("build-chat");
                channel.sendMessage("**" + player.getName() + "**: " + message).queue();
                tellConsole(fullMessage);
            } else {
                sender.sendMessage(ChatColor.RED + "/b <message>");
            }
        } else {
            if(args.length > 0) {
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message = message + args[i] + " ";
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                String fullMessage = "" + ChatColor.GREEN + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "BUILD" + ChatColor.GREEN + ChatColor.BOLD + ") " + ChatColor.RED + "Console"  + ChatColor.WHITE + ": " + ChatColor.GREEN + message;
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("skyprisoncore.builder.buildchat")) {
                        online.sendMessage(fullMessage);
                    }
                }
                TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("build-chat");
                channel.sendMessage("**Console**: " + message).queue();
                tellConsole(fullMessage);
            } else {
                sender.sendMessage(ChatColor.RED + "/b <message>");
            }
        }
        return true;
    }
}
