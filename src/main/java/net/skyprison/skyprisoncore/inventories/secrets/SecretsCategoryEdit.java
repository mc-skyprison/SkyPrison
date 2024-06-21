package net.skyprison.skyprisoncore.inventories.secrets;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import java.util.*;

public class SecretsCategoryEdit implements CustomInventory {
    private final Inventory inventory;
    private final String categoryId;
    private final DatabaseHook db;
    private String name = "";
    private String description = "";
    private byte[] displayItem;
    private String permission = "general";
    private String permissionMessage = "<red>You can't use this!";
    private List<String> regions = new ArrayList<>();
    private int order = 0;

    public void updateInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 10) {
                ItemStack item = new ItemStack(Material.OAK_SIGN);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Category Name", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text(this.name, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 11) {
                ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Description", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    lore.add(MiniMessage.miniMessage().deserialize(this.description).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 13) {
                ItemStack displayItem = ItemStack.deserializeBytes(this.displayItem);
                displayItem.editMeta(meta -> {
                    meta.displayName(Component.text("Display Item", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.empty());
                    lore.add(Component.text("CLICK TO GET A COPY", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, displayItem);
            } else if (i == 16) {
                ItemStack item = new ItemStack(Material.MANGROVE_SIGN);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Regions", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    int b = 1;
                    for (String region : this.regions) {
                        lore.add(Component.text(b + ". " + region, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                        b++;
                    }
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 19) {
                ItemStack item = new ItemStack(Material.REPEATER);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Category Order", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.order, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            }  else if (i == 24) {
                ItemStack item = new ItemStack(Material.DAYLIGHT_DETECTOR);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Permission", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.permission, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 25) {
                ItemStack item = new ItemStack(Material.BOOK);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Permission Message", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                    lore.add(MiniMessage.miniMessage().deserialize(this.permissionMessage).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            }
        }
    }

    public SecretsCategoryEdit(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID, String categoryId) {
        this.categoryId = categoryId;
        this.db = db;
        HashMap<String, SecretsCategoryEdit> edits = new HashMap<>();
        if (plugin.secretsCatEditing.containsKey(pUUID)) edits = plugin.secretsCatEditing.get(pUUID);
        edits.put(categoryId, this);
        plugin.secretsCatEditing.put(pUUID, edits);

        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("Secrets Category " + (categoryId != null ? "Editing" : "Creation"), TextColor.fromHexString("#0fc3ff")));
        if (categoryId != null) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT description, display_item, permission, " +
                    "permission_message, regions, category_order FROM secrets_categories WHERE name = ?")) {
                ps.setString(1, categoryId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    this.name = categoryId;
                    this.description = rs.getString(1);
                    this.displayItem = rs.getBytes(2);
                    this.permission = rs.getString(3);
                    this.permissionMessage = rs.getString(4);
                    this.regions = new ArrayList<>(Arrays.stream(rs.getString(5).split(";")).toList());
                    this.order = rs.getInt(6);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            ItemStack placeholder = new ItemStack(Material.LIGHT_GRAY_CONCRETE);
            placeholder.editMeta(meta -> meta.displayName(Component.text("PUT DISPLAY ITEM HERE", NamedTextColor.RED, TextDecoration.BOLD)));
            this.displayItem = placeholder.serializeAsBytes();
        }
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 35 || i == 27 && categoryId == null) {
                inventory.setItem(i, redPane);
            } else if (i < 8 || i == 28 || i == 29 || i == 33 || i == 34 || i == 30 && categoryId == null) {
                inventory.setItem(i, blackPane);
            } else if (i == 27) {
                HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
                ItemStack item = hAPI.getItemHead("10306");
                item.editMeta(meta -> meta.displayName(Component.text("Back to Secrets", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            } else if (i == 30) {
                ItemStack item = new ItemStack(Material.RED_CONCRETE);
                item.editMeta(meta -> meta.displayName(Component.text("Delete Category", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            } else if (i == 31) {
                ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
                item.editMeta(meta -> meta.displayName(Component.text(categoryId != null ? "Discard Changes" : "Discard Category", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            } else if (i == 32) {
                ItemStack item = new ItemStack(Material.LIME_CONCRETE);
                item.editMeta(meta -> meta.displayName(Component.text(categoryId != null ? "Save Changes" : "Create Category", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            }
        }

        updateInventory();
    }
    public boolean saveCategory() {
        if(this.categoryId != null && !this.categoryId.isEmpty()) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                    "UPDATE secrets_categories SET name = ?, description = ?, display_item = ?, permission = ?, permission_message = ?, regions = ?, category_order = ? WHERE name = ?")) {
                ps.setString(1, this.name);
                ps.setString(2, this.description);
                ps.setBytes(3, this.displayItem);
                ps.setString(4, this.permission);
                ps.setString(5, this.permissionMessage);
                ps.setString(6, String.join(";", this.regions));
                ps.setInt(7, this.order);
                ps.setString(8, this.categoryId);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO secrets_categories (name, description, display_item, permission, permission_message, regions, category_order) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, this.name);
                ps.setString(2, this.description);
                ps.setBytes(3, this.displayItem);
                ps.setString(4, this.permission);
                ps.setString(5, this.permissionMessage);
                ps.setString(6, String.join(";", this.regions));
                ps.setInt(7, this.order);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String getCategoryId() {
        return this.categoryId;
    }
    public String getName() {
        return this.name;
    }
    public String getDescription() {
        return this.description;
    }
    public byte[] getDisplayItem() {
        return this.displayItem;
    }
    public String getPermission() {
        return this.permission;
    }
    public String getPermissionMessage() {
        return this.permissionMessage;
    }
    public int getOrder() {
        return this.order;
    }
    public boolean setName(String name) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name FROM secrets_categories WHERE name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.name = name;
        updateInventory();
        return true;
    }
    public void setDescription(String description) {
        this.description = description;
        updateInventory();
    }
    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem.serializeAsBytes();
        updateInventory();
    }
    public void setPermission(String permission) {
        this.permission = permission;
        updateInventory();
    }
    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        updateInventory();
    }
    public void addRegion(String region) {
        this.regions.add(region);
        updateInventory();
    }
    public boolean removeRegion(int position) {
        position = position - 1;
        if(position < 0 || position > this.regions.size()) return false;
        this.regions.remove(position);
        updateInventory();
        return true;
    }
    public void setOrder(int order) {
        this.order = order;
        updateInventory();
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

