package net.skyprison.skyprisoncore;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.skyprison.skyprisonclaims.SkyPrisonClaims;
import net.skyprison.skyprisoncore.commands.*;
import net.skyprison.skyprisoncore.commands.chats.Admin;
import net.skyprison.skyprisoncore.commands.chats.Build;
import net.skyprison.skyprisoncore.commands.chats.Guard;
import net.skyprison.skyprisoncore.commands.chats.Staff;
import net.skyprison.skyprisoncore.commands.discord.Discord;
import net.skyprison.skyprisoncore.commands.guard.*;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.Referral;
import net.skyprison.skyprisoncore.commands.secrets.*;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.commands.donations.DonorAdd;
import net.skyprison.skyprisoncore.commands.donations.Purchases;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.skyprison.skyprisoncore.listeners.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;


public class SkyPrisonCore extends JavaPlugin {
    public HashMap<String, String> hexColour = new HashMap<>();
    public HashMap<UUID, Boolean> flyPvP = new HashMap<>();
    public HashMap<UUID, Integer> teleportMove = new HashMap<>();
    public Map<String, Integer> tokensData = new HashMap<>();

    public Map<Integer, UUID> discordLinking = new HashMap<>();

    public Map<String, Integer> blockBreaks = new HashMap<>();

    private FileConfiguration infoConf;

    public HashMap<UUID, String> stickyChat = new HashMap<>();

    public SkyPrisonClaims claimPlugin;

    public HashMap<Material, Double> minPrice = new HashMap<>();

    public Tokens tokens;

    private DiscordApi discApi;

    private DailyMissions dailyMissions;

