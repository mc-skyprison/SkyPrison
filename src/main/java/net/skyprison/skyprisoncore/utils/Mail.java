package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mail {
    private static final DatabaseHook db = SkyPrisonCore.db;
    public static List<String> getBoxesWithMail(Player player) {
        List<String> mailBoxes = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name FROM mail_boxes WHERE id IN " +
                "(SELECT DISTINCT mailbox_id FROM mails WHERE collected = 0 AND mailbox_id IN (SELECT mailbox_id FROM mail_boxes_users WHERE user_id = ?)) " +
                "UNION SELECT name FROM mail_boxes WHERE id = -1 AND EXISTS (SELECT 1 FROM mails WHERE user_id = ? AND collected = 0 AND mailbox_id = -1)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mailBoxes.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mailBoxes;
    }
    public static int getPreferredMailbox(UUID pUUID) {
        int mailBox = -1;
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT mailbox_id FROM mail_boxes_users WHERE user_id = ? AND preferred = 1")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mailBox = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mailBox;
    }
    public static int getMailboxAmount(UUID pUUID) {
        int mailBoxes = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(id) FROM mail_boxes WHERE owner_id = ? AND deleted = 0")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                mailBoxes = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mailBoxes;
    }
    public static boolean isMailBoxDeleted(int mailBox) {
        boolean deleted = false;
        try (Connection sConn = db.getConnection(); PreparedStatement sPs = sConn.prepareStatement("SELECT deleted FROM mail_boxes WHERE id = ?")) {
            sPs.setInt(1, mailBox);
            ResultSet sRs = sPs.executeQuery();
            if (sRs.next()) {
                deleted = sRs.getInt(1) == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deleted;
    }
    public static boolean isMailBoxValid(int mailBox) {
        boolean isValid = false;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM mail_boxes WHERE id = ? AND is_placed = 1 AND deleted = 0")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                isValid = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isValid;
    }
    public static int getValidMailBox(UUID pUUID) {
        int mailBox = getPreferredMailbox(pUUID);
        if (!isMailBoxValid(mailBox)) {
            mailBox = -1;
        }
        return mailBox;
    }

    public static int getMailBox(Block b) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM mail_boxes WHERE x = ? AND y = ? AND z = ? AND world = ?")) {
            ps.setInt(1, b.getX());
            ps.setInt(2, b.getY());
            ps.setInt(3, b.getZ());
            ps.setString(4, b.getWorld().getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static String getMailBoxName(int id) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static int getMailBoxByName(String name) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM mail_boxes WHERE LOWER(name) = LOWER(?) AND deleted = 0")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -2;
    }
}
