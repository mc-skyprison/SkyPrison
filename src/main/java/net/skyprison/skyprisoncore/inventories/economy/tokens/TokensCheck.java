package net.skyprison.skyprisoncore.inventories.economy.tokens;

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

public class TokensCheck implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final LinkedHashMap<ItemStack, TokenSource> tokenSources = new LinkedHashMap<>();
    private final List<ItemStack> topUsed = new ArrayList<>();
    private final List<ItemStack> leastUsed = new ArrayList<>();
    private final List<ItemStack> topMade = new ArrayList<>();
    private final List<ItemStack> leastMade = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack sortItem = new ItemStack(Material.BOOK);
    private final List<String> sorts = Arrays.asList("Top Used", "Least Used", "Most Tokens Made", "Least Tokens Made");
    public record TokenSource(int usage, int usagePos, double tokens, int tokenPos) {}
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
    public TokensCheck(SkyPrisonCore plugin, DatabaseHook db, String playerId) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Tokens Log", TextColor.fromHexString("#e03835")));
        record TokenData(String source, String source_data, int usage, int tokens) {}
        LinkedHashMap<List<String>, TokenData> tokenDatas = new LinkedHashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT amount, source, source_data FROM logs_tokens WHERE type = ?" + (playerId != null ? " AND user_id = ? " : ""))) {
            ps.setString(1, "receive");
            if(playerId != null) ps.setString(2, playerId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int tokens = rs.getInt(1);
                String source = rs.getString(2);
                String source_data = rs.getString(3);
                List<String> key = Arrays.asList(source, source_data);
                if(tokenDatas.containsKey(key)) {
                    TokenData tokenData = tokenDatas.get(key);
                    tokenDatas.put(key, new TokenData(source, source_data, tokenData.usage + 1, tokenData.tokens + tokens));
                } else {
                    tokenDatas.put(key, new TokenData(source, source_data, 1, tokens));
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
        List<TokenData> usageSort = new ArrayList<>(tokenDatas.values());
        usageSort.sort(Comparator.comparing(TokenData::usage).reversed());
        List<TokenData> tokenSort = new ArrayList<>(tokenDatas.values());
        tokenSort.sort(Comparator.comparing(TokenData::tokens).reversed());
        tokenDatas.forEach((item, tokenData) -> {
            ItemStack itemStack = new ItemStack(Material.OAK_SIGN);
            int usagePos = usageSort.indexOf(tokenData) + 1;
            int tokenPos = tokenSort.indexOf(tokenData) + 1;
            itemStack.editMeta(meta -> {
                meta.displayName(Component.text(item.getFirst() + " - " + item.getLast(), NamedTextColor.YELLOW, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Times Used: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(tokenData.usage), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(usagePos, NamedTextColor.GREEN))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text( "-----", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Tokens Made: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(tokenData.tokens), NamedTextColor.YELLOW))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(tokenPos, NamedTextColor.GREEN))
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            tokenSources.put(itemStack, new TokenSource(tokenData.usage, usagePos, tokenData.tokens, tokenPos));
        });

        topUsed.addAll(tokenSources.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(TokenSource::usage).reversed()))
                .map(Map.Entry::getKey).toList());
        leastUsed.addAll(tokenSources.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(TokenSource::usage)))
                .map(Map.Entry::getKey).toList());
        topMade.addAll(tokenSources.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(TokenSource::tokens).reversed()))
                .map(Map.Entry::getKey).toList());
        leastMade.addAll(tokenSources.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingDouble(TokenSource::tokens)))
                .map(Map.Entry::getKey).toList());

        ItemStack playerSearch = new ItemStack(Material.PLAYER_HEAD);
        playerSearch.editMeta(meta -> meta.displayName(Component.text("Player Search", NamedTextColor.YELLOW, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        ItemStack statsItem = new ItemStack(Material.NETHER_STAR);
        statsItem.editMeta(meta -> {
            meta.displayName(Component.text("Stats", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            int totalUsed = tokenSources.values().stream().mapToInt(TokenSource::usage).sum();
            double totalTokens = tokenSources.values().stream().mapToDouble(TokenSource::tokens).sum();
            lore.add(Component.text("Total Sources Used: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(totalUsed), NamedTextColor.YELLOW))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Total Tokens Made: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(totalTokens), NamedTextColor.YELLOW))
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
            case "Top Used" -> topUsed;
            case "Least Used" -> leastUsed;
            case "Most Tokens Made" -> topMade;
            case "Least Tokens Made" -> leastMade;
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

