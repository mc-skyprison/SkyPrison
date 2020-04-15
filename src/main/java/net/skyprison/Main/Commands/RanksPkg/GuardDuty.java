package net.skyprison.Main.Commands.RanksPkg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardDuty implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(player.hasPermission("skyprisoncore.guard.onduty")) {
                if(player.hasPermission("skyprisoncore.guard.adminguard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent remove adminguard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset skyprisoncore.guard.onduty");
                } else if(player.hasPermission("skyprisoncore.guard.seniorguard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent remove seniorguard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset skyprisoncore.guard.onduty");
                } else if(player.hasPermission("skyprisoncore.guard.guard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent remove guard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset skyprisoncore.guard.onduty");
                } else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent remove trialguard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset skyprisoncore.guard.onduty");
                }
                player.sendMessage(ChatColor.GOLD + "Guard Duty: " + ChatColor.BLUE + "You have gone " + ChatColor.RED + "off" + ChatColor.BLUE + " duty. Thank you for your continued support in creating the prison atmosphere. Please dispose of your gear using " + ChatColor.RED + " /dispose" + ChatColor.BLUE + ". Understand that you are not able to enforce any 'prison' rules at this time.");
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    online.sendMessage("" + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " has gone off duty...");
                }
            } else {
                if(player.hasPermission("skyprisoncore.guard.adminguard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent add adminguard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.guard.onduty");
                } else if(player.hasPermission("skyprisoncore.guard.seniorguard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent add seniorguard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.guard.onduty");
                } else if(player.hasPermission("skyprisoncore.guard.guard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent add guard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.guard.onduty");
                } else {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent add trialguard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.guard.onduty");
                }
                player.sendMessage(ChatColor.GOLD + "Guard Duty: " + ChatColor.BLUE + "You have gone " + ChatColor.GREEN + "on" + ChatColor.BLUE + " duty. Thank you for your continued support in creating the prison atmosphere. You should have access to all guard commands now. Understand that you need to enforce any 'prison' rules at this time.");
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    online.sendMessage("" + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " has gone on duty...");
                }
            }
        }
        return true;
    }
}
