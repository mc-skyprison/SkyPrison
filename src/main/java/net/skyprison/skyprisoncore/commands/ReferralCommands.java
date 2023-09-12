package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.Referral;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
                                                    NotificationsUtils.createNotification("referred", player.getName(), reffedPlayer.getUniqueId().toString(), beenReffed, null, true);
                                                }
                                                player.sendMessage(Component.text("You sucessfully referred ", NamedTextColor.DARK_AQUA)
                                                        .append(Component.text(reffedPlayer.getName(), NamedTextColor.AQUA)).append(Component.text(" and have received ", NamedTextColor.DARK_AQUA))
                                                        .append(Component.text("50", NamedTextColor.GOLD)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA)));
                                                plugin.tokens.addTokens(reffedPlayer.getUniqueId(), 250, "Referred Someone", player.getName());
                                                plugin.tokens.addTokens(player.getUniqueId(), 50, "Was Referred", reffedPlayer.getName());
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
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new Referral(plugin, db, player).getInventory()));
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
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new Referral(plugin, db, player).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("Specified player doesn't exist!"));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
    }
}
