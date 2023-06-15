package net.skyprison.skyprisoncore.commands.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TransportPass implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public TransportPass(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    private final Component prefix = Component.text("Tokens", NamedTextColor.AQUA).append(Component.text(" » ", NamedTextColor.DARK_GRAY));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if(player != null) {
            switch (args[1].toLowerCase()) {
                case "bus" -> {
                    if (!player.hasPermission("skyprisoncore.command.transportpass.bus")) {
                        if (plugin.tokensData.get(player.getUniqueId()) >= 250) {
                            int uTokens = plugin.tokensData.get(player.getUniqueId()) - 250;
                            player.sendMessage(prefix.append(Component.text("250 tokens ", NamedTextColor.AQUA).append(Component.text("was removed from your balance", NamedTextColor.GRAY))));
                            plugin.tokensData.put(player.getUniqueId(), uTokens);
                            plugin.asConsole("lp user " + player.getName() + " permission settemp skyprisoncore.command.transportpass.bus true 7d");
                        } else {
                            player.sendMessage(prefix.append(Component.text("You do not have enough tokens to buy this!", NamedTextColor.RED)));
                        }
                    } else {
                        player.sendMessage(prefix.append(Component.text("You already have a bus pass!", NamedTextColor.RED)));
                    }
                }
                case "train" -> {
                    if (!player.hasPermission("skyprisoncore.command.transportpass.train")) {
                        if (plugin.tokensData.get(player.getUniqueId()) >= 500) {
                            int uTokens = plugin.tokensData.get(player.getUniqueId()) - 500;
                            player.sendMessage(prefix.append(Component.text("500 tokens ", NamedTextColor.AQUA).append(Component.text("was removed from your balance", NamedTextColor.GRAY))));
                            plugin.tokensData.put(player.getUniqueId(), uTokens);
                            plugin.asConsole("lp user " + player.getName() + " permission settemp skyprisoncore.command.transportpass.train true 7d");
                        } else {
                            player.sendMessage(prefix.append(Component.text("You do not have enough tokens to buy this!", NamedTextColor.RED)));
                        }
                    } else {
                        player.sendMessage(prefix.append(Component.text("You already have a train pass!", NamedTextColor.RED)));
                    }
                }
                default -> player.sendMessage(prefix.append(Component.text("Incorrect Usage! /transportpass <bus/train>", NamedTextColor.RED)));
            }
        }
        return true;
    }
}
