package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
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
    private final DatabaseHook db;
    private final boolean canEdit;
    private final HashMap<Integer, HashMap<String, Object>> items;
    private final String category;

    public void updateInventory(Player player) {
        for (Integer position : items.keySet()) {
            HashMap<String, Object> itemData = items.get(position);
            boolean hasPerm = player.hasPermission("skyprisoncore.inventories." + this.category + "." + itemData.get("permission"));
            int maxUses = (int) itemData.get("max_uses");
            int pUses = (int) itemData.get("player_uses");
            boolean canUse = (boolean) itemData.get("can_use");

            ItemStack item = ItemStack.deserializeBytes((byte[]) itemData.get("item"));
            ItemMeta itemMeta = item.getItemMeta();
            if (!hasPerm || !canUse) {
                item.setType(Material.BARRIER);
                itemMeta = item.getItemMeta();
                if (itemMeta.hasEnchants()) {
                    itemMeta.getEnchants().keySet().forEach(itemMeta::removeEnchant);
                }
            }
            boolean hiddenName = false;
            if(itemMeta.hasDisplayName()) {
                String plainDisplay = MiniMessage.miniMessage().serialize(Objects.requireNonNull(itemMeta.displayName()));
                String strippedTags = MiniMessage.miniMessage().stripTags(plainDisplay);
                if(strippedTags.isEmpty() || strippedTags.isBlank()) {
                    hiddenName = true;
                }
            }
            if(!hiddenName) {
                List<Component> lore = Objects.requireNonNullElse(itemMeta.lore(), new ArrayList<>());

                if(!lore.isEmpty()) {
                    List<Component> newLore = new ArrayList<>();
                    for(Component loreLine : lore) {
                        String msg = MiniMessage.miniMessage().serialize(loreLine);
                        if(msg.contains("\\<papi:")) {
                            msg = msg.replaceAll("\\\\<papi:", "<papi:");
                        }
                        newLore.add(MiniMessage.miniMessage().deserialize(msg, TagResolver.standard(), SkyPrisonCore.papiTag(player)));
                    }
                    lore = newLore;
                }

                boolean useMoney = ((int) itemData.get("price_money") != 0);
                boolean useTokens = ((int) itemData.get("price_tokens") != 0);
                boolean useVouchers = ((int) itemData.get("price_voucher") != 0 && !itemData.get("price_voucher_type").toString().equalsIgnoreCase("none"));
                if (useMoney) {
                    Component moneyComp = Component.text("Money: $" + plugin.formatNumber((int) itemData.get("price_money")), NamedTextColor.GRAY, TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false);
                    if(useTokens || useVouchers) {
                        moneyComp = moneyComp.append(Component.text(" (Left Click)", NamedTextColor.GRAY, TextDecoration.ITALIC).decoration(TextDecoration.BOLD, false));
                    }
                    moneyComp = moneyComp.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    lore.add(moneyComp);
                }
                if (useTokens) {
                    Component tokenComp = Component.text("Tokens: " + plugin.formatNumber((int) itemData.get("price_tokens")), NamedTextColor.GRAY, TextDecoration.BOLD);
                    if(useMoney) {
                        tokenComp = tokenComp.append(Component.text(" (Right Click)", NamedTextColor.GRAY, TextDecoration.ITALIC).decoration(TextDecoration.BOLD, false));
                    } else if(useVouchers) {
                        tokenComp = tokenComp.append(Component.text(" (Left Click)", NamedTextColor.GRAY, TextDecoration.ITALIC).decoration(TextDecoration.BOLD, false));
                    }
                    tokenComp = tokenComp.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    lore.add(tokenComp);
                }
                if (useVouchers) {
                    Component voucherComp = Component.text("Voucher: " + plugin.formatNumber((int) itemData.get("price_voucher")), NamedTextColor.GRAY, TextDecoration.BOLD);
                    //voucherComp = voucherComp.append(Component.text(" (" + WordUtils.capitalize(itemData.get("price_voucher_type").toString().replace("-", "")) + ") ", NamedTextColor.GRAY));
                    if(useMoney && useTokens) {
                        voucherComp = voucherComp.append(Component.text(" (Shift Click)", NamedTextColor.GRAY, TextDecoration.ITALIC).decoration(TextDecoration.BOLD, false));
                    } else if(useMoney || useTokens) {
                        voucherComp = voucherComp.append(Component.text(" (Right Click)", NamedTextColor.GRAY, TextDecoration.ITALIC).decoration(TextDecoration.BOLD, false));
                    }
                    voucherComp = voucherComp.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    lore.add(voucherComp);
                }
                if(useMoney || useTokens || useVouchers) {
                    lore.add(Component.text(itemData.get("usage_lore") + " " + pUses + (maxUses > 0 ? "/" + maxUses : ""), canUse ? NamedTextColor.GRAY : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                }

                if (!hasPerm && canUse) {
                    lore.add(Component.empty());
                    lore.add(MiniMessage.miniMessage().deserialize((String) itemData.get("permission_message")).decoration(TextDecoration.ITALIC, false));
                }

                if (canEdit) {
                    lore.add(Component.empty());
                    if(useMoney && useTokens && useVouchers) {
                        lore.add(Component.text("SHIFT LEFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    } else {
                        lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    }
                }
                itemMeta.lore(lore);
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                item.setItemMeta(itemMeta);
            }
            inventory.setItem(position, item);
        }
    }
    public void updateUsage(Player player) {
        for (Integer position : items.keySet()) {
            HashMap<String, Object> itemData = items.get(position);
            int pUses = 0;
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT uses FROM gui_items_usage WHERE item_id = ? AND user_id = ?")) {
                ps.setInt(1, (int) itemData.get("id"));
                ps.setString(2, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    pUses = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            int maxUses = (int) itemData.get("max_uses");
            itemData.put("can_use", (maxUses == 0 || maxUses > pUses || canEdit));
            itemData.put("player_uses", pUses);
            items.put(position, itemData);
        }
    }
    public void updateUsage(Player player, int position) {
        HashMap<String, Object> itemData = items.get(position);
        int pUses = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT uses FROM gui_items_usage WHERE item_id = ? AND user_id = ?")) {
            ps.setInt(1, (int) itemData.get("id"));
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pUses = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int maxUses = (int) itemData.get("max_uses");
        itemData.put("can_use", maxUses == 0 || maxUses > pUses || canEdit);
        itemData.put("player_uses", pUses);
        items.put(position, itemData);
    }

    public DatabaseInventory(SkyPrisonCore plugin, DatabaseHook db, Player player, boolean canEdit, String category) {
        this.canEdit = canEdit;
        this.plugin = plugin;
        this.category = category;
        this.db = db;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text(WordUtils.capitalize(category.replace("-", "")), TextColor.fromHexString("#0fc3ff")));

        HashMap<Integer, HashMap<String, Object>> items = new HashMap<>();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, item, permission, permission_message, price_money, price_tokens, " +
                "price_voucher_type, price_voucher, commands, max_uses, usage_lore, position FROM gui_items WHERE category = ?")) {
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
                item.put("usage_lore", rs.getString(11));

                items.put(rs.getInt(12), item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.items = items;

        updateUsage(player);
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

    public HashMap<String, Object> getItem(int position) {
        if(items.containsKey(position)) {
            return items.get(position);
        }
        return null;
    }

    public String getCategory() {
        return this.category;
    }

    public boolean getCanEdit() {
        return this.canEdit;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
