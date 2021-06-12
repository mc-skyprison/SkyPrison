package com.github.drakepork.skyprisoncore;

import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.tp.Teleportations;
import com.Zrips.CMI.events.CMIPlayerTeleportEvent;
import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.bergerkiller.bukkit.common.events.EntityRemoveEvent;
import com.github.drakepork.skyprisoncore.Commands.contraband.Bow;
import com.github.drakepork.skyprisoncore.Commands.contraband.Contraband;
import com.github.drakepork.skyprisoncore.Commands.contraband.Safezone;
import com.github.drakepork.skyprisoncore.Commands.contraband.Sword;
import com.github.drakepork.skyprisoncore.Commands.economy.BuyBack;
import com.github.drakepork.skyprisoncore.Commands.referral.Referral;
import com.github.drakepork.skyprisoncore.Commands.referral.ReferralList;
import com.github.drakepork.skyprisoncore.utils.ConfigCreator;
import com.github.drakepork.skyprisoncore.utils.LangCreator;
import com.github.drakepork.skyprisoncore.utils.Placeholders;
import com.github.drakepork.skyprisoncore.utils.PluginReceiver;
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
import com.github.drakepork.skyprisoncore.Commands.*;
import com.github.drakepork.skyprisoncore.Commands.donations.DonorAdd;
import com.github.drakepork.skyprisoncore.Commands.donations.DonorBulk;
import com.github.drakepork.skyprisoncore.Commands.donations.Purchases;
import com.github.drakepork.skyprisoncore.Commands.economy.*;
import me.NoChance.PvPManager.Events.PlayerTagEvent;
import me.NoChance.PvPManager.Events.PlayerUntagEvent;
import me.NoChance.PvPManager.Managers.PlayerHandler;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.TokenManager;
import net.brcdev.shopgui.event.ShopPostTransactionEvent;
import net.brcdev.shopgui.event.ShopPreTransactionEvent;
import net.brcdev.shopgui.shop.ShopManager;
import net.goldtreeservers.worldguardextraflags.flags.Flags;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;


public class Core extends JavaPlugin implements Listener {
    public HashMap<String, String> hexColour = new HashMap<>();
    public HashMap<UUID, Boolean> flyPvP = new HashMap<>();

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

    @Inject private GuardDuty GuardDuty;

    @Inject private EndUpgrade EndUpgrade;

    @Inject private BuyBack BuyBack;

    @Inject private EconomyCheck EconomyCheck;
    @Inject private PermShop PermShop;

    @Inject private com.github.drakepork.skyprisoncore.Commands.secrets.SecretsGUI SecretsGUI;
    @Inject private com.github.drakepork.skyprisoncore.Commands.secrets.SecretFound SecretFound;

    @Inject private Bartender Bartender;

    @Inject private IgnoreTP IgnoreTP;

    @Inject private Safezone Safezone;

    @Inject private DontSell DontSell;

    @Inject private DropChest DropChest;
    @Inject private SpongeLoc SpongeLoc;
    @Inject private FirstjoinTop FirstjoinTop;

    FileConfiguration config = this.getConfig();

    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        PluginReceiver module = new PluginReceiver(this);
        Injector injector = module.createInjector();
        injector.injectMembers(this);

        this.configCreator.init();
        this.langCreator.init();

