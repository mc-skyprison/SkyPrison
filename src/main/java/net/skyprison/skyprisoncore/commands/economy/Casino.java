package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class Casino implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public Casino(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        /*
        args[0] = player
        args[1] = key
        args[2] = price
        args[3] = cooldown
         */

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);

        HashMap<String, Long> casinoCools = new HashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT casino_name, casino_cooldown FROM casino_cooldowns WHERE user_id = ?")) {
            ps.setString(1, user.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                casinoCools.put(rs.getString(1), rs.getLong(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long nCooldown = (Long.parseLong(args[3]) * 1000) + System.currentTimeMillis();
        if(!casinoCools.isEmpty()) {
            if(casinoCools.containsKey(args[1])) {
                long cooldown = casinoCools.get(args[1]);
                if (cooldown < System.currentTimeMillis()) {
                    if (user.getBalance() >= Integer.parseInt(args[2])) {
                        plugin.asConsole("money take " + user.getName() + " " + args[2]);
                        plugin.asConsole("crates key give " + user.getName() + " " + args[1] + " 1");
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE casino_cooldowns SET casino_cooldown = ? WHERE user_id = ? AND casino_name = ?")) {
                            ps.setLong(1, nCooldown);
                            ps.setString(2, user.getUniqueId().toString());
                            ps.setString(3, args[1]);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    long distance = cooldown - System.currentTimeMillis();
                    int days = (int) Math.floor(distance / (1000.0 * 60.0 * 60.0 * 24.0));
                    int hours = (int) Math.floor((distance % (1000.0 * 60.0 * 60.0 * 24.0)) / (1000.0 * 60.0 * 60.0));
                    int minutes = (int) Math.floor((distance % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                    int seconds = (int) Math.floor((distance % (1000.0 * 60.0)) / 1000.0);
                    if (days != 0.0) {
                        user.sendMessage(plugin.colourMessage("&cYou are still on cooldown, please wait " + days + " day " + hours + " hours " + minutes + " min " + seconds + " sec"));
                    } else {
                        if (hours != 0.0) {
                            user.sendMessage(plugin.colourMessage("&cYou are still on cooldown, please wait " + hours + " hours " + minutes + " min " + seconds + " sec"));
                        } else {
                            if (minutes != 0.0) {
                                user.sendMessage(plugin.colourMessage("&cYou are still on cooldown, please wait " + minutes + " min " + seconds + " sec"));
                            } else {
                                user.sendMessage(plugin.colourMessage("&cYou are still on cooldown, please wait " + seconds + " sec"));
                            }
                        }
                    }
                }
            } else {
                if (user.getBalance() >= Integer.parseInt(args[2])) {
                    plugin.asConsole("money take " + user.getName() + " " + args[2]);
                    plugin.asConsole("crates key give " + user.getName() + " " + args[1] + " 1");

                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO casino_cooldowns (user_id, casino_name, casino_cooldown) VALUES (?, ?, ?)")) {
                        ps.setString(1, user.getUniqueId().toString());
                        ps.setString(2, args[1]);
                        ps.setLong(3, nCooldown);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (user.getBalance() >= Integer.parseInt(args[2])) {
                plugin.asConsole("money take " + user.getName() + " " + args[2]);
                plugin.asConsole("crates key give " + user.getName() + " " + args[1] + " 1");

                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO casino_cooldowns (user_id, casino_name, casino_cooldown) VALUES (?, ?, ?)")) {
                    ps.setString(1, user.getUniqueId().toString());
                    ps.setString(2, args[1]);
                    ps.setLong(3, nCooldown);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
