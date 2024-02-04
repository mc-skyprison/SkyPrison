package net.skyprison.skyprisoncore.inventories.misc;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class NewsMessageEdit implements CustomInventory {
    private final Inventory inventory;
    private final int newsMessage;
    private String title = "";
    private String content = "";
    private String hover = "";
    private int priority = 1;
    private String clickType = "";
    private String clickData = "";
    private String permission = "general";
    private int limitedTime = 0;
    private long limitedStart = 0;
    private long limitedEnd = 0;

    public void updateInventory() {
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

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();

        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        for(int i = 0; i < inventory.getSize();i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 35) {
                inventory.setItem(i, redPane);
            } else if (i < 8 || i == 28 || i == 29 || i == 33 || i == 34 || i == 30 && getNewsMessage() == 0) {
                inventory.setItem(i, blackPane);
            }else if (i == 10) {
                ItemStack item = new ItemStack(Material.NAME_TAG);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("News Title", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Preview: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                lore.add(MiniMessage.miniMessage().deserialize(this.title).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 11) {
                ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("News Content", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Preview: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                lore.add(MiniMessage.miniMessage().deserialize(this.content).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 12) {
                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("News Hover", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Preview: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                lore.add(MiniMessage.miniMessage().deserialize(this.hover).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 13) {
                ItemStack item = new ItemStack(Material.DAYLIGHT_DETECTOR);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("News Permission", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text(this.permission, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 14) {
                ItemStack item = new ItemStack(Material.HOPPER);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("News Priority", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.priority, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 15) {
                ItemStack item = new ItemStack(Material.CHAIN_COMMAND_BLOCK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Click Type", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.clickType, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 16) {
                ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Click Data", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text(this.clickData, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 21) {
                ItemStack item = new ItemStack(Material.LIME_CANDLE);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Limited Start", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                date.setTime(this.limitedStart);
                lore.add(Component.text(formatter.format(date), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 22) {
                ItemStack item = new ItemStack(Material.CLOCK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Time Limited", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Time limited message is currently ", NamedTextColor.YELLOW)
                        .append(Component.text(this.limitedTime == 1 ? "ENABLED" : "DISABLED", this.limitedTime == 1 ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 23) {
                ItemStack item = new ItemStack(Material.RED_CANDLE);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Limited End", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                date.setTime(this.limitedEnd);
                lore.add(Component.text(formatter.format(date), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 27) {
                ItemStack item = hAPI.getItemHead("10306");
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Back to News Messages", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 30) {
                if(newsMessage != 0) {
                    ItemStack item = new ItemStack(Material.RED_CONCRETE);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.displayName(Component.text("Delete News Message", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    item.setItemMeta(itemMeta);
                    inventory.setItem(i, item);
                }
            } else if (i == 31) {
                ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(newsMessage != 0 ? "Discard Changes" : "Discard News Message", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 32) {
                ItemStack item = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(newsMessage != 0 ? "Save Changes" : "Create News Message", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            }
        }
    }


    public NewsMessageEdit(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID, int newsMessage) {
        this.newsMessage = newsMessage;

        HashMap<Integer, NewsMessageEdit> edits = new HashMap<>();
        if(plugin.newsEditing.containsKey(pUUID)) edits = plugin.newsEditing.get(pUUID);
        edits.put(newsMessage, this);
        plugin.newsEditing.put(pUUID, edits);

        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("News Message Edit", TextColor.fromHexString("#0fc3ff")));
        if(newsMessage != 0) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT title, content, hover, click_type, click_data, permission, " +
                    "priority, limited_time, limited_start, limited_end FROM news WHERE id = ?")) {
                ps.setInt(1, newsMessage);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    this.title = rs.getString(1);
                    this.content = rs.getString(2);
                    this.hover = rs.getString(3);
                    this.clickType = rs.getString(4);
                    this.clickData = rs.getString(5);
                    this.permission = rs.getString(6);
                    this.priority = rs.getInt(7);
                    this.limitedTime = rs.getInt(8);
                    this.limitedStart = rs.getLong(9);
                    this.limitedEnd = rs.getLong(10);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        updateInventory();
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
        return 1;
    }
    public int getNewsMessage() {
        return this.newsMessage;
    }
    public String getTitle() {
        return this.title;
    }
    public String getContent() {
        return this.content;
    }
    public String getHover() {
        return this.hover;
    }
    public int getPriority() {
        return this.priority;
    }
    public String getClickData() {
        return this.clickData;
    }
    public String getClickType() {
        return this.clickType;
    }
    public String getPermission() {
        return this.permission;
    }
    public int getLimitedTime() {
        return this.limitedTime;
    }
    public long getLimitedStart() {
        return this.limitedStart;
    }
    public long getLimitedEnd() {
        return this.limitedEnd;
    }
    public void setTitle(String title) {
        this.title = title;
        updateInventory();
    }
    public void setContent(String content) {
        this.content = content;
        updateInventory();
    }
    public void setHover(String hover) {
        this.hover = hover;
        updateInventory();
    }
    public void setPriority(int priority) {
        this.priority = priority;
        updateInventory();
    }
    public void setClickData(String clickData) {
        this.clickData = clickData;
        updateInventory();
    }
    public void setClickType(String clickType) {
        this.clickType = clickType;
        updateInventory();
    }
    public void setPermission(String permission) {
        this.permission = permission;
        updateInventory();
    }
    public void setLimitedTime(int limitedTime) {
        this.limitedTime = limitedTime;
        updateInventory();
    }
    public void setLimitedStart(long limitedStart) {
        this.limitedStart = limitedStart;
        updateInventory();
    }
    public void setLimitedEnd(long limitedEnd) {
        this.limitedEnd = limitedEnd;
        updateInventory();
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
