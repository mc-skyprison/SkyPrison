package net.skyprison.Main.Commands;

import net.skyprison.Main.Commands.Watchlist;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class WatchlistAdd implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length <2) {//command was not entered in full
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Usage: /watchlistadd <player> <reason>...");
            } else {
                File f = new File("plugins/SkyPrisonCore/watchlist.yml");
                YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                String target = args[0].toLowerCase();
                if(!yamlf.contains("wlist."+target)) {//target is not on watchlist, adding to watchlist
                    String reason = args[1];
                    for(int i = 2; i < args.length; i++) {
                        reason = reason +" "+ args[i];
                    }
                    long current = System.currentTimeMillis()/1000L;
                    long expire = current+(172800);//48 hours
                    yamlf.createSection("wlist."+ target);
                    yamlf.set("wlist."+ target+".expire", expire);
                    yamlf.set("wlist."+ target+".reason", reason);
                    try {
                        yamlf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(yamlf.getConfigurationSection("wlist").contains(target)) {//target was successfully added to the watchlist
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Player "+ChatColor.GOLD+target+ChatColor.YELLOW+" was added for reason \""+ChatColor.GOLD+reason+ChatColor.YELLOW+"\"...");
                    } else {//Bad plugin, dont do this
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.RED+"An internal error occurred, please contact an admin...");
                    }
                } else {//target was on watchlist, informing player
                    player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.RED+"Player is already on watchlist...");
                }
                for (String key : yamlf.getConfigurationSection("wlist").getKeys(false)) {//Checks if target is already on watchlist
                    Watchlist.wlistCleanup(f, yamlf, key);
                }
            }
        }
        return true;
    }
}