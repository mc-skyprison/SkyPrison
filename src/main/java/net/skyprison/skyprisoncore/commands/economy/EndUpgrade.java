package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EndUpgrade implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public EndUpgrade(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public int itemCost(Material itemType) {
		int cost = 0;
		switch (itemType.toString()) {
			case "DIAMOND_AXE", "DIAMOND_PICKAXE" -> cost += 300000;
			case "DIAMOND_SHOVEL" -> cost += 100000;
			case "DIAMOND_HOE" -> cost += 200000;
			case "DIAMOND_HELMET" -> cost += 500000;
			case "DIAMOND_CHESTPLATE" -> cost += 800000;
			case "DIAMOND_LEGGINGS" -> cost += 700000;
			case "DIAMOND_BOOTS" -> cost += 400000;
		}
		return cost;
	}

	public int upgradeCost(Player player, Boolean enchTransfer, Boolean repairReset) {
		int totalCost = 0;

		if(enchTransfer) {
			totalCost += 100000;
		}
		if(repairReset) {
			totalCost += 350000;
		}
		totalCost += itemCost(player.getInventory().getItemInMainHand().getType());
		return totalCost;
	}


	public void confirmGUI(Player player, Boolean enchTransfer, Boolean repairReset) {
		Inventory confirmGUI = Bukkit.createInventory(null, 27, Component.text("Confirm Upgrade", NamedTextColor.RED));

		int totalCost = upgradeCost(player, enchTransfer, repairReset);

		ItemStack confirmButton = new ItemStack(Material.GREEN_CONCRETE);
		ItemMeta confirmMeta = confirmButton.getItemMeta();
		confirmMeta.displayName(Component.text("Confirm Upgrade", NamedTextColor.GREEN, TextDecoration.BOLD)
				.decoration(TextDecoration.ITALIC, false));

		ArrayList<Component> lore = new ArrayList<>();
		if (!player.hasPermission("skyprisoncore.command.endupgrade.first-time")) {
			lore.add(Component.text("Total Cost: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(totalCost), NamedTextColor.GREEN))
					.decoration(TextDecoration.ITALIC, false));
		} else {
			lore.add(Component.text("Total Cost: ", NamedTextColor.GRAY).append(Component.text("FREE", NamedTextColor.GREEN))
					.decoration(TextDecoration.ITALIC, false));
		}

		NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
		NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");

		if(enchTransfer) {
			confirmMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 1);
			lore.add(Component.text("keeping enchants is ", NamedTextColor.GRAY).append(Component.text("ENABLED", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
					.decoration(TextDecoration.ITALIC, false));
		} else {
			confirmMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 0);
			lore.add(Component.text("Keeping enchants is ", NamedTextColor.GRAY).append(Component.text("NOT ENABLED", NamedTextColor.RED, TextDecoration.BOLD))
					.decoration(TextDecoration.ITALIC, false));
		}

		if(repairReset) {
			confirmMeta.getPersistentDataContainer().set(repKey, PersistentDataType.INTEGER, 1);
			lore.add(Component.text("Resetting Repair Cost is ", NamedTextColor.GRAY).append(Component.text("ENABLED", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
					.decoration(TextDecoration.ITALIC, false));
		} else {
			confirmMeta.getPersistentDataContainer().set(repKey, PersistentDataType.INTEGER, 0);
			lore.add(Component.text("Resetting Repair Cost is ", NamedTextColor.GRAY).append(Component.text("NOT ENABLED", NamedTextColor.RED, TextDecoration.BOLD))
					.decoration(TextDecoration.ITALIC, false));
		}

		confirmMeta.lore(lore);

		confirmButton.setItemMeta(confirmMeta);

		ItemStack cancelButton = new ItemStack(Material.RED_CONCRETE);
		ItemMeta cancelMeta = cancelButton.getItemMeta();
		cancelMeta.displayName(Component.text("Cancel Upgrade", NamedTextColor.RED, TextDecoration.BOLD)
				.decoration(TextDecoration.ITALIC, false));
		cancelButton.setItemMeta(cancelMeta);

		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		whiteMeta.displayName(Component.empty());
		whitePane.setItemMeta(whiteMeta);


		player.openInventory(confirmGUI);

		for(int i = 0; i < 27; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				whiteMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				whiteMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "confirm-endupgrade");
				whitePane.setItemMeta(whiteMeta);
				confirmGUI.setItem(i, whitePane);
			} else if(i == 4) {
				confirmGUI.setItem(i, player.getInventory().getItemInMainHand());
			} else if(i == 11) {
				confirmGUI.setItem(i, confirmButton);
			} else if(i == 15) {
				confirmGUI.setItem(i, cancelButton);
			} else {
				confirmGUI.setItem(i, whitePane);
			}
		}
	}

	public void netheriteGUI(Player player) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Inventory netheriteGUI = Bukkit.createInventory(null, 27, Component.text("End Upgrade Shop", NamedTextColor.RED));
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		whiteMeta.displayName(Component.empty());
		whitePane.setItemMeta(whiteMeta);
		ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta grayMeta = grayPane.getItemMeta();
		grayMeta.displayName(Component.empty());
		grayPane.setItemMeta(grayMeta);

		for (int i = 0; i < 27; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				grayMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				grayMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "netheriteupgrade");
				grayPane.setItemMeta(grayMeta);
				netheriteGUI.setItem(i, grayPane);
			} else if (i == 8 || i == 9 || i == 17 || i == 18 || i == 26) {
				netheriteGUI.setItem(i, grayPane);
			} else if(i == 11) {
				if(user.getBalance() >= 500000) {
					ItemStack confirmUpgrade = new ItemStack(Material.GREEN_CONCRETE);
					ArrayList<Component> lore = new ArrayList<>();
					ItemMeta confirmMeta = confirmUpgrade.getItemMeta();
					confirmMeta.displayName(Component.text("Reset Repair Cost", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("Total Cost: ", NamedTextColor.GRAY).append(Component.text("$500,000", NamedTextColor.GREEN))
							.decoration(TextDecoration.ITALIC, false));
					confirmMeta.lore(lore);
					confirmUpgrade.setItemMeta(confirmMeta);
					netheriteGUI.setItem(i, confirmUpgrade);
				} else {
					ItemStack confirmUpgrade = new ItemStack(Material.RED_CONCRETE);
					ArrayList<Component> lore = new ArrayList<>();
					ItemMeta confirmMeta = confirmUpgrade.getItemMeta();
					confirmMeta.displayName(Component.text("Reset Repair Cost", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("Total Cost: ", NamedTextColor.GRAY).append(Component.text("$500,000", NamedTextColor.GREEN))
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("-------", NamedTextColor.DARK_GRAY)
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("You can't afford this!", NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false));
					confirmMeta.lore(lore);
					confirmUpgrade.setItemMeta(confirmMeta);
					netheriteGUI.setItem(i, confirmUpgrade);
				}
			} else if(i == 13) {
				ItemStack hand = player.getInventory().getItemInMainHand();
				netheriteGUI.setItem(i, hand);
			} else if(i == 15) {
				ItemStack cancelUpgrade = new ItemStack(Material.RED_CONCRETE);
				ItemMeta cancelMeta = cancelUpgrade.getItemMeta();
				cancelMeta.displayName(Component.text("Cancel Repair Reset", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				cancelUpgrade.setItemMeta(cancelMeta);
				netheriteGUI.setItem(i, cancelUpgrade);
			} else {
				netheriteGUI.setItem(i, whitePane);
			}
		}
		player.openInventory(netheriteGUI);
	}

	public void openGUI(Player player, Boolean enchTransfer, Boolean repairReset) {

		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Inventory endUpgradeGUI = Bukkit.createInventory(null, 54, Component.text("End Upgrade Shop", NamedTextColor.RED));
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		whiteMeta.displayName(Component.empty());
		whitePane.setItemMeta(whiteMeta);

		int totalCost = upgradeCost(player, enchTransfer, repairReset);

		for (int i = 0; i < 54; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				whiteMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				whiteMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "endupgrade");
				whitePane.setItemMeta(whiteMeta);
				endUpgradeGUI.setItem(i, whitePane);
			} else if (i == 13) {
				ItemStack upgradeItem = new ItemStack(player.getInventory().getItemInMainHand().getType(), 1);
				ItemMeta upgradeMeta = upgradeItem.getItemMeta();
				upgradeMeta.lore(Arrays.asList(Component.text("Upgrade Cost: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(itemCost(upgradeItem.getType())), NamedTextColor.GREEN))
						.decoration(TextDecoration.ITALIC, false)
						, Component.text("-------", NamedTextColor.DARK_GRAY)
								.decoration(TextDecoration.ITALIC, false)));
				upgradeItem.setItemMeta(upgradeMeta);
				endUpgradeGUI.setItem(i, upgradeItem);
			} else if (i == 20) {
				ItemStack transferEnch = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta enchMeta = transferEnch.getItemMeta();
				ArrayList<Component> lore = new ArrayList<>();
				NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
				lore.add(Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text("$100,000", NamedTextColor.GREEN))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("-------", NamedTextColor.DARK_GRAY)
						.decoration(TextDecoration.ITALIC, false));
				if(enchTransfer) {
					enchMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 1);
					lore.add(Component.text("Enabled", NamedTextColor.GREEN, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
				} else {
					enchMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 0);
					lore.add(Component.text("Disabled", NamedTextColor.RED, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
				}
				enchMeta.displayName(Component.text("Transfer Enchants", NamedTextColor.YELLOW, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				enchMeta.lore(lore);
				transferEnch.setItemMeta(enchMeta);
				endUpgradeGUI.setItem(i, transferEnch);
			} else if (i == 24) {
				ItemStack repairCost = new ItemStack(Material.ENCHANTED_BOOK);
				ItemMeta repMeta = repairCost.getItemMeta();
				ArrayList<Component> lore = new ArrayList<>();
				NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
				lore.add(Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text("$350,000", NamedTextColor.GREEN))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("-------", NamedTextColor.DARK_GRAY)
						.decoration(TextDecoration.ITALIC, false));
				if(repairReset) {
					repMeta.getPersistentDataContainer().set(repKey, PersistentDataType.INTEGER, 1);
					lore.add(Component.text("Enabled", NamedTextColor.GREEN, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
				} else {
					repMeta.getPersistentDataContainer().set(repKey, PersistentDataType.INTEGER, 0);
					lore.add(Component.text("Disabled", NamedTextColor.RED, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
				}
				repMeta.displayName(Component.text("Reset Repair Cost", NamedTextColor.YELLOW, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				repMeta.lore(lore);
				repairCost.setItemMeta(repMeta);
				endUpgradeGUI.setItem(i, repairCost);
			} else if (i == 31) {
				if(user.getBalance() >= totalCost) {
					ItemStack confirmUpgrade = new ItemStack(Material.GREEN_CONCRETE);
					ArrayList<Component> lore = new ArrayList<>();
					ItemMeta confirmMeta = confirmUpgrade.getItemMeta();
					confirmMeta.displayName(Component.text("Upgrade Item", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("7Total Cost: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(totalCost), NamedTextColor.GREEN))
							.decoration(TextDecoration.ITALIC, false));
					confirmMeta.lore(lore);
					confirmUpgrade.setItemMeta(confirmMeta);
					endUpgradeGUI.setItem(i, confirmUpgrade);
				} else {
					ItemStack confirmUpgrade = new ItemStack(Material.RED_CONCRETE);
					ArrayList<Component> lore = new ArrayList<>();
					ItemMeta confirmMeta = confirmUpgrade.getItemMeta();
					confirmMeta.displayName(Component.text("Upgrade Item", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("7Total Cost: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(totalCost), NamedTextColor.GREEN))
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("-------", NamedTextColor.DARK_GRAY)
							.decoration(TextDecoration.ITALIC, false));
					lore.add(Component.text("You can't afford this!", NamedTextColor.RED)
							.decoration(TextDecoration.ITALIC, false));
					confirmMeta.lore(lore);
					confirmUpgrade.setItemMeta(confirmMeta);
					endUpgradeGUI.setItem(i, confirmUpgrade);
				}
			} else if(i == 45) {
				ItemStack info = new ItemStack(Material.BOOK);
				ItemMeta iMeta = info.getItemMeta();
				iMeta.displayName(Component.text("Usage Information", NamedTextColor.GREEN, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				ArrayList<Component> lore = new ArrayList<>();
				lore.add(Component.text("Transfer Enchants", NamedTextColor.GRAY, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Enabling this option will transfer", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("the current enchants of the item", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("when upgrading it to netherite.", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("---", NamedTextColor.DARK_GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.empty());
				lore.add(Component.text("Reset Repair Cost", NamedTextColor.GRAY, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Enabling this option will reset the", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("repair cost of the item, meaning you", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("can add more enchants or repair it in", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("an anvil if it previously said", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Too Expensive.", NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false));
				iMeta.lore(lore);
				info.setItemMeta(iMeta);
				endUpgradeGUI.setItem(i, info);
			}  else if(i == 49) {
				ItemStack balance = new ItemStack(Material.NETHER_STAR);
				ItemMeta bMeta = balance.getItemMeta();
				bMeta.displayName(Component.text("Your Balance", NamedTextColor.GOLD, TextDecoration.BOLD)
						.decoration(TextDecoration.ITALIC, false));
				bMeta.lore(List.of(Component.text(PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%"), NamedTextColor.GRAY)
						.decoration(TextDecoration.ITALIC, false)));
				balance.setItemMeta(bMeta);
				endUpgradeGUI.setItem(i, balance);
			} else {
				endUpgradeGUI.setItem(i, whitePane);
			}
		}
		player.openInventory(endUpgradeGUI);
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if(player.hasPermission("skyprisoncore.command.endupgrade.quest-complete")) {
				List<Material> upItems = Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
						Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_SHOVEL);
				ItemStack item = player.getInventory().getItemInMainHand();
				Material iMat = item.getType();
				if (upItems.contains(iMat)) {
					if(!player.hasPermission("skyprisoncore.command.endupgrade.first-time")) {
						openGUI(player, false, false);
					} else {
						confirmGUI(player, true, true);
					}
				} else {
					List<Material> nethItems = Arrays.asList(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
							Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_HOE, Material.NETHERITE_SHOVEL);
					if (nethItems.contains(iMat)) {
						netheriteGUI(player);
					} else {
						player.sendMessage(Component.text("You can't upgrade this item!", NamedTextColor.RED));
					}
				}
			} else {
				player.sendMessage(Component.text("You need to finish the Blacksmith quest before you can use this shop!", NamedTextColor.RED));
			}
		}
		return true;
	}
}
