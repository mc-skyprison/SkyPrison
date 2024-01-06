package net.skyprison.skyprisoncore.inventories.economy;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BuyBack implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final LinkedHashMap<Integer, SoldItem> soldItems = new LinkedHashMap<>();
    public record SoldItem(Material itemType, int amount, double price, int id) {}

    public void updateInventory(int id) {
        soldItems.remove(id);
        List<Integer> availableNums = new ArrayList<>(Arrays.asList(11, 12, 13, 14, 15));
        availableNums.forEach(integer -> inventory.setItem(integer, null));
        for (SoldItem soldItem : soldItems.values()) {
            if(availableNums.isEmpty()) break;
            ItemStack iSold = getSoldItem(plugin, soldItem);
            inventory.setItem(availableNums.getFirst(), iSold);
            availableNums.removeFirst();
        }
    }
    public BuyBack(SkyPrisonCore plugin, DatabaseHook db, Player player) {
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, 27, Component.text("Buyback Shop", TextColor.fromHexString("#0fc3ff")));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, item, amount, price, bought_back FROM logs_shop WHERE user_id = ? AND transaction_type != ? ORDER BY id DESC LIMIT 5")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, "BUY");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                if(!rs.getBoolean(5)) {
                    soldItems.put(rs.getInt(1), new SoldItem(Material.getMaterial(rs.getString(2)), rs.getInt(3), rs.getDouble(4), rs.getInt(1)));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        whitePane.getItemMeta().displayName(Component.empty());
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        grayPane.getItemMeta().displayName(Component.empty());
        for (int i = 0; i < 27; i++) {
            if(i >= 17 && i <= 21 || i >= 23 || i == 9) {
                inventory.setItem(i, grayPane);
            } else if(i <= 8 || i == 10 || i == 16) {
                inventory.setItem(i, whitePane);
            } else if(i == 22) {
                ItemStack balance = new ItemStack(Material.NETHER_STAR);
                balance.editMeta(meta -> {
                    meta.displayName(Component.text("Your Balance", NamedTextColor.GOLD, TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false));
                    meta.lore(Collections.singletonList(Component.text(PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%"),
                            NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                });
                inventory.setItem(i, balance);
            }
        }
        List<Integer> availableNums = new ArrayList<>(Arrays.asList(11, 12, 13, 14, 15));
        for (SoldItem soldItem : soldItems.values()) {
            if(availableNums.isEmpty()) break;
            ItemStack iSold = getSoldItem(plugin, soldItem);
            inventory.setItem(availableNums.getFirst(), iSold);
            availableNums.removeFirst();
        }
    }
    @NotNull
    private static ItemStack getSoldItem(SkyPrisonCore plugin, SoldItem soldItem) {
        ItemStack iSold = new ItemStack(soldItem.itemType);
        iSold.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            double newPrice = soldItem.amount * 3;
            String price = ChatUtils.formatNumber(newPrice);
            lore.add(Component.text("Amount: ", NamedTextColor.YELLOW).append(Component.text(soldItem.amount,
                    NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Cost: ", NamedTextColor.YELLOW).append(Component.text(price,
                    NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            NamespacedKey key = new NamespacedKey(plugin, "sold-id");
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, soldItem.id);
        });
        return iSold;
    }

    public SoldItem getSoldItem(int id) {
        return soldItems.get(id);
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
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

