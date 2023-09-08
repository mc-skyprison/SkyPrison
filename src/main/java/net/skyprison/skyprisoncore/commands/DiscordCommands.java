package net.skyprison.skyprisoncore.commands;


import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public class DiscordCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final DiscordApi discApi;
    private final PaperCommandManager<CommandSender> manager;

    public DiscordCommands(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
        this.manager = manager;
        createDiscordCommands();
    }

    private void createDiscordCommands() {
        cloud.commandframework.Command.Builder<CommandSender> discord = this.manager.commandBuilder("discord")
                .permission("skyprisoncore.command.discord")
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Component discordLink = Component.text("Join our discord at ", NamedTextColor.AQUA).appendNewline()
                            .append(Component.text("https://skyprison.net/discord", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));

                    player.sendMessage(discordLink);
                });

        manager.command(discord);

        manager.command(discord.literal("link")
                .senderType(Player.class)
                .permission("skyprisoncore.command.discord.link")
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    long discordId = PlayerManager.getPlayerDiscord(player.getUniqueId());

                    if(discordId == 0) {
                        if(!plugin.discordLinking.containsValue(player.getUniqueId())) {
                            Random rand = new Random();

                            int newCode = 0;

                            boolean foundNumb = false;
                            while (!foundNumb) {
                                newCode = rand.nextInt(99999) + 10000;

                                if (!plugin.discordLinking.containsKey(newCode))
                                    foundNumb = true;
                            }

                            player.sendMessage(Component.text("To link your discord account, use /link on our discord server and type this code into the field: " + newCode, NamedTextColor.AQUA)
                                    .clickEvent(ClickEvent.copyToClipboard(String.valueOf(newCode))));
                            plugin.discordLinking.put(newCode, player.getUniqueId());
                        } else {
                            player.sendMessage(Component.text("You're already trying to link!", NamedTextColor.RED));
                        }
                    } else {
                        player.sendMessage(Component.text("You've already linked your account!", NamedTextColor.RED));
                    }
                }));

        manager.command(discord.literal("unlink")
                .senderType(Player.class)
                .permission("skyprisoncore.command.discord.unlink")
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    long discordId = PlayerManager.getPlayerDiscord(player.getUniqueId());

                    if(discordId != 0) {
                        player.sendMessage(Component.text("Click here to confirm you wish to unlink your discord account.", NamedTextColor.AQUA)
                                .clickEvent(ClickEvent.callback(this::onUnlink, ClickCallback.Options.builder().lifetime(Duration.ofSeconds(30)).build())));
                    } else {
                        player.sendMessage(Component.text("Your account isn't linked to any discord account!", NamedTextColor.RED));
                    }
                }));

        manager.command(discord.literal("broadcast")
                .permission("skyprisoncore.command.discord.broadcast")
                .argument(LongArgument.of("channel_id"))
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    long channelId = c.get("channel_id");
                    String message = c.get("message");
                    Optional<TextChannel> optChannel = discApi.getTextChannelById(channelId);
                    if(optChannel.isPresent()) {
                        TextChannel channel = optChannel.get();
                        channel.sendMessage(message);
                    }
                }));
    }

    private void onUnlink(Audience audience) {
        if(audience instanceof Player player) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET discord_id = ? WHERE user_id = ?")) {
                ps.setInt(1, 0);
                ps.setString(2, player.getUniqueId().toString());
                ps.executeUpdate();
                player.sendMessage(Component.text("Successfully unlinked your Minecraft and Discord accounts!", NamedTextColor.GREEN));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
