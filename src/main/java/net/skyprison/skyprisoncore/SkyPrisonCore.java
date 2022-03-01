package net.skyprison.skyprisoncore;

import java.io.*;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisonclaims.SkyPrisonClaims;
import net.skyprison.skyprisoncore.commands.*;
import net.skyprison.skyprisoncore.commands.chats.Admin;
import net.skyprison.skyprisoncore.commands.chats.Build;
import net.skyprison.skyprisoncore.commands.chats.Guard;
import net.skyprison.skyprisoncore.commands.chats.Staff;
import net.skyprison.skyprisoncore.commands.guard.*;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.referral.Referral;
import net.skyprison.skyprisoncore.commands.referral.ReferralList;
import net.skyprison.skyprisoncore.commands.secrets.*;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.commands.donations.DonorAdd;
import net.skyprison.skyprisoncore.commands.donations.DonorBulk;
import net.skyprison.skyprisoncore.commands.donations.Purchases;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.skyprison.skyprisoncore.listeners.*;



public class SkyPrisonCore extends JavaPlugin implements Listener {
    public HashMap<String, String> hexColour = new HashMap<>();
    public HashMap<UUID, Boolean> flyPvP = new HashMap<>();
    public HashMap<UUID, Integer> teleportMove = new HashMap<>();
    public Map<String, Integer> tokensData = new HashMap<>();

    public Map<String, Integer> blockBreaks = new HashMap<>();

    public Connection conn;

    private File infoFile;
    private FileConfiguration infoConf;

    public HashMap<UUID, String> stickyChat = new HashMap<>();

    public SkyPrisonClaims claimPlugin;

    public HashMap<Material, Double> minPrice = new HashMap<>();


    private final DiscordUtil discordListener = new DiscordUtil(this);

    public Tokens tokens;


    public void onEnable() {

        tokens = new Tokens(this);

        github.scarsz.discordsrv.DiscordSRV.api.subscribe(discordListener);

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

        new ConfigCreator(this).init();
        new LangCreator(this).init();

        File dbFile = new File(this.getDataFolder() + File.separator + "SkyPrisonCore.db");
        DatabaseHook db = getDatabase();
        if(!dbFile.exists()) {
            db.createDatabase();
        }

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


        registerCommands();
        registerEvents();
    }


