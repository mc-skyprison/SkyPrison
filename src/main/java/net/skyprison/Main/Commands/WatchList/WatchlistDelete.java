package net.skyprison.Main.Commands.WatchList;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import org.bukkit.Bukkit;
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
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW+"Usage: /watchlistdelete <player>");
            } else {
                File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                        .getDataFolder() + "/watchList.yml");
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                String target = CMI.getInstance().getPlayerManager().getUser(args[0]).getUniqueId().toString();
                if(yamlf.getKeys(false).size()>0) {
                    net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
                    if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
                        if (yamlf.contains(target)) {//target is on the watchlist
                            yamlf.set(target, null);
                            try {
                                yamlf.save(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (!yamlf.contains(target)) {//target is not on the watchlist anymore
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW + "Player " + ChatColor.GOLD + Bukkit.getPlayer(args[0]).getName() + ChatColor.YELLOW + " was removed from the watchlist...");
                            } else {//bad plugin....
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "An internal error occurred, please contact an admin...");
                            }
                        } else {//target is not on watchlist
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Player is not on the watchlist...");
                        }
                    } else {
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW+"Usage: /watchlistdelete <player>");
                    }
                } else {//watchlist is empty
                    player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Player is not on the watchlist...");
                }
            }
        }
        return true;
    }
}
