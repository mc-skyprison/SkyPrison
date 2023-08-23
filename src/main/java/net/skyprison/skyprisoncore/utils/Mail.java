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
    public static int getMailBox(Block b) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM mail_boxes WHERE x = ? AND y = ? AND z = ? AND world = ?")) {
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
}
