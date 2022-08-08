package net.skyprison.skyprisoncore.commands.chats;

import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Guard implements CommandExecutor {
	private final ChatUtils chatUtils;

	public Guard(ChatUtils chatUtils) {
		this.chatUtils = chatUtils;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			chatUtils.chatSendMessage(args, sender, "guard", "791054021338464266");
		} else {
			chatUtils.consoleChatSend(args, "guard", "791054021338464266");
		}
		return true;
	}
}
