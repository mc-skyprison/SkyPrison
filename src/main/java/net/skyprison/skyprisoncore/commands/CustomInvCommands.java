package net.skyprison.skyprisoncore.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.misc.DatabaseInventory;
import net.skyprison.skyprisoncore.utils.CustomInvUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.SuggestionProvider;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class CustomInvCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSourceStack> manager;
    public CustomInvCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSourceStack> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createCustomInvCommands();
    }
    private void createCustomInvCommands() {
        manager.command(manager.commandBuilder("bartender")
                .permission("skyprisoncore.command.bartender")
                .optional("player", playerParser())
                .handler(c -> Bukkit.getScheduler().runTask(plugin, () -> {
                    Player player = c.getOrDefault("player", null);
                    if(player == null && c.sender() instanceof Player) {
                        player = (Player) c.sender();
                    }
                    if(player != null) {
                        Player finalPlayer = player;
                        Bukkit.getScheduler().runTask(plugin, () -> finalPlayer.openInventory(new DatabaseInventory(plugin, db, finalPlayer,
                                finalPlayer.hasPermission("skyprisoncore.inventories.bartender.editing"), "bartender").getInventory()));
                    } else {
                        c.sender().getSender().sendMessage(Component.text("Invalid Usage! /bartender (player)"));
                    }
                })));

        Command.Builder<CommandSourceStack> customInv = manager.commandBuilder("custominv")
                .permission("skyprisoncore.command.custominv");
        manager.command(customInv.literal("list")
                .permission("skyprisoncore.command.custominv.list")
                .optional("page", integerParser(1, 20), DefaultValue.constant(1))
                .handler(c -> {
                    int page = c.get("page");
                    Component list = CustomInvUtils.getFormattedList(page);
                    c.sender().getSender().sendMessage(list);
                }));
        manager.command(customInv.literal("open")
                .permission("skyprisoncore.command.custominv.open")
                .required("name", stringParser(), SuggestionProvider.suggestingStrings(CustomInvUtils.getList()))
                .optional("player", playerParser())
                .handler(c -> {
                    Player player = c.getOrDefault("player", null);
                    if(player == null && c.sender() instanceof Player) {
                        player = (Player) c.sender();
                    }
                    if(player != null) {
                        String invName = c.get("name");
                        if(CustomInvUtils.categoryExists(invName)) {
                            if (player.hasPermission("skyprisoncore.inventories." + invName)) {
                                Player finalPlayer = player;
                                Bukkit.getScheduler().runTask(plugin, () -> finalPlayer.openInventory(new DatabaseInventory(plugin, db, finalPlayer,
                                        finalPlayer.hasPermission("skyprisoncore.inventories." + invName + ".editing"), invName).getInventory()));
                            }
                        }
                    }
                }));
        manager.command(customInv.literal("create")
                .permission("skyprisoncore.command.custominv.create")
                .required("name", stringParser())
                .optional("display", stringParser())
                .optional("colour", stringParser())
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
