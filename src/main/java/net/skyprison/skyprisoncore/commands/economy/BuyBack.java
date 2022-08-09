package net.skyprison.skyprisoncore.commands.economy;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class BuyBack implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook hook;

	public BuyBack(SkyPrisonCore plugin, DatabaseHook hook) {
		this.plugin = plugin;
		this.hook = hook;
	}

	public void openGUI(Player player) {
		List<String> soldItems = new ArrayList<>();

		try {
			Connection conn = hook.getSQLConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT recent_item, recent_amount, recent_price, recent_id FROM recent_sells WHERE user_id = '" + player.getUniqueId() + "'");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				soldItems.add(rs.getString(1) + "/" + rs.getInt(2) + "/" + rs.getFloat(3) + "/" + rs.getInt(4));
			}
			hook.close(ps, rs, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Inventory buyBackGUI = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Buyback Shop");
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		ItemMeta grayMeta = grayPane.getItemMeta();
		whiteMeta.setDisplayName(" ");
		whitePane.setItemMeta(whiteMeta);
		grayMeta.setDisplayName(" ");
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
				bMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance");
				bMeta.setLore(Collections.singletonList(ChatColor.GRAY + "" + PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%")));
				balance.setItemMeta(bMeta);
				buyBackGUI.setItem(i, balance);
			}
		}
		List<Integer> availableNums = new ArrayList<>(Arrays.asList(11, 12, 13, 14, 15));

		for (String soldItem : soldItems) {
			String[] soldSplit = soldItem.split("/");
			String itemType = soldSplit[0];
			if (Material.getMaterial(itemType) == null) break;
			ItemStack iSold = new ItemStack(Material.getMaterial(itemType), 1);
			ItemMeta iSoldMeta = iSold.getItemMeta();
			List<String> lore = new ArrayList<>();
			double orgPrice = Double.parseDouble(soldSplit[2]);
			double newPrice = orgPrice * 3;
			String price = plugin.formatNumber(newPrice);
			int amount = Integer.parseInt(soldSplit[1]);
			lore.add(plugin.colourMessage("&eAmount: &7" + amount));
			lore.add(plugin.colourMessage("&eCost: &7$" + price));
			iSoldMeta.setLore(lore);
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
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			openGUI(player);
		}
		return true;
	}
}
