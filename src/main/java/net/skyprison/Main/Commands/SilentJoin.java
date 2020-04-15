package main.java.net.skyprison.Main.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class SilentJoin implements Listener, CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("cmi.messages.disablelogin")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set cmi.messages.disablelogin false");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission cmi.messages.disablequit false");
                player.sendMessage(ChatColor.GREEN+"Join and leave messages now disabled...");
                return true;
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set cmi.messages.disablelogin true");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission cmi.messages.disablequit true");
                player.sendMessage(ChatColor.GREEN+"Join and leave messages now enabled...");
            }
        }
        return true;
    }
}
