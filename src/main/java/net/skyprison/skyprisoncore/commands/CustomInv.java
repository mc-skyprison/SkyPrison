package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.DatabaseInventory;
import net.skyprison.skyprisoncore.inventories.DatabaseInventoryEdit;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomInv implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public CustomInv(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    public boolean saveItem(DatabaseInventoryEdit itemEdit) {
        if(itemEdit.getItemId() != -1) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE gui_items SET item = ?, permission = ?, permission_message = ?, price_money = ?, " +
                    "price_tokens = ?, price_voucher_type = ?, price_voucher = ?, commands = ?, max_uses = ?, usage_lore = ?, position = ?, category = ? WHERE id = ?")) {
                ps.setBytes(1, itemEdit.getItem());
                ps.setString(2, itemEdit.getPermission());
                ps.setString(3, itemEdit.getPermissionMessage());
                ps.setInt(4, itemEdit.getPriceMoney());
                ps.setInt(5, itemEdit.getPriceTokens());
                ps.setString(6, itemEdit.getPriceVoucherType());
                ps.setInt(7, itemEdit.getPriceVoucher());
                ps.setString(8, itemEdit.getCommands());
                ps.setInt(9, itemEdit.getMaxUses());
                ps.setString(10, itemEdit.getUsageLore());
                ps.setInt(11, itemEdit.getPosition());
                ps.setString(12, itemEdit.getCategory());
                ps.setInt(13, itemEdit.getItemId());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO gui_items (item, permission, permission_message, price_money, price_tokens, " +
                    "price_voucher_type, price_voucher, commands, max_uses, usage_lore, position, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setBytes(1, itemEdit.getItem());
                ps.setString(2, itemEdit.getPermission());
                ps.setString(3, itemEdit.getPermissionMessage());
                ps.setInt(4, itemEdit.getPriceMoney());
                ps.setInt(5, itemEdit.getPriceTokens());
                ps.setString(6, itemEdit.getPriceVoucherType());
                ps.setInt(7, itemEdit.getPriceVoucher());
                ps.setString(8, itemEdit.getCommands());
                ps.setInt(9, itemEdit.getMaxUses());
                ps.setString(10, itemEdit.getUsageLore());
                ps.setInt(11, itemEdit.getPosition());
                ps.setString(12, itemEdit.getCategory());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static void addUses(UUID pUUID, int itemId, DatabaseHook db) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO gui_items_usage (item_id, user_id, uses) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE uses = uses + VALUE(uses)")) {
            ps.setInt(1, itemId);
            ps.setString(2, pUUID.toString());
            ps.setInt(3, 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean categoryExists(String category) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM gui_items WHERE category = ?")) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void createCategory(String category) {
        ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO gui_items (item, permission, permission_message, price_money, price_tokens, " +
                "price_voucher_type, price_voucher, commands, max_uses, usage_lore, position, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setBytes(1, item.serializeAsBytes());
            ps.setString(2, "general");
            ps.setString(3, "<red>You cant use this!");
            ps.setInt(4, 0);
            ps.setInt(5, 0);
            ps.setString(6, "none");
            ps.setInt(7, 0);
            ps.setString(8, "");
            ps.setInt(9, 0);
            ps.setString(10, "Times Bought:");
            ps.setInt(11, 0);
            ps.setString(12, category);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // /custominv <inventory> <player>
        if(args.length == 1 && args[0].equalsIgnoreCase("list")) {
            List<String> categories = new ArrayList<>();
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT category FROM gui_items")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if(!categories.contains(rs.getString(1))) categories.add(rs.getString(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Component msg = Component.text("");
            msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                    .append(Component.text(" Inventories ", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

            int i = 1;
            for (String category : categories) {
                msg = msg.appendNewline().append(Component.text(i + ". ", NamedTextColor.GRAY).append(Component.text(category, NamedTextColor.YELLOW)));
                i++;
            }
            sender.sendMessage(msg);
            return true;
        }

        Player player;
        if(args.length > 1) {
            UUID pUUID = plugin.getPlayer(args[1]);
            if(pUUID == null) {
                sender.sendMessage(Component.text("No player with that name exists!", NamedTextColor.RED));
                return true;
            } else {
                player = Bukkit.getPlayer(pUUID);
                if(player == null || !player.isOnline()) {
                    sender.sendMessage(Component.text("Player must be online!", NamedTextColor.RED));
                    return true;
                }
            }
        } else if(sender instanceof Player oPlayer) {
            player = oPlayer;
        } else {
            sender.sendMessage(Component.text("Incorrect Usage! Available Commands: \n- /custominv <inventory> <player>\n- /custominv list", NamedTextColor.RED));
            return true;
        }

        if (categoryExists(args[0]) && player.hasPermission("skyprisoncore.inventories." + args[0])) {
            player.openInventory(new DatabaseInventory(plugin, db, player, player.hasPermission("skyprisoncore.inventories." + args[0] + ".editing"), args[0]).getInventory());
        } else if (player.hasPermission("skyprisoncore.command.custominv.create")) {
            createCategory(args[0]);
            player.openInventory(new DatabaseInventory(plugin, db, player, player.hasPermission("skyprisoncore.inventories." + args[0] + ".editing"), args[0]).getInventory());
        } else {
            sender.sendMessage(Component.text("No such category!", NamedTextColor.RED));
        }
        return true;
    }
}
