package net.skyprison.skyprisoncore.commands;

import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import net.kyori.adventure.audience.Audience;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;

import java.util.stream.Collectors;

public class Chats {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSender> manager;
    private final ChatUtils chatUtils;
    public Chats(SkyPrisonCore plugin, PaperCommandManager<CommandSender> manager, DiscordApi discApi) {
        this.plugin = plugin;
        this.manager = manager;
        createPrivateChats();
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
    private void createPrivateChats() {
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
    }
}
