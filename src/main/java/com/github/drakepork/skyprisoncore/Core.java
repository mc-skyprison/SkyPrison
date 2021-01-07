package com.github.drakepork.skyprisoncore;

import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.bencodez.votingplugin.user.UserManager;
import com.bencodez.votingplugin.user.VotingPluginUser;
import com.bergerkiller.bukkit.common.events.EntityRemoveEvent;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.github.drakepork.skyprisoncore.Utils.ConfigCreator;
import com.github.drakepork.skyprisoncore.Utils.LangCreator;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import com.github.drakepork.skyprisoncore.Commands.*;
import com.github.drakepork.skyprisoncore.Commands.Chats.BuildChat;
import com.github.drakepork.skyprisoncore.Commands.Donations.DonorAdd;
import com.github.drakepork.skyprisoncore.Commands.Donations.DonorBulk;
import com.github.drakepork.skyprisoncore.Commands.Donations.Purchases;
import com.github.drakepork.skyprisoncore.Commands.Opme.*;
import com.github.drakepork.skyprisoncore.Commands.contraband.CbHistory;
import com.github.drakepork.skyprisoncore.Commands.contraband.Contraband;
import com.github.drakepork.skyprisoncore.Commands.Chats.GuardChat;
import com.github.drakepork.skyprisoncore.Listeners.DiscordSRVListener;
import me.realized.tokenmanager.api.TokenManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


public class Core extends JavaPlugin implements Listener {
    public HashMap<String, String> hexColour = new HashMap<>();
    public HashMap<UUID, String> stickyChatEnabled = new HashMap<>();

    @Inject private BuildChat BuildChat;
    @Inject private GuardChat GuardChat;

    @Inject private DonorAdd DonorAdd;
    @Inject private DonorBulk DonorBulk;
    @Inject private Purchases Purchases;

    @Inject private Deop Deop;
    @Inject private Deopme Deopme;
    @Inject private Op Op;
    @Inject private Opdisable Opdisable;
    @Inject private Opme Opme;

    @Inject private CbHistory CbHistory;
    @Inject private Contraband Contraband;

    @Inject private Bounty Bounty;
    @Inject private Cheese Cheese;
    @Inject private DropChest DropChest;
    @Inject private KillInfo KillInfo;
    @Inject private MineTP MineTP;
    @Inject private playtimeRewards playtimeRewards;
    @Inject private Referral Referral;
    @Inject private RewardGUI RewardGUI;
    @Inject private SpongeLoc SpongeLoc;
    @Inject private EconomyCheck EconomyCheck;

    @Inject private ConfigCreator configCreator;
    @Inject private LangCreator langCreator;

    private DiscordSRVListener discordsrvListener = new DiscordSRVListener(this);
    FileConfiguration config = this.getConfig();

    public void onEnable() {
        DiscordSRV.api.subscribe(discordsrvListener);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        this.configCreator.init();
        this.langCreator.init();

        ArrayList files = new ArrayList();
        files.add("bounties.yml");
        files.add("spongeLocations.yml");
        files.add("regionLocations.yml");
        files.add("dropChest.yml");
        files.add("rewardGUI.yml");
        files.add("donations");
        files.add("watchList.yml");
        files.add("recentKills.yml");
        files.add("referrals.yml");
        files.add("logs");
        files.add("logs/staffchat");
        for (int i = 0; i < files.size(); i++) {
            File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                    .getDataFolder() + "/" + files.get(i));
            String file = (String) files.get(i);
            if(!f.exists()) {
                if(file.contains(".") ) {
                    try {
                        f.createNewFile();
                        getLogger().info("File " + files.get(i) + " successfully created");
                    } catch (IOException e) {
                        /*                    e.printStackTrace();*/
                        getLogger().info("File " + files.get(i) + " failed to create");
                    }
                } else {
                    f.mkdir();
                    getLogger().info("Folder " + files.get(i) + " successfully created");
                }
            }
        }

