package net.skyprison.skyprisoncore.commands.old;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

public class Claim implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public Claim(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        case "blocks" -> { // claim blocks buy/give/set/take <player> <amount>
            if(args.length > 1) {
                switch(args[1].toLowerCase()) {
                    case "buy" -> {
                        if(plugin.isInt(args[2])) {
                            long blocks = Integer.parseInt(args[2]);
                            double price = 40 * blocks;
                            if(user.getBalance() >= price) {
                                user.withdraw(price);

                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                                    ps.setLong(1, blocks);
                                    ps.setString(2, user.getUniqueId().toString());
                                    ps.executeUpdate();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                player.sendMessage(prefix.append(Component.text("Successfully bought ", TextColor.fromHexString("#20df80"))
                                        .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                        .append(Component.text(" blocks for $", TextColor.fromHexString("#20df80")))
                                        .append(Component.text(ChatUtils.formatNumber(price), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                            } else {
                                long needed = (long) (price - user.getBalance());
                                player.sendMessage(prefix.append(Component.text("You don't have enough money! You need $" + ChatUtils.formatNumber(needed) + " more..", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                        }
                    }
                    case "give" -> {
                        if(hasPerm(player)) {
                            CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                            if (tUser != null) {
                                if (plugin.isInt(args[3])) {
                                    long blocks = Integer.parseInt(args[3]);

                                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                                        ps.setLong(1, blocks);
                                        ps.setString(2, tUser.getUniqueId().toString());
                                        ps.executeUpdate();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    player.sendMessage(prefix.append(Component.text("Successfully gave ", TextColor.fromHexString("#20df80"))
                                            .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                            .append(Component.text(" blocks to ", TextColor.fromHexString("#20df80")))
                                            .append(Component.text(tUser.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));

                                    Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                            .append(Component.text(" has given you ", TextColor.fromHexString("#20df80")))
                                            .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                            .append(Component.text(" blocks!", TextColor.fromHexString("#20df80")));

                                    if(tUser.isOnline()) {
                                        tUser.getPlayer().sendMessage(msg);
                                    } else {
                                        NotificationsUtils.createNotification("claim-give", null, tUser.getUniqueId().toString(), msg, null, true);
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks give <player> <amount>", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                        }
                    }
                    case "set" -> { // /claim blocks set <player> <amount>
                        if(hasPerm(player)) {
                            CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                            if(tUser != null) {
                                if (plugin.isInt(args[3])) {
                                    long blocks = Integer.parseInt(args[3]);

                                    HashMap<String, Long> tBlocksData = getPlayerBlocks(tUser.getOfflinePlayer());
                                    long tBlocks = tBlocksData.get("used");
                                    if(tBlocks >= 0) {
                                        if(blocks - tBlocks >= 0) {
                                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = ? WHERE user_id = ?")) {
                                                ps.setLong(1, blocks);
                                                ps.setString(2, tUser.getUniqueId().toString());
                                                ps.executeUpdate();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }

                                            player.sendMessage(prefix.append(Component.text("Successfully set ", TextColor.fromHexString("#20df80"))
                                                            .append(Component.text(tUser.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)))
                                                    .append(Component.text(" blocks to ", TextColor.fromHexString("#20df80")))
                                                    .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));

                                            Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                    .append(Component.text(" set your blocks to ", TextColor.fromHexString("#20df80")))
                                                    .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                    .append(Component.text("!", TextColor.fromHexString("#20df80")));
                                            if (tUser.isOnline()) {
                                                tUser.getPlayer().sendMessage(msg);
                                            } else {
                                                NotificationsUtils.createNotification("claim-set", null, tUser.getUniqueId().toString(), msg, null, true);
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("This would put the player's total blocks below their used blocks!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Couldn't get that player's claim blocks!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks take <plyer> <amount>", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                        }
                    }
                    case "take" -> {
                        if(hasPerm(player)) {
                            CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                            if(tUser != null) {
                                if (plugin.isInt(args[3])) {
                                    long blocks = Integer.parseInt(args[3]);

                                    long tLeft = hasNeededBlocks(tUser.getOfflinePlayer(), blocks);
                                    if(tLeft >= 0) {
                                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks - ? WHERE user_id = ?")) {
                                            ps.setLong(1, blocks);
                                            ps.setString(2, tUser.getUniqueId().toString());
                                            ps.executeUpdate();
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }

                                        player.sendMessage(prefix.append(Component.text("Successfully took ", TextColor.fromHexString("#20df80"))
                                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                .append(Component.text(" blocks from ", TextColor.fromHexString("#20df80")))
                                                .append(Component.text(tUser.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));

                                        Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                .append(Component.text(" took ", TextColor.fromHexString("#20df80")))
                                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                .append(Component.text(" blocks from you!", TextColor.fromHexString("#20df80")));

                                        if(tUser.isOnline()) {
                                            tUser.getPlayer().sendMessage(msg);
                                        } else {
                                            NotificationsUtils.createNotification("claim-take", null, tUser.getUniqueId().toString(), msg, null, true);
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Player doesn't have enough claim blocks!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks take <plyer> <amount>", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                        }
                    }
                    default -> player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                }
            } else {
                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
            }
        }
        default -> {
            if (args.length > 1) { // /claim accept invite <id>
                if(args.length == 3) {
                    String claimId = NotificationsUtils.hasNotification(args[2], player);
                    if(!claimId.isEmpty()) {
                        boolean state = args[0].equalsIgnoreCase("accept");
                        switch (args[1].toLowerCase()) {
                            case "invite" -> {
                                if (state) {
                                    inviteAccept(player, claimId, args[2]);
                                } else {
                                    inviteDecline(player, claimId, args[2]);
                                }
                                return true;
                            }
                            case "transfer" -> {
                                if (state) {
                                    transferAccept(player, claimId, args[2]);
                                } else {
                                    transferDecline(player, claimId, args[2]);
                                }
                                return true;
                            }
                        }
                    }
                }
                if (plugin.isInt(args[1])) {
                    helpMessage(player, Integer.parseInt(args[1]));
                } else {
                    helpMessage(player, 1);
                }
            } else {
                helpMessage(player, 1);
            }
        }
        return true;
    }
}
