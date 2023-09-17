package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.mail.MailBox;
import net.skyprison.skyprisoncore.inventories.mail.MailBoxSend;
import net.skyprison.skyprisoncore.inventories.mail.MailHistory;
import net.skyprison.skyprisoncore.items.PostOffice;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.MailUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class MailCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    public MailCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createMailCommands();
    }
    private void createMailCommands() {
        Command.Builder<CommandSender> postOffice = this.manager.commandBuilder("postoffice")
                .permission("skyprisoncore.command.postoffice");
        List<String> postOfficeOptions = List.of("mailbox");
        this.manager.command(postOffice.literal("give")
                .permission("skyprisoncore.command.postoffice.give")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.<CommandSender>builder("type")
                        .withSuggestionsProvider((commandSenderCommandContext, s) -> postOfficeOptions))
                .argument(IntegerArgument.of("amount"))
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(postOfficeOptions.contains(type.toLowerCase())) {
                        ItemStack item = PostOffice.getItemFromType(plugin, type, amount);
                        if (item != null) {
                            plugin.giveItem(player, item);
                            c.getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));
        Command.Builder<CommandSender> mail = this.manager.commandBuilder("mail")
                .permission("skyprisoncore.command.mail");
        this.manager.command(mail.literal("history")
                .permission("skyprisoncore.command.mail.history")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new MailHistory(plugin, db, player.getUniqueId()).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(mail.literal("history")
                .permission("skyprisoncore.command.mail.history.others")
                .argument(StringArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        String playerName = c.getOrDefault("player", player.getName());
                        UUID pUUID = PlayerManager.getPlayerId(playerName);
                        if (pUUID != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new MailHistory(plugin, db, pUUID).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("Specified player doesn't exist!"));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(mail.literal("send")
                .permission("skyprisoncore.command.mail.send")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new MailBoxSend(plugin, db, player,
                                player.hasPermission("skyprisoncore.command.mail.send.items")).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        this.manager.command(mail.literal("open")
                .permission("skyprisoncore.command.mail.open")
                .argument(IntegerArgument.of("mailbox-id"))
                .argument(PlayerArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    Player player = c.getOrDefault("player", sender instanceof Player ? (Player) sender : null);
                    if(player != null) {
                        int mailBoxId = c.get("mailbox-id");
                        String mailBox = MailUtils.getMailBoxName(mailBoxId);
                        if(mailBox != null && !mailBox.isEmpty()) {
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(
                                    new MailBox(plugin, db, player, MailUtils.isOwner(player, mailBoxId), mailBoxId, 1).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("No mailbox found with that id!", NamedTextColor.RED));
                        }
                    } else {
                        sender.sendMessage(Component.text("Incorrect Usage! /mail open <id> <player>"));
                    }
                }));
        this.manager.command(mail.literal("expand")
                .permission("skyprisoncore.command.mail.expand")
                .argument(PlayerArgument.of("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    Player player = c.get("player");
                    if(player.hasPermission("skyprisoncore.mailboxes.amount.2")) {
                        Bukkit.getScheduler().runTask(plugin, () -> plugin.asConsole("lp user " + player.getName() + " permission set skyprisoncore.mailboxes.amount.3"));
                    } else if(player.hasPermission("skyprisoncore.mailboxes.amount.1")) {
                        Bukkit.getScheduler().runTask(plugin, () -> plugin.asConsole("lp user " + player.getName() + " permission set skyprisoncore.mailboxes.amount.2"));
                    } else {
                        sender.sendMessage(Component.text("Player already has the maximum amount of mailboxes!", NamedTextColor.RED));
                    }
                }));
    }
}