        ArrayList<String> files = new ArrayList<>();
        files.add("bounties.yml");
        files.add("spongelocations.yml");
        files.add("dropchest.yml");
        files.add("secrets.yml");
        files.add("donations");
        files.add("recentkills.yml");
        files.add("referrals.yml");
        files.add("rewardsdata.yml");
        files.add("secretsdata.yml");
        files.add("bartender.yml");
        files.add("spongedata.yml");
        files.add("blocksells.yml");
        files.add("firstjoindata.yml");
        files.add("recentsells.yml");
        files.add("teleportignore.yml");
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
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
            getLogger().info("Placeholders registered");
        }

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
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled SkyPrisonCore v" + getDescription().getVersion());
    }


    public String colourMessage(String message) {
        message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
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
    public void firstJoinCheck(PlayerJoinEvent event) throws ParseException, IOException {
        File fData = new File(this.getDataFolder() + File.separator + "firstjoindata.yml");
        YamlConfiguration firstJoinConf = YamlConfiguration.loadConfiguration(fData);
        String pUUID = event.getPlayer().getUniqueId().toString();
        if(!firstJoinConf.isConfigurationSection(pUUID)) {
            String firstJoinString = PlaceholderAPI.setPlaceholders(event.getPlayer(), "%player_first_join_date%");
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
            Date firstJoinDate = sdf.parse(firstJoinString);
            Long firstJoinMilli = firstJoinDate.getTime();
            firstJoinConf.set(pUUID + ".firstjoin", firstJoinMilli);
            firstJoinConf.save(fData);
        }
    }

    @EventHandler
    public void onTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        File ignoreData = new File(this.getDataFolder() + File.separator + "teleportignore.yml");
        YamlConfiguration ignoreConf = YamlConfiguration.loadConfiguration(ignoreData);
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


    //
    // Creates lists of people that have been /cb, and also creates the list containing all of the contraband
    //

    public Map<Player, Map.Entry<Player, Long>> hitcd = new HashMap<>();
    public boolean isGuardGear(ItemStack i) {
        if (i != null) {
            if (i.getType() == Material.CHAINMAIL_HELMET || i.getType() == Material.CHAINMAIL_CHESTPLATE || i.getType() == Material.CHAINMAIL_LEGGINGS || i.getType() == Material.CHAINMAIL_BOOTS || i.getType() == Material.DIAMOND_SWORD) {
                return true;
            } else if (i.getType() == Material.BOW) {
                return i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Guard Bow");
            } else if (i.getType() == Material.SHIELD) {
                return i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Guard Shield");
            } else return i.getType() == Material.LEAD && i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Cuffs");
        } else {
            return false;
        }
    }

    private void InvGuardGearDelPlyr(Player player) {
        for (int n = 0; n < player.getInventory().getSize(); n++) {
            ItemStack i = player.getInventory().getItem(n);
            if (i != null && isGuardGear(i)) {
                i.setAmount(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if ((!event.canBuild() || event.isCancelled()) && !event.getPlayer().hasPermission("antiblockjump.bypass"))
            event.getPlayer().setVelocity(new Vector(0, -5, 0));
    }

    @EventHandler
    public void lavaBucketMine(PlayerBucketEmptyEvent event) {
        if(!event.isCancelled()) {
            Player player = event.getPlayer();
            World pWorld = player.getWorld();
            if (event.getBucket().equals(Material.LAVA_BUCKET)) {
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(pWorld));
                ApplicableRegionSet regionList = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ()));
                if (pWorld.getName().equalsIgnoreCase("world_prison")) {
                    if(regionList.getRegions().contains(regions.getRegion("grass-mine"))) {
                        event.setCancelled(true);
                    } else if(regionList.getRegions().contains(regions.getRegion("desert-mine"))) {
                        event.setCancelled(true);
                    } else if(regionList.getRegions().contains(regions.getRegion("nether-mine"))) {
                        event.setCancelled(true);
                    } else if(regionList.getRegions().contains(regions.getRegion("snow-mine"))) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }


    @EventHandler
    public void guardHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damagee = (Player) event.getEntity();
            com.sk89q.worldedit.util.Location damagerLoc = BukkitAdapter.adapt(damager.getLocation());
            com.sk89q.worldedit.util.Location damageeLoc = BukkitAdapter.adapt(damagee.getLocation());
            LocalPlayer localDamager = WorldGuardPlugin.inst().wrapPlayer(damager);
            LocalPlayer localDamagee = WorldGuardPlugin.inst().wrapPlayer(damagee);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            if(flyPvP.containsKey(damager.getUniqueId()) && !flyPvP.containsKey(damagee.getUniqueId())) {
                flyPvP.remove(damager.getUniqueId());
            } else if(query.testState(damagerLoc, localDamager, Flags.FLY) && !query.testState(damageeLoc, localDamagee, Flags.FLY)) {
                getServer().getScheduler().scheduleSyncDelayedTask(this, () -> flyPvP.remove(damager.getUniqueId()), 1L);
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
                } else if(query.testState(damagerLoc, localDamager, Flags.FLY) && !query.testState(damageeLoc, localDamagee, Flags.FLY)) {
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () -> flyPvP.remove(damager.getUniqueId()), 1L);
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
    public void teleportTest(CMIPlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PvPManager pvpmanager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        PlayerHandler playerHandler = Objects.requireNonNull(pvpmanager).getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
        if(!pvpPlayer.isInCombat() && event.getSafe().getTpCondition().equals(Teleportations.TpCondition.Good)) {
            Location toLoc = event.getSafe().getSafeLoc();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            final ApplicableRegionSet regionListTo = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(toLoc.getBlockX(),
                    toLoc.getBlockY(), toLoc.getBlockZ()));
            boolean flyFalse = true;
            for (ProtectedRegion region : regionListTo) {
                if(region.getId().contains("fly") && !region.getId().contains("nofly") && !region.getId().contains("no-fly")) {
                    flyFalse = false;
                    getServer().getScheduler().scheduleSyncDelayedTask(this, () -> player.setAllowFlight(true), 1L);
                    break;
                }
            }
            if(flyFalse) {
                if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                    player.setAllowFlight(false);
                }
            }
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
        if (query.testState(toLoc, localPlayer, Flags.FLY)) {
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
        if (query.testState(toLoc, localPlayer, Flags.FLY)) {
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
                Set<String> soldItems = Objects.requireNonNull(yamlf.getConfigurationSection(player.getUniqueId().toString())).getKeys(false);
                if(soldItems.size() < 5) {
                    for(int i = 0; i < 5; i++) {
                        if(!yamlf.contains(player.getUniqueId() + "." + i)) {
                            yamlf.set(player.getUniqueId() + "." + i + ".time", System.currentTimeMillis());
                            yamlf.set(player.getUniqueId() + "." + i + ".price", event.getResult().getPrice());
                            yamlf.set(player.getUniqueId() + "." + i + ".amount", event.getResult().getAmount());
                            yamlf.set(player.getUniqueId() + "." + i + ".type", event.getResult().getShopItem().getItem().getType().toString());
                            yamlf.save(f);
                            break;
                        }
                    }
                } else {
                    long time = Long.MAX_VALUE;
                    String oldestSold = "";
                    for(String soldItem : soldItems) {
                        long newTime = yamlf.getLong(player.getUniqueId() + "." + soldItem + ".time");
                        if(newTime <= time) {
                            time = newTime;
                            oldestSold = soldItem;
                        }
                    }
                    yamlf.set(player.getUniqueId() + "." + oldestSold + ".time", System.currentTimeMillis());
                    yamlf.set(player.getUniqueId() + "." + oldestSold + ".price", event.getResult().getPrice());
                    yamlf.set(player.getUniqueId() + "." + oldestSold + ".amount", event.getResult().getAmount());
                    yamlf.set(player.getUniqueId() + "." + oldestSold + ".type", event.getResult().getShopItem().getItem().getType().toString());
                    yamlf.save(f);
                }
            } else {
                yamlf.set(player.getUniqueId() + ".0.time", System.currentTimeMillis());
                yamlf.set(player.getUniqueId() + ".0.price", event.getResult().getPrice());
                yamlf.set(player.getUniqueId() + ".0.amount", event.getResult().getAmount());
                yamlf.set(player.getUniqueId() + ".0.type", event.getResult().getShopItem().getItem().getType().toString());
                yamlf.save(f);
            }
        }
    }


    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PvPManager pvpmanager = (PvPManager) Bukkit.getPluginManager().getPlugin("PvPManager");
        PlayerHandler playerHandler = Objects.requireNonNull(pvpmanager).getPlayerHandler();
        PvPlayer pvpPlayer = playerHandler.get(player);
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
            ArrayList<String> guardWorlds = (ArrayList<String>) config.getList("guard-worlds");
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
            ArrayList<String> buildWorlds = (ArrayList<String>) config.getList("builder-worlds");
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
    public void invClick(InventoryClickEvent event) throws IOException {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            Inventory clickInv = event.getClickedInventory();
            if (clickInv != null && !clickInv.isEmpty() && clickInv.getItem(0) != null) {
                ItemStack fItem = clickInv.getItem(0);
                ItemMeta fMeta = Objects.requireNonNull(fItem).getItemMeta();
                PersistentDataContainer fData = fMeta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(this, "stop-click");
                NamespacedKey key1 = new NamespacedKey(this, "gui-type");
                if (fData.has(key, PersistentDataType.INTEGER) && fData.has(key1, PersistentDataType.STRING)) {
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
                                ItemMeta buyMeta = Objects.requireNonNull(buyItem).getItemMeta();
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
                                            NamespacedKey posKey = new NamespacedKey(this, "sold-pos");
                                            String buyPos = buyData.get(posKey, PersistentDataType.STRING);
                                            buyConf.set(player.getUniqueId() + "." + buyPos, null);
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
                                            pMain.setDamage(0);
                                        }

                                        if(!player.hasPermission("skyprisoncore.command.endupgrade.first-time")) {
                                            asConsole("money take " + player.getName() + " " + cost);
                                            player.sendMessage(colourMessage("&f[&aBlacksmith&f] &7Your item" + clickInv.getItem(4).getType() + " &7has been upgraded for &a$" + formatNumber(cost) + "&7!"));
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
                            YamlConfiguration rData = YamlConfiguration.loadConfiguration(rewardsDataFile);
                            File secretsDataFile = new File(this.getDataFolder() + File.separator
                                    + "secretsdata.yml");
                            YamlConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
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
                                    TokenManager tm = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                                    assert tm != null;
                                    tm.addTokens(player, tokenAmount);
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

/*        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
            if(!ChatColor.stripColor(event.getView().getTitle()).contains("Request Settings")
                    || !ChatColor.stripColor(event.getView().getTitle()).contains("Kit Selection (1/1)")) {
                if (isGuardGear(event.getCurrentItem())) {
                    event.setCancelled(true);
                }
                if (player.getOpenInventory().getType() != InventoryType.PLAYER) {
                    InvGuardGearDelOther(player);
                }
                InvGuardGearDelPlyr(player);
            }
        }*/
    }

    @EventHandler
    public void pickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!player.hasPermission("skyprisoncore.contraband.itembypass")
                    && !event.getEntity().getLocation().getWorld().getName().equalsIgnoreCase("events")) {
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
    public void voidFall(EntityRemoveEvent event) {
        if (event.getEntity().getLocation().getY() < -63) {
            if(event.getEntity().getWorld().getName().equalsIgnoreCase("world_prison")) {
                if (event.getEntityType() == EntityType.DROPPED_ITEM) {
                    Item item = (Item) event.getEntity();
                    ItemStack sItem = item.getItemStack();
                    File f = new File(this.getDataFolder() + File.separator + "dropchest.yml");
                    YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
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
                YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
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
                                    YamlConfiguration sDataConf = YamlConfiguration.loadConfiguration(spongeData);
                                    String pUUID = event.getPlayer().getUniqueId().toString();
                                    if(sDataConf.isConfigurationSection(pUUID)) {
                                        int spongeFound = sDataConf.getInt(pUUID + ".sponge-found") + 1;
                                        sDataConf.set(pUUID + ".sponge-found", spongeFound);
                                    } else {
                                        sDataConf.set(pUUID + ".sponge-found", 1);
                                    }
                                    sDataConf.save(spongeData);

                                    TokenManagerPlugin.getInstance().addTokens(event.getPlayer(), 25);
                                    event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + "25 tokens "
                                            + ChatColor.GRAY + "has been added to your balance.");
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
    public void villagerTrade(InventoryOpenEvent event) {
        if (event.getInventory().getType().equals(InventoryType.MERCHANT)) {
            Player player = (Player) event.getPlayer();
            player.sendMessage(ChatColor.RED + "Villager trading has been disabled");
            event.setCancelled(true);
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
    public void mineLogin(PlayerLoginEvent event) {
        CMIUser player = CMI.getInstance().getPlayerManager().getUser(event.getPlayer());
        if(player.getLogOutLocation() != null && player.getLogOutLocation().getWorld().getName().equalsIgnoreCase("world_prison")) {
            Location loc = player.getLogOutLocation();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
            assert regions != null;
            ApplicableRegionSet regionList = regions.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
            if(regionList.getRegions().contains(regions.getRegion("grass-mine"))) {
                asConsole("warp grass-mine " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("desert-mine"))) {
                asConsole("warp desert-mine " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("nether-mine"))) {
                asConsole("warp nether-mine " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("snow-mine"))) {
                asConsole("warp snow-mine " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("donor-mine1"))) {
                asConsole("warp donor-mine " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("donor-mine2"))) {
                asConsole("warp donor-mine1 " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("donor-mine3"))) {
                asConsole("warp donor-mine2 " + player.getName());
            } else if(regionList.getRegions().contains(regions.getRegion("guard-secretview"))) {
                asConsole("warp prison " + player.getName());
            }
        }
    }


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if(!event.isCancelled()) {
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
                if(axes.contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
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

        int pKills = kills.getInt(killer.getUniqueId() + ".pvpkills")+1;
        int pDeaths = kills.getInt(killed.getUniqueId() + ".pvpdeaths")+1;
        int pKillerStreak = kills.getInt(killer.getUniqueId() + ".pvpkillstreak")+1;
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
                TokenManagerPlugin.getInstance().addTokens(killer, 15);
            } else {
                killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "1" + ChatColor.GRAY + " token!");
                TokenManagerPlugin.getInstance().addTokens(killer, 1);
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
                TokenManagerPlugin.getInstance().addTokens(killer, 15);
            } else if(pKillerStreak % 50 == 0 && pKillerStreak > 100) {
                killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillerStreak + "&7! You have received &c&l30 &7tokens as a reward!"));
                TokenManagerPlugin.getInstance().addTokens(killer, 30);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void playerDeath(EntityDeathEvent event) {
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
                                                    killed.sendMessage(colourMessage(colourMessage("&7You have killed players &c&l1000 &7times! Therefore, you get a special tag!")));
                                                }

                                                if(pKillStreak % 5 == 0 && pKillStreak <= 100) {
                                                    killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l15 &7tokens as a reward!"));
                                                    TokenManagerPlugin.getInstance().addTokens(killer, 15);
                                                } else if(pKillStreak % 50 == 0 && pKillStreak > 100) {
                                                    killer.sendMessage(colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l30 &7tokens as a reward!"));
                                                    TokenManagerPlugin.getInstance().addTokens(killer, 30);
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


