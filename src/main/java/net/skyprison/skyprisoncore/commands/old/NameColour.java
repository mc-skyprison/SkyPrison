package net.skyprison.skyprisoncore.commands.old;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Index;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.Notifications;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class NameColour implements CommandExecutor { // /namecolour <coloured name> (player)
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public NameColour(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Component help = Component.text("");
        help = help.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Name Colouring ", TextColor.fromHexString("#03b09c"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        help = help.appendNewline().append(MiniMessage.miniMessage().deserialize("<#87fdd2>The named colours available are: \n<black>\\<black></black>, <dark_blue>\\<dark_blue></dark_blue>, " +
                "<dark_green>\\<dark_green></dark_green>, <dark_aqua>\\<dark_aqua></dark_aqua>, <dark_red>\\<dark_red></dark_red>, <dark_purple>\\<dark_purple></dark_purple>, " +
                "<gold>\\<gold></gold>, <gray>\\<gray></gray>, <dark_gray>\\<dark_gray></dark_gray>, <blue>\\<blue></blue>, " +
                "<green>\\<green></green>, <aqua>\\<aqua></aqua>, <red>\\<red></red>, <light_purple>\\<light_purple></light_purple>, <yellow>\\<yellow></yellow>, " +
                "<white>\\<white></white>"));

        help = help.append(MiniMessage.miniMessage().deserialize("""
                
                <gray><st>           </st>
                <#87fdd2>Hex colours are available by using <#568ac2><b><hex> \\<#87fdd2></b>"""));
        if (sender.hasPermission("skyprisoncore.command.namecolour.multicolour")) {
            help = help.append(MiniMessage.miniMessage().deserialize("""

                    <gray><st>           </st>
                    <#87fdd2>Gradients are available by using: <#568ac2><b>\\<gradient:colour1:colour2:colour1000></b>
                    
                    <#87fdd2>Example: <#568ac2><b>\\<gradient:red:#87fdd2></b>
                    <red><italic>Note: You can specify as many colours as you want!</italic>
                    
                    <#87fdd2>You can also use <#568ac2><b>\\<rainbow></b></#568ac2> to easily get a rainbow gradient!
                    <gray><st>           </st>
                    <#87fdd2>Example: <#568ac2><b>/namecolour \\<red>Drake\\<#5ee45e>Pork</b>
                    <gray><st>           </st>
                    <red><b>/namecolour (coloured name / remove)"""));
        } else {
            help = help.append(MiniMessage.miniMessage().deserialize("""

                    <#87fdd2>Example: <b>/namecolour <red>\\<red></b>
                    <gray><st>           </st>
                    <red><b>/namecolour (colour / remove)"""));
        }

        if(args.length > 0) {
            String pUUID = "";
            String playerName = "";
            if(sender instanceof Player && args.length < 2) {
                playerName = sender.getName();
                pUUID = ((Player) sender).getUniqueId().toString();
            } else {
                if(args.length > 1) {
                    if(sender.hasPermission("skyprisoncore.command.namecolour.others")) {
                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, current_name FROM users WHERE current_name = ?")) {
                            ps.setString(1, args[1]);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) {
                                pUUID = rs.getString(1);
                                playerName = rs.getString(2);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else if(sender instanceof Player) {
                        playerName = sender.getName();
                        pUUID = ((Player) sender).getUniqueId().toString();
                    }
                }
            }

            if(!playerName.isEmpty() && !pUUID.isEmpty()) {
                if(args[0].equalsIgnoreCase("remove")) {
                    if (sender instanceof Player player && (args.length < 2 || !sender.hasPermission("skyprisoncore.command.namecolour.others"))) {
                        player.customName(null);
                    } else if(sender.hasPermission("skyprisoncore.command.namecolour.others")) {
                        Component colourChange = Component.text("Your name colour was removed by ", TextColor.fromHexString("#87fdd2"))
                                .append(sender.name().decorate(TextDecoration.BOLD)).append(Component.text("!", TextColor.fromHexString("#87fdd2")));
                        Player player = Bukkit.getPlayer(UUID.fromString(pUUID));
                        if(player != null) {
                            player.customName(null);
                            player.sendMessage(colourChange);
                        } else {
                            Notifications.createNotification("namecolour-update", null, pUUID, colourChange, null, true);
                            Notifications.scheduleForOnline(pUUID, "namecolour", "remove");
                        }
                    }
                    sender.sendMessage(Component.text("Successfully removed the name color", TextColor.fromHexString("#87fdd2")));
                    return true;
                }

                if(sender.hasPermission("skyprisoncore.command.namecolour.multicolour")) {
                    Component nickName = plugin.getParsedString(sender, "namecolour", args[0]);
                    if (args[0].contains("<") && args[0].contains(">")) {
                        String plainName = PlainTextComponentSerializer.plainText().serialize(nickName);

                        if (plainName.equalsIgnoreCase(playerName) || (!plainName.equalsIgnoreCase(playerName) && sender.hasPermission("skyprisoncore.command.namecolour.others"))) {
                            if (sender instanceof Player player && (args.length < 2 || !sender.hasPermission("skyprisoncore.command.namecolour.others"))) {
                                player.customName(nickName);
                            } else if(sender.hasPermission("skyprisoncore.command.namecolour.others")) {
                                Component colourChange = Component.text("Your name colour was changed by ", TextColor.fromHexString("#87fdd2"))
                                        .append(sender.name().decorate(TextDecoration.BOLD)).append(Component.text(" to ", TextColor.fromHexString("#87fdd2"))).append(nickName);
                                Player player = Bukkit.getPlayer(UUID.fromString(pUUID));
                                if(player != null) {
                                    player.customName(nickName);
                                    player.sendMessage(colourChange);
                                } else {
                                    Notifications.createNotification("namecolour-update", null, pUUID, colourChange, null, true);
                                    Notifications.scheduleForOnline(pUUID, "namecolour", GsonComponentSerializer.gson().serialize(nickName));
                                }
                            }
                            sender.sendMessage(Component.text("Successfully changed the name color to ", TextColor.fromHexString("#87fdd2")).append(nickName));
                        } else {
                            sender.sendMessage(Component.text("That doesn't match your username!", NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("You havn't added any colours!", NamedTextColor.RED));
                    }
                } else {
                    if (args[0].startsWith("<") && args[0].endsWith(">")) {
                        String colour = args[0].replace("<", "").replace(">", "");
                        Index<String, NamedTextColor> colorIndex = NamedTextColor.NAMES;
                        Component nickName = Component.text(playerName);
                        if (TextColor.fromHexString(colour) != null) {
                            nickName = nickName.color(TextColor.fromHexString(colour));
                        } else if (colorIndex.value(colour) != null) {
                            nickName = nickName.color(colorIndex.value(colour));
                        } else {
                            sender.sendMessage(Component.text("Invalid colour! For available colours, see /namecolour", NamedTextColor.RED));
                            return true;
                        }

                        if (sender instanceof Player player && (args.length < 2 || !sender.hasPermission("skyprisoncore.command.namecolour.others"))) {
                            player.customName(nickName);
                        } else if(sender.hasPermission("skyprisoncore.command.namecolour.others")) {
                            Component colourChange = Component.text("Your name colour was changed by ", TextColor.fromHexString("#87fdd2"))
                                    .append(sender.name().decorate(TextDecoration.BOLD)).append(Component.text(" to ", TextColor.fromHexString("#87fdd2"))).append(nickName);
                            Player player = Bukkit.getPlayer(UUID.fromString(pUUID));
                            if(player != null) {
                                player.customName(nickName);
                                player.sendMessage(colourChange);
                            } else {
                                Notifications.createNotification("namecolour-update", null, pUUID, colourChange, null, true);
                                Notifications.scheduleForOnline(pUUID, "namecolour", GsonComponentSerializer.gson().serialize(nickName));
                            }
                        }
                        sender.sendMessage(Component.text("Successfully changed the name color to ", TextColor.fromHexString("#87fdd2")).append(nickName));
                    } else {
                        sender.sendMessage(Component.text("Incorrect Usage! /namecolour <colour>", NamedTextColor.RED));
                    }
                }
            } else {
                sender.sendMessage(Component.text("That player doesn't exist!", NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(help);
        }
        return true;
    }
}
