package com.github.drakepork.skyprisoncore.Commands.Chats;

import com.github.drakepork.skyprisoncore.Core;
import com.github.drakepork.skyprisoncore.Utils.ChatUtils;
import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardChat implements CommandExecutor {
    private Core plugin;

    @Inject
    public GuardChat(Core plugin) {
        this.plugin = plugin;
    }

    @Inject private ChatUtils chatUtils;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            chatUtils.chatSendMessage(args, sender, "guard", "guard-chat");
        } else {
            chatUtils.consoleChatSend(args, "guard", "guard-chat");
        }
        return true;
    }
}
