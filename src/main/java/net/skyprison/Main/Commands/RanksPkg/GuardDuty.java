package net.skyprison.Main.Commands.RanksPkg;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class GuardDuty implements CommandExecutor {
    private void DemoteGuard(Player player, int steps) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "lp user " + player.getName() + " demote guard";
        for (int i = 0; i < steps; i++) {
            Bukkit.dispatchCommand(console, command);
        }
    }

    private void PromoteGuard(Player player, int steps) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "lp user " + player.getName() + " promote guard";
        for (int i = 0; i < steps; i++) {
            Bukkit.dispatchCommand(console, command);
        }
    }

    private void GuardOnDuty(Player player) {
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            online.sendMessage("" + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " has gone on duty...");
        }
        player.sendMessage(ChatColor.GOLD + "Guard Duty: " + ChatColor.BLUE + "You have gone " + ChatColor.GREEN + "on" + ChatColor.BLUE + " duty. Thank you for your continued support in creating the prison atmosphere. You should have access to all guard commands now. Understand that you need to enforce any 'prison' rules at this time.");
    }

    private void GuardOffDuty(Player player) {
        if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        }
        player.sendMessage(ChatColor.GOLD + "Guard Duty: " + ChatColor.BLUE + "You have gone " + ChatColor.RED + "off" + ChatColor.BLUE + " duty. Thank you for your continued support in creating the prison atmosphere. Please dispose of your gear using " + ChatColor.RED + " /dispose" + ChatColor.BLUE + ". Understand that you are not able to enforce any 'prison' rules at this time.");
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            online.sendMessage("" + ChatColor.LIGHT_PURPLE + player.getName() + ChatColor.YELLOW + " has gone off duty...");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("guardpkg.guard") || player.hasPermission("guardpkg.trialguard") || player.hasPermission("guardpkg.admin")) {
                if (player.hasPermission("guardpkg.admin") && player.hasPermission("guardpkg.onduty")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set guardpkg.onduty false");
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " meta removeprefix 101");
                    GuardOffDuty(player);
                    return true;
                }
                if (player.hasPermission("guardpkg.admin") && !player.hasPermission("guardpkg.onduty")) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set guardpkg.onduty true");
                    if (!player.getDisplayName().equalsIgnoreCase("easels")) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " meta setprefix 101 \"[&4Adm-&9Guard&r] \"");
                    }
                    GuardOnDuty(player);
                    return true;
                }
                if (player.hasPermission("guardpkg.guard") && player.hasPermission("guardpkg.onduty")) {
                    DemoteGuard(player, 2);
                    GuardOffDuty(player);
                    return true;
                }
                if (player.hasPermission("guardpkg.trialguard") && player.hasPermission("guardpkg.onduty")) {
                    DemoteGuard(player, 1);
                    GuardOffDuty(player);
                    return true;
                }
                if (player.hasPermission("guardpkg.guard") && !player.hasPermission("guardpkg.onduty")) {
                    PromoteGuard(player, 2);
                    GuardOnDuty(player);
                    return true;
                }
                PromoteGuard(player, 1);
                GuardOnDuty(player);

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
