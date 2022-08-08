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
            tables.add("users");
            tables.add("bounties");
            tables.add("dailies");
            tables.add("referrals");
            tables.add("donations");
            tables.add("recently_killed");
            tables.add("teleport_ignore");
            tables.add("rewards_data");
            tables.add("secrets_data");
            tables.add("recent_sells");
            tables.add("block_sells");
            tables.add("shop_banned");
            tables.add("casino_cooldowns");
            for(String table : tables) {
                Connection conn;
                PreparedStatement ps;
                String sql = "";
                switch(table.toLowerCase()) {
                    case "users": // ALL DONZO
                        sql = "CREATE TABLE users (" +
                                "user_id varchar(255), " + // DONZO
                                "current_name TEXT, " + // DONZO
                                "first_join long, " + // DONZO
                                "tokens int, " + // DONZO
                                "sponges_found int, " + // DONZO
                                "blocks_mined int, " + // DONZO
                                "brews_drank int, " + // DONZO
                                "discord_id long, " + // DONZO
                                "pvp_deaths int, " + // DONZO
                                "pvp_kills int, " + // DONZO
                                "pvp_killstreak int, " + // DONZO
                                "PRIMARY KEY (user_id)" +
                                ")";
                        break;
                    case "casino_cooldowns": // ALL DONZO
                        sql = "CREATE TABLE casino_cooldowns (" +
                                "casino_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "casino_name varchar(255), " +
                                "casino_cooldown long, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "shop_banned": // ALL DONZO
                        sql = "CREATE TABLE shop_banned (" +
                                "banned_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "banned_user varchar(255), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                                "FOREIGN KEY (banned_user) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "block_sells": // ALL DONZO
                        sql = "CREATE TABLE block_sells (" +
                                "block_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "block_item varchar(255), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "recent_sells": // ALL DONZO
                        sql = "CREATE TABLE recent_sells (" +
                                "recent_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "recent_item varchar(255), " +
                                "recent_amount int, " +
                                "recent_price float, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "rewards_data": // ALL DONZO
                        sql = "CREATE TABLE rewards_data (" +
                                "rewards_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "reward_name varchar(255), " +
                                "reward_collected TINYINT, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "secrets_data": // ALL DONZO
                        sql = "CREATE TABLE secrets_data (" +
                                "secret_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "secret_name varchar(255), " +
                                "secret_amount int, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "teleport_ignore": // ALL DONZO
                        sql = "CREATE TABLE teleport_ignore (" +
                                "teleport_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "ignore_id archar(255), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                                "FOREIGN KEY (ignore_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "recently_killed": // ALL DONZO
                        sql = "CREATE TABLE recently_killed (" +
                                "killing_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "killer_id varchar(255), " +
                                "killed_id archar(255), " +
                                "killed_on long, " +
                                "FOREIGN KEY (killer_id) REFERENCES users(user_id), " +
                                "FOREIGN KEY (killed_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "bounties": // ALL DONZO
                        sql = "CREATE TABLE bounties (" +
                                "user_id varchar(255), " +
                                "prize float, " +
                                "bountied_by LONGTEXT, " +
                                "PRIMARY KEY (user_id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "referrals": // ALL DONZO
                        sql = "CREATE TABLE referrals (" +
                                "refer_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "referred_by varchar(255), " +
                                "refer_date TEXT, " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "dailies": // ALL DONZO
                        sql = "CREATE TABLE dailies (" +
                                "user_id varchar(255), " +
                                "current_streak int, " +
                                "total_collected int, " +
                                "highest_streak int, " +
                                "last_collected varchar(255), " +
                                "PRIMARY KEY (user_id), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
                        break;
                    case "donations": // ALL DONZO
                        sql = "CREATE TABLE donations (" +
                                "donor_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                                "user_id varchar(255), " +
                                "item_bought varchar(255), " +
                                "price float, " +
                                "currency varchar(255), " +
                                "amount int, " +
                                "date varchar(255), " +
                                "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                                ")";
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

        public void convertToSql() {
            ArrayList<String> tables = new ArrayList<>();
            tables.add("users");
            tables.add("bounties");
            tables.add("dailies");
            tables.add("referrals");
            tables.add("donations");
            tables.add("recently_killed");
            tables.add("teleport_ignore");
            tables.add("rewards_data");
            tables.add("secrets_data");
            tables.add("block_sells");
            tables.add("shop_banned");
            tables.add("casino_cooldowns");
            for(String table : tables) {
                String sql;
                List<Object> params;
                switch(table.toLowerCase()) {
                    case "users":
                        File tokenFile = new File(plugin.getDataFolder() + File.separator + "tokensdata.yml");
                        YamlConfiguration tokenConf = YamlConfiguration.loadConfiguration(tokenFile);
                        Set<String> tokensInfo = tokenConf.getConfigurationSection("players").getKeys(false);

                        for(String token : tokensInfo) {
                            int tokens = tokenConf.getInt("players." + token);

                            sql = "INSERT INTO users (user_id, current_name, first_join, tokens) VALUES (?, ?, ?, ?)";
                            params = new ArrayList<Object>() {{
                                add(token);
                                add(Bukkit.getOfflinePlayer(UUID.fromString(token)).getName());
                                add(Bukkit.getOfflinePlayer(UUID.fromString(token)).getFirstPlayed());
                                add(tokens);
                            }};
                            sqlUpdate(sql, params);
                        }

                        File spongesFile = new File(plugin.getDataFolder() + File.separator + "spongedata.yml");
                        YamlConfiguration spongeConf = YamlConfiguration.loadConfiguration(spongesFile);
                        Set<String> spongeInfo = spongeConf.getKeys(false);

                        for(String sponge : spongeInfo) {
                            int sponges = spongeConf.getInt(sponge + ".sponge-found");

                            sql = "INSERT INTO users (user_id, current_name, first_join, sponges_found) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET sponges_found = '" + sponges + "'";
                            params = new ArrayList<Object>() {{
                                add(sponge);
                                add(Bukkit.getOfflinePlayer(UUID.fromString(sponge)).getName());
                                add(Bukkit.getOfflinePlayer(UUID.fromString(sponge)).getFirstPlayed());
                                add(sponges);
                            }};
                            sqlUpdate(sql, params);
                        }

                        File blocksFile = new File(plugin.getDataFolder() + File.separator + "blocksmined.yml");
                        YamlConfiguration blockConf = YamlConfiguration.loadConfiguration(blocksFile);
                        Set<String> blockInfo = blockConf.getKeys(false);

                        for(String block : blockInfo) {
                            int blocks = blockConf.getInt(block);

                            sql = "INSERT INTO users (user_id, current_name, first_join, blocks_mined) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET blocks_mined = '" + blocks + "'";
                            params = new ArrayList<Object>() {{
                                add(block);
                                add(Bukkit.getOfflinePlayer(UUID.fromString(block)).getName());
                                add(Bukkit.getOfflinePlayer(UUID.fromString(block)).getFirstPlayed());
                                add(blocks);
                            }};
                            sqlUpdate(sql, params);
                        }

                        File discFile = new File(plugin.getDataFolder() + File.separator + "discord.yml");
                        YamlConfiguration discConf = YamlConfiguration.loadConfiguration(discFile);
                        Set<String> discInfo = discConf.getKeys(false);

                        for(String player : discInfo) {
                            long discId = discConf.getLong(player);

                            sql = "INSERT INTO users (user_id, current_name, first_join, discord_id) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET discord_id = '" + discId + "'";
                            params = new ArrayList<Object>() {{
                                add(player);
                                add(Bukkit.getOfflinePlayer(UUID.fromString(player)).getName());
                                add(Bukkit.getOfflinePlayer(UUID.fromString(player)).getFirstPlayed());
                                add(discId);
                            }};
                            sqlUpdate(sql, params);
                        }


                        File brewsFile = new File(plugin.getDataFolder() + File.separator + "brewsdrank.yml");
                        YamlConfiguration brewConf = YamlConfiguration.loadConfiguration(brewsFile);
                        Set<String> brewInfo = brewConf.getKeys(false);

                        for(String brew : brewInfo) {
                            int brews = brewConf.getInt(brew);

                            sql = "INSERT INTO users (user_id, current_name, first_join, brews_drank) VALUES (?, ?, ?, ?) ON CONFLICT(user_id) DO UPDATE SET brews_drank = '" + brews + "'";
                            params = new ArrayList<Object>() {{
                                add(brew);
                                add(Bukkit.getOfflinePlayer(UUID.fromString(brew)).getName());
                                add(Bukkit.getOfflinePlayer(UUID.fromString(brew)).getFirstPlayed());
                                add(brews);
                            }};
                            sqlUpdate(sql, params);
                        }

                        File killsFile = new File(plugin.getDataFolder() + File.separator + "recentkills.yml");
                        YamlConfiguration killsConf = YamlConfiguration.loadConfiguration(killsFile);
                        Set<String> killsInfo = killsConf.getKeys(false);

                        for(String killer : killsInfo) {
                            int deaths = killsConf.getInt(killer + ".pvpdeaths");
                            int kills = killsConf.getInt(killer + ".pvpkills");
                            int killstreak = killsConf.getInt(killer + ".pvpkillstreak");

                            sql = "INSERT INTO users (user_id, current_name, first_join, pvp_deaths, pvp_kills, pvp_killstreak) VALUES (?, ?, ?, ?, ?, ?) " +
                                    "ON CONFLICT(user_id) DO UPDATE SET pvp_killstreak = '" + killstreak + "', pvp_deaths = '" + deaths + "', pvp_kills = '" + kills + "'";
                            params = new ArrayList<Object>() {{
                                add(killer);
                                add(Bukkit.getOfflinePlayer(UUID.fromString(killer)).getName());
                                add(Bukkit.getOfflinePlayer(UUID.fromString(killer)).getFirstPlayed());
                                add(deaths);
                                add(kills);
                                add(killstreak);
                            }};
                            sqlUpdate(sql, params);
                        }
                        break;
                    case "teleport_ignore":
                        File tpIgnore = new File(plugin.getDataFolder() + File.separator + "teleportignore.yml");
                        YamlConfiguration tpConf = YamlConfiguration.loadConfiguration(tpIgnore);
                        Set<String> tpInfo = tpConf.getKeys(false);

                        for(String player : tpInfo) {
                            List<String> ignoreList = tpConf.getStringList(player + ".ignores");

                            for(String ignorePlayer : ignoreList) {
                                sql = "INSERT INTO teleport_ignore (user_id, ignore_id) VALUES (?, ?)";
                                params = new ArrayList<Object>() {{
                                    add(player);
                                    add(ignorePlayer);
                                }};
                                sqlUpdate(sql, params);
                            }
                        }
                        break;
                    case "casino_cooldowns":
                        File casinoFile = new File(plugin.getDataFolder() + File.separator + "casinocooldown.yml");
                        YamlConfiguration casinoConf = YamlConfiguration.loadConfiguration(casinoFile);
                        Set<String> casinoInfo = casinoConf.getKeys(false);

                        for(String player : casinoInfo) {
                            long end = casinoConf.getLong(player + ".casino_end");
                            long basic = casinoConf.getLong(player + ".casino_basic");
                            long superCool = casinoConf.getLong(player + ".casino_super");
                            long diamond = casinoConf.getLong(player + ".casino_diamond");
                            long enchant = casinoConf.getLong(player + ".casino_enchant");

                            HashMap<String, Long> casinoes = new HashMap<>();
                            casinoes.put("casino_end", end);
                            casinoes.put("casino_basic", basic);
                            casinoes.put("casino_super", superCool);
                            casinoes.put("casino_diamond", diamond);
                            casinoes.put("casino_enchant", enchant);

                            for(String casino : casinoes.keySet()) {
                                sql = "INSERT INTO casino_cooldowns (user_id, casino_name, casino_cooldown) VALUES (?, ?, ?)";
                                params = new ArrayList<Object>() {{
                                    add(player);
                                    add(casino);
                                    add(casinoes.get(casino));
                                }};
                                sqlUpdate(sql, params);
                            }
                        }
                        break;
                    case "shop_banned":
                        File bannedFile = new File(plugin.getDataFolder() + File.separator + "shopban.yml");
                        YamlConfiguration bannedConf = YamlConfiguration.loadConfiguration(bannedFile);
                        Set<String> bannedInfo = bannedConf.getKeys(false);

                        for(String player : bannedInfo) {
                            List<String> bannedPlayers = bannedConf.getStringList(player + ".banned-players");

                            for(String banned : bannedPlayers) {
                                sql = "INSERT INTO shop_banned (user_id, banned_user) VALUES (?, ?)";
                                params = new ArrayList<Object>() {{
                                    add(player);
                                    add(banned);
                                }};
                                sqlUpdate(sql, params);
                            }
                        }
                        break;
                    case "block_sells":
                        File sellBlocksFile = new File(plugin.getDataFolder() + File.separator + "blocksells.yml");
                        YamlConfiguration sellBlockConf = YamlConfiguration.loadConfiguration(sellBlocksFile);
                        Set<String> sellBlockInfo = sellBlockConf.getKeys(false);

                        for(String player : sellBlockInfo) {
                            List<String> sellBlocks = sellBlockConf.getStringList(player + ".blocked");
                            for(String sellBlock : sellBlocks) {
                                sql = "INSERT INTO block_sells (user_id, block_item) VALUES (?, ?)";
                                params = new ArrayList<Object>() {{
                                    add(player);
                                    add(sellBlock);
                                }};
                                sqlUpdate(sql, params);
                            }
                        }
                        break;
                    case "rewards_data":
                        File rewardsData = new File(plugin.getDataFolder() + File.separator + "secretsdata.yml");
                        YamlConfiguration rewardConf = YamlConfiguration.loadConfiguration(rewardsData);
                        Set<String> rewardInfo = rewardConf.getKeys(false);

                        for(String user : rewardInfo) {
                            if(rewardConf.isConfigurationSection(user + ".rewards")) {
                                Set<String> rewards = rewardConf.getConfigurationSection(user + ".rewards").getKeys(false);
                                for (String reward : rewards) {
                                    int boolToInt = 0;
                                    boolean hasColl = rewardConf.getBoolean(user + ".rewards." + reward + ".collected");
                                    if (hasColl) boolToInt = 1;

                                    sql = "INSERT INTO rewards_data (user_id, reward_name, reward_collected) VALUES (?, ?, ?)";
                                    int finalBoolToInt = boolToInt;
                                    params = new ArrayList<Object>() {{
                                        add(user);
                                        add(reward);
                                        add(finalBoolToInt);
                                    }};
                                    sqlUpdate(sql, params);
                                }
                            }
                        }
                        break;
                    case "secrets_data":
                        File secretsData = new File(plugin.getDataFolder() + File.separator + "secretsdata.yml");
                        YamlConfiguration secretConf = YamlConfiguration.loadConfiguration(secretsData);
                        Set<String> secretInfo = secretConf.getKeys(false);

                        for(String user : secretInfo) {
                            Set<String> secrets = secretConf.getConfigurationSection(user + ".secrets-found").getKeys(true);
                            ArrayList<String> allSecrets = new ArrayList<>();

                            for(String secret : secrets) {
                                String[] split = secret.split("\\.");
                                for(String splitted : split) {
                                    allSecrets.add(splitted);
                                }
                            }

                            allSecrets.removeIf(n -> (!n.matches(".*\\d.*")));

                            Set<String> secretNoDups = new LinkedHashSet<>(allSecrets);
                            allSecrets.clear();

                            allSecrets.addAll(secretNoDups);

                            for(String secret : allSecrets) {
                                String secretArea = secret.substring(0, secret.length() - 1);
                                sql = "INSERT INTO secrets_data (user_id, secret_name, secret_amount) VALUES (?, ?, ?)";
                                params = new ArrayList<Object>() {{
                                    add(user);
                                    add(secret);
                                    add(secretConf.getInt(user + ".secrets-found." + secretArea + "." + secret + ".times-found"));
                                }};
                                sqlUpdate(sql, params);
                            }
                        }
                        break;
                    case "recently_killed":
                        File killsFile2 = new File(plugin.getDataFolder() + File.separator + "recentkills.yml");
                        YamlConfiguration killsConf2 = YamlConfiguration.loadConfiguration(killsFile2);
                        Set<String> killsInfo2 = killsConf2.getKeys(false);
                        int asd = 0;
                        for(String killer : killsInfo2) {
                            if(killsConf2.isConfigurationSection(killer + ".kills")) {
                                Set<String> recentKills = killsConf2.getConfigurationSection(killer + ".kills").getKeys(false);
                                for (String user : recentKills) {
                                    if (asd == 0) {
                                        asd += 1;
                                    }
                                    sql = "INSERT INTO recently_killed (killer_id, killed_id, killed_on) VALUES (?, ?, ?)";
                                    params = new ArrayList<Object>() {{
                                        add(killer);
                                        add(user);
                                        add(killsConf2.getString(killer + ".kills." + user + ".time"));
                                    }};
                                    sqlUpdate(sql, params);
                                }
                            }
                        }
                        break;
                    case "bounties":
                        File bounty = new File(plugin.getDataFolder() + File.separator + "bounties.yml");
                        YamlConfiguration bConf = YamlConfiguration.loadConfiguration(bounty);
                        Set<String> bounties = bConf.getKeys(false);
                        for(String player : bounties) {
                            sql = "INSERT INTO bounties (user_id, prize, bountied_by) VALUES (?, ?, ?)";
                            params = new ArrayList<Object>() {{
                                add(player);
                                add(bConf.getDouble(player + ".bounty-prize"));
                                add(bConf.getList(player + ".bounty-contributors"));
                            }};
                            sqlUpdate(sql, params);
                        }
                        break;
                    case "referrals":
                        File refs = new File(plugin.getDataFolder() + File.separator + "referrals.yml");
                        YamlConfiguration rConf = YamlConfiguration.loadConfiguration(refs);
                        Set<String> refers = rConf.getKeys(false);
                        for(String refer : refers) {
                            List<String> refferals = rConf.getStringList(refer + ".reffedBy");
                            for(String refferal : refferals) {
                                sql = "INSERT INTO referrals (user_id, referred_by, refer_date) VALUES (?, ?, ?)";
                                String[] refSplit = refferal.split(":");
                                params = new ArrayList<Object>() {{
                                    add(refer);
                                    add(refSplit[0]);
                                    add(refSplit[1]);
                                }};
                                sqlUpdate(sql, params);
                            }
                        }
                        break;
                    case "dailies":
                        File daily = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
                        YamlConfiguration dConf = YamlConfiguration.loadConfiguration(daily);
                        Set<String> dailies = dConf.getConfigurationSection("players").getKeys(false);
                        for(String player : dailies) {
                            sql = "INSERT INTO dailies (user_id, current_streak, total_collected, highest_streak, last_collected) VALUES (?, ?, ?, ?, ?)";
                            params = new ArrayList<Object>() {{
                                add(player);
                                add(dConf.getInt("players." + player + ".current-streak"));
                                add(dConf.getInt("players." + player + ".total-collected"));
                                add(dConf.getInt("players." + player + ".highest-streak"));
                                add(dConf.getString("players." + player + ".last-collected"));
                            }};
                            sqlUpdate(sql, params);
                        }
                        break;
                    case "donations":
                        File donorFolder = new File(plugin.getDataFolder() + File.separator + "donations");
                        File[] files = donorFolder.listFiles();
                        for(File file : files) {
                            YamlConfiguration fileConf = YamlConfiguration.loadConfiguration(file);
                            Set<String> donors = fileConf.getKeys(false);
                            for (String player : donors) {
                                if (!player.equalsIgnoreCase("totalDonationAmount")) {
                                    sql = "INSERT INTO donations (user_id, item_bought, price, currency, amount, date) VALUES (?, ?, ?, ?, ?, ?)";
                                    params = new ArrayList<Object>() {{
                                        add(file.getName().split("\\.")[0]);
                                        add(fileConf.getString(player + ".item-bought"));
                                        add(fileConf.getDouble(player + ".item-price"));
                                        add(fileConf.getString(player + ".item-currency"));
                                        add(fileConf.getInt(player + ".item-quantity"));
                                        add(fileConf.getString(player + ".bought-date"));
                                    }};
                                    sqlUpdate(sql, params);
                                }
                            }
                        }
                        break;
                }
            }
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
