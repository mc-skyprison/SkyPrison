package net.skyprison.skyprisoncore.inventories.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VoteHistory implements CustomInventory {
    private final List<ItemStack> votes = new ArrayList<>();
    private final Inventory inventory;
    private int page = 1;
    private final int totalPages;
    private boolean sort = true;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack blackPane;

    public void updateSort() {
        this.sort = !this.sort;
        Collections.reverse(votes);
        ItemStack sortItem = new ItemStack(Material.CLOCK);
        sortItem.editMeta(meta -> {
            meta.displayName(Component.text("Sort Transactions", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Oldest -> Newest" : "Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(49, sortItem);
        updatePage(0);
    }

    public void updatePage(int page) {
        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }
        for(int i = 0; i < 45; i++) inventory.setItem(i, null);
        inventory.setItem(45, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(53, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        List<ItemStack> votesToShow = new ArrayList<>(votes);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            votesToShow = votesToShow.subList(toRemove, votesToShow.size());
        }
        Iterator<ItemStack> votesIterator = votesToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(votesIterator.hasNext()) {
                inventory.setItem(i, votesIterator.next());
            }
        }
    }

    public VoteHistory(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Vote History", NamedTextColor.RED));

        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT time, service, tokens FROM votes WHERE user_id = ? ORDER BY time ASC")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            while (rs.next()) {
                Date date = new Date(rs.getLong(1));
                String service = rs.getString(2);
                int tokens = rs.getInt(3);
                String name = dateFor.format(date);
                ItemStack item = new ItemStack(Material.CHERRY_SIGN);
                item.editMeta(meta -> {
                    meta.displayName(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    ArrayList<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Site: ", NamedTextColor.GRAY).append(Component.text(service, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Tokens: ", NamedTextColor.GRAY).append(Component.text(tokens, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                votes.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        totalPages = (int) Math.ceil((double) votes.size() / 45);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updateSort();
        updatePage(0);
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

