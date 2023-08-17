package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    public void openGUI(Player player, Boolean sort, String toggle, Integer page, String playerId) {
        File f = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + playerId + ".log");
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
        } catch (IOException ignored) {}

        if(!sort) {
            Collections.reverse(trans);
        }

        int totalPages = (int) Math.ceil(trans.size() / 45.0);

        if(totalPages >= page && page > 0) {
            Inventory transGUI = Bukkit.createInventory(null, 54, Component.text("Transaction History (Page " + page + "/" + totalPages + ")", NamedTextColor.RED));


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
                        if (totalPages == 1 || page == totalPages) {
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
                            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD).append(Component.text("Oldest -> Newest", NamedTextColor.YELLOW, TextDecoration.BOLD)
                                    .decoration(TextDecoration.ITALIC, false)));
                        } else {
                            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD).append(Component.text("Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD)
                                    .decoration(TextDecoration.ITALIC, false)));
                        }
                        sortMeta.lore(lore);
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

                        boolean allTrans = toggle.equalsIgnoreCase("null");

                        lore.add(Component.text("All Transactions", allTrans ? isSel : notSel).decoration(TextDecoration.BOLD, allTrans).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("ShopChest Transactions", allTrans ? isSel : notSel).decoration(TextDecoration.BOLD, allTrans).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Payment Transactions", allTrans ? isSel : notSel).decoration(TextDecoration.BOLD, allTrans).decoration(TextDecoration.ITALIC, false));
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

            SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

            for(Iterator<String> iterator = transList.iterator(); iterator.hasNext();) {
                if(b == 45)
                    break;
                ItemStack moneyHist = new ItemStack(Material.OAK_SIGN);
                ItemMeta moneyMeta = moneyHist.getItemMeta();
                String[] val = iterator.next().split(";");

                String name = val[0];

                if(plugin.isLong(val[0])) {
                    Date date = new Date(Long.parseLong(val[0]));
                    name = dateFor.format(date);
                }

                moneyMeta.displayName(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                ArrayList<Component> lore = new ArrayList<>();
                CMIUser oUser = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(val[1]));
                if (val[2].equalsIgnoreCase("withdraw")) {
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text("Sent Money", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("To: ", NamedTextColor.GRAY).append(Component.text(oUser.getName(), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                } else {
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text("Received Money", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("From: ", NamedTextColor.GRAY).append(Component.text(oUser.getName(), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                }
                lore.add(Component.text("Amount: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(Double.parseDouble(val[3])), NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
                if (Boolean.parseBoolean(val[4])) {
                    String[] item = val[5].split(" ");
                    String[] iName = item[0].split("\\{");
                    if (val[2].equalsIgnoreCase("withdraw")) {
                        lore.add(Component.text("Item(s) Bought: ", NamedTextColor.GRAY).append(Component.text(val[6] + " x " + iName[1], NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    } else {
                        lore.add(Component.text("Item(s) Sold: ", NamedTextColor.GRAY).append(Component.text(val[6] + " x " + iName[1], NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }
                moneyMeta.lore(lore);

                if(b == 0) {
                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    moneyMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    moneyMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "transaction-history");


                    NamespacedKey key2 = new NamespacedKey(plugin, "sort");
                    moneyMeta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, sort.toString());

                    NamespacedKey key3 = new NamespacedKey(plugin, "toggle");
                    moneyMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, Objects.requireNonNullElse(toggle, "null"));

                    NamespacedKey key4 = new NamespacedKey(plugin, "page");
                    moneyMeta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);

                    NamespacedKey key5 = new NamespacedKey(plugin, "lookup-user");
                    moneyMeta.getPersistentDataContainer().set(key5, PersistentDataType.STRING, playerId);
                }

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

    public void openLookupGUI(Player player, Boolean sort, String toggle, Integer page, String playerId) {
        File folder = new File(plugin.getDataFolder() + File.separator + "logs" + File.separator + "transactions");
        File[] files = folder.listFiles();
        // Date ; other user ; withdraw/deposit ; amount ; was Quickshop ; what bought/sold if quickshop ; amount
        ArrayList<String> trans = new ArrayList<>();
        if(files != null) {
            for (File file : files) {
                try {
                    BufferedReader read = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = read.readLine()) != null) {
                        String[] lineSplit = line.split(";");
                        if (lineSplit[1].equalsIgnoreCase(playerId)) {
                            if (!toggle.equalsIgnoreCase("null")) {
                                if (Boolean.parseBoolean(toggle)) {
                                    if (Boolean.parseBoolean(lineSplit[4])) {
                                        trans.add(line + ";" + file.getName().split("\\.")[0]);
                                    }
                                } else {
                                    if (!Boolean.parseBoolean(lineSplit[4])) {
                                        trans.add(line + ";" + file.getName().split("\\.")[0]);
                                    }
                                }
                            } else {
                                trans.add(line + ";" + file.getName().split("\\.")[0]);
                            }
                        }
                    }
                    read.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!sort) {
            Collections.reverse(trans);
        }

        int totalPages = (int) Math.ceil(trans.size() / 45.0);

        if(totalPages >= page && page > 0) {
            Inventory transGUI = Bukkit.createInventory(null, 54, Component.text("Transaction History (Page " + page + "/" + totalPages + ")", NamedTextColor.RED));


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
                        if (totalPages == 1 || page == totalPages) {
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

                        boolean allTrans = toggle.equalsIgnoreCase("null");

                        lore.add(Component.text("All Transactions", allTrans ? isSel : notSel).decoration(TextDecoration.BOLD, allTrans).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("ShopChest Transactions", allTrans ? isSel : notSel).decoration(TextDecoration.BOLD, allTrans).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Payment Transactions", allTrans ? isSel : notSel).decoration(TextDecoration.BOLD, allTrans).decoration(TextDecoration.ITALIC, false));
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

            for(Iterator<String> iterator = transList.iterator(); iterator.hasNext();) {
                if(b == 45)
                    break;
                ItemStack moneyHist = new ItemStack(Material.OAK_SIGN);
                ItemMeta moneyMeta = moneyHist.getItemMeta();
                String[] val = iterator.next().split(";");
                moneyMeta.displayName(Component.text(val[0], NamedTextColor.GOLD, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                ArrayList<Component> lore = new ArrayList<>();
                CMIUser oUser = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(val[7]));
                if (val[2].equalsIgnoreCase("withdraw")) {
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text("Received Money", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("From: ", NamedTextColor.GRAY).append(Component.text(oUser.getName(), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                } else {
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text("Sent Money", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("To: ", NamedTextColor.GRAY).append(Component.text(oUser.getName(), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                }

                lore.add(Component.text("Amount: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(Double.parseDouble(val[3])), NamedTextColor.WHITE))
                        .decoration(TextDecoration.ITALIC, false));
                if (Boolean.parseBoolean(val[4])) {
                    String[] item = val[5].split(" ");
                    String[] iName = item[0].split("\\{");
                    if (val[2].equalsIgnoreCase("withdraw")) {
                        lore.add(Component.text("Item(s) Bought: ", NamedTextColor.GRAY).append(Component.text(val[6] + " x " + iName[1], NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    } else {
                        lore.add(Component.text("Item(s) Sold: ", NamedTextColor.GRAY).append(Component.text(val[6] + " x " + iName[1], NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    }
                }
                moneyMeta.lore(lore);

                if(b == 0) {
                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    moneyMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    moneyMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "transaction-history");


                    NamespacedKey key2 = new NamespacedKey(plugin, "sort");
                    moneyMeta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, sort.toString());

                    NamespacedKey key3 = new NamespacedKey(plugin, "toggle");
                    moneyMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, Objects.requireNonNullElse(toggle, "null"));

                    NamespacedKey key4 = new NamespacedKey(plugin, "page");
                    moneyMeta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);

                    NamespacedKey key5 = new NamespacedKey(plugin, "lookup-user");
                    moneyMeta.getPersistentDataContainer().set(key5, PersistentDataType.STRING, playerId);
                }

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



    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player player) {
            if(args.length == 0) {
                openGUI(player, false, "null", 1, player.getUniqueId().toString());
            } else {
                if(args[0].matches("-?\\d+")) {
                    int page = Integer.parseInt(args[0]);
                    openGUI(player, false, "null", page, player.getUniqueId().toString());
                } else {
                    if(player.hasPermission("skyprisoncore.command.moneyhistory.others")) {
                        if(args.length == 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
                                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);
                                openGUI(player, false, "null", 1, user.getUniqueId().toString());
                            } else {
                                player.sendMessage(Component.text("No player with that name exists!", NamedTextColor.RED));
                            }
                        } else {
                            if(args[0].equalsIgnoreCase("lookup")) {
                                if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    openLookupGUI(player, false, "null", 1, user.getUniqueId().toString());
                                } else {
                                    player.sendMessage(Component.text("No player with that name exists!", NamedTextColor.RED));
                                }
                            }
                        }
                    } else {
                        player.sendMessage(Component.text("Incorrect Usage! /moneyhistory (page)", NamedTextColor.RED));
                    }
                }
            }
        }
        return true;
    }
}
