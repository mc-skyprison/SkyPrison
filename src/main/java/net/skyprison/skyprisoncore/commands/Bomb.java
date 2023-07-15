package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class Bomb implements CommandExecutor {
    private final SkyPrisonCore plugin;
    public Bomb(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    public static ItemStack getBomb(SkyPrisonCore plugin, String bombName, int amount) {
        if(bombName.contains("_")) {
            bombName = bombName.split("_")[0];
        }

        bombName = bombName.toLowerCase();

        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        ItemStack item = hAPI.getItemHead(getBombHdb(bombName));
        SkullMeta iMeta = (SkullMeta) item.getItemMeta();
        iMeta.displayName(Component.text(WordUtils.capitalize(bombName) + " Bomb", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // /bomb give <player> <type> <amount>
        if(args.length == 4) {
            String bomb = args[2];
            if(args[0].equalsIgnoreCase("give")) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                if(user != null) {
                    if(getBombHdb(bomb) != null) {
                        if(plugin.isInt(args[3])) {
                            int amount = Integer.parseInt(args[3]);
                            ItemStack item = getBomb(plugin, bomb, amount);
                            if(user.getInventory().canFit(item)) {
                                user.getInventory().addItem(item);
                            } else {
                                user.getLocation().getWorld().dropItem(user.getLocation(), item);
                            }
                        } else {
                            sender.sendMessage(Component.text("Amount specified isnt a number!", NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("Wrong Type! Available types are: Small, Medium, Large, Massive & Nuke", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text(args[1] + " is not a valid player name!", NamedTextColor.RED));
                }
            }
        } else {
            if(sender instanceof Player player) {
                player.sendMessage(Component.text("Incorrect usage! /bomb give <player> <type> <amount>", NamedTextColor.RED));
            } else {
                plugin.tellConsole(Component.text("Incorrect usage! /bomb give <player> <type> <amount>", NamedTextColor.RED));
            }
        }
        return true;
    }
}
