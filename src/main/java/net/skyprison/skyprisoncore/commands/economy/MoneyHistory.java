package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.*;
import java.util.*;

public class MoneyHistory implements CommandExecutor {

    private final SkyPrisonCore plugin;

    public MoneyHistory(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    //
    // Player is a player
    // Boolean sort is true == oldest -> newest and false == newest -> oldest
    // Boolean toggle is null == no sort, true == only shopchest and false == only /pay
    // Integer page is the page, ikr madness
    //
    public void openGUI(Player player, Boolean sort, String toggle, Integer page) {
        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + player.getUniqueId() + ".log");
        // Date ; other user ; withdraw/deposit ; amount ; was Quickshop ; what bought/sold if quickshop ; amount
        ArrayList<String> trans = new ArrayList<>();
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line;
            while ((line = read.readLine()) != null) {
                if(!toggle.equalsIgnoreCase("null")) {
                    String[] lineSplit = line.split(";");
                    if(Boolean.parseBoolean(toggle)) {
                        if(Boolean.parseBoolean(lineSplit[4])) {
                            trans.add(line);
                        }
                    } else {
                        if(!Boolean.parseBoolean(lineSplit[4])) {
                            trans.add(line);
                        }
                    }
                } else {
                    trans.add(line);
                }
            }
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!sort) {
            Collections.reverse(trans);
        }

        int totalPages = (int) Math.ceil(trans.size() / 45.0);

