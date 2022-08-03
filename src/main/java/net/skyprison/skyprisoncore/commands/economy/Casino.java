package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Casino implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public Casino(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        /*
        args[0] = player
        args[1] = key
        args[2] = price
        args[3] = cooldown

        -= ORDER =-
         end
         basic
         superCool
         diamond
         enchant
         */

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);

        String casinoCooldowns = "";
        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT casino_cooldowns FROM users WHERE user_id = '" + user.getUniqueId() + "'");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                casinoCooldowns = rs.getString(1);
                casinoCooldowns = casinoCooldowns.replace("[", "");
                casinoCooldowns = casinoCooldowns.replace("]", "");
                casinoCooldowns = casinoCooldowns.replace(" ", "");
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String> casinoCools = Arrays.asList(casinoCooldowns.split(","));

        HashMap<String, Integer> getPos = new HashMap<>();
        getPos.put("casino_end", 0);
        getPos.put("casino_basic", 1);
        getPos.put("casino_super", 2);
        getPos.put("casino_diamond", 3);
        getPos.put("casino_enchant", 4);

        if(!casinoCools.isEmpty()) {
            String cooldown = casinoCools.get(getPos.get(args[1]));
            long dateFinish = Long.parseLong(cooldown);
            if(dateFinish < System.currentTimeMillis()) {
                if (user.getBalance() >= Integer.parseInt(args[2])) {
                    plugin.asConsole("money take " + user.getName() + " " + args[2]);
                    plugin.asConsole("crates key give " + user.getName() + " " + args[1] + " 1");
                    long nCooldown = (Long.parseLong(args[3]) * 1000) + System.currentTimeMillis();
                    String sql = "UPDATE users SET casino_cooldowns = ? WHERE user_id = ?";

                    casinoCools.set(getPos.get(args[1]), String.valueOf(nCooldown));
                    List<Object> params = new ArrayList<Object>() {{
                        add(casinoCools);
                        add(user.getUniqueId().toString());
                    }};
                    hook.sqlUpdate(sql, params);
                }
            } else {
                long distance = dateFinish - System.currentTimeMillis();
                int days = (int) Math.floor(distance / (1000.0 * 60.0 * 60.0 * 24.0));
                int hours = (int) Math.floor((distance % (1000.0 * 60.0 * 60.0 * 24.0)) / (1000.0 * 60.0 * 60.0));
                int minutes = (int) Math.floor((distance % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                int seconds = (int) Math.floor((distance % (1000.0 * 60.0)) / 1000.0);
                if(days != 0.0) {
                    user.sendMessage(plugin.colourMessage("&cYou are still on cooldown, please wait " + days + " day " + hours + " hours " + minutes + " min " + seconds + " sec"));
                } else {
                    if(hours != 0.0) {
                        user.sendMessage(plugin.colourMessage("&cYou are still on cooldown, please wait " + hours + " hours " + minutes + " min " + seconds + " sec"));
                    } else {
                        if(minutes != 0.0) {
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
            }
        }
        return true;
    }
}