    public void registerCommands() {
        Objects.requireNonNull(getCommand("tokens")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("token")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("tokens")).setTabCompleter(new TabCompleter(this));
        Objects.requireNonNull(getCommand("token")).setTabCompleter(new TabCompleter(this));
        Objects.requireNonNull(getCommand("donoradd")).setExecutor(new DonorAdd(this));
        Objects.requireNonNull(getCommand("donorbulk")).setExecutor(new DonorBulk());
        Objects.requireNonNull(getCommand("purchases")).setExecutor(new Purchases());
        Objects.requireNonNull(getCommand("econcheck")).setExecutor(new EconomyCheck(this));
        Objects.requireNonNull(getCommand("permshop")).setExecutor(new PermShop(this));
        Objects.requireNonNull(getCommand("spongeloc")).setExecutor(new SpongeLoc(this));
        Objects.requireNonNull(getCommand("dropchest")).setExecutor(new DropChest(this));
        Objects.requireNonNull(getCommand("dontsell")).setExecutor(new DontSell(this));
        Objects.requireNonNull(getCommand("endupgrade")).setExecutor(new EndUpgrade(this));
        Objects.requireNonNull(getCommand("secretfound")).setExecutor(new SecretFound(this));
        Objects.requireNonNull(getCommand("rewards")).setExecutor(new SecretsGUI(this));
        Objects.requireNonNull(getCommand("bounty")).setExecutor(new Bounty(this));
        Objects.requireNonNull(getCommand("killinfo")).setExecutor(new KillInfo(this));
        Objects.requireNonNull(getCommand("firstjointop")).setExecutor(new FirstjoinTop(this));
        Objects.requireNonNull(getCommand("referral")).setExecutor(new Referral(this));
        Objects.requireNonNull(getCommand("referrallist")).setExecutor(new ReferralList(this));
        Objects.requireNonNull(getCommand("bartender")).setExecutor(new Bartender(this));
        Objects.requireNonNull(getCommand("sword")).setExecutor(new Sword(this));
        Objects.requireNonNull(getCommand("bow")).setExecutor(new Bow(this));
        Objects.requireNonNull(getCommand("contraband")).setExecutor(new Contraband(this));
        Objects.requireNonNull(getCommand("ignoretp")).setExecutor(new IgnoreTP(this));
        Objects.requireNonNull(getCommand("guardduty")).setExecutor(new GuardDuty(this));
        Objects.requireNonNull(getCommand("safezone")).setExecutor(new Safezone(this));
        Objects.requireNonNull(getCommand("buyback")).setExecutor(new BuyBack(this));
        Objects.requireNonNull(getCommand("daily")).setExecutor(new Daily(this));
        Objects.requireNonNull(getCommand("shopban")).setExecutor(new ShopBan(this));
        Objects.requireNonNull(getCommand("enchtable")).setExecutor(new EnchTable(this));
        Objects.requireNonNull(getCommand("removeitalics")).setExecutor(new RemoveItalics(this));
        Objects.requireNonNull(getCommand("bottledexp")).setExecutor(new BottledExp(this));
        Objects.requireNonNull(getCommand("transportpass")).setExecutor(new TransportPass(this));
        Objects.requireNonNull(getCommand("bail")).setExecutor(new Bail(this));
        Objects.requireNonNull(getCommand("casino")).setExecutor(new Casino(this));
        Objects.requireNonNull(getCommand("skyplot")).setExecutor(new SkyPlot(this));
        Objects.requireNonNull(getCommand("plot")).setExecutor(new PlotTeleport(this));
        Objects.requireNonNull(getCommand("moneyhistory")).setExecutor(new MoneyHistory(this));
        Objects.requireNonNull(getCommand("g")).setExecutor(new Guard(this, new ChatUtils(this)));
        Objects.requireNonNull(getCommand("b")).setExecutor(new Build(this, new ChatUtils(this)));
        Objects.requireNonNull(getCommand("a")).setExecutor(new Admin(this, new ChatUtils(this)));
        Objects.requireNonNull(getCommand("s")).setExecutor(new Staff(this, new ChatUtils(this)));
    }


    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new AsyncChat(this), this);
        pm.registerEvents(new BlockBreak(this), this);
        pm.registerEvents(new BlockDamage(this), this);
        pm.registerEvents(new BlockPlace(this), this);
        pm.registerEvents(new BrewDrink(this), this);
        pm.registerEvents(new CMIPlayerTeleportRequest(this), this);
        pm.registerEvents(new CMIUserBalanceChange(this), this);
        pm.registerEvents(new EntityDamageByEntity(this), this);
        pm.registerEvents(new EntityDeath(this, new Safezone(this)), this);
        pm.registerEvents(new EntityPickupItem(this), this);
        pm.registerEvents(new EntityRemoveFromWorld(this), this);
        pm.registerEvents(new InventoryClick(this, new EconomyCheck(this), new DropChest(this), new Bounty(this),
                new SecretsGUI(this), new Daily(this), new MoneyHistory(this), new EndUpgrade(this), new BuyBack(this)), this);
        pm.registerEvents(new InventoryOpen(), this);
        pm.registerEvents(new LeavesDecay(), this);
        pm.registerEvents(new McMMOLevelUp(this), this);
        pm.registerEvents(new McMMOPartyChat(), this);
        pm.registerEvents(new PlayerChangedWorld(), this);
        pm.registerEvents(new PlayerInteract(this), this);
        pm.registerEvents(new PlayerJoin(this, getDatabase()), this);
        pm.registerEvents(new PlayerMove(this), this);
        pm.registerEvents(new PlayerPostRespawn(this), this);
        pm.registerEvents(new PlayerQuit(this, getDatabase()), this);
        pm.registerEvents(new PlayerRiptide(), this);
        pm.registerEvents(new PlayerTag(this), this);
        pm.registerEvents(new PlayerTeleport(this), this);
        pm.registerEvents(new PlayerUnJail(), this);
        pm.registerEvents(new PlayerUntag(this), this);
        pm.registerEvents(new ShopCreate(this), this);
        pm.registerEvents(new ShopPostTransaction(this), this);
        pm.registerEvents(new ShopPreTransaction(this), this);
        pm.registerEvents(new ShopPurchase(this), this);
        pm.registerEvents(new ShopSuccessPurchase(this), this);
        pm.registerEvents(new UnsellRegion(), this);
    }


    public DatabaseHook getDatabase() {
        return new DatabaseHook(this);
    }


    /*	@EventHandler
	public void onCourseCompletion(PlayerFinishCourseEvent event) {
		Player player = event.getPlayer();
		List<String> completedCourses = PlayerInfo.getCompletedCourses(player);
		List<String> allCourses = CourseInfo.getAllCourseNames();
		Collections.sort(allCourses);
		Collections.sort(completedCourses);
		if(completedCourses.equals(allCourses)) {
			asConsole("lp user " + player.getName() + " permission set deluxetags.tag.Parkourist");
		}
	}*/


    public String removeColorCodes(String message) {
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
        }
        return matcher.appendTail(buffer).toString();
    }


    public String removeColour(String message){
        message = removeColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
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

        github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(discordListener);
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
    public void chatToDiscord(AsyncChatEvent event) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(event.getPlayer());
        new MessageBuilder()
                .append("**" + user.getGroupName() + "** " +  event.getPlayer().name() + " » " + event.message())
                .send(discApi.getTextChannelById("788108242797854751").get());
    }

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
}


