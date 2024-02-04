package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.recipes.CustomMain;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
        manager.command(manager.commandBuilder("firstjointop")
                .permission("skyprisoncore.command.firstjointop")
                .argument(IntegerArgument.<CommandSender>builder("page").withMin(1).asOptionalWithDefault(1))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    int page = c.get("page");
                    sendFirstjoin(sender, page);
                }));
        manager.command(manager.commandBuilder("customrecipes")
                .senderType(Player.class)
                .permission("skyprisoncore.command.customrecipes")
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new CustomMain().getInventory()));
                }));

        Command.Builder<CommandSender> ignoreTp = manager.commandBuilder("ignoreteleport", "ignoretp")
                .senderType(Player.class)
                .permission("skyprisoncore.command.ignoretp");
        manager.command(ignoreTp);

        manager.command(ignoreTp.literal("list")
                .handler(c -> {
                    Player player = (Player) c.getSender();
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

        manager.command(ignoreTp.argument(StringArgument.of("player"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
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
