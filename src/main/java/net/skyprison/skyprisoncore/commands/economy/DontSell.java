package net.skyprison.skyprisoncore.commands.economy;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class DontSell implements CommandExecutor {
	private SkyPrisonCore plugin;

	public DontSell(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(player.hasPermission("shopguiplus.sell.all")) {
				File f = new File(plugin.getDataFolder() + File.separator + "blocksells.yml");
				FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
				if(args.length >= 1) {
					if(args[0].equalsIgnoreCase("list")) {
						if(yamlf.isConfigurationSection(player.getUniqueId().toString())) {
							ArrayList<String> blockedSales = (ArrayList) yamlf.getList(player.getUniqueId().toString() + ".blocked");
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
							ItemStack item = new ItemStack(Material.getMaterial(args[0].toUpperCase()), 1);
							if(ShopGuiPlusApi.getItemStackShopItem(item) != null) {
								String iName = item.getType().name();

								ArrayList blockedSales;
								if (yamlf.getList(player.getUniqueId().toString() + ".blocked") != null && !yamlf.getList(player.getUniqueId().toString() + ".blocked").isEmpty()) {
									blockedSales = (ArrayList) yamlf.getList(player.getUniqueId().toString() + ".blocked");
								} else {
									blockedSales = new ArrayList();
								}

								if (!blockedSales.contains(iName)) {
									blockedSales.add(iName);
									player.sendMessage(plugin.colourMessage("&aSuccessfully &lADDED &aitem to the dont sell list!"));
								} else {
									blockedSales.remove(iName);
									player.sendMessage(plugin.colourMessage("&aSuccessfully &lREMOVED &aitem from the dont sell list!"));
								}
								yamlf.set(player.getUniqueId().toString() + ".blocked", blockedSales);
								try {
									yamlf.save(f);
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else {
								player.sendMessage(plugin.colourMessage("&cThis item can't be sold!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cThat is not a valid item name!"));
						}
					}
				} else {
					ItemStack item = player.getInventory().getItemInMainHand();
					if (item != null && !item.getType().equals(Material.AIR)) {
						if(ShopGuiPlusApi.getItemStackShopItem(item) != null) {
							String iName = item.getType().name();

							ArrayList blockedSales;
							if (yamlf.getList(player.getUniqueId().toString() + ".blocked") != null && !yamlf.getList(player.getUniqueId().toString() + ".blocked").isEmpty()) {
								blockedSales = (ArrayList) yamlf.getList(player.getUniqueId().toString() + ".blocked");
							} else {
								blockedSales = new ArrayList();
							}

							if (!blockedSales.contains(iName)) {
								blockedSales.add(iName);
								player.sendMessage(plugin.colourMessage("&aSuccessfully &lADDED &aitem to the dont sell list!"));
							} else {
								blockedSales.remove(iName);
								player.sendMessage(plugin.colourMessage("&aSuccessfully &lREMOVED &aitem from the dont sell list!"));
							}
							yamlf.set(player.getUniqueId().toString() + ".blocked", blockedSales);
							try {
								yamlf.save(f);
							} catch (IOException e) {
								e.printStackTrace();
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
