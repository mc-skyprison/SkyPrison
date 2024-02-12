package net.skyprison.skyprisoncore.inventories.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EconomyCheck implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final LinkedHashMap<ItemStack, SoldItem> soldItems = new LinkedHashMap<>();
    private final List<ItemStack> topSold = new ArrayList<>();
    private final List<ItemStack> leastSold = new ArrayList<>();
    private final List<ItemStack> topMade = new ArrayList<>();
    private final List<ItemStack> leastMade = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack sortItem = new ItemStack(Material.BOOK);
    private final List<String> sorts = Arrays.asList("Top Sold", "Least Sold", "Most Money Made", "Least Money Made");
    public record SoldItem(int amount, int amPos, double price, int priPos) {}
    private int sortPos = 0;
    public void updatePage(int page) {
        List<ItemStack> itemsToShow = getItemList();

        int totalPages = (int) Math.ceil((double) itemsToShow.size() / 45);

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
            itemsToShow = itemsToShow.subList(toRemove, itemsToShow.size());
        }
        Iterator<ItemStack> itemIterator = itemsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                inventory.setItem(i, itemIterator.next());
            } else break;
        }
    }
    public void updateSort(Boolean direction) {
        if(direction != null) sortPos = direction ? (sortPos + 1) % sorts.size() : (sortPos - 1 + sorts.size()) % sorts.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        sortItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            sorts.forEach(sort -> {
                boolean selected = getSort().equalsIgnoreCase(sort);
                lore.add(Component.text((selected ? " " : "") + StringUtils.capitalize(sort), selected ? selectedColor : color)
                        .decoration(TextDecoration.BOLD, selected).decoration(TextDecoration.ITALIC, false));
            });
            meta.lore(lore);
        });
        inventory.setItem(48, sortItem);
        updatePage(0);
    }
    public EconomyCheck(SkyPrisonCore plugin, DatabaseHook db, String playerId) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Shop Log", TextColor.fromHexString("#e03835")));
        record SoldData(String item, int amount, double price) {}
        LinkedHashMap<String, SoldData> soldDatas = new LinkedHashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT item, amount, price FROM logs_shop WHERE transaction_type != ? AND bought_back != ?" + (playerId != null ? " AND user_id = ? " : ""))) {
            ps.setString(1, "BUY");
            ps.setInt(2, 1);
            if(playerId != null) ps.setString(3, playerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String item = rs.getString(1);
                if(soldDatas.containsKey(item)) {
                    SoldData soldItem = soldDatas.get(item);
                    soldDatas.put(item, new SoldData(item, soldItem.amount() + rs.getInt(2), soldItem.price() + rs.getDouble(3)));
                } else {
                    soldDatas.put(item, new SoldData(item, rs.getInt(2), rs.getDouble(3)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        sortItem.editMeta(meta -> meta.displayName(Component.text("Switch Sorting", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        List<SoldData> amountSort = new ArrayList<>(soldDatas.values());
        amountSort.sort(Comparator.comparing(SoldData::amount).reversed());
        List<SoldData> priceSort = new ArrayList<>(soldDatas.values());
        priceSort.sort(Comparator.comparing(SoldData::price).reversed());
        soldDatas.forEach((item, soldData) -> {
            ItemStack itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(item)));
            int amountPos = amountSort.indexOf(soldData) + 1;
            int moneyPos = priceSort.indexOf(soldData) + 1;
            itemStack.editMeta(meta -> {
                meta.displayName(Component.text(item.replace("_", " "), NamedTextColor.YELLOW, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Amount Sold: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(soldData.amount), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(amountPos, NamedTextColor.GREEN))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text( "-----", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Money Made: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(soldData.price), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(moneyPos, NamedTextColor.GREEN))
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            soldItems.put(itemStack, new SoldItem(soldData.amount(), amountPos, soldData.price(), moneyPos));
        });

        topSold.addAll(soldItems.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(SoldItem::amount).reversed()))
                .map(Map.Entry::getKey).toList());
        leastSold.addAll(soldItems.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(SoldItem::amount)))
                .map(Map.Entry::getKey).toList());
        topMade.addAll(soldItems.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(SoldItem::price).reversed()))
                .map(Map.Entry::getKey).toList());
        leastMade.addAll(soldItems.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(SoldItem::price)))
                .map(Map.Entry::getKey).toList());

        ItemStack playerSearch = new ItemStack(Material.PLAYER_HEAD);
        playerSearch.editMeta(meta -> meta.displayName(Component.text("Player Search", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        ItemStack statsItem = new ItemStack(Material.NETHER_STAR);
        statsItem.editMeta(meta -> {
            meta.displayName(Component.text("Stats", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            int totalSold = soldItems.values().stream().mapToInt(SoldItem::amount).sum();
            double totalMoney = soldItems.values().stream().mapToDouble(SoldItem::price).sum();
            lore.add(Component.text("Total Amount Sold: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(totalSold), NamedTextColor.YELLOW))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Total Money Made: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(totalMoney), NamedTextColor.YELLOW))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        for (int i = 45; i < 54; i++) {
            if(i == 48) {
                inventory.setItem(i, sortItem);
            } else if(i == 49) {
                inventory.setItem(i, statsItem);
            } else if(i == 50) {
                inventory.setItem(i, playerSearch);
            } else {
                inventory.setItem(i, blackPane);
            }
        }
        updateSort(null);
    }
    public String getSort() {
        return this.sorts.get(sortPos);
    }
    public List<ItemStack> getItemList() {
        return switch (getSort()) {
            case "Top Sold" -> topSold;
            case "Least Sold" -> leastSold;
            case "Most Money Made" -> topMade;
            case "Least Money Made" -> leastMade;
            default -> new ArrayList<>();
        };
    }
    @Override
    public int page() {
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
