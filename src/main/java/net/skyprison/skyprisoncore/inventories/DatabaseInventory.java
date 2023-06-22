package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import java.util.Objects;

public class DatabaseInventory implements CustomInventory{
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final boolean canEdit;
    private final HashMap<Integer, HashMap<String, Object>> items;
    private final String category;

    public void updateInventory(Player player) {
        for (Integer position : items.keySet()) {
            HashMap<String, Object> itemData = items.get(position);
            boolean hasPerm = player.hasPermission("skyprisoncore.inventories." + this.category + "." + itemData.get("permission"));
            ItemStack item = ItemStack.deserializeBytes((byte[]) itemData.get("item"));
            ItemMeta itemMeta = item.getItemMeta();
            List<Component> lore = Objects.requireNonNullElse(itemMeta.lore(), new ArrayList<>());
            if(!hasPerm) {
                item.setType(Material.BARRIER);
                if(itemMeta.hasEnchants()) {
                    itemMeta.getEnchants().keySet().forEach(itemMeta::removeEnchant);
                }
                lore = new ArrayList<>();
                lore.add(MiniMessage.miniMessage().deserialize((String) itemData.get("permissison-message")));
            } else {
                if((int) itemData.get("price_money") != 0) {
                    lore.add(Component.text("Cost: $" + plugin.formatNumber((int) itemData.get("price_money"))));
                }
                if((int) itemData.get("price_tokens") != 0) {
                    lore.add(Component.text("Cost: " + plugin.formatNumber((int) itemData.get("price_tokens")) + " tokens"));
                }
                if((int) itemData.get("price_voucher") != 0) {
                    lore.add(Component.text("Cost: " + plugin.formatNumber((int) itemData.get("price_voucher")) + " " + WordUtils.capitalize(itemData.get("price_voucher_type").toString())));
                }
            }
            if (canEdit) {
                lore.add(Component.empty());
                lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED));
                itemMeta.lore(lore);
            }
            item.setItemMeta(itemMeta);
            inventory.setItem(position, item);
        }
    }

    public DatabaseInventory(SkyPrisonCore plugin, DatabaseHook db, Player player, boolean canEdit, String category) {
        this.canEdit = canEdit;
        this.plugin = plugin;
        this.category = category;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text(WordUtils.capitalize(category.replace("-", " ")), TextColor.fromHexString("#0fc3ff")));

        HashMap<Integer, HashMap<String, Object>> items = new HashMap<>();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, item, permission, permission_message, price_money, price_tokens, " +
                "price_voucher_type, price_voucher, commands, max_uses, position FROM gui_items WHERE category = ?")) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> item = new HashMap<>();
                item.put("id", rs.getInt(1));
                item.put("item", rs.getBytes(2));
                item.put("permission", rs.getString(3));
                item.put("permission_message", rs.getString(4));
                item.put("price_money", rs.getInt(5));
                item.put("price_tokens", rs.getInt(6));
                item.put("price_voucher_type", rs.getString(7));
                item.put("price_voucher", rs.getInt(8));
                item.put("commands", rs.getString(9));
                item.put("max_uses", rs.getInt(10));

                items.put(rs.getInt(11), item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.items = items;
        updateInventory(player);
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

    public boolean getCanEdit() {
        return this.canEdit;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
