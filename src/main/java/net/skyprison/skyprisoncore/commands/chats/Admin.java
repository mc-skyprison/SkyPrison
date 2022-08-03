package net.skyprison.skyprisoncore.commands.chats;

import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Admin implements CommandExecutor {
	private final ChatUtils chatUtils;

	public Admin(ChatUtils chatUtils) {
		this.chatUtils = chatUtils;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "admin", "791054229136605194");
		} else {
			chatUtils.consoleChatSend(args, "admin", "791054229136605194");
		}
		return true;
	}
}
