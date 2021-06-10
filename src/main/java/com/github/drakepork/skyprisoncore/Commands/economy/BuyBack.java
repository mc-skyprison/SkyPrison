package com.github.drakepork.skyprisoncore.Commands.economy;

import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;

public class BuyBack implements CommandExecutor {
	private Core plugin;

	@Inject
	public BuyBack(Core plugin) {
		this.plugin = plugin;
	}

	public void openGUI(Player player) {
		File f = new File(plugin.getDataFolder() + File.separator + "recentsells.yml");
		FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		Set<String> soldItems = yamlf.getConfigurationSection(player.getUniqueId().toString()).getKeys(false);
		Inventory bartenderGUI = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Buyback Shop");
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
				bartenderGUI.setItem(i, whitePane);
			} else if(i >= 17 && i <= 21 || i >= 23 && i <= 26 || i == 9) {
				bartenderGUI.setItem(i, grayPane);
			} else if(i >= 0 && i <= 8 || i == 10 || i == 16) {
				bartenderGUI.setItem(i, whitePane);
			} else if(i == 22) {
				ItemStack balance = new ItemStack(Material.NETHER_STAR);
				ItemMeta bMeta = balance.getItemMeta();
				bMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance");
				bMeta.setLore(Arrays.asList(ChatColor.GRAY + "" + PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%")));
				balance.setItemMeta(bMeta);
				bartenderGUI.setItem(i, balance);
			}
		}
		List<Integer> availableNums = new LinkedList(Arrays.asList(11, 12, 13, 14, 15));

		for (String soldItem : soldItems) {
			String itemType = yamlf.getString(player.getUniqueId().toString() + "." + soldItem + ".type");
			ItemStack iSold = new ItemStack(Material.getMaterial(itemType), 1);
			ItemMeta iSoldMeta = iSold.getItemMeta();
			List lore = new ArrayList();
			Double orgPrice = yamlf.getDouble(player.getUniqueId().toString() + "." + soldItem + ".price");
			Double newPrice = orgPrice * 3;
			String price = plugin.formatNumber(newPrice);
			// iSoldMeta.setDisplayName(plugin.colourMessage("&e&l" + iSoldMeta.getDisplayName()));
			int amount = yamlf.getInt(player.getUniqueId().toString() + "." + soldItem + ".amount");
			lore.add(plugin.colourMessage("&eAmount: &7" + amount));
			lore.add(plugin.colourMessage("&eCost: &7$" + price));
			iSoldMeta.setLore(lore);
			NamespacedKey key = new NamespacedKey(plugin, "sold-type");
			iSoldMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemType);
			NamespacedKey key1 = new NamespacedKey(plugin, "sold-amount");
			iSoldMeta.getPersistentDataContainer().set(key1, PersistentDataType.INTEGER, amount);
			NamespacedKey key2 = new NamespacedKey(plugin, "sold-price");
			iSoldMeta.getPersistentDataContainer().set(key2, PersistentDataType.DOUBLE, newPrice);
			NamespacedKey key3 = new NamespacedKey(plugin, "sold-pos");
			iSoldMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, soldItem);
			iSold.setItemMeta(iSoldMeta);
			bartenderGUI.setItem(availableNums.get(0), iSold);
			availableNums.remove(0);
		}

		player.openInventory(bartenderGUI);
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			File f = new File(plugin.getDataFolder() + File.separator + "recentsells.yml");
			FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
			if(yamlf.isConfigurationSection(player.getUniqueId().toString())) {
				openGUI(player);
			} else {
				player.sendMessage(plugin.colourMessage("&cYou havn't sold anything!"));
			}
		}
		return true;
	}
}
