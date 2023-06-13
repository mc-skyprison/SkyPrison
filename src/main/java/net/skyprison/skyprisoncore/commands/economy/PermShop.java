package net.skyprison.skyprisoncore.commands.economy;

import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PermShop implements CommandExecutor {

	public PermShop() {
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if (args.length == 1) {
				String shop = args[0];
				ShopGuiPlugin shopGUI = ShopGuiPlusApi.getPlugin();
				if(shopGUI.getShopManager().getShopById(shop) != null) {
					if (player.hasPermission("shopguiplus.shops." + shop) || player.isOp()) {
						shopGUI.getShopManager().openShopMenu(player, shop, 1, true);
					} else {
						if (player.hasPermission("group.free")) {
							player.sendMessage(Component.text("You can't use prison shops!", NamedTextColor.RED));
						} else {
							if (shop.equalsIgnoreCase("center")) {
								player.sendMessage(Component.text("You must be Desert+ to use this shop!", NamedTextColor.RED));
							} else {
								player.sendMessage(Component.text("You must be Free+ to use this shop!", NamedTextColor.RED));
							}
						}
					}
				} else {
					player.sendMessage(Component.text("Not a valid shop!", NamedTextColor.RED));
				}
			} else {
				player.sendMessage(Component.text("Wrong Usage!", NamedTextColor.RED));
			}
		}
		return true;
	}
}
