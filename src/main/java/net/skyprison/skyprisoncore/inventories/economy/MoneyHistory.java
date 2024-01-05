package net.skyprison.skyprisoncore.inventories.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

public class MoneyHistory implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<ItemStack> transToDisplay = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack sortItem = new ItemStack(Material.CLOCK);
    private final ItemStack typeItem = new ItemStack(Material.COMPASS);
    private boolean sort = true;
    private final List<String> types = Arrays.asList("All Transactions", "Payments", "Player Shops");
    public record Transaction(ItemStack item, UUID receiver, UUID sender, String type, double amount, Date date, ItemStack itemSold) {}
    private int typePos = 0;
    public void updatePage(int page) {
        List<ItemStack> transToShow = transToDisplay;

        int totalPages = (int) Math.ceil((double) transToShow.size() / 45);

        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }

        for(int i = 0; i < 45; i++) {
            inventory.setItem(i, null);
        }

        inventory.setItem(46, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(52, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            transToShow = transToShow.subList(toRemove, transToShow.size());
        }
        Iterator<ItemStack> itemIterator = transToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                inventory.setItem(i, itemIterator.next());
            } else break;
        }
    }
    public void updateSort() {
        sort = !sort;
        Collections.reverse(transactions);
        sortItem.editMeta(meta -> {
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Oldest -> Newest" : "Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(49, sortItem);
        updatePage(0);
    }
    public void updateType(Boolean direction) {
        if(direction != null) typePos = direction ? (typePos + 1) % transactions.size() : (typePos - 1 + transactions.size()) % transactions.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        typeItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            types.forEach(type -> {
                boolean selected = getType().equalsIgnoreCase(type);
                lore.add(Component.text((selected ? " " : "") + WordUtils.capitalize(type), selected ? selectedColor : color)
                        .decoration(TextDecoration.BOLD, selected).decoration(TextDecoration.ITALIC, false));
            });
            meta.lore(lore);
        });
        inventory.setItem(48, typeItem);
        transToDisplay.clear();
        if(getType().equalsIgnoreCase("All Transactions")) {
            transToDisplay.addAll(transactions.stream().map(Transaction::item).toList());
        } else {
            transToDisplay.addAll(transactions.stream().filter(transaction -> transaction.type().equalsIgnoreCase(getType())).map(Transaction::item).toList());
        }
        updatePage(0);
    }
    public MoneyHistory(SkyPrisonCore plugin, DatabaseHook db, String playerId) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Transaction History", TextColor.fromHexString("#e03835")));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT sender_id, receiver_id, amount, logged_date, item FROM logs_transactions " +
                        "WHERE sender_id = ? OR receiver_id = ? ORDER BY logged_date DESC")) {
            ps.setString(1, playerId);
            ps.setString(2, playerId);
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            while(rs.next()) {
                UUID sender = UUID.fromString(rs.getString("sender_id"));
                UUID receiver = UUID.fromString(rs.getString("receiver_id"));
                String senderName = PlayerManager.getPlayerName(sender);
                String receiverName = PlayerManager.getPlayerName(receiver);
                double amount = rs.getDouble("amount");
                Timestamp date = rs.getTimestamp("logged_date");

                byte[] serializedItem =  rs.getBytes("item");
                ItemStack item = serializedItem != null ? ItemStack.deserializeBytes(serializedItem) : null;

                boolean isSender = sender.toString().equalsIgnoreCase(playerId);
                ItemStack displayItem = new ItemStack(Material.OAK_SIGN);
                displayItem.editMeta(meta -> {
                    meta.displayName(Component.text(dateFor.format(date), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                    ArrayList<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text(isSender ? "Sent Money" : "Received Money", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text((isSender ? "To" : "From") + ": ", NamedTextColor.GRAY)
                            .append(Component.text((isSender ? receiverName : senderName), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Amount: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(amount), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    if (item != null) {
                        lore.add(Component.text("Item" + (item.getAmount() > 1 ? "(s) " : " ") + (isSender ? "Bought" : "Sold") + ": ", NamedTextColor.GRAY)
                                .append(Component.text(item.getType().toString().replace("_", " ") + " x " + item.getAmount(), NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    }
                    meta.lore(lore);
                });
                transactions.add(new Transaction(displayItem, receiver, sender, (item != null ? "payment" : "shop"), amount, date, item));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        sortItem.editMeta(meta -> {
            meta.displayName(Component.text("Sort Transactions", NamedTextColor.GOLD, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Oldest -> Newest" : "Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        typeItem.editMeta(meta -> meta.displayName(Component.text("Toggle Transactions", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));

        for (int i = 45; i < 54; i++) {
            if(i == 48) {
                inventory.setItem(i, sortItem);
            } else if(i == 50) {
                inventory.setItem(i, typeItem);
            } else {
                inventory.setItem(i, blackPane);
            }
        }
        updateType(null);
    }
    public String getType() {
        return types.get(typePos);
    }
    @Override
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }
    @Override
    public List<Object> customClickList() {
        return null;
    }
    public int getPage() {
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
