package net.skyprison.skyprisoncore.inventories.economy.tokens;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

public class TokensHistory implements CustomInventory {
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
    private final List<String> types = Arrays.asList("All History", "Token Shop", "Other Removals", "From Secrets", "From Voting", "From Other");
    public record Transaction(ItemStack item, String type, double amount, String source, String sourceData, Date date) {}
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
        Collections.reverse(transToDisplay);
        sortItem.editMeta(meta -> {
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Newest -> Oldest" : "Oldest -> Newest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(48, sortItem);
        updatePage(0);
    }
    public void updateType(Boolean direction) {
        if(direction != null) typePos = direction ? (typePos + 1) % types.size() : (typePos - 1 + types.size()) % types.size();
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
        inventory.setItem(50, typeItem);
        transToDisplay.clear();
        if(getType().equalsIgnoreCase("All History")) {
            transToDisplay.addAll(transactions.stream().map(Transaction::item).toList());
        } else {
            transToDisplay.addAll(transactions.stream().filter(transaction -> {
                switch (getType()) {
                    case "Token Shop" -> {
                        return transaction.source().equalsIgnoreCase("tokenshop");
                    }
                    case "Other Removals" -> {
                        return !transaction.source().equalsIgnoreCase("tokenshop") && transaction.type().equalsIgnoreCase("remove");
                    }
                    case "From Secrets" -> {
                        return transaction.source().equalsIgnoreCase("secret");
                    }
                    case "From Voting" -> {
                        return transaction.source().equalsIgnoreCase("voting");
                    }
                    case "From Other" -> {
                        return  transaction.type().equalsIgnoreCase("receive")
                                && !transaction.source().equalsIgnoreCase("secret")
                                && !transaction.source().equalsIgnoreCase("voting");
                    }
                    default -> {
                        return false;
                    }
                }
            }).map(Transaction::item).toList());
        }
        sort = false;
        updateSort();
    }
    public TokensHistory(SkyPrisonCore plugin, DatabaseHook db, String playerId) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Tokens History", TextColor.fromHexString("#e03835")));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT type, amount, source, source_data, logged_date FROM logs_tokens WHERE user_id = ?")) {
            ps.setString(1, playerId);
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            while(rs.next()) {
                String type = rs.getString("type");
                int amount = rs.getInt("amount");
                String source = rs.getString("source");
                String sourceData = rs.getString("source_data");
                Timestamp date = rs.getTimestamp("logged_date");

                String visualType = switch (type) {
                    case "receive" -> "Received Tokens";
                    case "remove" -> "Removed Tokens";
                    default -> "Set Tokens";
                };

                ItemStack displayItem = new ItemStack(Material.OAK_SIGN);
                displayItem.editMeta(meta -> {
                    meta.displayName(Component.text(dateFor.format(date), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                    ArrayList<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text(visualType, NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("From: ", NamedTextColor.GRAY)
                            .append(Component.text(source, NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Amount: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(amount) + " tokens", NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Source: ", NamedTextColor.GRAY).append(MiniMessage.miniMessage().deserialize(sourceData != null ? sourceData : "None").colorIfAbsent(NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                transactions.add(new Transaction(displayItem, type, amount, source, sourceData, date));
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
                    .append(Component.text(sort ? "Newest -> Oldest" : "Oldest -> Newest", NamedTextColor.YELLOW, TextDecoration.BOLD))
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

