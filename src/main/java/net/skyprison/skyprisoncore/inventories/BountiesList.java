package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BountiesList implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<ItemStack> bounties = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);

    public void updatePage(int page) {
        List<ItemStack> bountiesToShow = new ArrayList<>(bounties);

        int totalPages = (int) Math.ceil((double) bounties.size() / 45);

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
            bountiesToShow = bountiesToShow.subList(toRemove, bountiesToShow.size());
        }
        Iterator<ItemStack> bountyIterator = bountiesToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(bountyIterator.hasNext()) {
                inventory.setItem(i, bountyIterator.next());
            } else break;
        }
    }

    public BountiesList(SkyPrisonCore plugin, DatabaseHook db) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Bounties", TextColor.fromHexString("#e03835")));

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        LinkedHashMap<UUID, Double> unsorted = new LinkedHashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, prize FROM bounties")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                unsorted.put(UUID.fromString(rs.getString(1)), rs.getDouble(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LinkedHashMap<UUID, Double> sortedMap = new LinkedHashMap<>();
        unsorted.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

        for (UUID bountyPlayer : sortedMap.keySet()) {
            ArrayList<Component> lore = new ArrayList<>();
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            head.editMeta(SkullMeta.class, meta -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bountyPlayer);
                String name = offlinePlayer.getName();
                if(name != null) {
                    meta.setOwningPlayer(offlinePlayer);
                } else {
                    name = PlayerManager.getPlayerName(bountyPlayer);
                }
                meta.displayName(Component.text(Objects.requireNonNullElse(name, "Name Not Found.."),
                        NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                lore.addFirst(Component.text("Bounty: ", NamedTextColor.YELLOW).append(Component.text("$" + plugin.formatNumber(sortedMap.get(bountyPlayer)),
                                NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            bounties.add(head);
        }

        for(int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
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
    public int getPage() {
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

