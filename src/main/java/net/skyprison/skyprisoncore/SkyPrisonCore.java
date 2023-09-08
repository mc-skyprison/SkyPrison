package net.skyprison.skyprisoncore;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.ClickEvent.Action;
import net.kyori.adventure.text.event.HoverEvent;
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
import net.skyprison.skyprisoncore.commands.discord.Discord;
import net.skyprison.skyprisoncore.commands.donations.DonorAdd;
import net.skyprison.skyprisoncore.commands.donations.Purchases;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.inventories.*;
import net.skyprison.skyprisoncore.inventories.mail.MailBox;
import net.skyprison.skyprisoncore.inventories.mail.MailBoxSend;
import net.skyprison.skyprisoncore.inventories.mail.MailHistory;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsCategoryEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsEdit;
import net.skyprison.skyprisoncore.inventories.smith.BlacksmithTrimmer;
import net.skyprison.skyprisoncore.inventories.smith.EndBlacksmithUpgrade;
import net.skyprison.skyprisoncore.inventories.smith.GrassBlacksmithUpgrade;
import net.skyprison.skyprisoncore.items.*;
import net.skyprison.skyprisoncore.listeners.advancedregionmarket.UnsellRegion;
import net.skyprison.skyprisoncore.listeners.brewery.BrewDrink;
import net.skyprison.skyprisoncore.listeners.cmi.CMIPlayerTeleportRequest;
import net.skyprison.skyprisoncore.listeners.cmi.CMIUserBalanceChange;
import net.skyprison.skyprisoncore.listeners.cmi.PlayerUnJail;
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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
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

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SkyPrisonCore extends JavaPlugin {
    public HashMap<UUID, Boolean> flyPvP = new HashMap<>();
    public HashMap<UUID, Integer> teleportMove = new HashMap<>();
    public Map<UUID, Integer> tokensData = new HashMap<>();
    public HashMap<UUID, String> userTags = new HashMap<>();
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
    public List<UUID> inviteMailBox = new ArrayList<>();
    public List<UUID> deleteClaim = new ArrayList<>();
    public List<UUID> transferClaim = new ArrayList<>();
    public HashMap<UUID, String> stickyChat = new HashMap<>();
    public HashMap<UUID, Boolean> customClaimHeight = new HashMap<>();
    public HashMap<UUID, Boolean>  customClaimShape = new HashMap<>();
    public HashMap<Material, Double> minPrice = new HashMap<>();
    public List<Location> shinyGrass = new ArrayList<>();
    public Tokens tokens;
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
    public static StateFlag FLY;
    public static StringFlag EFFECTS;
    public static StringFlag CONSOLECMD;
    public HashMap<UUID, LinkedHashMap<String, Integer>> shopLogAmountPlayer = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<String, Double>> shopLogPricePlayer = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<String, Integer>> shopLogPagePlayer = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<String, Integer>> tokenLogUsagePlayer = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<String, Integer>> tokenLogAmountPlayer = new HashMap<>();
    public HashMap<UUID, LinkedHashMap<String, Integer>> tokenLogPagePlayer = new HashMap<>();
    private final ScheduledExecutorService dailyExecutor = Executors.newSingleThreadScheduledExecutor();
    public static final HashMap<UUID, JailTimer> currentlyJailed = new HashMap<>();
    public static final HashMap<Audience, Audience> lastMessaged = new HashMap<>();
    private PaperCommandManager<CommandSender> manager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private CommandConfirmationManager<CommandSender> confirmationManager;
    public static final HashMap<UUID, Long> bribeCooldown = new HashMap<>();
    public static final HashMap<UUID, Long> releasePapersCooldown = new HashMap<>();
    public static final HashMap<UUID, Integer> safezoneViolators = new HashMap<>();
    public static final List<UUID> creatingSecret = new ArrayList<>();
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
            FLY = flyFlag;
            EFFECTS = effectsFlag;
            CONSOLECMD = consoleFlag;
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
            this.particles = PlayerParticlesAPI.getInstance();
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

        tokens = new Tokens(this, getDatabase());

        dailyMissions = new DailyMissions(this, getDatabase());

        registerMinPrice();

        final Function<CommandTree<CommandSender>, CommandExecutionCoordinator<CommandSender>> executionCoordinatorFunction =
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build();

        final Function<CommandSender, CommandSender> mapperFunction = Function.identity();
        try {
            this.manager = new PaperCommandManager<>(this, executionCoordinatorFunction, mapperFunction, mapperFunction);
        } catch (final Exception e) {
            this.getLogger().severe("Failed to initialize the command manager!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
        ));

        this.minecraftHelp = new MinecraftHelp<>( "/spc help", AudienceProvider.nativeAudience(), this.manager);

        if (this.manager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
            this.manager.registerBrigadier();
        }

        if (this.manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            ((PaperCommandManager<CommandSender>) this.manager).registerAsynchronousCompletions();
        }

        this.confirmationManager = new CommandConfirmationManager<>(30L, TimeUnit.SECONDS,
                context -> context.getCommandContext().getSender().sendMessage(Component.text("Confirmation required. Confirm using /example confirm.", NamedTextColor.RED)),
                sender -> sender.sendMessage(Component.text("You don't have any pending commands.", NamedTextColor.RED))
        );

        this.confirmationManager.registerConfirmationProcessor(this.manager);

        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(component -> Component.text()
                        .append(pluginPrefix)
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(component).build()
                ).apply(this.manager, AudienceProvider.nativeAudience());

        this.registerCommands();


        registerEvents();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this, dailyMissions, getDatabase()).register();
            getLogger().info("Placeholders registered");
        }

        Timer dayTimer = new Timer();
        Calendar tommorow = Calendar.getInstance();
        tommorow.add(Calendar.DAY_OF_YEAR, 1);
        tommorow.set(Calendar.HOUR, 0);
        tommorow.set(Calendar.MINUTE, 1);
        tommorow.set(Calendar.SECOND, 0);
        tommorow.set(Calendar.MILLISECOND, 0);
        dayTimer.schedule(new NextDayTask(this, getDatabase()), tommorow.getTime());

        Timer monthTimer = new Timer();
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
        nextMonth.set(Calendar.HOUR, 0);
        nextMonth.set(Calendar.MINUTE, 1);
        nextMonth.set(Calendar.SECOND, 0);
        nextMonth.set(Calendar.MILLISECOND, 0);


        monthTimer.schedule(new MonthlyTask(this, getDatabase()), nextMonth.getTime());

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> sendNewsMessage(player, 0));
            }
        }.runTaskTimer(this, 20*950, 20*950);

        new BukkitRunnable() {
            @Override
            public void run() {
                checkOnlineDailies();
                if(tokensData != null && !tokensData.isEmpty()) {
                    Map<UUID, Integer> token = tokensData;
                    for (UUID pUUID : token.keySet()) {
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET tokens = ? WHERE user_id = ?")) {
                            ps.setInt(1, token.get(pUUID));
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

    public Component getParsedName(String name, boolean allTags) {
        TagResolver.Builder resolver = TagResolver.builder();
        if(allTags) {
            resolver.resolvers(StandardTags.defaults());
        } else {
            resolver.resolvers(StandardTags.color(), StandardTags.gradient(), StandardTags.rainbow());
        }
        final MiniMessage miniMessage = MiniMessage.builder().tags(resolver.build()).build();
        return miniMessage.deserialize(name);
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
            particles.removeFixedEffectsInRange(shinyGrass.get(0), 1000);
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
            discApi.addListener(new SlashCommandCreate(this, getDatabase()));
            discApi.addListener(new MessageCreate(this, new ChatUtils(this, discApi), discApi));
            discApi.addListener(new UserRoleAdd(this, getDatabase()));
            discApi.addListener(new UserRoleRemove(this, getDatabase()));
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
                try(Connection conn = getDatabase().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
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

    public void giveItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> didntFit = player.getInventory().addItem(item);
        for(ItemStack dropItem : didntFit.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), dropItem).setOwner(player.getUniqueId());
        }
    }

    public void registerCommands() {
        manager.command(
                manager.commandBuilder("spc")
                        .literal("help")
                        .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                        .handler(context -> minecraftHelp.queryCommands(context.getOrDefault("query", ""), context.getSender()))
        );

        Command.Builder<CommandSender> treefeller = this.manager.commandBuilder("treefeller")
                .permission("skyprisoncore.command.treefeller");
        List<String> treefellerOptions = List.of("axe", "speed", "cooldown", "durability", "repair");
        this.manager.command(treefeller.literal("give")
                .permission("skyprisoncore.command.treefeller.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> treefellerOptions))
                .argument(IntegerArgument.of("amount"))
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
                        giveItem(player, treeItem);
                        c.getSender().sendMessage(Component.text("Successfully sent!"));
                    }
                }));

        List<String> blacksmithOptions = List.of("astrid", "end", "trim");
        this.manager.command(this.manager.commandBuilder("blacksmith")
                .permission("skyprisoncore.command.blacksmith")
                .argument(StringArgument.<CommandSender>builder("blacksmith")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> blacksmithOptions))
                .argument(PlayerArgument.optional("player"))
                .handler(c -> Bukkit.getScheduler().runTask(this, () -> {
                    Player player = c.getOptional("player").isPresent() ? (Player) c.getOptional("player").get() : c.getSender() instanceof Player ? (Player) c.getSender() : null;
                    if(player != null) {
                        final String blacksmith = c.get("blacksmith");
                        CustomInventory inv = openBlacksmith(blacksmith, player);
                        if (inv != null) {
                            player.openInventory(inv.getInventory());
                        } else {
                            c.getSender().sendMessage(Component.text("Invalid Usage! /blacksmith <blacksmith> (player)", NamedTextColor.RED));
                        }
                    } else {
                        c.getSender().sendMessage(Component.text("Invalid Usage! /blacksmith <blacksmith> <player>", NamedTextColor.RED));
                    }
                })));

        Command.Builder<CommandSender> voucher = this.manager.commandBuilder("voucher")
                .permission("skyprisoncore.command.voucher");
        this.manager.command(voucher.literal("give")
                        .permission("skyprisoncore.command.voucher.give")
                        .argument(PlayerArgument.of("player"))
                        .argument(StringArgument.<CommandSender>builder("voucher")
                                .withSuggestionsProvider((commandSenderCommandContext, s) -> List.of("token-shop", "mine-reset", "single-use-enderchest")))
                        .argument(IntegerArgument.of("amount"))
                        .handler(c -> {
                            final Player player = c.get("player");
                            final String voucherType = c.get("voucher");
                            final int amount = c.get("amount");
                            ItemStack voucherItem = Vouchers.getVoucherFromType(this, voucherType, amount);
                            if(voucherItem != null) {
                                giveItem(player, voucherItem);
                                c.getSender().sendMessage(Component.text("Successfully sent!"));
                            }
                        }));

        Command.Builder<CommandSender> greg = this.manager.commandBuilder("greg")
                .permission("skyprisoncore.command.greg");
        List<String> gregOptions = List.of("grease", "allay-dust", "strength", "speed", "fire-resistance", "instant-health", "instant-damage",
                "release-papers", "fake-release-papers");
        this.manager.command(greg.literal("give")
                .permission("skyprisoncore.command.greg.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> gregOptions))
                .argument(IntegerArgument.of("amount"))
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(gregOptions.contains(type.toLowerCase())) {
                        ItemStack item = Greg.getItemFromType(this, type, amount);
                        if (item != null) {
                            giveItem(player, item);
                            c.getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));
        Command.Builder<CommandSender> postOffice = this.manager.commandBuilder("postoffice")
                .permission("skyprisoncore.command.postoffice");
        List<String> postOfficeOptions = List.of("mailbox");
        this.manager.command(postOffice.literal("give")
                .permission("skyprisoncore.command.postoffice.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> postOfficeOptions))
                .argument(IntegerArgument.of("amount"))
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(postOfficeOptions.contains(type.toLowerCase())) {
                        ItemStack item = PostOffice.getItemFromType(this, type, amount);
                        if (item != null) {
                            giveItem(player, item);
                            c.getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));


        Command.Builder<CommandSender> endSmith = this.manager.commandBuilder("endsmith")
                .permission("skyprisoncore.command.endsmith");
        List<String> endSmithOptions = List.of("reset-repair", "keep-enchants", "keep-trims");
        this.manager.command(endSmith.literal("addon")
                .permission("skyprisoncore.command.endsmith.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> endSmithOptions))
                .argument(IntegerArgument.of("amount"))
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(endSmithOptions.contains(type.toLowerCase())) {
                        ItemStack item = BlacksmithEnd.getItemFromType(this, type, "", amount);
                        if (item != null) {
                            giveItem(player, item);
                            c.getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));
        List<String> templateOptions = List.of("helmet", "chestplate", "leggings", "boots", "axe", "pickaxe", "shovel", "hoe");
        this.manager.command(endSmith.literal("template")
                .permission("skyprisoncore.command.endsmith.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> templateOptions))
                .argument(IntegerArgument.of("amount"))
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(templateOptions.contains(type.toLowerCase())) {
                        ItemStack item = BlacksmithEnd.getItemFromType(this, "upgrade-template", type, amount);
                        if (item != null) {
                            giveItem(player, item);
                            c.getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));

        this.manager.command(this.manager.commandBuilder("bartender")
                .permission("skyprisoncore.command.bartender")
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    Player player = c.getOptional("player").isPresent() ? (Player) c.getOptional("player").get() : c.getSender() instanceof Player ? (Player) c.getSender() : null;
                    if(player != null) {
                        Bukkit.getScheduler().runTask(this, () -> player.openInventory(new DatabaseInventory(this, db, player,
                                player.hasPermission("skyprisoncore.inventories.bartender.editing"), "bartender").getInventory()));
                    } else {
                        c.getSender().sendMessage(Component.text("Invalid Usage! /bartender (player)"));
                    }
                }));

        Command.Builder<CommandSender> customInv = this.manager.commandBuilder("custominv")
                .permission("skyprisoncore.command.custominv");
        this.manager.command(customInv.literal("list")
                .permission("skyprisoncore.command.custominv.list")
                .argument(IntegerArgument.<CommandSender>builder("page").asOptionalWithDefault(1).withMin(1).withMax(20))
                .handler(c -> {
                    int page = c.get("page");
                    Component list = new CustomInv(db).getFormattedList(page);
                    c.getSender().sendMessage(list);
                }));
        this.manager.command(customInv.literal("open")
                .permission("skyprisoncore.command.custominv.open")
                .argument(StringArgument.<CommandSender>builder("name")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> new CustomInv(db).getList()))
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    Player player = c.getOptional("player").isPresent() ? (Player) c.getOptional("player").get() : c.getSender() instanceof Player ? (Player) c.getSender() : null;
                    if(player != null) {
                        CustomInv inv = new CustomInv(db);
                        String invName = c.get("name");
                        if(inv.categoryExists(invName)) {
                            if (player.hasPermission("skyprisoncore.inventories." + invName)) {
                                Bukkit.getScheduler().runTask(this, () -> player.openInventory(new DatabaseInventory(this, db, player,
                                        player.hasPermission("skyprisoncore.inventories." + invName + ".editing"), invName).getInventory()));
                            }
                        }
                    }
                }));
        this.manager.command(customInv.literal("create")
                .permission("skyprisoncore.command.custominv.create")
                .argument(StringArgument.of("name"))
                .argument(StringArgument.optional("display"))
                .argument(StringArgument.optional("colour"))
                .handler(c -> {
                    CustomInv inv = new CustomInv(db);
                    String name = c.get("name");
                    if(!inv.categoryExists(name)) {
                        String colour = c.getOrDefault("colour", null);
                        if(colour == null || NamedTextColor.NAMES.value(colour) != null || TextColor.fromHexString(colour) != null) {
                            inv.createCategory(name, c.getOrDefault("display", null), colour);
                        }
                    }
                }));
        Command.Builder<CommandSender> vote = this.manager.commandBuilder("vote")
                .permission("skyprisoncore.command.vote")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    Component msg = Component.text("Vote for our server!", NamedTextColor.DARK_RED, TextDecoration.BOLD);
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Planet Minecraft", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on PlanetMinecraft", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://www.planetminecraft.com/server/sky-prison/vote/")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft Serverlist", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft Serverlist", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraft-server-list.com/server/473461/vote/")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft Servers", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft Servers", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraftservers.org/vote/457013")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("TopG", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on TopG", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://topg.org/Minecraft/in-471006")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft Buzz", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft Buzz", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraft.buzz/vote/1142")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft MP", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft MP", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraft-mp.com/server/279527/vote/")));
                    sender.sendMessage(msg);
                });
        this.manager.command(vote);

        this.manager.command(vote.literal("history")
                .permission("skyprisoncore.command.vote.history")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(this, () -> player.openInventory(new VoteHistory(this, db, player.getUniqueId()).getInventory()));
                    }
                }));

        this.manager.command(vote.literal("history")
                .permission("skyprisoncore.command.vote.history.others")
                .argument(StringArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        String playerName = c.getOrDefault("player", player.getName());
                        UUID pUUID = PlayerManager.getPlayerId(playerName);
                        if (pUUID != null) {
                            Bukkit.getScheduler().runTask(this, () -> player.openInventory(new VoteHistory(this, db, pUUID).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("Specified player doesn't exist!"));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(this.manager.commandBuilder("stellraw")
                .permission("skyprisoncore.command.stellraw")
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    String msg = c.get("message");
                    Component fMsg = getParsedString(c.getSender(), "chat", msg);
                    getServer().sendMessage(fMsg);
                }));
        this.manager.command(this.manager.commandBuilder("votefix")
                .permission("skyprisoncore.command.votefix")
                .handler(c -> {
                    try {
                        String sql = "INSERT INTO votes (user_id, time, service, address, tokens) VALUES (?, ?, ?, ?, ?)";
                        FileInputStream fstream = new FileInputStream(this.getDataFolder()+ File.separator + "user_votes.txt");
                        long currTime = System.currentTimeMillis();
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream)); Connection conn = db.getConnection();
                             PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setLong(2, currTime);
                            ps.setString(3, "Unknown");
                            ps.setString(4, "Unknown");
                            ps.setInt(5, 0);
                            String line;
                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                                String[] parts = line.split(";");
                                if (parts.length == 2) {
                                    String userId = parts[0];
                                    int totalVotes = Integer.parseInt(parts[1]);
                                    for (int i = 0; i < totalVotes; i++) {
                                        ps.setString(1, userId);
                                        ps.addBatch();
                                        if (i % 100 == 0) {
                                            ps.executeBatch();
                                        }
                                    }
                                    ps.executeBatch();
                                }
                            }
                            System.out.println("DONE!");
                        } catch (IOException | SQLException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }));
        Command.Builder<CommandSender> mail = this.manager.commandBuilder("mail")
                .permission("skyprisoncore.command.mail");
        this.manager.command(mail.literal("history")
                .permission("skyprisoncore.command.mail.history")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(this, () -> player.openInventory(new MailHistory(this, db, player.getUniqueId()).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(mail.literal("history")
                .permission("skyprisoncore.command.mail.history.others")
                .argument(StringArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        String playerName = c.getOrDefault("player", player.getName());
                        UUID pUUID = PlayerManager.getPlayerId(playerName);
                        if (pUUID != null) {
                            Bukkit.getScheduler().runTask(this, () -> player.openInventory(new MailHistory(this, db, pUUID).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("Specified player doesn't exist!"));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(mail.literal("send")
                .permission("skyprisoncore.command.mail.send")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(this, () -> player.openInventory(new MailBoxSend(this, db, player,
                                player.hasPermission("skyprisoncore.command.mail.send.items")).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(mail.literal("open")
                .permission("skyprisoncore.command.mail.open")
                .argument(IntegerArgument.of("mailbox-id"))
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    Player player = c.getOrDefault("player", sender instanceof Player ? (Player) sender : null);
                    if(player != null) {
                        int mailBoxId = c.get("mailbox-id");
                        String mailBox = Mail.getMailBoxName(mailBoxId);
                        if(mailBox != null && !mailBox.isEmpty()) {
                            Bukkit.getScheduler().runTask(this, () -> player.openInventory(
                                    new MailBox(this, db, player, isOwner(player, mailBoxId), mailBoxId, 1).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("No mailbox found with that id!", NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("Incorrect Usage! /mail open <id> <player>"));
                    }
                }));

        this.manager.command(mail.literal("expand")
                .permission("skyprisoncore.command.mail.expand")
                .argument(PlayerArgument.of("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    Player player = c.get("player");
                    if(player.hasPermission("skyprisoncore.mailboxes.amount.2")) {
                        Bukkit.getScheduler().runTask(this, () -> asConsole("lp user " + player.getName() + " permission set skyprisoncore.mailboxes.amount.3"));
                    } else if(player.hasPermission("skyprisoncore.mailboxes.amount.1")) {
                        Bukkit.getScheduler().runTask(this, () -> asConsole("lp user " + player.getName() + " permission set skyprisoncore.mailboxes.amount.2"));
                    } else {
                        sender.sendMessage(Component.text("Player already has the maximum amount of mailboxes!", NamedTextColor.RED));
                    }
                }));
        Command.Builder<CommandSender> referral = this.manager.commandBuilder("referral", "ref", "refer")
                .permission("skyprisoncore.command.referral")
                .handler(c -> c.getSender().sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN)));

        manager.command(referral);

        this.manager.command((referral.literal("player"))
                .permission("skyprisoncore.command.referral.player")
                .argument(StringArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        String playerName = c.getOrDefault("player", null);
                        if (playerName != null) {
                            UUID pUUID = PlayerManager.getPlayerId(playerName);
                            if(pUUID != null) {
                                boolean hasReferred = false;
                                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM referrals WHERE referred_by = ?")) {
                                    ps.setString(1, player.getUniqueId().toString());
                                    ResultSet rs = ps.executeQuery();
                                    if (rs.next()) {
                                        hasReferred = true;
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                                if (!hasReferred) {
                                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(player.getUniqueId());
                                    long playtime = TimeUnit.MILLISECONDS.toHours(user.getTotalPlayTime());
                                    if (playtime >= 1 && playtime < 24) { // Checks that the player has played more than an hour on the server but less than 24 hours.
                                        CMIUser reffedPlayer = CMI.getInstance().getPlayerManager().getUser(pUUID);
                                        if (reffedPlayer != null) {
                                            if (!user.getLastIp().equalsIgnoreCase(reffedPlayer.getLastIp())) {
                                                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                                                        "INSERT INTO referrals (user_id, referred_by, refer_date) VALUES (?, ?, ?)")) {
                                                    ps.setString(1, reffedPlayer.getUniqueId().toString());
                                                    ps.setString(2, player.getUniqueId().toString());
                                                    ps.setLong(3, System.currentTimeMillis());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                Component beenReffed = Component.text(player.getName(), NamedTextColor.AQUA)
                                                        .append(Component.text(" has referred you! You have received ", NamedTextColor.DARK_AQUA))
                                                        .append(Component.text("250", NamedTextColor.YELLOW)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA));
                                                if (reffedPlayer.isOnline()) {
                                                    reffedPlayer.getPlayer().sendMessage(beenReffed);
                                                } else {
                                                    Notifications.createNotification("referred", player.getName(), reffedPlayer.getUniqueId().toString(), beenReffed, null, true);
                                                }
                                                player.sendMessage(Component.text("You sucessfully referred ", NamedTextColor.DARK_AQUA)
                                                        .append(Component.text(reffedPlayer.getName(), NamedTextColor.AQUA)).append(Component.text(" and have received ", NamedTextColor.DARK_AQUA))
                                                        .append(Component.text("50", NamedTextColor.GOLD)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA)));
                                                tokens.addTokens(reffedPlayer.getUniqueId(), 250, "Referred Someone", player.getName());
                                                tokens.addTokens(player.getUniqueId(), 50, "Was Referred", reffedPlayer.getName());
                                            } else {
                                                player.sendMessage(Component.text("/referral <player>", NamedTextColor.RED));
                                            }
                                        } else {
                                            player.sendMessage(Component.text("/referral <player>", NamedTextColor.RED));
                                        }
                                    } else {
                                        if (playtime < 1) {
                                            player.sendMessage(Component.text("You need to play 1 hour to be able to refer someone!", NamedTextColor.RED));
                                        } else {
                                            player.sendMessage(Component.text("You have played too long to refer anyone!", NamedTextColor.RED));
                                        }
                                    }
                                } else {
                                    player.sendMessage(Component.text("You have already referred someone!", NamedTextColor.RED));
                                }
                            } else {
                                player.sendMessage(Component.text("Specified player doesn't exist!", NamedTextColor.RED));
                            }
                        } else {
                            c.getSender().sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!", NamedTextColor.RED));
                    }
                }));
        this.manager.command(referral.literal("help")
                .permission("skyprisoncore.command.referral.help")
                .handler(c -> c.getSender().sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN))));

        this.manager.command(referral.literal("history", "list")
                .permission("skyprisoncore.command.referral.history")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(this, () -> player.openInventory(new Referral(this, db, player).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(referral.literal("history", "list")
                .permission("skyprisoncore.command.referral.history.others")
                .argument(StringArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        String playerName = c.getOrDefault("player", player.getName());
                        UUID pUUID = PlayerManager.getPlayerId(playerName);
                        if (pUUID != null) {
                            Bukkit.getScheduler().runTask(this, () -> player.openInventory(new Referral(this, db, player).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("Specified player doesn't exist!"));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        new ChatCommands(this, this.manager, discApi);
        new JailCommands(this, this.manager);
        new SecretsCommands(this, this.manager);

        Objects.requireNonNull(getCommand("tokens")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("token")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("tokens")).setTabCompleter(new TabCompleter());
        Objects.requireNonNull(getCommand("token")).setTabCompleter(new TabCompleter());
        Objects.requireNonNull(getCommand("donoradd")).setExecutor(new DonorAdd(getDatabase()));
        Objects.requireNonNull(getCommand("purchases")).setExecutor(new Purchases(getDatabase(), this));
        Objects.requireNonNull(getCommand("econcheck")).setExecutor(new EconomyCheck(this));
        Objects.requireNonNull(getCommand("permshop")).setExecutor(new PermShop());
        Objects.requireNonNull(getCommand("sponge")).setExecutor(new Sponge(this, getDatabase()));
        Objects.requireNonNull(getCommand("dontsell")).setExecutor(new DontSell(getDatabase()));
        Objects.requireNonNull(getCommand("bounty")).setExecutor(new Bounty(getDatabase(), this));
        Objects.requireNonNull(getCommand("killinfo")).setExecutor(new KillInfo(getDatabase()));
        Objects.requireNonNull(getCommand("firstjointop")).setExecutor(new FirstjoinTop(this, getDatabase()));

        Objects.requireNonNull(getCommand("ignoretp")).setExecutor(new IgnoreTeleport(this, getDatabase()));

        Objects.requireNonNull(getCommand("buyback")).setExecutor(new BuyBack(this, getDatabase()));
        Objects.requireNonNull(getCommand("daily")).setExecutor(new Daily(this, getDatabase()));
        Objects.requireNonNull(getCommand("shopban")).setExecutor(new ShopBan(getDatabase()));
        Objects.requireNonNull(getCommand("enchtable")).setExecutor(new EnchTable());
        Objects.requireNonNull(getCommand("removeitalics")).setExecutor(new RemoveItalics(this));
        Objects.requireNonNull(getCommand("bottledexp")).setExecutor(new BottledExp(this));
        Objects.requireNonNull(getCommand("transportpass")).setExecutor(new TransportPass(this));
        Objects.requireNonNull(getCommand("casino")).setExecutor(new Casino(this, getDatabase()));
        Objects.requireNonNull(getCommand("skyplot")).setExecutor(new SkyPlot(this));
        Objects.requireNonNull(getCommand("plot")).setExecutor(new PlotTeleport(this));
        Objects.requireNonNull(getCommand("moneyhistory")).setExecutor(new MoneyHistory(this));
        Objects.requireNonNull(getCommand("tags")).setExecutor(new Tags(this, getDatabase()));
        Objects.requireNonNull(getCommand("bomb")).setExecutor(new Bomb(this));
        Objects.requireNonNull(getCommand("minereset")).setExecutor(new MineReset(this));
        Objects.requireNonNull(getCommand("randomgive")).setExecutor(new RandomGive(this));
        Objects.requireNonNull(getCommand("customrecipes")).setExecutor(new CustomRecipes(this));
        Objects.requireNonNull(getCommand("claim")).setExecutor(new Claim(this, getDatabase()));

        Objects.requireNonNull(getCommand("rename")).setExecutor(new Rename());
        Objects.requireNonNull(getCommand("itemlore")).setExecutor(new ItemLore(this));
        Objects.requireNonNull(getCommand("namecolour")).setExecutor(new NameColour(this, getDatabase()));
        Objects.requireNonNull(getCommand("news")).setExecutor(new News(this, getDatabase()));

        Objects.requireNonNull(getCommand("discord")).setExecutor(new Discord(this, getDatabase(), discApi));
    }

    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockBreak(this, dailyMissions, particles), this);
        pm.registerEvents(new BlockDamage(this, getDatabase(), dailyMissions), this);
        pm.registerEvents(new BlockPlace(this, dailyMissions, db), this);
        pm.registerEvents(new BrewDrink(getDatabase()), this);
        pm.registerEvents(new CMIPlayerTeleportRequest(getDatabase()), this);
        pm.registerEvents(new CMIUserBalanceChange(this), this);
        pm.registerEvents(new EntityDamageByEntity(this), this);
        pm.registerEvents(new EntityDeath(this, getDatabase(), dailyMissions), this);
        pm.registerEvents(new EntityPickupItem(this), this);
        pm.registerEvents(new InventoryClick(this, new EconomyCheck(this), new Bounty(getDatabase(), this),
                new Daily(this, getDatabase()), new MoneyHistory(this),
                new BuyBack(this, getDatabase()), getDatabase(), new Tags(this, getDatabase()), particles, new CustomRecipes(this)), this);
        pm.registerEvents(new InventoryOpen(this), this);
        pm.registerEvents(new LeavesDecay(), this);
        pm.registerEvents(new McMMOLevelUp(this), this);
        pm.registerEvents(new PlayerChangedWorld(), this);
        pm.registerEvents(new PlayerInteract(this, db), this);
        pm.registerEvents(new PlayerMove(this), this);
        pm.registerEvents(new PlayerPostRespawn(), this);
        pm.registerEvents(new PlayerTag(this), this);
        pm.registerEvents(new PlayerTeleport(this), this);
        pm.registerEvents(new PlayerUnJail(), this);
        pm.registerEvents(new PlayerUntag(), this);
        pm.registerEvents(new ShopCreate(this), this);
        pm.registerEvents(new ShopPostTransaction(getDatabase(), dailyMissions), this);
        pm.registerEvents(new ShopPreTransaction(getDatabase()), this);
        pm.registerEvents(new ShopPurchase(getDatabase()), this);
        pm.registerEvents(new ShopSuccessPurchase(this), this);
        pm.registerEvents(new UnsellRegion(), this);
        pm.registerEvents(new PlayerFish(dailyMissions), this);
        pm.registerEvents(new InventoryClose(), this);
        pm.registerEvents(new EntityDamage(this), this);
        pm.registerEvents(new PlayerCommandPreprocess(), this);
        pm.registerEvents(new ParkourFinish(this, dailyMissions), this);
        pm.registerEvents(new PlayerTogglePvP(), this);
        pm.registerEvents(new ServerLoad(this, particles, getDatabase()), this);
        pm.registerEvents(new CrateObtainReward(getDatabase()), this);
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

        pm.registerEvents(new AsyncChat(this, discApi, getDatabase(), new Tags(this, getDatabase()), new ItemLore(this)), this);
        pm.registerEvents(new PlayerQuit(this, getDatabase(), discApi, dailyMissions), this);
        pm.registerEvents(new PlayerJoin(this, getDatabase(), discApi, dailyMissions, particles), this);

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

    public static boolean isOwner(Player player, int mailBox) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT owner_id FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UUID ownerId = UUID.fromString(rs.getString(1));
                return player.getUniqueId().equals(ownerId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static @NotNull TagResolver papiTag(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            final String papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value();
            final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + papiPlaceholder + '%');
            final Component componentPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);
            return Tag.inserting(componentPlaceholder);
        });
    }


    private DatabaseHook getDatabase() {
        return db;
    }

    public void sendNewsMessage(Player player, int newsMessage) {
        Component msg = Component.newline().append(MiniMessage.miniMessage().deserialize("<b><#0fc3ff>Sky<#ff0000>Prison <#e65151>News</b>").appendNewline().appendSpace());
        HoverEvent<Component> hoverEvent = null;
        ClickEvent clickEvent = null;
        if(newsMessage == 0) {
            LinkedHashMap<HashMap<String, Object>, Integer> newsMessages = new LinkedHashMap<>();

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT content, hover, click_type, click_data, permission, priority, " +
                    "limited_time, limited_start, limited_end FROM news")) {
                ps.setInt(1, newsMessage);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if(rs.getInt(7) != 0) {
                        long start = rs.getLong(8);
                        long end = rs.getLong(9);
                        long curr = System.currentTimeMillis();
                        if(start < curr || end < curr) continue;
                    }
                    if(player.hasPermission("skyprisoncore.news." + rs.getString(5))) {
                        HashMap<String, Object> messageComps = new HashMap<>();
                        Component message = MiniMessage.miniMessage().deserialize(rs.getString(1));
                        messageComps.put("content", message);
                        if(!rs.getString(2).isEmpty()) {
                            messageComps.put("hover", HoverEvent.showText(MiniMessage.miniMessage().deserialize(rs.getString(2))));
                        }
                        if(!rs.getString(3).isEmpty()) {
                            Action action = Objects.requireNonNull(Action.NAMES.value(rs.getString(3).toLowerCase()));
                            String value = "";
                            switch (action) {
                                case OPEN_URL, SUGGEST_COMMAND, COPY_TO_CLIPBOARD -> value = rs.getString(4);
                                case RUN_COMMAND -> value = "/" + rs.getString(4);
                            }
                            clickEvent = ClickEvent.clickEvent(action, value);
                            messageComps.put("click", clickEvent);
                        }
                        newsMessages.put(messageComps, rs.getInt(6));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(!newsMessages.isEmpty()) {
                List<Integer> cumulativeWeights = new ArrayList<>();
                int totalWeight = 0;

                for (Integer weight : newsMessages.values()) {
                    totalWeight += weight;
                    cumulativeWeights.add(totalWeight);
                }

                Random rand = new Random();
                int randomWeight = rand.nextInt(totalWeight);

                int index = Collections.binarySearch(cumulativeWeights, randomWeight);

                if (index < 0) {
                    index = Math.abs(index + 1);
                }
                HashMap<String, Object> finalMsg = new ArrayList<>(newsMessages.keySet()).get(index);
                Component content = (Component) finalMsg.get("content");
                hoverEvent = (HoverEvent<Component>) finalMsg.getOrDefault("hover", null);
                clickEvent = (ClickEvent) finalMsg.getOrDefault("click", null);
                msg = msg.append(content);
            }
        } else {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT content, hover, click_type, click_data FROM news WHERE id = ?")) {
                ps.setInt(1, newsMessage);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Component message = MiniMessage.miniMessage().deserialize(rs.getString(1));
                    msg = msg.append(message);
                    if(!rs.getString(2).isEmpty()) {
                        hoverEvent = HoverEvent.showText(MiniMessage.miniMessage().deserialize(rs.getString(2)));
                    }
                    if(!rs.getString(3).isEmpty()) {
                        Action action = Objects.requireNonNull(Action.NAMES.value(rs.getString(3).toLowerCase()));
                        String value = "";
                        switch (action) {
                            case OPEN_URL, SUGGEST_COMMAND, COPY_TO_CLIPBOARD -> value = rs.getString(4);
                            case RUN_COMMAND -> value = "/" + rs.getString(4);
                        }
                        clickEvent = ClickEvent.clickEvent(action, value);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        msg = msg.appendNewline().hoverEvent(hoverEvent).clickEvent(clickEvent);
        player.sendMessage(msg);
    }

    public String ticksToTime(int ticks) { // 500 -> 24:00
        String time = String.valueOf(ticks / 1000.0);
        String[] split = time.split("\\.");
        int minutes = (Integer.parseInt(split[1]) * 60) % 60;
        int hours = Integer.parseInt(split[0]);
        String sMinutes = String.valueOf(minutes);
        String sHours = String.valueOf(hours);
        if(minutes < 10) {
            sMinutes = "0" + sMinutes;
        }
        if(hours < 10) {
            sHours = "0" + sHours;
        }
        return sHours + ":" + sMinutes;
    }

    public int timeToTicks(String time) {
        String[] split = time.split(":");
        return (int) ((Integer.parseInt(split[0]) * 1000) + ((Math.rint(Integer.parseInt(split[1]) / 60.0 * 100.0) / 100.0) * 1000));
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
            try(Connection conn = getDatabase().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT last_collected FROM dailies WHERE user_id = ?")) {
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

    public static String getQuestionMarks(List<String> list) {
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

    public void tellConsole(Component message){
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public void asConsole(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public String formatNumber(double value) {
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        return df.format(value);
    }

    public Map<Player, Map.Entry<Player, Long>> hitcd = new HashMap<>();
    public boolean isGuardGear(ItemStack i) {
        if (i != null) {
            if (i.getType() == Material.CHAINMAIL_HELMET || i.getType() == Material.CHAINMAIL_CHESTPLATE || i.getType() == Material.CHAINMAIL_LEGGINGS || i.getType() == Material.CHAINMAIL_BOOTS || i.getType() == Material.DIAMOND_SWORD) {
                return true;
            } else if (i.getType() == Material.BOW && i.getItemMeta().hasDisplayName()) {
                return i.getItemMeta().hasDisplayName() && Objects.requireNonNull(i.getItemMeta().displayName()).toString().contains("Guard Bow") && i.getItemMeta().isUnbreakable();
            } else if (i.getType() == Material.SHIELD && i.getItemMeta().hasDisplayName()) {
                return i.getItemMeta().hasDisplayName() && Objects.requireNonNull(i.getItemMeta().displayName()).toString().contains("Guard Shield") && i.getItemMeta().isUnbreakable();
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
}
