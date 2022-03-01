package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class DatabaseHook {
        private static final String db_file_name = "skyprisondb";
        //private final boolean sqlEnabled;
        private int port;
        //Connection connection;

        private SkyPrisonCore plugin;

    public DatabaseHook(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }



        // SQL creation stuff
        public Connection getSQLConnection() {
            return getLocal();
        }


        private Connection getLocal() {
            File dataFolder = new File(plugin.getDataFolder(), db_file_name + ".db");
            if (!dataFolder.exists()){
                try {
                    dataFolder.getParentFile().mkdir();
                    dataFolder.createNewFile();
                    createDatabase();
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
        DONATIONS (STRING LIST
        TELEPORT_IGNORES (UUID LIST)
        TOKENS (INT)
        SPONGES_FOUND (INT)
        RECENT_SELLS (STRING)
        FIRSTJOIN (LONG)
        SHOPBANNED (UUID LIST)
        BLOCKSMINED (INT)
        BREWS DRANK (INT)
        SELL-BLOCKS (STRING LIST)

        //

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
            tables.add("users");
            tables.add("bounties");
            tables.add("spongelocations");
            tables.add("casinocooldowns");
            for(String table : tables) {
                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                String sql = "";
                switch(table.toLowerCase()) {
                    case "users":
                        sql = "CREATE TABLE users (" +
                                "user_id varchar(255)," +
                                "current_name varchar(255)," +
                                "previous_names varchar(255)," +
                                "donations varchar(255)," +
                                "tokens int(16)," +
                                "sponges_found int(16)," +
                                "recent_Sells varchar(255)," +
                                "firstjoin long(255)," +
                                "shop_banned varchar(255)," +
                                "blocks_mined int(16))," +
                                "brews_drank  int(16)," +
                                "sell_blocks varchar(255)" +
                                ")";
                        break;
                    case "bounties":
                        sql = "CREATE TABLE bounties (" +
                                "user_id varchar(255)," +
                                "prize float(53)" +
                                ")";
                        break;
                    case "spongelocations":
                        break;
                    case "casinocooldowns":
                        break;
                }

                try {
                    conn = getSQLConnection();

                    ps = conn.prepareStatement(sql);
                    rs = ps.executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } finally {
                    close(ps, rs, conn);
                }
            }
        }

        //Processing
        boolean sqlUpdate(String statement, @NonNull List<Object> params) {
            Connection conn = null;
            PreparedStatement ps = null;
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
            } catch (SQLException ex) {
                success = false;
                ex.printStackTrace();
            } finally {
                close(ps, null, conn);
            }
            return success;
        }

        boolean sqlUpdate(List<String> statement1, List<List<Object>> params1) {
            Connection conn = null;
            PreparedStatement ps = null;
            boolean success = true;
            try {
                conn = getSQLConnection();
                for (int i = 0; i < statement1.size(); i++) {
                    String statement = statement1.get(i);
                    List<Object> params = params1.get(i);
                    if (ps == null)
                        ps = conn.prepareStatement(statement);
                    else
                        ps.addBatch(statement);
                    if (params != null) {
                        Iterator<Object> it = params.iterator();
                        int paramIndex = 1;
                        while (it.hasNext()) {
                            ps.setObject(paramIndex, it.next());
                            paramIndex++;
                        }
                    }
                }
                assert ps != null;
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                success = false;
                ex.printStackTrace();
            } finally {
                close(ps, null, conn);
            }
            return success;
        }

        void close(PreparedStatement ps, ResultSet rs, Connection conn) {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
                if (rs != null) rs.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
