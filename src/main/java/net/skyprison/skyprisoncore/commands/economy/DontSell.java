package net.skyprison.skyprisoncore.commands.economy;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DontSell implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook hook;

	public DontSell(SkyPrisonCore plugin, DatabaseHook hook) {
		this.plugin = plugin;
		this.hook = hook;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(player.hasPermission("shopguiplus.sell.all")) {
				List<String> blockedSales = new ArrayList<>();

				try {
					Connection conn = hook.getSQLConnection();
					PreparedStatement ps = conn.prepareStatement("SELECT block_item FROM block_sells WHERE user_id = '" + player.getUniqueId() + "'");
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						blockedSales.add(rs.getString(1));
					}
					hook.close(ps, rs, conn);
				} catch (SQLException e) {
					e.printStackTrace();
				}



				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("list")) {
						if(!blockedSales.isEmpty()) {
							String blockedFormatted = "&b---=== &c&lBlocked Items &b===---";
							for(String blockedSale : blockedSales) {
								blockedFormatted += "\n&b- &3" + blockedSale;
							}
							player.sendMessage(plugin.colourMessage(blockedFormatted));
						} else {
							player.sendMessage(plugin.colourMessage("&cYou havn't blocked any items!"));
						}
					} else {
						if (Material.getMaterial(args[0].toUpperCase()) != null) {
							ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(args[0].toUpperCase())), 1);
							if(ShopGuiPlusApi.getItemStackShopItem(item) != null) {
								String iName = item.getType().name();

								if (!blockedSales.contains(iName)) {
									blockedSales.add(iName);
									player.sendMessage(plugin.colourMessage("&aSuccessfully &lADDED &aitem to the dont sell list!"));
								} else {
									blockedSales.remove(iName);
									player.sendMessage(plugin.colourMessage("&aSuccessfully &lREMOVED &aitem from the dont sell list!"));
								}
								String sql = "UPDATE users SET sell_blocks = ? WHERE user_id = ?";
								List<Object> params = new ArrayList<>() {{
									add(blockedSales);
									add(player.getUniqueId().toString());
								}};
								hook.sqlUpdate(sql, params);
							} else {
								player.sendMessage(plugin.colourMessage("&cThis item can't be sold!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cThat is not a valid item name!"));
						}
					}
				} else {
					ItemStack item = player.getInventory().getItemInMainHand();
					if (!item.getType().equals(Material.AIR)) {
						if(ShopGuiPlusApi.getItemStackShopItem(item) != null) {
							String iName = item.getType().name();

							if (!blockedSales.contains(iName)) {
								blockedSales.add(iName);
								player.sendMessage(plugin.colourMessage("&aSuccessfully &lADDED &aitem to the dont sell list!"));

								String sql = "INSERT INTO block_sells (user_id, block_item) VALUES (?, ?)";
								List<Object> params = new ArrayList<>() {{
									add(player.getUniqueId().toString());
									add(iName);
								}};
								hook.sqlUpdate(sql, params);
							} else {
								blockedSales.remove(iName);
								player.sendMessage(plugin.colourMessage("&aSuccessfully &lREMOVED &aitem from the dont sell list!"));

								String sql = "DELETE FROM block_sells WHERE user_id = ? AND block_item = ?";
								List<Object> params = new ArrayList<>() {{
									add(player.getUniqueId().toString());
									add(iName);
								}};
								hook.sqlUpdate(sql, params);
							}

						} else {
							player.sendMessage(plugin.colourMessage("&cThis item can't be sold!"));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cYou're not holding any item!"));
					}
				}
			} else {
				player.sendMessage(plugin.colourMessage("&cYou need to have access to /sellall to use this command!"));
			}
		}

		return true;
	}
}
