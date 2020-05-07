package net.skyprison.Main.Commands.WatchList;

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
                String target = args[0].toLowerCase();
                if(yamlf.getConfigurationSection("wlist").getKeys(false).size()>0) {
                    net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
                    if(yamlf.getConfigurationSection("wlist").contains(target)) {//target is on the watchlist
                        yamlf.set("wlist." + target, null);
                        try {
                            yamlf.save(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!yamlf.getConfigurationSection("wlist").contains(target)) {//target is not on the watchlist anymore
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.YELLOW+"Player "+ChatColor.GOLD+target+ChatColor.YELLOW+" was removed from the watchlist...");
                        } else {//bad plugin....
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"An internal error occurred, please contact an admin...");
                        }
                    } else {//target is not on watchlist
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"Player is not on the watchlist...");
                    }
                } else {//watchlist is empty
                    player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"Player is not on the watchlist...");
                }
            }
        }
        return true;
    }
}
