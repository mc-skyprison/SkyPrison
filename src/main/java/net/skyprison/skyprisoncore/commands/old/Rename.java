package net.skyprison.skyprisoncore.commands.old;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class Rename implements CommandExecutor { // /rename <name/remove>
    public Rename() {}

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player player) {
            if(args.length > 0) {
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType().isItem() && !heldItem.getType().isAir()) {
                    ItemMeta heldMeta = heldItem.getItemMeta();
                    if (!args[0].equalsIgnoreCase("remove")) {
                        String name = String.join(" ", args);
                        Component displayName = MiniMessage.miniMessage().deserialize(name);
                        heldMeta.displayName(displayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                        player.getInventory().getItemInMainHand().setItemMeta(heldMeta);
                        player.sendMessage(Component.text("Successfully changed custom item name to ", NamedTextColor.YELLOW).append(displayName));
                    } else {
                        if (heldItem.hasDisplayName()) {
                            heldMeta.displayName(null);
                            player.getInventory().getItemInMainHand().setItemMeta(heldMeta);
                            player.sendMessage(Component.text("Successfully removed custom item name!", NamedTextColor.YELLOW));
                        } else {
                            player.sendMessage(Component.text("This item doesn't have a display name!", NamedTextColor.RED));
                        }
                    }
                } else {
                    player.sendMessage(Component.text("You're not holding an item!", NamedTextColor.RED));
                }
            } else {
                player.sendMessage(Component.text("Incorrect Usage! /rename <name/remove>", NamedTextColor.RED));
            }
        } else {
            sender.sendMessage(Component.text("This command can only be used in game!", NamedTextColor.RED));
        }
        return true;
    }
}
