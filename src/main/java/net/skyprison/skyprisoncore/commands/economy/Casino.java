package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Casino implements CommandExecutor {
    private final SkyPrisonCore plugin;

    @Inject
    public Casino(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        /*
        args[0] = player
        args[1] = key
        args[2] = price
        args[3] = cooldown
         */

        File f = new File(plugin.getDataFolder() + File.separator + "casinocooldown.yml");
        FileConfiguration fData = YamlConfiguration.loadConfiguration(f);

        CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);
        if(fData.isConfigurationSection(user.getUniqueId().toString())) {
            if(fData.getString(user.getUniqueId() + "." + args[1]) != null && !fData.getString(user.getUniqueId() + "." + args[1]).isEmpty()) {
                String cooldown = fData.getString(user.getUniqueId() + "." + args[1]);
                long dateFinish = Long.parseLong(cooldown);
                if(dateFinish < System.currentTimeMillis()) {
                    if (user.getBalance() >= Integer.parseInt(args[2])) {
                        plugin.asConsole("money take " + user.getName() + " " + args[2]);
                        plugin.asConsole("crates givekey " + args[0] + " " + args[1] + " 1");
                        long nCooldown = (Long.parseLong(args[3]) * 1000) + System.currentTimeMillis();
                        fData.set(user.getUniqueId() + "." + args[1], nCooldown);
                    }
                } else {
                    long distance = dateFinish - System.currentTimeMillis();
                    int days = (int) Math.floor(distance / (1000 * 60 * 60 * 24));
                    int hours = (int) Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                    int minutes = (int) Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
                    int seconds = (int) Math.floor((distance % (1000 * 60)) / 1000);
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
                    plugin.asConsole("crates givekey " + args[0] + " " + args[1] + " 1");
                    long cooldown = (Long.parseLong(args[3]) * 1000) + System.currentTimeMillis();
                    fData.set(user.getUniqueId() + "." + args[1], cooldown);
                }
            }
        } else {
            if (user.getBalance() >= Integer.parseInt(args[2])) {
                plugin.asConsole("money take " + user.getName() + " " + args[2]);
                plugin.asConsole("crates givekey " + args[0] + " " + args[1] + " 1");
                long cooldown = (Long.parseLong(args[3]) * 1000) + System.currentTimeMillis();
                fData.set(user.getUniqueId() + "." + args[1], cooldown);
            }
        }
        try {
            fData.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }
}
