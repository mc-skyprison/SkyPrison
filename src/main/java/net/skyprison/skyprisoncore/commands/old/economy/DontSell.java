package net.skyprison.skyprisoncore.commands.old.economy;

import net.brcdev.shopgui.ShopGuiPlusApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DontSell implements CommandExecutor {
	private final DatabaseHook db;

	public DontSell(DatabaseHook db) {
		this.db = db;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if(player.hasPermission("shopguiplus.sell.all")) {
				List<String> blockedSales = new ArrayList<>();

				try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT block_item FROM block_sells WHERE user_id = '" + player.getUniqueId() + "'")) {
					ResultSet rs = ps.executeQuery();
					while(rs.next()) {
						blockedSales.add(rs.getString(1));
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("list")) {
						if(!blockedSales.isEmpty()) {
							Component blockMsg = Component.text("---=== ", NamedTextColor.AQUA).append(Component.text("Blocked Items", NamedTextColor.RED, TextDecoration.BOLD))
									.append(Component.text(" ===---", NamedTextColor.AQUA));
							for(String blockedSale : blockedSales) {
								blockMsg = blockMsg.append(Component.text("\n-", NamedTextColor.AQUA).append(Component.text(blockedSale, NamedTextColor.DARK_AQUA)));
							}
							player.sendMessage(blockMsg);
						} else {
							player.sendMessage(Component.text("You havn't blocked any items!", NamedTextColor.RED));
						}
					} else {
						if (Material.getMaterial(args[0].toUpperCase()) != null) {
							ItemStack item = new ItemStack(Objects.requireNonNull(Material.getMaterial(args[0].toUpperCase())), 1);
							if(ShopGuiPlusApi.getItemStackShopItem(item) != null) {
								String iName = item.getType().name();

								if (!blockedSales.contains(iName)) {
									player.sendMessage(Component.text("Successfully ", NamedTextColor.GREEN).append(Component.text("ADDED", NamedTextColor.GREEN, TextDecoration.BOLD))
											.append(Component.text("item to the dont sell list!", NamedTextColor.GREEN)));
								} else {
									player.sendMessage(Component.text("Successfully ", NamedTextColor.GREEN).append(Component.text("REMOVED", NamedTextColor.GREEN, TextDecoration.BOLD))
											.append(Component.text("item from the dont sell list!", NamedTextColor.GREEN)));
								}

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO block_sells (user_id, block_item) VALUES (?, ?)")) {
									ps.setString(1, player.getUniqueId().toString());
									ps.setString(2, iName);
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							} else {
								player.sendMessage(Component.text("This item can't be sold!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("That is not a valid item name!", NamedTextColor.RED));
						}
					}
				} else {
					ItemStack item = player.getInventory().getItemInMainHand();
					if (!item.getType().equals(Material.AIR)) {
						if(ShopGuiPlusApi.getItemStackShopItem(item) != null) {
							String iName = item.getType().name();

							if (!blockedSales.contains(iName)) {
								player.sendMessage(Component.text("Successfully ", NamedTextColor.GREEN).append(Component.text("ADDED", NamedTextColor.GREEN, TextDecoration.BOLD))
										.append(Component.text("item to the dont sell list!", NamedTextColor.GREEN)));

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO block_sells (user_id, block_item) VALUES (?, ?)")) {
									ps.setString(1, player.getUniqueId().toString());
									ps.setString(2, iName);
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							} else {
								player.sendMessage(Component.text("Successfully ", NamedTextColor.GREEN).append(Component.text("REMOVED", NamedTextColor.GREEN, TextDecoration.BOLD))
										.append(Component.text("item from the dont sell list!", NamedTextColor.GREEN)));

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM block_sells WHERE user_id = ? AND block_item = ?")) {
									ps.setString(1, player.getUniqueId().toString());
									ps.setString(2, iName);
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}

						} else {
							player.sendMessage(Component.text("This item can't be sold!", NamedTextColor.RED));
						}
					} else {
						player.sendMessage(Component.text("You're not holding any item!", NamedTextColor.RED));
					}
				}
			} else {
				player.sendMessage(Component.text("You need to have access to /sellall to use this command!", NamedTextColor.RED));
			}
		}

		return true;
	}
}
