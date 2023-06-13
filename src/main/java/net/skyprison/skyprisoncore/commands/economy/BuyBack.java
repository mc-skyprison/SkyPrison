package net.skyprison.skyprisoncore.commands.economy;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BuyBack implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public BuyBack(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	public void openGUI(Player player) {
		List<String> soldItems = new ArrayList<>();

		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT recent_item, recent_amount, recent_price, recent_id FROM recent_sells WHERE user_id = ?")) {
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				soldItems.add(rs.getString(1) + "/" + rs.getInt(2) + "/" + rs.getFloat(3) + "/" + rs.getInt(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Inventory buyBackGUI = Bukkit.createInventory(null, 27, Component.text("Buyback Shop", NamedTextColor.GOLD));
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		ItemMeta grayMeta = grayPane.getItemMeta();
		whiteMeta.displayName(Component.empty());
		whitePane.setItemMeta(whiteMeta);
		grayMeta.displayName(Component.empty());
		grayPane.setItemMeta(grayMeta);
		for (int i = 0; i < 27; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				whiteMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				whiteMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "buyback");
				whitePane.setItemMeta(whiteMeta);
				buyBackGUI.setItem(i, whitePane);
			} else if(i >= 17 && i <= 21 || i >= 23 || i == 9) {
				buyBackGUI.setItem(i, grayPane);
			} else if(i <= 8 || i == 10 || i == 16) {
				buyBackGUI.setItem(i, whitePane);
			} else if(i == 22) {
				ItemStack balance = new ItemStack(Material.NETHER_STAR);
				ItemMeta bMeta = balance.getItemMeta();
				bMeta.displayName(Component.text("Your Balance", NamedTextColor.GOLD, TextDecoration.BOLD));
				bMeta.lore(Collections.singletonList(Component.text(PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%"), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
				balance.setItemMeta(bMeta);
				buyBackGUI.setItem(i, balance);
			}
		}
		List<Integer> availableNums = new ArrayList<>(Arrays.asList(11, 12, 13, 14, 15));

		for (String soldItem : soldItems) {
			String[] soldSplit = soldItem.split("/");
			String itemType = soldSplit[0];
			if (Material.getMaterial(itemType) == null) break;
			ItemStack iSold = new ItemStack(Objects.requireNonNull(Material.getMaterial(itemType)), 1);
			ItemMeta iSoldMeta = iSold.getItemMeta();
			List<Component> lore = new ArrayList<>();
			double orgPrice = Double.parseDouble(soldSplit[2]);
			double newPrice = orgPrice * 3;
			String price = plugin.formatNumber(newPrice);
			int amount = Integer.parseInt(soldSplit[1]);
			lore.add(Component.text("Amount: ", NamedTextColor.YELLOW).append(Component.text(amount, NamedTextColor.GRAY)));
			lore.add(Component.text("Cost: ", NamedTextColor.YELLOW).append(Component.text(price, NamedTextColor.GRAY)));
			iSoldMeta.lore(lore);
			NamespacedKey key = new NamespacedKey(plugin, "sold-type");
			iSoldMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemType);
			NamespacedKey key1 = new NamespacedKey(plugin, "sold-amount");
			iSoldMeta.getPersistentDataContainer().set(key1, PersistentDataType.INTEGER, amount);
			NamespacedKey key2 = new NamespacedKey(plugin, "sold-price");
			iSoldMeta.getPersistentDataContainer().set(key2, PersistentDataType.DOUBLE, newPrice);
			NamespacedKey key3 = new NamespacedKey(plugin, "sold-id");
			iSoldMeta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, Integer.parseInt(soldSplit[3]));
			iSold.setItemMeta(iSoldMeta);
			buyBackGUI.setItem(availableNums.get(0), iSold);
			availableNums.remove(0);
		}

		player.openInventory(buyBackGUI);
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			openGUI(player);
		}
		return true;
	}
}
