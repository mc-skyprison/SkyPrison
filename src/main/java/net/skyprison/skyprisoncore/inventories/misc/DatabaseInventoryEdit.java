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
import java.util.*;

public class DatabaseInventoryEdit implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final int itemId;
    private byte[] item;
    private String permission = "general";
    private String permissionMessage = "<red>You cant use this!";
    private int priceMoney = 0;
    private int priceTokens = 0;
    private int priceVoucher = 0;
    private String priceVoucherType = "none";
    private String commands = "";
    private int maxUses = 0;
    private String usageLore = "Times Bought:";
    private final int position;
    private final String category;

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

        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 35) {
                inventory.setItem(i, redPane);
            } else if (i < 8 || i == 28 || i == 29 || i == 33 || i == 34 || i == 30 && getItemId() == -1) {
                inventory.setItem(i, blackPane);
            } else if (i == 10) {
                ItemStack item = new ItemStack(Material.DAYLIGHT_DETECTOR);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Item Permission", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));;
                lore.add(Component.text(this.permission, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 11) {
                ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Permission Message", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                lore.add(MiniMessage.miniMessage().deserialize(this.permissionMessage).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 13) {
                ItemStack preview = ItemStack.deserializeBytes(this.item);
                ItemMeta previewMeta = preview.getItemMeta();
                List<Component> lore = Objects.requireNonNullElse(previewMeta.lore(), new ArrayList<>());
                lore.add(Component.empty());
                lore.add(Component.text("CLICK TO GET A COPY", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                previewMeta.lore(lore);
                preview.setItemMeta(previewMeta);
                inventory.setItem(i, preview);
            } else if (i == 15) {
                ItemStack item = new ItemStack(Material.GOLD_NUGGET);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Money Cost", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.priceMoney, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 16) {
                ItemStack item = new ItemStack(Material.EMERALD);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Token Cost", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.priceTokens, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 19) {
                ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Commands", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                if(!this.commands.isEmpty() && !this.commands.isBlank()) {
                    int b = 1;
                    for (String command : this.commands.split("<new_command>")) {
                        lore.add(Component.text(b + ". ", NamedTextColor.GRAY).append(Component.text(command, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                        b++;
                    }
                    itemMeta.lore(lore);
                }
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 20) {
                ItemStack item = new ItemStack(Material.REPEATER);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Max Uses", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.maxUses, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 21) {
                ItemStack item = new ItemStack(Material.COMPARATOR);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Usage Lore", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.maxUses, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 24) {
                ItemStack item = new ItemStack(Material.BOOK);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Voucher Type", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.priceVoucherType, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));

                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 25) {
                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta itemMeta = item.getItemMeta();

                itemMeta.displayName(Component.text("Voucher Cost", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Currently: ", NamedTextColor.YELLOW).append(Component.text(this.priceVoucher, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false));
                itemMeta.lore(lore);
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            }  else if (i == 27) {
                ItemStack item = hAPI.getItemHead("10306");
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Back to " + this.category, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 30) {
                if (itemId != -1) {
                    ItemStack item = new ItemStack(Material.RED_CONCRETE);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.displayName(Component.text("Delete Item", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    item.setItemMeta(itemMeta);
                    inventory.setItem(i, item);
                }
            } else if (i == 31) {
                ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(itemId != -1 ? "Discard Changes" : "Discard Item", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 32) {
                ItemStack item = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(itemId != -1 ? "Save Changes" : "Create Item", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            }
        }
    }


    public DatabaseInventoryEdit(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID, int itemId, int position, String category) {
        this.itemId = itemId;
        this.position = position;
        this.category = category;
        this.plugin = plugin;

        HashMap<Integer, DatabaseInventoryEdit> edits = new HashMap<>();
        if (plugin.itemEditing.containsKey(pUUID)) edits = plugin.itemEditing.get(pUUID);
        edits.put(itemId, this);
        plugin.itemEditing.put(pUUID, edits);

        this.inventory = plugin.getServer().createInventory(this, 36, Component.text("Item Editing", TextColor.fromHexString("#0fc3ff")));
        if (itemId != -1) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT item, permission, permission_message, price_money, price_tokens, " +
                    "price_voucher_type, price_voucher, commands, max_uses, usage_lore FROM gui_items WHERE id = ?")) {
                ps.setInt(1, itemId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    this.item = rs.getBytes(1);
                    this.permission = rs.getString(2);
                    this.permissionMessage = rs.getString(3);
                    this.priceMoney = rs.getInt(4);
                    this.priceTokens = rs.getInt(5);
                    this.priceVoucherType = rs.getString(6);
                    this.priceVoucher = rs.getInt(7);
                    this.commands = rs.getString(8);
                    this.maxUses = rs.getInt(9);
                    this.usageLore = rs.getString(10);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            ItemStack placeholder = new ItemStack(Material.LIGHT_GRAY_CONCRETE);
            ItemMeta placeMeta = placeholder.getItemMeta();
            placeMeta.displayName(Component.text("PUT DISPLAY ITEM HERE", NamedTextColor.RED, TextDecoration.BOLD));
            placeholder.setItemMeta(placeMeta);
            this.item = placeholder.serializeAsBytes();
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
    public int getItemId() {
        return this.itemId;
    }
    public byte[] getItem() {
        return this.item;
    }
    public String getPermission() {
        return this.permission;
    }
    public String getPermissionMessage() {
        return this.permissionMessage;
    }
    public int getPriceMoney() {
        return this.priceMoney;
    }
    public int getPriceTokens() {
        return this.priceTokens;
    }
    public int getPriceVoucher() {
        return this.priceVoucher;
    }
    public String getPriceVoucherType() {
        return this.priceVoucherType;
    }

    public String getCommands() {
        return this.commands;
    }
    public String getCategory() {
        return this.category;
    }
    public int getMaxUses() {
        return this.maxUses;
    }
    public String getUsageLore() {
        return this.usageLore;
    }
    public int getPosition() {
        return this.position;
    }

    public void setItem(ItemStack item) {
        this.item = item.serializeAsBytes();
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

    public void setPriceMoney(int priceMoney) {
        this.priceMoney = priceMoney;
        updateInventory();
    }

    public void setPriceTokens(int priceTokens) {
        this.priceTokens = priceTokens;
        updateInventory();
    }

    public void setPriceVoucher(int priceVoucher) {
        this.priceVoucher = priceVoucher;
        updateInventory();
    }

    public void setPriceVoucherType(String priceVoucherType) {
        this.priceVoucherType = priceVoucherType;
        updateInventory();
    }

    public void setCommands(String command) {
        if(plugin.isInt(command)) {
            int cmdNum = Integer.parseInt(command) - 1;
            List<String> cmds = new ArrayList<>(Arrays.stream(commands.split("<new_command>")).toList());
            if(cmdNum < cmds.size()) {
                cmds.remove(cmdNum);
                this.commands = String.join("<new_command>", cmds);
                updateInventory();
            }
        } else {
            if(!this.commands.isEmpty() && !this.commands.isBlank())
                this.commands += "<new_command>" + command;
            else
                this.commands = command;
            updateInventory();
        }
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
        updateInventory();
    }
    public void setUsageLore(String usageLore) {
        this.usageLore = usageLore;
        updateInventory();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
