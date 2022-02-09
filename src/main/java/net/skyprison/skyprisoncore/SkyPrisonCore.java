package net.skyprison.skyprisoncore;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import com.Zrips.CMI.events.CMIPlayerUnjailEvent;
import com.Zrips.CMI.events.CMIUserBalanceChangeEvent;
import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import com.google.common.collect.Lists;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import net.alex9849.arm.events.UnsellRegionEvent;
import net.coreprotect.CoreProtect;
import net.skyprison.skyprisonclaims.SkyPrisonClaims;
import net.skyprison.skyprisoncore.commands.*;
import net.skyprison.skyprisoncore.commands.guard.*;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.referral.Referral;
import net.skyprison.skyprisoncore.commands.referral.ReferralList;
import net.skyprison.skyprisoncore.commands.secrets.*;
import net.skyprison.skyprisoncore.utils.*;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.skyprison.skyprisoncore.commands.donations.DonorAdd;
import net.skyprison.skyprisoncore.commands.donations.DonorBulk;
import net.skyprison.skyprisoncore.commands.donations.Purchases;
import me.NoChance.PvPManager.Events.PlayerTagEvent;
import me.NoChance.PvPManager.Events.PlayerUntagEvent;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.maxgamer.quickshop.api.QuickShopAPI;
import org.maxgamer.quickshop.api.event.ShopCreateEvent;
import org.maxgamer.quickshop.api.event.ShopPurchaseEvent;
import org.maxgamer.quickshop.api.event.ShopSuccessPurchaseEvent;
import org.maxgamer.quickshop.api.shop.Shop;


public class SkyPrisonCore extends JavaPlugin implements Listener {
    public HashMap<String, String> hexColour = new HashMap<>();
    public HashMap<UUID, Boolean> flyPvP = new HashMap<>();
    public HashMap<UUID, Integer> teleportMove = new HashMap<>();
    public Map<String, Integer> tokensData = new HashMap<>();

    public Map<String, Integer> blockBreaks = new HashMap<>();

    public Connection conn;

    private File infoFile;
    private FileConfiguration infoConf;

    @Inject public Tokens tokens;
    @Inject private TransportPass transportPass;
    @Inject private TabCompleter tabCompleter;

    @Inject private ConfigCreator configCreator;
    @Inject private LangCreator langCreator;

    @Inject private DonorAdd DonorAdd;
    @Inject private DonorBulk DonorBulk;
    @Inject private Purchases Purchases;

    @Inject private ReferralList ReferralList;
    @Inject private Referral Referral;

    @Inject private Bounty Bounty;
    @Inject private KillInfo KillInfo;

    @Inject private Sword Sword;
    @Inject private Bow Bow;
    @Inject private Contraband Contraband;

    @Inject private net.skyprison.skyprisoncore.commands.guard.GuardDuty GuardDuty;

    @Inject private EndUpgrade EndUpgrade;

    @Inject private BuyBack BuyBack;

    @Inject private EconomyCheck EconomyCheck;
    @Inject private PermShop PermShop;

    @Inject private SecretsGUI SecretsGUI;
    @Inject private SecretFound SecretFound;

    @Inject private Bartender Bartender;

    @Inject private IgnoreTP IgnoreTP;

    @Inject private Safezone Safezone;

    @Inject private DontSell DontSell;

    @Inject private DropChest DropChest;
    @Inject private SpongeLoc SpongeLoc;
    @Inject private FirstjoinTop FirstjoinTop;


    @Inject private ShopBan shopBan;

    @Inject private Daily daily;

    @Inject private Bail bail;
    @Inject private Casino casino;


    @Inject private EnchTable enchTable;
    @Inject private RemoveItalics removeItalics;
    @Inject private BottledExp bottledExp;
    @Inject private MoneyHistory moneyHistory;


/*    MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");
    MVWorldManager wMan = core.getMVWorldManager();
    byte[] array = new byte[7];
            new Random().nextBytes(array);
    String generatedString = new String(array, Charset.forName("UTF-8"));
            wMan.addWorld("world_minigames_bingo_" + generatedString, World.Environment.NORMAL, null, WorldType.NORMAL, true, null);
            wMan.addWorld("world_minigames_bingo_" + generatedString + "_nether", World.Environment.NETHER, null, WorldType.NORMAL, true, null);
            wMan.addWorld("world_minigames_bingo_" + generatedString + "_the_end", World.Environment.THE_END, null, WorldType.NORMAL, true, null);
    World world = event.getRegion().getRegionworld();*/

    @Inject private plots plots;
    @Inject private PlotTeleport plotTeleport;

    FileConfiguration config = this.getConfig();
    
    public SkyPrisonClaims claimPlugin;

    HashMap<Material, Double> minPrice = new HashMap<>();

