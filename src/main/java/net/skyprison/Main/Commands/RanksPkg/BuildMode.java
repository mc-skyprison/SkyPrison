package net.skyprison.Main.Commands.RanksPkg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildMode implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("skyprisoncore.builder.onduty")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent remove builder");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset skyprisoncore.builder.onduty");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "gamemode survival " + player.getName());
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi warp prison " + player.getName());
                player.sendMessage(ChatColor.GOLD + "Build Mode: " + ChatColor.BLUE + "You have gone " + ChatColor.RED + "off" + ChatColor.BLUE + " duty.");
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent add builder");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.builder.onduty");
                player.sendMessage(ChatColor.GOLD + "Build Mode: " + ChatColor.BLUE + "You have gone " + ChatColor.GREEN + "on" + ChatColor.BLUE + " duty.");
            }
        }
        return true;
    }
}