    public void onEnable() {
        String dToken = getConfig().getString("discord-token");

        Plugin sPlugin = this;

        if(dToken != null && !dToken.isEmpty()) {
            discApi = new DiscordApiBuilder()
                    .setToken(dToken)
                    .login()
                    .join();

            onConnectToDiscord(discApi);

            new BukkitRunnable() {
                @Override
                public void run() {
                    updateDiscordRoles(discApi);
                }
            }.runTaskTimerAsynchronously(this, 20 * 1800, 20 * 1800);
        }

        tokens = new Tokens(this, getDatabase());

        dailyMissions = new DailyMissions();

        registerMinPrice();
        registerCommands();
        registerEvents();

        new ConfigCreator(this).init();
        new LangCreator(this).init();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this, dailyMissions, getDatabase()).register();
            getLogger().info("Placeholders registered");
        }
        
        if(Bukkit.getPluginManager().getPlugin("SkyPrisonClaims") != null) {
            claimPlugin = (SkyPrisonClaims) Bukkit.getPluginManager().getPlugin("SkyPrisonClaims");
        }

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


        new BukkitRunnable() {
            @Override
            public void run() {
                checkDailies();
            }
        }.runTaskTimerAsynchronously(this, 20 * 3800, 20 * 3800);

        File infoFile = new File(this.getDataFolder() + File.separator + "info.yml");
        infoConf = YamlConfiguration.loadConfiguration(infoFile);

        new BukkitRunnable() {
            @Override
            public void run() {
                if(Bukkit.getServer().getOnlinePlayers().size() > 0) {
                    announcer();
                }
            }
        }.runTaskTimer(this, 20*950, 20*950);

        new BukkitRunnable() {
            @Override
            public void run() {
                updateTopic();
                checkOnlineDailies();
                if(tokensData != null && !tokensData.isEmpty()) {
                    Map<String, Integer> token = tokensData;
                    for (String pUUID : token.keySet()) {
                        String sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
                        List<Object> params = new ArrayList<Object>() {{
                            add(tokensData.get(pUUID));
                            add(pUUID);
                        }};
                        getDatabase().sqlUpdate(sql, params);
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 20 * 635, 20 * 635);
    }

    @Override
    public void onDisable() {
        if(discApi != null) {
            discApi.getTextChannelById("788108242797854751").get().sendMessage(":octagonal_sign: **Server has stopped**");
            discApi.getServerTextChannelById("788108242797854751").get().updateTopic("Server is offline!");
            discApi.disconnect();
        }
        getLogger().info("Disabled SkyPrisonCore v" + getDescription().getVersion());
    }


    private void onConnectToDiscord(DiscordApi discApi) {
        getLogger().info("Connected to Discord as " + discApi.getYourself().getDiscriminatedName());
        getLogger().info("Open the following url to invite the bot: " + discApi.createBotInvite());
        discApi.getTextChannelById("788108242797854751").get().sendMessage(":white_check_mark: **Server has started**");

        discApi.addListener(new MessageCreate(this, new ChatUtils(this, discApi), discApi, getDatabase()));
        discApi.addListener(new UserRoleAdd(this, getDatabase()));
        discApi.addListener(new UserRoleRemove(this, getDatabase()));
    }

    private void updateTopic() {
        TextChannel channel = discApi.getTextChannelById("788108242797854751").get();
        channel.asServerTextChannel().get().updateTopic("Online Players: " + Bukkit.getOnlinePlayers().size() + "/50");
    }


    public void updateDiscordRoles(DiscordApi discApi) {
        for(Player player : Bukkit.getOnlinePlayers()) {
            long discordId = 0;
            try {
                Connection conn = getDatabase().getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    discordId = rs.getLong(1);
                }
                getDatabase().close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(discordId != 0) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                String roleName = user.getRank().getName();
                Role role = discApi.getRolesByName(roleName).iterator().next();
                try {
                    User discUser = discApi.getUserById(discordId).get();
                    Server server = discApi.getServerById("782795465632251955").get();
                    if(!discUser.getRoles(server).contains(role)) {
                        discUser.addRole(role);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerMinPrice() {
        minPrice.put(Material.BIRCH_LOG, 5.0);
        minPrice.put(Material.BIRCH_PLANKS, 2.0);
        minPrice.put(Material.BIRCH_SAPLING, 5.0);
        minPrice.put(Material.COAL, 9.0);
        minPrice.put(Material.COBBLESTONE, 2.0);
        minPrice.put(Material.STONE, 4.0);
        minPrice.put(Material.SANDSTONE, 2.0);
        minPrice.put(Material.SMOOTH_SANDSTONE, 4.0);
        minPrice.put(Material.SNOW_BLOCK, 1.0);
        minPrice.put(Material.GLOWSTONE, 3.0);
        minPrice.put(Material.NETHERRACK, 2.0);
        minPrice.put(Material.PUMPKIN, 6.0);
        minPrice.put(Material.NETHER_WART_BLOCK, 10.0);
        minPrice.put(Material.IRON_INGOT, 33.0);
        minPrice.put(Material.LAPIS_LAZULI, 5.5);
        minPrice.put(Material.BAMBOO, 2.0);
        minPrice.put(Material.STICK, 1.0);
        minPrice.put(Material.GOLD_NUGGET, 6.0);
        minPrice.put(Material.GOLD_INGOT, 46.0);
        minPrice.put(Material.EMERALD, 51.0);
        minPrice.put(Material.GREEN_DYE, 11.0);
        minPrice.put(Material.SUGAR_CANE, 3.0);
        minPrice.put(Material.SUGAR, 6.0);
        minPrice.put(Material.DIAMOND, 66.0);
        minPrice.put(Material.CHARCOAL, 6.5);
        minPrice.put(Material.NETHER_WART, 3.0);
        minPrice.put(Material.BEEF, 16.0);
        minPrice.put(Material.PORKCHOP, 16.0);
        minPrice.put(Material.SALMON, 6.0);
        minPrice.put(Material.TROPICAL_FISH, 46.0);
        minPrice.put(Material.LEATHER, 6.0);
        minPrice.put(Material.BONE, 16.0);
        minPrice.put(Material.ROTTEN_FLESH, 16.0);
        minPrice.put(Material.COOKED_BEEF, 21.0);
        minPrice.put(Material.COOKED_PORKCHOP, 21.0);
        minPrice.put(Material.COOKED_SALMON, 16.0);
        minPrice.put(Material.PUFFERFISH, 31.0);
        minPrice.put(Material.SPIDER_EYE, 16.0);
        minPrice.put(Material.STRING, 16.0);
        minPrice.put(Material.COD, 6.0);
        minPrice.put(Material.COOKED_COD, 16.0);
        minPrice.put(Material.MELON_SLICE, 2.0);
        minPrice.put(Material.APPLE, 5.0);
    }

    public void registerCommands() {
        Objects.requireNonNull(getCommand("tokens")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("token")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("tokens")).setTabCompleter(new TabCompleter(this));
        Objects.requireNonNull(getCommand("token")).setTabCompleter(new TabCompleter(this));
        Objects.requireNonNull(getCommand("donoradd")).setExecutor(new DonorAdd(getDatabase()));
        Objects.requireNonNull(getCommand("purchases")).setExecutor(new Purchases(getDatabase(), this));
        Objects.requireNonNull(getCommand("econcheck")).setExecutor(new EconomyCheck(this));
        Objects.requireNonNull(getCommand("permshop")).setExecutor(new PermShop(this));
        Objects.requireNonNull(getCommand("spongeloc")).setExecutor(new SpongeLoc(this));
        Objects.requireNonNull(getCommand("dropchest")).setExecutor(new DropChest(this));
        Objects.requireNonNull(getCommand("dontsell")).setExecutor(new DontSell(this, getDatabase()));
        Objects.requireNonNull(getCommand("endupgrade")).setExecutor(new EndUpgrade(this));
        Objects.requireNonNull(getCommand("secretfound")).setExecutor(new SecretFound(this, dailyMissions, getDatabase()));
        Objects.requireNonNull(getCommand("rewards")).setExecutor(new SecretsGUI(this, getDatabase()));
        Objects.requireNonNull(getCommand("bounty")).setExecutor(new Bounty(getDatabase(), this));
        Objects.requireNonNull(getCommand("killinfo")).setExecutor(new KillInfo(getDatabase()));
        Objects.requireNonNull(getCommand("firstjointop")).setExecutor(new FirstjoinTop(this, getDatabase()));
        Objects.requireNonNull(getCommand("referral")).setExecutor(new Referral(this, discApi, getDatabase()));
        Objects.requireNonNull(getCommand("bartender")).setExecutor(new Bartender(this));
        Objects.requireNonNull(getCommand("sword")).setExecutor(new Sword(this));
        Objects.requireNonNull(getCommand("discord")).setExecutor(new Discord(this, getDatabase(), discApi));
        Objects.requireNonNull(getCommand("bow")).setExecutor(new Bow(this));
        Objects.requireNonNull(getCommand("contraband")).setExecutor(new Contraband(this));
        Objects.requireNonNull(getCommand("ignoretp")).setExecutor(new IgnoreTeleport(this, getDatabase()));
        Objects.requireNonNull(getCommand("guardduty")).setExecutor(new GuardDuty(this));
        Objects.requireNonNull(getCommand("safezone")).setExecutor(new Safezone(this));
        Objects.requireNonNull(getCommand("buyback")).setExecutor(new BuyBack(this, getDatabase()));
        Objects.requireNonNull(getCommand("daily")).setExecutor(new Daily(this, new DatabaseHook(this)));
        Objects.requireNonNull(getCommand("shopban")).setExecutor(new ShopBan(getDatabase(), this));
        Objects.requireNonNull(getCommand("enchtable")).setExecutor(new EnchTable(this));
        Objects.requireNonNull(getCommand("removeitalics")).setExecutor(new RemoveItalics(this));
        Objects.requireNonNull(getCommand("bottledexp")).setExecutor(new BottledExp(this));
        Objects.requireNonNull(getCommand("transportpass")).setExecutor(new TransportPass(this));
        Objects.requireNonNull(getCommand("bail")).setExecutor(new Bail(this));
        Objects.requireNonNull(getCommand("casino")).setExecutor(new Casino(this, getDatabase()));
        Objects.requireNonNull(getCommand("skyplot")).setExecutor(new SkyPlot(this));
        Objects.requireNonNull(getCommand("plot")).setExecutor(new PlotTeleport(this));
        Objects.requireNonNull(getCommand("moneyhistory")).setExecutor(new MoneyHistory(this));
        Objects.requireNonNull(getCommand("g")).setExecutor(new Guard(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("b")).setExecutor(new Build(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("a")).setExecutor(new Admin(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("s")).setExecutor(new Staff(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("donorreset")).setExecutor(new DonorReset(this));
        Objects.requireNonNull(getCommand("customenchant")).setExecutor(new CustomEnchant(this));
        Objects.requireNonNull(getCommand("sptags")).setExecutor(new Tags(this));
    }


    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AsyncPlayerChat(this, discApi), this);
        pm.registerEvents(new BlockBreak(this), this);
        pm.registerEvents(new BlockDamage(this, getDatabase()), this);
        pm.registerEvents(new BlockPlace(this), this);
        pm.registerEvents(new BrewDrink(this, getDatabase()), this);
        pm.registerEvents(new CMIPlayerTeleportRequest(this, getDatabase()), this);
        pm.registerEvents(new CMIUserBalanceChange(this), this);
        pm.registerEvents(new EntityDamageByEntity(this), this);
        pm.registerEvents(new EntityDeath(this, new Safezone(this), getDatabase()), this);
        pm.registerEvents(new EntityPickupItem(this), this);
        pm.registerEvents(new EntityRemoveFromWorld(this), this);
        pm.registerEvents(new InventoryClick(this, new EconomyCheck(this), new DropChest(this), new Bounty(getDatabase(), this),
                new SecretsGUI(this, getDatabase()), new Daily(this, new DatabaseHook(this)), new MoneyHistory(this), new EndUpgrade(this),
                new BuyBack(this, getDatabase()), new SkyPlot(this), getDatabase()), this);
        pm.registerEvents(new InventoryOpen(), this);
        pm.registerEvents(new LeavesDecay(), this);
        pm.registerEvents(new McMMOLevelUp(this), this);
        pm.registerEvents(new McMMOPartyChat(discApi), this);
        pm.registerEvents(new PlayerChangedWorld(), this);
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerJoin(this, getDatabase(), discApi, dailyMissions), this);
        pm.registerEvents(new PlayerMove(this), this);
        pm.registerEvents(new PlayerPostRespawn(this), this);
        pm.registerEvents(new PlayerQuit(this, getDatabase(), discApi), this);
        pm.registerEvents(new PlayerRiptide(), this);
        pm.registerEvents(new PlayerTag(this), this);
        pm.registerEvents(new PlayerTeleport(this), this);
        pm.registerEvents(new PlayerUnJail(), this);
        pm.registerEvents(new PlayerUntag(this), this);
        pm.registerEvents(new ShopCreate(this), this);
        pm.registerEvents(new ShopPostTransaction(getDatabase()), this);
        pm.registerEvents(new ShopPreTransaction(getDatabase()), this);
        pm.registerEvents(new ShopPurchase(this, getDatabase()), this);
        pm.registerEvents(new ShopSuccessPurchase(this), this);
        pm.registerEvents(new UnsellRegion(), this);
        pm.registerEvents(new PlayerFish(this, dailyMissions), this);
        pm.registerEvents(new InventoryClose(this), this);
        pm.registerEvents(new EntityDamage(this), this);
        pm.registerEvents(new PlayerCommandPreprocess(this), this);
    }


    public DatabaseHook getDatabase() {
        return new DatabaseHook(this);
    }

    private void announcer() {
        boolean prisonPlayers = false;
        boolean freePlayers = false;
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission("group.free")) {
                freePlayers = true;
            } else if(player.hasPermission("group.default")) {
                prisonPlayers = true;
            }
        }
        Random rand = new Random();
        Component fMsgComp = null;
        Component pMsgComp = null;

        String splitter = "          &8&l&m⎯⎯⎯⎯=⎯⎯⎯⎯⎯⎯⎯⎯=⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯=⎯⎯⎯⎯⎯⎯⎯⎯=⎯⎯⎯⎯&f";


        String hSplitter = "&8&l&m⎯⎯⎯⎯⎯=⎯⎯⎯⎯⎯⎯⎯⎯=⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯=⎯⎯⎯⎯⎯⎯⎯⎯=⎯⎯⎯⎯⎯&f";

        if(freePlayers) {
            ArrayList<String> free = new ArrayList<>(infoConf.getStringList("general"));
            free.addAll(infoConf.getStringList("free"));
            String fullMsg = free.get(rand.nextInt(free.size()));

            String msg = StringUtils.substringsBetween(fullMsg, "<T>", "</T>")[0];

            String[] splitMsg = msg.split("\\[LINE]");

            StringBuilder cMsg = new StringBuilder();

            for(Iterator<String> iterator = Arrays.stream(splitMsg).iterator(); iterator.hasNext();) {
                msg = iterator.next();
                String centeredMsg = getCenteredLine(msg);
                cMsg.append(centeredMsg);
                if(iterator.hasNext()) {
                    cMsg.append("\n");
                }
            }

            fMsgComp = Component.text(colourMessage("\n" + getCenteredLine("&4&lInfo") + "\n" + splitter + "\n" + cMsg + "&l\n" + splitter));

            String[] hMsg = StringUtils.substringsBetween(fullMsg, "<H>", "</H>");
            String[] cmd = StringUtils.substringsBetween(fullMsg, "<C>", "</C>");
            String[] url = StringUtils.substringsBetween(fullMsg, "<URL>", "</URL>");


            if(cmd != null)
                fMsgComp = fMsgComp.clickEvent(ClickEvent.runCommand(cmd[0]));
            if(url != null)
                fMsgComp = fMsgComp.clickEvent(ClickEvent.openUrl(url[0]));

            if(hMsg != null) {
                splitMsg = hMsg[0].split("\\[LINE]");
                cMsg = new StringBuilder();
                for(Iterator<String> iterator = Arrays.stream(splitMsg).iterator(); iterator.hasNext();) {
                    hMsg[0] = iterator.next();
                    String centeredMsg = getCenteredHover(hMsg[0]);
                    cMsg.append(centeredMsg);
                    if(iterator.hasNext()) {
                        cMsg.append("\n");
                    }
                }
                fMsgComp = fMsgComp.hoverEvent(HoverEvent.showText(Component.text(colourMessage(hSplitter + "\n" + cMsg + "&l\n" + hSplitter))));
            }

        }
        if(prisonPlayers) {
            ArrayList<String> prison = new ArrayList<>(infoConf.getStringList("general"));
            prison.addAll(infoConf.getStringList("prison"));
            String fullMsg = prison.get(rand.nextInt(prison.size()));

            String msg = StringUtils.substringsBetween(fullMsg, "<T>", "</T>")[0];

            String[] splitMsg = msg.split("\\[LINE]");

            StringBuilder cMsg = new StringBuilder();

            for(Iterator<String> iterator = Arrays.stream(splitMsg).iterator(); iterator.hasNext();) {
                String centeredMsg = getCenteredLine(iterator.next());
                cMsg.append(centeredMsg);
                if(iterator.hasNext()) {
                    cMsg.append("\n");
                }
            }

            pMsgComp = Component.text(colourMessage("\n" + getCenteredLine("&4&lInfo") + "\n" + splitter + "\n" + cMsg + "\n" + splitter));


            String[] hMsg = StringUtils.substringsBetween(fullMsg, "<H>", "</H>");
            String[] cmd = StringUtils.substringsBetween(fullMsg, "<C>", "</C>");
            String[] url = StringUtils.substringsBetween(fullMsg, "<URL>", "</URL>");


            if(cmd != null)
                pMsgComp = pMsgComp.clickEvent(ClickEvent.runCommand(cmd[0]));
            if(url != null)
                pMsgComp = pMsgComp.clickEvent(ClickEvent.openUrl(url[0]));

            if(hMsg != null) {
                splitMsg = hMsg[0].split("\\[LINE]");
                cMsg = new StringBuilder();
                for(Iterator<String> iterator = Arrays.stream(splitMsg).iterator(); iterator.hasNext();) {
                    hMsg[0] = iterator.next();
                    String centeredMsg = getCenteredHover(hMsg[0]);
                    cMsg.append(centeredMsg);
                    if(iterator.hasNext()) {
                        cMsg.append("\n");
                    }
                }
                pMsgComp = pMsgComp.hoverEvent(HoverEvent.showText(Component.text(colourMessage(hSplitter + "\n" + cMsg + "\n" + hSplitter))));
            }
        }


        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            if(player.hasPermission("group.free")) {
                player.sendMessage(fMsgComp);
            } else {
                player.sendMessage(pMsgComp);
            }
        }
    }

    private final static int CENTER_PX = 154;

    private final static int CENTER_PX_HOVER = 114;

    public static String getCenteredHover(String message){
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
            }else if(previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX_HOVER - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb + message;
    }

    public static String getCenteredLine(String message){
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
            }else if(previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb + message;
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

    public void checkOnlineDailies() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String currDate = formatter.format(date);

        for(Player player : Bukkit.getOnlinePlayers()) {
            String lastDay = "";
            try {
                Connection conn = getDatabase().getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT last_collected FROM dailies WHERE user_id = '" + player.getUniqueId() + "'");
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    lastDay = rs.getString(1);
                }
                getDatabase().close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(lastDay != null && !lastDay.equalsIgnoreCase(currDate)) {
                player.sendMessage(colourMessage("&aYou can collect your &l/daily&l!"));
            }
        }
    }

    public void checkDailies() {
        ArrayList<String> dailyPlayers = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate yesterday = LocalDate.now().minusDays(2);
        String yesterDate = yesterday.format(formatter);

        try {
            Connection conn = getDatabase().getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT user_id, current_streak FROM dailies WHERE last_collected = '" + yesterDate + "'");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                if(rs.getInt(2) > 0) dailyPlayers.add(rs.getString(1));
            }
            getDatabase().close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(String player : dailyPlayers) {
            String sql = "UPDATE dailies SET current_streak = 0 WHERE user_id = ?";
            List<Object> params = new ArrayList<Object>() {{
                add(player);
            }};
            getDatabase().sqlUpdate(sql, params);
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

/*

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

*/


}


