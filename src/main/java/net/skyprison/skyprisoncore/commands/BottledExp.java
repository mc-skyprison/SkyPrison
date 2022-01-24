package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class BottledExp implements CommandExecutor {
	private final SkyPrisonCore plugin;
	@Inject
	public BottledExp(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void createBottle(Player player, Integer amount) {
		ItemStack expBottle = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta expMeta = expBottle.getItemMeta();
		expMeta.setDisplayName(plugin.colourMessage("&5Experience Bottle &7(Throw)"));
		ArrayList<String> lore = new ArrayList<>();
		lore.add(plugin.colourMessage("&7Experience: &e" + plugin.formatNumber(amount)));
		expMeta.setLore(lore);
		NamespacedKey key = new NamespacedKey(plugin, "exp-amount");
		expMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, amount);
		expBottle.setItemMeta(expMeta);
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		if (user.getInventory().canFit(expBottle)) {
			user.takeExp(amount);
			user.getInventory().addItem(expBottle);
			plugin.asConsole("money take " + player.getName() + " " + amount * 0.25);
			player.sendMessage(plugin.colourMessage("&4&l-" + plugin.formatNumber(amount)) + " XP");
		} else {
			player.sendMessage(plugin.colourMessage("&cYou do not have space in your inventory!"));
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 1) {
				CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
				if (plugin.isInt(args[0])) {
					int expToWithdraw = Integer.parseInt(args[0]);
					if (expToWithdraw > 0) {
						if (player.getTotalExperience() >= expToWithdraw) {
							if (user.getBalance() >= expToWithdraw * 0.25) {
								int allowedExpWithdraw = 2500;
								if (player.hasPermission("skyprisoncore.command.bottledexp.tier1")) {
									allowedExpWithdraw = 10000;
								}
								if (player.hasPermission("skyprisoncore.command.bottledexp.tier2")) {
									allowedExpWithdraw = 1000000;
								}

								if (expToWithdraw <= allowedExpWithdraw) {
									createBottle(player, expToWithdraw);
								} else {
									player.sendMessage(plugin.colourMessage("&cYou can't drain that much experience at once! (Limit: " + plugin.formatNumber(allowedExpWithdraw) + ")"));
								}
							} else {
								player.sendMessage(plugin.colourMessage("&cYou need $" + plugin.formatNumber(expToWithdraw * 0.25) + " to bottle that amount of experience!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cYou do not have that amount of experience!"));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cYou can't withdraw 0 or less experience!"));
					}
				} else if (args[0].equalsIgnoreCase("all")) {
					if (player.hasPermission("skyprisoncore.command.bottledexp.tier2")) {
						if (player.getTotalExperience() > 0) {
							if (user.getBalance() >= player.getTotalExperience() * 0.25) {
								createBottle(player, player.getTotalExperience());
							} else {
								player.sendMessage(plugin.colourMessage("&cYou need $" + plugin.formatNumber(player.getTotalExperience() * 0.25) + " to bottle that amount of experience!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cYou can't withdraw 0 or less experience!"));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cYou do not have permission to /xpb all"));

					}
				}
			} else {
				player.sendMessage(plugin.colourMessage("&7Current Experience: &e" + plugin.formatNumber(player.getTotalExperience()) + "\n&c/xpb <amount>"));
			}
		}
		return true;
	}
}
