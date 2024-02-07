package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.misc.VoteHistory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;

import java.util.UUID;

import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class VoteCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    public VoteCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createVoteCommands();
    }
    private void createVoteCommands() {
        Command.Builder<CommandSender> vote = manager.commandBuilder("vote")
                .permission("skyprisoncore.command.vote")
                .handler(c -> {
                    CommandSender sender = c.sender();
                    Component msg = Component.text("Vote for our server!", NamedTextColor.DARK_RED, TextDecoration.BOLD);
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Planet Minecraft", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on PlanetMinecraft", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://www.planetminecraft.com/server/sky-prison/vote/")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft Serverlist", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft Serverlist", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraft-server-list.com/server/473461/vote/")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft Servers", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft Servers", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraftservers.org/vote/457013")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("TopG", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on TopG", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://topg.org/Minecraft/in-471006")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft Buzz", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft Buzz", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraft.buzz/vote/1142")));
                    msg = msg.appendNewline();
                    msg = msg.append(Component.text("Minecraft MP", NamedTextColor.RED, TextDecoration.BOLD).hoverEvent(HoverEvent
                                    .showText(Component.text("Click Here to vote on Minecraft MP", NamedTextColor.AQUA)))
                            .clickEvent(ClickEvent.openUrl("https://minecraft-mp.com/server/279527/vote/")));
                    sender.sendMessage(msg);
                });
        manager.command(vote);

        manager.command(vote.literal("history")
                .senderType(Player.class)
                .permission("skyprisoncore.command.vote.history")
                .handler(c -> {
                    Player sender = c.sender();
                    Bukkit.getScheduler().runTask(plugin, () -> sender.openInventory(new VoteHistory(plugin, db, sender.getUniqueId()).getInventory()));
                }));

        manager.command(vote.literal("history")
                .senderType(Player.class)
                .permission("skyprisoncore.command.vote.history.others")
                .optional("player", stringParser())
                .handler(c -> {
                    Player sender = c.sender();
                    String playerName = c.getOrDefault("player", sender.getName());
                    UUID pUUID = PlayerManager.getPlayerId(playerName);
                    if (pUUID != null) {
                        Bukkit.getScheduler().runTask(plugin, () -> sender.openInventory(new VoteHistory(plugin, db, pUUID).getInventory()));
                    } else {
                        sender.sendMessage(Component.text("Specified player doesn't exist!"));
                    }
                }));
    }
}
