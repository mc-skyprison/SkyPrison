package net.skyprison.skyprisoncore.commands;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DonorReset implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public DonorReset(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        switch (args[0].toLowerCase()) {
            case "level1" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("group.donor1") && player.getWorld().getName().equalsIgnoreCase("world_prison")) {
                        player.sendMessage(plugin.colourMessage("&f[&cMines&f] &dFirst Donor Mine &7has been reset!"));
                    }
                }
            }
            case "level2" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("group.donor2") && player.getWorld().getName().equalsIgnoreCase("world_prison")) {
                        player.sendMessage(plugin.colourMessage("&f[&cMines&f] &dSecond Donor Mine &7has been reset!"));
                    }
                }
            }
            case "level3" -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("group.donor3") && player.getWorld().getName().equalsIgnoreCase("world_prison")) {
                        player.sendMessage(plugin.colourMessage("&f[&cMines&f] &dThird Donor Mine &7has been reset!"));
                    }
                }
            }
        }
        return true;
    }
}
