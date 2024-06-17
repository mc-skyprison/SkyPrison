package net.skyprison.skyprisoncore.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.secrets.Secrets;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsCategoryEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsHistory;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class SecretsCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSourceStack> manager;
    public SecretsCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSourceStack> manager) {
        this.plugin = plugin;
        this.manager = manager;
        createSecretsCommands();
    }
    private void createSecretsCommands() {
        Command.Builder<CommandSourceStack> secrets = manager.commandBuilder("secrets", "secret")
                .permission("skyprisoncore.command.secrets")
                .handler(c -> {
                    Player sender = (Player) c.sender().getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> sender.openInventory(new Secrets(plugin, db, sender, "", sender.hasPermission("skyprisoncore.command.secrets.create.secret"),
                            sender.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory()));
                });
        manager.command(secrets);

        manager.command(secrets.literal("history")
                .permission("skyprisoncore.command.secrets.history")
                .handler(c -> {
                    Player player = (Player) c.sender().getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SecretsHistory(plugin, db, player.getUniqueId()).getInventory()));
                }));

        manager.command(secrets.literal("history")
                .permission("skyprisoncore.command.secrets.history.others")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender().getSender();
                    UUID pUUID = PlayerManager.getPlayerId(c.get("player"));
                    if(pUUID != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SecretsHistory(plugin, db, pUUID).getInventory()));
                    } else {
                        player.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                    }
                }));
        Command.Builder<CommandSourceStack> secretsCreate = secrets.literal("create")
                .permission("skyprisoncore.command.secrets.create");

        manager.command(secretsCreate.literal("secret")
                .permission("skyprisoncore.command.secrets.create.secret")
                .handler(c -> {
                    Player player = (Player) c.sender().getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SecretsEdit(plugin, db, player.getUniqueId(), -1).getInventory()));
                }));

        manager.command(secretsCreate.literal("category")
                .permission("skyprisoncore.command.secrets.create.category")
                .handler(c -> {
                    Player player = (Player) c.sender().getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new SecretsCategoryEdit(plugin, db, player.getUniqueId(), null).getInventory()));
                }));
    }
}
