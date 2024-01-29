package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BottledExpCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSender> manager;

    public BottledExpCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.manager = manager;
        createBottledExpCommands();
    }
    private void createBottledExpCommands() {
        Command.Builder<CommandSender> xpb = manager.commandBuilder("bottledexp", "xpb")
                .senderType(Player.class)
                .permission("skyprisoncore.command.bottledexp")
                .argument(IntegerArgument.<CommandSender>builder("amount").withMin(1).build());


        manager.command(xpb);
        manager.command(xpb.permission("skyprisoncore.command.bottledexp.tier3")
                .argument(IntegerArgument.<CommandSender>builder("bottles").withMin(1).withMax(64).asOptionalWithDefault(1).build())
                .handler(c -> {
                    int amount = c.get("amount");
                    int bottles = c.get("bottles");
                    int tAmount = amount * bottles;
                    Player player = (Player) c.getSender();
                    boolean canWithdraw = canPlayerWithdraw(player, tAmount);
                    if(canWithdraw) {
                        if (getTotalExperience(player) >= tAmount) {
                            if (PlayerManager.getBalance(player) >= tAmount * 0.25) {
                                createMultipleBottles(player, amount, bottles, tAmount);
                            } else {
                                player.sendMessage(Component.text("You need $" + ChatUtils.formatNumber(amount * 0.25)
                                        + " to bottle that amount of experience!", NamedTextColor.RED));
                            }
                        } else {
                            player.sendMessage(Component.text("You do not have that amount of experience!", NamedTextColor.RED));
                        }
                    } else {
                        player.sendMessage(Component.text("You can't withdraw that amount of experience at a time!", NamedTextColor.RED));
                    }
                }));

        manager.command(manager.commandBuilder("bottledexp", "xpb")
                .permission("skyprisoncore.command.bottledexp.tier2")
                .literal("all")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    if (PlayerManager.getBalance(player) >= getTotalExperience(player) * 0.25) {
                        createBottle(player, getTotalExperience(player));
                    } else {
                        player.sendMessage(Component.text("You need $" + ChatUtils.formatNumber(getTotalExperience(player) * 0.25)
                                + " to bottle that amount of experience!", NamedTextColor.RED));
                    }
                }));
    }
    public boolean canPlayerWithdraw(Player player, Integer amount) {
        if (player.hasPermission("skyprisoncore.command.bottledexp.tier2") || player.hasPermission("skyprisoncore.command.bottledexp.tier3")) {
            return true;
        } else if (player.hasPermission("skyprisoncore.command.bottledexp.tier1") && amount <= 10000) {
            return true;
        } else return amount <= 2500;
    }
    public void createBottle(Player player, Integer amount) {
        ItemStack expBottle = getExpBottle(amount, 1);
        if (player.getInventory().addItem(expBottle).isEmpty()) {
            player.giveExp(-amount);
            plugin.getServer().getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + amount * 0.25));
            player.sendMessage(Component.text("-" + ChatUtils.formatNumber(amount) + " XP", NamedTextColor.DARK_RED, TextDecoration.BOLD));
        } else {
            player.sendMessage(Component.text("You do not have space in your inventory!", NamedTextColor.RED));
        }
    }
    @NotNull
    private ItemStack getExpBottle(Integer amount, Integer bottleAmount) {
        ItemStack expBottle = new ItemStack(Material.EXPERIENCE_BOTTLE, bottleAmount);
        expBottle.editMeta(meta -> {
            meta.displayName(Component.text("Experience Bottle ", NamedTextColor.DARK_PURPLE).append(Component.text("(Throw)", NamedTextColor.GRAY)));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Experience: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(amount), NamedTextColor.YELLOW)));
            meta.lore(lore);
            NamespacedKey key = new NamespacedKey(plugin, "exp-amount");
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, amount);
        });
        return expBottle;
    }
    public void createMultipleBottles(Player player, Integer amount, Integer bAmount, Integer tAmount) {
        ItemStack expBottle = getExpBottle(amount, bAmount);
        if (player.getInventory().addItem(expBottle).isEmpty()) {
            player.giveExp(-tAmount);
            plugin.getServer().getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + tAmount * 0.25));
            player.sendMessage(Component.text("-" + ChatUtils.formatNumber(tAmount) + " XP", NamedTextColor.DARK_RED, TextDecoration.BOLD));
        } else {
            player.sendMessage(Component.text("You do not have space in your inventory!", NamedTextColor.RED));
        }
    }
    public int getTotalExperience(int level) {
        int xp = 0;
        if (level >= 0 && level <= 15) {
            xp = (int) Math.round(Math.pow(level, 2) + 6 * level);
        } else if (level > 15 && level <= 30) {
            xp = (int) Math.round((2.5 * Math.pow(level, 2) - 40.5 * level + 360));
        } else if (level > 30) {
            xp = (int) Math.round(((4.5 * Math.pow(level, 2) - 162.5 * level + 2220)));
        }
        return xp;
    }
    public int getTotalExperience(Player player) {
        return Math.round(player.getExp() * player.getExpToLevel()) + getTotalExperience(player.getLevel());
    }
}
