package net.skyprison.Main.Commands.RanksPkg;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardChat implements CommandExecutor {
    public void tellConsole(String message){
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            if(args.length > 0) {
                Player player = (Player) sender;
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message = message + args[i] + " ";
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                String fullMessage = "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "GUARD" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.RED + "" + player.getName() + ChatColor.WHITE + ": " + ChatColor.DARK_AQUA + message;
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("skyprisoncore.guard.guardchat")) {
                        online.sendMessage(fullMessage);
                    }
                }
                TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("guard-chat");
                channel.sendMessage("**" + player.getName() + "**: " + message).queue();
                tellConsole(fullMessage);
            } else {
                sender.sendMessage(ChatColor.RED + "/g <message>");
            }
        } else {
            if(args.length > 0) {
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message = message + args[i] + " ";
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                String fullMessage = "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "GUARD" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.RED + "Console" + ChatColor.WHITE + ": " + ChatColor.DARK_AQUA + message;
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("skyprisoncore.guard.guardchat")) {
                        online.sendMessage(fullMessage);
                    }
                }
                TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("guard-chat");
                channel.sendMessage("**Console**: " + message).queue();
                tellConsole(fullMessage);
            } else {
                sender.sendMessage(ChatColor.RED + "/g <message>");
            }
        }
        return true;
    }
}