    public void onEnable() {
        minPrice.put(Material.BIRCH_LOG, 4.0);
        minPrice.put(Material.BIRCH_PLANKS, 1.0);
        minPrice.put(Material.BIRCH_SAPLING, 4.0);
        minPrice.put(Material.COAL, 8.0);
        minPrice.put(Material.COBBLESTONE, 1.0);
        minPrice.put(Material.STONE, 3.0);
        minPrice.put(Material.SANDSTONE, 1.0);
        minPrice.put(Material.SMOOTH_SANDSTONE, 3.0);
        minPrice.put(Material.SNOW_BLOCK, 0.5);
        minPrice.put(Material.GLOWSTONE, 2.0);
        minPrice.put(Material.NETHERRACK, 1.0);
        minPrice.put(Material.PUMPKIN, 5.0);
        minPrice.put(Material.NETHER_WART_BLOCK, 9.0);
        minPrice.put(Material.IRON_INGOT, 32.0);
        minPrice.put(Material.LAPIS_LAZULI, 4.5);
        minPrice.put(Material.BAMBOO, 1.0);
        minPrice.put(Material.STICK, 0.5);
        minPrice.put(Material.GOLD_NUGGET, 5.0);
        minPrice.put(Material.GOLD_INGOT, 45.0);
        minPrice.put(Material.EMERALD, 50.0);
        minPrice.put(Material.GREEN_DYE, 10.0);
        minPrice.put(Material.SUGAR_CANE, 2.0);
        minPrice.put(Material.SUGAR, 5.0);
        minPrice.put(Material.DIAMOND, 65.0);
        minPrice.put(Material.CHARCOAL, 5.5);
        minPrice.put(Material.NETHER_WART, 2.0);
        minPrice.put(Material.BEEF, 15.0);
        minPrice.put(Material.PORKCHOP, 15.0);
        minPrice.put(Material.SALMON, 5.0);
        minPrice.put(Material.TROPICAL_FISH, 45.0);
        minPrice.put(Material.LEATHER, 5.0);
        minPrice.put(Material.BONE, 15.0);
        minPrice.put(Material.ROTTEN_FLESH, 15.0);
        minPrice.put(Material.COOKED_BEEF, 20.0);
        minPrice.put(Material.COOKED_PORKCHOP, 20.0);
        minPrice.put(Material.COOKED_SALMON, 15.0);
        minPrice.put(Material.PUFFERFISH, 30.0);
        minPrice.put(Material.SPIDER_EYE, 15.0);
        minPrice.put(Material.STRING, 15.0);
        minPrice.put(Material.COD, 5.0);
        minPrice.put(Material.COOKED_COD, 15.0);
        minPrice.put(Material.MELON_SLICE, 1.0);
        minPrice.put(Material.APPLE, 4.0);

        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        PluginReceiver module = new PluginReceiver(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        configCreator.init();
        langCreator.init();

/*        String url = "jdbc:sqlite:" + this.getDataFolder() + File.separator + "SkyPrisonCore.db";
        File dbFile = new File(this.getDataFolder() + File.separator + "SkyPrisonCore.db");
        if(!dbFile.exists()) {
            ArrayList<String> tables = new ArrayList<>();
            tables.add("bounties");
            tables.add("spongelocations");
            tables.add("dropchest");
            tables.add("recentkills");
            tables.add("referrals");
            tables.add("rewardsdata");
            tables.add("secretsdata");
            tables.add("spongedata");
            tables.add("blocksells");
            tables.add("firstjoindata");
            tables.add("recentsells");
            tables.add("teleportignore");
            tables.add("dailyreward");
            tables.add("blocksmined");
            tables.add("shopban");
            tables.add("brewsdrank");
            for(String table : tables) {
                try {
                    conn = DriverManager.getConnection(url);
                    String sql = "CREATE TABLE bounties (" +
                            "user_id varchar(255)," +
                            "prize float(53)" +
                            ")";
                    conn.prepareStatement(sql).executeQuery();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }*/
/*        ArrayList<String> files = new ArrayList<>();
        files.add("bartender.yml");
        files.add("donations");
        files.add("secrets.yml");
        for (String file : files) {
            File f = new File(this.getDataFolder() + File.separator + file);
            if(!f.exists()) {
                if(file.contains(".") ) {
                    try {
                        f.createNewFile();
                        getLogger().info("File " + file + " successfully created");
                    } catch (IOException e) {
                        e.printStackTrace();
                        getLogger().info("File " + file + " failed to create");
                    }
                } else {
                    f.mkdir();
                    getLogger().info("Folder " + file + " successfully created");
                }
            }
        }*/

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
            getLogger().info("Placeholders registered");
        }
        
        if(Bukkit.getPluginManager().getPlugin("SkyPrisonClaims") != null) {
            claimPlugin = (SkyPrisonClaims) Bukkit.getPluginManager().getPlugin("SkyPrisonClaims");
        }

        Objects.requireNonNull(getCommand("tokens")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("token")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("tokens")).setTabCompleter(tabCompleter);
        Objects.requireNonNull(getCommand("token")).setTabCompleter(tabCompleter);

        Objects.requireNonNull(getCommand("donoradd")).setExecutor(DonorAdd);
        Objects.requireNonNull(getCommand("donorbulk")).setExecutor(DonorBulk);
        Objects.requireNonNull(getCommand("purchases")).setExecutor(Purchases);

        Objects.requireNonNull(getCommand("econcheck")).setExecutor(EconomyCheck);
        Objects.requireNonNull(getCommand("permshop")).setExecutor(PermShop);

        Objects.requireNonNull(getCommand("spongeloc")).setExecutor(SpongeLoc);
        Objects.requireNonNull(getCommand("dropchest")).setExecutor(DropChest);

        Objects.requireNonNull(getCommand("dontsell")).setExecutor(DontSell);
        Objects.requireNonNull(getCommand("endupgrade")).setExecutor(EndUpgrade);

        Objects.requireNonNull(getCommand("secretfound")).setExecutor(SecretFound);
        Objects.requireNonNull(getCommand("rewards")).setExecutor(SecretsGUI);

        Objects.requireNonNull(getCommand("bounty")).setExecutor(Bounty);
        Objects.requireNonNull(getCommand("killinfo")).setExecutor(KillInfo);

        Objects.requireNonNull(getCommand("firstjointop")).setExecutor(FirstjoinTop);

        Objects.requireNonNull(getCommand("referral")).setExecutor(Referral);
        Objects.requireNonNull(getCommand("referrallist")).setExecutor(ReferralList);

        Objects.requireNonNull(getCommand("bartender")).setExecutor(Bartender);

        Objects.requireNonNull(getCommand("sword")).setExecutor(Sword);
        Objects.requireNonNull(getCommand("bow")).setExecutor(Bow);
        Objects.requireNonNull(getCommand("contraband")).setExecutor(Contraband);

        Objects.requireNonNull(getCommand("ignoretp")).setExecutor(IgnoreTP);

        Objects.requireNonNull(getCommand("guardduty")).setExecutor(GuardDuty);

        Objects.requireNonNull(getCommand("safezone")).setExecutor(Safezone);

        Objects.requireNonNull(getCommand("buyback")).setExecutor(BuyBack);

        Objects.requireNonNull(getCommand("daily")).setExecutor(daily);

        Objects.requireNonNull(getCommand("shopban")).setExecutor(shopBan);

        Objects.requireNonNull(getCommand("enchtable")).setExecutor(enchTable);

        Objects.requireNonNull(getCommand("removeitalics")).setExecutor(removeItalics);

        Objects.requireNonNull(getCommand("bottledexp")).setExecutor(bottledExp);
        Objects.requireNonNull(getCommand("transportpass")).setExecutor(transportPass);


        Objects.requireNonNull(getCommand("bail")).setExecutor(bail);
        Objects.requireNonNull(getCommand("casino")).setExecutor(casino);

        Objects.requireNonNull(getCommand("plots")).setExecutor(plots);
        Objects.requireNonNull(getCommand("plot")).setExecutor(plotTeleport);
        Objects.requireNonNull(getCommand("moneyhistory")).setExecutor(moneyHistory);
        String line;
        String splitBy = ",";
        try {
            BufferedReader br = new BufferedReader(new FileReader(this.getDataFolder() + File.separator
                    + "colors.csv"));
            while ((line = br.readLine()) != null) {
                String[] colors = line.split(splitBy);
                hexColour.put(colors[0].toLowerCase().replaceAll(" ", ""), colors[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Plugin sPlugin = this;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    checkDailies(sPlugin);
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, 20 * 1800, 20 * 1800);



        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    checkOnlineDailies(sPlugin);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, 20 * 600, 20 * 600);


        infoFile = new File(this.getDataFolder() + File.separator + "info.yml");
        infoConf = YamlConfiguration.loadConfiguration(infoFile);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.getServer().getOnlinePlayers().size() > 0) {
                    announcer();
                }
            }
        }.runTaskTimer(this, 20*900, 20*900);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(tokensData != null && !tokensData.isEmpty()) {
                        Map<String, Integer> token = tokensData;
                        File tData = new File(sPlugin.getDataFolder() + File.separator + "tokensdata.yml");
                        FileConfiguration tokenConf = YamlConfiguration.loadConfiguration(tData);
                        for (String pUUID : token.keySet()) {
                            tokenConf.set("players." + pUUID, token.get(pUUID));
                        }
                        tokenConf.save(tData);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, 20 * 600, 20 * 600);

    }



    private void announcer() {
        ArrayList<String> prison = new ArrayList<>(infoConf.getStringList("general"));
        ArrayList<String> free = new ArrayList<>(infoConf.getStringList("general"));
        prison.addAll(infoConf.getStringList("prison"));
        free.addAll(infoConf.getStringList("free"));
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            String msg;
            Random rand = new Random();
            if(player.hasPermission("group.free")) {
                msg = free.get(rand.nextInt(free.size()));
            } else {
                msg = prison.get(rand.nextInt(free.size()));
            }
            String nMsg = msg.replaceAll("%player%", player.getName());
            asConsole(nMsg);
        }
    }

    @Override
    public void onDisable() {
/*        if (discApi != null) {
            // Make sure to disconnect the bot when the plugin gets disabled
            discApi.disconnect();
            discApi = null;
        }*/
        getLogger().info("Disabled SkyPrisonCore v" + getDescription().getVersion());
    }

    public boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String colourMessage(String message) {
        message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }

    public void checkOnlineDailies(Plugin plugin) throws ParseException {
        File f = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
        FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String currDate = formatter.format(date);

        for(Player player : Bukkit.getOnlinePlayers()) {
            String lastDay = dailyConf.getString("players." + player.getUniqueId() + ".last-collected");
            if(!lastDay.equalsIgnoreCase(currDate)) {
                player.sendMessage(colourMessage("&aYou can collect your &l/daily&l!"));
            }
        }
    }

    public void checkDailies(Plugin plugin) throws IOException, ParseException {
        File f = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
        FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
        Set<String> dailyPlayers = dailyConf.getConfigurationSection("players").getKeys(false);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String currDate = formatter.format(date);
        Date newDate = formatter.parse(currDate);

        String lastDay = dailyConf.getString("current-day");

        if(!lastDay.equalsIgnoreCase(currDate)) {
            dailyConf.set("current-day", currDate);
            for(String dPlayer : dailyPlayers) {
                String collectedDay = dailyConf.getString("players." + dPlayer + ".last-collected");
                Date collectedDate = formatter.parse(collectedDay);
                long daysSinceLast = Math.round((newDate.getTime() - collectedDate.getTime()) / (double) 86400000);

                int rewardStreak = dailyConf.getInt("players." + dPlayer + ".current-streak");
                if (daysSinceLast >= 2 && rewardStreak != 1) {
                    dailyConf.set("players." + dPlayer + ".current-streak", 0);
                }
            }
            dailyConf.save(f);
        }
    }

    public void tellConsole(String message){
        Bukkit.getConsoleSender().sendMessage(message);
    }
    public void asConsole(String command){
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public String formatNumber(double value) {
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        return df.format(value);
    }

    public String translateHexColorCodes(String message) {
        if(StringUtils.substringsBetween(message, "{#", "}") != null) {
            String[] hexNames = StringUtils.substringsBetween(message, "{#", "}");
            for (String hexName : hexNames) {
                if (hexColour.get(hexName.toLowerCase()) != null) {
                    message = message.replaceAll(hexName, hexColour.get(hexName.toLowerCase()).substring(1));
                }
            }
        }
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            File f = new File(this.getDataFolder() + File.separator + "dailyreward.yml");
            FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String currDate = formatter.format(date);

            for(Player player : Bukkit.getOnlinePlayers()) {
                String lastDay = dailyConf.getString("players." + player.getUniqueId() + ".last-collected");
                if(lastDay == null || !lastDay.equalsIgnoreCase(currDate)) {
                    player.sendMessage(colourMessage("&aYou can collect your &l/daily&l!"));
                }
            }


            File fData = new File(this.getDataFolder() + File.separator + "firstjoindata.yml");
            FileConfiguration firstJoinConf = YamlConfiguration.loadConfiguration(fData);
            String pUUID = event.getPlayer().getUniqueId().toString();
            if(!firstJoinConf.isConfigurationSection(pUUID)) {
                String firstJoinString = PlaceholderAPI.setPlaceholders(event.getPlayer(), "%player_first_join_date%");
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Date firstJoinDate;
                try {
                    firstJoinDate = sdf.parse(firstJoinString);
                    Long firstJoinMilli = firstJoinDate.getTime();
                    firstJoinConf.set(pUUID + ".firstjoin", firstJoinMilli);
                    firstJoinConf.save(fData);
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                }
            }
            File tokenMine = new File(this.getDataFolder() + File.separator + "blocksmined.yml");
            FileConfiguration mineConf = YamlConfiguration.loadConfiguration(tokenMine);

            if(mineConf.contains(pUUID)) {
                blockBreaks.put(pUUID, mineConf.getInt(pUUID));
            } else {
                blockBreaks.put(pUUID, 0);
            }

            File tData = new File(this.getDataFolder() + File.separator + "tokensdata.yml");
            FileConfiguration tokenConf = YamlConfiguration.loadConfiguration(tData);
            tokensData.put(pUUID, tokenConf.getInt("players." + pUUID));

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            Player player = event.getPlayer();
            com.sk89q.worldedit.util.Location locWE = BukkitAdapter.adapt(player.getLocation());
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                player.setAllowFlight(query.testState(locWE, localPlayer, claimPlugin.FLY));
            }

            if(player.getWorld().getName().equalsIgnoreCase("world_prison") || player.getWorld().getName().equalsIgnoreCase("world_event")) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
            } else {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
            }

        });
    }

    @EventHandler
    public void playerUnjailed(CMIPlayerUnjailEvent event) {
        CMIUser user = event.getUser();
        user.getPlayer().playSound(user.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String pUUID = event.getPlayer().getUniqueId().toString();

            File tokenMine = new File(this.getDataFolder() + File.separator
                    + "blocksmined.yml");
            FileConfiguration mineConf = YamlConfiguration.loadConfiguration(tokenMine);
            mineConf.set(pUUID, blockBreaks.get(pUUID));
            blockBreaks.remove(pUUID);

            File tData = new File(this.getDataFolder() + File.separator + "tokensdata.yml");
            FileConfiguration tokenConf = YamlConfiguration.loadConfiguration(tData);
            tokenConf.set("players." + pUUID, tokensData.get(pUUID));
            tokensData.remove(pUUID);
            try {
                mineConf.save(tokenMine);
                tokenConf.save(tData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @EventHandler
    public void onTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        File ignoreData = new File(this.getDataFolder() + File.separator + "teleportignore.yml");
        FileConfiguration ignoreConf = YamlConfiguration.loadConfiguration(ignoreData);
        Player askedPlayer = event.getWhoAccepts();
        Player askingPlayer = event.getWhoOffers();
        if(ignoreConf.isConfigurationSection(askedPlayer.getUniqueId().toString())) {
            List<?> ignoredPlayers = ignoreConf.getList(askedPlayer.getUniqueId() + ".ignores");
            assert ignoredPlayers != null;
            if(ignoredPlayers.contains(askingPlayer.getUniqueId().toString())) {
                askingPlayer.sendMessage(colourMessage(askedPlayer.displayName() + "&eis ignoring you!"));
                event.setCancelled(true);
            }
        }
    }

    public Map<Player, Map.Entry<Player, Long>> hitcd = new HashMap<>();
    public boolean isGuardGear(ItemStack i) {
        if (i != null) {
            if (i.getType() == Material.CHAINMAIL_HELMET || i.getType() == Material.CHAINMAIL_CHESTPLATE || i.getType() == Material.CHAINMAIL_LEGGINGS || i.getType() == Material.CHAINMAIL_BOOTS || i.getType() == Material.DIAMOND_SWORD) {
                return true;
            } else if (i.getType() == Material.BOW) {
                return i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Guard Bow") && i.getItemMeta().isUnbreakable();
            } else if (i.getType() == Material.SHIELD) {
                return i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Guard Shield") && i.getItemMeta().isUnbreakable();
            }
        }
        return false;
    }

    public void InvGuardGearDelPlyr(Player player) {
        for (int n = 0; n < player.getInventory().getSize(); n++) {
            ItemStack i = player.getInventory().getItem(n);
            if (i != null && isGuardGear(i)) {
                i.setAmount(0);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(event.getPlayer());
        user.getLastBlockLeave();
        if ((!event.canBuild() || event.isCancelled()) && !event.getPlayer().hasPermission("antiblockjump.bypass"))
            event.getPlayer().setVelocity(new Vector(0, -5, 0));
    }

    @EventHandler
    public void guardHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damagee = (Player) event.getEntity();

            if(damager.getWorld().getName().equalsIgnoreCase("world_prison")) {
                Material dmgWpn = damager.getInventory().getItemInMainHand().getType();
                if (dmgWpn.equals(Material.WOODEN_AXE) || dmgWpn.equals(Material.STONE_AXE) || dmgWpn.equals(Material.IRON_AXE) || dmgWpn.equals(Material.DIAMOND_AXE) || dmgWpn.equals(Material.NETHERITE_AXE)) {
                    event.setDamage(event.getDamage() - 4);
                }
            }

            com.sk89q.worldedit.util.Location damagerLoc = BukkitAdapter.adapt(damager.getLocation());
            com.sk89q.worldedit.util.Location damageeLoc = BukkitAdapter.adapt(damagee.getLocation());
            LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer(damager);
            LocalPlayer localDamagee = WorldGuardPlugin.inst().wrapPlayer(damagee);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            if(flyPvP.containsKey(damager.getUniqueId()) && !flyPvP.containsKey(damagee.getUniqueId())) {
                flyPvP.remove(damager.getUniqueId());
            } else if(query.testState(damagerLoc, localDamager, claimPlugin.FLY) && !query.testState(damageeLoc, localDamagee, claimPlugin.FLY)) {
                getServer().getScheduler().runTaskLaterAsynchronously(this, () -> flyPvP.remove(damager.getUniqueId()), 1L);
            }
            if(damager.hasPermission("skyprisoncore.guard.onduty") && damagee.hasPermission("skyprisoncore.guard.onduty")) {
                event.setCancelled(true);
            } else if (damagee.hasPermission("skyprisoncore.showhit")) {
                Map.Entry<Player, Long> lasthit = this.hitcd.get(damager);
                if (hitcd.get(damager) == null || (lasthit.getKey() == damagee && System.currentTimeMillis() / 1000L - lasthit.getValue() > 5L) || lasthit.getKey() !=damagee) {
                    damagee.sendMessage(ChatColor.RED + "You have been hit by " + damager.getName());
                    hitcd.put(damager, new AbstractMap.SimpleEntry<>(damagee, System.currentTimeMillis() / 1000L));
                }
            }
        } else if(event.getDamager() instanceof Projectile) {
            Projectile pArrow = (Projectile) event.getDamager();
            if(pArrow.getShooter() instanceof Player && event.getEntity() instanceof Player) {
                Player damager = (Player) pArrow.getShooter();
                Player damagee = (Player) event.getEntity();
                com.sk89q.worldedit.util.Location damagerLoc = BukkitAdapter.adapt(damager.getLocation());
                com.sk89q.worldedit.util.Location damageeLoc = BukkitAdapter.adapt(damagee.getLocation());
                LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer(damager);
                LocalPlayer localDamagee = WorldGuardPlugin.inst().wrapPlayer(damagee);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                if(flyPvP.containsKey(damager.getUniqueId()) && !flyPvP.containsKey(damagee.getUniqueId())) {
                    flyPvP.remove(damager.getUniqueId());
                } else if(query.testState(damagerLoc, localDamager, claimPlugin.FLY) && !query.testState(damageeLoc, localDamagee, claimPlugin.FLY)) {
                    getServer().getScheduler().runTaskLaterAsynchronously(this, () -> flyPvP.remove(damager.getUniqueId()), 1L);
                }
                if (damager.hasPermission("skyprisoncore.guard.onduty") && damagee.hasPermission("skyprisoncore.guard.onduty")) {
                    event.setCancelled(true);
                } else if (damagee.hasPermission("skyprisoncore.showhit")) {
                    Map.Entry<Player, Long> lasthit = this.hitcd.get(damager);
                    if (hitcd.get(damager) == null || (lasthit.getKey() == damagee && System.currentTimeMillis() / 1000L - lasthit.getValue() > 5L) || lasthit.getKey() != damagee) {
                        damagee.sendMessage(ChatColor.RED + "You have been shot by " + damager.getName());
                        hitcd.put(damager, new AbstractMap.SimpleEntry<>(damagee, System.currentTimeMillis() / 1000L));
                    }
                }
            }
        }
    }

    @EventHandler
    public void shopMinPrice(ShopCreateEvent event) {
        Material itemMaterial = event.getShop().getItem().getType();
        if(minPrice.containsKey(itemMaterial)) {
            double minItemPrice = minPrice.get(itemMaterial);
            double setPrice = event.getShop().getPrice();
            if(setPrice < minItemPrice) {
                Bukkit.getPlayer(event.getCreator()).sendMessage(colourMessage("&cMinimum price for this item is $" + minItemPrice + "!"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void shopBanned(ShopPurchaseEvent event) {
        File f = new File(this.getDataFolder() + File.separator + "shopban.yml");
        FileConfiguration shopConf = YamlConfiguration.loadConfiguration(f);
        if(shopConf.isConfigurationSection(event.getShop().getOwner().toString())) {
            ArrayList<String> bannedPlayers = (ArrayList<String>) shopConf.getStringList(event.getShop().getOwner() + ".banned-players");
            if(bannedPlayers.contains(event.getPurchaser().toString())) {
                Bukkit.getPlayer(event.getPurchaser()).sendMessage(colourMessage("&cThis player has banned you from their shops!"));
                event.setCancelled(true);
            }
        }
    }



    @EventHandler
    public void flightDeath(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
            Location toLoc = player.getLocation();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            final ApplicableRegionSet regionListTo = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                    toLoc.getBlockY(), toLoc.getBlockZ()));
            boolean flyFalse = true;
            for (ProtectedRegion region : regionListTo) {
                if (region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                    flyFalse = false;
                    player.setAllowFlight(true);
                    break;
                }
            }
            if (flyFalse) {
                if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                    player.setAllowFlight(false);
                }
            }
        }
    }

    @EventHandler
    public void flightTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PvPManager pvpmanager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        PlayerHandler playerHandler = Objects.requireNonNull(pvpmanager).getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
        Location toLoc = event.getTo();
        Location fromLoc = event.getFrom();
/*        if(toLoc.getWorld() != fromLoc.getWorld()) {
            if(fromLoc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
            } else if (toLoc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1000);
            }
        }*/

        if(!pvpPlayer.isInCombat() && !event.isCancelled() && !player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionsTo = container.get(BukkitAdapter.adapt(toLoc.getWorld()));
            RegionManager regionsFrom = container.get(BukkitAdapter.adapt(fromLoc.getWorld()));
            final ApplicableRegionSet regionListTo = Objects.requireNonNull(regionsTo).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                    toLoc.getBlockY(), toLoc.getBlockZ()));
            final ApplicableRegionSet regionListFrom = Objects.requireNonNull(regionsFrom).getApplicableRegions(BlockVector3.at(fromLoc.getBlockX(),
                    fromLoc.getBlockY(), fromLoc.getBlockZ()));

            com.sk89q.worldedit.util.Location fromLocWE = BukkitAdapter.adapt(event.getFrom());
            com.sk89q.worldedit.util.Location toLocWE = BukkitAdapter.adapt(toLoc);
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();

            if(!regionListTo.getRegions().isEmpty() && !regionListFrom.getRegions().isEmpty()) {
                ProtectedRegion toRegion = null;
                for (final ProtectedRegion rg : regionListTo) {
                    if(toRegion == null)
                        toRegion = rg;
                    if(rg.getPriority() > toRegion.getPriority()) {
                        toRegion = rg;
                    }
                }
                ProtectedRegion fromRegion = null;
                for (final ProtectedRegion rg : regionListFrom) {
                    if(fromRegion == null)
                        fromRegion = rg;
                    if(rg.getPriority() > fromRegion.getPriority()) {
                        fromRegion = rg;
                    }
                }
                if (query.testState(toLocWE, localPlayer, claimPlugin.FLY) || (toRegion.getId().contains("fly") && !toRegion.getId().contains("nofly") && !toRegion.getId().contains("no-fly"))) {
                    getServer().getScheduler().runTaskLaterAsynchronously(this, () -> player.setAllowFlight(true), 1L);
                    if (!query.testState(fromLocWE, localPlayer, claimPlugin.FLY)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can fly now!");
                    }
                } else if (query.testState(fromLocWE, localPlayer, claimPlugin.FLY) || (fromRegion.getId().contains("fly") && !fromRegion.getId().contains("nofly") && !fromRegion.getId().contains("no-fly"))) {
                    getServer().getScheduler().runTaskLaterAsynchronously(this, () -> player.setAllowFlight(false), 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                    }
                }
            } else {
                if (query.testState(toLocWE, localPlayer, claimPlugin.FLY)) {
                    getServer().getScheduler().runTaskLaterAsynchronously(this, () -> player.setAllowFlight(true), 1L);
                    if (!query.testState(fromLocWE, localPlayer, claimPlugin.FLY)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can fly now!");
                    }
                } else if (query.testState(fromLocWE, localPlayer, claimPlugin.FLY)) {
                    getServer().getScheduler().runTaskLaterAsynchronously(this, () -> player.setAllowFlight(false), 1L);
                    if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                    }
                }
                if(!regionListFrom.getRegions().isEmpty()) {
                    ProtectedRegion fromRegion = null;
                    for (final ProtectedRegion rg : regionListFrom) {
                        if(fromRegion == null)
                            fromRegion = rg;
                        if(rg.getPriority() > fromRegion.getPriority()) {
                            fromRegion = rg;
                        }
                    }
                    if (query.testState(fromLocWE, localPlayer, claimPlugin.FLY) || (fromRegion.getId().contains("fly") && !fromRegion.getId().contains("nofly") && !fromRegion.getId().contains("no-fly"))) {
                        getServer().getScheduler().runTaskLaterAsynchronously(this, () -> player.setAllowFlight(false), 1L);
                        if (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                        }
                    }
                }
            }
        }
    }

