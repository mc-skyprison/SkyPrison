package net.skyprison.skyprisoncore.Commands.economy;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.inject.Inject;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.brcdev.shopgui.exception.player.PlayerDataNotLoadedException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PermShop implements CommandExecutor {
	private SkyPrisonCore plugin;

	@Inject
	public PermShop(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 1) {
				String shop = args[0];
				if(ShopGuiPlusApi.getShop(shop) != null) {
					if (player.hasPermission("shopguiplus.shops." + shop) || player.isOp()) {
						try {
							ShopGuiPlusApi.openShop(player, shop, 1);
						} catch (PlayerDataNotLoadedException e) {
							e.printStackTrace();
						}
					} else {
						if (player.hasPermission("group.free")) {
							player.sendMessage(ChatColor.RED + "You can't use prison shops!");
						} else {
							if (shop.equalsIgnoreCase("center")) {
								player.sendMessage(ChatColor.RED + "You must be Desert+ to use this shop!");
							} else {
								player.sendMessage(ChatColor.RED + "You must be Free+ to use this shop!");
							}
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "Not a valid shop!");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Wrong Usage!");
			}
		}
		return true;
	}
}
