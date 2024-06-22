package net.skyprison.skyprisoncore.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.mail.MailBox;
import net.skyprison.skyprisoncore.inventories.mail.MailBoxSend;
import net.skyprison.skyprisoncore.inventories.mail.MailHistory;
import net.skyprison.skyprisoncore.items.PostOffice;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.MailUtils;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.List;
import java.util.UUID;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class MailCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSourceStack> manager;
    public MailCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSourceStack> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createMailCommands();
    }
    private void createMailCommands() {
        Command.Builder<CommandSourceStack> postOffice = manager.commandBuilder("postoffice")
                .permission("skyprisoncore.command.postoffice");
        List<String> postOfficeOptions = List.of("mailbox");
        manager.command(postOffice.literal("give")
                .permission("skyprisoncore.command.postoffice.give")
                .required("player", playerParser())
                .required("type", stringParser(), SuggestionProvider.suggestingStrings(postOfficeOptions))
                .required("amount", integerParser())
                .handler(c -> {
                    final Player player = c.get("player");
                    final String type = c.get("type");
                    final int amount = c.get("amount");
                    if(postOfficeOptions.contains(type.toLowerCase())) {
                        ItemStack item = PostOffice.getItemFromType(plugin, type, amount);
                        if (item != null) {
                            PlayerManager.giveItems(player, item);
                            c.sender().getSender().sendMessage(Component.text("Successfully sent!"));
                        }
                    }
                }));
        Command.Builder<CommandSourceStack> mail = manager.commandBuilder("mail")
                .permission("skyprisoncore.command.mail");
        manager.command(mail.literal("history")
                .permission("skyprisoncore.command.mail.history")
                .handler(c -> {
                    Player sender = (Player) c.sender().getSender();
                    Bukkit.getScheduler().runTask(plugin, () -> sender.openInventory(new MailHistory(plugin, db, sender.getUniqueId()).getInventory()));
                }));
        manager.command(mail.literal("history")
                .permission("skyprisoncore.command.mail.history.others")
                .optional("player", stringParser())
                .handler(c -> {
                    Player sender = (Player) c.sender().getSender();
                    String playerName = c.getOrDefault("player", sender.getName());
                    UUID pUUID = PlayerManager.getPlayerId(playerName);
                    if (pUUID != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> sender.openInventory(new MailHistory(plugin, db, pUUID).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Specified player doesn't exist!"));
                    }
                }));
        manager.command(mail.literal("send")
                .permission("skyprisoncore.command.mail.send")
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new MailBoxSend(plugin, db, player,
                                player.hasPermission("skyprisoncore.command.mail.send.items")).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
        manager.command(mail.literal("open")
                .permission("skyprisoncore.command.mail.open")
                .required("mailbox-id", integerParser())
                .optional("player", playerParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
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
        manager.command(mail.literal("expand")
                .permission("skyprisoncore.command.mail.expand")
                .required("player", playerParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    Player player = c.get("player");
                    if(player.hasPermission("skyprisoncore.mailboxes.amount.2")) {
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.mailboxes.amount.3"));
                    } else if(player.hasPermission("skyprisoncore.mailboxes.amount.1")) {
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.mailboxes.amount.2"));
                    } else {
                        sender.sendMessage(Component.text("Player already has the maximum amount of mailboxes!", NamedTextColor.RED));
                    }
                }));
    }
}
