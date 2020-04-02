package net.skyprison.Main.Commands.RanksPkg;

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
            Player player = (Player)sender;
            if (!player.hasPermission("builderpkg.admin") && !player.hasPermission("builderpkg.builder")) {
                return false;
            } else {
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message = message + args[i] + " ";
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                String fullMessage = "" + ChatColor.GREEN + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "BUILDER" + ChatColor.GREEN + ChatColor.BOLD + ") " + ChatColor.RED + "" + player.getName() + ChatColor.WHITE + ": " + ChatColor.GREEN + message;
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("builderpkg.builder") || online.hasPermission("builderpkg.admin")) {
                        online.sendMessage(fullMessage);
                    }
                }
                tellConsole(fullMessage);
                return true;
            }
        } else {
            String message = "";
            for (int i = 0; i < args.length; i++) {
                message = message + args[i] + " ";
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            String fullMessage = "" + ChatColor.GREEN + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "BUILDER" + ChatColor.GREEN + ChatColor.BOLD + ") " + ChatColor.RED + "Console"  + ChatColor.WHITE + ": " + ChatColor.GREEN + message;
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.hasPermission("builderpkg.builder") || online.hasPermission("builderpkg.admin")) {
                    online.sendMessage(fullMessage);
                }
            }
            tellConsole(fullMessage);
            return true;
        }
    }
}
