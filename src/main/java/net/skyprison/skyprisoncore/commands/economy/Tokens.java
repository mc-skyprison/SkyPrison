package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang.WordUtils;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tokens implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public Tokens(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }


    private void sendHelpMessage(Player player) {
        player.sendMessage(plugin.colourMessage("&8m━━━━━━━━━━━━━━━━━|  &bTokens &8&m|━━━━━━━━━━━━━━━━━━"));
        player.sendMessage(plugin.colourMessage("&b/tokens balance (player) &8» &7Check your own or other players token balance"));
        player.sendMessage(plugin.colourMessage("&b/tokens shop &8» &7Opens the token shop"));
        player.sendMessage(plugin.colourMessage("&b/tokens top &8» &7Displays the top token balances"));
        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
            player.sendMessage(plugin.colourMessage("&b/tokens add <player> <anount> &8» &7Adds tokens to the specified player"));
            player.sendMessage(plugin.colourMessage("&b/tokens remove <player> <anount> &8» &7Removes tokens from the specified player"));
            player.sendMessage(plugin.colourMessage("&b/tokens set <player> <anount> &8» &7Sets tokens of the specified amount for the specified player"));
            player.sendMessage(plugin.colourMessage("&b/tokens giveall <amount> &8» &7Gives tokens of the specified amount to everyone online"));
        }
    }


    public void addTokens(CMIUser player, Integer amount, String type, String source) {
        if(player.isOnline()) {
            int tokens = amount;
            tokens += plugin.tokensData.get(player.getUniqueId().toString());
            plugin.tokensData.put(player.getUniqueId().toString(), tokens);
            player.sendMessage(plugin.colourMessage("&bTokens &8» &b" + plugin.formatNumber(amount) + " tokens &7has been added to your balance"));
        } else {
            String sql = "UPDATE users SET tokens = tokens + ? WHERE user_id = ?";
            List<Object> params = new ArrayList<>() {{
                add(amount);
                add(player.getUniqueId().toString());
            }};
            hook.sqlUpdate(sql, params);
        }

        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + player.getUniqueId() + ".log");
        FileWriter fData = null;
        try {
            fData = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pData = new PrintWriter(fData);

        Long date = System.currentTimeMillis();
        // Date ; remove/receive ; amount ; type ; what bought if tokenshop
        if(type.isEmpty()) {
            pData.println(date + ";receive;" + amount + ";Unknown;null");
        } else {
            if(source.isEmpty()) {
                pData.println(date + ";receive;" + amount + ";" + type + ";null");
            } else {
                pData.println(date + ";receive;" + amount + ";" + type + ";" + source);
            }
        }

        pData.flush();
        pData.close();
    }

    public void openCheckGUI(Player player, int page, String sortMethod) {
        // date ; amount ; tokens ;  type ; source
        LinkedHashMap<String, Integer> tokenLogAmount = plugin.tokenLogAmountPlayer.get(player.getUniqueId());
        LinkedHashMap<String, Integer> tokenLogUsage = plugin.tokenLogUsagePlayer.get(player.getUniqueId());
        LinkedHashMap<String, Integer> shopLogPage = plugin.tokenLogPagePlayer.get(player.getUniqueId());

        ArrayList<Integer> tokenLogUsageList = new ArrayList<>();
        ArrayList<Integer> tokenLogAmountList = new ArrayList<>();

        LinkedHashMap<String, Integer> tokenLogAmountSortedTop = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> tokenLogAmountSortedBottom = new LinkedHashMap<>();

        LinkedHashMap<String, Integer> tokenLogUsageSortedTop = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> tokenLogUsageSortedBottom = new LinkedHashMap<>();


        int totalTokensUsage = 0;
        int totalTokensMade = 0;
        for (HashMap.Entry<String, Integer> entry : tokenLogAmount.entrySet()) {
            totalTokensMade += entry.getValue();
            tokenLogAmountList.add(entry.getValue());
        }
        tokenLogAmountList.sort(Collections.reverseOrder());
        for (int num : tokenLogAmountList) {
            for (HashMap.Entry<String, Integer> entry : tokenLogAmount.entrySet()) {
                if (entry.getValue().equals(num)) {
                    tokenLogAmountSortedTop.put(entry.getKey(), num);
                }
            }
        }
        Collections.sort(tokenLogAmountList);
        for (int num : tokenLogAmountList) {
            for (HashMap.Entry<String, Integer> entry : tokenLogAmount.entrySet()) {
                if (entry.getValue().equals(num)) {
                    tokenLogAmountSortedBottom.put(entry.getKey(), num);
                }
            }
        }
        Collections.sort(tokenLogUsageList);
        for (int num : tokenLogUsageList) {
            for (HashMap.Entry<String, Integer> entry : tokenLogUsage.entrySet()) {
                if (entry.getValue().equals(num)) {
                    tokenLogUsageSortedBottom.put(entry.getKey(), num);
                }
            }
        }

        for (HashMap.Entry<String, Integer> entry : tokenLogUsage.entrySet()) {
            totalTokensUsage += tokenLogUsage.get(entry.getKey());
            tokenLogUsageList.add(entry.getValue());
        }
        tokenLogUsageList.sort(Collections.reverseOrder());
        for (int num : tokenLogUsageList) {
            for (HashMap.Entry<String, Integer> entry : tokenLogUsage.entrySet()) {
                if (entry.getValue().equals(num)) {
                    tokenLogUsageSortedTop.put(entry.getKey(), num);
                }
            }
        }
        Collections.sort(tokenLogUsageList);
        for (int num : tokenLogUsageList) {
            for (HashMap.Entry<String, Integer> entry : tokenLogUsage.entrySet()) {
                if (entry.getValue().equals(num)) {
                    tokenLogUsageSortedBottom.put(entry.getKey(), num);
                }
            }
        }
        if(sortMethod.equalsIgnoreCase("amounttop")) {
            int pageNew = 0;
            int i = 0;
            shopLogPage = new LinkedHashMap<>();
            for (HashMap.Entry<String, Integer> entry : tokenLogAmountSortedTop.entrySet()) {
                if(i == 45) {
                    pageNew = 1 + pageNew;
                    i = 0;
                }
                shopLogPage.put(entry.getKey(), pageNew);
                i++;
            }
        } else if(sortMethod.equalsIgnoreCase("amountbottom")) {
            int pageNew = 0;
            int i = 0;
            shopLogPage = new LinkedHashMap<>();
            for (HashMap.Entry<String, Integer> entry : tokenLogAmountSortedBottom.entrySet()) {
                if(i == 45) {
                    pageNew = 1 + pageNew;
                    i = 0;
                }
                shopLogPage.put(entry.getKey(), pageNew);
                i++;
            }
        } else if(sortMethod.equalsIgnoreCase("usagetop")) {
            int pageNew = 0;
            int i = 0;
            shopLogPage = new LinkedHashMap<>();
            for (HashMap.Entry<String, Integer> entry : tokenLogUsageSortedTop.entrySet()) {
                if(i == 45) {
                    pageNew = 1 + pageNew;
                    i = 0;
                }
                shopLogPage.put(entry.getKey(), pageNew);
                i++;
            }
        } else if(sortMethod.equalsIgnoreCase("usagebottom")) {
            int pageNew = 0;
            int i = 0;
            shopLogPage = new LinkedHashMap<>();
            for (HashMap.Entry<String, Integer> entry : tokenLogUsageSortedBottom.entrySet()) {
                if(i == 45) {
                    pageNew = 1 + pageNew;
                    i = 0;
                }
                shopLogPage.put(entry.getKey(), pageNew);
                i++;
            }
        }

        ArrayList<Integer> totalPages = new ArrayList<>();
        Inventory shopLogInv = Bukkit.createInventory(null, 54, ChatColor.RED + "Tokens Log | Page " + page);
        int i = 0;
        for (HashMap.Entry<String, Integer> entry : shopLogPage.entrySet()) {
            if(entry.getValue() == page) {
                ArrayList<String> lore = new ArrayList<>();
                ItemStack item = new ItemStack(Material.OAK_SIGN);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + entry.getKey());
                lore.add(ChatColor.GRAY + "Amount Used: " + ChatColor.YELLOW + plugin.formatNumber(tokenLogAmount.get(entry.getKey())));
                int amountPos = new ArrayList<>(tokenLogAmountSortedTop.keySet()).indexOf(entry.getKey()) + 1;
                lore.add(ChatColor.GRAY + "Position: " + ChatColor.GREEN + amountPos);
                lore.add(ChatColor.DARK_GRAY + "-----");
                lore.add(ChatColor.GRAY + "Tokens Made: " + ChatColor.YELLOW + plugin.formatNumber(tokenLogUsage.get(entry.getKey())));
                int moneyPos = new ArrayList<>(tokenLogUsageSortedTop.keySet()).indexOf(entry.getKey()) + 1;
                lore.add(ChatColor.GRAY + "Position: " + ChatColor.GREEN + moneyPos);
                meta.setLore(lore);
                if(i == 0) {
                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    meta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tokencheck");
                    NamespacedKey key4 = new NamespacedKey(plugin, "page");
                    meta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);
                }
                item.setItemMeta(meta);
                shopLogInv.setItem(i, item);
                i++;
            }
            totalPages.add(entry.getValue());
        }

        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack pageChange = new ItemStack(Material.PAPER);
        ItemStack itemSort = new ItemStack(Material.BOOK);
        ArrayList<String> lore = new ArrayList<>();
        ItemStack itemStats = new ItemStack(Material.NETHER_STAR);
        ItemMeta metaStats = itemStats.getItemMeta();
        ItemMeta metaSort = itemSort.getItemMeta();
        ItemMeta itemMeta = pageChange.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        pageChange.setItemMeta(itemMeta);
        for (int b = 45; b < 54; b++) {
            if (b == 47) {
                metaSort.setDisplayName(ChatColor.GREEN + "Top Sources Used");
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 48) {
                metaSort.setDisplayName(ChatColor.GREEN + "Least Sources Used");
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 49) {
                metaSort.setDisplayName(ChatColor.GREEN + "Player Search");
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 50) {
                metaSort.setDisplayName(ChatColor.GREEN + "least Tokens Made");
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 51) {
                metaSort.setDisplayName(ChatColor.GREEN + "Top Tokens Made");
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if(b == 53) {
                metaStats.setDisplayName(ChatColor.YELLOW + "Stats");
                lore.add(ChatColor.GRAY + "Total Sources Used: " + ChatColor.YELLOW + plugin.formatNumber(totalTokensUsage));
                lore.add(ChatColor.GRAY + "Total Tokens Made: " + ChatColor.YELLOW + plugin.formatNumber(totalTokensMade));
                metaStats.setLore(lore);
                itemStats.setItemMeta(metaStats);
                shopLogInv.setItem(b, itemStats);
            } else {
                shopLogInv.setItem(b, pane);
            }
            if (page == 0) {
                if (!(totalPages.size() < 1) && !Collections.max(totalPages).equals(page) && b == 52) {
                    shopLogInv.setItem(b, pageChange);
                }
            } else if (Collections.max(totalPages).equals(page)) {
                if(b == 46) {
                    itemMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                    pageChange.setItemMeta(itemMeta);
                    shopLogInv.setItem(b, pageChange);
                }
            } else {
                if(b == 46) {
                    itemMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                    pageChange.setItemMeta(itemMeta);
                    shopLogInv.setItem(b, pageChange);
                } else if(b == 52) {
                    shopLogInv.setItem(b, pageChange);
                }
            }
        }
        player.openInventory(shopLogInv);
    }


    public void openHistoryGUI(Player player, Boolean sort, int toggle, Integer page, String playerId) {
        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + playerId + ".log");
        // Date ; remove/receive ; amount ; type ; what bought if tokenshop
        ArrayList<String> trans = new ArrayList<>();
        try {
            BufferedReader read = new BufferedReader(new FileReader(f));
            String line;
            while ((line = read.readLine()) != null) {
                String[] lineSplit = line.split(";");
                if(toggle == 1) {
                    trans.add(line);
                } else if(toggle == 2) {
                    if(lineSplit[3].equalsIgnoreCase("tokenshop")) {
                        trans.add(line);
                    }
                } else if(toggle == 3) {
                    if(lineSplit[3].equalsIgnoreCase("Other Removals")) {
                        trans.add(line);
                    }
                } else if(toggle == 4) {
                    if(lineSplit[3].equalsIgnoreCase("secret")) {
                        trans.add(line);
                    }
                } else if(toggle == 5) {
                    if(lineSplit[3].equalsIgnoreCase("voting")) {
                        trans.add(line);
                    }
                } else if(toggle == 6) {
                    if(!lineSplit[3].equalsIgnoreCase("secret")
                            && !lineSplit[3].equalsIgnoreCase("voting")
                            && !lineSplit[3].equalsIgnoreCase("tokenshop")
                            && !lineSplit[3].equalsIgnoreCase("Other Removals")) {
                        trans.add(line);
                    }
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

        if((totalPages >= page && page > 0) || page == 1) {
            Inventory transGUI = Bukkit.createInventory(null, 54, ChatColor.RED + "Tokens History (Page " + page + "/" + totalPages + ")");


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
                        if (totalPages < 2 || page == totalPages) {
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

                        NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                        sortMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                        NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                        sortMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "token-history");

                        NamespacedKey key2 = new NamespacedKey(plugin, "sort");
                        sortMeta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, sort.toString());

                        NamespacedKey key3 = new NamespacedKey(plugin, "toggle");
                        sortMeta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, toggle);

                        NamespacedKey key4 = new NamespacedKey(plugin, "page");
                        sortMeta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);

                        NamespacedKey key5 = new NamespacedKey(plugin, "lookup-user");
                        sortMeta.getPersistentDataContainer().set(key5, PersistentDataType.STRING, playerId);

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
                        String col4 = "&7";
                        String col5 = "&7";
                        String col6 = "&7";

                        if(toggle == 1) {
                            col1 = "&a&l";
                        } else if(toggle == 2) {
                            col2 = "&a&l";
                        } else if(toggle == 3) {
                            col3 = "&a&l";
                        } else if(toggle == 4) {
                            col4 = "&a&l";
                        } else if(toggle == 5) {
                            col5 = "&a&l";
                        } else if(toggle == 6) {
                            col6 = "&a&l";
                        }
                        lore.add(plugin.colourMessage(col1 + "All History"));
                        lore.add(plugin.colourMessage(col2 + "TokenShop Purchases"));
                        lore.add(plugin.colourMessage(col3 + "Other Removals"));
                        lore.add(plugin.colourMessage(col4 + "Tokens from Secrets"));
                        lore.add(plugin.colourMessage(col5 + "Tokens from Voting"));
                        lore.add(plugin.colourMessage(col6 + "Tokens from Other"));
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
            // Date ; remove/receive ; amount ; type ; what bought if tokenshop
            SimpleDateFormat DateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            for(Iterator<String> iterator = transList.iterator(); iterator.hasNext();) {
                if(b == 45)
                    break;
                ItemStack moneyHist = new ItemStack(Material.OAK_SIGN);
                ItemMeta moneyMeta = moneyHist.getItemMeta();
                String[] val = iterator.next().split(";");

                Date date = new Date(Long.parseLong(val[0]));
                String name = DateFor.format(date);

                moneyMeta.setDisplayName(plugin.colourMessage("&6&l" + name));
                ArrayList<String> lore = new ArrayList<>();
                if (val[1].equalsIgnoreCase("remove")) {
                    lore.add(plugin.colourMessage("&7Type: &fRemoved Tokens"));
                } else {
                    lore.add(plugin.colourMessage("&7Type: &fReceived Tokens"));
                }
                String type = WordUtils.capitalize(val[3]);
                lore.add(plugin.colourMessage("&7From: &f" + type));
                lore.add(plugin.colourMessage("&7Amount: &f" + plugin.formatNumber(Integer.parseInt(val[2])) + " tokens"));
                if(val.length == 5) {
                    if (val[3].equalsIgnoreCase("tokenshop")) {
                        lore.add(plugin.colourMessage("&7Item Bought: &f" + val[4]));
                    } else {
                        String source = WordUtils.capitalize(val[4]);
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            lore.add(plugin.colourMessage("&7Source: &f" + source));
                        }
                    }
                }
                moneyMeta.setLore(lore);

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


    public void openShopGUI(Player player, String bar, Integer page) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        ItemMeta blackMeta = blackPane.getItemMeta();
        redMeta.setDisplayName(" ");
        redPane.setItemMeta(redMeta);
        blackMeta.setDisplayName(" ");
        blackPane.setItemMeta(blackMeta);
        Inventory tokenShopGUI = null;
        switch(page) {
            case 1:
                tokenShopGUI = Bukkit.createInventory(null, 54, ChatColor.RED + "Bartender Shop");
                for (int i = 0; i < 54; i++) {
                    if (i == 0) {
                        NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                        redMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                        NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                        redMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tokenshop-" + page);
                        redPane.setItemMeta(redMeta);
                        tokenShopGUI.setItem(i, redPane);
                    } else if (i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45) {
                        tokenShopGUI.setItem(i, redPane);
                    } else if (i == 49) {
                        ItemStack balance = new ItemStack(Material.NETHER_STAR);
                        ItemMeta bMeta = balance.getItemMeta();
                        bMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance");
                        bMeta.setLore(Collections.singletonList(ChatColor.GRAY + "" + PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%")));
                        balance.setItemMeta(bMeta);
                        tokenShopGUI.setItem(i, balance);
                    } else if (i == 50) {
                        ItemStack balance = new ItemStack(Material.PAPER);
                        ItemMeta bMeta = balance.getItemMeta();
                        bMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Next Page");
                        bMeta.setLore(Collections.singletonList(ChatColor.GRAY + "" + PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%")));
                        balance.setItemMeta(bMeta);
                        tokenShopGUI.setItem(i, balance);
                    } else if (i == 53) {
                        ItemStack balance = new ItemStack(Material.BOOK);
                        ItemMeta bMeta = balance.getItemMeta();
                        bMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance");
                        bMeta.setLore(Collections.singletonList(ChatColor.GRAY + "" + PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%")));
                        balance.setItemMeta(bMeta);
                        tokenShopGUI.setItem(i, balance);
                    } else if(i == 10) {
                        ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 11) {
                        ItemStack item = new ItemStack(Material.IRON_HELMET);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 13) {
                        ItemStack item = new ItemStack(Material.ENDER_CHEST);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 15) {
                        ItemStack item = new ItemStack(Material.IRON_SWORD);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 16) {
                        ItemStack item = new ItemStack(Material.CROSSBOW);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 19) {
                        ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 20) {
                        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 22) {
                        ItemStack item = new ItemStack(Material.SNOWBALL, 16);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 24) {
                        ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 25) {
                        ItemStack item = new ItemStack(Material.BOW);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 28) {
                        ItemStack item = new ItemStack(Material.DIAMOND_LEGGINGS);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 29) {
                        ItemStack item = new ItemStack(Material.IRON_LEGGINGS);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 31) {
                        ItemStack item = new ItemStack(Material.ARROW);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 33) {
                        ItemStack item = new ItemStack(Material.DIAMOND_AXE);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 34) {
                        ItemStack item = new ItemStack(Material.TRIDENT);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 37) {
                        ItemStack item = new ItemStack(Material.DIAMOND_BOOTS);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 38) {
                        ItemStack item = new ItemStack(Material.IRON_BOOTS);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 40) {
                        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE, 64);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 42) {
                        ItemStack item = new ItemStack(Material.DIAMOND_SHOVEL);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else if(i == 43) {
                        ItemStack item = new ItemStack(Material.TURTLE_HELMET);
                        ItemMeta iMeta = item.getItemMeta();
                        tokenShopGUI.setItem(i, item);
                    } else {
                        tokenShopGUI.setItem(i, blackPane);
                    }
                }
                break;
            case 2:

        };


        player.openInventory(tokenShopGUI);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "help":
                        sendHelpMessage(player);
                        break;
                    case "giveall":
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if(args.length > 1) {
                                if (plugin.isInt(args[1])) {
                                    if(Integer.parseInt(args[1]) >= 0) {
                                        String type = "";
                                        String source = "";

                                        if(args.length > 2) {
                                            type = args[2].replace("_", " ");
                                        }

                                        if(args.length > 3) {
                                            source = args[3].replace("_", " ");
                                        }

                                        for(Player oPlayer : Bukkit.getOnlinePlayers()) {
                                            CMIUser user = CMI.getInstance().getPlayerManager().getUser(oPlayer);
                                            addTokens(user, Integer.parseInt(args[1]), type, source);
                                        }
                                        player.sendMessage(plugin.colourMessage("&bTokens &8» &7Added &b" + plugin.formatNumber(Integer.parseInt(args[1])) + " tokens &7to everyone online"));
                                    } else {
                                        player.sendMessage(plugin.colourMessage("&cThe number must be positive!"));
                                    }
                                } else {
                                    player.sendMessage(plugin.colourMessage("&cThe amount must be a number!"));
                                }
                            } else {
                                player.sendMessage(plugin.colourMessage("&cCorrect Usage: /tokens giveall <amount>"));
                            }
                        } else {
                            player.sendMessage(plugin.colourMessage("&4Error:&c You do not have permission to execute this command..."));
                        }
                        break;
                    case "add":
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if(args.length > 2) {
                                if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if (plugin.isInt(args[2])) {
                                        if (Integer.parseInt(args[2]) >= 0) {
                                            String type = "";
                                            String source = "";

                                            if(args.length > 3) {
                                                type = args[3].replace("_", " ");
                                            }

                                            if(args.length > 4) {
                                                source = args[4].replace("_", " ");
                                            }

                                            addTokens(oPlayer, Integer.parseInt(args[2]), type, source);
                                            player.sendMessage(plugin.colourMessage("&bTokens &8» &7Added &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7to " + oPlayer.getName()));
                                        } else {
                                            player.sendMessage(plugin.colourMessage("&cThe number must be positive!"));
                                        }
                                    } else {
                                        player.sendMessage(plugin.colourMessage("&cThe amount must be a number!"));
                                    }
                                }
                            } else {
                                player.sendMessage(plugin.colourMessage("&cCorrect Usage: /tokens add <player> <amount>"));
                            }
                        } else {
                            player.sendMessage(plugin.colourMessage("&4Error:&c You do not have permission to execute this command..."));
                        }
                        break;
                    case "remove":
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if(args.length > 2) {
                                if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if(oPlayer.isOnline()) {
                                        int tokens = plugin.tokensData.get(oPlayer.getUniqueId().toString());
                                        if(tokens != 0) {
                                            if(plugin.isInt(args[2])) {
                                                tokens -= Integer.parseInt(args[2]);
                                                plugin.tokensData.put(oPlayer.getUniqueId().toString(), Math.max(tokens, 0));
                                                player.sendMessage(plugin.colourMessage("&bTokens &8» &7Removed &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7from " + oPlayer.getName()));
                                                oPlayer.sendMessage(plugin.colourMessage("&bTokens &8» &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7was removed from your balance"));

                                                File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                                FileWriter fData = null;
                                                try {
                                                    fData = new FileWriter(f, true);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                PrintWriter pData = new PrintWriter(fData);

                                                Long date = System.currentTimeMillis();
                                                // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                if(args.length == 3) {
                                                    pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                                } else {
                                                    String type = args[3].replace("_", " ");

                                                    if(args.length == 4) {
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                    } else {
                                                        String source = args[4].replace("_", " ");
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                    }
                                                }

                                                pData.flush();
                                                pData.close();
                                            } else {
                                                player.sendMessage(plugin.colourMessage("&cThe amount must be a number!"));
                                            }
                                        } else {
                                            player.sendMessage(plugin.colourMessage("&cYou can't remove tokens from soneone with 0 tokens!"));
                                        }
                                    } else {
                                        if (plugin.isInt(args[2])) {
                                            try {
                                                Connection conn = hook.getSQLConnection();
                                                PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = '" + oPlayer.getUniqueId() + "'");
                                                ResultSet rs = ps.executeQuery();
                                                int tokens = 0;
                                                while(rs.next()) {
                                                    tokens = rs.getInt(1);
                                                }
                                                hook.close(ps, rs, conn);

                                                tokens -= Integer.parseInt(args[2]);
                                                tokens = Math.max(tokens, 0);

                                                String sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
                                                int finalTokens = tokens;
                                                List<Object> params = new ArrayList<>() {{
                                                    add(finalTokens);
                                                    add(oPlayer.getUniqueId().toString());
                                                }};
                                                hook.sqlUpdate(sql, params);
                                                player.sendMessage(plugin.colourMessage("&bTokens &8» &7Removed &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7from " + oPlayer.getName()));

                                                File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                                FileWriter fData = null;
                                                try {
                                                    fData = new FileWriter(f, true);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                PrintWriter pData = new PrintWriter(fData);

                                                Long date = System.currentTimeMillis();
                                                // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                if(args.length == 3) {
                                                    pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                                } else {
                                                    String type = args[3].replace("_", " ");

                                                    if(args.length == 4) {
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                    } else {
                                                        String source = args[4].replace("_", " ");
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                    }
                                                }

                                                pData.flush();
                                                pData.close();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            player.sendMessage(plugin.colourMessage("&cThe amount must be a number!"));
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage(plugin.colourMessage("&cCorrect Usage: /tokens remove <player> <amount>"));
                            }
                        } else {
                            player.sendMessage(plugin.colourMessage("&4Error:&c You do not have permission to execute this command..."));
                        }
                        break;
                    case "history": // /token history (player)
                        if(args.length == 1) {
                            openHistoryGUI(player, false, 1, 1, player.getUniqueId().toString());
                        } else if(args.length == 2) {
                            if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                                if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    openHistoryGUI(player, false, 1, 1, oPlayer.getUniqueId().toString());
                                } else {
                                    player.sendMessage(plugin.colourMessage("&cNo player with that name exists!"));
                                }
                            } else {
                                player.sendMessage(plugin.colourMessage("&4Error:&c You do not have permission to execute this command..."));
                            }
                        } else {
                            player.sendMessage(plugin.colourMessage("&cCorrect Usage: /tokens history"));
                        }
                        break;
                    case "check": // /token check (player)
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            LinkedHashMap<String, Integer> tokenLogAmount = new LinkedHashMap<>();
                            LinkedHashMap<String, Integer> tokenLogUsage = new LinkedHashMap<>();
                            LinkedHashMap<String, Integer> tokenLogPage = new LinkedHashMap<>();

                            ArrayList<String> tokenLogs = new ArrayList<>();
                                if(args.length > 1) {
                                    if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                        CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                        try {
                                            FileInputStream fstream = new FileInputStream(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + user.getUniqueId() + ".log");
                                            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                                            String strLine;
                                            // Date ; remove/receive ; amount ; type ; source
                                            while ((strLine = br.readLine()) != null) {
                                                if(strLine.split(";")[1].equalsIgnoreCase("receive")) {
                                                    tokenLogs.add(strLine);
                                                }
                                            }
                                            fstream.close();
                                        } catch (Exception e) {
                                            System.err.println("Error: " + e.getMessage());
                                        }
                                    }
                                } else {
                                    File folder = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions");
                                    for(File f : Objects.requireNonNull(folder.listFiles())) {
                                        try {
                                            FileInputStream fstream = new FileInputStream(f);
                                            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                                            String strLine;
                                            // Date ; remove/receive ; amount ; type ; source
                                            while ((strLine = br.readLine()) != null) {
                                                if (strLine.split(";")[1].equalsIgnoreCase("receive")) {
                                                    tokenLogs.add(strLine);
                                                }
                                            }
                                            fstream.close();
                                        } catch (Exception e) {
                                            System.err.println("Error: " + e.getMessage());
                                        }
                                    }
                                }

                                int page = 0;
                                int i = 0;
                                for(String strLine : tokenLogs) {
                                    String[] str = strLine.split(";");
                                    // Date ; remove/receive ; amount ; type ; source
                                    int tokenAmount = Integer.parseInt(str[2]);

                                    String source = str[4].toLowerCase();

                                    if (tokenLogAmount.containsKey(source)) {
                                        int newNum = tokenAmount + tokenLogAmount.get(source);
                                        tokenLogAmount.put(source, newNum);
                                    } else {
                                        tokenLogAmount.put(source, tokenAmount);
                                        if (i == 45) {
                                            page = 1 + page;
                                            i = 0;
                                        }
                                        tokenLogPage.put(source, page);
                                        i++;
                                    }

                                    if (tokenLogUsage.containsKey(source)) {
                                        int newNum = 1 + tokenLogUsage.get(source);
                                        tokenLogUsage.put(source, newNum);
                                    } else {
                                        tokenLogUsage.put(source, 1);
                                    }
                                }

                                plugin.tokenLogAmountPlayer.put(player.getUniqueId(), tokenLogAmount);
                                plugin.tokenLogUsagePlayer.put(player.getUniqueId(), tokenLogUsage);
                                plugin.tokenLogPagePlayer.put(player.getUniqueId(), tokenLogPage);
                                openCheckGUI(player, 0, "default");
                        } else {
                            player.sendMessage(plugin.colourMessage("&4Error:&c You do not have permission to execute this command..."));
                        }
                        break;
                    case "set":
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if(args.length > 2) {
                                if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if (plugin.isInt(args[2])) {
                                        if(Integer.parseInt(args[2]) >= 0) {
                                            if(oPlayer.isOnline()) {
                                                plugin.tokensData.put(oPlayer.getUniqueId().toString(), Integer.parseInt(args[2]));
                                                player.sendMessage(plugin.colourMessage("&bTokens &8» &7Set " + oPlayer.getName() + "'s tokens to &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens"));
                                                oPlayer.sendMessage(plugin.colourMessage("&bTokens &8» &7Your token balance was set to &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens"));
                                            } else {
                                                String sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
                                                List<Object> params = new ArrayList<>() {{
                                                    add(Integer.parseInt(args[2]));
                                                    add(oPlayer.getUniqueId().toString());
                                                }};
                                                hook.sqlUpdate(sql, params);
                                                player.sendMessage(plugin.colourMessage("&bTokens &8» &7Set " + oPlayer.getName() + "'s tokens to &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens"));

                                            }

                                            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                            FileWriter fData = null;
                                            try {
                                                fData = new FileWriter(f, true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            PrintWriter pData = new PrintWriter(fData);

                                            Long date = System.currentTimeMillis();
                                            // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                            pData.println(date + ";set;" + args[2] + ";admin;null");
                                            pData.flush();
                                            pData.close();
                                        } else {
                                            player.sendMessage(plugin.colourMessage("&cYou can't set the token balance to a negative number!"));
                                        }
                                    } else {
                                        player.sendMessage(plugin.colourMessage("&cThe amount must be a number!"));
                                    }
                                }
                            } else {
                                player.sendMessage(plugin.colourMessage("&cCorrect Usage: /tokens set <player> <amount>"));
                            }
                        } else {
                            player.sendMessage(plugin.colourMessage("&4Error:&c You do not have permission to execute this command..."));
                        }
                        break;
                    case "balance":
                    case "bal":
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(oPlayer.isOnline()) {
                                    player.sendMessage(plugin.colourMessage("&bTokens &8» &7" + oPlayer.getName() + "'s token balance is &b" + plugin.formatNumber(plugin.tokensData.get(oPlayer.getUniqueId().toString())) + " &7tokens"));
                                } else {
                                    try {
                                        Connection conn = hook.getSQLConnection();
                                        PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = '" + oPlayer.getUniqueId() + "'");
                                        ResultSet rs = ps.executeQuery();
                                        if(rs.next()) {
                                            player.sendMessage(plugin.colourMessage("&bTokens &8» &7" + oPlayer.getName() + "'s token balance is &b" + plugin.formatNumber(rs.getInt(1)) + " &7tokens"));
                                        } else {
                                            player.sendMessage(plugin.colourMessage("&bTokens &8» &7" + oPlayer.getName() + "'s token balance is &b0 &7tokens"));
                                        }
                                        hook.close(ps, rs, conn);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                player.sendMessage(plugin.colourMessage("&cPlayer does not exist!"));
                            }
                        } else {
                            player.sendMessage(plugin.colourMessage("&bTokens &8» &7Your token balance is &b" + plugin.formatNumber(plugin.tokensData.get(player.getUniqueId().toString())) + " &7tokens"));
                        }
                        break;
                    case "shop":
                        Bukkit.dispatchCommand(player, "cp tokenshop");
                        break;
                    case "top":
                        player.chat("/lb tokens");
                        break;
                }
            } else {
                sendHelpMessage(player);
            }
        } else {
            if(args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "giveall":
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                if(Integer.parseInt(args[1]) >= 0) {
                                    for(Player oPlayer : Bukkit.getOnlinePlayers()) {
                                        String type = "";
                                        String source = "";

                                        if(args.length > 2) {
                                            type = args[2].replace("_", " ");
                                        }

                                        if(args.length > 3) {
                                            source = args[3].replace("_", " ");
                                        }

                                        CMIUser user = CMI.getInstance().getPlayerManager().getUser(oPlayer);
                                        addTokens(user, Integer.parseInt(args[1]), type, source);
                                    }
                                    plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7Added &b" + plugin.formatNumber(Integer.parseInt(args[1])) + " tokens &7to everyone online"));
                                } else {
                                    plugin.tellConsole(plugin.colourMessage("&cThe number must be positive!"));
                                }
                            } else {
                                plugin.tellConsole(plugin.colourMessage("&cThe amount must be a number!"));
                            }
                        } else {
                            plugin.tellConsole(plugin.colourMessage("&cCorrect Usage: /tokens giveall <amount>"));
                        }
                        break;
                    case "add":
                        if(args.length > 2) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (plugin.isInt(args[2])) {
                                    if(Integer.parseInt(args[2]) >= 0) {
                                        String type = "";
                                        String source = "";

                                        if(args.length > 3) {
                                            type = args[3].replace("_", " ");
                                        }

                                        if(args.length > 4) {
                                            source = args[4].replace("_", " ");
                                        }

                                        addTokens(oPlayer, Integer.parseInt(args[2]), type, source);
                                        plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7Added &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7to " + oPlayer.getName()));
                                    } else {
                                        plugin.tellConsole(plugin.colourMessage("&cThe number must be positive!"));
                                    }
                                } else {
                                    plugin.tellConsole(plugin.colourMessage("&cThe amount must be a number!"));
                                }
                            }
                        } else {
                            plugin.tellConsole(plugin.colourMessage("&cCorrect Usage: /tokens add <player> <amount>"));
                        }
                        break;
                    case "remove":
                        if(args.length > 2) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(oPlayer.isOnline()) {
                                    int tokens = plugin.tokensData.get(oPlayer.getUniqueId().toString());
                                    if(tokens != 0) {
                                        if(plugin.isInt(args[2])) {
                                            tokens -= Integer.parseInt((args[2]));
                                            plugin.tokensData.put(oPlayer.getUniqueId().toString(), Math.max(tokens, 0));
                                            plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7Removed &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7from " + oPlayer.getName()));
                                            oPlayer.sendMessage(plugin.colourMessage("&bTokens &8» &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7was removed from your balance"));

                                            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                            FileWriter fData = null;
                                            try {
                                                fData = new FileWriter(f, true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            PrintWriter pData = new PrintWriter(fData);

                                            Long date = System.currentTimeMillis();
                                            // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                            if(args.length == 3) {
                                                pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                            } else {
                                                String type = args[3].replace("_", " ");

                                                if(args.length == 4) {
                                                    pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                } else {
                                                    String source = args[4].replace("_", " ");
                                                    pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                }
                                            }

                                            pData.flush();
                                            pData.close();
                                        } else {
                                            plugin.tellConsole(plugin.colourMessage("&cThe amount must be a number!"));
                                        }
                                    } else {
                                        plugin.tellConsole(plugin.colourMessage("&cYou can't remove tokens from soneone with 0 tokens!"));
                                    }
                                } else {
                                    if (plugin.isInt(args[2])) {
                                        try {
                                            Connection conn = hook.getSQLConnection();
                                            PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = '" + oPlayer.getUniqueId() + "'");
                                            ResultSet rs = ps.executeQuery();
                                            int tokens = 0;
                                            while(rs.next()) {
                                                tokens = rs.getInt(1);
                                            }
                                            hook.close(ps, rs, conn);

                                            tokens -= Integer.parseInt(args[2]);
                                            tokens = Math.max(tokens, 0);

                                            String sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
                                            int finalTokens = tokens;
                                            List<Object> params = new ArrayList<>() {{
                                                add(finalTokens);
                                                add(oPlayer.getUniqueId().toString());
                                            }};
                                            hook.sqlUpdate(sql, params);
                                            plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7Removed &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens &7from " + oPlayer.getName()));

                                            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                            FileWriter fData = null;
                                            try {
                                                fData = new FileWriter(f, true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            PrintWriter pData = new PrintWriter(fData);

                                            Long date = System.currentTimeMillis();
                                            // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                            if(args.length == 3) {
                                                pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                            } else {
                                                String type = args[3].replace("_", " ");

                                                if(args.length == 4) {
                                                    pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                } else {
                                                    String source = args[4].replace("_", " ");
                                                    pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                }
                                            }

                                            pData.flush();
                                            pData.close();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        plugin.tellConsole(plugin.colourMessage("&cThe amount must be a number!"));
                                    }
                                }
                            }
                        } else {
                            plugin.tellConsole(plugin.colourMessage("&cCorrect Usage: /tokens remove <player> <amount>"));
                        }
                        break;
                    case "set":
                        if(args.length > 2) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (plugin.isInt(args[2])) {
                                    if(Integer.parseInt(args[2]) >= 0) {
                                        if(oPlayer.isOnline()) {
                                            plugin.tokensData.put(oPlayer.getUniqueId().toString(), Integer.parseInt(args[2]));
                                            plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7Set " + oPlayer.getName() + "'s tokens to &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens"));
                                            oPlayer.sendMessage(plugin.colourMessage("&bTokens &8» &7Your token balance was set to &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens"));
                                        } else {
                                            String sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
                                            List<Object> params = new ArrayList<>() {{
                                                add(Integer.parseInt(args[2]));
                                                add(oPlayer.getUniqueId().toString());
                                            }};
                                            hook.sqlUpdate(sql, params);
                                            oPlayer.sendMessage(plugin.colourMessage("&bTokens &8» &7Set " + oPlayer.getName() + "'s tokens to &b" + plugin.formatNumber(Integer.parseInt(args[2])) + " tokens"));
                                        }

                                        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                        FileWriter fData = null;
                                        try {
                                            fData = new FileWriter(f, true);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        PrintWriter pData = new PrintWriter(fData);

                                        Long date = System.currentTimeMillis();
                                        // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                        pData.println(date + ";set;" + args[2] + ";admin;null");
                                        pData.flush();
                                        pData.close();
                                    } else {
                                        plugin.tellConsole(plugin.colourMessage("&cYou can't set the token balance to a negative number!"));
                                    }
                                } else {
                                    plugin.tellConsole(plugin.colourMessage("&cThe amount must be a number!"));
                                }
                            }
                        } else {
                            plugin.tellConsole(plugin.colourMessage("&cCorrect Usage: /tokens set <player> <amount>"));
                        }
                        break;
                    case "balance":
                    case "bal":
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(oPlayer.isOnline()) {
                                    plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7" + oPlayer.getName() + "'S token balance is &b" + plugin.formatNumber(plugin.tokensData.get(oPlayer.getUniqueId().toString())) + "tokens"));
                                } else {
                                    try {
                                        Connection conn = hook.getSQLConnection();
                                        PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = '" + oPlayer.getUniqueId() + "'");
                                        ResultSet rs = ps.executeQuery();
                                        if(rs.next()) {
                                            plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7" + oPlayer.getName() + "'s token balance is &b" + plugin.formatNumber(rs.getInt(1)) + " &7tokens"));
                                        } else {
                                            plugin.tellConsole(plugin.colourMessage("&bTokens &8» &7" + oPlayer.getName() + "'s token balance is &b0 &7tokens"));
                                        }
                                        hook.close(ps, rs, conn);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                plugin.tellConsole(plugin.colourMessage("&cPlayer does not exist!"));
                            }
                        } else {
                            plugin.tellConsole(plugin.colourMessage("&cYou must specify a player!!"));
                        }
                        break;
                }
            }
        }
        return true;
    }
}

