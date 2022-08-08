package net.skyprison.skyprisoncore.commands.discord;


import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
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


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                player.sendMessage(plugin.colourMessage("&3Join our discord at &9&lhttp://discord.gg/T9DwRcPpgj&3!"));
            } else if(args.length == 1) {
                if (args[0].equalsIgnoreCase("link")) {
                    try {
                        Connection conn = db.getSQLConnection();
                        PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                            Random rand = new Random();

                            int newCode = 0;

                            boolean foundNumb = false;
                            while (!foundNumb) {
                                newCode = rand.nextInt(99999) + 10000;

                                if (!plugin.discordLinking.containsKey(newCode))
                                    foundNumb = true;
                            }

                            player.sendMessage(plugin.colourMessage("&bTo link your discord account, send this code to the SkyPrison Bot: &l" + newCode));
                            plugin.discordLinking.put(newCode, player.getUniqueId());
                        } else {
                            player.sendMessage(plugin.colourMessage("&cYou've already linked your account!"));
                        }
                        db.close(ps, rs, conn);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    player.sendMessage(plugin.colourMessage("&3Join our discord at &9&lhttp://discord.gg/T9DwRcPpgj&3!"));
                }
            } else {
                if (player.hasPermission("skyprisoncore.command.discord.broadcast")) {
                    if (args.length > 2) {
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
                    }
                } else {
                    player.sendMessage(plugin.colourMessage("&cYou do not have access to this!"));
                }
            }
        } else {
            if(args.length > 2) {
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
            }
        }
        return true;
    }
}
