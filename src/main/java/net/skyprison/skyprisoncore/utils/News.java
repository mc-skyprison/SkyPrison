package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.inventories.misc.NewsMessageEdit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class News {
    public static boolean saveNewsMessage(NewsMessageEdit newsEdit, DatabaseHook db) {
        if(newsEdit.getNewsMessage() != 0) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE news SET title = ?, content = ?, hover = ?, click_type = ?, click_data = ?,  " +
                    "permission = ?, priority = ?, last_updated = ?, limited_time = ?, limited_start = ?, limited_end = ? WHERE id = ?")) {
                ps.setString(1, newsEdit.getTitle());
                ps.setString(2, newsEdit.getContent());
                ps.setString(3, newsEdit.getHover());
                ps.setString(4, newsEdit.getClickType());
                ps.setString(5, newsEdit.getClickData());
                ps.setString(6, newsEdit.getPermission());
                ps.setInt(7, newsEdit.getPriority());
                ps.setLong(8, System.currentTimeMillis());
                ps.setInt(9, newsEdit.getLimitedTime());
                ps.setLong(10, newsEdit.getLimitedStart());
                ps.setLong(11, newsEdit.getLimitedEnd());
                ps.setInt(12, newsEdit.getNewsMessage());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO news (title, content, hover, click_type, click_data, permission, priority, " +
                    "created_on, last_updated, limited_time, limited_start, limited_end) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, newsEdit.getTitle());
                ps.setString(2, newsEdit.getContent());
                ps.setString(3, newsEdit.getHover());
                ps.setString(4, newsEdit.getClickType());
                ps.setString(5, newsEdit.getClickData());
                ps.setString(6, newsEdit.getPermission());
                ps.setInt(7, newsEdit.getPriority());
                ps.setLong(8, System.currentTimeMillis());
                ps.setLong(9, System.currentTimeMillis());
                ps.setInt(10, newsEdit.getLimitedTime());
                ps.setLong(11, newsEdit.getLimitedStart());
                ps.setLong(12, newsEdit.getLimitedEnd());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
