package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextMeta);
        ItemStack prevPage = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        prevPage.setItemMeta(prevMeta);

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
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Create News Message", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
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
                    ItemMeta itemMeta = item.getItemMeta();
                    String title = (String) msgData.get("title");

                    itemMeta.displayName(MiniMessage.miniMessage().deserialize(title).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Click to show News Message", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                    if(canEdit) {
                        lore.add(Component.empty());
                        lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    }

                    NamespacedKey key = new NamespacedKey(plugin, "news-message");
                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, newsMessage);

                    itemMeta.lore(lore);
                    item.setItemMeta(itemMeta);
                    inventory.setItem(i, item);
                }
            }
        }
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
