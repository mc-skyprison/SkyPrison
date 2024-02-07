package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.javacord.api.DiscordApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.skyprison.skyprisoncore.SkyPrisonCore.lastMessaged;
import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.StringParser.*;

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
                .optional("message", greedyStringParser())
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.sender(), "build", "800885673732997121"))
                .build()
        );
        manager.command(manager.commandBuilder("a")
                .permission("skyprisoncore.command.admin")
                .optional("message", greedyStringParser())
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.sender(), "admin", "791054229136605194"))
                .build()
        );
        manager.command(manager.commandBuilder("g")
                .permission("skyprisoncore.command.guard")
                .optional("message", greedyStringParser())
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.sender(), "guard", "791054021338464266"))
                .build()
        );
        manager.command(manager.commandBuilder("s")
                .permission("skyprisoncore.command.staff")
                .optional("message", greedyStringParser())
                .handler(c -> runCommand(c.getOrDefault("message", ""), c.sender(), "staff", "791054076787163166"))
                .build()
        );
        manager.command(manager.commandBuilder("msg")
                .permission("skyprisoncore.command.msg")
                .required("player", playerParser())
                .required("message", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender();
                    final Player player = c.get("player");
                    final String message = c.get("message");
                    sendPrivateMessage(sender, player, message);
                }));
        manager.command(manager.commandBuilder("reply", "r")
                .permission("skyprisoncore.command.reply")
                .required("message", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender();
                    if(!lastMessaged.isEmpty() && lastMessaged.containsKey(sender) && lastMessaged.get(sender) != null
                            && ((lastMessaged.get(sender) instanceof Player player && player.isOnline()) || lastMessaged.get(sender) instanceof ConsoleCommandSender)) {
                        sendPrivateMessage(sender, lastMessaged.get(sender), c.get("message"));
                    } else {
                        sender.sendMessage(Component.text("Noone to reply to found..", NamedTextColor.RED));
                    }
                }));
        manager.command(manager.commandBuilder("stellraw")
                .permission("skyprisoncore.command.stellraw")
                .required("message", greedyStringParser())
                .handler(c -> {
                    String msg = c.get("message");
                    Component fMsg = plugin.getParsedString(c.sender(), "chat", msg);
                    plugin.getServer().sendMessage(fMsg);
                }));

        Command.Builder<CommandSender> nameColour = manager.commandBuilder("namecolour", "namecolor", "multicolour", "multicolor", "nc")
                .permission("skyprisoncore.command.namecolour")
                .handler(c -> sendNameColourHelp(c.sender()));
        manager.command(nameColour);

        Command.Builder<Player> remCol = nameColour.literal("remove")
                .senderType(Player.class)
                .handler(c -> {
                    Player player = c.sender();
                    removeNameColour(player, player.getUniqueId(), false);
                });
        manager.command(remCol);
        manager.command(remCol.permission("skyprisoncore.command.namecolour.other")
                .required("player", stringParser())
                .handler(c -> {
                    CommandSender sender = c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                        return;
                    }
                    removeNameColour(sender, targetId, true);
                }));

        manager.command(nameColour.required("colour/name", greedyFlagYieldingStringParser(), SuggestionProvider.suggestingStrings(getColours()))
                .flag(manager.flagBuilder("player").withAliases("p").withComponent(stringParser()).withPermission(Permission.of("skyprisoncore.command.namecolour.other")))
                .handler(c -> {
                    CommandSender sender = c.sender();
                    boolean isOther = c.flags().contains("player");

                    if(!isOther && !(sender instanceof Player)) {
                        sender.sendMessage(Component.text("You need to specify a player!", NamedTextColor.RED));
                        return;
                    }
                    UUID targetId;
                    if(isOther) {
                        targetId = PlayerManager.getPlayerId(c.flags().get("player"));
                    } else targetId = ((Player) sender).getUniqueId();

                    if(targetId == null) {
                        sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                        return;
                    }

                    String colName = c.get("colour/name");

                    if(colName.startsWith("<") && colName.endsWith(">")) {
                        setNameColour(sender, targetId, colName, isOther);
                        return;
                    }
                    if(!sender.hasPermission("skyprisoncore.command.namecolour.multicolour")) {
                        sender.sendMessage(Component.text("You don't have permission to use multicolour!", NamedTextColor.RED));
                        return;
                    }
                    setMultiColour(sender, targetId, colName, isOther);
                }));
    }
    private List<String> getColours() {
        List<String> namedCols = new ArrayList<>(NamedTextColor.NAMES.keys().stream().toList());
        namedCols.replaceAll(s -> "<" + s + ">");
        return new ArrayList<>(namedCols);
    }
    private void removeNameColour(CommandSender sender, UUID player, boolean isOther) {
        Player target = Bukkit.getPlayer(player);
        if(target != null) {
            target.customName(null);
        } else {
            NotificationsUtils.scheduleForOnline(player, "namecolour", "remove");
        }
        sender.sendMessage(Component.text("Successfully removed the name color", TextColor.fromHexString("#87fdd2")));
        if(isOther) {
            Component colourChange = Component.text("Your name colour was removed by ", TextColor.fromHexString("#87fdd2"))
                    .append(sender.name().decorate(TextDecoration.BOLD)).append(Component.text("!", TextColor.fromHexString("#87fdd2")));
            PlayerManager.sendMessage(player, colourChange, "namecolour-update");
        }
    }
    private void setMultiColour(CommandSender sender, UUID player, String colouredName, boolean isOther) {
        Player target = Bukkit.getPlayer(player);
        String playerName = target != null && target.isOnline() ? target.getName() : PlayerManager.getPlayerName(player);
        if(playerName == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }
        Component nickName = plugin.getParsedString(sender, "namecolour", colouredName);
        String plainName = MiniMessage.miniMessage().stripTags(colouredName);
        Bukkit.getLogger().info(plainName);
        Bukkit.getLogger().info(playerName);
        if(!sender.hasPermission("skyprisoncore.command.namecolour.nickname") && !plainName.equalsIgnoreCase(playerName)) {
            sender.sendMessage(Component.text("That doesn't match your name!", NamedTextColor.RED));
            return;
        }
        setName(sender, player, nickName, isOther);
    }
    private void setNameColour(CommandSender sender, UUID player, String colour, boolean isOther) {
        Component nickName = MiniMessage.miniMessage().deserialize(colour);
        if(!nickName.hasStyling()) {
            sender.sendMessage(Component.text("Invalid colour! For available colours, see /namecolour", NamedTextColor.RED));
            return;
        }
        Player target = Bukkit.getPlayer(player);
        String playerName = target != null && target.isOnline() ? target.getName() : PlayerManager.getPlayerName(player);
        if(playerName == null) {
            sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
            return;
        }
        setName(sender, player, Component.text(playerName).style(nickName.style()), isOther);
    }
    private void setName(CommandSender sender, UUID player, Component nickName, boolean isOther) {
        Player target = Bukkit.getPlayer(player);
        if(target != null) {
            target.customName(nickName);
        } else {
            NotificationsUtils.scheduleForOnline(player, "namecolour", GsonComponentSerializer.gson().serialize(nickName));
        }

        sender.sendMessage(Component.text("Successfully changed the name color to ", TextColor.fromHexString("#87fdd2")).append(nickName.colorIfAbsent(NamedTextColor.WHITE)));
        if(isOther) {
            Component colourChange = Component.text("Your name colour was changed by ", TextColor.fromHexString("#87fdd2"))
                    .append(sender.name().decorate(TextDecoration.BOLD)).append(Component.text(" to ", TextColor.fromHexString("#87fdd2"))).append(nickName.colorIfAbsent(NamedTextColor.WHITE));
            PlayerManager.sendMessage(player, colourChange, "namecolour-update");
        }
    }

    private void sendNameColourHelp(CommandSender sender) {
        Component help = Component.empty();
        help = help.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Name Colouring ", TextColor.fromHexString("#03b09c"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        help = help.appendNewline().append(MiniMessage.miniMessage().deserialize(
                "<#87fdd2>The named colours available are: \n<black>\\<black></black>, <dark_blue>\\<dark_blue></dark_blue>, " +
                "<dark_green>\\<dark_green></dark_green>, <dark_aqua>\\<dark_aqua></dark_aqua>, <dark_red>\\<dark_red></dark_red>, <dark_purple>\\<dark_purple></dark_purple>, " +
                "<gold>\\<gold></gold>, <gray>\\<gray></gray>, <dark_gray>\\<dark_gray></dark_gray>, <blue>\\<blue></blue>, " +
                "<green>\\<green></green>, <aqua>\\<aqua></aqua>, <red>\\<red></red>, <light_purple>\\<light_purple></light_purple>, <yellow>\\<yellow></yellow>, " +
                "<white>\\<white></white>"));

        help = help.append(MiniMessage.miniMessage().deserialize("""
                
                <gray><st>           </st>
                <#87fdd2>Hex colours are available by using <#568ac2><b><hex> \\<#87fdd2></b>"""));
        if (sender.hasPermission("skyprisoncore.command.namecolour.multicolour")) {
            help = help.append(MiniMessage.miniMessage().deserialize("""

                    <gray><st>           </st>
                    <#87fdd2>Gradients are available by using: <#568ac2><b>\\<gradient:colour1:colour2:colour1000></b>
                    
                    <#87fdd2>Example: <#568ac2><b>\\<gradient:red:#87fdd2></b>
                    <red><italic>Note: You can specify as many colours as you want!</italic>
                    
                    <#87fdd2>You can also use <#568ac2><b>\\<rainbow></b></#568ac2> to easily get a rainbow gradient!
                    <gray><st>           </st>
                    <#87fdd2>Example: <#568ac2><b>/namecolour \\<red>Drake\\<#5ee45e>Pork</b>
                    <gray><st>           </st>
                    <red><b>/namecolour (coloured name / remove)"""));
        } else {
            help = help.append(MiniMessage.miniMessage().deserialize("""

                    <#87fdd2>Example: <b>/namecolour <red>\\<red></b>
                    <gray><st>           </st>
                    <red><b>/namecolour (colour / remove)"""));
        }
        sender.sendMessage(help);
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
