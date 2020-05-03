package net.skyprison.Main.Commands;

import net.skyprison.Main.Commands.Watchlist;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class WatchlistDelete implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length <1) {//command was not entered in full
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Usage: /watchlistdelete <player> ...");
            } else {
                File f = new File("plugins/SkyPrisonCore/watchlist.yml");
                YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                String target = args[1].toLowerCase();
                for (String key : yamlf.getConfigurationSection("wlist").getKeys(false)) {//Checks if target is already on watchlist
                    Watchlist.wlistCleanup(f, yamlf, key);
                }
                if(yamlf.getConfigurationSection("wlist").contains(target)) {//target is on the watchlist
                    yamlf.getConfigurationSection("wlist").set("wlist." + target, null);
                    try {
                        yamlf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(!yamlf.getConfigurationSection("wlist").contains(target)) {//target is not on the watchlist anymore
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Player "+ChatColor.GOLD+target+ChatColor.YELLOW+" was removed from the watchlist...");
                    } else {//bad plugin....
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.RED+"An internal error occurred, please contact an admin...");
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.RED+"Player is not on the watchlist...");
                }

            }
        }
        return true;
    }
}
