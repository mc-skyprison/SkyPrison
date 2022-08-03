package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.UUID;

public class Bail implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public Bail(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    private HashMap<UUID, Double> bailOut = new HashMap<>();

    private HashMap<UUID, Long> coolDown = new HashMap<>();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            if(user.isJailed()) {
                if(coolDown.containsKey(user.getUniqueId())) {
                    long releaseTime = coolDown.get(user.getUniqueId());
                    long currTime = System.currentTimeMillis();
                    if(releaseTime > currTime) {
                        long timeTill = releaseTime - currTime;
                        int minutes = (int) Math.floor((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                        int seconds = (int) Math.floor((timeTill % (1000.0 * 60.0)) / 1000.0);
                        if(minutes != 0.0) {
                            user.sendMessage(plugin.colourMessage("&cYou've recently bailed yourself out! Wait " + minutes + " min " + seconds + " sec"));
                        } else {
                            user.sendMessage(plugin.colourMessage("&cYou've recently bailed yourself out! Wait " + seconds + " sec"));
                        }
                        return true;
                    }
                }
                if (args.length == 0) {
                    double bailCash = 0;
                    boolean hasCash = false;
                    if (player.hasPermission("group.end")) {
                        if(user.getBalance() >= 50000) {
                            bailCash = 50000;
                            hasCash = true;
                        }
                    } else if (player.hasPermission("group.hell")) {
                        if(user.getBalance() >= 25000) {
                            bailCash = 25000;
                            hasCash = true;
                        }
                    } else if (player.hasPermission("group.free")) {
                        if(user.getBalance() >= 10000) {
                            bailCash = 10000;
                            hasCash = true;
                        }
                    } else if (player.hasPermission("group.nether")) {
                        if(user.getBalance() >= 1000) {
                            bailCash = 2500;
                            hasCash = true;
                        }
                    } else if (player.hasPermission("group.default")) {
                        if(user.getBalance() >= 250) {
                            bailCash = 500;
                            hasCash = true;
                        }
                    } else {
                        hasCash = true;
                    }
                    if(hasCash) {
                        bailOut.put(player.getUniqueId(), bailCash);
                        TextComponent msg = Component.text(plugin.colourMessage("&eClick here to bail yourself out for &a$" + plugin.formatNumber(bailCash)))
                                .clickEvent(ClickEvent.runCommand("/bail confirm"))
                                .hoverEvent(Component.text(plugin.colourMessage("&eClick me!")));
                        player.sendMessage(msg);
                    } else {
                        player.sendMessage(plugin.colourMessage("&cYou can't afford to bail yourself out!"));
                    }
                } else {
                    if(bailOut.containsKey(player.getUniqueId())) {
                        if(user.getBalance() >= bailOut.get(player.getUniqueId())) {
                            plugin.asConsole("money take " + player.getName() + " " + bailOut.get(player.getUniqueId()));
                            coolDown.put(player.getUniqueId(), System.currentTimeMillis() + 3600000);
                            user.unjail();
                        } else {
                            player.sendMessage(plugin.colourMessage("&cYou no longer have enough money! Cancelling bailout.."));
                        }
                        bailOut.remove(player.getUniqueId());
                    } else {
                        player.sendMessage(plugin.colourMessage("&CYou have no pending bail.."));
                    }
                }
            }
        }
        return true;
    }
}
