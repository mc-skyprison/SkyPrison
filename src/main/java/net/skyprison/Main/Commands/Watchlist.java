package net.skyprison.Main.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class Watchlist implements CommandExecutor {

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    public static void wlistCleanup(File f, YamlConfiguration yamlf, String key) {
        long current = System.currentTimeMillis()/1000L;
        long expire = yamlf.getLong("wlist." + key + ".expires");
        if(current>expire) {//key needs to be removed as watch has expired
            yamlf.getConfigurationSection(key).set("wlist." + key, null);
            try {
                yamlf.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length <1 || args.length >2) {//command was not entered in full
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Usage: /watchlist <player/page#>...");
            } else {//command has appropriate number of args
                if(isInt(args[1])) {//Displaying page of names
                    int pageNum = Integer.parseInt(args[1]);
                    if(pageNum<1) {//Page requested is less than 1
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Please use a positive integer...");
                    } else {
                        File f = new File("plugins/SkyPrisonCore/watchlist.yml");
                        YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                        for (String key : yamlf.getConfigurationSection("wlist").getKeys(false)) {//Checks if target is already on watchlist
                            wlistCleanup(f, yamlf, key);
                        }
                        int listlength = yamlf.getKeys(false).size();
                        int display = 10;//number of names to display in list
                        int dispNum = ((pageNum-1) * display)+1;//starting digit for list
                        int dispLast = (dispNum + display-1);//last digit to display
                        int maxPages = (int) Math.round(listlength / 10.0) * 10;//number of pages containing names
                        int count = 0;//increment counter for display
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"Displaying Page "+pageNum);
                        for (String key : yamlf.getConfigurationSection("wlist").getKeys(false)) {//Checks if target is already on watchlist
                            count++;
                            if(count == dispNum && count<=dispLast) {//display key
                                player.sendMessage(ChatColor.YELLOW+"\n"+dispNum+". "+key);
                            } else if (count > dispLast) {//breaks if exceeding names displayed
                                break;
                            }
                        }
                        player.sendMessage(ChatColor.YELLOW+"\n-----Page ("+pageNum+"/"+maxPages+")-----");
                    }
                } else {//treating second arg as a string
                    File f = new File("plugins/SkyPrisonCore/watchlist.yml");
                    YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                    String target = args[1].toLowerCase();
                    for (String key : yamlf.getConfigurationSection("wlist").getKeys(false)) {//Runs through watchlist removing expired players on watchlist
                        wlistCleanup(f, yamlf, key);
                    }
                    if(!yamlf.contains("wlist."+target)) {//target is not on watchlist
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.RED+"Player is not on watchlist...");
                    } else {
                        String reason = yamlf.getString("wlist."+target+".reason");
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + ": " + ChatColor.YELLOW+"\nPlayer: "+target+"\nReason: "+reason);
                    }
                }
            }
        }
        return true;
    }
}
