package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.misc.DatabaseInventoryEdit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class CustomInvUtils {
    public static boolean saveItem(DatabaseInventoryEdit itemEdit) {
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
    public static boolean categoryExists(String category) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM gui_inventories WHERE id = ?")) {
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
    public static void createCategory(String category, String display, String colour) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO gui_inventories (id, display, colour) VALUES (?, ?, ?)")) {
            ps.setString(1, category);
            ps.setString(2, display);
            ps.setString(3, colour);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<String> getList() {
        List<String> categories = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM gui_inventories")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
    public static Component getFormattedList(int page) {
        List<String> categories = getList();
        int totalPages = (int) Math.ceil(categories.size() / 10.0);

        int prevPage = Math.max(1, page - 1);
        int nextPage = Math.min(totalPages, page + 1);

        int pageStart = (page - 1) * 10;
        Component msg = Component.empty();
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Custom Inventories ", TextColor.fromHexString("#FFFF00"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        for (int i = pageStart; i < Math.min(pageStart + 10, categories.size()); i++) {
            String category = categories.get(i);
            int listNum = i + 1;
            msg = msg.append(Component.text("\n" + listNum + ". ", TextColor.fromHexString("#cea916"))
                    .append(Component.text(category))
                    .hoverEvent(HoverEvent.showText(Component.text("Open GUI", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/custominv open " + category)));
        }
        if (page == 1) {
            if(categories.size() > 10) {
                msg = msg.append(Component.text("\n" + page, TextColor.fromHexString("#266d27"))
                        .append(Component.text("/", NamedTextColor.GRAY)
                                .append(Component.text(totalPages, TextColor.fromHexString("#266d27"))))
                        .append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY)))
                                .clickEvent(ClickEvent.runCommand("/custominv list " + nextPage))));
            }
        } else if (page == totalPages) {
            msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/custominv list " + prevPage))
                    .append(Component.text(page, TextColor.fromHexString("#266d27"))
                            .append(Component.text("/", NamedTextColor.GRAY)
                                    .append(Component.text(totalPages, TextColor.fromHexString("#266d27"))))));
        } else {
            msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/custominv list " + prevPage))
                    .append(Component.text(page, TextColor.fromHexString("#266d27"))
                            .append(Component.text("/", NamedTextColor.GRAY)
                                    .append(Component.text(totalPages, TextColor.fromHexString("#266d27")))))
                    .append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))))
                    .clickEvent(ClickEvent.runCommand("/custominv list " + nextPage)));
        }
        return msg;
    }
}
