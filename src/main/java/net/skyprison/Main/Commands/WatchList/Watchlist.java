package net.skyprison.Main.Commands.WatchList;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class Watchlist implements CommandExecutor {

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length <1 || args.length >2) {//command was not entered in full
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.YELLOW+"Usage: /watchlist <player/page#>...");
            } else {//command has appropriate number of args
                if(isInt(args[0])) {//Displaying page of names
                    int pageNum = Integer.parseInt(args[0]);
                    if(pageNum<1) {//Page requested is less than 1
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"Please use a positive integer...");
                    } else {
                        File f = new File("plugins/SkyPrisonCore/watchlist.yml");
                        YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                        if(yamlf.getConfigurationSection("wlist").getKeys(false).size()>0) {
                            net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
                            int listlength = yamlf.getConfigurationSection("wlist").getKeys(false).size();
                            if (listlength > 0) {
                                int maxPages = (int) Math.round((listlength + 4 )/ 10.0);//number of pages containing names
                                int display = 10;//number of names to display in list
                                int dispNum = ((pageNum - 1) * display) + 1;//starting digit for list
                                int dispLast = (dispNum + display - 1);//last digit to display
                                int count = 0;//increment counter for display
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + "\n" + ChatColor.YELLOW + "------ Page " + pageNum+" ------");
                                for (String key : yamlf.getConfigurationSection("wlist").getKeys(false)) {//Checks if target is already on watchlist
                                    count++;
                                    if (count == dispNum && count <= dispLast) {//display key
                                        player.sendMessage(ChatColor.YELLOW + "" + dispNum + ". " + key);
                                        dispNum++;
                                    } else if (count > dispLast) {//breaks if exceeding names displayed
                                        break;
                                    }
                                }
                                player.sendMessage(ChatColor.YELLOW + "-----Page (" + pageNum + "/" + maxPages + ")-----");
                            } else {
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.YELLOW + "No players on watchlist");
                            }
                        } else {
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"Watchlist is empty...");
                        }
                    }
                } else {//treating second arg as a string
                    File f = new File("plugins/SkyPrisonCore/watchlist.yml");
                    YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                    String target = args[0].toLowerCase();
                    net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
                    if(!yamlf.contains("wlist."+target)) {//target is not on watchlist
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"Player is not on watchlist...");
                    } else {//target is on list, display information
                        String reason = yamlf.getString("wlist."+target+".reason");
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.YELLOW+"\nPlayer: "+ChatColor.GOLD+target+ChatColor.YELLOW+"\nReason: "+ChatColor.GOLD+reason);
                    }
                }
            }
        }
        return true;
    }
}
