package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.misc.Referral;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.TokenUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class ReferralCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    public ReferralCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createReferralCommands();
    }
    private void createReferralCommands() {
        Command.Builder<Player> referral = manager.commandBuilder("referral", "ref", "refer")
                .senderType(Player.class)
                .permission("skyprisoncore.command.referral")
                .handler(c -> c.sender().sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN)));

        manager.command(referral);

        manager.command(referral
                .required("player", stringParser())
                .handler(c -> {
                    Player player = c.sender();
                    String targetName = c.get("player");
                    UUID tUUID = PlayerManager.getPlayerId(targetName);
                    if(tUUID == null) {
                        player.sendMessage(Component.text("Specified player doesn't exist!", NamedTextColor.RED));
                        return;
                    }

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
                    if (hasReferred) {
                        player.sendMessage(Component.text("You have already referred someone!", NamedTextColor.RED));
                        return;
                    }

                    long playtime = TimeUnit.MILLISECONDS.toHours(PlayerManager.getPlaytime(player));
                    if (playtime < 1 || playtime > 24) { // Checks that the player has played more than an hour on the server but less than 24 hours.
                        if (playtime < 1) {
                            player.sendMessage(Component.text("You need to play 1 hour to be able to refer someone!", NamedTextColor.RED));
                        } else {
                            player.sendMessage(Component.text("You have played too long to refer anyone!", NamedTextColor.RED));
                        }
                        return;
                    }

                    if (PlayerManager.getLastIp(player).equalsIgnoreCase(PlayerManager.getLastIp(tUUID))) {
                        player.sendMessage(Component.text("You can't refer this player! (Same IP)", NamedTextColor.RED));
                        return;
                    }

                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO referrals (user_id, referred_by, refer_date) VALUES (?, ?, ?)")) {
                        ps.setString(1, tUUID.toString());
                        ps.setString(2, player.getUniqueId().toString());
                        ps.setLong(3, System.currentTimeMillis());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    Component beenReffed = Component.text(player.getName(), NamedTextColor.AQUA)
                            .append(Component.text(" has referred you! You have received ", NamedTextColor.DARK_AQUA))
                            .append(Component.text("250", NamedTextColor.YELLOW)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA));

                    PlayerManager.sendMessage(tUUID, beenReffed, "referred", player.getName());

                    player.sendMessage(Component.text("You sucessfully referred ", NamedTextColor.DARK_AQUA)
                            .append(Component.text(targetName, NamedTextColor.AQUA))
                            .append(Component.text(" and have received ", NamedTextColor.DARK_AQUA))
                            .append(Component.text("50", NamedTextColor.GOLD)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA)));
                    TokenUtils.addTokens(tUUID, 250, "Referred Someone", player.getName());
                    TokenUtils.addTokens(player.getUniqueId(), 50, "Was Referred", targetName);
                }));

        manager.command(referral.literal("help")
                .handler(c -> c.sender().sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN))));

        manager.command(referral.literal("history", "list")
                .handler(c -> {
                    Player player = c.sender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new Referral(plugin, db, player.getUniqueId()).getInventory()));
                }));

        manager.command(referral.literal("history", "list")
                .permission("skyprisoncore.command.referral.history.others")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = c.sender();
                    String targetName = c.get("player");
                    UUID tUUID = PlayerManager.getPlayerId(targetName);
                    if (tUUID == null) {
                        player.sendMessage(Component.text("Specified player doesn't exist!"));
                        return;
                    }
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new Referral(plugin, db, tUUID).getInventory()));
                }));
    }
}
