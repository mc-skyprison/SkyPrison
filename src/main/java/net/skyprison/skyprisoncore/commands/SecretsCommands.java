package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.Secrets;
import net.skyprison.skyprisoncore.inventories.SecretsHistory;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class SecretsCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSender> manager;
    public SecretsCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.manager = manager;
        createSecretsCommands();
    }
    private void createSecretsCommands() {
        List<String> categories = Arrays.asList("all", "grass", "desert", "nether", "snow", "prison-other", "skycity");
        Command.Builder<CommandSender> secrets = manager.commandBuilder("secrets", "secret")
                .permission("skyprisoncore.command.secrets")
                .argument(StringArgument.<CommandSender>builder("category")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> categories).asOptionalWithDefault("main"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("You can only run this in game!", NamedTextColor.RED));
                        return;
                    }
                    final String category = c.getOrDefault("category", "main");
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new Secrets(plugin, db, player, category).getInventory()));
                });
        manager.command(secrets);

        manager.command(secrets.literal("history")
                .permission("skyprisoncore.command.secrets.history")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SecretsHistory(plugin, db, player.getUniqueId()).getInventory()));
                }));

        manager.command(secrets.literal("history")
                .permission("skyprisoncore.command.secrets.history.others")
                .senderType(Player.class)
                .argument(StringArgument.of("player"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    UUID pUUID = PlayerManager.getPlayerId(c.get("player"));
                    if(pUUID != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SecretsHistory(plugin, db, pUUID).getInventory()));
                    } else {
                        player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                    }
                }));
    }
}
