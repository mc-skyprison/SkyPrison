package net.skyprison.Main.Commands.WatchList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class Watchlist implements CommandExecutor {
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public void openGUI(Player player, int page) {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                .getDataFolder() + "/watchList.yml");
        FileConfiguration suspPlayers = YamlConfiguration.loadConfiguration(f);
        Set<String> watchList = suspPlayers.getKeys(false);
        ArrayList<String> arr = new ArrayList();
        ArrayList totalPages = new ArrayList();
        for(String suspPlayer : watchList) {
            if(suspPlayers.getInt(suspPlayer + ".page") == page) {
                arr.add(suspPlayer);
            }
            totalPages.add(suspPlayers.getInt(suspPlayer + ".page"));
        }
        Inventory watchlistGUI = Bukkit.createInventory(null, 54, ChatColor.RED + "Watchlist");
        int i = 0;
        for (String suspPlayer : arr) {
            ArrayList lore = new ArrayList();
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(suspPlayer)));
            meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Bukkit.getOfflinePlayer(UUID.fromString(suspPlayer)).getName());
            String reason = suspPlayers.getString(suspPlayer + ".reason");
            if(reason.length() > 60) {
                String reason1 = reason.substring(0, 40);
                String reason2 = reason.substring(40);
                lore.add(ChatColor.YELLOW + "Reason: " + ChatColor.GOLD + reason1 + "-");
                lore.add(ChatColor.GOLD + reason2);
            } else {
                lore.add(ChatColor.YELLOW + "Reason: " + ChatColor.GOLD + reason);
            }
            lore.add(ChatColor.YELLOW + "Added By: " + ChatColor.GOLD + Bukkit.getPlayer(UUID.fromString(suspPlayers.getString(suspPlayer + ".added-by"))).getName());
            meta.setLore(lore);
            head.setItemMeta(meta);
            watchlistGUI.setItem(i, head);
            i++;
        }
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack pageChange = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = pageChange.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        pageChange.setItemMeta(itemMeta);
        for (int b = 45; b < 54; b++) {
            if (page == 0) {
                if(totalPages.size() < 1) {
                    watchlistGUI.setItem(b, pane);
                } else {
                    if (Collections.max(totalPages).equals(page)) {
                        watchlistGUI.setItem(b, pane);
                    } else {
                        if(b != 52) {
                            watchlistGUI.setItem(b, pane);
                        } else {
                            watchlistGUI.setItem(b, pageChange);
                        }
                    }
                }
            } else if (Collections.max(totalPages).equals(page)) {
                if(b != 46) {
                    watchlistGUI.setItem(b, pane);
                } else {
                    itemMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                    pageChange.setItemMeta(itemMeta);
                    watchlistGUI.setItem(b, pageChange);
                }
            } else {
                if(b != 46 && b != 52) {
                    watchlistGUI.setItem(b, pane);
                } else if(b == 46) {
                    itemMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                    pageChange.setItemMeta(itemMeta);
                    watchlistGUI.setItem(b, pageChange);
                } else {
                    watchlistGUI.setItem(b, pageChange);
                }
            }
        }
        player.openInventory(watchlistGUI);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
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
            if(args.length < 1 || args.length > 2) {//command was not entered in full
                openGUI(player, 0);
            } else {//command has appropriate number of args
                if(isInt(args[0])) {//Displaying page of names
                    int pageNum = Integer.parseInt(args[0]);
                    if(pageNum<1) {//Page requested is less than 1
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED+"Please use a positive integer...");
                    } else {
                        if(yamlf.getKeys(false).size()>0) {
                            net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
                            int listlength = yamlf.getKeys(false).size();
                            if (listlength > 0) {
                                int maxPages = (int) Math.round((listlength + 4)/10.0);//number of pages containing names
                                int display = 10;//number of names to display in list
                                int dispNum = ((pageNum - 1) * display) + 1;//starting digit for list
                                int dispLast = (dispNum + display - 1);//last digit to display
                                int count = 0;//increment counter for display
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + "\n" + ChatColor.YELLOW + "------ Page " + pageNum+" ------");
                                for (String key : yamlf.getKeys(false)) {//Checks if target is already on watchlist
                                    count++;
                                    if (count == dispNum && count <= dispLast) {//display key
                                        player.sendMessage(ChatColor.YELLOW + "" + dispNum + ". " + Bukkit.getOfflinePlayer(UUID.fromString(key)).getName());
                                        dispNum++;
                                    } else if (count > dispLast) {//breaks if exceeding names displayed
                                        break;
                                    }
                                }
                                player.sendMessage(ChatColor.YELLOW + "-----Page (" + pageNum + "/" + maxPages + ")-----");
                            } else {
                                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.YELLOW + "No players on watchlist");
                            }
                        } else {
                            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.RED+"Watchlist is empty...");
                        }
                    }
                } else {//treating second arg as a string
                    String target = Bukkit.getPlayer(args[0]).getUniqueId().toString();
                    net.skyprison.Main.SkyPrisonMain.wlistCleanup(f, yamlf);
                    if(!yamlf.contains(target)) {//target is not on watchlist
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " + ChatColor.RED + "Player is not on watchlist...");
                    } else {//target is on list, display information
                        String reason = yamlf.getString(target + ".reason");
                        player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "] " +
                                ChatColor.YELLOW + "\nPlayer: " + ChatColor.GOLD + Bukkit.getOfflinePlayer(UUID.fromString(target)).getName() +
                                ChatColor.YELLOW + "\nReason: " + ChatColor.GOLD + reason);
                    }
                }
            }
        }
        return true;
    }
}
