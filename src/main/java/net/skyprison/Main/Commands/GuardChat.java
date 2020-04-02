package net.skyprison.Main.Commands;

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
            Player player = (Player)sender;
            if (!player.hasPermission("guardpkg.admin") && !player.hasPermission("guardpkg.guard") && !player.hasPermission("guardpkg.trialguard")) {
                return false;
            } else {
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message = message + args[i] + " ";
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                String fullMessage = "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "GUARD" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.RED + "" + player.getName() + ChatColor.WHITE + ": " + ChatColor.DARK_AQUA + message;
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("guardpkg.admin") || online.hasPermission("guardpkg.guard") || online.hasPermission("guardpkg.trialguard")) {
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
            String fullMessage = "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "(" + ChatColor.GRAY + ChatColor.BOLD + "GUARD" + ChatColor.DARK_GRAY + ChatColor.BOLD + ") " + ChatColor.RED + "Console" + ChatColor.WHITE + ": " + ChatColor.DARK_AQUA + message;
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.hasPermission("guardpkg.admin") || online.hasPermission("guardpkg.guard") || online.hasPermission("guardpkg.trialguard")) {
                    online.sendMessage(fullMessage);
                }
            }
            tellConsole(fullMessage);
            return true;
        }
    }
}
