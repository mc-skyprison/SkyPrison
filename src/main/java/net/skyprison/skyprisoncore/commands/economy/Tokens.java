package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
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
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Tokens implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public Tokens(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    private final Component prefix = Component.text("Tokens", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY));


    private void sendHelpMessage(Player player) {
        Component helpMsg = Component.text("");
        helpMsg = helpMsg.append(Component.text("━━━━━━━━━━━━━━━━━|", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH)).append(Component.text(" Tokens ", NamedTextColor.AQUA))
                .append(Component.text("|━━━━━━━━━━━━━━━━━━", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));

        helpMsg = helpMsg.append(Component.text("\n/tokens balance (player)", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(Component.text("Check your own or other players token balance", NamedTextColor.GRAY)))

                .append(Component.text("\n/tokens shop", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Opens the token shop", NamedTextColor.GRAY)))

                .append(Component.text("\n/tokens top", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(Component.text("Displays the top token balances", NamedTextColor.GRAY)));


        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
            helpMsg = helpMsg.append(Component.text("\n/tokens add <player> <anount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Adds tokens to the specified player", NamedTextColor.GRAY)))

                    .append(Component.text("\n/tokens remove <player> <anount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Removes tokens from the specified player", NamedTextColor.GRAY)))

                    .append(Component.text("\n/tokens set <player> <anount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Sets the tokens of the specified player to the specified amount", NamedTextColor.GRAY)))

                    .append(Component.text("\n/tokens giveall <amount>", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                            .append(Component.text("Gives tokens of the specified amount to everyone online", NamedTextColor.GRAY)));
        }
        player.sendMessage(helpMsg);
    }


    public void addTokens(UUID pUUID, Integer amount, String type, String source) {
        Player player = Bukkit.getPlayer(pUUID);
        if(player != null && player.isOnline()) {
            int tokens = amount;
            tokens += plugin.tokensData.get(pUUID);
            plugin.tokensData.put(pUUID, tokens);
            player.sendMessage(prefix.append(Component.text(plugin.formatNumber(amount) + " tokens ", NamedTextColor.AQUA).append(Component.text("has been added to your balance", NamedTextColor.GRAY))));
        } else {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = tokens + ? WHERE user_id = ?")) {
                ps.setInt(1, amount);
                ps.setString(2, pUUID.toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + pUUID + ".log");
        FileWriter fData = null;
        try {
            fData = new FileWriter(f, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fData != null) {
            PrintWriter pData = new PrintWriter(fData);

            Long date = System.currentTimeMillis();
            // Date ; remove/receive ; amount ; type ; what bought if tokenshop
            if (type.isEmpty()) {
                pData.println(date + ";receive;" + amount + ";Unknown;null");
            } else {
                if (source.isEmpty()) {
                    pData.println(date + ";receive;" + amount + ";" + type + ";null");
                } else {
                    pData.println(date + ";receive;" + amount + ";" + type + ";" + source);
                }
            }

            pData.flush();
            pData.close();
        }
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
        Inventory shopLogInv = Bukkit.createInventory(null, 54, Component.text("Tokens Log | Page " + page));
        int i = 0;
        for (HashMap.Entry<String, Integer> entry : shopLogPage.entrySet()) {
            if(entry.getValue() == page) {
                ArrayList<Component> lore = new ArrayList<>();
                ItemStack item = new ItemStack(Material.OAK_SIGN);
                ItemMeta meta = item.getItemMeta();
                String[] name = entry.getKey().split(";");
                meta.displayName(Component.text(WordUtils.capitalize(name[1] + " - (" + name[0] + ")"), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Times Used: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(tokenLogUsage.get(entry.getKey())), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                int usagePos = new ArrayList<>(tokenLogUsageSortedTop.keySet()).indexOf(entry.getKey()) + 1;
                lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(usagePos, NamedTextColor.GREEN))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("-----", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Tokens Made: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(tokenLogAmount.get(entry.getKey())), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                int tokensPos = new ArrayList<>(tokenLogAmountSortedTop.keySet()).indexOf(entry.getKey()) + 1;
                lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(tokensPos, NamedTextColor.GREEN))
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
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
        ArrayList<Component> lore = new ArrayList<>();
        ItemStack itemStats = new ItemStack(Material.NETHER_STAR);
        ItemMeta metaStats = itemStats.getItemMeta();
        ItemMeta metaSort = itemSort.getItemMeta();
        ItemMeta itemMeta = pageChange.getItemMeta();
        itemMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        pageChange.setItemMeta(itemMeta);
        for (int b = 45; b < 54; b++) {
            if (b == 47) {
                metaSort.displayName(Component.text("Top Sources Used", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 48) {
                metaSort.displayName(Component.text("Least Sources Used", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 49) {
                metaSort.displayName(Component.text("Player Search", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 50) {
                metaSort.displayName(Component.text("least Tokens Made", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if (b == 51) {
                metaSort.displayName(Component.text("Top Tokens Made", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
                itemSort.setItemMeta(metaSort);
                shopLogInv.setItem(b, itemSort);
            } else if(b == 53) {
                metaStats.displayName(Component.text("Stats", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Total Sources Used: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(totalTokensUsage), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Total Tokens Made: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(totalTokensMade), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                metaStats.lore(lore);
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
                    itemMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false));
                    pageChange.setItemMeta(itemMeta);
                    shopLogInv.setItem(b, pageChange);
                }
            } else {
                if(b == 46) {
                    itemMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false));
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
            Inventory transGUI = Bukkit.createInventory(null, 54, Component.text("Tokens History (Page " + page + "/" + totalPages + ")", NamedTextColor.RED));


            ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta grayMeta = grayPane.getItemMeta();
            grayMeta.displayName(Component.empty());
            grayPane.setItemMeta(grayMeta);

            for (int i = 45; i < 54; i++) {
                switch (i) {
                    case 45 -> {
                        if (page == 1) {
                            transGUI.setItem(i, grayPane);
                        } else {
                            ItemStack prevPage = new ItemStack(Material.PAPER);
                            ItemMeta prevMeta = prevPage.getItemMeta();
                            prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN)
                                    .decoration(TextDecoration.ITALIC, false));
                            prevPage.setItemMeta(prevMeta);
                            transGUI.setItem(i, prevPage);
                        }
                    }
                    case 53 -> {
                        if (totalPages < 2 || page == totalPages) {
                            transGUI.setItem(i, grayPane);
                        } else {
                            ItemStack nextPage = new ItemStack(Material.PAPER);
                            ItemMeta nextMeta = nextPage.getItemMeta();
                            nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN)
                                    .decoration(TextDecoration.ITALIC, false));
                            nextPage.setItemMeta(nextMeta);
                            transGUI.setItem(i, nextPage);
                        }
                    }
                    case 48 -> {
                        ItemStack sortItem = new ItemStack(Material.CLOCK);
                        ItemMeta sortMeta = sortItem.getItemMeta();
                        sortMeta.displayName(Component.text("Sort Transactions", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false));
                        ArrayList<Component> lore = new ArrayList<>();
                        if (sort) {
                            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD).append(Component.text("Oldest -> Newest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                                    .decoration(TextDecoration.ITALIC, false));
                        } else {
                            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD).append(Component.text("Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                        sortMeta.lore(lore);
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
                    }
                    case 50 -> {
                        ItemStack toggleItem = new ItemStack(Material.COMPASS);
                        ItemMeta toggleMeta = toggleItem.getItemMeta();
                        toggleMeta.displayName(Component.text("Toggle Transactions", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false));
                        ArrayList<Component> lore = new ArrayList<>();
                        NamedTextColor notSel = NamedTextColor.GRAY;
                        NamedTextColor isSel = NamedTextColor.GREEN;

                        lore.add(Component.text("All History", toggle == 1 ? isSel : notSel).decoration(TextDecoration.BOLD, toggle == 1).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("TokenShop Purchases", toggle == 2 ? isSel : notSel).decoration(TextDecoration.BOLD, toggle == 2).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Other Removals", toggle == 3 ? isSel : notSel).decoration(TextDecoration.BOLD, toggle == 3).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Tokens from Secrets", toggle == 4 ? isSel : notSel).decoration(TextDecoration.BOLD, toggle == 4).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Tokens from Voting", toggle == 5 ? isSel : notSel).decoration(TextDecoration.BOLD, toggle == 5).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Tokens from Other", toggle == 6 ? isSel : notSel).decoration(TextDecoration.BOLD, toggle == 6).decoration(TextDecoration.ITALIC, false));
                        toggleMeta.lore(lore);
                        toggleItem.setItemMeta(toggleMeta);
                        transGUI.setItem(i, toggleItem);
                    }
                    case 46, 47, 49, 51, 52 -> transGUI.setItem(i, grayPane);
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

                moneyMeta.displayName(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                ArrayList<Component> lore = new ArrayList<>();
                if (val[1].equalsIgnoreCase("remove")) {
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text("Removed Tokens", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                } else {
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text("Received Tokens", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                }
                String type = WordUtils.capitalize(val[3]);
                lore.add(Component.text("From: ", NamedTextColor.GRAY).append(Component.text(type, NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Amount: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(val[2])) + " tokens", NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
                if(val.length == 5) {
                    if (val[3].equalsIgnoreCase("tokenshop")) {
                        lore.add(Component.text("Item Bought: ", NamedTextColor.GRAY).append(Component.text(val[4], NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    } else {
                        String source = WordUtils.capitalize(val[4]);
                        if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            lore.add(Component.text("Source: ", NamedTextColor.GRAY).append(Component.text(source, NamedTextColor.WHITE))
                                    .decoration(TextDecoration.ITALIC, false));
                        }
                    }
                }
                moneyMeta.lore(lore);

                moneyHist.setItemMeta(moneyMeta);
                transGUI.setItem(b, moneyHist);
                iterator.remove();
                b++;
            }

            player.openInventory(transGUI);
        } else {
            player.sendMessage(Component.text("Page doesn't exist! Total pages: " + totalPages, NamedTextColor.RED));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player player) {
            if(args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "help" -> sendHelpMessage(player);
                    case "giveall" -> {
                        if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if (args.length > 1) {
                                if (plugin.isInt(args[1])) {
                                    if (Integer.parseInt(args[1]) >= 0) {
                                        String type = "";
                                        String source = "";

                                        if (args.length > 2) {
                                            type = args[2].replace("_", " ");
                                        }

                                        if (args.length > 3) {
                                            source = args[3].replace("_", " ");
                                        }

                                        for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                                            addTokens(oPlayer.getUniqueId(), Integer.parseInt(args[1]), type, source);
                                        }
                                        player.sendMessage(prefix.append(Component.text("Added ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[1])) + " tokens ",
                                                NamedTextColor.AQUA).append(Component.text("to everyone online", NamedTextColor.GRAY)))));
                                    } else {
                                        player.sendMessage(Component.text("The number must be positive!", NamedTextColor.RED));
                                    }
                                } else {
                                    player.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                }
                            } else {
                                player.sendMessage(Component.text("Incorrect Usage! /tokens giveall <amount>", NamedTextColor.RED));
                            }
                        } else {
                            player.sendMessage(Component.text("Error: ", NamedTextColor.DARK_RED).append(Component.text("You do not have permission to execute this command...", NamedTextColor.RED)));
                        }
                    }
                    case "add" -> {
                        if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if (args.length > 2) {
                                if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if (plugin.isInt(args[2])) {
                                        if (Integer.parseInt(args[2]) >= 0) {
                                            String type = "";
                                            String source = "";

                                            if (args.length > 3) {
                                                type = args[3].replace("_", " ");
                                            }

                                            if (args.length > 4) {
                                                source = args[4].replace("_", " ");
                                            }

                                            addTokens(oPlayer.getUniqueId(), Integer.parseInt(args[2]), type, source);
                                            player.sendMessage(prefix.append(Component.text("Added ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ",
                                                    NamedTextColor.AQUA).append(Component.text("to " + oPlayer.getName(), NamedTextColor.GRAY)))));
                                        } else {
                                            player.sendMessage(Component.text("The number must be positive!", NamedTextColor.RED));
                                        }
                                    } else {
                                        player.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                    }
                                }
                            } else {
                                player.sendMessage(Component.text("Incorrect Usage! /tokens add <player> <amount>", NamedTextColor.RED));
                            }
                        } else {
                            player.sendMessage(Component.text("Error: ", NamedTextColor.DARK_RED).append(Component.text("You do not have permission to execute this command...", NamedTextColor.RED)));
                        }
                    }
                    case "remove" -> {
                        if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if (args.length > 2) {
                                if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if (oPlayer.isOnline()) {
                                        int tokens = plugin.tokensData.get(oPlayer.getUniqueId());
                                        if (tokens != 0) {
                                            if (plugin.isInt(args[2])) {
                                                tokens -= Integer.parseInt(args[2]);
                                                plugin.tokensData.put(oPlayer.getUniqueId(), Math.max(tokens, 0));
                                                player.sendMessage(prefix.append(Component.text("Removed ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ",
                                                                NamedTextColor.AQUA).append(Component.text("from " + oPlayer.getName(), NamedTextColor.GRAY)))));
                                                oPlayer.getPlayer().sendMessage(prefix.append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ", NamedTextColor.AQUA)
                                                        .append(Component.text("was removed from your balance", NamedTextColor.GRAY))));

                                                File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                                FileWriter fData = null;
                                                try {
                                                    fData = new FileWriter(f, true);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                if(fData != null) {
                                                    PrintWriter pData = new PrintWriter(fData);

                                                    Long date = System.currentTimeMillis();
                                                    // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                    if (args.length == 3) {
                                                        pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                                    } else {
                                                        String type = args[3].replace("_", " ");

                                                        if (args.length == 4) {
                                                            pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                        } else {
                                                            String source = args[4].replace("_", " ");
                                                            pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                        }
                                                    }

                                                    pData.flush();
                                                    pData.close();
                                                }
                                            } else {
                                                player.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                            }
                                        } else {
                                            player.sendMessage(Component.text("You can't remove tokens from soneone with 0 tokens!", NamedTextColor.RED));
                                        }
                                    } else {
                                        if (plugin.isInt(args[2])) {
                                            int tokens = 0;
                                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = ?")) {
                                                ps.setString(1, oPlayer.getUniqueId().toString());
                                                ResultSet rs = ps.executeQuery();
                                                if (rs.next()) {
                                                    tokens = rs.getInt(1);
                                                    tokens -= Integer.parseInt(args[2]);
                                                    tokens = Math.max(tokens, 0);
                                                }
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }

                                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                                                ps.setInt(1, tokens);
                                                ps.setString(2, oPlayer.getUniqueId().toString());
                                                ps.executeUpdate();

                                                player.sendMessage(prefix.append(Component.text("Removed ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ",
                                                        NamedTextColor.AQUA).append(Component.text("from " + oPlayer.getName(), NamedTextColor.GRAY)))));

                                                File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                                FileWriter fData = null;
                                                try {
                                                    fData = new FileWriter(f, true);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                if (fData != null) {
                                                    PrintWriter pData = new PrintWriter(fData);
                                                    Long date = System.currentTimeMillis();
                                                    // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                    if (args.length == 3) {
                                                        pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                                    } else {
                                                        String type = args[3].replace("_", " ");

                                                        if (args.length == 4) {
                                                            pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                        } else {
                                                            String source = args[4].replace("_", " ");
                                                            pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                        }
                                                    }
                                                    pData.flush();
                                                    pData.close();
                                                }
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            player.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage(Component.text("Incorrect Usage! /tokens remove <player> <amount>", NamedTextColor.RED));
                            }
                        } else {
                            player.sendMessage(Component.text("Error: ", NamedTextColor.DARK_RED).append(Component.text("You do not have permission to execute this command...", NamedTextColor.RED)));
                        }
                    }
                    case "history" -> { // /token history (player)
                        if (args.length == 1) {
                            openHistoryGUI(player, false, 1, 1, player.getUniqueId().toString());
                        } else if (args.length == 2) {
                            if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                                if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    openHistoryGUI(player, false, 1, 1, oPlayer.getUniqueId().toString());
                                } else {
                                    player.sendMessage(Component.text("No player with that name exists!", NamedTextColor.RED));
                                }
                            } else {
                                player.sendMessage(Component.text("Error: ", NamedTextColor.DARK_RED).append(Component.text("You do not have permission to execute this command...", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(Component.text("Incorrect Usage! /tokens history", NamedTextColor.RED));
                        }
                    }
                    case "check" -> { // /token check (player)
                        if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            LinkedHashMap<String, Integer> tokenLogAmount = new LinkedHashMap<>();
                            LinkedHashMap<String, Integer> tokenLogUsage = new LinkedHashMap<>();
                            LinkedHashMap<String, Integer> tokenLogPage = new LinkedHashMap<>();

                            ArrayList<String> tokenLogs = new ArrayList<>();
                            if (args.length > 1) {
                                if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    try {
                                        FileInputStream fstream = new FileInputStream(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + user.getUniqueId() + ".log");
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
                                        e.printStackTrace();}
                                }
                            } else {
                                File folder = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions");
                                for (File f : Objects.requireNonNull(folder.listFiles())) {
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
                                        e.printStackTrace();
                                    }
                                }
                            }

                            int page = 0;
                            int i = 0;
                            for (String strLine : tokenLogs) {
                                String[] str = strLine.split(";");
                                // Date ; remove/receive ; amount ; type ; source
                                int tokenAmount = Integer.parseInt(str[2]);

                                String source = str[3].toLowerCase() + ";" + str[4].toLowerCase();

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
                            player.sendMessage(Component.text("Error: ", NamedTextColor.DARK_RED).append(Component.text("You do not have permission to execute this command...", NamedTextColor.RED)));
                        }
                    }
                    case "set" -> {
                        if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                            if (args.length > 2) {
                                if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if (plugin.isInt(args[2])) {
                                        if (Integer.parseInt(args[2]) >= 0) {
                                            if (oPlayer.isOnline()) {
                                                plugin.tokensData.put(oPlayer.getUniqueId(), Integer.parseInt(args[2]));
                                                player.sendMessage(prefix.append(Component.text("Set " + oPlayer.getName() + "'s tokens to ", NamedTextColor.GRAY)
                                                        .append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens", NamedTextColor.AQUA))));
                                                oPlayer.getPlayer().sendMessage(prefix.append(Component.text("Your token balance was set to ", NamedTextColor.GRAY)
                                                        .append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens", NamedTextColor.AQUA))));
                                            } else {
                                                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                                                    ps.setInt(1, Integer.parseInt(args[2]));
                                                    ps.setString(2, oPlayer.getUniqueId().toString());
                                                    ps.executeUpdate();
                                                    player.sendMessage(prefix.append(Component.text("Set " + oPlayer.getName() + "'s tokens to ", NamedTextColor.GRAY)
                                                            .append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens", NamedTextColor.AQUA))));
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }

                                            }

                                            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                            FileWriter fData = null;
                                            try {
                                                fData = new FileWriter(f, true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (fData != null) {
                                                PrintWriter pData = new PrintWriter(fData);

                                                Long date = System.currentTimeMillis();
                                                // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                pData.println(date + ";set;" + args[2] + ";admin;null");
                                                pData.flush();
                                                pData.close();
                                            }
                                        } else {
                                            player.sendMessage(Component.text("You can't set the token balance to a negative number!", NamedTextColor.RED));
                                        }
                                    } else {
                                        player.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                    }
                                }
                            } else {
                                player.sendMessage(Component.text("Incorrect Usage! /tokens set <player> <amount>", NamedTextColor.RED));
                            }
                        } else {
                            player.sendMessage(Component.text("Error: ", NamedTextColor.DARK_RED).append(Component.text("You do not have permission to execute this command...", NamedTextColor.RED)));
                        }
                    }
                    case "balance", "bal" -> {
                        if (args.length > 1) {
                            if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (oPlayer.isOnline()) {
                                    player.sendMessage(prefix.append(Component.text(oPlayer.getName() + "'s token balance is ", NamedTextColor.GRAY)
                                            .append(Component.text(plugin.formatNumber(plugin.tokensData.get(oPlayer.getUniqueId())) + " tokens", NamedTextColor.AQUA))));
                                } else {
                                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = ?")) {
                                        ps.setString(1, oPlayer.getUniqueId().toString());
                                        ResultSet rs = ps.executeQuery();
                                        if (rs.next()) {
                                            player.sendMessage(prefix.append(Component.text(oPlayer.getName() + "'s token balance is ", NamedTextColor.GRAY)
                                                    .append(Component.text(plugin.formatNumber(rs.getInt(1)) + " tokens", NamedTextColor.AQUA))));
                                        } else {
                                            player.sendMessage(prefix.append(Component.text(oPlayer.getName() + "'s token balance is ", NamedTextColor.GRAY)
                                                    .append(Component.text("0 tokens", NamedTextColor.AQUA))));
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                player.sendMessage(Component.text("Player does not exist!", NamedTextColor.RED));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Your token balance is ", NamedTextColor.GRAY)
                                    .append(Component.text(plugin.formatNumber(plugin.tokensData.get(player.getUniqueId())) + " tokens", NamedTextColor.AQUA))));
                        }
                    }
                    case "shop" -> { // /token shop (page/edit)
                        if (args.length > 1) {
                            if (plugin.isInt(args[1])) {
                                int page = Integer.parseInt(args[1]);
                                if (page > 3) page = 1;
                                //openShopGUI(player, page);
                            } else if (args[1].equalsIgnoreCase("edit")) {
                                if (player.hasPermission("skyprisoncore.command.tokens.admin")) {
                                    if (args.length > 2) {
                                        if (plugin.isInt(args[2])) {
                                            int page = Integer.parseInt(args[2]);
                                            if (page > 3) page = 1;
                                            //openShopEditGUI(player, page);
                                        } else {
                                            player.sendMessage(Component.text("Incorrect Usage! /token shop edit (page)", NamedTextColor.RED));
                                        }
                                    } else {
                                        //openShopEditGUI(player, 1);
                                    }
                                } else {
                                    player.sendMessage(Component.text("Incorrect Usage! /token shop (page)", NamedTextColor.RED));
                                }
                            } else {
                                player.sendMessage(Component.text("Incorrect Usage! /token shop (page)", NamedTextColor.RED));
                            }
                        } else {
                            //openShopGUI(player, 1);
                        }
                    }
                    case "top" -> player.chat("/lb tokens");
                }
            } else {
                sendHelpMessage(player);
            }
        } else {
            if(args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "giveall" -> {
                        if (args.length > 1) {
                            if (plugin.isInt(args[1])) {
                                if (Integer.parseInt(args[1]) >= 0) {
                                    for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                                        String type = "";
                                        String source = "";

                                        if (args.length > 2) {
                                            type = args[2].replace("_", " ");
                                        }

                                        if (args.length > 3) {
                                            source = args[3].replace("_", " ");
                                        }

                                        addTokens(oPlayer.getUniqueId(), Integer.parseInt(args[1]), type, source);
                                    }
                                    sender.sendMessage(prefix.append(Component.text("Added ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[1])) + " tokens ",
                                            NamedTextColor.AQUA).append(Component.text("to everyone online", NamedTextColor.GRAY)))));       
                                } else {
                                    sender.sendMessage(Component.text("The number must be positive!", NamedTextColor.RED));
                                }
                            } else {
                                sender.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                            }
                        } else {
                            sender.sendMessage(Component.text("Incorrect Usage! /tokens giveall <amount>", NamedTextColor.RED));
                        }
                    }
                    case "add" -> {
                        if (args.length > 2) {
                            if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (plugin.isInt(args[2])) {
                                    if (Integer.parseInt(args[2]) >= 0) {
                                        String type = "";
                                        String source = "";

                                        if (args.length > 3) {
                                            type = args[3].replace("_", " ");
                                        }

                                        if (args.length > 4) {
                                            source = args[4].replace("_", " ");
                                        }

                                        addTokens(oPlayer.getUniqueId(), Integer.parseInt(args[2]), type, source);
                                        sender.sendMessage(prefix.append(Component.text("Added ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ",
                                                NamedTextColor.AQUA).append(Component.text("to " + oPlayer.getName(), NamedTextColor.GRAY)))));
                                    } else {
                                        sender.sendMessage(Component.text("The number must be positive!", NamedTextColor.RED));
                                    }
                                } else {
                                    sender.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                }
                            }
                        } else {
                            sender.sendMessage(Component.text("Incorrect Usage! /tokens add <player> <amount>", NamedTextColor.RED));
                        }
                    }
                    case "remove" -> {
                        if (args.length > 2) {
                            if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (oPlayer.isOnline()) {
                                    int tokens = plugin.tokensData.get(oPlayer.getUniqueId());
                                    if (tokens != 0) {
                                        if (plugin.isInt(args[2])) {
                                            tokens -= Integer.parseInt((args[2]));
                                            plugin.tokensData.put(oPlayer.getUniqueId(), Math.max(tokens, 0));
                                            sender.sendMessage(prefix.append(Component.text("Removed ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ",
                                                    NamedTextColor.AQUA).append(Component.text("from " + oPlayer.getName(), NamedTextColor.GRAY)))));
                                            oPlayer.getPlayer().sendMessage(prefix.append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ", NamedTextColor.AQUA)
                                                    .append(Component.text("was removed from your balance", NamedTextColor.GRAY))));

                                            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                            FileWriter fData = null;
                                            try {
                                                fData = new FileWriter(f, true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if(fData != null) {
                                                PrintWriter pData = new PrintWriter(fData);

                                                Long date = System.currentTimeMillis();
                                                // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                if (args.length == 3) {
                                                    pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                                } else {
                                                    String type = args[3].replace("_", " ");

                                                    if (args.length == 4) {
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                    } else {
                                                        String source = args[4].replace("_", " ");
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                    }
                                                }

                                                pData.flush();
                                                pData.close();
                                            }
                                        } else {
                                            sender.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                        }
                                    } else {
                                        sender.sendMessage(Component.text("You can't remove tokens from soneone with 0 tokens!", NamedTextColor.RED));
                                    }
                                } else {
                                    if (plugin.isInt(args[2])) {
                                        int tokens = 0;
                                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = ?")) {
                                            ps.setString(1, oPlayer.getUniqueId().toString());
                                            ResultSet rs = ps.executeQuery();
                                            if (rs.next()) {
                                                tokens = rs.getInt(1);
                                                tokens -= Integer.parseInt(args[2]);
                                                tokens = Math.max(tokens, 0);
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }


                                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                                            ps.setInt(1, tokens);
                                            ps.setString(2, oPlayer.getUniqueId().toString());
                                            ps.executeUpdate();
                                            sender.sendMessage(prefix.append(Component.text("Removed ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens ",
                                                    NamedTextColor.AQUA).append(Component.text("from " + oPlayer.getName(), NamedTextColor.GRAY)))));

                                            File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                            FileWriter fData = null;
                                            try {
                                                fData = new FileWriter(f, true);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (fData != null) {
                                                PrintWriter pData = new PrintWriter(fData);

                                                Long date = System.currentTimeMillis();
                                                // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                                if (args.length == 3) {
                                                    pData.println(date + ";remove;" + args[2] + ";Other Removals;null");
                                                } else {
                                                    String type = args[3].replace("_", " ");

                                                    if (args.length == 4) {
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";null");
                                                    } else {
                                                        String source = args[4].replace("_", " ");
                                                        pData.println(date + ";remove;" + args[2] + ";" + type + ";" + source);
                                                    }
                                                }

                                                pData.flush();
                                                pData.close();
                                            }
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }


                                    } else {
                                        sender.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                    }
                                }
                            }
                        } else {
                            sender.sendMessage(Component.text("Incorrect Usage! /tokens remove <player> <amount>", NamedTextColor.RED));
                        }
                    }
                    case "set" -> {
                        if (args.length > 2) {
                            if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (plugin.isInt(args[2])) {
                                    if (Integer.parseInt(args[2]) >= 0) {
                                        if (oPlayer.isOnline()) {
                                            plugin.tokensData.put(oPlayer.getUniqueId(), Integer.parseInt(args[2]));
                                            sender.sendMessage(prefix.append(Component.text("Set " + oPlayer.getName() + "'s tokens to ", NamedTextColor.GRAY)
                                                    .append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens", NamedTextColor.AQUA))));
                                            oPlayer.getPlayer().sendMessage(prefix.append(Component.text("Your token balance was set to ", NamedTextColor.GRAY)
                                                    .append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens", NamedTextColor.AQUA))));
                                        } else {
                                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                                                ps.setInt(1, Integer.parseInt(args[2]));
                                                ps.setString(2, oPlayer.getUniqueId().toString());
                                                ps.executeUpdate();
                                                sender.sendMessage(prefix.append(Component.text("Set " + oPlayer.getName() + "'s tokens to ", NamedTextColor.GRAY)
                                                        .append(Component.text(plugin.formatNumber(Integer.parseInt(args[2])) + " tokens", NamedTextColor.AQUA))));
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "token-transactions" + File.separator + oPlayer.getUniqueId() + ".log");
                                        FileWriter fData = null;
                                        try {
                                            fData = new FileWriter(f, true);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (fData != null) {
                                            PrintWriter pData = new PrintWriter(fData);

                                            Long date = System.currentTimeMillis();
                                            // Date ; remove/receive ; amount ; type ; what bought if tokenshop
                                            pData.println(date + ";set;" + args[2] + ";admin;null");
                                            pData.flush();
                                            pData.close();
                                        }
                                    } else {
                                        sender.sendMessage(Component.text("You can't set the token balance to a negative number!"));
                                    }
                                } else {
                                    sender.sendMessage(Component.text("The amount must be a number!", NamedTextColor.RED));
                                }
                            }
                        } else {
                            sender.sendMessage(Component.text("Incorrect Usage! /tokens set <player> <amount>", NamedTextColor.RED));
                        }
                    }
                    case "balance", "bal" -> {
                        if (args.length > 1) {
                            if (CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser oPlayer = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if (oPlayer.isOnline()) {
                                    sender.sendMessage(prefix.append(Component.text(oPlayer.getName() + "'s token balance is ", NamedTextColor.GRAY)
                                            .append(Component.text(plugin.formatNumber(plugin.tokensData.get(oPlayer.getUniqueId())) + " tokens", NamedTextColor.AQUA))));
                                } else {
                                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = ?")) {
                                        ps.setString(1, oPlayer.getUniqueId().toString());
                                        ResultSet rs = ps.executeQuery();
                                        if (rs.next()) {
                                            sender.sendMessage(prefix.append(Component.text(oPlayer.getName() + "'s token balance is ", NamedTextColor.GRAY)
                                                    .append(Component.text(plugin.formatNumber(rs.getInt(1)) + " tokens", NamedTextColor.AQUA))));
                                        } else {
                                            sender.sendMessage(prefix.append(Component.text(oPlayer.getName() + "'s token balance is ", NamedTextColor.GRAY)
                                                    .append(Component.text("0 tokens", NamedTextColor.AQUA))));
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                sender.sendMessage(Component.text("Player does not exist!", NamedTextColor.RED));
                            }
                        } else {
                            sender.sendMessage(Component.text("You must specify a player!", NamedTextColor.RED));
                        }
                    }
                }
            }
        }
        return true;
    }
}

