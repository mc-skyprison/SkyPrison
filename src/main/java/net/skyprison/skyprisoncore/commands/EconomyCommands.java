package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.Zrips.CMI.CMI;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.economy.BountiesList;
import net.skyprison.skyprisoncore.inventories.economy.BuyBack;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.skyprison.skyprisoncore.SkyPrisonCore.bountyCooldown;

public class EconomyCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    private final Component bountyPrefix = Component.text("Bounties", NamedTextColor.RED).append(Component.text(" | ", NamedTextColor.WHITE));

    public EconomyCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createBountyCommands();
        createMiscCommands();
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    private void createMiscCommands() {
        manager.command(manager.commandBuilder("buyback")
                .senderType(Player.class)
                .permission("skyprisoncore.command.buyback")
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new BuyBack(plugin, db, player).getInventory()));
                }));
    }
    private void createBountyCommands() {
        Command.Builder<CommandSender> bounty = manager.commandBuilder("bounty")
                .permission("skyprisoncore.command.bounty")
                .handler(c -> c.getSender().sendMessage(getBountyHelp()));

        manager.command(bounty);

        manager.command(bounty.literal("set")
                .senderType(Player.class)
                .argument(StringArgument.of("player"))
                .argument(DoubleArgument.<CommandSender>builder("amount").withMin(100).asRequired().build())
                .handler(c -> {
                    String bountyTarget = c.get("player");
                    UUID bountyTargetId = PlayerManager.getPlayerId(bountyTarget);
                    if(bountyTargetId == null) {
                        c.getSender().sendMessage(Component.text("Player doesn't exist!", NamedTextColor.RED));
                        return;
                    }
                    Player player = (Player) c.getSender();

                    if(player.getUniqueId().equals(bountyTargetId)) {
                        player.sendMessage(Component.text("You can't put a bounty on yourself!", NamedTextColor.RED));
                        return;
                    }

                    if(!player.hasPermission("skyprisoncore.command.bounty.bypass") &&
                            bountyCooldown.containsKey(player.getUniqueId()) && bountyCooldown.get(player.getUniqueId()) > System.currentTimeMillis()) {
                        long timeTill = bountyCooldown.get(player.getUniqueId()) - System.currentTimeMillis();
                        int minutes = (int) Math.floor((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                        int seconds = (int) Math.floor((timeTill % (1000.0 * 60.0)) / 1000.0);
                        player.sendMessage(Component.text("You can't put another bounty yet! Available in: " + minutes + "m " + seconds + "s", NamedTextColor.RED));
                        return;
                    }

                    boolean hasBypass = false;

                    Player isOnline = Bukkit.getPlayer(bountyTargetId);
                    if(isOnline != null) {
                        if(isOnline.hasPermission("skyprisoncore.command.bounty.bypass")) {
                            hasBypass = true;
                        }
                    } else {
                        LuckPerms luckAPI = LuckPermsProvider.get();
                        UserManager userManager = luckAPI.getUserManager();
                        CompletableFuture<User> userFuture = userManager.loadUser(bountyTargetId);
                        try {
                            hasBypass = userFuture.thenApplyAsync(user -> {
                                CachedPermissionData permissionData = user.getCachedData().getPermissionData();
                                return permissionData.checkPermission("skyprisoncore.command.bounty.bypass").asBoolean();
                            }).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if(hasBypass) {
                        c.getSender().sendMessage(Component.text("You can't put a bounty on this player!", NamedTextColor.RED));
                        return;
                    }

                    double prize = c.get("amount");

                    double bountyPrize = round(prize, 2);
                    if (CMI.getInstance().getPlayerManager().getUser(player).getBalance() < bountyPrize) {
                        player.sendMessage(Component.text("You do not have enough money..", NamedTextColor.RED));
                        return;
                    }


                    String bountiedBy = "";
                    boolean hasBounty = false;
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT bountied_by FROM bounties WHERE user_id = ?")) {
                        ps.setString(1, bountyTargetId.toString());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            hasBounty = true;
                            bountiedBy = rs.getString(1);
                            bountiedBy = bountiedBy.replace("[", "");
                            bountiedBy = bountiedBy.replace("]", "");
                            bountiedBy = bountiedBy.replace(" ", "");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Component bountyMsg = bountyPrefix;
                    Component targetMsg = bountyPrefix;

                    String targetName = PlayerManager.getPlayerName(bountyTargetId);

                    if (hasBounty) {
                        bountyMsg = bountyMsg.append(Component.text(player.getName() + " has increased the bounty on " + targetName + " by ", NamedTextColor.YELLOW)
                                .append(Component.text("$" + plugin.formatNumber(bountyPrize) + "!", NamedTextColor.GREEN)));

                        targetMsg = targetMsg.append(Component.text(player.getName() + " has increased the bounty on you by ", NamedTextColor.YELLOW)
                                .append(Component.text("$" + plugin.formatNumber(bountyPrize) + "!", NamedTextColor.GREEN)));

                        bountiedBy += "," + player.getUniqueId();

                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE bounties SET prize = prize + ?, bountied_by = ? WHERE user_id = ?")) {
                            ps.setDouble(1, bountyPrize);
                            ps.setString(2, bountiedBy);
                            ps.setString(3, bountyTargetId.toString());
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bountyMsg = bountyMsg.append(Component.text(player.getName() + " has put a ", NamedTextColor.YELLOW)
                                .append(Component.text("$" + plugin.formatNumber(bountyPrize), NamedTextColor.GREEN))
                                .append(Component.text(" bounty on " + targetName + "!", NamedTextColor.YELLOW)));

                        targetMsg = targetMsg.append(Component.text(player.getName() + " has put a ", NamedTextColor.YELLOW)
                                .append(Component.text("$" + plugin.formatNumber(bountyPrize), NamedTextColor.GREEN))
                                .append(Component.text(" bounty on you!", NamedTextColor.YELLOW)));

                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO bounties (user_id, prize, bountied_by) VALUES (?, ?, ?)")) {
                            ps.setString(1, bountyTargetId.toString());
                            ps.setDouble(2, bountyPrize);
                            ps.setString(3, player.getUniqueId().toString());
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                            "cmi money take " + player.getName() + " " + bountyPrize));
                    bountyCooldown.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
                    Audience receivers = Bukkit.getServer().filterAudience(audience -> {
                        if(audience instanceof Player onlinePlayer) {
                            return !onlinePlayer.hasPermission("skyprisoncore.command.bounty.silent") && !onlinePlayer.getUniqueId().equals(bountyTargetId);
                        }
                        return true;
                    });
                    receivers.sendMessage(bountyMsg);

                    if (isOnline != null) {
                        isOnline.sendMessage(targetMsg);
                    } else {
                        NotificationsUtils.createNotification("bountied", null, String.valueOf(bountyTargetId), targetMsg, null, true);
                    }
                }));

        manager.command(bounty.literal("help")
                .handler(c -> c.getSender().sendMessage(getBountyHelp())));

        manager.command(bounty.literal("list")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new BountiesList(plugin, db).getInventory()));
                }));

        manager.command(bounty.literal("mute")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    if(!player.hasPermission("skyprisoncore.command.bounty.silent")) {
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                "lp user " + player.getName() + " permission set skyprisoncore.command.bounty.silent true"));
                        player.sendMessage(bountyPrefix.append(Component.text("Bounty messages muted!", NamedTextColor.YELLOW)));
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                "lp user " + player.getName() + " permission set skyprisoncore.command.bounty.silent false"));
                        player.sendMessage(bountyPrefix.append(Component.text("Bounty messages unmuted!", NamedTextColor.YELLOW)));
                    }
                }));
    }
    private Component getBountyHelp() {
        return Component.textOfChildren(Component.text("----==== ", NamedTextColor.WHITE)
                        .append(Component.text("Bounties", NamedTextColor.RED))
                        .append(Component.text("====----", NamedTextColor.WHITE)))
                .append(Component.text("\n/bounty set <player> <amount>", NamedTextColor.YELLOW)
                        .append(Component.text(" - Set a bounty on a player", NamedTextColor.WHITE)))
                .append(Component.text("\n/bounty help", NamedTextColor.YELLOW)
                        .append(Component.text(" - Shows this", NamedTextColor.WHITE)))
                .append(Component.text("\n/bounty list", NamedTextColor.YELLOW)
                        .append(Component.text(" - Shows all players with bountiesr", NamedTextColor.WHITE)))
                .append(Component.text("\n/bounty mute", NamedTextColor.YELLOW)
                        .append(Component.text(" - Mutes/Unmutes bounty messages except for bounties towards yourself", NamedTextColor.WHITE)));
    }
}