        if(totalPages >= page && page > 0) {
            Inventory transGUI = Bukkit.createInventory(null, 54, ChatColor.RED + "Transaction History (Page " + page + "/" + totalPages + ")");


            ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta grayMeta = grayPane.getItemMeta();
            grayMeta.setDisplayName(" ");
            grayPane.setItemMeta(grayMeta);

            for (int i = 45; i < 54; i++) {
                switch (i) {
                    case 45:
                        if (page == 1) {
                            transGUI.setItem(i, grayPane);
                        } else {
                            ItemStack prevPage = new ItemStack(Material.PAPER);
                            ItemMeta prevMeta = prevPage.getItemMeta();
                            prevMeta.setDisplayName(plugin.colourMessage("&aPrevious Page"));
                            prevPage.setItemMeta(prevMeta);
                            transGUI.setItem(i, prevPage);
                        }
                        break;
                    case 53:
                        if (totalPages == 1 || page == totalPages) {
                            transGUI.setItem(i, grayPane);
                        } else {
                            ItemStack nextPage = new ItemStack(Material.PAPER);
                            ItemMeta nextMeta = nextPage.getItemMeta();
                            nextMeta.setDisplayName(plugin.colourMessage("&aNext Page"));
                            nextPage.setItemMeta(nextMeta);
                            transGUI.setItem(i, nextPage);
                        }
                        break;
                    case 48:
                        ItemStack sortItem = new ItemStack(Material.CLOCK);
                        ItemMeta sortMeta = sortItem.getItemMeta();
                        sortMeta.setDisplayName(plugin.colourMessage("&6Sort Transactions"));
                        ArrayList<String> lore = new ArrayList<>();
                        if(sort) {
                            lore.add(plugin.colourMessage("&6Current Sort: &e&lOldest -> Newest"));
                        } else {
                            lore.add(plugin.colourMessage("&6Current Sort: &e&lNewest -> Oldest"));
                        }
                        sortMeta.setLore(lore);
                        sortItem.setItemMeta(sortMeta);
                        transGUI.setItem(i, sortItem);
                        break;
                    case 50:
                        ItemStack toggleItem = new ItemStack(Material.COMPASS);
                        ItemMeta toggleMeta = toggleItem.getItemMeta();
                        toggleMeta.setDisplayName(plugin.colourMessage("&6Toggle Transactions"));
                        lore = new ArrayList<>();
                        String col1 = "&7";
                        String col2 = "&7";
                        String col3 = "&7";

                        if(toggle.equalsIgnoreCase("null")) {
                            col1 = "&a&l";
                        } else if(toggle.equalsIgnoreCase("true")) {
                            col2 = "&a&l";
                        } else {
                            col3 = "&a&l";
                        }
                        lore.add(plugin.colourMessage(col1 + "All Transactions"));
                        lore.add(plugin.colourMessage(col2 + "ShopChest Transactions"));
                        lore.add(plugin.colourMessage(col3 + "Payment Transactions"));
                        toggleMeta.setLore(lore);
                        toggleItem.setItemMeta(toggleMeta);
                        transGUI.setItem(i, toggleItem);
                        break;
                    case 46:
                    case 47:
                    case 49:
                    case 51:
                    case 52:
                        transGUI.setItem(i, grayPane);
                        break;

                }
            }

            int b = 0;

            List<String> transList = trans.subList(0, trans.size());
            if(page != 1)
                transList = trans.subList(((page-1) * 45), trans.size());

            for(Iterator<String> iterator = transList.iterator(); iterator.hasNext();) {
                if(b == 45)
                    break;
                ItemStack moneyHist = new ItemStack(Material.OAK_SIGN);
                ItemMeta moneyMeta = moneyHist.getItemMeta();
                String[] val = iterator.next().split(";");
                moneyMeta.setDisplayName(plugin.colourMessage("&6&l" + val[0]));
                ArrayList<String> lore = new ArrayList<>();
                CMIUser oUser = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(val[1]));
                if (val[2].equalsIgnoreCase("withdraw")) {
                    lore.add(plugin.colourMessage("&7Type: &fSent Money"));
                    lore.add(plugin.colourMessage("&7To: &f" + oUser.getName()));
                } else {
                    lore.add(plugin.colourMessage("&7Type: &fReceived Money"));
                    lore.add(plugin.colourMessage("&7From: &f" + oUser.getName()));
                }
                lore.add(plugin.colourMessage("&7Amount: &f$" + plugin.formatNumber(Double.parseDouble(val[3]))));
                if (Boolean.parseBoolean(val[4])) {
                    String[] item = val[5].split(" ");
                    String[] iName = item[0].split("\\{");
                    if (val[2].equalsIgnoreCase("withdraw")) {
                        lore.add(plugin.colourMessage("&7Item(s) Bought: &f" + val[6] + " x " + iName[1]));
                    } else {
                        lore.add(plugin.colourMessage("&7Item(s) Sold: &f" + val[6] + " x " + iName[1]));
                    }
                }
                moneyMeta.setLore(lore);

                if(b == 0) {
                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    moneyMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    moneyMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "transaction-history");


                    NamespacedKey key2 = new NamespacedKey(plugin, "sort");
                    moneyMeta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, sort.toString());

                    NamespacedKey key3 = new NamespacedKey(plugin, "toggle");
                    if(toggle == null) {
                        moneyMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, "null");
                    } else {
                        moneyMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, toggle.toString());
                    }

                    NamespacedKey key4 = new NamespacedKey(plugin, "page");
                    moneyMeta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);
                }

                moneyHist.setItemMeta(moneyMeta);
                transGUI.setItem(b, moneyHist);
                iterator.remove();
                b++;
            }

            player.openInventory(transGUI);
        } else {
            player.sendMessage(plugin.colourMessage("&cPage doesn't exist! Total pages: " + totalPages));
        }
    }



    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                openGUI(player, false, "null", 1);
            } else if (args.length == 1) {
                if(args[0].matches("-?\\d+")) {
                    int page = Integer.parseInt(args[0]);
                    openGUI(player, false, "null", page);
                } else {
                    player.sendMessage(plugin.colourMessage("&cCorrect Usage: /moneyhistory (page)"));
                }
            } else {
                player.sendMessage(plugin.colourMessage("&cCorrect Usage: /moneyhistory (page)"));
            }
        }
        return true;
    }
}
