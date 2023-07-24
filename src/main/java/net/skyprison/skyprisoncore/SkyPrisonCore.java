package net.skyprison.skyprisoncore;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.skyprison.skyprisoncore.commands.*;
import net.skyprison.skyprisoncore.commands.chats.Admin;
import net.skyprison.skyprisoncore.commands.chats.Build;
import net.skyprison.skyprisoncore.commands.chats.Guard;
import net.skyprison.skyprisoncore.commands.chats.Staff;
import net.skyprison.skyprisoncore.commands.discord.Discord;
import net.skyprison.skyprisoncore.commands.donations.DonorAdd;
import net.skyprison.skyprisoncore.commands.donations.Purchases;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.guard.*;
import net.skyprison.skyprisoncore.commands.secrets.SecretFound;
import net.skyprison.skyprisoncore.commands.secrets.SecretsGUI;
import net.skyprison.skyprisoncore.inventories.*;
import net.skyprison.skyprisoncore.items.BlacksmithEnd;
import net.skyprison.skyprisoncore.items.Greg;
import net.skyprison.skyprisoncore.items.TreeFeller;
import net.skyprison.skyprisoncore.items.Vouchers;
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
import org.bukkit.command.ConsoleCommandSender;
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

    public HashMap<Audience, Audience> lastMessaged = new HashMap<>();

    private BukkitCommandManager<CommandSender> manager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private CommandConfirmationManager<CommandSender> confirmationManager;

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
                        .append(Component.text("Sky", TextColor.fromHexString("#00ffff")))
                        .append(Component.text("Prison", TextColor.fromHexString("#FF0000")))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(component).build()
                ).apply(this.manager, AudienceProvider.nativeAudience());

        this.registerCommands();


        registerEvents();

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this, dailyMissions, getDatabase()).register();
            getLogger().info("Placeholders registered");
        }

        Timer timer = new Timer();
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DAY_OF_YEAR, 1);
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 1);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        timer.schedule(new NextDayTask(this, getDatabase()), date.getTime());

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

    public void sendPrivateMessage(CommandSender sender, Audience receiver, String message) {
        Component senderName = sender.name();
        Component pMsg = Component.empty().append(Component.text(" » ", TextColor.fromHexString("#940b34")))
                .append(getParsedString(sender, "private", message).colorIfAbsent(NamedTextColor.GRAY));
        if (sender instanceof Player toPlayer) {
            Component customName = toPlayer.customName();
            if (customName != null) senderName = customName;
        }

        Component receiverName = Component.text("Unknown");
        if(receiver instanceof Player player) {
            receiverName = Objects.requireNonNullElse(player.customName(), player.displayName());
        } else if(receiver instanceof CommandSender receiving) {
            receiverName = receiving.name();
        }

        Component msgTo = Component.empty().append(Component.text("Me", TextColor.fromHexString("#f02d68"))).append(Component.text(" ⇒ ", TextColor.fromHexString("#940b34")))
                .append(receiverName.colorIfAbsent(TextColor.fromHexString("#f02d68")));
        Component msgFrom = Component.empty().append(senderName.colorIfAbsent(TextColor.fromHexString("#f02d68"))).append(Component.text(" ⇒ ", TextColor.fromHexString("#940b34")))
                .append(Component.text("Me", TextColor.fromHexString("#f02d68")));

        sender.sendMessage(msgTo.append(pMsg));
        receiver.sendMessage(msgFrom.append(pMsg));

        if (lastMessaged.isEmpty() || !lastMessaged.containsKey(sender) || !lastMessaged.get(sender).equals(receiver)) {
            lastMessaged.put(sender, receiver);
        }
        if (lastMessaged.isEmpty() || !lastMessaged.containsKey(receiver)) {
            lastMessaged.put(receiver, sender);
        }
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
                        player.getInventory().addItem(treeItem);
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
                                player.getInventory().addItem(voucherItem);
                                c.getSender().sendMessage(Component.text("Successfully sent!"));
                            }
                        }));

        Command.Builder<CommandSender> greg = this.manager.commandBuilder("greg")
                .permission("skyprisoncore.command.greg");
        List<String> gregOptions = List.of("grease", "allay-dust", "strength", "speed", "fire-resistance", "instant-health", "instant-damage");
        this.manager.command(greg.literal("give")
                .permission("skyprisoncore.command.greg.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> gregOptions))
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
                            player.getInventory().addItem(item);
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
                            player.getInventory().addItem(item);
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
                            player.getInventory().addItem(item);
                            c.getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));

        this.manager.command(this.manager.commandBuilder("msg")
                .permission("skyprisoncore.command.msg")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    final Player player = c.get("player");
                    final String message = c.get("message");
                    sendPrivateMessage(sender, player, message);
                }));

        this.manager.command(this.manager.commandBuilder("bartender")
                .permission("skyprisoncore.command.bartender")
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    Player player = c.getOptional("player").isPresent() ? (Player) c.getOptional("player").get() : c.getSender() instanceof Player ? (Player) c.getSender() : null;
                    if(player != null) {
                        player.openInventory(new DatabaseInventory(this, db, player, player.hasPermission("skyprisoncore.inventories.bartender.editing"), "bartender").getInventory());
                    } else {
                        c.getSender().sendMessage(Component.text("Invalid Usage! /bartender (player)"));
                    }
                }));

        this.manager.command(this.manager.commandBuilder("reply", "r")
                .permission("skyprisoncore.command.reply")
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(!lastMessaged.isEmpty() && lastMessaged.containsKey(sender) && lastMessaged.get(sender) != null
                            && ((lastMessaged.get(sender) instanceof Player player && player.isOnline()) || lastMessaged.get(sender) instanceof ConsoleCommandSender)) {
                        sendPrivateMessage(sender, lastMessaged.get(sender), c.get("message"));
                    } else {
                        sender.sendMessage(Component.text("Noone to reply to found..", NamedTextColor.RED));
                    }
                }));

        Objects.requireNonNull(getCommand("tokens")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("token")).setExecutor(tokens);
        Objects.requireNonNull(getCommand("tokens")).setTabCompleter(new TabCompleter());
        Objects.requireNonNull(getCommand("token")).setTabCompleter(new TabCompleter());
        Objects.requireNonNull(getCommand("donoradd")).setExecutor(new DonorAdd(getDatabase()));
        Objects.requireNonNull(getCommand("purchases")).setExecutor(new Purchases(getDatabase(), this));
        Objects.requireNonNull(getCommand("econcheck")).setExecutor(new EconomyCheck(this));
        Objects.requireNonNull(getCommand("permshop")).setExecutor(new PermShop());
        Objects.requireNonNull(getCommand("sponge")).setExecutor(new Sponge(this, getDatabase()));
        Objects.requireNonNull(getCommand("dropchest")).setExecutor(new DropChest(this));
        Objects.requireNonNull(getCommand("dontsell")).setExecutor(new DontSell(getDatabase()));
        Objects.requireNonNull(getCommand("endupgrade")).setExecutor(new EndUpgrade(this));
        Objects.requireNonNull(getCommand("secretfound")).setExecutor(new SecretFound(this, dailyMissions, getDatabase()));
        Objects.requireNonNull(getCommand("rewards")).setExecutor(new SecretsGUI(this, getDatabase()));
        Objects.requireNonNull(getCommand("bounty")).setExecutor(new Bounty(getDatabase(), this));
        Objects.requireNonNull(getCommand("killinfo")).setExecutor(new KillInfo(getDatabase()));
        Objects.requireNonNull(getCommand("firstjointop")).setExecutor(new FirstjoinTop(this, getDatabase()));
        Objects.requireNonNull(getCommand("sword")).setExecutor(new Sword());
        Objects.requireNonNull(getCommand("bow")).setExecutor(new Bow());
        Objects.requireNonNull(getCommand("contraband")).setExecutor(new Contraband());
        Objects.requireNonNull(getCommand("ignoretp")).setExecutor(new IgnoreTeleport(this, getDatabase()));
        Objects.requireNonNull(getCommand("guardduty")).setExecutor(new GuardDuty(this));
        Objects.requireNonNull(getCommand("safezone")).setExecutor(new Safezone(this));
        Objects.requireNonNull(getCommand("buyback")).setExecutor(new BuyBack(this, getDatabase()));
        Objects.requireNonNull(getCommand("daily")).setExecutor(new Daily(this, getDatabase()));
        Objects.requireNonNull(getCommand("shopban")).setExecutor(new ShopBan(getDatabase()));
        Objects.requireNonNull(getCommand("enchtable")).setExecutor(new EnchTable());
        Objects.requireNonNull(getCommand("removeitalics")).setExecutor(new RemoveItalics(this));
        Objects.requireNonNull(getCommand("bottledexp")).setExecutor(new BottledExp(this));
        Objects.requireNonNull(getCommand("transportpass")).setExecutor(new TransportPass(this));
        Objects.requireNonNull(getCommand("bail")).setExecutor(new Bail(this));
        Objects.requireNonNull(getCommand("casino")).setExecutor(new Casino(this, getDatabase()));
        Objects.requireNonNull(getCommand("skyplot")).setExecutor(new SkyPlot(this));
        Objects.requireNonNull(getCommand("plot")).setExecutor(new PlotTeleport(this));
        Objects.requireNonNull(getCommand("moneyhistory")).setExecutor(new MoneyHistory(this));
        Objects.requireNonNull(getCommand("tags")).setExecutor(new Tags(this, getDatabase()));
        Objects.requireNonNull(getCommand("bomb")).setExecutor(new Bomb(this));
        Objects.requireNonNull(getCommand("furnace")).setExecutor(new VirtualFurnace(this));
        Objects.requireNonNull(getCommand("minereset")).setExecutor(new MineReset(this));
        Objects.requireNonNull(getCommand("randomgive")).setExecutor(new RandomGive(this));
        Objects.requireNonNull(getCommand("customrecipes")).setExecutor(new CustomRecipes(this));
        Objects.requireNonNull(getCommand("claim")).setExecutor(new Claim(this, getDatabase()));
        Objects.requireNonNull(getCommand("custominv")).setExecutor(new CustomInv(this, getDatabase()));

        Objects.requireNonNull(getCommand("rename")).setExecutor(new Rename());
        Objects.requireNonNull(getCommand("itemlore")).setExecutor(new ItemLore(this));
        Objects.requireNonNull(getCommand("namecolour")).setExecutor(new NameColour(this, getDatabase()));
        Objects.requireNonNull(getCommand("news")).setExecutor(new News(this, getDatabase()));

        Objects.requireNonNull(getCommand("referral")).setExecutor(new Referral(this, getDatabase()));
        Objects.requireNonNull(getCommand("discord")).setExecutor(new Discord(this, getDatabase(), discApi));
        Objects.requireNonNull(getCommand("g")).setExecutor(new Guard(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("b")).setExecutor(new Build(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("a")).setExecutor(new Admin(new ChatUtils(this, discApi)));
        Objects.requireNonNull(getCommand("s")).setExecutor(new Staff(new ChatUtils(this, discApi)));
    }


    public void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new BlockBreak(this, dailyMissions, particles), this);
        pm.registerEvents(new BlockDamage(this, getDatabase(), dailyMissions), this);
        pm.registerEvents(new BlockPlace(this, dailyMissions), this);
        pm.registerEvents(new BrewDrink(getDatabase()), this);
        pm.registerEvents(new CMIPlayerTeleportRequest(getDatabase()), this);
        pm.registerEvents(new CMIUserBalanceChange(this), this);
        pm.registerEvents(new EntityDamageByEntity(this), this);
        pm.registerEvents(new EntityDeath(this, new Safezone(this), getDatabase(), dailyMissions), this);
        pm.registerEvents(new EntityPickupItem(this), this);
        pm.registerEvents(new EntityRemoveFromWorld(this), this);
        pm.registerEvents(new InventoryClick(this, new EconomyCheck(this), new DropChest(this), new Bounty(getDatabase(), this),
                new SecretsGUI(this, getDatabase()), new Daily(this, getDatabase()), new MoneyHistory(this), new EndUpgrade(this),
                new BuyBack(this, getDatabase()), new SkyPlot(this), getDatabase(), new Tags(this, getDatabase()), particles, new CustomRecipes(this)), this);
        pm.registerEvents(new InventoryOpen(this), this);
        pm.registerEvents(new LeavesDecay(), this);
        pm.registerEvents(new McMMOLevelUp(this), this);
        pm.registerEvents(new PlayerChangedWorld(), this);
        pm.registerEvents(new PlayerInteract(this), this);
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

    public String hasNotification(String id, OfflinePlayer player) {
        String notification = "";
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT extra_data FROM notifications WHERE id = ? AND user_id = ?")) {
            ps.setString(1, id);
            ps.setString(2, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notification = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notification;
    }

    public HashMap<Integer, List<String>> getNotificationsFromExtra(List<String> extraData) {
        HashMap<Integer, List<String>> notifications = new HashMap<>();
        if(extraData.isEmpty()) return notifications;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT type, extra_data, user_id FROM notifications WHERE extra_data IN " + getQuestionMarks(extraData))) {
            for (int i = 0; i < extraData.size(); i++) {
                ps.setString(i + 1, extraData.get(i));
            }
            ResultSet rs = ps.executeQuery();
            int i = 0;
            while (rs.next()) {
                List<String> data = new ArrayList<>();
                data.add(rs.getString(1)); // type
                data.add(rs.getString(2)); // data
                data.add(rs.getString(3)); // uuid
                notifications.put(i, data);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }


    public List<String> hasNotifications(String type, List<String> extraData, OfflinePlayer player) {
        List<String> notifications = new ArrayList<>();
        if(extraData.isEmpty()) return notifications;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT extra_data FROM notifications WHERE type = ? AND user_id = ? AND extra_data IN "
                + getQuestionMarks(extraData))) {
            ps.setString(1, type);
            ps.setString(2, player.getUniqueId().toString());

            for (int i = 0; i < extraData.size(); i++) {
                ps.setString(i + 3, extraData.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }


    public static void scheduleForOnline(String pUUID, String type, String content) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO schedule_online (user_id, type, content) VALUES (?, ?, ?)")) {
            ps.setString(1, pUUID);
            ps.setString(2, type);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createNotification(String type, String extraData, String pUUID, Component msg, String id, boolean deleteOnView) {
        if(id == null || id.isEmpty()) id = UUID.randomUUID().toString();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO notifications (id, type, extra_data, user_id, message, delete_on_view) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, type);
            ps.setString(3, extraData);
            ps.setString(4, pUUID);
            ps.setString(5, GsonComponentSerializer.gson().serialize(msg));
            ps.setInt(6, deleteOnView ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNotification(String id) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNotification(String type, String extraData, OfflinePlayer player) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE extra_data = ? AND user_id = ? AND type = ?")) {
            ps.setString(1, extraData);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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


    public UUID getPlayer(String name) {
        try(Connection conn = getDatabase().getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE current_name = ?")) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return UUID.fromString(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getQuestionMarks(List<String> list) {
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


