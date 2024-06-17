package net.skyprison.skyprisoncore.commands;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.items.Greg;
import net.skyprison.skyprisoncore.utils.JailTimer;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.mariadb.jdbc.Statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.skyprison.skyprisoncore.SkyPrisonCore.*;
import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

public class JailCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSourceStack> manager;
    public JailCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSourceStack> manager) {
        this.plugin = plugin;
        this.manager = manager;
        createJailCommands();
    }
    private void createJailCommands() {
        manager.command(manager.commandBuilder("jail")
                .permission("skyprisoncore.command.jail")
                .required("player", playerParser())
                .optional("reason", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final String message = c.getOrDefault("reason", null);

                    setJail(sender, player, message, TimeUnit.MINUTES.toMillis(5), true);
                }));
        manager.command(manager.commandBuilder("unjail")
                .permission("skyprisoncore.command.unjail")
                .required("player", playerParser(), SuggestionProvider.suggestingStrings(currentlyJailed.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).map(Player::getName).toList()))
                .optional("reason", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final String message = c.getOrDefault("reason", null);
                    setUnjail(sender, player, message, "unjail");
                }));
        manager.command(manager.commandBuilder("bribe")
                .permission("skyprisoncore.command.bribe")
                .required("player", playerParser(), SuggestionProvider.suggestingStrings(currentlyJailed.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).map(Player::getName).toList()))
                .required("type", integerParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final int type = c.get("type");
                    bribeCooldown.put(player.getUniqueId(), TimeUnit.MINUTES.toMillis(10) + System.currentTimeMillis());
                    if(type > 0) {
                        plugin.getServer().getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(
                                plugin.getServer().getConsoleSender(), "cmi money take " + player.getName() + " " + type));
                        setUnjail(sender, player, "bribe", "bribe");
                    } else if(type == 0) {
                        releasePapers(sender, player);
                    } else if(type == -1) {
                        fakeReleasePapers(player);
                    } else if(type == -2) {
                        setUnjail(sender, player, "fake-release-papers", "fake-release-papers");
                    } else if(type == -3) {
                        extendJail(player);
                    }
                }));
        manager.command(manager.commandBuilder("bow")
                .permission("skyprisoncore.command.bow")
                .required("player", playerParser())
                .optional("reason", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final String message = c.getOrDefault("reason", "Bow / Crossbow");
                    contrabandMessager(sender, player, "bow", message);
                }));
        manager.command(manager.commandBuilder("sword")
                .permission("skyprisoncore.command.sword")
                .required("player", playerParser())
                .optional("reason", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final String message = c.getOrDefault("reason", "Sword");
                    contrabandMessager(sender, player, "sword", message);
                }));
        manager.command(manager.commandBuilder("contraband")
                .permission("skyprisoncore.command.contraband")
                .required("player", playerParser())
                .optional("reason", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final String message = c.getOrDefault("reason", "Contraband");
                    contrabandMessager(sender, player, "contraband", message);
                }));
        manager.command(manager.commandBuilder("safezone")
                .permission("skyprisoncore.command.safezone")
                .required("player", playerParser())
                .optional("reason", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    if(!sender.equals(player)) {
                        final String message = c.getOrDefault("reason", "Safezoning");
                        int viols = safezoneViolators.getOrDefault(player.getUniqueId(), 0) + 1;
                        int violsLeft = 3 - viols;
                        if (viols < 3) {
                            player.sendMessage(Component.text("You have received 1 safezone warn(s)! (" + violsLeft + " warn(s) left until jail!)", NamedTextColor.RED));
                            sender.sendMessage(Component.text("Target has received 1 safezone warn(s)! (" + violsLeft + " warn(s) left until jail!)", NamedTextColor.RED));
                            safezoneViolators.put(player.getUniqueId(), viols);
                        } else {
                            player.sendMessage(Component.text("You have been jailed for safezoning!", NamedTextColor.RED));
                            sender.sendMessage(Component.text("Target has been jailed!", NamedTextColor.RED));
                            safezoneViolators.remove(player.getUniqueId());
                            setJail(sender, player, message, TimeUnit.MINUTES.toMillis(5), true);
                        }
                    } else {
                        sender.sendMessage(Component.text("You can't /safezone yourself!", NamedTextColor.RED));
                    }
                }));
        manager.command(manager.commandBuilder("guardduty")
                .permission("skyprisoncore.command.guardduty")
                .handler(c -> {
                    LinkedHashMap<String, String> guardRanks = new LinkedHashMap<>() {{
                        put("srguard", "skyprisoncore.guard.srguard");
                        put("guard", "skyprisoncore.guard.guard");
                        put("trguard", "skyprisoncore.guard.trguard");
                    }};
                    Player player = (Player) c.sender().getSender();
                    String action;
                    String messageAction;

                    if (!player.hasPermission("skyprisoncore.guard.onduty")) {
                        action = "add";
                        messageAction = "ON";
                        player.sendMessage(Component.text("You are now ON duty!", NamedTextColor.RED));
                    } else {
                        action = "remove";
                        messageAction = "OFF";
                        PlayerManager.checkGuardGear(player);
                        player.sendMessage(Component.text("You are now OFF duty!", NamedTextColor.RED));
                    }
                    for (Map.Entry<String, String> entry : guardRanks.entrySet()) {
                        if (player.hasPermission(entry.getValue())) {
                            plugin.getServer().getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(),
                                    "lp user " + player.getName() + " parent " + action + " " + entry.getKey()));
                            break;
                        }
                    }
                    plugin.getServer().sendMessage(Component.text("Guard ", NamedTextColor.AQUA)
                            .append(player.displayName().colorIfAbsent(NamedTextColor.BLUE)).append(Component.text(" is now ", NamedTextColor.AQUA))
                            .append(Component.text(messageAction, NamedTextColor.AQUA, TextDecoration.BOLD)).append(Component.text(" duty!", NamedTextColor.AQUA)));
                }));
    }
    private void contrabandMessager(CommandSender sender, Player player, String type, String reason) {
        Component cbPrefix = Component.text("Contraband", TextColor.fromHexString("#564387")).append(Component.text(" » ", NamedTextColor.DARK_GRAY));
        if(!sender.equals(player)) {
            MaterialSetTag mats = type.equalsIgnoreCase("bow") ? MaterialTags.BOWS : type.equalsIgnoreCase("sword") ? MaterialTags.SWORDS :
                    new MaterialSetTag(new NamespacedKey(plugin, "contraband"), Material.EGG, Material.SNOWBALL, Material.FLINT_AND_STEEL, Material.TRIDENT);
            if(mats == null) return;
            AtomicBoolean hasContraband = new AtomicBoolean(false);
            mats.getValues().forEach(mat -> {
                if(player.getInventory().contains(mat, 1)) {
                    hasContraband.set(true);
                }
            });

            if(!hasContraband.get()) {
                sender.sendMessage(cbPrefix.append(Component.text("Player doesn't have any " + type + (type.equalsIgnoreCase("contraband") ? "!" : "s!"), NamedTextColor.RED)));
                return;
            }



            type = type.equalsIgnoreCase("bow") ? "bow / crossbow" : type;
            ClickCallback.Options options = ClickCallback.Options.builder()
                    .lifetime(Duration.ofSeconds(30))
                    .uses(1)
                    .build();
            Timer timer = new Timer();
            String finalType = type;
            timer.scheduleAtFixedRate(new TimerTask() {
                int i = 0;

                @Override
                public void run() {
                    if (i == 6) {
                        sender.sendMessage(cbPrefix.append(Component.text("Time's Up! Did the player hand over their " + finalType + "? ", TextColor.fromHexString("#4dabdd")))
                                .append(Component.text("CLICK HERE TO JAIL PLAYER", NamedTextColor.RED, TextDecoration.BOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to jail player!", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.callback(c ->
                                                setJail(sender, player, reason, TimeUnit.MINUTES.toMillis(5), true), options))));
                        timer.cancel();
                    } else {
                        int timeLeft = 5 - i;
                        sender.sendMessage(cbPrefix.append(Component.text("They have ", TextColor.fromHexString("#4dabdd")))
                                .append(Component.text(timeLeft, NamedTextColor.YELLOW, TextDecoration.BOLD))
                                .append(Component.text(" seconds to hand over their " + finalType + "!", TextColor.fromHexString("#4dabdd"))));

                        player.sendMessage(cbPrefix.append(Component.text("You have ", TextColor.fromHexString("#4dabdd")))
                                .append(Component.text(timeLeft, NamedTextColor.YELLOW, TextDecoration.BOLD))
                                .append(Component.text(" seconds to hand over your " + finalType + "!", TextColor.fromHexString("#4dabdd"))));
                        i++;
                    }
                }
            }, 0, 1000);
        } else {
            sender.sendMessage(Component.text("You can't /" + type + " yourself!", NamedTextColor.RED));
        }
    }
    public static void setJail(CommandSender sender, Player player, String reason, long time, boolean firstTime) {
        int logsId = 0;
        long totalTime = time;

        UUID senderId = null;
        if(sender instanceof Player playerSender) senderId = playerSender.getUniqueId();
        if(firstTime) {
            Component start = Component.empty().color(NamedTextColor.GRAY);
            Component playerName = Component.text(player.getName(), TextColor.fromHexString("#e23857"), TextDecoration.BOLD);
            Component senderName = Component.text(sender.getName(), TextColor.fromHexString("#e23857"), TextDecoration.BOLD);
            Component yourself = Component.text("You've", TextColor.fromHexString("#e23857"), TextDecoration.BOLD);
            Component timeComp = Component.text(" for ").append(Component.text("5", TextColor.fromHexString("#e23857"), TextDecoration.BOLD))
                    .append(Component.text(" minutes!"));
            Component reasonMsg = reason != null ? Component.text("\n Reason").append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(reason)) : Component.empty();

            sender.sendMessage(start.append(yourself).append(Component.text(" successfully jailed ")).append(playerName)
                    .append(timeComp).append(reasonMsg));
            player.sendMessage(start.append(yourself).append(Component.text(" been jailed by ")).append(senderName)
                    .append(timeComp).append(reasonMsg));
            Audience receivers =  Bukkit.getServer().filterAudience(audience -> !audience.equals(sender) && !audience.equals(player));
            receivers.sendMessage(start.append(playerName).append(Component.text(" has been jailed by ")).append(senderName)
                    .append(timeComp).append(reasonMsg));

            try (Connection conn = db.getConnection(); PreparedStatement ps =
                    conn.prepareStatement("INSERT INTO logs_jail (sender_id, target_id, reason, type, active, time_started, length_total) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, senderId != null ? senderId.toString() : null);
                ps.setString(2, player.getUniqueId().toString());
                ps.setString(3, reason);
                ps.setString(4, "jail");
                ps.setInt(5, 1);
                ps.setLong(6, System.currentTimeMillis());
                ps.setLong(7, time);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    logsId = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, length_total FROM logs_jail WHERE target_id = ? AND active = 1")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    logsId = rs.getInt(1);
                    totalTime = rs.getLong(2);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Timer jail = new Timer();
        JailTimer task = new JailTimer(db, player, time, logsId, totalTime);
        currentlyJailed.put(player.getUniqueId(), task);
        jail.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(1));
    }

    public static void setUnjail(CommandSender sender, Player player, String reason, String type) {
        JailTimer playerJail = currentlyJailed.get(player.getUniqueId());
        currentlyJailed.remove(player.getUniqueId());
        playerJail.cancel();
        playerJail.timeBar.removeViewer(player);

        Component start = Component.empty().color(NamedTextColor.GRAY);
        Component yourself = Component.text("You've", TextColor.fromHexString("#e23857"), TextDecoration.BOLD);
        if(type.equalsIgnoreCase("unjail")) {
            Component playerName = Component.text(player.getName(), TextColor.fromHexString("#e23857"), TextDecoration.BOLD);
            Component senderName = Component.text(sender.getName(), TextColor.fromHexString("#e23857"), TextDecoration.BOLD);
            Component reasonMsg = reason != null ? Component.text("\n Reason").append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(reason)) : Component.empty();

            sender.sendMessage(start.append(yourself).append(Component.text(" successfully released ")).append(playerName)
                    .append(Component.text(" from jail!")).append(reasonMsg));
            player.sendMessage(start.append(yourself).append(Component.text(" been released from jail by ")).append(senderName)
                    .append(Component.text("!")).append(reasonMsg));
            Audience receivers = Bukkit.getServer().filterAudience(audience -> !audience.equals(sender) && !audience.equals(player));
            receivers.sendMessage(start.append(playerName).append(Component.text(" has been released from jail by ")).append(senderName)
                    .append(Component.text("!")).append(reasonMsg));
        } else if(type.equalsIgnoreCase("bribe")) {
            player.sendMessage(start.append(yourself).append(Component.text(" successfully bribed yourself out of jail!")));
        } else if(type.equalsIgnoreCase("release-papers")) {
            player.sendMessage(start.append(yourself).append(Component.text(" successfully used your Release Papers!")));
        } else if(type.equalsIgnoreCase("fake-release-papers")) {
            player.sendMessage(start.append(yourself).append(Component.text(" successfully used your Fake Release Papers!")));
        }
        Location leaveLoc = new Location(Bukkit.getWorld("world_prison"), 0.5, 135, 0.5);
        player.teleportAsync(leaveLoc);
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        UUID senderId = null;
        if(sender instanceof Player playerSender) senderId = playerSender.getUniqueId();
        long currTime = System.currentTimeMillis();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO logs_jail (sender_id, target_id, reason, type, active, time_started) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, senderId != null ? senderId.toString() : null);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, reason);
            ps.setString(4, type);
            ps.setLong(5, 0);
            ps.setLong(6, currTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE logs_jail SET active = ?, time_finished = ?, length_served = ? WHERE id = ?")) {
            ps.setInt(1, 0);
            ps.setLong(2, currTime);
            ps.setLong(3, playerJail.timeServed);
            ps.setInt(4, playerJail.logsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void fakeReleasePapers(Player player) {
        ItemStack fakePapers = Greg.getFakeReleasePapers(plugin, 1);
        PlayerInventory pInv = player.getInventory();
        if(pInv.containsAtLeast(fakePapers, 1)) {
            player.getInventory().removeItem(fakePapers);
        }
    }
    private void releasePapers(CommandSender sender, Player player) {
        ItemStack realPapers = Greg.getReleasePapers(plugin, 1);
        PlayerInventory pInv = player.getInventory();
        if(pInv.containsAtLeast(realPapers, 1)) {
            player.getInventory().removeItem(realPapers);
        } else {
            return;
        }
        setUnjail(sender, player, "release-papers", "release-papers");
    }
    private void extendJail(Player player) {
        JailTimer timer = currentlyJailed.get(player.getUniqueId());
        long minute = TimeUnit.MINUTES.toMillis(1);
        timer.increaseTime(minute);

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE logs_jail SET length_total = length_total + ? WHERE id = ?")) {
            ps.setLong(1, minute);
            ps.setInt(2, timer.logsId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
