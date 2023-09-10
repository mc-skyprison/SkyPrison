package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;

import java.util.Objects;

import static net.skyprison.skyprisoncore.SkyPrisonCore.lastMessaged;

public class ChatCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSender> manager;
    private final ChatUtils chatUtils;
    public ChatCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSender> manager, DiscordApi discApi) {
        this.plugin = plugin;
        this.manager = manager;
        createChatCommands();
        this.chatUtils = new ChatUtils(plugin, discApi);
    }
    private void runCommand(String msg, CommandSender sender, String chatId, String discordId) {
        if(!msg.isEmpty()) {
            chatUtils.chatSendMessage(msg, sender, chatId, discordId);
        } else {
            if(sender instanceof Player player) {
                chatUtils.stickyChatCheck(player, chatId, discordId);
            } else {
                chatUtils.wrongUsage(sender, chatId);
            }
        }
    }
    private void createChatCommands() {
        manager.command(manager.commandBuilder("b")
                .permission("skyprisoncore.command.build")
                .argument(StringArgument.optional("message", StringArgument.StringMode.GREEDY))
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.getSender(), "build", "800885673732997121"))
                .build()
        );
        manager.command(manager.commandBuilder("a")
                .permission("skyprisoncore.command.admin")
                .argument(StringArgument.optional("message", StringArgument.StringMode.GREEDY))
                .handler(c -> {
                    runCommand(c.getOrDefault("message", ""), c.getSender(), "admin", "791054229136605194");
                })
                .build()
        );
        manager.command(manager.commandBuilder("g")
                .permission("skyprisoncore.command.guard")
                .argument(StringArgument.optional("message", StringArgument.StringMode.GREEDY))
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.getSender(), "guard", "791054021338464266"))
                .build()
        );
        manager.command(manager.commandBuilder("s")
                .permission("skyprisoncore.command.staff")
                .argument(StringArgument.optional("message", StringArgument.StringMode.GREEDY))
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.getSender(), "staff", "791054076787163166"))
                .build()
        );
        manager.command(manager.commandBuilder("msg")
                .permission("skyprisoncore.command.msg")
                .argument(PlayerArgument.of("player"))
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    final Player player = c.get("player");
                    final String message = c.get("message");
                    sendPrivateMessage(sender, player, message);
                }));
        manager.command(manager.commandBuilder("reply", "r")
                .permission("skyprisoncore.command.reply")
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    CommandSender sender = c.getSender();
                    if(!lastMessaged.isEmpty() && lastMessaged.containsKey(sender) && lastMessaged.get(sender) != null
                            && ((lastMessaged.get(sender) instanceof Player player && player.isOnline()) || lastMessaged.get(sender) instanceof ConsoleCommandSender)) {
                        sendPrivateMessage(sender, lastMessaged.get(sender), c.get("message"));
                    } else {
                        sender.sendMessage(Component.text("Noone to reply to found..", NamedTextColor.RED));
                    }
                }));
        manager.command(manager.commandBuilder("stellraw")
                .permission("skyprisoncore.command.stellraw")
                .argument(StringArgument.greedy("message"))
                .handler(c -> {
                    String msg = c.get("message");
                    Component fMsg = plugin.getParsedString(c.getSender(), "chat", msg);
                    plugin.getServer().sendMessage(fMsg);
                }));
    }
    private void sendPrivateMessage(CommandSender sender, Audience receiver, String message) {
        Component senderName = sender.name();
        Component pMsg = Component.empty().append(Component.text(" » ", TextColor.fromHexString("#940b34")))
                .append(plugin.getParsedString(sender, "private", message).colorIfAbsent(NamedTextColor.GRAY));
        if (sender instanceof Player toPlayer) {
            Component customName = toPlayer.customName();
            if (customName != null) senderName = customName;
        }

        Component receiverName = Component.text("Unknown");
        if(receiver instanceof Player player) {
            receiverName = Objects.requireNonNullElse(player.customName(), player.displayName());
        } else if(receiver instanceof CommandSender receiving) {
            receiverName = receiving.name();
        }

        Component msgTo = Component.empty().append(Component.text("Me", TextColor.fromHexString("#f02d68"))).append(Component.text(" ⇒ ", TextColor.fromHexString("#940b34")))
                .append(receiverName.colorIfAbsent(TextColor.fromHexString("#f02d68")));
        Component msgFrom = Component.empty().append(senderName.colorIfAbsent(TextColor.fromHexString("#f02d68"))).append(Component.text(" ⇒ ", TextColor.fromHexString("#940b34")))
                .append(Component.text("Me", TextColor.fromHexString("#f02d68")));

        sender.sendMessage(msgTo.append(pMsg));
        receiver.sendMessage(msgFrom.append(pMsg));

        if (lastMessaged.isEmpty() || !lastMessaged.containsKey(sender) || !lastMessaged.get(sender).equals(receiver)) {
            lastMessaged.put(sender, receiver);
        }
        if (lastMessaged.isEmpty() || !lastMessaged.containsKey(receiver)) {
            lastMessaged.put(receiver, sender);
        }
    }
}
