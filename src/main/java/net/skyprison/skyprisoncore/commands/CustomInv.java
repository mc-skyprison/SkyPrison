package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.DatabaseInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomInv implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public CustomInv(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player) {
            if(args.length > 0) {
                if(player.hasPermission("skyprisoncore.inventories." + args[0])) {
                    player.openInventory(new DatabaseInventory(plugin, db, player, player.hasPermission("skyprisoncore.inventories." + args[0] + ".editing"), args[0]).getInventory());
                }
            } else {
                player.sendMessage(Component.text("Incorrect Usage! /custominv <inventory>", NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.text("Command can only be used by a player!", NamedTextColor.RED));
        }
        return true;
    }
}
