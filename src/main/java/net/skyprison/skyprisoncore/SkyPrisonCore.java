package net.skyprison.skyprisoncore;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.session.SessionManager;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import litebans.api.Entry;
import litebans.api.Events;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.skyprison.skyprisoncore.commands.*;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.inventories.mail.MailBoxSend;
import net.skyprison.skyprisoncore.inventories.misc.DatabaseInventoryEdit;
import net.skyprison.skyprisoncore.inventories.misc.NewsMessageEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsCategoryEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsEdit;
import net.skyprison.skyprisoncore.inventories.smith.BlacksmithTrimmer;
import net.skyprison.skyprisoncore.inventories.smith.EndBlacksmithUpgrade;
import net.skyprison.skyprisoncore.inventories.smith.GrassBlacksmithUpgrade;
import net.skyprison.skyprisoncore.items.BlacksmithEnd;
import net.skyprison.skyprisoncore.items.Greg;
import net.skyprison.skyprisoncore.items.TreeFeller;
import net.skyprison.skyprisoncore.items.Vouchers;
import net.skyprison.skyprisoncore.listeners.advancedregionmarket.UnsellRegion;
import net.skyprison.skyprisoncore.listeners.brewery.BrewDrink;
import net.skyprison.skyprisoncore.listeners.cmi.CMIPlayerTeleportRequest;
import net.skyprison.skyprisoncore.listeners.cmi.CMIUserBalanceChange;
import net.skyprison.skyprisoncore.listeners.discord.MessageCreate;
import net.skyprison.skyprisoncore.listeners.discord.SlashCommandCreate;
import net.skyprison.skyprisoncore.listeners.discord.UserRoleAdd;
import net.skyprison.skyprisoncore.listeners.discord.UserRoleRemove;
import net.skyprison.skyprisoncore.listeners.excellentcrates.CrateObtainReward;
import net.skyprison.skyprisoncore.listeners.mcmmo.McMMOLevelUp;
import net.skyprison.skyprisoncore.listeners.mcmmo.McMMOPartyChat;
import net.skyprison.skyprisoncore.listeners.minecraft.*;
import net.skyprison.skyprisoncore.listeners.nuvotifier.Votifier;
import net.skyprison.skyprisoncore.listeners.parkour.ParkourFinish;
import net.skyprison.skyprisoncore.listeners.pvpmanager.PlayerTag;
import net.skyprison.skyprisoncore.listeners.pvpmanager.PlayerTogglePvP;
import net.skyprison.skyprisoncore.listeners.pvpmanager.PlayerUntag;
import net.skyprison.skyprisoncore.listeners.quickshop.ShopCreate;
import net.skyprison.skyprisoncore.listeners.quickshop.ShopPurchase;
import net.skyprison.skyprisoncore.listeners.quickshop.ShopSuccessPurchase;
import net.skyprison.skyprisoncore.listeners.shopguiplus.ShopPostTransaction;
import net.skyprison.skyprisoncore.listeners.shopguiplus.ShopPreTransaction;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import net.skyprison.skyprisoncore.utils.claims.ConsoleCommandFlagHandler;
import net.skyprison.skyprisoncore.utils.claims.EffectFlagHandler;
import net.skyprison.skyprisoncore.utils.claims.FlyFlagHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class SkyPrisonCore extends JavaPlugin {
    public HashMap<UUID, Boolean> flyPvP = new HashMap<>();
    public Map<Player, Map.Entry<Player, Long>> hitcd = new HashMap<>();
    public HashMap<UUID, Integer> teleportMove = new HashMap<>();
    public HashMap<UUID, ArrayList<String>> missions = new HashMap<>();
    public Map<String, Long> mineCools = new HashMap<>();
    public HashMap<UUID, List<Object>> chatLock = new HashMap<>();
    public Map<Integer, UUID> discordLinking = new HashMap<>();
    public Map<UUID, Integer> blockBreaks = new HashMap<>();
    public List<UUID> newsMessageChanges = new ArrayList<>();
    public List<UUID> customItemChanges = new ArrayList<>();
    public List<UUID> deleteMailbox = new ArrayList<>();
    public List<UUID> cancelMailSendConfirm = new ArrayList<>();
    public List<UUID> kickMemberMailbox = new ArrayList<>();
    public HashMap<UUID, MailBoxSend> writingMail = new HashMap<>();
    public HashMap<UUID, String> stickyChat = new HashMap<>();
    public List<UUID> inviteMailBox = new ArrayList<>();
    public HashMap<Material, Double> minPrice = new HashMap<>();
    public List<Location> shinyGrass = new ArrayList<>();
    private DiscordApi discApi;
    public DailyMissions dailyMissions;
    public ArrayList<Location> bombLocs = new ArrayList<>();
    private PlayerParticlesAPI particles;
    public List<Block> grassLocations = new ArrayList<>();
    public Timer shinyTimer = new Timer();
    public Timer spongeTimer = new Timer();
    public HashMap<UUID, HashMap<Integer, DatabaseInventoryEdit>> itemEditing = new HashMap<>();
    public HashMap<UUID, HashMap<Integer, NewsMessageEdit>> newsEditing = new HashMap<>();
    public HashMap<UUID, HashMap<Integer, SecretsEdit>> secretsEditing = new HashMap<>();
    public HashMap<UUID, HashMap<String, SecretsCategoryEdit>> secretsCatEditing = new HashMap<>();
    public List<UUID> secretChanges = new ArrayList<>();
    public List<UUID> secretCategoryChanges = new ArrayList<>();
    public HashMap<UUID, MailBoxSend> mailSend = new HashMap<>();
    public static DatabaseHook db;
    private final ScheduledExecutorService dailyExecutor = Executors.newSingleThreadScheduledExecutor();
    public static final HashMap<UUID, JailTimer> currentlyJailed = new HashMap<>();
    public static final HashMap<Audience, Audience> lastMessaged = new HashMap<>();
    private PaperCommandManager<CommandSender> manager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    public static final HashMap<UUID, Long> bribeCooldown = new HashMap<>();
    public static final HashMap<UUID, Long> bountyCooldown = new HashMap<>();
    public static final HashMap<UUID, Integer> safezoneViolators = new HashMap<>();
    public static final Component pluginPrefix = Component.text("Sky", TextColor.fromHexString("#0fc3ff")).append(Component.text("Prison", TextColor.fromHexString("#ff0000")));
    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flyFlag = new StateFlag("fly", false);
            StringFlag effectsFlag = new StringFlag("give-effects");
            StringFlag consoleFlag = new StringFlag("console-command");
            registry.register(flyFlag);
            registry.register(effectsFlag);
            registry.register(consoleFlag);
            ClaimUtils.FLY = flyFlag;
            ClaimUtils.EFFECTS = effectsFlag;
            ClaimUtils.CONSOLECMD = consoleFlag;
            getLogger().info("Loaded Custom Flags");
        } catch (FlagConflictException ignored) {
        }
    }

    public void onEnable() {
        new ConfigCreator(this).init();
        new LangCreator(this).init();

        try {
            db = new DatabaseHook(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String dToken = getConfig().getString("discord-token");

        if (Bukkit.getPluginManager().isPluginEnabled("PlayerParticles")) {
            particles = PlayerParticlesAPI.getInstance();
        }

        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();
        sessionManager.registerHandler(FlyFlagHandler.FACTORY, null);
        sessionManager.registerHandler(EffectFlagHandler.FACTORY, null);
        sessionManager.registerHandler(ConsoleCommandFlagHandler.FACTORY, null);

        if(dToken != null && !dToken.isEmpty()) {
            discApi = new DiscordApiBuilder()
                    .setToken(dToken)
                    .setAllNonPrivilegedIntentsAnd(Intent.MESSAGE_CONTENT, Intent.GUILD_MEMBERS)
                    .login()
                    .join();

            onConnectToDiscord();
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateDiscordRoles();
                }
            }.runTaskTimerAsynchronously(this, 20 * 1800, 20 * 1800);
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateTopic();
                }
            }.runTaskTimerAsynchronously(this, 20 * 1800, 20 * 1800);
        }

        dailyMissions = new DailyMissions(this, db);

        registerMinPrice();

        new ClaimUtils(this, db).initializeData();

        manager = PaperCommandManager.createNative(this, ExecutionCoordinator.simpleCoordinator());


        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            manager.registerBrigadier();
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        minecraftHelp = MinecraftHelp.createNative("/spc help", manager);

        MinecraftExceptionHandler.<CommandSender>createNative()
                .defaultHandlers()
                .decorator(component -> Component.text()
                                .append(pluginPrefix)
                                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).registerTo(manager);

        registerCommands();
        registerEvents();

        new Tags().loadTags(db);

        PlayerManager.loadIgnores();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this, dailyMissions, db).register();
            getLogger().info("Placeholders registered");
        }

        new Recipes(this);

        Timer dayTimer = new Timer();
        Calendar tommorow = Calendar.getInstance();
        tommorow.add(Calendar.DAY_OF_YEAR, 1);
        tommorow.set(Calendar.HOUR, 0);
        tommorow.set(Calendar.MINUTE, 1);
        tommorow.set(Calendar.SECOND, 0);
        tommorow.set(Calendar.MILLISECOND, 0);
        dayTimer.schedule(new NextDayTask(this, db), tommorow.getTime());

        Timer monthTimer = new Timer();
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
        nextMonth.set(Calendar.HOUR, 0);
        nextMonth.set(Calendar.MINUTE, 1);
        nextMonth.set(Calendar.SECOND, 0);
        nextMonth.set(Calendar.MILLISECOND, 0);
        monthTimer.schedule(new MonthlyTask(this, db), nextMonth.getTime());

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> NewsUtils.sendNewsMessage(player, 0));
            }
        }.runTaskTimer(this, 20*950, 20*950);

        new BukkitRunnable() {
            @Override
            public void run() {
                checkOnlineDailies();
                Map<UUID, Integer> tokensData = TokenUtils.getTokensData();
                if(tokensData != null && !tokensData.isEmpty()) {
                    for (UUID pUUID : tokensData.keySet()) {
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                            ps.setInt(1, tokensData.get(pUUID));
                            ps.setString(2, pUUID.toString());
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(missions != null && !missions.isEmpty()) {
                    Map<UUID, ArrayList<String>> tempMissions = missions;
                    for (UUID pUUID : tempMissions.keySet()) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(pUUID);
                        for(String mission : dailyMissions.getMissions(player)) {
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE daily_missions SET amount = ?, completed = ? WHERE user_id = ? AND type = ?")) {
                                ps.setInt(1, dailyMissions.getMissionAmount(player, mission));
                                ps.setInt(2, dailyMissions.isCompleted(player, mission) ? 1 : 0);
                                ps.setString(3, pUUID.toString());
                                ps.setString(4, mission);
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 20 * 635, 20 * 635);
    }
    private final Map<String, TagResolver> DEFAULT_TAGS = Map.ofEntries(
            Map.entry("color", StandardTags.color()),
            Map.entry("reset", StandardTags.reset()),
            Map.entry("click", StandardTags.clickEvent()),
            Map.entry("hover", StandardTags.hoverEvent()),
            Map.entry("keybind", StandardTags.keybind()),
            Map.entry("translatable", StandardTags.translatable()),
            Map.entry("insertion", StandardTags.insertion()),
            Map.entry("rainbow", StandardTags.rainbow()),
            Map.entry("gradient", StandardTags.gradient()),
            Map.entry("transition", StandardTags.transition()),
            Map.entry("font", StandardTags.font()),
            Map.entry("newline", StandardTags.newline()),
            Map.entry("italic", StandardTags.decorations(TextDecoration.ITALIC)),
            Map.entry("bold", StandardTags.decorations(TextDecoration.BOLD)),
            Map.entry("strikethrough", StandardTags.decorations(TextDecoration.STRIKETHROUGH)),
            Map.entry("obfuscated", StandardTags.decorations(TextDecoration.OBFUSCATED)),
            Map.entry("underlined", StandardTags.decorations(TextDecoration.UNDERLINED))
    );

    public boolean hasVoucher(Player player, String voucherType, int amount) {
        PlayerInventory inv = player.getInventory();
        ItemStack voucher = Vouchers.getVoucherFromType(this, voucherType, amount);
        return inv.containsAtLeast(voucher, amount);
    }
    public TextColor getChatColour(Player player) {
        TextColor chatColour = NamedTextColor.GRAY;
        if(player != null) {
            LuckPerms luckAPI = LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = luckAPI.getPlayerAdapter(Player.class).getUser(player);
            String chatColourString = Objects.requireNonNullElse(user.getCachedData().getMetaData().getMetaValue("chat-colour"), "");
            chatColour = MiniMessage.miniMessage().deserialize(chatColourString).color();
        }
        return chatColour;
    }
    public Component getParsedString(CommandSender sender, String formatType, String message) {
        TagResolver.Builder resolver = TagResolver.builder();
        if (!sender.hasPermission("skyprisoncore.format." + formatType)) return Component.text(message);

        for (Map.Entry<String, TagResolver> entry : DEFAULT_TAGS.entrySet()) {
            if (sender.hasPermission("skyprisoncore.format." + formatType + "." + entry.getKey())) {
                resolver.resolver(entry.getValue());
            }
        }
        if(sender instanceof Player player) {
            if(player.hasPermission("skyprisoncore.format." + formatType + ".papi")) {
                resolver.resolver(papiTag(player));
            }
        }
        final MiniMessage miniMessage = MiniMessage.builder().tags(resolver.build()).build();
        return miniMessage.deserialize(message);
    }
    @Override
    public void onDisable() {
        if(!shinyGrass.isEmpty()) {
            particles.removeFixedEffectsInRange(shinyGrass.getFirst(), 1000);
        }
        if(discApi != null && discApi.getServerTextChannelById("788108242797854751").isPresent()) {
            try {
                discApi.getServerTextChannelById("788108242797854751").get().sendMessage(":octagonal_sign: **Server has stopped**");
                discApi.getServerTextChannelById("788108242797854751").get().updateTopic("Server is offline!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            discApi.disconnect();
        }

        shinyTimer.cancel();
        spongeTimer.cancel();
        dailyExecutor.shutdown();
        getLogger().info("Disabled SkyPrisonCore v" + getPluginMeta().getVersion());
    }

    private void onConnectToDiscord() {
        if(discApi != null && discApi.getTextChannelById("788108242797854751").isPresent()) {
            getLogger().info("Connected to Discord as " + discApi.getYourself().getDiscriminatedName());
            getLogger().info("Open the following url to invite the bot: " + discApi.createBotInvite());
            discApi.getTextChannelById("788108242797854751").get().sendMessage(":white_check_mark: **Server has started**");

            try {
                for(SlashCommand command : discApi.getGlobalSlashCommands().get()) {
                    if(!command.getName().equalsIgnoreCase("link")) {
                        command.delete();
                    }
                }
                if(discApi.getServerById("782795465632251955").isPresent()) {
                    Server serv = discApi.getServerById("782795465632251955").get();
                    for (SlashCommand command : discApi.getServerSlashCommands(serv).get()) {
                        if(!command.getName().equalsIgnoreCase("link")) {
                            command.delete();
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException ignored) {
            }

            SlashCommand.with("link", "Link your Discord and Minecraft Account", List.of(
                            SlashCommandOption.createWithChoices(SlashCommandOptionType.STRING, "link-code", "Code for linking", true)
                    )).createGlobal(discApi).join();
            discApi.addListener(new SlashCommandCreate(this, db));
            discApi.addListener(new MessageCreate(this, new ChatUtils(this, discApi), discApi));
            discApi.addListener(new UserRoleAdd(this, db));
            discApi.addListener(new UserRoleRemove(this, db));
        }
    }

    private void updateTopic() {
        if(discApi != null && discApi.getServerTextChannelById("788108242797854751").isPresent()) {
            discApi.getServerTextChannelById("788108242797854751").get().updateTopic("Online Players: " + Bukkit.getOnlinePlayers().size() + "/50");
        }
    }

    public void updateDiscordRoles() {
        if(discApi != null && discApi.getServerById("782795465632251955").isPresent()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                long discordId = 0;
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        discordId = rs.getLong(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (discordId != 0) {
                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                    String roleName = user.getRank().getName();
                    Role role = discApi.getRolesByName(roleName).iterator().next();
                    try {
                        User discUser = discApi.getUserById(discordId).get();
                        Server server = discApi.getServerById("782795465632251955").get();
                        if (!discUser.getRoles(server).contains(role)) {
                            discUser.addRole(role);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
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

    private CustomInventory openBlacksmith(String blacksmith, Player player) {
        switch (blacksmith.toLowerCase()) {
            case "astrid" -> {
                return new GrassBlacksmithUpgrade(this, player);
            }
            case "end" -> {
                return new EndBlacksmithUpgrade(this, player);
            }
            case "trim" -> {
                return new BlacksmithTrimmer(this, player);
            }
            default -> {
                return null;
            }
        }
    }

    public void registerCommands() {
        Command.Builder<CommandSender> treefeller = manager.commandBuilder("treefeller")
                .permission("skyprisoncore.command.treefeller");
        List<String> treefellerOptions = List.of("axe", "speed", "cooldown", "durability", "repair");
        manager.command(treefeller.literal("give")
                .permission("skyprisoncore.command.treefeller.give")
                .required("player", playerParser())
                .required("type", stringParser(), SuggestionProvider.suggestingStrings(treefellerOptions))
                .required("amount", integerParser(1))
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(treefellerOptions.contains(type.toLowerCase())) {
                        ItemStack treeItem;
                        if(type.equalsIgnoreCase("axe")) {
                            treeItem = TreeFeller.getAxe(this, amount);
                        } else if(type.equalsIgnoreCase("repair")) {
                            treeItem = TreeFeller.getRepairItem(this, amount);
                        } else {
                            treeItem = TreeFeller.getUpgradeItem(this, type, amount);
                        }
                        PlayerManager.giveItems(player, treeItem);
                        c.sender().sendMessage(Component.text("Successfully sent!"));
                    }
                }));

        List<String> blacksmithOptions = List.of("astrid", "end", "trim");
        manager.command(manager.commandBuilder("blacksmith")
                .permission("skyprisoncore.command.blacksmith")
                .required("blacksmith", stringParser(), SuggestionProvider.suggestingStrings(blacksmithOptions))
                .optional("player", playerParser())
                .handler(c -> Bukkit.getScheduler().runTask(this, () -> {
                    Player player = c.getOrDefault("player", null);
                    if(player == null && c.sender() instanceof Player) {
                        player = (Player) c.sender();
                    }

                    if(player != null) {
                        final String blacksmith = c.get("blacksmith");
                        CustomInventory inv = openBlacksmith(blacksmith, player);
                        if (inv != null) {
                            player.openInventory(inv.getInventory());
                        } else {
                            c.sender().sendMessage(Component.text("Invalid Usage! /blacksmith <blacksmith> (player)", NamedTextColor.RED));
                        }
                    } else {
                        c.sender().sendMessage(Component.text("Invalid Usage! /blacksmith <blacksmith> <player>", NamedTextColor.RED));
                    }
                })));

        Command.Builder<CommandSender> voucher = manager.commandBuilder("voucher")
                .permission("skyprisoncore.command.voucher");
        manager.command(voucher.literal("give")
                .permission("skyprisoncore.command.voucher.give")
                .required("player", playerParser())
                .required("voucher", stringParser(), SuggestionProvider.suggestingStrings(List.of("token-shop", "mine-reset", "single-use-enderchest")))
                .required("amount", integerParser())
                .handler(c -> {
                    final Player player = c.get("player");
                    final String voucherType = c.get("voucher");
                    final int amount = c.get("amount");
                    ItemStack voucherItem = Vouchers.getVoucherFromType(this, voucherType, amount);
                    if(voucherItem != null) {
                        PlayerManager.giveItems(player, voucherItem);
                        c.sender().sendMessage(Component.text("Successfully sent!"));
                    }
                }));

        Command.Builder<CommandSender> greg = manager.commandBuilder("greg")
                .permission("skyprisoncore.command.greg");
        List<String> gregOptions = List.of("grease", "allay-dust", "strength", "speed", "fire-resistance", "instant-health", "instant-damage",
                "release-papers", "fake-release-papers");
        manager.command(greg.literal("give")
                .permission("skyprisoncore.command.greg.give")
                .required("player", playerParser())
                .required("type", stringParser(), SuggestionProvider.suggestingStrings(gregOptions))
                .required("amount", integerParser())
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(gregOptions.contains(type.toLowerCase())) {
                        ItemStack item = Greg.getItemFromType(this, type, amount);
                        if (item != null) {
                            PlayerManager.giveItems(player, item);
                            c.sender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));

        Command.Builder<CommandSender> endSmith = manager.commandBuilder("endsmith")
                .permission("skyprisoncore.command.endsmith");
        List<String> endSmithOptions = List.of("reset-repair", "keep-enchants", "keep-trims");
        manager.command(endSmith.literal("addon")
                .permission("skyprisoncore.command.endsmith.give")
                .required("player", playerParser())
                .required("type", stringParser(), SuggestionProvider.suggestingStrings(endSmithOptions))
                .required("amount", integerParser())
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(endSmithOptions.contains(type.toLowerCase())) {
                        ItemStack item = BlacksmithEnd.getItemFromType(this, type, "", amount);
                        if (item != null) {
                            PlayerManager.giveItems(player, item);
                            c.sender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));
        List<String> templateOptions = List.of("helmet", "chestplate", "leggings", "boots", "axe", "pickaxe", "shovel", "hoe");
        manager.command(endSmith.literal("template")
                .permission("skyprisoncore.command.endsmith.give")
                .required("player", playerParser())
                .required("type", stringParser(), SuggestionProvider.suggestingStrings(templateOptions))
                .required("amount", integerParser())
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(templateOptions.contains(type.toLowerCase())) {
                        ItemStack item = BlacksmithEnd.getItemFromType(this, "upgrade-template", type, amount);
                        if (item != null) {
                            PlayerManager.giveItems(player, item);
                            c.sender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));

        Command.Builder<CommandSender> bomb = manager.commandBuilder("bomb")
                .permission("skyprisoncore.command.bomb");
        List<String> bombOptions = List.of("small", "medium", "large", "massive", "nuke");
        manager.command(bomb.literal("give")
                .permission("skyprisoncore.command.bomb.give")
                .required("player", playerParser())
                .required("type", stringParser(), SuggestionProvider.suggestingStrings(bombOptions))
                .required("amount", integerParser())
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(bombOptions.contains(type.toLowerCase())) {
                        ItemStack item = BombUtils.getBomb(this, type, amount);
                        PlayerManager.giveItems(player, item);
                        c.sender().sendMessage(Component.text("Successfully sent!"));
                    }
                }));

        manager.command(manager.commandBuilder("enchtable", "ench", "enchanttable")
                .senderType(Player.class)
                .permission("skyprisoncore.command.enchtable")
                .handler(c -> {
                    Player player = c.sender();
                    Location loc;
                    String worldName = player.getWorld().getName();

                    switch (worldName.toLowerCase()) {
                        case "world_prison" -> loc = new Location(Bukkit.getWorld("world_prison"), -121, 150, -175);
                        case "world_free" -> loc = new Location(Bukkit.getWorld("world_free"), -2941, 148, -758);
                        case "world_free_nether" -> loc = new Location(Bukkit.getWorld("world_free_nether"), -17, 116, -52);
                        case "world_free_end" -> loc = new Location(Bukkit.getWorld("world_free_end"), -203, 77, 4);
                        default -> {
                            player.sendMessage(Component.text("You can't use /enchtable in this world!", NamedTextColor.RED));
                            return;
                        }
                    }
                    getServer().getScheduler().runTask(this, () -> player.openEnchanting(loc, true));
                })
                .build()
        );

        new AdminCommands(this, manager);
        new ChatCommands(this, manager, discApi, db);
        new JailCommands(this, manager);
        new SecretsCommands(this, manager);
        new DiscordCommands(this, db, discApi, manager);
        new VoteCommands(this, db, manager);
        new MailCommands(this, db, manager);
        new StoreCommands(db, manager);
        new ReferralCommands(this, db, manager);
        new CustomInvCommands(this, db, manager);
        new BottledExpCommands(this, manager);
        new EconomyCommands(this, db, manager);
        new ClaimCommands(this, db, manager);
        new MiscCommands(this, db, manager);

        Permission bountyBypass = new Permission("skyprisoncore.command.bounty.bypass", PermissionDefault.FALSE);
        Bukkit.getPluginManager().addPermission(bountyBypass);
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockBreak(this, dailyMissions, particles), this);
        pm.registerEvents(new BlockDamage(this, db, dailyMissions), this);
        pm.registerEvents(new BlockPlace(this, dailyMissions, db), this);
        pm.registerEvents(new BrewDrink(db), this);
        pm.registerEvents(new CMIPlayerTeleportRequest(), this);
        pm.registerEvents(new CMIUserBalanceChange(this, db), this);
        pm.registerEvents(new EntityDamageByEntity(this), this);
        pm.registerEvents(new EntityDeath(this, db, dailyMissions), this);
        pm.registerEvents(new EntityPickupItem(this), this);
        pm.registerEvents(new InventoryClick(this, db, particles), this);
        pm.registerEvents(new InventoryOpen(this), this);
        pm.registerEvents(new LeavesDecay(), this);
        pm.registerEvents(new McMMOLevelUp(this), this);
        pm.registerEvents(new PlayerChangedWorld(), this);
        pm.registerEvents(new PlayerInteract(this, db, dailyMissions), this);
        pm.registerEvents(new PlayerMove(this), this);
        pm.registerEvents(new PlayerPostRespawn(), this);
        pm.registerEvents(new PlayerTag(this), this);
        pm.registerEvents(new PlayerTeleport(this), this);
        pm.registerEvents(new PlayerUntag(), this);
        pm.registerEvents(new ShopCreate(this), this);
        pm.registerEvents(new ShopPostTransaction(db, dailyMissions), this);
        pm.registerEvents(new ShopPreTransaction(db), this);
        pm.registerEvents(new ShopPurchase(db), this);
        pm.registerEvents(new ShopSuccessPurchase(db), this);
        pm.registerEvents(new UnsellRegion(), this);
        pm.registerEvents(new PlayerFish(dailyMissions), this);
        pm.registerEvents(new InventoryClose(), this);
        pm.registerEvents(new EntityDamage(this), this);
        pm.registerEvents(new PlayerCommandPreprocess(), this);
        pm.registerEvents(new ParkourFinish(this, dailyMissions), this);
        pm.registerEvents(new PlayerTogglePvP(), this);
        pm.registerEvents(new ServerLoad(this, particles, db), this);
        pm.registerEvents(new CrateObtainReward(db), this);
        pm.registerEvents(new EntityToggleGlide(), this);
        pm.registerEvents(new PlayerBucketEmpty(), this);
        pm.registerEvents(new AsyncChatDecorate(this), this);
        pm.registerEvents(new SignChange(this), this);
        pm.registerEvents(new PlayerEditBook(this), this);
        pm.registerEvents(new PrepareAnvil(), this);
        pm.registerEvents(new EnchantItem(this), this);
        pm.registerEvents(new PrepareItemEnchant(this), this);
        pm.registerEvents(new PlayerItemConsume(this), this);
        pm.registerEvents(new Votifier(this, db), this);
        pm.registerEvents(new PlayerSwapHandItems(this), this);
        pm.registerEvents(new CraftItem(), this);

        pm.registerEvents(new AsyncChat(this, discApi, db), this);
        pm.registerEvents(new PlayerQuit(this, db, discApi, dailyMissions), this);
        pm.registerEvents(new PlayerJoin(this, db, discApi, dailyMissions, particles), this);

        pm.registerEvents(new McMMOPartyChat(discApi), this);

        if(discApi != null) {
            Events.get().register(new Events.Listener() {
                @Override
                public void entryAdded(Entry entry) {
                    if (discApi.getTextChannelById("823392480241516584").isPresent() && entry.getType().equals("ban") && entry.getExecutorUUID() != null) {
                        CMIUser bannedUser = CMI.getInstance().getPlayerManager().getUser(entry.getUuid());
                        if(!entry.getExecutorUUID().equalsIgnoreCase("console")) {
                            CMIUser user = CMI.getInstance().getPlayerManager().getUser(entry.getExecutorUUID());
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle(bannedUser.getName() + " has been banned!");
                            embed.setColor(java.awt.Color.RED);
                            embed.setDescription("Banned by: " + user.getName() + "\nReason: " + entry.getReason() + "\nDuration: " + entry.getDurationString());
                            discApi.getTextChannelById("823392480241516584").get().sendMessage(embed);
                        } else {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle(bannedUser.getName() + " has been banned!");
                            embed.setColor(java.awt.Color.RED);
                            embed.setDescription("Banned by: CONSOLE \nReason: " + entry.getReason() + "\nDuration: " + entry.getDurationString());
                            discApi.getTextChannelById("823392480241516584").get().sendMessage(embed);
                        }
                    }
                }
                @Override
                public void entryRemoved(Entry entry) {
                    if (discApi.getTextChannelById("823392480241516584").isPresent() && entry.getType().equals("ban") && entry.getExecutorUUID() != null) {
                        CMIUser bannedUser = CMI.getInstance().getPlayerManager().getUser(entry.getUuid());
                        if(!entry.getExecutorUUID().equalsIgnoreCase("console")) {
                            CMIUser user = CMI.getInstance().getPlayerManager().getUser(entry.getExecutorUUID());
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle(bannedUser.getName() + " has been unbanned!");
                            embed.setColor(java.awt.Color.GREEN);
                            embed.setDescription("Unbanned by: " + user.getName() + "\nReason: " + entry.getRemovalReason());
                            discApi.getTextChannelById("823392480241516584").get().sendMessage(embed);
                        } else {
                            EmbedBuilder embed = new EmbedBuilder();
                            embed.setTitle(bannedUser.getName() + " has been unbanned!");
                            embed.setColor(java.awt.Color.GREEN);
                            embed.setDescription("Unbanned by: CONSOLE \nReason: " + entry.getRemovalReason());
                            discApi.getTextChannelById("823392480241516584").get().sendMessage(embed);
                        }
                    }
                }
            });
        }
    }

    public static @NotNull TagResolver papiTag(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            final String papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value();
            final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + papiPlaceholder + '%');
            final Component componentPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);
            return Tag.inserting(componentPlaceholder);
        });
    }
    public boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public void checkOnlineDailies() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String currDate = formatter.format(date);

        for(Player player : Bukkit.getOnlinePlayers()) {
            String lastDay = "";
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT last_collected FROM dailies WHERE user_id = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    lastDay = rs.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(lastDay != null && !lastDay.equalsIgnoreCase(currDate)) {
                player.sendMessage(Component.text("You can collect your ", NamedTextColor.GREEN).append(Component.text("/daily!", NamedTextColor.GREEN, TextDecoration.BOLD)));
            }
        }
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    public static String getQuestionMarks(List<?> list) {
        if(!list.isEmpty()) {
            return "(" + list.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";
        } else {
            return "";
        }
    }
    public final MiniMessage playerMsgBuilder = MiniMessage.builder()
            .tags(TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .build()
            )
            .build();
}
