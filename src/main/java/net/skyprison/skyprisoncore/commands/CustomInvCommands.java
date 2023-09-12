package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.DatabaseInventory;
import net.skyprison.skyprisoncore.utils.CustomInvUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomInvCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    public CustomInvCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createCustomInvCommands();
    }
    private void createCustomInvCommands() {
        manager.command(manager.commandBuilder("bartender")
                .permission("skyprisoncore.command.bartender")
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    Player player = c.getOptional("player").isPresent() ? (Player) c.getOptional("player").get() : c.getSender() instanceof Player ? (Player) c.getSender() : null;
                    if(player != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new DatabaseInventory(plugin, db, player,
                                player.hasPermission("skyprisoncore.inventories.bartender.editing"), "bartender").getInventory()));
                    } else {
                        c.getSender().sendMessage(Component.text("Invalid Usage! /bartender (player)"));
                    }
                }));

        Command.Builder<CommandSender> customInv = manager.commandBuilder("custominv")
                .permission("skyprisoncore.command.custominv");
        manager.command(customInv.literal("list")
                .permission("skyprisoncore.command.custominv.list")
                .argument(IntegerArgument.<CommandSender>builder("page").asOptionalWithDefault(1).withMin(1).withMax(20))
                .handler(c -> {
                    int page = c.get("page");
                    Component list = CustomInvUtils.getFormattedList(page);
                    c.getSender().sendMessage(list);
                }));
        manager.command(customInv.literal("open")
                .permission("skyprisoncore.command.custominv.open")
                .argument(StringArgument.<CommandSender>builder("name")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> CustomInvUtils.getList()))
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    Player player = c.getOptional("player").isPresent() ? (Player) c.getOptional("player").get() : c.getSender() instanceof Player ? (Player) c.getSender() : null;
                    if(player != null) {
                        String invName = c.get("name");
                        if(CustomInvUtils.categoryExists(invName)) {
                            if (player.hasPermission("skyprisoncore.inventories." + invName)) {
                                Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new DatabaseInventory(plugin, db, player,
                                        player.hasPermission("skyprisoncore.inventories." + invName + ".editing"), invName).getInventory()));
                            }
                        }
                    }
                }));
        manager.command(customInv.literal("create")
                .permission("skyprisoncore.command.custominv.create")
                .argument(StringArgument.of("name"))
                .argument(StringArgument.optional("display"))
                .argument(StringArgument.optional("colour"))
                .handler(c -> {
                    String name = c.get("name");
                    if(!CustomInvUtils.categoryExists(name)) {
                        String colour = c.getOrDefault("colour", null);
                        if(colour == null || NamedTextColor.NAMES.value(colour) != null || TextColor.fromHexString(colour) != null) {
                            CustomInvUtils.createCategory(name, c.getOrDefault("display", null), colour);
                        }
                    }
                }));
    }
}
