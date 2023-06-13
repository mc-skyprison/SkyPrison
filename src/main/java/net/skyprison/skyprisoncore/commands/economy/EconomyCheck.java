package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class EconomyCheck implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public EconomyCheck(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void openGUI(Player player, int page, String sortMethod) {
		LinkedHashMap<String, Integer> shopLogAmount = plugin.shopLogAmountPlayer.get(player.getUniqueId());
		LinkedHashMap<String, Double> shopLogPrice = plugin.shopLogPricePlayer.get(player.getUniqueId());
		LinkedHashMap<String, Integer> shopLogPage = plugin.shopLogPagePlayer.get(player.getUniqueId());

		ArrayList<Double> shopLogPriceList = new ArrayList<>();
		ArrayList<Integer> shopLogAmountList = new ArrayList<>();

		LinkedHashMap<String, Integer> shopLogAmountSortedTop = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> shopLogAmountSortedBottom = new LinkedHashMap<>();

		LinkedHashMap<String, Double> shopLogPriceSortedTop = new LinkedHashMap<>();
		LinkedHashMap<String, Double> shopLogPriceSortedBottom = new LinkedHashMap<>();


		double totalMoneyMade = 0.0;
		int totalItemSold = 0;
		for (HashMap.Entry<String, Integer> entry : shopLogAmount.entrySet()) {
			totalItemSold += entry.getValue();
			shopLogAmountList.add(entry.getValue());
		}
		shopLogAmountList.sort(Collections.reverseOrder());
		for (int num : shopLogAmountList) {
			for (HashMap.Entry<String, Integer> entry : shopLogAmount.entrySet()) {
				if (entry.getValue().equals(num)) {
					shopLogAmountSortedTop.put(entry.getKey(), num);
				}
			}
		}
		Collections.sort(shopLogAmountList);
		for (int num : shopLogAmountList) {
			for (HashMap.Entry<String, Integer> entry : shopLogAmount.entrySet()) {
				if (entry.getValue().equals(num)) {
					shopLogAmountSortedBottom.put(entry.getKey(), num);
				}
			}
		}
		Collections.sort(shopLogPriceList);
		for (double num : shopLogPriceList) {
			for (HashMap.Entry<String, Double> entry : shopLogPrice.entrySet()) {
				if (entry.getValue().equals(num)) {
					shopLogPriceSortedBottom.put(entry.getKey(), num);
				}
			}
		}

		for (HashMap.Entry<String, Double> entry : shopLogPrice.entrySet()) {
			totalMoneyMade += shopLogPrice.get(entry.getKey());
			shopLogPriceList.add(entry.getValue());
		}
		shopLogPriceList.sort(Collections.reverseOrder());
		for (double num : shopLogPriceList) {
			for (HashMap.Entry<String, Double> entry : shopLogPrice.entrySet()) {
				if (entry.getValue().equals(num)) {
					shopLogPriceSortedTop.put(entry.getKey(), num);
				}
			}
		}
		Collections.sort(shopLogPriceList);
		for (double num : shopLogPriceList) {
			for (HashMap.Entry<String, Double> entry : shopLogPrice.entrySet()) {
				if (entry.getValue().equals(num)) {
					shopLogPriceSortedBottom.put(entry.getKey(), num);
				}
			}
		}
		if(sortMethod.equalsIgnoreCase("amounttop")) {
			int pageNew = 0;
			int i = 0;
			shopLogPage = new LinkedHashMap<>();
			for (HashMap.Entry<String, Integer> entry : shopLogAmountSortedTop.entrySet()) {
				if(i == 45) {
					pageNew = 1 + pageNew;
					i = 0;
				}
				shopLogPage.put(entry.getKey(), pageNew);
				i++;
			}
		} else if(sortMethod.equalsIgnoreCase("amountbottom")) {
			int pageNew = 0;
			int i = 0;
			shopLogPage = new LinkedHashMap<>();
			for (HashMap.Entry<String, Integer> entry : shopLogAmountSortedBottom.entrySet()) {
				if(i == 45) {
					pageNew = 1 + pageNew;
					i = 0;
				}
				shopLogPage.put(entry.getKey(), pageNew);
				i++;
			}
		} else if(sortMethod.equalsIgnoreCase("moneytop")) {
			int pageNew = 0;
			int i = 0;
			shopLogPage = new LinkedHashMap<>();
			for (HashMap.Entry<String, Double> entry : shopLogPriceSortedTop.entrySet()) {
				if(i == 45) {
					pageNew = 1 + pageNew;
					i = 0;
				}
				shopLogPage.put(entry.getKey(), pageNew);
				i++;
			}
		} else if(sortMethod.equalsIgnoreCase("moneybottom")) {
			int pageNew = 0;
			int i = 0;
			shopLogPage = new LinkedHashMap<>();
			for (HashMap.Entry<String, Double> entry : shopLogPriceSortedBottom.entrySet()) {
				if(i == 45) {
					pageNew = 1 + pageNew;
					i = 0;
				}
				shopLogPage.put(entry.getKey(), pageNew);
				i++;
			}
		}

		DecimalFormat df = new DecimalFormat("###,###,###");
		NumberFormat defaultFormat = NumberFormat.getCurrencyInstance();
		ArrayList<Integer> totalPages = new ArrayList<>();
		Inventory shopLogInv = Bukkit.createInventory(null, 54, Component.text("Shop Log | Page " + page, NamedTextColor.RED));
		int i = 0;
		for (HashMap.Entry<String, Integer> entry : shopLogPage.entrySet()) {
			if(entry.getValue() == page) {
				ArrayList<Component> lore = new ArrayList<>();
				String mat = entry.getKey().replaceAll(" ", "_").toUpperCase();
				ItemStack item = new ItemStack(Material.valueOf(mat));
				ItemMeta meta = item.getItemMeta();
				meta.displayName(Component.text(entry.getKey(), NamedTextColor.YELLOW)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Amount Sold: ", NamedTextColor.GRAY).append(Component.text(df.format(shopLogAmount.get(entry.getKey())), NamedTextColor.YELLOW))
						.decoration(TextDecoration.ITALIC, false));
				int amountPos = new ArrayList<>(shopLogAmountSortedTop.keySet()).indexOf(entry.getKey()) + 1;
				lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(amountPos, NamedTextColor.GREEN))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text( "-----", NamedTextColor.DARK_GRAY)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Money Made: ", NamedTextColor.GRAY).append(Component.text(defaultFormat.format(shopLogPrice.get(entry.getKey())), NamedTextColor.YELLOW))
						.decoration(TextDecoration.ITALIC, false));
				int moneyPos = new ArrayList<>(shopLogPriceSortedTop.keySet()).indexOf(entry.getKey()) + 1;
				lore.add(Component.text("Position: ", NamedTextColor.GRAY).append(Component.text(moneyPos, NamedTextColor.GREEN))
						.decoration(TextDecoration.ITALIC, false));
				meta.lore(lore);
				if(i == 0) {
					NamespacedKey key = new NamespacedKey(plugin, "stop-click");
					meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
					NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
					meta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "econcheck");
					NamespacedKey key4 = new NamespacedKey(plugin, "page");
					meta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);
				}
				item.setItemMeta(meta);
				shopLogInv.setItem(i, item);
				i++;
			}
			totalPages.add(entry.getValue());
		}

		ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack nextPage = new ItemStack(Material.PAPER);
		ItemStack prevPage = new ItemStack(Material.PAPER);
		ItemStack itemSort = new ItemStack(Material.BOOK);
		ArrayList<Component> lore = new ArrayList<>();
		ItemStack itemStats = new ItemStack(Material.NETHER_STAR);
		ItemMeta metaStats = itemStats.getItemMeta();
		ItemMeta metaSort = itemSort.getItemMeta();
		ItemMeta nextMeta = nextPage.getItemMeta();
		nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN)
				.decoration(TextDecoration.ITALIC, false));
		nextPage.setItemMeta(nextMeta);
		ItemMeta prevMeta = nextPage.getItemMeta();
		prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN)
				.decoration(TextDecoration.ITALIC, false));
		prevPage.setItemMeta(prevMeta);
		for (int b = 45; b < 54; b++) {
			// 46 & 52

			if(b == 46 && totalPages.size() > 1 && page < totalPages.size()) {
				shopLogInv.setItem(b, nextPage);
			} else if(b == 52 && totalPages.size() > 1 && page > 1) {
				shopLogInv.setItem(b, prevPage);
			} else if (b == 47) {
				metaSort.displayName(Component.text("Top Sold", NamedTextColor.GREEN)
						.decoration(TextDecoration.ITALIC, false));
				itemSort.setItemMeta(metaSort);
				shopLogInv.setItem(b, itemSort);
			} else if (b == 48) {
				metaSort.displayName(Component.text("Least Sold", NamedTextColor.GREEN)
						.decoration(TextDecoration.ITALIC, false));
				itemSort.setItemMeta(metaSort);
				shopLogInv.setItem(b, itemSort);
			} else if (b == 49) {
				metaSort.displayName(Component.text("Player Search", NamedTextColor.GREEN)
						.decoration(TextDecoration.ITALIC, false));
				itemSort.setItemMeta(metaSort);
				shopLogInv.setItem(b, itemSort);
			} else if (b == 50) {
				metaSort.displayName(Component.text("Least Money Made", NamedTextColor.GREEN)
						.decoration(TextDecoration.ITALIC, false));
				itemSort.setItemMeta(metaSort);
				shopLogInv.setItem(b, itemSort);
			} else if (b == 51) {
				metaSort.displayName(Component.text("Top Money Made", NamedTextColor.GREEN)
						.decoration(TextDecoration.ITALIC, false));
				itemSort.setItemMeta(metaSort);
				shopLogInv.setItem(b, itemSort);
			} else if(b == 53) {
				metaStats.displayName(Component.text("Stats", NamedTextColor.YELLOW)
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Total Amount Sold: ", NamedTextColor.GRAY).append(Component.text(df.format(totalItemSold), NamedTextColor.YELLOW))
						.decoration(TextDecoration.ITALIC, false));
				lore.add(Component.text("Total Money Made: ", NamedTextColor.GRAY).append(Component.text(defaultFormat.format(totalMoneyMade), NamedTextColor.YELLOW))
						.decoration(TextDecoration.ITALIC, false));
				metaStats.lore(lore);
				itemStats.setItemMeta(metaStats);
				shopLogInv.setItem(b, itemStats);
			} else {
				shopLogInv.setItem(b, pane);
			}
		}
		player.openInventory(shopLogInv);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		LinkedHashMap<String, Integer> shopLogAmount = new LinkedHashMap<>();
		LinkedHashMap<String, Double> shopLogPrice = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> shopLogPage = new LinkedHashMap<>();

		if(sender instanceof Player player) {
			String sortMethod = "default";
			if(args.length == 1) {
				sortMethod = args[0];
			}
			if(args.length > 0 && args.length <= 2) {
				if (args[0].equalsIgnoreCase("player")) {
					if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
						try {
							FileInputStream fstream = new FileInputStream(Objects.requireNonNull(Bukkit.getPluginManager()
									.getPlugin("ShopGUIPlus")).getDataFolder()
									+ File.separator + "shop.log");
							BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
							String strLine;
							int page = 0;
							int i = 0;
							while ((strLine = br.readLine()) != null) {
								if (strLine.contains(";")) {
									String[] str = strLine.split(";");
									if (str.length > 0) {
										if (str[2].equalsIgnoreCase("sold")) {
											if(str[1].equalsIgnoreCase(args[1])) {
												int shopAmount = Integer.parseInt(str[3].replaceAll(",", ""));
												String shopPriceString = str[5].replaceAll(",", "");
												double shopPrice = Double.parseDouble(shopPriceString.replaceAll("\\$", ""));
												if (shopLogAmount.containsKey(str[4])) {
													int newNum = shopAmount + shopLogAmount.get(str[4]);
													shopLogAmount.put(str[4], newNum);
												} else {
													shopLogAmount.put(str[4], shopAmount);
													if (i == 45) {
														page = 1 + page;
														i = 0;
													}
													shopLogPage.put(str[4], page);
													i++;
												}

												if (shopLogPrice.containsKey(str[4])) {
													double newNum = shopPrice + shopLogPrice.get(str[4]);
													shopLogPrice.put(str[4], newNum);
												} else {
													shopLogPrice.put(str[4], shopPrice);
												}
											}
										}
									}
								}
							}
							fstream.close();
						} catch (Exception e) {
							System.err.println("Error: " + e.getMessage());
						}
					}
				}
			} else {
				try {
					FileInputStream fstream = new FileInputStream(Objects.requireNonNull(Bukkit.getPluginManager()
							.getPlugin("ShopGUIPlus")).getDataFolder()
							+ File.separator + "shop.log");
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
					String strLine;
					int page = 0;
					int i = 0;
					while ((strLine = br.readLine()) != null) {
						if (strLine.contains(";")) {
							String[] str = strLine.split(";");
							if (str.length > 0) {
								if (str[2].equalsIgnoreCase("sold")) {
									int shopAmount = Integer.parseInt(str[3].replaceAll(",", ""));
									String shopPriceString = str[5].replaceAll(",", "");
									double shopPrice = Double.parseDouble(shopPriceString.replaceAll("\\$", ""));
									if (shopLogAmount.containsKey(str[4])) {
										int newNum = shopAmount + shopLogAmount.get(str[4]);
										shopLogAmount.put(str[4], newNum);
									} else {
										shopLogAmount.put(str[4], shopAmount);
										if (i == 45) {
											page = 1 + page;
											i = 0;
										}
										shopLogPage.put(str[4], page);
										i++;
									}

									if (shopLogPrice.containsKey(str[4])) {
										double newNum = shopPrice + shopLogPrice.get(str[4]);
										shopLogPrice.put(str[4], newNum);
									} else {
										shopLogPrice.put(str[4], shopPrice);
									}
								}
							}
						}
					}
					fstream.close();
				} catch (Exception e) {
					System.err.println("Error: " + e.getMessage());
				}
			}
			plugin.shopLogAmountPlayer.put(player.getUniqueId(), shopLogAmount);
			plugin.shopLogPricePlayer.put(player.getUniqueId(), shopLogPrice);
			plugin.shopLogPagePlayer.put(player.getUniqueId(), shopLogPage);
			openGUI(player, 0, sortMethod);
		}
		return true;
	}

}

