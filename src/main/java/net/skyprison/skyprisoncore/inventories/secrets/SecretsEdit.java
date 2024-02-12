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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SecretsEdit implements CustomInventory {
    private final Inventory inventory;
    private final int secretsId;
    private final DatabaseHook db;
    private String name = "";
    private byte[] displayItem;
    private String category = "";
    private String type = "secret";
    private String rewardType = "tokens";
    private int rewardAmount = 0;
    private String cooldown = "0d";
    private int maxUses = 0;

    public void updateInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 10) {
                ItemStack item = new ItemStack(Material.OAK_SIGN);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Secret Name", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(MiniMessage.miniMessage().deserialize(this.name)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            }if (i == 11 && secretsId != -1) {
                ItemStack item = new ItemStack(Material.CHERRY_SIGN);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Place Secret", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Click to get a sign that adds a secret location", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
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
            } else if (i == 15) {
                ItemStack item = new ItemStack(Material.CLOCK);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Cooldown", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.cooldown, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 16) {
                ItemStack item = new ItemStack(Material.REPEATER);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Max Uses", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.maxUses, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 19) {
                ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Category", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.category, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 20) {
                ItemStack item = new ItemStack(Material.PAPER);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Type", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.type, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 24) {
                ItemStack item = new ItemStack(Material.BOOK);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Reward Type", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.rewardType, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            } else if (i == 25) {
                ItemStack item = new ItemStack(Material.EMERALD);
                item.editMeta(meta -> {
                    meta.displayName(Component.text("Reward Amount", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.rewardAmount, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                inventory.setItem(i, item);
            }
        }
    }

    public SecretsEdit(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID, int secretsId) {
        this.secretsId = secretsId;
        this.db = db;

        HashMap<Integer, SecretsEdit> edits = new HashMap<>();
        if (plugin.secretsEditing.containsKey(pUUID)) edits = plugin.secretsEditing.get(pUUID);
        edits.put(secretsId, this);
        plugin.secretsEditing.put(pUUID, edits);

        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("Secret " + (secretsId != -1 ? "Editing" : "Creation"), TextColor.fromHexString("#0fc3ff")));
        if (secretsId != -1) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name, display_item, category, type, reward_type, " +
                    "reward, cooldown, max_uses FROM secrets WHERE id = ?")) {
                ps.setInt(1, secretsId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    this.name = rs.getString(1);
                    this.displayItem = rs.getBytes(2);
                    this.category = rs.getString(3);
                    this.type = rs.getString(4);
                    this.rewardType = rs.getString(5);
                    this.rewardAmount = rs.getInt(6);
                    this.cooldown = rs.getString(7);
                    this.maxUses = rs.getInt(8);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            ItemStack placeholder = new ItemStack(Material.LIGHT_GRAY_CONCRETE);
            ItemMeta placeMeta = placeholder.getItemMeta();
            placeMeta.displayName(Component.text("PUT DISPLAY ITEM HERE", NamedTextColor.RED, TextDecoration.BOLD));
            placeholder.setItemMeta(placeMeta);
            this.displayItem = placeholder.serializeAsBytes();
        }
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 35 || i == 27 && secretsId == -1) {
                inventory.setItem(i, redPane);
            } else if (i < 8 || i == 28 || i == 29 || i == 33 || i == 34 || i == 30 && secretsId == -1) {
                inventory.setItem(i, blackPane);
            } else if (i == 27) {
                HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
                ItemStack item = hAPI.getItemHead("10306");
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Back to Secrets", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 30) {
                ItemStack item = new ItemStack(Material.RED_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Delete Secret", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 31) {
                ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(secretsId != -1 ? "Discard Changes" : "Discard Secret", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 32) {
                ItemStack item = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(secretsId != -1 ? "Save Changes" : "Create Secret", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            }
        }

        updateInventory();
    }
    public boolean saveSecret() {
        if(secretsId != -1) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE secrets SET name = ?, display_item = ?, category = ?, type = ?, reward_type = ?, reward = ?, cooldown = ?, max_uses = ? WHERE id = ?")) {
                ps.setString(1, this.name);
                ps.setBytes(2, this.displayItem);
                ps.setString(3, this.category);
                ps.setString(4, this.type);
                ps.setString(5, this.rewardType);
                ps.setInt(6, this.rewardAmount);
                ps.setString(7, this.cooldown);
                ps.setInt(8, this.maxUses);
                ps.setInt(9, this.secretsId);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO secrets (name, display_item, category, type, reward_type, reward, cooldown, max_uses) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, this.name);
                ps.setBytes(2, this.displayItem);
                ps.setString(3, this.category);
                ps.setString(4, this.type);
                ps.setString(5, this.rewardType);
                ps.setInt(6, this.rewardAmount);
                ps.setString(7, this.cooldown);
                ps.setInt(8, this.maxUses);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public int getSecretsId() {
        return this.secretsId;
    }
    public String getName() {
        return this.name;
    }
    public byte[] getDisplayItem() {
        return this.displayItem;
    }
    public String getCategory() {
        return this.category;
    }
    public String getType() {
        return this.type;
    }
    public String getRewardType() {
        return this.rewardType;
    }
    public int getRewardAmount() {
        return this.rewardAmount;
    }
    public String getCooldown() {
        return this.cooldown;
    }
    public int getMaxUses() {
        return this.maxUses;
    }
    public void setName(String name) {
        this.name = name;
        updateInventory();
    }
    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem.serializeAsBytes();
        updateInventory();
    }
    public void setCategory(String category) {
        this.category = category;
        updateInventory();
    }
    public void setType(String type) {
        this.type = type;
        updateInventory();
    }
    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
        updateInventory();
    }
    public void setRewardAmount(int rewardAmount) {
        this.rewardAmount = rewardAmount;
        updateInventory();
    }
    public void setCooldown(String cooldown) {
        this.cooldown = cooldown;
        updateInventory();
    }
    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
        updateInventory();
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

