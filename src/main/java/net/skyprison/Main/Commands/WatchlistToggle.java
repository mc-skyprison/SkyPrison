package main.java.net.skyprison.Main.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WatchlistToggle implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("skyprisoncore.watchlist.silent")) {//player already has watchlist silenced
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.watchlist.silent false");
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Watchlist now unsilenced. You will receive messages when players on the watchlist join the server.");
            } else {//player does not have watchlist silenced
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.watchlist.silent true");
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Watchlist now silenced. You will not receive messages when players on the watchlist join the server.");
            }
        }
        return true;
    }
}
