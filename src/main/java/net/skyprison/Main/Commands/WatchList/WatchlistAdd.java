package net.skyprison.Main.Commands.WatchList;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class WatchlistAdd implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length <2) {//command was not entered in full
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW+"Usage: /watchlistadd <player> <reason>");
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
                if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
                    if (!yamlf.contains(target)) {//target is not on watchlist, adding to watchlist
                        String reason = args[1];
                        for (int i = 2; i < args.length; i++) {
                            reason = reason + " " + args[i];
                        }
                        long current = System.currentTimeMillis() / 1000L;
                        long expire = current + (604800);//1 WEEK
                        Set<String> watchList = yamlf.getKeys(false);
                        int page = 0;
                        for (int i = 0; i < watchList.size(); ) {
                            ArrayList arr = new ArrayList();
                            for (String suspPlayer : watchList) {
                                if (yamlf.getInt(suspPlayer + ".page") == i) {
                                    arr.add(suspPlayer);
                                }
                            }
                            if (arr.size() <= 44) {
                                page = i;
                                break;
                            } else {
                                i++;
                                continue;
                            }
                        }
                        yamlf.set(target + ".expire", expire);
                        yamlf.set(target + ".reason", reason);
                        yamlf.set(target + ".page", page);
                        try {
                            yamlf.save(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (yamlf.contains(target)) {//target was successfully added to the watchlist
                            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                if ((online.hasPermission("skyprisoncore.watchlist.basic") && !online.hasPermission("skyprisoncore.watchlist.silent")) || online == player) {
                                    online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW + "Player " + ChatColor.GOLD + CMI.getInstance().getPlayerManager().getUser(args[0]).getName() + ChatColor.YELLOW + " was added for reason \n\"" + ChatColor.GOLD + reason + ChatColor.YELLOW + "\"");
                                }
                            }
                        } else {//Bad plugin, dont do this
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.DARK_RED + "An internal error occurred, please contact an admin...");
                        }
                    } else {//target was on watchlist, informing player
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Player is already on watchlist...");
                    }
                } else {
                    player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW + "Usage: /watchlistadd <player> <reason>");
                }
                net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
            }
        }
        return true;
    }
}