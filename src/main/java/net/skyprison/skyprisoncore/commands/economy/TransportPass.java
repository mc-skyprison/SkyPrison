package net.skyprison.skyprisoncore.commands.economy;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransportPass implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public TransportPass(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        switch (args[1].toLowerCase()) {
            case "bus":
                if(!player.hasPermission("skyprisoncore.command.transportpass.bus")) {
                    if(plugin.tokensData.get(player.getUniqueId().toString()) >= 250) {
                        int uTokens = plugin.tokensData.get(player.getUniqueId().toString()) - 250;
                        player.sendMessage(plugin.colourMessage("&bTokens &8» &b250 tokens &7was removed from your balance"));
                        plugin.tokensData.put(player.getUniqueId().toString(), uTokens);
                        plugin.asConsole("lp user " + player.getName() + " permission settemp skyprisoncore.command.transportpass.bus true 7d");
                    } else {
                        player.sendMessage(plugin.colourMessage("&bTokens &8» &cYou do not have enough tokens to buy this!"));
                    }
                } else {
                    player.sendMessage(plugin.colourMessage("&cYou already have a bus pass!"));
                }
                break;
            case "train":
                if(!player.hasPermission("skyprisoncore.command.transportpass.train")) {
                    if(plugin.tokensData.get(player.getUniqueId().toString()) >= 500) {
                        int uTokens = plugin.tokensData.get(player.getUniqueId().toString()) - 500;
                        player.sendMessage(plugin.colourMessage("&bTokens &8» &b500 tokens &7was removed from your balance"));
                        plugin.tokensData.put(player.getUniqueId().toString(), uTokens);
                        plugin.asConsole("lp user " + player.getName() + " permission settemp skyprisoncore.command.transportpass.train true 7d");
                    } else {
                        player.sendMessage(plugin.colourMessage("&bTokens &8» &cYou do not have enough tokens to buy this!"));
                    }
                } else {
                    player.sendMessage(plugin.colourMessage("&cYou already have a train pass!"));
                }
                break;
            default:
                player.sendMessage(plugin.colourMessage("&cCorrect Usage: /transportpass <bus/train>"));
                break;
        }
        return true;
    }
}
