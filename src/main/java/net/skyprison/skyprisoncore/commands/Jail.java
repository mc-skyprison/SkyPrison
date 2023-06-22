package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class Jail implements CommandExecutor { // /jail <player> (time) (reason)
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public Jail(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(args.length > 0) {
            UUID pUUID = plugin.getPlayer(args[0]);
            if(pUUID != null) {
                int time = 5;
                String reason = "Jailed by Guard";
                if(args.length > 1) {
                    if(plugin.isInt(args[1]) && sender.hasPermission("skyprisoncore.command.jail.any")) {
                        time = Integer.parseInt(args[1]);
                        if(args.length > 2) {
                            reason = String.join(" ", Arrays.stream(args).toList().subList(3, args.length - 1));
                        }
                    } else {
                        reason = String.join(" ", Arrays.stream(args).toList().subList(3, args.length - 1));
                    }
                }
            } else {
                sender.sendMessage(Component.text("Player doesn't exist!", NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.text("Incorrect Usaage! /jail <player> (time) (reason)", NamedTextColor.RED));
        }
        return true;
    }
}
