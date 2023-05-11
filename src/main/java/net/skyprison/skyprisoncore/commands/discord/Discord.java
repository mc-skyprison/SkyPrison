package net.skyprison.skyprisoncore.commands.discord;


import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        if(audience instanceof Player) {
            Player player = (Player) audience;
            String sql = "UPDATE users SET discord_id = ? WHERE user_id = ?";
            List<Object> params = new ArrayList<>() {{
                add(0);
                add(player.getUniqueId().toString());
            }};

            db.sqlUpdate(sql, params);
            player.sendMessage(plugin.colourMessage("&aSuccessfully unlinked your Minecraft and Discord accounts!"));
        }
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                player.sendMessage(plugin.colourMessage("&3Join our discord at &9&lhttp://discord.gg/T9DwRcPpgj&3!"));
            } else if(args.length == 1) {
                long discordId = 0;
                try {
                    Connection conn = db.getSQLConnection();
                    PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) {
                        discordId = rs.getLong(1);
                    }
                    db.close(ps, rs, conn);
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
                        String msg = plugin.colourMessage("&bTo link your discord account,  &l" + newCode);
                        player.sendMessage(Component.text(msg).clickEvent(clickEvent));
                        plugin.discordLinking.put(newCode, player.getUniqueId());
                    } else {
                        player.sendMessage(plugin.colourMessage("&cYou've already linked your account!"));
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
                        String msg = plugin.colourMessage("&bClick here to confirm you wish to unlink your discord account.");
                        player.sendMessage(Component.text(msg).clickEvent(clickEvent));
                    } else {
                        player.sendMessage(plugin.colourMessage("&cYour account isn't linked to any discord account!"));
                    }
                } else {
                    player.sendMessage(plugin.colourMessage("&3Join our discord at &9&lhttp://discord.gg/T9DwRcPpgj&3!"));
                }
            } else if(args.length == 2) {
                if (player.hasPermission("skyprisoncore.command.discord.admin")) {
/*                    db.deleteUser(args[1]);
                    player.sendMessage("success???");*/
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
                        TextChannel channel = discApi.getTextChannelById(discordId).get();
                        channel.sendMessage(cMessage.toString());
                    }
                } else {
                    player.sendMessage(plugin.colourMessage("&cYou do not have access to this!"));
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
                    TextChannel channel = discApi.getTextChannelById(discordId).get();
                    channel.sendMessage(cMessage.toString());
                }
            }
        }
        return true;
    }
}
