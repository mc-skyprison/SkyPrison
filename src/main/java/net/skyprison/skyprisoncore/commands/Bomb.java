package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.apache.commons.lang.WordUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Bomb implements CommandExecutor {
    private final SkyPrisonCore plugin;
    public Bomb(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }




    public static ItemStack getBomb(String bombName, int amount) {
        SkyPrisonCore plugin = JavaPlugin.getPlugin(SkyPrisonCore.class);
        if(bombName.contains("_")) {
            bombName = bombName.split("_")[0];
        }

        bombName = bombName.toLowerCase();

        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        ItemStack item = hAPI.getItemHead(getBombHdb(bombName));
        SkullMeta iMeta = (SkullMeta) item.getItemMeta();
        iMeta.displayName(Component.text(plugin.colourMessage("&e" + WordUtils.capitalize(bombName) + " Bomb")));
        NamespacedKey key = new NamespacedKey(plugin, "bomb-type");
        iMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, bombName);
        item.setItemMeta(iMeta);
        item.setAmount(amount);
        return item;
    }

    public static String getBombHdb(String bombType) {
        HashMap<String, String> bombTypes = new HashMap<>();
        bombTypes.put("small", "29488");
        bombTypes.put("medium", "11556");
        bombTypes.put("large", "11567");
        bombTypes.put("massive", "11555");
        bombTypes.put("nuke", "11564");

        return bombTypes.get(bombType);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // /bomb give <player> <type> <amount>
        if(args.length == 4) {
            String bomb = args[2];
            if(args[0].equalsIgnoreCase("give")) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                if(user != null) {
                    if(getBombHdb(bomb) != null) {
                        if(plugin.isInt(args[3])) {
                            int amount = Integer.parseInt(args[3]);
                            ItemStack item = getBomb(bomb, amount);
                            if(user.getInventory().canFit(item)) {
                                user.getInventory().addItem(item);
                            } else {
                                user.getLocation().getWorld().dropItem(user.getLocation(), item);
                            }
                        } else {
                            sender.sendMessage(plugin.colourMessage("&cAmount specified isnt a number!"));
                        }
                    } else {
                        sender.sendMessage(plugin.colourMessage("&cWrong Type! Available types are: Small, Medium, Large, Massive & Nuke"));
                    }
                } else {
                    sender.sendMessage(plugin.colourMessage("&c" + args[1] + " is not a valid player name!"));
                }
            } else {
                // NOT GIVE
            }
        } else {
            if(sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage(plugin.colourMessage("&cWrong usage! /bomb give <player> <type> <amount>"));
            } else {
                plugin.tellConsole(plugin.colourMessage("&cWrong usage! /bomb give <player> <type> <amount>"));
            }
        }
        return true;
    }
}