        String url = "jdbc:sqlite:"+ Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                .getDataFolder() + "/SkyPrisonDB.db";
        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                getLogger().info("The driver name is " + meta.getDriverName());
                getLogger().info("A new database has been created.");
            }
        } catch (SQLException e) {
            getLogger().info(e.getMessage());
        }


        getCommand("g").setExecutor(this.GuardChat);
        getCommand("b").setExecutor(this.BuildChat);
        getCommand("spongeloc").setExecutor(this.SpongeLoc);
        getCommand("dropchest").setExecutor(this.DropChest);
        getCommand("minetp").setExecutor(this.MineTP);
        getCommand("opme").setExecutor(this.Opme);
        getCommand("deopme").setExecutor(this.Deopme);
        getCommand("rewards").setExecutor(this.RewardGUI);
        getCommand("contraband").setExecutor(this.Contraband);
        getCommand("cbhistory").setExecutor(this.CbHistory);
        getCommand("donoradd").setExecutor(this.DonorAdd);
        getCommand("donorbulk").setExecutor(this.DonorBulk);
        getCommand("purchases").setExecutor(this.Purchases);
        getCommand("bounty").setExecutor(this.Bounty);
        getCommand("killinfo").setExecutor(this.KillInfo);
        getCommand("cheese").setExecutor(this.Cheese);
        getCommand("referral").setExecutor(this.Referral);
        getCommand("test").setExecutor(this.playtimeRewards);
        getCommand("econcheck").setExecutor(this.EconomyCheck);
        if (getConfig().getBoolean("enable-op-command")) {
            getCommand("op").setExecutor(new Op());
        } else {
            getCommand("op").setExecutor(new Opdisable());
        }
        if (getConfig().getBoolean("enable-deop-command")) {
            getCommand("deop").setExecutor(new Deop());
        } else {
            getCommand("deop").setExecutor(new Opdisable());
        }
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    PacketContainer packet = event.getPacket();
                    if(packet.getChatComponents().read(0).toString() != null && !packet.getChatComponents().read(0).toString().isEmpty()) {
                        if (packet.getChatComponents().read(0).toString() instanceof String) {
                            String chatMsg = packet.getChatComponents().read(0).toString();
                            if (chatMsg.contains("Next ranks:")) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled SkyPrisonCore v" + getDescription().getVersion());
        DiscordSRV.api.unsubscribe(discordsrvListener);
    }


    public String colourMessage(String message){
        message = translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }


    public String removeColour(String message){
        message = removeColorCodes(ChatColor.translateAlternateColorCodes('&', message));
        return message;
    }

    public void tellConsole(String message){
        Bukkit.getConsoleSender().sendMessage(message);
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
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "\\}");
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


    public String removeColorCodes(String message) {
        if(StringUtils.substringsBetween(message, "{#", "}") != null) {
            String[] hexNames = StringUtils.substringsBetween(message, "{#", "}");
            for (String hexName : hexNames) {
                if (hexColour.get(hexName.toLowerCase()) != null) {
                    message = message.replaceAll("\\{#" + hexName + "\\}", "");
                }
            }
        }
        final Pattern hexPattern = Pattern.compile("\\{#" + "([A-Fa-f0-9]{6})" + "\\}");
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, "");
        }
        return matcher.appendTail(buffer).toString();
    }

    @EventHandler
    public void stickyChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(stickyChatEnabled.containsKey(player.getUniqueId())) {
            File lang = new File(this.getDataFolder() + File.separator
                    + "lang" + File.separator + this.getConfig().getString("lang-file"));
            FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
            event.setCancelled(true);
            String stickiedChat = stickyChatEnabled.get(player.getUniqueId());
            String[] split = stickiedChat.split("-");

            String message = event.getMessage();
            String format = langConf.getString("chat." + split[0] + ".format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
            message = format.replaceAll("\\[message\\]", Matcher.quoteReplacement(message));
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.hasPermission("royalasylum.chat." + split[0])) {
                    online.sendMessage(translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));
                }
            }

            Bukkit.getConsoleSender().sendMessage(translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));

            String dFormat = langConf.getString("chat.discordSRV.format").replaceAll("\\[name\\]", Matcher.quoteReplacement(player.getName()));
            String dMessage = dFormat.replaceAll("\\[message\\]", Matcher.quoteReplacement(event.getMessage()));
            TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(split[0] + "-chat");
            channel.sendMessage(dMessage).queue();
        }
    }




    //
    // Creates lists of people that have been /cb, and also creates the list containing all of the contraband
    //

    public List<Material> contraband() {
        ArrayList arr = (ArrayList) config.getList("contrabands");
        List<Material> contraband = Lists.newArrayList();
        for(int i = 0; i < arr.size(); i++) {
                contraband.add(Material.getMaterial(arr.get(i).toString().toUpperCase()));
        }
        return contraband;
    }
    public ArrayList<Player> cbed = new ArrayList();
    public HashMap<Player, Player> cbedMap = new HashMap();
    public Map<Player, Map.Entry<Player, Long>> hitcd = new HashMap();
    public Map<String, HashMap<String, Inventory>> cbGuards = new HashMap();
    public boolean isGuardGear(ItemStack i) {
        if (i != null) {
            if (i.getType() == Material.CHAINMAIL_HELMET || i.getType() == Material.CHAINMAIL_CHESTPLATE || i.getType() == Material.CHAINMAIL_LEGGINGS || i.getType() == Material.CHAINMAIL_BOOTS || i.getType() == Material.DIAMOND_SWORD) {
                return true;
            } else if (i.getType() == Material.BOW) {
                if (i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Guard Bow")) {
                    return true;
                } else {
                    return false;
                }
            } else if (i.getType() == Material.SHIELD) {
                if (i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Guard Shield")) {
                    return true;
                } else {
                    return false;
                }
            } else if (i.getType() == Material.LEAD && i.getItemMeta().hasDisplayName() && i.getItemMeta().getDisplayName().contains("Cuffs")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private void InvGuardGearDelPlyr(Player player) {
        for (int n = 0; n < player.getInventory().getSize(); n++) {
            ItemStack i = player.getInventory().getItem(n);
            if (i != null &&
                    isGuardGear(i)) {
                i.setAmount(0);
            }
        }
    }

    private void InvGuardGearDelOther(Player player) {
        boolean deletedsomething = false;
        for (int n = 0; n < player.getOpenInventory().getTopInventory().getSize(); n++) {
            ItemStack i = player.getOpenInventory().getTopInventory().getItem(n);
            if (i != null &&
                    isGuardGear(i)) {
                i.setAmount(0);
                deletedsomething = true;
            }
        }

        if (deletedsomething) {
            player.closeInventory();
        }
    }

    private void cbedRemInv(Player target, Player guard) {
        int m = 0;
        target.closeInventory();
        Inventory cbInv = Bukkit.getServer().createInventory(null, 27, ChatColor.DARK_RED + "Contraband! " + ChatColor.RED + target.getName());
        for (int n = 0; n < target.getInventory().getSize(); n++) {
            ItemStack i = target.getInventory().getItem(n);
            if (i != null) {
                for (Material cb : contraband()) {
                    if (i.getType() == cb && m < 27) {
                        ItemStack newcb = new ItemStack(i.getType());
                        ItemMeta newmeta = i.getItemMeta();
                        newcb.setItemMeta(newmeta);
                        cbInv.setItem(m, newcb);
                        m++;
                        i.setAmount(0);
                    }
                }
            }
        }
        if(this.cbGuards.get(guard.getName().toLowerCase()) == null) {
            HashMap<String, Inventory> cbArchive = new HashMap<String, Inventory>();
            cbArchive.put("blank", cbInv);
            this.cbGuards.put(guard.getName().toLowerCase(), cbArchive);
        }
        HashMap<String, Inventory> cbArchive = this.cbGuards.get(guard.getName().toLowerCase());
        cbArchive.put(target.getName().toLowerCase(), cbInv);
        this.cbGuards.put(guard.getName().toLowerCase(), cbArchive);
        guard.openInventory(cbInv);
        guard.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " has handed over their contraband!");
    }

    public static void wlistCleanup(File f, YamlConfiguration yamlf) {
        long current = System.currentTimeMillis()/1000L;
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (String key : yamlf.getKeys(false)) {
            long expire = yamlf.getLong(key + ".expire");
            if(current > expire) {
                yamlf.set(key, null);
                try {
                    yamlf.save(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //
    // EventHandlers regarding RanksPkg
    //
    /*@EventHandler
    public void cbinvclose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if(human instanceof Player) {
            Player closer = Bukkit.getPlayer(human.getUniqueId());
            Map.Entry<Player, Long> lasthit = (Map.Entry) this.hitcd.get();
            if(event.getInventory() == this.cbhist.)
        }
    }*/
    @EventHandler
    public void cbedChat(AsyncPlayerChatEvent event) {
        final Player target = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (this.cbed.contains(target)) {
            if (args[0].equalsIgnoreCase("yes")) {
                event.setCancelled(true);
                target.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You have selected to turn over your contraband. All contraband items have been removed from your inventory!");
                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        cbed.remove(target);
                        cbedRemInv(target, (Player) cbedMap.get(target));
                        cbedMap.remove(target);
                    }
                }, 5L);
            } else if (args[0].equalsIgnoreCase("no")) {
                event.setCancelled(true);
                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                    public void run() {
                        cbed.remove(target);
                        Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "jail " + target.getName());
                        target.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You have selected to go to jail. All contraband items will remain in your inventory!");
                        (cbedMap.get(target)).sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + target.getName() + ChatColor.YELLOW + " has gone to jail!");
                        cbedMap.remove(target);
                    }
                }, 5L);
            } else {
                target.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "Please respond Yes or No before you proceed...");
                event.setCancelled(true);
            }
        }
    }



    @EventHandler
    public void guardHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damagee = (Player) event.getEntity();
            if(damager.hasPermission("skyprisoncore.guard.onduty") && damagee.hasPermission("skyprisoncore.guard.onduty")) {
                event.setCancelled(true);
            } else if (damagee.hasPermission("skyprisoncore.showhit")) {
                Map.Entry<Player, Long> lasthit = (Map.Entry) this.hitcd.get(damager);
                if (hitcd.get(damager) == null || (lasthit.getKey() == damagee && System.currentTimeMillis() / 1000L - ((Long) lasthit.getValue()).longValue() > 5L) || lasthit.getKey() !=damagee) {
                    damagee.sendMessage(ChatColor.RED + "You have been hit by " + damager.getName());
                    hitcd.put(damager, new AbstractMap.SimpleEntry(damagee, Long.valueOf(System.currentTimeMillis() / 1000L)));
                }
            }
        }
    }

    @EventHandler
    public void moveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (cbed.contains(player)) {
            event.setCancelled(true);
        }
        if (player.hasPermission("skyprisoncore.guard.onduty")) {
            ArrayList arr = (ArrayList) config.getList("guard-worlds");
            Boolean inWorld = false;
            for(int i = 0; i < arr.size(); i++) {
                if(player.getWorld().getName().equalsIgnoreCase((String) arr.get(i))) {
                    inWorld = true;
                    break;
                }
            }
            if(inWorld == false) {
                event.setCancelled(true);
                player.sendMessage("" + ChatColor.RED + "Please go off duty when leaving the prison world!");
            }
        }
        if(player.hasPermission("skyprisoncore.builder.onduty")) {
            ArrayList arr = (ArrayList) config.getList("builder-worlds");
            Boolean inWorld = false;
            for(int i = 0; i < arr.size(); i++) {
                if(player.getWorld().getName().equalsIgnoreCase((String) arr.get(i))) {
                    inWorld = true;
                    break;
                }
            }
            if(inWorld == false) {
                event.setCancelled(true);
                player.sendMessage("" + ChatColor.RED + "Please go off duty when leaving the build worlds!");
            }
        }
    }

    @EventHandler
    public boolean invClick(InventoryClickEvent event) {
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
                case "skycity-other":
                case "prison-other":
                    if (event.getSlot() == 40) {
                        RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                    }
                    break;
                case "all":
                    switch (event.getSlot()) {
                        case 31:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                            break;
                        case 11:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "grass");
                            break;
                        case 12:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "desert");
                            break;
                        case 13:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "nether");
                            break;
                        case 14:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "snow");
                            break;
                        case 15:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "prison-other");
                            break;
                        case 20:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "skycity");
                            break;
                        case 21:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "skycity-other");
                            break;
                    }
                    break;
                case "rewards":
                    if (event.getCurrentItem() == null) {
                        break;
                    }
                    if (event.getSlot() == 49) {
                        RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                    } else if (event.getCurrentItem().getType().equals(Material.CHEST_MINECART)) {
                        Player player = Bukkit.getPlayer(human.getName());
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
                            if(rData.getString(foundValue + ".reward-type").equalsIgnoreCase("points")) {
                                int pointAmount = rData.getInt(foundValue + ".reward");
                                VotingPluginUser user = UserManager.getInstance().getVotingPluginUser(player);
                                Bukkit.getScheduler().runTaskAsynchronously(this, () -> user.addPoints(pointAmount));
                                pData.set(player.getUniqueId().toString() + ".rewards." + foundValue + ".collected", true);
                                try {
                                    pData.save(secretsDataFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                player.sendMessage(colourMessage("&f[&eSecrets&f] &aYou received " + pointAmount + " points!"));
                                RewardGUI.openGUI(player, "rewards");
                            } else if(rData.getString(foundValue + ".reward-type").equalsIgnoreCase("tokens")) {
                                int tokenAmount = rData.getInt(foundValue + ".reward");
                                TokenManager tm = (TokenManager) Bukkit.getServer().getPluginManager().getPlugin("TokenManager");
                                tm.addTokens(player, tokenAmount);
                                pData.set(player.getUniqueId().toString() + ".rewards." + foundValue + ".collected", true);
                                try {
                                    pData.save(secretsDataFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                player.sendMessage(colourMessage("&f[&eSecrets&f] &aYou received " + tokenAmount + " tokens!"));
                                RewardGUI.openGUI(player, "rewards");
                            }
                        }
                    }
                    break;
                case "main":
                    switch(event.getSlot()) {
                        case 13:
                            break;
                        case 20:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                            break;
                        case 24:
                            RewardGUI.openGUI(Bukkit.getPlayer(human.getName()), "rewards");
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

        Player player = (Player) event.getWhoClicked();
        if (cbed.contains(player)) {
            event.setCancelled(true);
            if (player.getOpenInventory().getTitle().equalsIgnoreCase(ChatColor.DARK_RED + "You've been caught with contraband!")) {
                if (event.getSlot() == 11) {
                    player.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You have selected to turn over your contraband. All contraband items have been removed from your inventory!");
                    cbed.remove(player);
                    cbedMap.get(player).sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " has given up their contraband!");
                    cbedRemInv(player, cbedMap.get(player));
                    cbedMap.remove(player);
                    return true;
                }
                if (event.getSlot() == 15) {
                    this.cbed.remove(player);
                    Bukkit.getServer().dispatchCommand(getServer().getConsoleSender(), "jail " + player.getName());
                    player.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + "You have selected to go to jail. All contraband items will remain in your inventory!");
                    (cbedMap.get(player)).sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.GOLD + player.getName() + ChatColor.YELLOW + " has gone to jail!");
                    cbedMap.remove(player);
                    return true;
                }
            } else {
                return false;
            }
        }
        if (!player.hasPermission("skyprisoncore.guard.itembypasss")
                && !player.getLocation().getWorld().getName().equalsIgnoreCase("prison")
                && !player.getLocation().getWorld().getName().equalsIgnoreCase("events")
                && !player.getOpenInventory().getType().equals(InventoryType.CREATIVE)) {
            if (isGuardGear(event.getCurrentItem())) {
                event.setCancelled(true);
            }
            if (player.getOpenInventory().getType() != InventoryType.PLAYER) {
                InvGuardGearDelOther(player);
            }
            InvGuardGearDelPlyr(player);
            return true;
        } else {
            return false;
        }
    }

    @EventHandler
    public void pickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (!player.hasPermission("skyprisoncore.guard.itembypass")
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
            if(event.getEntity().getWorld().getName().equalsIgnoreCase("prison")) {
                if (event.getEntityType() == EntityType.DROPPED_ITEM) {
                    Item item = (Item) event.getEntity();
                    ItemStack sItem = item.getItemStack();
                    File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                            .getDataFolder() + "/dropChest.yml");
                    YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                    if (!yamlf.isConfigurationSection("items")) {
                        yamlf.createSection("items");
                    }
                    try {
                        yamlf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Set<String> dropList = yamlf.getConfigurationSection("items").getKeys(false);
                    int page = 0;
                    for (int i = 0; i < dropList.size() + 2; ) {
                        ArrayList arr = new ArrayList();
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
                            continue;
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
    public void spongeEvent(BlockDamageEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if (b.getType() == Material.SPONGE) {
            if (loc.getWorld().getName().equalsIgnoreCase("prison") || loc.getWorld().getName().equalsIgnoreCase("event_world")) {
                File f = new File("plugins/SkyPrisonCore/spongeLocations.yml");
                YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                Set setList = yamlf.getConfigurationSection("locations").getKeys(false);
                for (int i = 0; i < setList.size(); i++) {
                    if (yamlf.contains("locations." + i)) {
                        World w = Bukkit.getServer().getWorld(yamlf.getString("locations." + i + ".world"));
                        Location spongeLoc = new Location(w, yamlf.getDouble("locations." + i + ".x"), yamlf.getDouble("locations." + i + ".y"), yamlf.getDouble("locations." + i + ".z"));
                        spongeLoc = spongeLoc.getBlock().getLocation();
                        if (loc.equals(spongeLoc)) {
                            loc.getBlock().setType(Material.AIR);
                            for (int v = 0; v < setList.size(); v++) {
                                Random random = new Random();
                                int rand = random.nextInt(setList.size());
                                Location placeSponge = new Location(w, yamlf.getDouble("locations." + rand + ".x"), yamlf.getDouble("locations." + rand + ".y"), yamlf.getDouble("locations." + rand + ".z"));
                                placeSponge = placeSponge.getBlock().getLocation();
                                if (!placeSponge.equals(loc)) {
                                    for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                        online.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW + "Sponge" + ChatColor.WHITE + "] " + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW + " has found the sponge! A new one will be hidden somewhere in prison.");
                                    }
                                    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                                    String command = "cmi usermeta " + event.getPlayer().getName() + " increment spongefound +1";
                                    Bukkit.dispatchCommand(console, command);
                                    command = "tokensadd " + event.getPlayer().getName() + " 25";
                                    Bukkit.dispatchCommand(console, command);
                                    break;
                                }
                            }
                            break;
                        }
                    } else {
                        continue;
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
        if (event.getInventory().getType() != InventoryType.MERCHANT) {
        } else if (event.getInventory().getType().equals(InventoryType.MERCHANT)) {
            Player player = (Player) event.getPlayer();
            player.sendMessage(ChatColor.RED + "Villager trading has been disabled");
            event.setCancelled(true);
        }
    }

    //
// EventHandlers regarding Farming & Mining
//
    @EventHandler
    public void cactusGrow(ItemSpawnEvent event) {
        ItemStack b = event.getEntity().getItemStack();
        Location loc = event.getLocation();
        if (b.getType() == Material.CACTUS && loc.getWorld().getName().equalsIgnoreCase("world")) {
            int random = (int) (Math.random() * 10 + 1);
            if (random == 10) {
            }
        }
    }

    public static ItemStack setItemDamage(ItemStack item, int damage) {
        Damageable im = (Damageable) item.getItemMeta();
        int dmg = im.getDamage();
        if(item.containsEnchantment(Enchantment.DURABILITY)) {
            int enchantLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
            im.setDamage(damage / enchantLevel + dmg);
        } else {
            im.setDamage(damage + dmg);
        }
        item.setItemMeta((ItemMeta) im);
        return item;
    }


    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if(!event.isCancelled()) {
            if (b.getType() == Material.SNOW_BLOCK && loc.getWorld().getName().equalsIgnoreCase("prison")) {
                event.setDropItems(false);
                Location cob = loc.add(0.5D, 0.0D, 0.5D);
                ItemStack snowblock = new ItemStack(Material.SNOW_BLOCK, 1);
                loc.getWorld().dropItem(cob, snowblock);
            } else if (b.getType() == Material.SNOW_BLOCK && loc.getWorld().getName().equalsIgnoreCase("events")) {
                event.setDropItems(false);
            } else if (b.getType() == Material.BIRCH_LOG && loc.getWorld().getName().equalsIgnoreCase("prison")) {
                ArrayList axes = new ArrayList();
                axes.add(Material.DIAMOND_AXE);
                axes.add(Material.GOLDEN_AXE);
                axes.add(Material.IRON_AXE);
                axes.add(Material.STONE_AXE);
                axes.add(Material.WOODEN_AXE);
                axes.add(Material.NETHERITE_AXE);
                if(axes.contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                    Boolean birchDown = true;
                    int birchDrops = 0;
                    Location birchLoc;
                    Location saplingLoc;
                    int i = 0;
                    while (birchDown) {
                        birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i, loc.getBlockZ());
                        if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                            birchLoc.getBlock().setType(Material.AIR);
                            birchDrops++;
                            i++;
                        } else {
                            saplingLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - i + 1, loc.getBlockZ());
                            Location finalSaplingLoc = saplingLoc;
                            if (birchLoc.getBlock().getType() == Material.GRASS_BLOCK || birchLoc.getBlock().getType() == Material.DIRT) {
                                getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                    public void run() {
                                        finalSaplingLoc.getBlock().setType(Material.BIRCH_SAPLING);
                                    }
                                }, 2L);
                            }
                            birchDown = false;
                        }
                    }
                    Boolean birchUp = true;
                    int x = 1;
                    while (birchUp) {
                        birchLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() + x, loc.getBlockZ());
                        if (birchLoc.getBlock().getType() == Material.BIRCH_LOG) {
                            birchLoc.getBlock().setType(Material.AIR);
                            birchDrops++;
                            x++;
                        } else {
                            birchUp = false;
                        }
                    }
                    ItemStack birchLog = new ItemStack(Material.BIRCH_LOG, birchDrops);
                    loc.getWorld().dropItem(loc, birchLog);
                    ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                    setItemDamage(item, birchDrops);
                } else {
                    Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
                    if (newLoc.getBlock().getType() == Material.GRASS_BLOCK || newLoc.getBlock().getType() == Material.DIRT) {
                        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                            public void run() {
                                loc.getBlock().setType(Material.BIRCH_SAPLING);
                            }
                        }, 2L);
                    }
                }
            } else if (b.getType() == Material.WHEAT && loc.getWorld().getName().equalsIgnoreCase("prison")) {
                if (!event.getPlayer().isOp()) {
                    BlockData bdata = b.getBlockData();
                    if (bdata instanceof Ageable) {
                        Ageable age = (Ageable) bdata;
                        if (age.getAge() != age.getMaximumAge()) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "This wheat isn't ready for harvest..");
                        } else {
                            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                                public void run() {
                                    loc.getBlock().setType(Material.WHEAT);
                                }
                            }, 2L);
                        }
                    }
                }
            }
        }
    }

    //
    // EventHandlers regarding OpMe commands
    //
    @EventHandler
    public void deOpOnJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (config.getBoolean("deop-on-join")) {
            if (p.isOp() && !p.hasPermission("skyprisoncore.deop.joinbypass")) {
                p.setOp(false);
            }
        }
