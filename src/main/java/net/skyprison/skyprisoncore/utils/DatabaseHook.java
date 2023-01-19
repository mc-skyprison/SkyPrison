package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseHook {
        private static final String db_file_name = "skyprisondb";
        //private final boolean sqlEnabled;
        private int port;
        //Connection connection;

        private final SkyPrisonCore plugin;

    public DatabaseHook(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }



        // SQL creation stuff
        public Connection getSQLConnection() {
            return getLocal();
        }


        private Connection getLocal() {
            File dataFolder = new File(plugin.getDataFolder(), db_file_name + ".db");
            if (!dataFolder.exists()) {
                try {
                    dataFolder.getParentFile().mkdir();
                    dataFolder.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "File write error: " + dataFolder.getPath());
                    e.printStackTrace();
                }
            }
            try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
            }
            return null;
        }

        /*


        //

        User Table
        USER_UUID (UUID)
        LATEST_NAME (STRING)
        PREVIOUS_NAMES (STRING LIST)
        TELEPORT_IGNORES (UUID LIST)
        TOKENS (INT)
        SPONGES_FOUND (INT)
        RECENT_SELLS (STRING)
        FIRSTJOIN (LONG)
        SHOPBANNED (UUID LIST)
        BLOCKSMINED (INT)
        BREWS DRANK (INT)
        DISCORD_USER (STRING?)
        SELL-BLOCKS (STRING LIST)

        //

        Donations Table
        SOME_ID
        USER_UUID
        ITEM_BOUGHT
        PRICE
        AMOUNT
        DATE

        SPONGE TABLE
        SPONGELOC_ID (INT)
        WORLD (STRING)
        X (DOUBLE)
        Y (DOUBLE)
        Z (DOUBLE)

        //

        Daily
        USER_UUID (UUID)
        CURRENT STREAK (INT)
        TOTAL STREAK (INT
        HIGHEST STREAK (INT)
        LAST-COLLECTED (STRING)


        //

        Bounties
        UNIQUE_ID (INT)
        BOUNTIED_USER (UUID)
        PUT_BOUNTY (UUID LIST)
        AMOUNT (DOUBLE)


        //
        Casino
        USER_UUID (UUID)
        CASINO_END (LONG)
        CASINO_BASIC (LONG)
        CASINO_SUPER (LONG)
        CASINO_DIAMOND (LONG)
        CASINO_ENCHANT (LONG)
         */

        public void createDatabase() {
            ArrayList<String> tables = new ArrayList<>();
            tables.add("tags");
            tables.add("user");
            for(String table : tables) {
                Connection conn;
                PreparedStatement ps;
                String sql = "";
                switch(table.toLowerCase()) {
                    case "tags":
                        sql = "CREATE TABLE tags (" +
                                "tags_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "tags_display varchar(255), " +
                                "tags_lore varchar(255), " +
                                "tags_effect varchar(255), " +
                                "tags_permission varchar(255) " +
                                ")";
                        break;
                    case "user":
                        sql = "ALTER TABLE users ADD active_tag int";
                        break;
                }

                try {
                    conn = getSQLConnection();
                    ps = conn.prepareStatement(sql);
                    ps.executeUpdate();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            Bukkit.getLogger().info("donzo!");
        }

        public void deleteUser(String UUID) {
            List<Object> params = new ArrayList<Object>() {{
                add(UUID);
            }};
            String sql = "delete FROM users WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM block_sells WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM bounties WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM casino_cooldowns WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM dailies WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM donations WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM recent_sells WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM recently_killed WHERE killer_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM recently_killed WHERE killed_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM referrals WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM referrals WHERE referred_by = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM rewards_data WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM secrets_data WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM shop_banned WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM shop_banned WHERE banned_user = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM teleport_ignore WHERE user_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM teleport_ignore WHERE ignore_id = ?";
            sqlUpdate(sql, params);
            sql = "delete FROM users WHERE user_id = ?";
            sqlUpdate(sql, params);
        }

        public void convertToSql() {
            Bukkit.getLogger().info("donzo!");
        }

    //Processing
        public boolean sqlUpdate(String statement, @NonNull List<Object> params) {
            Connection conn;
            PreparedStatement ps;
            boolean success = true;
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement(statement);
                Iterator<Object> it = params.iterator();
                int paramIndex = 1;
                while (it.hasNext()) {
                    ps.setObject(paramIndex, it.next());
                    paramIndex++;
                }
                ps.executeUpdate();
                close(ps, null, conn);
            } catch (SQLException e) {
                success = false;
                e.printStackTrace();
            }
            return success;
        }

    public void close(PreparedStatement ps, ResultSet rs, Connection conn) {
        try {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
            if (rs != null) rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
