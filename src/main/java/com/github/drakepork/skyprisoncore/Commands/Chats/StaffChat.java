package com.github.drakepork.skyprisoncore.Commands.Chats;

import com.github.drakepork.skyprisoncore.Core;
import com.github.drakepork.skyprisoncore.Utils.ChatUtils;
import com.google.inject.Inject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChat implements CommandExecutor {
	private Core plugin;

	@Inject
	public StaffChat(Core plugin) {
		this.plugin = plugin;
	}

	@Inject private ChatUtils chatUtils;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "staff", "staff-chat");
		} else {
			chatUtils.consoleChatSend(args, "staff", "staff-chat");
		}
		return true;
	}
}