/*        String url = "jdbc:sqlite:"+ Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                .getDataFolder() + "/SkyPrisonDB.db";
        String sql = "CREATE TABLE IF NOT EXISTS user (\n"
                + " id integer PRIMARY KEY,\n"
                + " name text NOT NULL,\n"
                + " capacity real\n"
                + ");";
        try{
            Connection conn = DriverManager.getConnection(url);
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        String sql1 = "INSERT INTO user(name) VALUES(?)";

        try{
            Connection conn = DriverManager.getConnection(url);
            PreparedStatement pstmt = conn.prepareStatement(sql1);
            pstmt.setString(1, p.getUniqueId().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }*/
    }

    @EventHandler
    public void consoleCommands(ServerCommandEvent event) {
        String[] args = event.getCommand().split(" ");
        if(args[0].equalsIgnoreCase("cmi") && args[1].equalsIgnoreCase("staffmsg")) {
            TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
            ArrayList msg = new ArrayList();
            for(int i = 2; i < args.length; i++) {
                msg.add(args[i]);
            }
            String string = String.join(" ", msg);
            channel.sendMessage("**Console**: " + string).queue();
        }

        if(args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("adminchat")) {
            TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("admin-chat");
            ArrayList msg = new ArrayList();
            for(int i = 1; i < args.length; i++) {
                msg.add(args[i]);
            }
            String string = String.join(" ", msg);
            channel.sendMessage("**Console**: " + string).queue();
        }
    }

    @EventHandler
    public void commands(PlayerCommandPreprocessEvent event) throws IOException {
        Player player = event.getPlayer();
        String[] args = event.getMessage().split(" ");
        if (player.equals(Bukkit.getPlayer("blueberry09"))) {
            if (args[0].equalsIgnoreCase("/cmi") && args[1].equalsIgnoreCase("cuff") && (args[2].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("false"))) {
                event.setCancelled(true);
            }
        }

        if(args[0].equalsIgnoreCase("/cmi") && args[1].equalsIgnoreCase("staffmsg")) {
            if(player.hasPermission("cmi.command.staffmsg")) {
                if(args.length > 2) {
                    TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("staff-chat");
                    ArrayList msg = new ArrayList();
                    for(int i = 2; i < args.length; i++) {
                        msg.add(args[i]);
                    }
                    String string = String.join(" ", msg);
                    channel.sendMessage("**" + player.getName() + "**: " + string).queue();

                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date();
                    String currDate = dateFormat.format(date);
                    File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                            .getDataFolder() + "/logs/staffchat/" + currDate + ".txt");
                    String message = "[" + timeFormat.format(date) + "] " + player.getName() + ": " + string;
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(f, true);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.append(message);
                    bufferedWriter.newLine();
                    bufferedWriter.close();
                }
            }
        }

        if(args[0].equalsIgnoreCase("/a") || args[0].equalsIgnoreCase("/adminchat")) {
            if(player.hasPermission("mcmmo.chat.adminchat ")) {
                if(args.length > 1) {
                    TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("admin-chat");
                    ArrayList msg = new ArrayList();
                    for(int i = 1; i < args.length; i++) {
                        msg.add(args[i]);
                    }
                    String string = String.join(" ", msg);
                    channel.sendMessage("**" + player.getName() + "**: " + string).queue();
                }
            }
        }

        if (event.getMessage().startsWith("/") && event.getMessage().contains(":op")
                | event.getMessage().contains(":OP") | event.getMessage().contains(":Op")
                | event.getMessage().contains(":oP") | event.getMessage().contains(":deop")
                | event.getMessage().contains(":DEOP") | event.getMessage().contains(":Deop")
                | event.getMessage().contains(":dEop") | event.getMessage().contains(":deOp")
                | event.getMessage().contains(":deoP") | event.getMessage().contains(":DEop")
                | event.getMessage().contains(":dEOp") | event.getMessage().contains(":deOP")
                | event.getMessage().contains(":DeoP") | event.getMessage().contains(":DEOp")
                | event.getMessage().contains(":dEOP") | event.getMessage().contains(":DeOP")
                | event.getMessage().contains(":DEoP")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.WHITE + "Unknown command. Type " + '"' + "/help" + '"' + " for help.");
        }
    }

    //
    //Event Handlers regarding silent join/leave
    //

    @EventHandler
    public void silentLogOff(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!player.getPlayer().equals(Bukkit.getPlayer("DrakePork"))) {
            if (player.hasPermission("cmi.messages.disablequit")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "s " + player.getName() + " has left silently...");
            }
        }
    }

    @EventHandler
    public void silentJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!player.getPlayer().equals(Bukkit.getPlayer("DrakePork"))) {
            if (player.hasPermission("cmi.messages.disablelogin")) {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "s " + player.getName() + " has joined silently...");
            }
        }
    }

    //
    // Event Handlers regarding watchlist
    //
    @EventHandler
    public void watchListJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                .getDataFolder() + "/watchList.yml");
        YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
        wlistCleanup(f,yamlf);
        if(yamlf.contains(player.getUniqueId().toString())) {
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.hasPermission("skyprisoncore.watchlist.basic") && !player.hasPermission("skyprisoncore.watchlist.silent")) {
                    online.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "WATCHLIST" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " " + ChatColor.RED + player.getName() + ChatColor.YELLOW + " has just logged on and is on the watchlist. Please use /watchlist <player> to see why...");
                }
            }
        }
    }


    @EventHandler
    public void retroDeath(PlayerDeathEvent event) {
        if(event.getEntity().equals(Bukkit.getPlayer(UUID.fromString("c9ce0599-b87a-490f-8cb1-6d58f2594be7")))) {
            event.setDeathMessage("");
        }
    }

    //
    // Event Handlers regarding bounties
    //

    public void PvPSet(Player killed, Player killer) {
        File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                .getDataFolder() + "/recentKills.yml");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileConfiguration kills = YamlConfiguration.loadConfiguration(f);
        VotingPluginUser userTokens = UserManager.getInstance().getVotingPluginUser(killer);


        int pKills = kills.getInt(killer.getUniqueId().toString() + ".pvpkills")+1;
        int pDeaths = kills.getInt(killed.getUniqueId().toString() + ".pvpdeaths")+1;
        int pKillerStreak = kills.getInt(killer.getUniqueId().toString() + ".pvpkillstreak")+1;
        if(!kills.contains(killer.getUniqueId().toString() + ".pvpdeaths")) {
            kills.set(killer.getUniqueId().toString() + ".pvpdeaths", 0);
        }
        kills.set(killer.getUniqueId().toString() + ".pvpkills", pKills);
        kills.set(killer.getUniqueId().toString() + ".pvpkillstreak", pKillerStreak);
        kills.set(killer.getUniqueId().toString() + ".kills." + killed.getUniqueId().toString() + ".time", System.currentTimeMillis());

        kills.set(killed.getUniqueId().toString() + ".pvpkillstreak", 0);
        kills.set(killed.getUniqueId().toString() + ".pvpdeaths", pDeaths);
        try {
            kills.save(f);
            if(killed.hasPermission("skyprisoncore.guard.onduty")) {
                killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "15" + ChatColor.GRAY + " token!");
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> userTokens.addPoints(15));
            } else {
                killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "1" + ChatColor.GRAY + " token!");
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> userTokens.addPoints(1));
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void playerRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        if(player.getWorld().getName().equalsIgnoreCase("prison")) {
            Location loc = player.getLocation();
            player.teleportAsync(loc);
        }
    }

    @EventHandler
    public void anvilStop(InventoryOpenEvent event) {
        if(event.getInventory().getType().equals(InventoryType.ANVIL)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player && event.getEntity().getKiller() instanceof Player) {
            Player killed = (Player) event.getEntity();
            Player killer = killed.getKiller();
            if(!killed.equals(killer)) {
                //
                // Bounty Stuff
                //
                File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                        .getDataFolder() + "/bounties.yml");
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileConfiguration bounty = YamlConfiguration.loadConfiguration(f);
                Set<String> bountyList = bounty.getKeys(false);
                for (String bountyPlayer : bountyList) {
                    if(killed.getUniqueId().equals(UUID.fromString(bountyPlayer))) {
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
                f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
                        .getDataFolder() + "/recentKills.yml");
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                FileConfiguration kills = YamlConfiguration.loadConfiguration(f);
                CMIUser userK = CMI.getInstance().getPlayerManager().getUser(killer);
                CMIUser userD = CMI.getInstance().getPlayerManager().getUser(killed);
                if(!userD.getLastIp().equalsIgnoreCase(userK.getLastIp())) {
                    if(kills.contains(killer.getUniqueId().toString() + ".kills")) {
                        Set<String> killsList = kills.getConfigurationSection(killer.getUniqueId().toString() + ".kills").getKeys(false);
                        if(killsList.contains(killed.getUniqueId().toString())) {
                            for(String killedPlayer : killsList) {
                                if(killed.getUniqueId().equals(UUID.fromString(killedPlayer))) {
                                    Long time = kills.getLong(killer.getUniqueId().toString() + ".kills." + killedPlayer + ".time");
                                    Long timeLeft = System.currentTimeMillis() - time;
                                    if(TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 300) {
                                        PvPSet(killed, killer);
                                    } else {
                                        int pKills = kills.getInt(killer.getUniqueId().toString() + ".pvpkills")+1;
                                        int pDeaths = kills.getInt(killed.getUniqueId().toString() + ".pvpdeaths")+1;
                                        int pKillStreak = kills.getInt(killer.getUniqueId().toString() + ".pvpkillstreak")+1;
                                        kills.set(killer.getUniqueId().toString() + ".pvpkills", pKills);
                                        kills.set(killer.getUniqueId().toString() + ".pvpkillstreak", pKillStreak);

                                        kills.set(killed.getUniqueId().toString() + ".pvpkillstreak", 0);
                                        kills.set(killed.getUniqueId().toString() + ".pvpdeaths", pDeaths);
                                        try {
                                            kills.save(f);
                                            Long timeRem = 300 - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                            killer.sendMessage(ChatColor.GRAY + "You have to wait " + ChatColor.RED + timeRem + ChatColor.GRAY + " seconds before receiving tokens from this player!");
                                        } catch (IOException e) {
                                            e.printStackTrace();
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


