package net.skyprison.skyprisoncore.commands.old;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomGive implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public RandomGive(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // /randomgive <player> <item> <amount> (-s)
        if(args.length > 2) {
            if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[0]);
                if(user.isOnline()) {
                    Player player = user.getPlayer();
                    if (plugin.isInt(args[2])) {
                        int amount = Integer.parseInt(args[2]);
                        ItemStack item;
                        TextComponent senderMessage;
                        TextComponent playerMessage;
                        switch (args[1].toLowerCase()) {
                            case "candle" -> {
                                List<Material> candles = new ArrayList<>(MaterialSetTag.CANDLES.getValues());
                                Collections.shuffle(candles);
                                item = new ItemStack(candles.get(0), amount);
                                if (user.getInventory().canFit(item)) {
                                    player.getInventory().addItem(item);
                                } else {
                                    player.getLocation().getWorld().dropItem(player.getLocation(), item);
                                }
                                senderMessage = Component.text("Successfully gave ", NamedTextColor.GREEN)
                                        .append(Component.text(player.getName(), NamedTextColor.GREEN, TextDecoration.BOLD))
                                        .append(Component.text(" " + amount + " randomly coloured candles!", NamedTextColor.GREEN));
                                playerMessage = Component.text("You've received " + amount + " randomly coloured candles!", NamedTextColor.GREEN);
                                if (args.length > 3) {
                                    if (args[3].equalsIgnoreCase("-s")) {
                                        playerMessage = null;
                                    }
                                }
                                if (playerMessage != null) {
                                    sender.sendMessage(senderMessage);
                                    player.sendMessage(playerMessage);
                                }
                            }
                            case "concrete" -> {
                                List<Material> concretes = new ArrayList<>(MaterialTags.CONCRETES.getValues());
                                Collections.shuffle(concretes);
                                item = new ItemStack(concretes.get(0), amount);
                                if (user.getInventory().canFit(item)) {
                                    player.getInventory().addItem(item);
                                } else {
                                    player.getLocation().getWorld().dropItem(player.getLocation(), item);
                                }
                                senderMessage = Component.text("Successfully gave ", NamedTextColor.GREEN)
                                        .append(Component.text(player.getName(), NamedTextColor.GREEN, TextDecoration.BOLD))
                                        .append(Component.text(" " + amount + " randomly coloured concrete!", NamedTextColor.GREEN));
                                playerMessage = Component.text("You've received " + amount + " randomly coloured conrete!", NamedTextColor.GREEN);
                                if (args.length > 3) {
                                    if (args[3].equalsIgnoreCase("-s")) {
                                        playerMessage = null;
                                    }
                                }
                                if (playerMessage != null) {
                                    sender.sendMessage(senderMessage);
                                    player.sendMessage(playerMessage);
                                }
                            }
                            default ->
                                    sender.sendMessage(Component.text("Can't randomly give of that item!", NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("Amount must be a number!", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("Player must be online!", NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text("Player doesn't exist!", NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.text("Correct Usage: /randomgive <player> <item> <amount>", NamedTextColor.RED));
        }
        return true;
    }
}