/*    @EventHandler
    public void chatToDiscord(AsyncChatEvent event) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(event.getPlayer());
        new MessageBuilder()
                .append("**" + user.getGroupName() + "** " +  event.getPlayer().name() + " » " + event.message())
                .send(discApi.getTextChannelById("788108242797854751").get());
    }*/


    @EventHandler
    public void worldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fWorld = event.getFrom();
        World tWorld = player.getWorld();
        if(tWorld.getName().equalsIgnoreCase("world_prison") || tWorld.getName().equalsIgnoreCase("world_event")) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
        } else if(fWorld.getName().equalsIgnoreCase("world_prison")) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
        }
    }

    @EventHandler
    public void fuckOffKiyan(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if(player.getName().equalsIgnoreCase("Kiyan")) {
            String cmd = event.getMessage();
            if(cmd.toLowerCase().startsWith("/cmi msg drakepork")) {
                asConsole("ctellraw " + player.getName() +
                        " <T>{#scarpaflow}[{#redviolet}me {#scarpaflow}» " +
                        "{#redviolet}DrakePork{#scarpaflow}] {#melrose}" + cmd.substring(18) + "</T>");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onUnTag(PlayerUntagEvent event) {
        Player player = event.getPlayer();
        com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(player.getLocation());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (query.testState(toLoc, localPlayer, claimPlugin.FLY)) {
            player.setAllowFlight(true);
        }
    }

    @EventHandler
    public void onTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        com.sk89q.worldedit.util.Location toLoc = BukkitAdapter.adapt(player.getLocation());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        if (query.testState(toLoc, localPlayer, claimPlugin.FLY)) {
            flyPvP.put(player.getUniqueId(), true);
        }
    }

    @EventHandler
    public void preShopSell(ShopPreTransactionEvent event) {
        if(event.getShopAction().equals(ShopManager.ShopAction.SELL_ALL)) {
            Player player = event.getPlayer();
            File f = new File(this.getDataFolder() + File.separator + "blocksells.yml");
            FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
            if(yamlf.isConfigurationSection(player.getUniqueId().toString())) {
                String iName = event.getShopItem().getItem().getType().name();
                ArrayList blockedSales = (ArrayList) yamlf.getList(player.getUniqueId() + ".blocked");
                if(Objects.requireNonNull(blockedSales).contains(iName)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onShopSell(ShopPostTransactionEvent event) throws IOException {
        if(event.getResult().getShopAction() == ShopManager.ShopAction.SELL
                || event.getResult().getShopAction() == ShopManager.ShopAction.SELL_ALL) {
            Player player = event.getResult().getPlayer();
            File f = new File(this.getDataFolder() + File.separator + "recentsells.yml");
            FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
            if(yamlf.isConfigurationSection(player.getUniqueId().toString())) {
                ArrayList<String> soldItems = (ArrayList<String>) Objects.requireNonNull(yamlf.getStringList(player.getUniqueId() + ".sold-items"));
                soldItems.add(0, event.getResult().getShopItem().getItem().getType()
                        + "/" + event.getResult().getAmount()
                        + "/" + event.getResult().getPrice());
                if(soldItems.size() > 5) {
                    soldItems.remove(5);
                }
                yamlf.set(player.getUniqueId() + ".sold-items", soldItems);
            } else {
                yamlf.set(player.getUniqueId() + ".sold-items", Lists.newArrayList(event.getResult().getShopItem().getItem().getType()
                        + "/" + event.getResult().getAmount()
                        + "/" + event.getResult().getPrice()));
            }
            yamlf.save(f);
        }
    }


    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PvPManager pvpmanager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        PlayerHandler playerHandler = Objects.requireNonNull(pvpmanager).getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
        if(teleportMove.containsKey(player.getUniqueId())) {
            Location toLoc = event.getTo();
            Location fromLoc = event.getFrom();
            if(toLoc.getBlockX() != fromLoc.getBlockX() || toLoc.getBlockZ() != fromLoc.getBlockZ()) {
                getServer().getScheduler().cancelTask(teleportMove.get(player.getUniqueId()));
                teleportMove.remove(player.getUniqueId());
                player.sendMessage(colourMessage("&cTeleport Cancelled!"));
            }
        }
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                Location toLoc = event.getTo();
                Location fromLoc = event.getFrom();
                if(toLoc.getBlockX() != fromLoc.getBlockX() || toLoc.getBlockZ() != fromLoc.getBlockZ()) {
                    boolean toFly = true;
                    boolean fromFly = true;

                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
                    final ApplicableRegionSet regionListTo = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                            toLoc.getBlockY(), toLoc.getBlockZ()));
                    final ApplicableRegionSet regionListFrom = regions.getApplicableRegions(BlockVector3.at(fromLoc.getBlockX(),
                            fromLoc.getBlockY(), fromLoc.getBlockZ()));

                    for (int i = 0; i <= toLoc.getBlockY(); i++) {
                        Location blockCheck = new Location(toLoc.getWorld(), toLoc.getBlockX(), toLoc.getBlockY() - i, toLoc.getBlockZ());
                        Block block = blockCheck.getBlock();
                        if (block.isSolid() && !block.isLiquid()  && !block.isPassable()) {
                            toFly = false;
                            break;
                        }
                    }
                    for (int i = 0; i <= fromLoc.getBlockY(); i++) {
                        Location blockCheck = new Location(fromLoc.getWorld(), fromLoc.getBlockX(), fromLoc.getBlockY() - i, fromLoc.getBlockZ());
                        Block block = blockCheck.getBlock();
                        if (block.isSolid() && !block.isLiquid()  && !block.isPassable()) {
                            fromFly = false;
                            break;
                        }
                    }
                    for (ProtectedRegion region : regionListTo) {
                        if(region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                            toFly = true;
                            break;
                        }
                    }
                    for (ProtectedRegion region : regionListFrom) {
                        if(region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                            fromFly = true;
                            break;
                        }
                    }
                    if (toFly && !fromFly) {
                        if (!pvpPlayer.isInCombat()) {
                            player.setAllowFlight(true);
                            player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can fly now!");
                        }
                    } else if (!toFly && fromFly) {
                        player.setAllowFlight(false);
                        player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You can no longer fly!");
                    }
                }
                if (pvpPlayer.isInCombat()) {
                    if (toLoc.getBlockY() <= 3) {
                        pvpPlayer.unTag();
                    }
                }
            }
        }
        if (player.hasPermission("skyprisoncore.guard.onduty") && !player.isOp()) {
            ArrayList<String> guardWorlds = (ArrayList<String>) config.getStringList("guard-worlds");
            boolean inWorld = false;
            for(String guardWorld : Objects.requireNonNull(guardWorlds)) {
                if(player.getWorld().getName().equalsIgnoreCase(guardWorld)) {
                    inWorld = true;
                    break;
                }
            }
            if(!inWorld) {
                if((event.getFrom().getBlockX() != event.getTo().getBlockX()) || (event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Please go off duty!");
                }
            }
        }
        if(player.hasPermission("skyprisoncore.builder.onduty") && !player.isOp()) {
            ArrayList<String> buildWorlds = (ArrayList<String>) config.getStringList("builder-worlds");
            boolean inWorld = false;
            for(String buildWorld : Objects.requireNonNull(buildWorlds)) {
                if(player.getWorld().getName().equalsIgnoreCase(buildWorld)) {
                    inWorld = true;
                    break;
                }
            }
            if(!inWorld) {
                if((event.getFrom().getBlockX() != event.getTo().getBlockX()) || (event.getFrom().getBlockZ() != event.getTo().getBlockZ())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Please go off duty!");
                }
            }
        }
    }

    @EventHandler
    public void brewConsume(BrewDrinkEvent event) throws IOException {
        Player player = event.getPlayer();
        File brewData = new File(this.getDataFolder() + File.separator + "brewsdrank.yml");
        FileConfiguration brewConf = YamlConfiguration.loadConfiguration(brewData);
        int totalBrews = 0;
        if(brewConf.contains(player.getUniqueId().toString())) {
            totalBrews = brewConf.getInt(player.getUniqueId().toString());
        }
        brewConf.set(player.getUniqueId().toString(), totalBrews + 1);
        brewConf.save(brewData);
    }

    @EventHandler
    public void expBottleConsume(PlayerInteractEvent event) {
        if(event.getItem() != null && event.getItem().getType().equals(Material.EXPERIENCE_BOTTLE) && event.getHand().equals(EquipmentSlot.HAND)) {
            if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                ItemStack expBottle = event.getItem();
                ItemMeta expMeta = expBottle.getItemMeta();
                NamespacedKey key = new NamespacedKey(this, "exp-amount");
                if (expMeta.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                    event.setCancelled(true);
                    int expToGive = expMeta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
                    Player player = event.getPlayer();
                    player.giveExp(expToGive);
                    player.sendMessage(colourMessage("&2&l+" + formatNumber(expToGive)) + " XP");
                    if (expBottle.getAmount() - 1 > 0) {
                        expBottle.setAmount(expBottle.getAmount() - 1);
                    } else {
                        player.getInventory().removeItem(expBottle);
                    }
                }
            }
        }
    }


    // Date ; other user ; withdraw/deposit ; amount ; was Quickshop ; what bought/sold if quickshop

    @EventHandler
    public void marketTransaction(ShopSuccessPurchaseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            UUID purchaser = event.getPurchaser();
            UUID shopOwner = event.getShop().getOwner();
            File f = new File(this.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + purchaser + ".log");
            File f2 = new File(this.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + shopOwner + ".log");
            FileWriter fData = null;
            FileWriter fData2 = null;
            try {
                fData = new FileWriter(f, true);
                fData2 = new FileWriter(f2, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PrintWriter pData = new PrintWriter(fData);
            PrintWriter pData2 = new PrintWriter(fData2);

            Date date = new Date();
            SimpleDateFormat DateFor = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            String stringDate = DateFor.format(date);

            if (event.getShop().isBuying()) {
                pData.println(stringDate + ";" + shopOwner + ";deposit;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
                pData2.println(stringDate + ";" + purchaser + ";withdraw;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
            } else {
                pData.println(stringDate + ";" + shopOwner + ";withdraw;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
                pData2.println(stringDate + ";" + purchaser + ";deposit;" + event.getBalance() + ";true;" + event.getShop().getItem() + ";" + event.getAmount());
            }

            pData.flush();
            pData.close();
            pData2.flush();
            pData2.close();
        });
    }

    @EventHandler
    public void cmiTransaction(CMIUserBalanceChangeEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            CMIUser toUser = event.getUser();
            CMIUser fromUser = event.getSource();
            if (toUser != null && fromUser != null) {
                File f = new File(this.getDataFolder() + File.separator + "logs" + File.separator + "transactions" + File.separator + toUser.getUniqueId() + ".log");
                FileWriter fData = null;
                try {
                    fData = new FileWriter(f, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PrintWriter pData = new PrintWriter(fData);

                Date date = new Date();
                SimpleDateFormat DateFor = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                String stringDate = DateFor.format(date);
                double amount = event.getTo() - event.getFrom();
                if (event.getActionType().equalsIgnoreCase("withdraw")) {
                    amount = event.getFrom() - event.getTo();
                }
                pData.println(stringDate + ";" + fromUser.getUniqueId() + ";" + event.getActionType().toLowerCase() + ";" + amount + ";false;null;null");

                pData.flush();
                pData.close();
            }
        });
    }

    @EventHandler
    public void marketUnrent(UnsellRegionEvent event) {
        if (event.getRegion().getRegionworld().getName().equalsIgnoreCase("world_skycity")) {
            World world = event.getRegion().getRegionworld();
            Location locMax = event.getRegion().getRegion().getMaxPoint().toLocation(world);
            Location locMin = event.getRegion().getRegion().getMinPoint().toLocation(world);
            Plugin quickshopPlugin = Bukkit.getPluginManager().getPlugin("QuickShop");
            if (quickshopPlugin != null && quickshopPlugin.isEnabled()) {
                QuickShopAPI quickshopApi = (QuickShopAPI) quickshopPlugin;
                for(Shop shop : quickshopApi.getShopManager().getPlayerAllShops(event.getRegion().getOwner())) {
                    Location shopLoc = shop.getLocation();
                    if ((shopLoc.getBlockX() >= locMax.getBlockX() && shopLoc.getBlockX() <= locMin.getBlockX()) || (shopLoc.getBlockX() <= locMax.getBlockX() && shopLoc.getBlockX() >= locMin.getBlockX())) {
                        if ((shopLoc.getBlockZ() >= locMax.getBlockZ() && shopLoc.getBlockZ() <= locMin.getBlockZ()) || (shopLoc.getBlockZ() <= locMax.getBlockZ() && shopLoc.getBlockZ() >= locMin.getBlockZ())) {
                            if ((shopLoc.getBlockY() >= locMax.getBlockY() && shopLoc.getBlockY() <= locMin.getBlockY()) || (shopLoc.getBlockY() <= locMax.getBlockY() && shopLoc.getBlockY() >= locMin.getBlockY())) {
                                shop.delete();
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void invClick(InventoryClickEvent event) throws IOException {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            user.getCMIPlayTime().getPlayDayOfToday().getTotalTime();
            Inventory clickInv = event.getClickedInventory();
            if (clickInv != null && !clickInv.isEmpty() && clickInv.getItem(0) != null) {
                ItemStack fItem = clickInv.getItem(0);
                ItemMeta fMeta = Objects.requireNonNull(fItem).getItemMeta();
                PersistentDataContainer fData = fMeta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(this, "stop-click");
                NamespacedKey key1 = new NamespacedKey(this, "gui-type");
                if (fData.has(key, PersistentDataType.INTEGER) && fData.has(key1, PersistentDataType.STRING)) {
                    if(clickInv.getItem(event.getSlot()) == null) {
                        event.setCancelled(true);
                    }
                    int clickCheck = fData.get(key, PersistentDataType.INTEGER);
                    String guiType = fData.get(key1, PersistentDataType.STRING);
                    if (clickCheck == 1) {
                        event.setCancelled(true);
                        switch (Objects.requireNonNull(guiType)) {
                            case "bartender-grass":
                                File f = new File(this.getDataFolder() + File.separator + "bartender.yml");
                                FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                                ItemStack alc = event.getCurrentItem();
                                ItemMeta alcMeta = Objects.requireNonNull(alc).getItemMeta();
                                PersistentDataContainer alcData = alcMeta.getPersistentDataContainer();
                                NamespacedKey alcKey = new NamespacedKey(this, "alc-type");
                                if(alcData.has(alcKey, PersistentDataType.STRING)) {
                                    String alcType = alcData.get(alcKey, PersistentDataType.STRING);
                                    int price = yamlf.getInt("grass." + alcType + ".price");
                                    if(user.getBalance() >= price) {
                                        if (user.getInventory().getFreeSlots() != 0) {
                                            if (!alc.getType().equals(Material.MILK_BUCKET)) {
                                                int quality = yamlf.getInt("grass." + alcType + ".quality");
                                                String type = yamlf.getString("grass." + alcType + ".type");
                                                player.sendMessage(colourMessage("&f[{#green}Bartender&f] &eYou bought " + type + "!"));
                                                asConsole("brew create " + type + " " + quality + " " + player.getName());
                                            } else {
                                                player.sendMessage(colourMessage("&f[{#green}Bartender&f] &eYou bought Milk!"));
                                                asConsole("give " + player.getName() + " milk_bucket");
                                            }
                                            asConsole("money take " + player.getName() + " " + price);
                                            // Bartender.openGUI(player, "bartender-grass");
                                        } else {
                                            player.sendMessage(colourMessage("&cYou do not have enough space in your inventory!"));
                                        }
                                    } else {
                                        player.sendMessage(colourMessage("&cYou do not have enough money!"));
                                    }
                                }
                                break;
                            case "buyback":
                                NamespacedKey typeKey = new NamespacedKey(this, "sold-type");
                                ItemStack buyItem = event.getCurrentItem();
                                if(buyItem != null) {
                                    ItemMeta buyMeta = buyItem.getItemMeta();
                                    PersistentDataContainer buyData = buyMeta.getPersistentDataContainer();
                                    if(buyData.has(typeKey, PersistentDataType.STRING)) {
                                        NamespacedKey amKey = new NamespacedKey(this, "sold-amount");
                                        NamespacedKey priKey = new NamespacedKey(this, "sold-price");
                                        String itemType = buyData.get(typeKey, PersistentDataType.STRING);
                                        int itemAmount = buyData.get(amKey, PersistentDataType.INTEGER);
                                        Double itemPrice = buyData.get(priKey, PersistentDataType.DOUBLE);
                                        ItemStack iSold = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(itemType))), itemAmount);
                                        if(user.getInventory().canFit(iSold)) {
                                            if(user.getBalance() >= itemPrice) {
                                                File buyFile = new File(this.getDataFolder() + File.separator + "recentsells.yml");
                                                FileConfiguration buyConf = YamlConfiguration.loadConfiguration(buyFile);
                                                ArrayList<String> soldItems = (ArrayList<String>) Objects.requireNonNull(buyConf.getStringList(player.getUniqueId() + ".sold-items"));
                                                NamespacedKey posKey = new NamespacedKey(this, "sold-pos");
                                                int buyPos = buyData.get(posKey, PersistentDataType.INTEGER);
                                                soldItems.remove(buyPos);
                                                buyConf.set(player.getUniqueId() + ".sold-items", soldItems);
                                                buyConf.save(buyFile);
                                                asConsole("give " + player.getName() + " " + itemType + " " + itemAmount);
                                                asConsole("money take " + player.getName() + " " + itemPrice);
                                                BuyBack.openGUI(player);
                                            } else {
                                                player.sendMessage(colourMessage("&cYou do not have enough money!"));
                                            }
                                        } else {
                                            player.sendMessage(colourMessage("&cYou do not have enough space in your inventory!"));
                                        }
                                    }
                                }
                                break;
                            case "netheriteupgrade":
                                if(event.getSlot() == 11) {
                                    ItemStack pMain = player.getInventory().getItemInMainHand();
                                    if(pMain.getType() != Material.AIR) {
                                        if (clickInv.getItem(event.getSlot()).getType() == Material.GREEN_CONCRETE) {
                                            asConsole("cmi money take " + user.getName() + " 500000");
                                            pMain.setRepairCost(0);
                                            player.sendMessage(colourMessage("&f[&aBlacksmith&f] &7Your &3" + clickInv.getItem(13).getType() + " &7has had its repair cost reset for &a$500,000&7!"));
                                            player.closeInventory();
                                        } else {
                                            player.sendMessage(colourMessage("&cYou can't afford this!"));
                                        }
                                    } else {
                                        player.closeInventory();
                                        player.sendMessage(colourMessage("&f[&aBlacksmith&f] &cYou are not holding anything in your hand!"));
                                    }
                                } else if(event.getSlot() == 15) {
                                    player.closeInventory();
                                }
                                break;
                            case "endupgrade":
                                ItemStack clickedItem = clickInv.getItem(event.getSlot());
                                if(event.getSlot() == 20) {
                                    ItemStack repItem = clickInv.getItem(24);
                                    PersistentDataContainer repData = Objects.requireNonNull(repItem).getPersistentDataContainer();
                                    NamespacedKey repKey = new NamespacedKey(this, "repair-state");
                                    int repCheck = repData.get(repKey, PersistentDataType.INTEGER);

                                    PersistentDataContainer clickData = Objects.requireNonNull(clickedItem).getPersistentDataContainer();
                                    NamespacedKey enchKey = new NamespacedKey(this, "ench-state");
                                    int enchCheck = clickData.get(enchKey, PersistentDataType.INTEGER);
                                    if (enchCheck != 1) {
                                        EndUpgrade.openGUI(player, true, repCheck == 1);
                                    } else {
                                        EndUpgrade.openGUI(player, false, repCheck == 1);
                                    }
                                } else if(event.getSlot() == 24) {
                                    ItemStack enchItem = clickInv.getItem(20);
                                    PersistentDataContainer enchData = Objects.requireNonNull(enchItem).getPersistentDataContainer();
                                    NamespacedKey enchKey = new NamespacedKey(this, "ench-state");
                                    int enchCheck = enchData.get(enchKey, PersistentDataType.INTEGER);

                                    PersistentDataContainer clickData = Objects.requireNonNull(clickedItem).getPersistentDataContainer();
                                    NamespacedKey repKey = new NamespacedKey(this, "repair-state");
                                    int repCheck = clickData.get(repKey, PersistentDataType.INTEGER);
                                    if (repCheck != 1) {
                                        EndUpgrade.openGUI(player, enchCheck == 1, true);
                                    } else {
                                        EndUpgrade.openGUI(player, enchCheck == 1, false);
                                    }
                                }  else if(event.getSlot() == 31) {
                                    ItemStack pMain = player.getInventory().getItemInMainHand();
                                    if(pMain.getType() != Material.AIR) {
                                        if (clickInv.getItem(event.getSlot()).getType() == Material.GREEN_CONCRETE) {
                                            ItemStack enchState = clickInv.getItem(20);
                                            assert enchState != null;
                                            PersistentDataContainer enchData = enchState.getPersistentDataContainer();
                                            NamespacedKey enchKey = new NamespacedKey(this, "ench-state");
                                            int enchCheck = enchData.get(enchKey, PersistentDataType.INTEGER);

                                            ItemStack repItem = clickInv.getItem(24);
                                            PersistentDataContainer repData = Objects.requireNonNull(repItem).getPersistentDataContainer();
                                            NamespacedKey repKey = new NamespacedKey(this, "repair-state");
                                            int repCheck = repData.get(repKey, PersistentDataType.INTEGER);

                                            EndUpgrade.confirmGUI(player, enchCheck == 1, repCheck == 1);
                                        } else {
                                            player.sendMessage(colourMessage("&cYou can't afford this!"));
                                        }
                                    } else {
                                        player.closeInventory();
                                        player.sendMessage(colourMessage("&f[&aBlacksmith&f] &cYou are not holding anything in your hand!"));
                                    }
                                }
                                break;
                            case "confirm-endupgrade":
                                if(event.getSlot() == 11) {
                                    ItemStack pMain = player.getInventory().getItemInMainHand();
                                    if (pMain.getType() != Material.AIR) {
                                        ItemStack confirmItem = clickInv.getItem(11);
                                        PersistentDataContainer confirmData = Objects.requireNonNull(confirmItem).getPersistentDataContainer();
                                        NamespacedKey enchKey = new NamespacedKey(this, "ench-state");
                                        int enchCheck = confirmData.get(enchKey, PersistentDataType.INTEGER);

                                        NamespacedKey repKey = new NamespacedKey(this, "repair-state");
                                        int repCheck = confirmData.get(repKey, PersistentDataType.INTEGER);

                                        int cost = EndUpgrade.upgradeCost(player, enchCheck == 1, repCheck == 1);

                                        switch (pMain.getType().toString()) {
                                            case "DIAMOND_AXE":
                                                pMain.setType(Material.NETHERITE_AXE);
                                                break;
                                            case "DIAMOND_PICKAXE":
                                                pMain.setType(Material.NETHERITE_PICKAXE);
                                                break;
                                            case "DIAMOND_SHOVEL":
                                                pMain.setType(Material.NETHERITE_SHOVEL);
                                                break;
                                            case "DIAMOND_HOE":
                                                pMain.setType(Material.NETHERITE_HOE);
                                                break;
                                            case "DIAMOND_HELMET":
                                                pMain.setType(Material.NETHERITE_HELMET);
                                                break;
                                            case "DIAMOND_CHESTPLATE":
                                                pMain.setType(Material.NETHERITE_CHESTPLATE);
                                                break;
                                            case "DIAMOND_LEGGINGS":
                                                pMain.setType(Material.NETHERITE_LEGGINGS);
                                                break;
                                            case "DIAMOND_BOOTS":
                                                pMain.setType(Material.NETHERITE_BOOTS);
                                                break;
                                        }
                                        if (enchCheck != 1) {
                                            if (pMain.hasEnchants()) {
                                                for (Enchantment ench : pMain.getEnchants().keySet()) {
                                                    pMain.removeEnchant(ench);
                                                }
                                            }
                                        }
                                        if (repCheck == 1) {
                                            pMain.setRepairCost(0);
                                        }

                                        if(!player.hasPermission("skyprisoncore.command.endupgrade.first-time")) {
                                            asConsole("money take " + player.getName() + " " + cost);
                                            player.sendMessage(colourMessage("&f[&aBlacksmith&f] &7Your &3" + clickInv.getItem(4).getType() + " &7has been upgraded for &a$" + formatNumber(cost) + "&7!"));
                                        } else {
                                            asConsole("lp user " + player.getName() + " permission unset skyprisoncore.command.endupgrade.first-time");
                                            player.sendMessage(colourMessage("&f[&aBlacksmith&f] &7Your &3" + clickInv.getItem(4).getType() + " &7has been upgraded!"));

                                        }
                                        player.closeInventory();
                                    }
                                } else if (event.getSlot() == 15) {
                                    player.closeInventory();
                                }
                                break;
                            case "transaction-history":
                                Material clickedMat = Objects.requireNonNull(event.getClickedInventory().getItem(event.getSlot())).getType();

                                NamespacedKey tKey = new NamespacedKey(this, "sort");
                                NamespacedKey tKey1 = new NamespacedKey(this, "toggle");
                                NamespacedKey tKey2 = new NamespacedKey(this, "page");
                                Boolean transSort = Boolean.parseBoolean(fData.get(tKey, PersistentDataType.STRING));
                                String transToggle = fData.get(tKey1, PersistentDataType.STRING);
                                int transPage = fData.get(tKey2, PersistentDataType.INTEGER);
                                if(clickedMat.equals(Material.PAPER)) {
                                    if (event.getSlot() == 45) {
                                        moneyHistory.openGUI(player, transSort, transToggle, transPage-1);
                                    } else if (event.getSlot() == 53) {
                                        moneyHistory.openGUI(player, transSort, transToggle, transPage+1);
                                    }
                                } else if(clickedMat.equals(Material.CLOCK)) {
                                    if(transSort)
                                        moneyHistory.openGUI(player, false, transToggle, transPage);
                                    else
                                        moneyHistory.openGUI(player, true, transToggle, transPage);
                                } else if (clickedMat.equals(Material.COMPASS)) {
                                    if(transToggle.equalsIgnoreCase("null")) {
                                        moneyHistory.openGUI(player, transSort, "true", 1);
                                    } else if(transToggle.equalsIgnoreCase("true")) {
                                        moneyHistory.openGUI(player, transSort, "false", 1);
                                    } else if(transToggle.equalsIgnoreCase("false")) {
                                        moneyHistory.openGUI(player, transSort, "null", 1);

                                    }

                                }
                                break;
                            case "daily-reward":
                                if(event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.MINECART)) {
                                    player.sendMessage(colourMessage("&cYou've already collected the daily reward!"));
                                } else if(event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.CHEST_MINECART)) {
                                    File dailyFile = new File(this.getDataFolder() + File.separator + "dailyreward.yml");
                                    FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(dailyFile);
                                    int currStreak = dailyConf.getInt("players." + player.getUniqueId() + ".current-streak");
                                    int highestStreak = dailyConf.getInt("players." + player.getUniqueId() + ".highest-streak");
                                    int totalCollected = dailyConf.getInt("players." + player.getUniqueId() + ".total-collected");

                                    Random rand = new Random();
                                    int tReward = rand.nextInt(25) + 25;

                                    if(currStreak + 1 % 7 == 0) {
                                        tReward = 250;
                                    }

                                    Random rand2 = new Random();
                                    int randInt = rand2.nextInt(1000) + 1;
                                    if(randInt == 666) {
                                        tReward = randInt;
                                    }

                                    tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), tReward);
                                    dailyConf.set("players." + player.getUniqueId() + ".current-streak", currStreak + 1);
                                    dailyConf.set("players." + player.getUniqueId() + ".total-streak", totalCollected + 1);

                                    if(currStreak >= highestStreak) {
                                        dailyConf.set("players." + player.getUniqueId() + ".highest-streak", currStreak + 1);
                                    }

                                    Date date = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    String currDate = formatter.format(date);

                                    dailyConf.set("players." + player.getUniqueId() + ".last-collected", currDate);
                                    dailyConf.save(dailyFile);
                                    daily.openGUI(player);
                                }
                                break;
                            case "plotteleport":
                                ItemStack itemClick = clickInv.getItem(event.getSlot());
                                PersistentDataContainer plotData = Objects.requireNonNull(itemClick).getPersistentDataContainer();
                                NamespacedKey plotKey = new NamespacedKey(this, "x");
                                if(plotData.has(plotKey, PersistentDataType.DOUBLE)) {
                                    NamespacedKey plotKey1 = new NamespacedKey(this, "y");
                                    NamespacedKey plotKey2 = new NamespacedKey(this, "z");
                                    NamespacedKey plotKey3 = new NamespacedKey(this, "world");
                                    double x = plotData.get(plotKey, PersistentDataType.DOUBLE);
                                    double y = plotData.get(plotKey1, PersistentDataType.DOUBLE);
                                    double z = plotData.get(plotKey2, PersistentDataType.DOUBLE);
                                    World world = Bukkit.getWorld(plotData.get(plotKey3, PersistentDataType.STRING));
                                    Location loc = new Location(world, x, y, z);
                                    if(player.getWorld().getName().equalsIgnoreCase("world_skycity") || player.hasPermission("cmi.command.tpa.warmupbypass")) {
                                        player.teleportAsync(loc);
                                        player.sendMessage(colourMessage("&aTeleported to plot!"));
                                    } else {
                                        player.closeInventory();
                                        player.sendMessage(colourMessage("&aTeleporting to your plot in 5 seconds, Don't move!"));
                                        BukkitTask task = getServer().getScheduler().runTaskLater(this, () -> {
                                            teleportMove.remove(player.getUniqueId());
                                            player.teleport(loc);
                                            player.sendMessage(colourMessage("&aTeleported to plot!"));
                                        }, 100L);
                                        teleportMove.put(player.getUniqueId(), task.getTaskId());

                                    }
                                }
                                break;
                        }
                    }
                }
            }
        }
        String[] pageCheck = ChatColor.stripColor(event.getView().getTitle()).split(" ");
        if (ChatColor.stripColor(event.getView().getTitle()).contains("Secrets")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
            }
            String[] title = event.getView().getTitle().split(" - ");
            HumanEntity human = event.getWhoClicked();
            if (human instanceof Player) {
                switch (title[1].toLowerCase()) {
                    case "grass":
                    case "desert":
                    case "nether":
                    case "snow":
                    case "skycity":
                    case "marina":
                    case "skycity-other":
                    case "prison-other":
                        if (event.getSlot() == 40) {
                            SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                        }
                        break;
                    case "all":
                        switch (event.getSlot()) {
                            case 31:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                                break;
                            case 11:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "grass");
                                break;
                            case 12:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "desert");
                                break;
                            case 13:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "nether");
                                break;
                            case 14:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "snow");
                                break;
                            case 15:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "prison-other");
                                break;
                            case 22:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "skycity");
                                break;
                        }
                        break;
                    case "rewards":
                        if (event.getCurrentItem() == null) {
                            break;
                        }
                        if (event.getSlot() == 49) {
                            SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                        } else if (event.getCurrentItem().getType().equals(Material.CHEST_MINECART)) {
                            Player player = Bukkit.getPlayer(human.getName());
                            assert player != null;
                            File rewardsDataFile = new File(this.getDataFolder() + File.separator
                                    + "rewardsdata.yml");
                            FileConfiguration rData = YamlConfiguration.loadConfiguration(rewardsDataFile);
                            File secretsDataFile = new File(this.getDataFolder() + File.separator
                                    + "secretsdata.yml");
                            FileConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
                            ItemStack currItem = event.getCurrentItem();
                            NamespacedKey key = new NamespacedKey(this, "reward");
                            ItemMeta itemMeta = currItem.getItemMeta();
                            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                            String foundValue;
                            if(container.has(key, PersistentDataType.STRING)) {
                                foundValue = container.get(key, PersistentDataType.STRING);
                                if(Objects.requireNonNull(rData.getString(foundValue + ".reward-type")).equalsIgnoreCase("points")) {
                                    int pointAmount = rData.getInt(foundValue + ".reward");
                                    VotingPluginUser user = UserManager.getInstance().getVotingPluginUser(player);
                                    Bukkit.getScheduler().runTaskAsynchronously(this, () -> user.addPoints(pointAmount));
                                    pData.set(player.getUniqueId() + ".rewards." + foundValue + ".collected", true);
                                    try {
                                        pData.save(secretsDataFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage(colourMessage("&f[&eSecrets&f] &aYou received " + pointAmount + " points!"));
                                    SecretsGUI.openGUI(player, "rewards");
                                } else if(Objects.requireNonNull(rData.getString(foundValue + ".reward-type")).equalsIgnoreCase("tokens")) {
                                    int tokenAmount = rData.getInt(foundValue + ".reward");
                                    tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), tokenAmount);
                                    pData.set(player.getUniqueId() + ".rewards." + foundValue + ".collected", true);
                                    try {
                                        pData.save(secretsDataFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    player.sendMessage(colourMessage("&f[&eSecrets&f] &aYou received " + tokenAmount + " tokens!"));
                                    SecretsGUI.openGUI(player, "rewards");
                                }
                            }
                        }
                        break;
                    case "main":
                        switch(event.getSlot()) {
                            case 13:
                                break;
                            case 20:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                                break;
                            case 24:
                                SecretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "rewards");
                                break;
                        }
                }
            }
        } else if (pageCheck[0].equalsIgnoreCase("Shop") && pageCheck[1].equalsIgnoreCase("Log")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(pageCheck[4])-1;
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "default");
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(pageCheck[4])+1;
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "default");
                    }
                } else if(event.getCurrentItem().getType() == Material.BOOK) {
                    if(event.getSlot() == 47) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "amounttop");
                    } else if(event.getSlot() == 48) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "amountbottom");
                    } else if(event.getSlot() == 49) {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(ChatColor.RED + "/econcheck player <player>");
                    } else if(event.getSlot() == 50) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "moneybottom");
                    } else if(event.getSlot() == 51) {
                        int page = Integer.parseInt(pageCheck[4]);
                        EconomyCheck.openGUI((Player) event.getWhoClicked(), page, "moneytop");
                    }
                }
            }
        }
        String[] dropChest = ChatColor.stripColor(event.getView().getTitle()).split(" ");
        if(dropChest[0].equalsIgnoreCase("Drop") && dropChest[1].equalsIgnoreCase("Party")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(dropChest[4])-1;
                        DropChest.openGUI((Player) event.getWhoClicked(), page);
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(dropChest[4])+1;
                        DropChest.openGUI((Player) event.getWhoClicked(), page);
                    }
                }
            }
        }

        if (ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("bounties")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(dropChest[4])-1;
                        Bounty.openGUI((Player) event.getWhoClicked(), page);
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(dropChest[4])+1;
                        Bounty.openGUI((Player) event.getWhoClicked(), page);
                    }
                }
            }
        }
        if (ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Referral List")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
            }
        }

        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
            if(event.getClickedInventory() instanceof PlayerInventory) {
                InvGuardGearDelPlyr(player);
            }
        }
    }

    @EventHandler
    public void pickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
                if (isGuardGear(event.getItem().getItemStack())) {
                    event.setCancelled(true);
                }
                InvGuardGearDelPlyr(player);
            }
        }
    }

    //
    // EventHandlers regarding DropParty Chest
    //

    @EventHandler
    public void voidFall(EntityRemoveFromWorldEvent event) {
        if (event.getEntity().getLocation().getY() < -63) {
            if(event.getEntity().getWorld().getName().equalsIgnoreCase("world_prison")) {
                if (event.getEntityType() == EntityType.DROPPED_ITEM) {
                    Item item = (Item) event.getEntity();
                    ItemStack sItem = item.getItemStack();
                    File f = new File(this.getDataFolder() + File.separator + "dropchest.yml");
                    FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                    if (!yamlf.isConfigurationSection("items")) {
                        yamlf.createSection("items");
                    }
                    try {
                        yamlf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Set<String> dropList = Objects.requireNonNull(yamlf.getConfigurationSection("items")).getKeys(false);
                    int page = 0;
                    for (int i = 0; i < dropList.size() + 2; ) {
                        ArrayList<String> arr = new ArrayList<>();
                        for (String dropItem : dropList) {
                            if (yamlf.getInt("items." + dropItem + ".page") == i) {
                                arr.add(dropItem);
                            }
                        }
                        if (arr.size() <= 44) {
                            page = i;
                            break;
                        } else {
                            i++;
                        }
                    }
                    for (int i = 0; i < dropList.size() + 2; i++) {
                        if (!yamlf.contains("items." + i)) {
                            yamlf.set("items." + i + ".item", sItem);
                            yamlf.set("items." + i + ".page", page);
                            try {
                                yamlf.save(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    //
    // EventHandlers regarding Sponge Event
    //

    @EventHandler
    public void spongeEvent(BlockDamageEvent event) throws IOException {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if (b.getType() == Material.SPONGE) {
            if (loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                File f = new File(this.getDataFolder() + File.separator + "spongelocations.yml");
                FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                Set<String> setList = Objects.requireNonNull(yamlf.getConfigurationSection("locations")).getKeys(false);
                for (int i = 0; i < setList.size(); i++) {
                    if (yamlf.contains("locations." + i)) {
                        World w = Bukkit.getServer().getWorld(Objects.requireNonNull(yamlf.getString("locations." + i + ".world")));
                        Location spongeLoc = new Location(w, yamlf.getDouble("locations." + i + ".x"),
                                yamlf.getDouble("locations." + i + ".y"), yamlf.getDouble("locations." + i + ".z"));
                        spongeLoc = spongeLoc.getBlock().getLocation();
                        if (loc.equals(spongeLoc)) {
                            loc.getBlock().setType(Material.AIR);
                            for (int v = 0; v < setList.size(); v++) {
                                Random random = new Random();
                                int rand = random.nextInt(setList.size());
                                Location placeSponge = new Location(w, yamlf.getDouble("locations." + rand + ".x"),
                                        yamlf.getDouble("locations." + rand + ".y"), yamlf.getDouble("locations." + rand + ".z"));
                                placeSponge = placeSponge.getBlock().getLocation();
                                if (!placeSponge.equals(loc)) {
                                    for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                        online.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW + "Sponge" + ChatColor.WHITE + "] "
                                                + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW
                                                + " has found the sponge! A new one will be hidden somewhere in prison.");
                                    }
                                    File spongeData = new File(this.getDataFolder() + File.separator
                                            + "spongedata.yml");
                                    FileConfiguration sDataConf = YamlConfiguration.loadConfiguration(spongeData);
                                    String pUUID = event.getPlayer().getUniqueId().toString();
                                    if(sDataConf.isConfigurationSection(pUUID)) {
                                        int spongeFound = sDataConf.getInt(pUUID + ".sponge-found") + 1;
                                        sDataConf.set(pUUID + ".sponge-found", spongeFound);
                                    } else {
                                        sDataConf.set(pUUID + ".sponge-found", 1);
                                    }
                                    sDataConf.save(spongeData);

                                    tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(event.getPlayer()), 25);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }


    //
    // EventHandlers regarding Villager Trading
    //

    @EventHandler
    public void CustomVillagerTrades(VillagerAcquireTradeEvent event) {
        AbstractVillager villager = event.getEntity();
        List<MerchantRecipe> vSales = Lists.newArrayList(villager.getRecipes());

        vSales.removeIf(recipe -> recipe.getResult().getType().equals(Material.EMERALD));

        ItemStack wham = new ItemStack(Material.BOOK, 1);
        ItemMeta whams = wham.getItemMeta();
        whams.setDisplayName("Ur a potat");
        wham.setItemMeta(whams);
        MerchantRecipe newRecipe = new MerchantRecipe(wham, 5);
        newRecipe.addIngredient(new ItemStack(Material.COOKED_PORKCHOP, 69));
        vSales.add(newRecipe);
        villager.setRecipes(vSales);
    }

    @EventHandler
    public void villagerTrade(InventoryOpenEvent event) {
        if (event.getInventory().getType().equals(InventoryType.MERCHANT)) {
            Player player = (Player) event.getPlayer();
            player.sendMessage(ChatColor.RED + "Villager trading has been disabled");
            if(!player.isOp()) {
                event.setCancelled(true);
            }
        } else if(event.getInventory().getType().equals(InventoryType.SMITHING)) {
            event.setCancelled(true);
        }
    }

    //
    // EventHandlers regarding Farming & Mining
    //

    @EventHandler
    public void birchLeavesAppleDrop(LeavesDecayEvent event) {
        if(event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("world_prison")) {
            if(event.getBlock().getType() == Material.BIRCH_LEAVES) {
                if (Math.random() < 0.025) {
                    ItemStack apple = new ItemStack(Material.APPLE, 1);
                    event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), apple);
                }
            }
        }
    }


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if(!event.isCancelled()) {
            if(loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                if(!event.getPlayer().isOp()) {
                    if(b.getType().equals(Material.TNT)) {
                        event.setCancelled(true);
                    }
                }
            } else {
                if (!CoreProtect.getInstance().getAPI().hasPlaced(event.getPlayer().getName(), event.getBlock(), 300, 0)) {
                    String pUUID = event.getPlayer().getUniqueId().toString();
                    int brokeBlocks = blockBreaks.get(pUUID);
                    if (brokeBlocks >= 2000) {
                        blockBreaks.put(pUUID, 0);
                        Random rand = new Random();
                        int tReward = rand.nextInt(25 - 10 + 1) + 10;
                        tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(event.getPlayer()), tReward);
                        event.getPlayer().sendMessage(ChatColor.GRAY + "You've mined 2,000 blocks and have received some tokens!");
                    } else {
                        blockBreaks.put(pUUID, brokeBlocks + 1);
                    }
                }
                if (b.getType() == Material.SNOW_BLOCK && loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                    event.setDropItems(false);
                    Location cob = loc.add(0.5D, 0.0D, 0.5D);
                    ItemStack snowblock = new ItemStack(Material.SNOW_BLOCK, 1);
                    loc.getWorld().dropItem(cob, snowblock);
                } else if (b.getType() == Material.SNOW_BLOCK && loc.getWorld().getName().equalsIgnoreCase("world_event")) {
                    event.setDropItems(false);
                } else if (b.getType() == Material.BIRCH_LOG && loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                    ArrayList<Material> axes = new ArrayList<>();
                    axes.add(Material.DIAMOND_AXE);
                    axes.add(Material.GOLDEN_AXE);
                    axes.add(Material.IRON_AXE);
                    axes.add(Material.STONE_AXE);
                    axes.add(Material.WOODEN_AXE);
                    axes.add(Material.NETHERITE_AXE);
                    if (axes.contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                        if (!event.getPlayer().isSneaking()) {
                            boolean birchDown = true;
                            int birchDrops = 0;
                            Location birchLoc;
                            Location saplingLoc;
                            int i = 0;
                            while (birchDown) {
                                birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i, loc.getBlockZ());
                                if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                                    birchLoc.getBlock().breakNaturally();
                                    birchDrops++;
                                    i++;
                                } else {
                                    saplingLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i + 1, loc.getBlockZ());
                                    Location finalSaplingLoc = saplingLoc;
                                    if (birchLoc.getBlock().getType() == Material.GRASS_BLOCK || birchLoc.getBlock().getType() == Material.DIRT) {
                                        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> finalSaplingLoc.getBlock().setType(Material.BIRCH_SAPLING), 2L);
                                    }
                                    birchDown = false;
                                }
                            }
                            boolean birchUp = true;
                            int x = 1;
                            while (birchUp) {
                                birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + x, loc.getBlockZ());
                                if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                                    birchLoc.getBlock().breakNaturally();
                                    birchDrops++;
                                    x++;
                                } else {
                                    birchUp = false;
                                }
                            }

                            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                            Damageable im = (Damageable) item.getItemMeta();
                            Material axe = item.getType();
                            int dmg = im.getDamage();
                            if (item.containsEnchantment(Enchantment.DURABILITY)) {
                                int enchantLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
                                if (birchDrops / enchantLevel + dmg > axe.getMaxDurability()) {
                                    event.getPlayer().getInventory().remove(item);
                                } else {
                                    im.setDamage(birchDrops / enchantLevel + dmg);
                                    item.setItemMeta((ItemMeta) im);
                                }
                            } else {
                                if (birchDrops + dmg > axe.getMaxDurability()) {
                                    event.getPlayer().getInventory().remove(item);
                                } else {
                                    im.setDamage(birchDrops + dmg);
                                    item.setItemMeta((ItemMeta) im);
                                }
                            }
                        } else {
                            Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                            if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                                getServer().getScheduler().scheduleSyncDelayedTask(this, () -> loc.getBlock().setType(Material.BIRCH_SAPLING), 2L);

                            }
                        }
                    } else {
                        Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                        if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> loc.getBlock().setType(Material.BIRCH_SAPLING), 2L);
                        }
                    }
                } else if (b.getType() == Material.WHEAT && loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                    if (!event.getPlayer().isOp()) {
                        BlockData bdata = b.getBlockData();
                        if (bdata instanceof Ageable) {
                            Ageable age = (Ageable) bdata;
                            if (age.getAge() != age.getMaximumAge()) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "This wheat isn't ready for harvest..");
                            } else {
                                getServer().getScheduler().scheduleSyncDelayedTask(this, () -> loc.getBlock().setType(Material.WHEAT), 2L);
                            }
                        }
                    }
                } else if (b.getType() == Material.BIRCH_SAPLING && loc.getWorld().getName().equalsIgnoreCase("world_prison") && !event.getPlayer().isOp()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    //
    // Event Handlers regarding bounties
    //

    @EventHandler
    public void playerRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
            Location loc = player.getLocation();
            player.teleportAsync(loc);
        }
    }

    public void PvPSet(Player killed, Player killer) {
        File f = new File(this.getDataFolder() + File.separator + "recentkills.yml");
        FileConfiguration kills = YamlConfiguration.loadConfiguration(f);

        int pKills = kills.getInt(killer.getUniqueId() + ".pvpkills") + 1;
        int pDeaths = kills.getInt(killed.getUniqueId() + ".pvpdeaths") + 1;
        int pKillerStreak = kills.getInt(killer.getUniqueId() + ".pvpkillstreak") + 1;
        if(!kills.contains(killer.getUniqueId() + ".pvpdeaths")) {
            kills.set(killer.getUniqueId() + ".pvpdeaths", 0);
        }
        kills.set(killer.getUniqueId() + ".pvpkills", pKills);
        kills.set(killer.getUniqueId() + ".pvpkillstreak", pKillerStreak);
        kills.set(killer.getUniqueId() + ".kills." + killed.getUniqueId() + ".time", System.currentTimeMillis());

        kills.set(killed.getUniqueId() + ".pvpkillstreak", 0);
        kills.set(killed.getUniqueId() + ".pvpdeaths", pDeaths);
        try {
            kills.save(f);
            if(killed.hasPermission("skyprisoncore.guard.onduty")) {
                killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "15" + ChatColor.GRAY + " token!");
                tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 15);
            } else {
                killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "1" + ChatColor.GRAY + " token!");
                tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 1);
            }

            if(pDeaths == 1000) {
                asConsole("lp user " + killed.getName() + " permission set deluxetags.tag.death");
                killer.sendMessage(colourMessage(colourMessage("&7You have died a whopping &c&l1000 &7times! Therefore, you get a special tag!")));
            }

            if(pKills == 1000) {
                asConsole("lp user " + killer.getName() + " permission set deluxetags.tag.kills");
                killed.sendMessage(colourMessage(colourMessage("&7You have killed players &c&l1000 &7times! Therefore, you get a special tag!")));
            }

            if(pKillerStreak % 5 == 0 && pKillerStreak <= 100) {
                killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillerStreak + "&7! You have received &c&l15 &7tokens as a reward!"));
                tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 15);
            } else if(pKillerStreak % 50 == 0 && pKillerStreak > 100) {
                killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillerStreak + "&7! You have received &c&l30 &7tokens as a reward!"));
                tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 30);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void playerDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(Safezone.safezoneViolators.containsKey(player.getUniqueId())) {
                Safezone.safezoneViolators.remove(player.getUniqueId());
            }
        }

        if(event.getEntity() instanceof Player && event.getEntity().getKiller() != null) {
            Player killed = (Player) event.getEntity();
            Player killer = killed.getKiller();
            if(!killed.equals(killer)) {
                if(!killed.getWorld().getName().equalsIgnoreCase("world_event")) {
                    //
                    // Bounty Stuff
                    //
                    File f = new File(this.getDataFolder() + File.separator + "bounties.yml");
                    FileConfiguration bounty = YamlConfiguration.loadConfiguration(f);
                    Set<String> bountyList = bounty.getKeys(false);
                    for (String bountyPlayer : bountyList) {
                        if (killed.getUniqueId().equals(UUID.fromString(bountyPlayer))) {
                            try {
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "money give " + killer.getName() + " " + bounty.getDouble(bountyPlayer + ".bounty-prize"));
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi usermeta " + killer.getName() + " increment bounties_collected +1 -s");
                                bounty.set(bountyPlayer, null);
                                bounty.save(f);
                                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                    if (!online.hasPermission("skyprisoncore.bounty.silent")) {
                                        online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "]" + ChatColor.YELLOW + " " + killer.getName() + " has claimed the bounty on " + killed.getName() + "!");
                                    }
                                }
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    //
                    // Token Kills Stuff
                    //
                    f = new File(this.getDataFolder() + File.separator + "recentkills.yml");
                    FileConfiguration kills = YamlConfiguration.loadConfiguration(f);
                    CMIUser userK = CMI.getInstance().getPlayerManager().getUser(killer);
                    CMIUser userD = CMI.getInstance().getPlayerManager().getUser(killed);
                    if (!userD.getLastIp().equalsIgnoreCase(userK.getLastIp())) {
                        if (kills.contains(killer.getUniqueId() + ".kills")) {
                            Set<String> killsList = Objects.requireNonNull(kills.getConfigurationSection(killer.getUniqueId() + ".kills")).getKeys(false);
                            if (killsList.contains(killed.getUniqueId().toString())) {
                                for (String killedPlayer : killsList) {
                                    if (killed.getUniqueId().equals(UUID.fromString(killedPlayer))) {
                                        long time = kills.getLong(killer.getUniqueId() + ".kills." + killedPlayer + ".time");
                                        long timeLeft = System.currentTimeMillis() - time;
                                        if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 300) {
                                            PvPSet(killed, killer);
                                        } else {
                                            if (killer.getWorld().getName().equalsIgnoreCase("world_prison")
                                                    || killer.getWorld().getName().equalsIgnoreCase("world_free")
                                                    || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")
                                                    || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")) {
                                                int pKills = kills.getInt(killer.getUniqueId() + ".pvpkills") + 1;
                                                int pDeaths = kills.getInt(killed.getUniqueId() + ".pvpdeaths") + 1;
                                                int pKillStreak = kills.getInt(killer.getUniqueId() + ".pvpkillstreak") + 1;
                                                kills.set(killer.getUniqueId() + ".pvpkills", pKills);
                                                kills.set(killer.getUniqueId() + ".pvpkillstreak", pKillStreak);

                                                kills.set(killed.getUniqueId() + ".pvpkillstreak", 0);
                                                kills.set(killed.getUniqueId() + ".pvpdeaths", pDeaths);

                                                if(pDeaths == 1000) {
                                                    asConsole("lp user " + killed.getName() + " permission set deluxetags.tag.death");
                                                    killer.sendMessage(colourMessage(colourMessage("&7You have died a whopping &c&l1000 &7times! Therefore, you get a special tag!")));
                                                }

                                                if(pKills == 1000) {
                                                    asConsole("lp user " + killer.getName() + " permission set deluxetags.tag.kills");
                                                    killed.sendMessage(colourMessage(colourMessage("&7You have killed &c&l1000 &7players! Therefore, you get a special tag!")));
                                                }

                                                if(pKillStreak % 5 == 0 && pKillStreak <= 100) {
                                                    killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l15 &7tokens as a reward!"));
                                                    tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 15);

                                                } else if(pKillStreak % 50 == 0 && pKillStreak > 100) {
                                                    killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l30 &7tokens as a reward!"));
                                                    tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 30);
                                                }

                                                try {
                                                    kills.save(f);
                                                    long timeRem = 300 - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                                    killer.sendMessage(ChatColor.GRAY + "You have to wait " + ChatColor.RED + timeRem + ChatColor.GRAY + " seconds before receiving tokens from this player!");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                PvPSet(killed, killer);
                            }
                        } else {
                            PvPSet(killed, killer);
                        }
                    }
                }
            }
        }
    }
}


