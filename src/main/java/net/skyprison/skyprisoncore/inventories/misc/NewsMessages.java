package net.skyprison.skyprisoncore.inventories.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NewsMessages implements CustomInventory {
    private final Inventory inventory;
    private final int page;
    private final DatabaseHook db;
    private final boolean canEdit;

    public NewsMessages(SkyPrisonCore plugin, DatabaseHook db, boolean canEdit, int page) {
        this.db = db;
        this.page = page;
        this.canEdit = canEdit;

        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("News Messages", TextColor.fromHexString("#0fc3ff")));
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        ItemStack prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        HashMap<Integer, HashMap<String, Object>> messages = new HashMap<>();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, title, permission, last_updated, " +
                "limited_time, limited_start, limited_end FROM news")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> message = new HashMap<>();
                message.put("title", rs.getString(2));
                message.put("permission", rs.getString(3));
                message.put("last_updated", rs.getLong(4));
                message.put("limited_time", rs.getInt(5));
                message.put("limited_start", rs.getLong(6));
                message.put("limited_end", rs.getLong(7));

                messages.put(rs.getInt(1), message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        int totalPages = (int) Math.ceil((double) messages.size() / 28);

        if(page > totalPages) {
            page = 1;
        }

        List<Integer> newsToShow = new ArrayList<>(messages.keySet());

        int toRemove = 28 * (page - 1);
        if(toRemove != 0) {
            newsToShow = newsToShow.subList(toRemove, newsToShow.size());
        }
        Iterator<Integer> newsIterator = newsToShow.iterator();


        for(int i = 0; i < inventory.getSize();i++) {
            if(i == 49 && canEdit) {
                ItemStack item = new ItemStack(Material.LIME_CONCRETE);
                item.editMeta(meta -> meta.displayName(Component.text("Create News Message", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            } else if (i == 47 && page != 1) {
                inventory.setItem(i, prevPage);
            } else if (i == 51 && totalPages > 1 && page != totalPages) {
                inventory.setItem(i, nextPage);
            } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45 || i == 53) {
                inventory.setItem(i, redPane);
            } else if (i < 8 || i > 45 && i < 53) {
                inventory.setItem(i, blackPane);
            } else {
                if (newsIterator.hasNext()) {
                    Integer newsMessage = newsIterator.next();
                    HashMap<String, Object> msgData = messages.get(newsMessage);
                    ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
                    item.editMeta(meta -> {
                        meta.displayName(MiniMessage.miniMessage().deserialize((String) msgData.get("title")).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("Click to show News Message", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                        if(canEdit) {
                            lore.add(Component.empty());
                            lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        }
                        meta.lore(lore);
                        NamespacedKey key = new NamespacedKey(plugin, "news-message");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, newsMessage);
                    });
                    inventory.setItem(i, item);
                }
            }
        }
    }

    @Override
    public int page() {
        return this.page;
    }

    public boolean getCanEdit() {
        return this.canEdit;
    }

    public DatabaseHook getDatabase() {
        return this.db;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
