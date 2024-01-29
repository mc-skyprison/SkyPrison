package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Referral implements CustomInventory {
    private final List<ItemStack> referrals = new ArrayList<>();
    private final Inventory inventory;
    private int page = 1;
    private final int totalPages;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack blackPane;
    private boolean sort = true;

    public void updatePage(int page) {
        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }
        for(int i = 0; i < 45; i++) inventory.setItem(i, null);
        inventory.setItem(45, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(53, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        List<ItemStack> refsToShow = new ArrayList<>(referrals);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            refsToShow = refsToShow.subList(toRemove, refsToShow.size());
        }
        Iterator<ItemStack> refsIterator = refsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(refsIterator.hasNext()) {
                inventory.setItem(i, refsIterator.next());
            }
        }
    }
    public void updateSort() {
        this.sort = !this.sort;
        Collections.reverse(referrals);
        ItemStack sortItem = new ItemStack(Material.CLOCK);
        sortItem.editMeta(meta -> {
            meta.displayName(Component.text("Sort Referrals", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Oldest -> Newest" : "Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(49, sortItem);
        updatePage(0);
    }
    public Referral(SkyPrisonCore plugin, DatabaseHook db, UUID targetId) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Referral List", NamedTextColor.RED));

        SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        LinkedHashMap<UUID, List<String>> reffedBy = new LinkedHashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT referred_by, refer_date FROM referrals WHERE user_id = ? ORDER BY refer_Date ASC")) {
            ps.setString(1, targetId.toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                List<String> reffedData = new ArrayList<>();
                UUID refUUID = UUID.fromString(rs.getString(1));
                reffedData.add(PlayerManager.getPlayerName(refUUID));
                Date date = new Date(rs.getLong(2));
                reffedData.add(dateFor.format(date));
                reffedBy.put(UUID.fromString(rs.getString(1)), reffedData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        reffedBy.forEach((uuid, data) -> {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(SkullMeta.class, meta -> {
                meta.displayName(Component.text(data.get(0), NamedTextColor.YELLOW, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Referred you on: " + data.get(1), NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            referrals.add(head);
        });

        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        totalPages = (int) Math.ceil((double) referrals.size() / 45);

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updateSort();
        updatePage(0);
    }

    @Override
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }
    @Override
    public List<Object> customClickList() {
        return null;
    }
    @Override
    public int getPage() {
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

