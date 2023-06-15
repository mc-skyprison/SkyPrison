package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BottledExp implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public BottledExp(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void createBottle(Player player, Integer amount) {
		ItemStack expBottle = new ItemStack(Material.EXPERIENCE_BOTTLE, 1);
		ItemMeta expMeta = expBottle.getItemMeta();
		expMeta.displayName(Component.text("Experience Bottle ", NamedTextColor.DARK_PURPLE).append(Component.text("(Throw)", NamedTextColor.GRAY)));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Experience: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(amount), NamedTextColor.YELLOW)));
		expMeta.lore(lore);
		NamespacedKey key = new NamespacedKey(plugin, "exp-amount");
		expMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, amount);
		expBottle.setItemMeta(expMeta);
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		if (user.getInventory().canFit(expBottle)) {
			user.takeExp(amount);
			user.getInventory().addItem(expBottle);
			plugin.asConsole("money take " + player.getName() + " " + amount * 0.25);
			player.sendMessage(Component.text("-" + plugin.formatNumber(amount) + " XP", NamedTextColor.DARK_RED, TextDecoration.BOLD));
		} else {
			player.sendMessage(Component.text("You do not have space in your inventory!", NamedTextColor.RED));
		}
	}


	public void createMultipleBottles(Player player, Integer amount, Integer bAmount, Integer tAmount) {
		ItemStack expBottle = new ItemStack(Material.EXPERIENCE_BOTTLE, bAmount);
		ItemMeta expMeta = expBottle.getItemMeta();
		expMeta.displayName(Component.text("Experience Bottle ", NamedTextColor.DARK_PURPLE).append(Component.text("(Throw)", NamedTextColor.GRAY)));
		ArrayList<Component> lore = new ArrayList<>();
		lore.add(Component.text("Experience: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(amount), NamedTextColor.YELLOW)));
		expMeta.lore(lore);
		NamespacedKey key = new NamespacedKey(plugin, "exp-amount");
		expMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, amount);
		expBottle.setItemMeta(expMeta);
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		if (user.getInventory().canFit(expBottle)) {
			user.takeExp(tAmount);
			user.getInventory().addItem(expBottle);
			plugin.asConsole("money take " + player.getName() + " " + tAmount * 0.25);
			player.sendMessage(Component.text("-" + plugin.formatNumber(tAmount) + " XP", NamedTextColor.DARK_RED, TextDecoration.BOLD));
		} else {
			player.sendMessage(Component.text("You do not have space in your inventory!", NamedTextColor.RED));
		}
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
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
									allowedExpWithdraw = 10000000;
								}

								if (expToWithdraw <= allowedExpWithdraw) {
									createBottle(player, expToWithdraw);
								} else {
									player.sendMessage(Component.text("You can't drain that much experience at once! (Limit: " + plugin.formatNumber(allowedExpWithdraw) + ")", NamedTextColor.RED));
								}
							} else {
								player.sendMessage(Component.text("You need $" + plugin.formatNumber(expToWithdraw * 0.25) + " to bottle that amount of experience!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("You do not have that amount of experience!", NamedTextColor.RED));
						}
					} else {
						player.sendMessage(Component.text("You can't withdraw 0 or less experience!", NamedTextColor.RED));
					}
				} else if (args[0].equalsIgnoreCase("all")) {
					if (player.hasPermission("skyprisoncore.command.bottledexp.tier2")) {
						if (player.getTotalExperience() > 0) {
							if (user.getBalance() >= player.getTotalExperience() * 0.25) {
								createBottle(player, player.getTotalExperience());
							} else {
								player.sendMessage(Component.text("You need $" + plugin.formatNumber(player.getTotalExperience() * 0.25) + " to bottle that amount of experience!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("You can't withdraw 0 or less experience!", NamedTextColor.RED));
						}
					} else {
						player.sendMessage(Component.text("You do not have permission to /xpb all", NamedTextColor.RED));
					}
				} else {
					player.sendMessage(Component.text("Incorrect usage: /xpb <experience/all>", NamedTextColor.RED));
				}
			} else if(args.length == 2) {
				CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
				if(plugin.isInt(args[0]) && plugin.isInt(args[1])) {
					if(player.hasPermission("skyprisoncore.command.bottledexp.tier3")) {
						int expToWithdraw = Integer.parseInt(args[0]);
						int bottlesToMake = Integer.parseInt(args[1]);
						if (expToWithdraw > 0 && bottlesToMake > 0) {
							if(bottlesToMake <= 64) {
								if (player.getTotalExperience() >= expToWithdraw * bottlesToMake) {
									int actualExpWithdraw = expToWithdraw * bottlesToMake;
									if (user.getBalance() >= actualExpWithdraw * 0.25) {
										createMultipleBottles(player, expToWithdraw, bottlesToMake, actualExpWithdraw);
									}
								} else {
									player.sendMessage(Component.text("You do not have that amount of experience!", NamedTextColor.RED));
								}
							} else {
								player.sendMessage(Component.text("You can only withdraw a max of 64 bottles at a time!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("You can't withdraw 0 or less experience!", NamedTextColor.RED));
						}
					} else {
						player.sendMessage(Component.text("You do not have permission to /xpb <experience (bottle amount)", NamedTextColor.RED));
					}
				} else {
					player.sendMessage(Component.text("Incorrect usage: /xpb <experience> (how many bottles to make)", NamedTextColor.RED));
				}
			} else {
				player.sendMessage(Component.text("Current Experience: ", NamedTextColor.GRAY).append(Component.text(plugin.formatNumber(player.getTotalExperience()), NamedTextColor.YELLOW))
						.append(Component.text("\n/xpb <amount>", NamedTextColor.RED)));
			}
		}
		return true;
	}
}
