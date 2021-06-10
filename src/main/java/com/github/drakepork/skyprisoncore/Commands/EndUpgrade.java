package com.github.drakepork.skyprisoncore.Commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EndUpgrade implements CommandExecutor {
	private Core plugin;

	@Inject
	public EndUpgrade(Core plugin) {
		this.plugin = plugin;
	}

	public void openGUI(Player player, Boolean enchTransfer, Boolean repairReset) {

		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Inventory endUpgradeGUI = Bukkit.createInventory(null, 54, ChatColor.RED + "End Upgrade Shop");
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		whiteMeta.setDisplayName(" ");
		whitePane.setItemMeta(whiteMeta);

		int totalCost = 0;
		if(repairReset) {
			totalCost += 10;
		}
		if(enchTransfer) {
			totalCost += 10;
		}
		switch(player.getInventory().getItemInMainHand().getType().toString()) {
			case "DIAMOND_AXE":
				totalCost += 10;
				break;
			case "DIAMOND_PICKAXE":
				totalCost += 10;
				break;
			case "DIAMOND_SHOVEL":
				totalCost += 10;
				break;
			case "DIAMOND_HOE":
				totalCost += 10;
				break;
			case "DIAMOND_HELMET":
				totalCost += 10;
				break;
			case "DIAMOND_CHESTPLATE":
				totalCost += 10;
				break;
			case "DIAMOND_LEGGINGS":
				totalCost += 10;
				break;
			case "DIAMOND_BOOTS":
				totalCost += 10;
				break;
		}

		for (int i = 0; i < 54; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				whiteMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				whiteMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "endupgrade");
				whitePane.setItemMeta(whiteMeta);
				endUpgradeGUI.setItem(i, whitePane);
			} else if (i == 13) {
				endUpgradeGUI.setItem(i, player.getInventory().getItemInMainHand());
			} else if (i == 20) {
				ItemStack transferEnch = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta enchMeta = transferEnch.getItemMeta();
				ArrayList lore = new ArrayList();
				NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
				lore.add(plugin.colourMessage("&7Cost: &a$"));
				lore.add(plugin.colourMessage("&8-------"));
				if(enchTransfer) {
					enchMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 1);
					lore.add(plugin.colourMessage("&a&lEnabled"));
				} else {
					enchMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 0);
					lore.add(plugin.colourMessage("&c&lDisabled"));
				}
				enchMeta.setDisplayName(plugin.colourMessage("&e&lTransfer Enchants"));
				enchMeta.setLore(lore);
				transferEnch.setItemMeta(enchMeta);
				endUpgradeGUI.setItem(i, transferEnch);
			} else if (i == 24) {
				ItemStack repairCost = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta repMeta = repairCost.getItemMeta();
				ArrayList lore = new ArrayList();
				NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
				lore.add(plugin.colourMessage("&7Cost: &a$"));
				lore.add(plugin.colourMessage("&8-------"));
				if(repairReset) {
					repMeta.getPersistentDataContainer().set(repKey, PersistentDataType.INTEGER, 1);
					lore.add(plugin.colourMessage("&a&lEnabled"));
				} else {
					repMeta.getPersistentDataContainer().set(repKey, PersistentDataType.INTEGER, 0);
					lore.add(plugin.colourMessage("&c&lDisabled"));
				}
				repMeta.setDisplayName(plugin.colourMessage("&e&lReset Repair Cost"));
				repMeta.setLore(lore);
				repairCost.setItemMeta(repMeta);
				endUpgradeGUI.setItem(i, repairCost);
			} else if (i == 31) {
				if(user.getBalance() >= totalCost) {
					ItemStack confirmUpgrade = new ItemStack(Material.EMERALD_BLOCK);
					ArrayList lore = new ArrayList();
					ItemMeta confirmMeta = confirmUpgrade.getItemMeta();
					confirmMeta.setDisplayName(plugin.colourMessage("&3&lUpgrade Item"));
					lore.add(plugin.colourMessage("&7Total Cost: &a$" + totalCost));
					confirmUpgrade.setItemMeta(confirmMeta);
					endUpgradeGUI.setItem(i, confirmUpgrade);
				} else {
					ItemStack confirmUpgrade = new ItemStack(Material.REDSTONE_BLOCK);
					ArrayList lore = new ArrayList();
					ItemMeta confirmMeta = confirmUpgrade.getItemMeta();
					confirmMeta.setDisplayName(plugin.colourMessage("&3&lUpgrade Item"));
					lore.add(plugin.colourMessage("&7Total Cost: &a$" + totalCost));
					lore.add(plugin.colourMessage("&8-------"));
					lore.add(plugin.colourMessage("&cYou can't afford this!"));
					confirmUpgrade.setItemMeta(confirmMeta);
					endUpgradeGUI.setItem(i, confirmUpgrade);
				}
			} else if(i == 49) {
				ItemStack balance = new ItemStack(Material.NETHER_STAR);
				ItemMeta bMeta = balance.getItemMeta();
				bMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Your Balance");
				bMeta.setLore(Arrays.asList(ChatColor.GRAY + "" + PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%")));
				balance.setItemMeta(bMeta);
				endUpgradeGUI.setItem(i, balance);
			} else {
				endUpgradeGUI.setItem(i, whitePane);
			}
		}
		player.openInventory(endUpgradeGUI);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(player.hasPermission("skyprisoncore.command.endupgrade.quest-complete")) {
				List upItems = Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
						Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_SHOVEL);
				ItemStack item = player.getInventory().getItemInMainHand();
				Material iMat = item.getType();
				if (upItems.contains(iMat)) {
					openGUI(player, false, false);
				} else {
					player.sendMessage(plugin.colourMessage("&cYou can't upgrade this item!"));
				}
			} else {
				player.sendMessage(plugin.colourMessage("&cYou need to finish the Blacksmith quest before you can use this shop!"));
			}
		}
		return true;
	}
}
