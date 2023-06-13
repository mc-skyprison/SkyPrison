package net.skyprison.skyprisoncore.commands.economy;

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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Bartender implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public Bartender(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}


	public void openGUI(Player player, String bar) {
		File f = new File(plugin.getDataFolder() + File.separator + "bartender.yml");
		FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
		Set<String> alchohols = yamlf.getConfigurationSection(bar).getKeys(false);
		Inventory bartenderGUI = Bukkit.createInventory(null, 45, Component.text("Bartender Shop", NamedTextColor.RED));
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		ItemMeta grayMeta = grayPane.getItemMeta();
		whiteMeta.displayName(Component.empty());
		whitePane.setItemMeta(whiteMeta);
		whiteMeta.displayName(Component.empty());
		grayPane.setItemMeta(grayMeta);
		for (int i = 0; i < 45; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				grayMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				grayMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "bartender-" + bar);
				grayPane.setItemMeta(grayMeta);
				bartenderGUI.setItem(i, grayPane);
			} else if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i >= 35 && i <= 39 || i >= 41) {
				bartenderGUI.setItem(i, grayPane);
			} else if(i <= 7 || i == 10 || i == 16 || i == 19 || i == 25 || i == 28 || i == 34) {
				bartenderGUI.setItem(i, whitePane);
			} else if(i == 40) {
				ItemStack balance = new ItemStack(Material.NETHER_STAR);
				ItemMeta bMeta = balance.getItemMeta();
				bMeta.displayName(Component.text("Your Balance", NamedTextColor.GOLD, TextDecoration.BOLD));
				bMeta.lore(Collections.singletonList(Component.text(PlaceholderAPI.setPlaceholders(player, "%cmi_user_balance_formatted%"), NamedTextColor.GRAY)));
				balance.setItemMeta(bMeta);
				bartenderGUI.setItem(i, balance);
			}
		}
		if(!alchohols.isEmpty()) {
			for (String dropItem : alchohols) {
				ItemStack alcohol = yamlf.getItemStack(bar + "." + dropItem + ".item");
				if(alcohol != null) {
					ItemMeta alcMeta = alcohol.getItemMeta();
					List<Component> lore = new ArrayList<>();
					if (alcMeta.lore() != null && !alcMeta.lore().isEmpty()) {
						lore = alcMeta.lore();
					}
					String price = plugin.formatNumber(yamlf.getDouble(bar + "." + dropItem + ".price"));
					lore.add(0, Component.text("Price: ", NamedTextColor.YELLOW).append(Component.text("$" + price, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
					alcMeta.lore(lore);
					NamespacedKey key = new NamespacedKey(plugin, "alc-type");
					alcMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, dropItem);
					alcohol.setItemMeta(alcMeta);
					int slot = yamlf.getInt(bar + "." + dropItem + ".slot");
					bartenderGUI.setItem(slot, alcohol);
				}
			}
		}

		player.openInventory(bartenderGUI);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {
			if (args.length == 1) {
				openGUI(player, args[0]);
			}
		}
		return true;
	}
}

