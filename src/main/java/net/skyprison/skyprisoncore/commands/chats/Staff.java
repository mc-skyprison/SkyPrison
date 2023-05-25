package net.skyprison.skyprisoncore.commands.chats;

import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Staff implements CommandExecutor {
	private final ChatUtils chatUtils;

	public Staff(ChatUtils chatUtils) {
		this.chatUtils = chatUtils;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "staff", "791054076787163166");
		} else {
			chatUtils.consoleChatSend(args, "staff", "791054076787163166");
		}
		return true;
	}
}
