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
                } else if(player.hasPermission("skyprisoncore.guard.warden")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission unset skyprisoncore.guard.onduty");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.respawngroup.free true");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.spawngroup.free true");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.respawngroup.guard false");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.spawngroup.guard false");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.keepinventory false");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.keepexp false");
                } else if(player.hasPermission("skyprisoncore.guard.guard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent remove guard");
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
                } else if(player.hasPermission("skyprisoncore.guard.warden")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.guard.onduty");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.respawngroup.free false");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.spawngroup.free false");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.respawngroup.guard true");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.spawngroup.guard true");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.keepinventory true");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp group warden permission set cmi.keepexp true");
                } else if(player.hasPermission("skyprisoncore.guard.guard")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " parent add guard");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.guard.onduty");
                }
                if(!player.hasPermission("skyprisoncore.guard.warden")) {
                    player.sendMessage(ChatColor.GOLD + "Guard Duty: " + ChatColor.BLUE + "You have gone " + ChatColor.GREEN + "on" + ChatColor.BLUE + " duty. Thank you for your continued support in creating the prison atmosphere. You should have access to all guard commands now. Understand that you need to enforce any 'prison' rules at this time.");
                    for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                        online.sendMessage("" + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " has gone on duty...");
                    }
                } else {
                    player.sendMessage(ChatColor.GOLD + "Guard Duty: " + ChatColor.BLUE + "You have gone " + ChatColor.GREEN + "on" + ChatColor.BLUE + " duty. Thank you for your continued support in creating the prison atmosphere. You should have access to all guard commands now. Understand that you need to enforce any 'prison' rules at this time.");
                    for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                        online.sendMessage(ChatColor.YELLOW + "Warden " + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " has gone on duty...");
                    }
                }
            }
        }
        return true;
    }
}
