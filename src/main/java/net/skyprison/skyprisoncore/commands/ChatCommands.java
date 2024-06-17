package net.skyprison.skyprisoncore.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.misc.Ignore;
import net.skyprison.skyprisoncore.inventories.misc.IgnoreEdit;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.javacord.api.DiscordApi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;
import static net.skyprison.skyprisoncore.SkyPrisonCore.lastMessaged;
import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.StringParser.*;

public class ChatCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSourceStack> manager;
    private final ChatUtils chatUtils;
    private final DatabaseHook db;

    public ChatCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSourceStack> manager, DiscordApi discApi, DatabaseHook db) {
        this.plugin = plugin;
        this.manager = manager;
        this.db = db;
        createChatCommands();
        this.chatUtils = new ChatUtils(plugin, discApi);
    }

    private void staffMessageHandler(String msg, CommandSender sender, String chatId) {
        if(!msg.isEmpty()) {
            chatUtils.sendPrivateMessage(msg, sender instanceof Player player ? player.getName() : "Console", chatId);
        } else {
            if(sender instanceof Player player) {
                chatUtils.stickyChatCheck(player, chatId);
            } else {
                sender.sendMessage(text("You need to specify a message!", RED));
            }
        }
    }

    private void createChatCommands() {
        manager.command(manager.commandBuilder("b")
                .permission("skyprisoncore.command.build")
                .optional("message", greedyStringParser())
                .handler(c -> staffMessageHandler(c.getOrDefault("message", ""), c.sender().getSender(), "build"))
                .build()
        );
        manager.command(manager.commandBuilder("admin", "a", "y")
                .permission("skyprisoncore.command.admin")
                .optional("message", greedyStringParser())
                .handler(c -> staffMessageHandler(c.getOrDefault("message", ""), c.sender().getSender(), "admin"))
                .build()
        );
        manager.command(manager.commandBuilder("g")
                .permission("skyprisoncore.command.guard")
                .optional("message", greedyStringParser())
                .handler(c -> staffMessageHandler(c.getOrDefault("message", ""), c.sender().getSender(), "guard"))
                .build()
        );
        manager.command(manager.commandBuilder("s")
                .permission("skyprisoncore.command.staff")
                .optional("message", greedyStringParser())
                .handler(c -> staffMessageHandler(c.getOrDefault("message", ""), c.sender().getSender(), "staff"))
                .build()
        );

        manager.command(manager.commandBuilder("msg")
                .permission("skyprisoncore.command.msg")
                .required("player", playerParser())
                .required("message", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    final Player player = c.get("player");
                    final String message = c.get("message");
                    sendPrivateMessage(sender, player, message);
                }));
        manager.command(manager.commandBuilder("reply", "r")
                .permission("skyprisoncore.command.reply")
                .required("message", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    if(!lastMessaged.isEmpty() && lastMessaged.containsKey(sender) && lastMessaged.get(sender) != null
                            && ((lastMessaged.get(sender) instanceof Player player && player.isOnline()) || lastMessaged.get(sender) instanceof ConsoleCommandSender)) {
                        sendPrivateMessage(sender, lastMessaged.get(sender), c.get("message"));
                    } else {
                        sender.sendMessage(text("Noone to reply to found..", RED));
                    }
                }));
        manager.command(manager.commandBuilder("stellraw")
                .permission("skyprisoncore.command.stellraw")
                .required("message", greedyStringParser())
                .handler(c -> {
                    String msg = c.get("message");
                    Component fMsg = plugin.getParsedString(c.sender().getSender(), "chat", msg);
                    plugin.getServer().sendMessage(fMsg);
                }));

        Command.Builder<CommandSourceStack> nameColour = manager.commandBuilder("namecolour", "namecolor", "multicolour", "multicolor", "nc")
                .permission("skyprisoncore.command.namecolour")
                .handler(c -> sendNameColourHelp(c.sender().getSender()));
        manager.command(nameColour);

        Command.Builder<CommandSourceStack> remCol = nameColour.literal("remove")
                .handler(c -> {
                    Player player = (Player) c.sender().getSender();
                    removeNameColour(player, player.getUniqueId(), false);
                });
        manager.command(remCol);
        manager.command(remCol.permission("skyprisoncore.command.namecolour.other")
                .required("player", stringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        sender.sendMessage(text("Player not found!", RED));
                        return;
                    }
                    removeNameColour(sender, targetId, true);
                }));

        manager.command(nameColour.required("colour/name", greedyFlagYieldingStringParser(), SuggestionProvider.suggestingStrings(getColours()))
                .flag(manager.flagBuilder("player").withAliases("p").withComponent(stringParser()).withPermission(Permission.of("skyprisoncore.command.namecolour.other")))
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    boolean isOther = c.flags().contains("player");

                    if(!isOther && !(sender instanceof Player)) {
                        sender.sendMessage(text("You need to specify a player!", RED));
                        return;
                    }
                    UUID targetId;
                    if(isOther) {
                        targetId = PlayerManager.getPlayerId(c.flags().get("player"));
                    } else targetId = ((Player) sender).getUniqueId();

                    if(targetId == null) {
                        sender.sendMessage(text("Player not found!", RED));
                        return;
                    }

                    String colName = c.get("colour/name");

                    if(colName.startsWith("<") && colName.endsWith(">")) {
                        setNameColour(sender, targetId, colName, isOther);
                        return;
                    }
                    if(!sender.hasPermission("skyprisoncore.command.namecolour.multicolour")) {
                        sender.sendMessage(text("You don't have permission to use multicolour!", RED));
                        return;
                    }
                    setMultiColour(sender, targetId, colName, isOther);
                }));

        manager.command(manager.commandBuilder("ignore")
                .permission("skyprisoncore.command.ignore")
                .optional("player", stringParser(), DefaultValue.constant(""))
                .handler(c -> {
                    Player player = (Player) c.sender().getSender();
                    String target = c.get("player");
                    if(target.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new Ignore(player).getInventory()));
                        return;
                    }
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(text("Player not found!", RED));
                        return;
                    }
                    if(targetId.equals(player.getUniqueId())) {
                        player.sendMessage(text("You can't ignore yourself!", RED));
                        return;
                    }
                    PlayerManager.Ignore ignore = PlayerManager.getPlayerIgnore(player.getUniqueId(), targetId);
                    if(ignore == null) {
                        ignore = new PlayerManager.Ignore(player.getUniqueId(), targetId, false, false);
                        PlayerManager.addPlayerIgnores(ignore);
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                                "INSERT INTO user_ignores (user_id, ignored_id) VALUES (?, ?)")) {
                            ps.setString(1, player.getUniqueId().toString());
                            ps.setString(2, ignore.targetId().toString());
                            ps.executeUpdate();
                            player.sendMessage(text("Successfully added ", NamedTextColor.GREEN)
                                    .append(text(target, NamedTextColor.GREEN, BOLD))
                                    .append(text(" to /ignore! Opening ignore options..", NamedTextColor.GREEN)));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                    PlayerManager.Ignore finalIgnore = ignore;
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new IgnoreEdit(player, finalIgnore, db).getInventory()));
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
        sender.sendMessage(text("Successfully removed the name color", TextColor.fromHexString("#87fdd2")));
        if(isOther) {
            Component colourChange = text("Your name colour was removed by ", TextColor.fromHexString("#87fdd2"))
                    .append(sender.name().decorate(BOLD)).append(text("!", TextColor.fromHexString("#87fdd2")));
            PlayerManager.sendMessage(player, colourChange, "namecolour-update");
        }
    }
    private void setMultiColour(CommandSender sender, UUID player, String colouredName, boolean isOther) {
        Player target = Bukkit.getPlayer(player);
        String playerName = target != null && target.isOnline() ? target.getName() : PlayerManager.getPlayerName(player);
        if(playerName == null) {
            sender.sendMessage(text("Player not found!", RED));
            return;
        }
        Component nickName = plugin.getParsedString(sender, "namecolour", colouredName);
        String plainName = MiniMessage.miniMessage().stripTags(colouredName);
        if(!sender.hasPermission("skyprisoncore.command.namecolour.nickname") && !plainName.equalsIgnoreCase(playerName)) {
            sender.sendMessage(text("That doesn't match your name!", RED));
            return;
        }
        setName(sender, player, nickName, isOther);
    }
    private void setNameColour(CommandSender sender, UUID player, String colour, boolean isOther) {
        Component nickName = MiniMessage.miniMessage().deserialize(colour);
        if(!nickName.hasStyling()) {
            sender.sendMessage(text("Invalid colour! For available colours, see /namecolour", RED));
            return;
        }
        Player target = Bukkit.getPlayer(player);
        String playerName = target != null && target.isOnline() ? target.getName() : PlayerManager.getPlayerName(player);
        if(playerName == null) {
            sender.sendMessage(text("Player not found!", RED));
            return;
        }
        setName(sender, player, text(playerName).style(nickName.style()), isOther);
    }
    private void setName(CommandSender sender, UUID player, Component nickName, boolean isOther) {
        Player target = Bukkit.getPlayer(player);
        if(target != null) {
            target.customName(nickName);
        } else {
            NotificationsUtils.scheduleForOnline(player, "namecolour", GsonComponentSerializer.gson().serialize(nickName));
        }

        sender.sendMessage(text("Successfully changed the name color to ", TextColor.fromHexString("#87fdd2")).append(nickName.colorIfAbsent(NamedTextColor.WHITE)));
        if(isOther) {
            Component colourChange = text("Your name colour was changed by ", TextColor.fromHexString("#87fdd2"))
                    .append(sender.name().decorate(BOLD)).append(text(" to ", TextColor.fromHexString("#87fdd2"))).append(nickName.colorIfAbsent(NamedTextColor.WHITE));
            PlayerManager.sendMessage(player, colourChange, "namecolour-update");
        }
    }

    private void sendNameColourHelp(CommandSender sender) {
        Component help = Component.empty();
        help = help.append(text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(text(" Name Colouring ", TextColor.fromHexString("#03b09c"), BOLD))
                .append(text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
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
        boolean isIgnored = false;
        if(sender instanceof Player sPlayer && receiver instanceof Player rPlayer) {
            PlayerManager.Ignore ignoring = PlayerManager.getPlayerIgnore(sPlayer.getUniqueId(), rPlayer.getUniqueId());
            if(ignoring != null && ignoring.ignorePrivate()) {
                sender.sendMessage(text("Can't message players you're ignoring!", RED));
                return;
            }
            PlayerManager.Ignore ignored = PlayerManager.getPlayerIgnore(rPlayer.getUniqueId(), sPlayer.getUniqueId());
            if(ignored != null && ignored.ignorePrivate()) {
                isIgnored = true;
            }
        }

        Component senderName = sender.name();
        Component pMsg = Component.empty().append(text(" » ", TextColor.fromHexString("#940b34")))
                .append(plugin.getParsedString(sender, "private", message).colorIfAbsent(NamedTextColor.GRAY));
        if (sender instanceof Player toPlayer) {
            Component customName = toPlayer.customName();
            if (customName != null) senderName = customName;
        }

        Component receiverName = text("Unknown");
        if(receiver instanceof Player player) {
            receiverName = Objects.requireNonNullElse(player.customName(), player.displayName());
        } else if(receiver instanceof CommandSender receiving) {
            receiverName = receiving.name();
        }

        Component msgTo = Component.empty().append(text("Me", TextColor.fromHexString("#f02d68"))).append(text(" ⇒ ", TextColor.fromHexString("#940b34")))
                .append(receiverName.colorIfAbsent(TextColor.fromHexString("#f02d68")));
        Component msgFrom = Component.empty().append(senderName.colorIfAbsent(TextColor.fromHexString("#f02d68"))).append(text(" ⇒ ", TextColor.fromHexString("#940b34")))
                .append(text("Me", TextColor.fromHexString("#f02d68")));

        sender.sendMessage(msgTo.append(pMsg));
        if(!isIgnored) {
            receiver.sendMessage(msgFrom.append(pMsg));
        }

        if (lastMessaged.isEmpty() || !lastMessaged.containsKey(sender) || !lastMessaged.get(sender).equals(receiver)) {
            lastMessaged.put(sender, receiver);
        }
        if (!isIgnored && lastMessaged.isEmpty() || !lastMessaged.containsKey(receiver)) {
            lastMessaged.put(receiver, sender);
        }
    }
}
