package net.skyprison.skyprisoncore.commands.discord;


import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class Discord implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final DiscordApi discApi;

    public Discord(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
    }

    private void onUnlink(Audience audience) {
        if(audience instanceof Player player) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET discord_id = ? WHERE user_id = ?")) {
                ps.setInt(1, 0);
                ps.setString(2, player.getUniqueId().toString());
                ps.executeUpdate();
                player.sendMessage(Component.text("Successfully unlinked your Minecraft and Discord accounts").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player player) {
            Component discordLink = Component.text("Join our discord at ").color(NamedTextColor.DARK_AQUA).appendNewline().append(Component.text("https://skyprison.net/discord").color(NamedTextColor.DARK_BLUE).decorate(TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false);

            if(args.length == 0) {
                player.sendMessage(discordLink);
            } else if(args.length == 1) {
                long discordId = 0;
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) {
                        discordId = rs.getLong(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (args[0].equalsIgnoreCase("link")) {
                    if(discordId == 0) {
                        Random rand = new Random();

                        int newCode = 0;

                        boolean foundNumb = false;
                        while (!foundNumb) {
                            newCode = rand.nextInt(99999) + 10000;

                            if (!plugin.discordLinking.containsKey(newCode))
                                foundNumb = true;
                        }
                        ClickCallback.Options options = ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(1))
                                .uses(1)
                                .build();
                        ClickEvent clickEvent = ClickEvent.callback(
                                this::onUnlink,
                                options
                        );
                        player.sendMessage(Component.text("To link your discord account, use /link on our discord server and type this code into the field: " + newCode).color(NamedTextColor.AQUA).clickEvent(clickEvent).decoration(TextDecoration.ITALIC, false));
                        plugin.discordLinking.put(newCode, player.getUniqueId());
                    } else {
                        player.sendMessage(Component.text("You've already linked your account!").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    }
                } else if (args[0].equalsIgnoreCase("unlink")) {
                    if(discordId != 0) {
                        ClickCallback.Options options = ClickCallback.Options.builder()
                                .lifetime(Duration.ofMinutes(1))
                                .uses(1)
                                .build();
                        ClickEvent clickEvent = ClickEvent.callback(
                                this::onUnlink,
                                options
                        );
                        player.sendMessage(Component.text("Click here to confirm you wish to unlink your discord account.").color(NamedTextColor.AQUA).clickEvent(clickEvent).decoration(TextDecoration.ITALIC, false));
                    } else {
                        player.sendMessage(Component.text("Your account isn't linked to any discord account!").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    }
                } else {
                    player.sendMessage(discordLink);
                }
            } else {
                if (player.hasPermission("skyprisoncore.command.discord.broadcast")) {
                    // /discord broadcast <channel id> <message>
                    if (args[0].equalsIgnoreCase("broadcast")) {
                        long discordId = Long.parseLong(args[1]);

                        ArrayList<String> orgMsg = new ArrayList<>(Arrays.asList(args));

                        orgMsg.remove(0);
                        orgMsg.remove(0);

                        StringBuilder cMessage = new StringBuilder();
                        for (String arg : orgMsg) {
                            cMessage.append(arg).append(" ");
                        }
                        Optional<TextChannel> optChannel = discApi.getTextChannelById(discordId);
                        if(optChannel.isPresent()) {
                            TextChannel channel = optChannel.get();
                            channel.sendMessage(cMessage.toString());
                        }
                    }
                } else {
                    player.sendMessage(Component.text("You don't have access to this command!").color(NamedTextColor.RED));
                }
            }
        } else {
            if(args.length > 2) {
                if (args[0].equalsIgnoreCase("broadcast")) {
                    // /discord broadcast <channel id> <message>
                    long discordId = Long.parseLong(args[1]);

                    ArrayList<String> orgMsg = new ArrayList<>(Arrays.asList(args));

                    orgMsg.remove(0);
                    orgMsg.remove(0);

                    StringBuilder cMessage = new StringBuilder();
                    for (String arg : orgMsg) {
                        cMessage.append(arg).append(" ");
                    }
                    Optional<TextChannel> optChannel = discApi.getTextChannelById(discordId);
                    if(optChannel.isPresent()) {
                        TextChannel channel = optChannel.get();
                        channel.sendMessage(cMessage.toString());
                    }
                }
            }
        }
        return true;
    }
}
