package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.misc.NewsMessages;
import net.skyprison.skyprisoncore.inventories.misc.PlotTeleport;
import net.skyprison.skyprisoncore.inventories.recipes.CustomMain;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.PaperCommandManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class MiscCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    private static FirstJoins firstJoins = null;
    public MiscCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createMiscCommands();
    }
    private record FirstJoins(long date, List<FirstJoin> joins) {}
    private record FirstJoin(UUID uuid, String name, long firstJoin, String date) {}
    private void createMiscCommands() {
        manager.command(manager.commandBuilder("killinfo", "killsinfo", "killstats", "pvpstats")
                .senderType(Player.class)
                .permission("skyprisoncore.command.killinfo")
                .handler(c -> {
                    Player player = c.sender();
                    int deaths = 0;
                    int pKills = 0;
                    int streak = 0;
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT pvp_deaths, pvp_kills, pvp_killstreak FROM users WHERE user_id = ?")) {
                        ps.setString(1, player.getUniqueId().toString());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            deaths = rs.getInt(1);
                            pKills = rs.getInt(2);
                            streak = rs.getInt(3);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    double KDRatio;
                    if(deaths == 0 && pKills == 0) {
                        KDRatio = 0.0;
                    } else if(deaths == 0) {
                        KDRatio = SkyPrisonCore.round(pKills, 2);
                    } else {
                        KDRatio = SkyPrisonCore.round((double) pKills/deaths, 2);
                    }

                    Component killMsg = Component.text("--= PvP Stats =--", NamedTextColor.RED);

                    killMsg = killMsg.append(Component.text("\nPvP Kills: ", NamedTextColor.GRAY).append(Component.text(pKills, NamedTextColor.RED)))
                            .append(Component.text("\nPvP Deaths: ", NamedTextColor.GRAY).append(Component.text(deaths, NamedTextColor.RED)))
                            .append(Component.text("\nKill Streak: ", NamedTextColor.GRAY).append(Component.text(streak, NamedTextColor.RED)))
                            .append(Component.text("\nK/D Ratio: ", NamedTextColor.GRAY).append(Component.text(KDRatio, NamedTextColor.RED)));
                    player.sendMessage(killMsg);
                }));

        manager.command(manager.commandBuilder("firstjointop")
                .permission("skyprisoncore.command.firstjointop")
                .optional("page", integerParser(1), DefaultValue.constant(1))
                .handler(c -> {
                    CommandSender sender = c.sender();
                    int page = c.get("page");
                    sendFirstjoin(sender, page);
                }));

        manager.command(manager.commandBuilder("customrecipes")
                .senderType(Player.class)
                .permission("skyprisoncore.command.customrecipes")
                .handler(c -> {
                    Player player = c.sender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new CustomMain().getInventory()));
                }));

        Command.Builder<Player> ignoreTp = manager.commandBuilder("ignoreteleport", "ignoretp")
                .senderType(Player.class)
                .permission("skyprisoncore.command.ignoretp");
        manager.command(ignoreTp);

        manager.command(ignoreTp.literal("list")
                .handler(c -> {
                    Player player = c.sender();
                    List<UUID> ignoredPlayers = getIgnoredTps(player);
                    if(!ignoredPlayers.isEmpty()) {
                        Component ignoreMsg = Component.text("---=== ", NamedTextColor.AQUA).append(Component.text("Ignoring Teleports", NamedTextColor.RED, TextDecoration.BOLD))
                                .append(Component.text(" ===---", NamedTextColor.AQUA));
                        for(UUID ignoredPlayer : ignoredPlayers) {
                            ignoreMsg = ignoreMsg.append(Component.text("\n- ", NamedTextColor.AQUA).append(Component.text(
                                    Objects.requireNonNullElse(PlayerManager.getPlayerName(ignoredPlayer), "NULL"), NamedTextColor.DARK_AQUA)));
                        }
                        player.sendMessage(ignoreMsg);
                    } else {
                        player.sendMessage(Component.text("You havn't ignored any players!", NamedTextColor.RED));
                    }
                }));

        manager.command(ignoreTp.required("player", stringParser())
                .handler(c -> {
                    Player player = c.sender();
                    String target = c.get("player");
                    if(target.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(Component.text("You can't ignore yourself!", NamedTextColor.RED));
                        return;
                    }
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                        return;
                    }
                    if(PlayerManager.hasPermission(targetId, "skyprisoncore.command.ignoreteleport.bypass")) {
                        player.sendMessage(Component.text("You can't ignore this player!", NamedTextColor.RED));
                        return;
                    }
                    List<UUID> ignoredPlayers = getIgnoredTps(player);

                    boolean isIgnored = ignoredPlayers.contains(targetId);

                    String sql = isIgnored ? "DELETE FROM teleport_ignore WHERE user_id = ? AND ignore_id = ?" : "INSERT INTO teleport_ignore (user_id, ignore_id) VALUES (?, ?)";
                    Component msg = Component.text("Successfully ", NamedTextColor.GREEN).append(Component.text(isIgnored ? "REMOVED" : "ADDED",
                            NamedTextColor.GREEN, TextDecoration.BOLD)).append(Component.text(" " + target, NamedTextColor.GREEN))
                            .append(Component.text((isIgnored ? " from" : " to") + " your teleport ignore list!", NamedTextColor.GREEN));

                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, player.getUniqueId().toString());
                        ps.setString(2, targetId.toString());
                        ps.executeUpdate();
                        player.sendMessage(msg);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }));

        manager.command(manager.commandBuilder("news")
                .senderType(Player.class)
                .permission("skyprisoncore.command.news")
                .handler(c -> {
                    Player player = c.sender();
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.openInventory(new NewsMessages(plugin, db, player.hasPermission("skyprisoncore.command.news.edit"), 1).getInventory()));
                }));

        manager.command(manager.commandBuilder("removeitalics")
                .senderType(Player.class)
                .permission("skyprisoncore.command.removeitalics")
                .handler(c -> {
                    Player player = c.sender();
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(!item.hasDisplayName()) {
                        player.sendMessage(Component.text("This item doesn't have a custom name!", NamedTextColor.RED));
                        return;
                    }
                    Map<TextDecoration, TextDecoration.State> decor = item.getItemMeta().displayName().decorations();
                    if(decor.containsKey(TextDecoration.ITALIC) && decor.get(TextDecoration.ITALIC).equals(TextDecoration.State.FALSE)) {
                        player.sendMessage(Component.text("Italics has already been removed from this item!", NamedTextColor.RED));
                        return;
                    }
                    if (PlayerManager.getBalance(player) < 50000) {
                        player.sendMessage(Component.text("You need $50,000 to use this!", NamedTextColor.RED));
                        return;
                    }
                    Component confirm = Component.text("Click here to confirm italics removal", NamedTextColor.YELLOW, TextDecoration.BOLD)
                            .hoverEvent(HoverEvent.showText(Component.text("Click to confirm italics removal", NamedTextColor.GRAY)))
                            .clickEvent(ClickEvent.callback(audience -> {
                                if (PlayerManager.getBalance(player) < 50000) {
                                    player.sendMessage(Component.text("You need $50,000 to use this!", NamedTextColor.RED));
                                    return;
                                }
                                item.editMeta(meta -> meta.displayName(meta.displayName().decoration(TextDecoration.ITALIC, false)));
                                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + 50000));
                                player.sendMessage(Component.text("Successfully removed italics from item name!", NamedTextColor.GREEN));
                            }));
                    player.sendMessage(confirm);

                }));


        manager.command(manager.commandBuilder("plotteleport", "plot", "plottp")
                .senderType(Player.class)
                .permission("skyprisoncore.command.plotteleport")
                .handler(c -> {
                    Player player = c.sender();
                    if(player.getWorld().getName().equalsIgnoreCase("world_prison")) {
                        player.sendMessage(Component.text("Can't use this command here!", NamedTextColor.RED));
                        return;
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new PlotTeleport(player).getInventory()));
                }));
    }
    private List<UUID> getIgnoredTps(Player player) {
        List<UUID> ignoredPlayers = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT ignore_id FROM teleport_ignore WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ignoredPlayers.add(UUID.fromString(rs.getString(1)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ignoredPlayers;
    }
    private void sendFirstjoin(CommandSender sender, int page) {
        List<FirstJoin> firstJoins = new ArrayList<>();
        boolean refresh = false;
        if(MiscCommands.firstJoins != null) {
            if(MiscCommands.firstJoins.date() < System.currentTimeMillis()) {
                refresh = true;
            }
        } else refresh = true;
        if(refresh) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, current_name, first_join FROM users")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong(3) != 0) {
                        firstJoins.add(new FirstJoin(UUID.fromString(rs.getString(1)), rs.getString(2),
                                rs.getLong(3), ChatUtils.formatDate(rs.getLong(3))));
                    }
                }
                firstJoins.sort(Comparator.comparingLong(o -> o.firstJoin));
                MiscCommands.firstJoins = new FirstJoins(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10), firstJoins);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else firstJoins = new ArrayList<>(MiscCommands.firstJoins.joins());

        int totalPages = (int) Math.ceil(firstJoins.size() / 10.0);
        if(page > totalPages) page = 1;
        Component msg = Component.empty();
        msg = msg.append(Component.text("       ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Firstjoins ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("       ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));


        List<FirstJoin> joinsToShow = new ArrayList<>(firstJoins);

        int toDelete = 10 * (page - 1);
        if (toDelete != 0) {
            joinsToShow = joinsToShow.subList(toDelete, joinsToShow.size());
        }
        int i = 0;

        TextColor pColour = TextColor.fromHexString("#99d3fc");
        TextColor oColour = TextColor.fromHexString("#d4dddd");
        for (FirstJoin firstJoin : joinsToShow) {
            if (i == 10) break;
            boolean isPlayer = sender instanceof Player player && player.getUniqueId().equals(firstJoin.uuid);
            msg = msg.appendNewline().append(Component.text(firstJoins.indexOf(firstJoin) + 1 + ". ", isPlayer ? pColour : NamedTextColor.GRAY, TextDecoration.BOLD))
                    .append(Component.text(firstJoin.name, isPlayer ? pColour : oColour)).append(Component.text(" - ", isPlayer ? pColour : NamedTextColor.GRAY)
                    .append(Component.text(firstJoin.date, isPlayer ? pColour : oColour)));

            i++;
        }
        if(sender instanceof Player player) {
            List<FirstJoin> finalFirstJoins = firstJoins;
            msg = msg.append(firstJoins.stream().filter(firstJoin -> firstJoin.uuid.equals(player.getUniqueId())).findFirst().map(firstJoin ->
                    Component.newline().append(Component.text(finalFirstJoins.indexOf(firstJoin) + 1 + ". ", pColour, TextDecoration.BOLD))
                    .append(Component.text(firstJoin.name, pColour, TextDecoration.BOLD)).append(Component.text(" - ", pColour, TextDecoration.BOLD)
                            .append(Component.text(firstJoin.date, pColour, TextDecoration.BOLD)))).orElse(Component.empty()));
        }
        int nextPage = page + 1;
        int prevPage = page - 1;
        Component pages = Component.text(page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                .append(Component.text(totalPages, TextColor.fromHexString("#266d27"))));
        Component next = Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> { if(sender instanceof Player player) sendFirstjoin(player, nextPage); }));
        Component prev = Component.text("<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> { if(sender instanceof Player player) sendFirstjoin(player, prevPage); }));

        if (page == 1 && page != totalPages) {
            msg = msg.appendNewline().append(pages).append(next);
        } else if (page != 1 && page == totalPages) {
            msg = msg.appendNewline().append(prev).append(pages);
        } else if (page != 1) {
            msg = msg.appendNewline().append(prev).append(pages).append(next);
        }
        sender.sendMessage(msg);
    }
}
