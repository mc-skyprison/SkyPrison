package net.skyprison.skyprisoncore.commands.chats;

import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Build implements CommandExecutor {
	private final ChatUtils chatUtils;

	public Build(ChatUtils chatUtils) {
		this.chatUtils = chatUtils;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "build", "800885673732997121");
		} else {
			chatUtils.consoleChatSend(args, "build", "800885673732997121");
		}
		return true;
	}
}
