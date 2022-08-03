package net.skyprison.skyprisoncore.commands.discord;


import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class Discord implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public Discord(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
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
            }
        }
        return true;
    }
}
