package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
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

import java.util.UUID;

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
                    CommandSender sender = c.getSender();
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
                .permission("skyprisoncore.command.vote.history")
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new VoteHistory(plugin, db, player.getUniqueId()).getInventory()));
                    }
                }));

        manager.command(vote.literal("history")
                .permission("skyprisoncore.command.vote.history.others")
                .argument(StringArgument.optional("player"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(sender instanceof Player player) {
                        String playerName = c.getOrDefault("player", player.getName());
                        UUID pUUID = PlayerManager.getPlayerId(playerName);
                        if (pUUID != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new VoteHistory(plugin, db, pUUID).getInventory()));
                        } else {
                            sender.sendMessage(Component.text("Specified player doesn't exist!"));
                        }
                    } else {
                        sender.sendMessage(Component.text("Can only be used by a player!"));
                    }
                }));
    }
}
