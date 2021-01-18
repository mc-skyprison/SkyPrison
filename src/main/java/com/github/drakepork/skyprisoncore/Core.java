package com.github.drakepork.skyprisoncore;

import java.io.*;
import java.util.*;
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
import com.github.drakepork.skyprisoncore.Commands.Chats.*;
import com.github.drakepork.skyprisoncore.Commands.Donations.DonorCheck;
import com.github.drakepork.skyprisoncore.Commands.contraband.Bow;
import com.github.drakepork.skyprisoncore.Commands.contraband.Sword;
import com.github.drakepork.skyprisoncore.Commands.referral.Referral;
import com.github.drakepork.skyprisoncore.Commands.referral.ReferralList;
import com.github.drakepork.skyprisoncore.Utils.ConfigCreator;
import com.github.drakepork.skyprisoncore.Utils.LangCreator;
import com.google.inject.Inject;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import com.github.drakepork.skyprisoncore.Commands.*;
import com.github.drakepork.skyprisoncore.Commands.Donations.DonorAdd;
import com.github.drakepork.skyprisoncore.Commands.Donations.DonorBulk;
import com.github.drakepork.skyprisoncore.Commands.Donations.Purchases;
import com.github.drakepork.skyprisoncore.Listeners.Discord;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.api.TokenManager;
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
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
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
    @Inject private AdminChat AdminChat;
    @Inject private StaffChat StaffChat;
    @Inject private DiscordChat DiscordChat;

    @Inject private DonorAdd DonorAdd;
    @Inject private DonorBulk DonorBulk;
    @Inject private Purchases Purchases;
    @Inject private DonorCheck DonorCheck;

    @Inject private ReferralList ReferralList;
    @Inject private Referral Referral;

    @Inject private Bounty Bounty;
    @Inject private KillInfo KillInfo;

    @Inject private Sword Sword;
    @Inject private Bow Bow;

    @Inject private EconomyCheck EconomyCheck;
    @Inject private PermShop PermShop;

    @Inject private DropChest DropChest;
    @Inject private TokenTeleport TokenTeleport;
    @Inject private playtimeRewards playtimeRewards;
    @Inject private RewardGUI RewardGUI;
    @Inject private SpongeLoc SpongeLoc;
    @Inject private FirstjoinTop FirstjoinTop;

    @Inject private ConfigCreator configCreator;
    @Inject private LangCreator langCreator;

    private Discord discordListener = new Discord(this);
    FileConfiguration config = this.getConfig();

    public void onEnable() {
        github.scarsz.discordsrv.DiscordSRV.api.subscribe(discordListener);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        this.configCreator.init();
        this.langCreator.init();

        ArrayList<String> files = new ArrayList();
        files.add("bounties.yml");
        files.add("spongelocations.yml");
        files.add("dropchest.yml");
        files.add("secrets.yml");
        files.add("donations.yml");
        files.add("recentkills.yml");
        files.add("referrals.yml");
        for (String file : files) {
            File f = new File(this.getDataFolder() + File.separator + file);
            if(!f.exists()) {
                if(file.contains(".") ) {
                    try {
                        f.createNewFile();
                        getLogger().info("File " + file + " successfully created");
                    } catch (IOException e) {
                        getLogger().info("File " + file + " failed to create");
                    }
                } else {
                    f.mkdir();
                    getLogger().info("Folder " + file + " successfully created");
                }
            }
        }

        getCommand("g").setExecutor(this.GuardChat);
        getCommand("b").setExecutor(this.BuildChat);
        getCommand("a").setExecutor(this.AdminChat);
        getCommand("d").setExecutor(this.DiscordChat);
        getCommand("s").setExecutor(this.StaffChat);

        getCommand("donoradd").setExecutor(this.DonorAdd);
        getCommand("donorbulk").setExecutor(this.DonorBulk);
        getCommand("purchases").setExecutor(this.Purchases);
        getCommand("donorcheck").setExecutor(this.DonorCheck);

        getCommand("spongeloc").setExecutor(this.SpongeLoc);
        getCommand("dropchest").setExecutor(this.DropChest);
        getCommand("tokenteleport").setExecutor(this.TokenTeleport);
        getCommand("rewards").setExecutor(this.RewardGUI);

        getCommand("bounty").setExecutor(this.Bounty);
        getCommand("killinfo").setExecutor(this.KillInfo);

        getCommand("test").setExecutor(this.playtimeRewards);

        getCommand("econcheck").setExecutor(this.EconomyCheck);
        getCommand("permshop").setExecutor(this.PermShop);

        getCommand("firstjointop").setExecutor(this.FirstjoinTop);

        getCommand("referral").setExecutor(this.Referral);
        getCommand("referrallist").setExecutor(this.ReferralList);

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    PacketContainer packet = event.getPacket();
                    if(packet.getChatComponents() != null) {
                        if (packet.getChatComponents().read(0) != null) {
                            if (packet.getChatComponents().read(0).toString() != null) {
                                if (packet.getChatComponents().read(0).toString() instanceof String) {
                                    String chatMsg = packet.getChatComponents().read(0).toString();
                                    if (chatMsg.contains("Next ranks:")) {
                                        event.setCancelled(true);
                                    }
                                }
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
        github.scarsz.discordsrv.DiscordSRV.api.unsubscribe(discordListener);
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
            TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(split[0] + "-chat");
            channel.sendMessage(dMessage).queue();
        }
    }

    //
    // Creates lists of people that have been /cb, and also creates the list containing all of the contraband
    //

    public Map<Player, Map.Entry<Player, Long>> hitcd = new HashMap();
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
            if (i != null && isGuardGear(i)) {
                i.setAmount(0);
            }
        }
    }

    private void InvGuardGearDelOther(Player player) {
        boolean deletedsomething = false;
        for (int n = 0; n < player.getOpenInventory().getTopInventory().getSize(); n++) {
            ItemStack i = player.getOpenInventory().getTopInventory().getItem(n);
            if (i != null && isGuardGear(i)) {
                i.setAmount(0);
                deletedsomething = true;
            }
        }

        if (deletedsomething) {
            player.closeInventory();
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
                Map.Entry lasthit = this.hitcd.get(damager);
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
        if (player.hasPermission("skyprisoncore.guard.onduty")) {
            ArrayList<String> guardWorlds = (ArrayList) config.getList("guard-worlds");
            Boolean inWorld = false;
            for(String guardWorld : guardWorlds) {
                if(player.getWorld().getName().equalsIgnoreCase(guardWorld)) {
                    inWorld = true;
                    break;
                }
            }
            if(inWorld == false) {
                event.setCancelled(true);
                getServer().getScheduler().scheduleSyncDelayedTask(this, () -> player.sendMessage(ChatColor.RED + "Please go off duty!"), 2L);

            }
        }
        if(player.hasPermission("skyprisoncore.builder.onduty")) {
            ArrayList<String> buildWorlds = (ArrayList) config.getList("builder-worlds");
            Boolean inWorld = false;
            for(String buildWorld : buildWorlds) {
                if(player.getWorld().getName().equalsIgnoreCase(buildWorld)) {
                    inWorld = true;
                    break;
                }
            }
            if(inWorld == false) {
                event.setCancelled(true);
                getServer().getScheduler().scheduleSyncDelayedTask(this, () -> player.sendMessage(ChatColor.RED + "Please go off duty!"), 2L);

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
        if (!player.hasPermission("skyprisoncore.contraband.itembypasss")
                && !player.getLocation().getWorld().getName().equalsIgnoreCase("world_prison")
                && !player.getLocation().getWorld().getName().equalsIgnoreCase("world_event")
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
                    File f = new File(this.getDataFolder() + File.separator + "dropChest.yml");
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
    public void spongeEvent(BlockDamageEvent event) throws IOException {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if (b.getType() == Material.SPONGE) {
            if (loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                File f = new File(this.getDataFolder() + File.separator + "spongelocations.yml");
                YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                Set setList = yamlf.getConfigurationSection("locations").getKeys(false);
                for (int i = 0; i < setList.size(); i++) {
                    if (yamlf.contains("locations." + i)) {
                        World w = Bukkit.getServer().getWorld(yamlf.getString("locations." + i + ".world"));
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
                                        sDataConf.set(pUUID, spongeFound);
                                    } else {
                                        sDataConf.set(pUUID, 0);
                                    }
                                    sDataConf.save(spongeData);

                                    TokenManagerPlugin.getInstance().addTokens(event.getPlayer(), 25);
                                    event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "Tokens" + ChatColor.DARK_GRAY + " » " + ChatColor.AQUA + "10 tokens "
                                            + ChatColor.GRAY + "has been added to your balance.");
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
                ArrayList axes = new ArrayList();
                axes.add(Material.DIAMOND_AXE);
                axes.add(Material.GOLDEN_AXE);
                axes.add(Material.IRON_AXE);
                axes.add(Material.STONE_AXE);
                axes.add(Material.WOODEN_AXE);
                axes.add(Material.NETHERITE_AXE);
                if(axes.contains(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                    if (!event.getPlayer().isSneaking()) {
                        Boolean birchDown = true;
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
                        Boolean birchUp = true;
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
            } else if (b.getType() == Material.WHEAT && loc.getWorld().getName().equalsIgnoreCase("prison")) {
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
            }
        }
    }

    //
    // Event Handlers regarding bounties
    //

    public void PvPSet(Player killed, Player killer) {
        File f = new File(this.getDataFolder() + File.separator + "recentkills.yml");
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
        if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
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
                File f = new File(this.getDataFolder() + File.separator +"bounties.yml");
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
                f = new File(this.getDataFolder() + File.separator + "recentkills.yml");
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


