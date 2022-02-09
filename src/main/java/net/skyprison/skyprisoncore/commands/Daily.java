package net.skyprison.skyprisoncore.commands;

import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Daily implements CommandExecutor {

	private SkyPrisonCore plugin;

	@Inject
	public Daily(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void openGUI(Player player) {
		File f = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
		FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
		Inventory dailyGUI = Bukkit.createInventory(null, 27, ChatColor.RED + "Daily Reward");

		ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta paneMeta = pane.getItemMeta();
		paneMeta.setDisplayName(" ");
		pane.setItemMeta(paneMeta);
		int currStreak = dailyConf.getInt("players." + player.getUniqueId() + ".current-streak");
		int highestStreak = dailyConf.getInt("players." + player.getUniqueId() + ".highest-streak");
		boolean hasCollected = false;
		if(dailyConf.isConfigurationSection("players." + player.getUniqueId())) {
			String collectedDay = dailyConf.getString("players." + player.getUniqueId() + ".last-collected");
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			String currDate = formatter.format(date);
			if(currDate.equalsIgnoreCase(collectedDay)) {
				hasCollected = true;
			}
		}

		for (int i = 0; i < 27; i++) {
			switch(i) {
				case 0:
					ItemStack startPane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
					ItemMeta startMeta = startPane.getItemMeta();
					startMeta.setDisplayName(" ");
					NamespacedKey key = new NamespacedKey(plugin, "stop-click");
					startMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
					NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
					startMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "daily-reward");
					startPane.setItemMeta(startMeta);
					dailyGUI.setItem(i, startPane);
					break;
				case 13:
					ItemStack dReward;
					if(!hasCollected) {
						dReward = new ItemStack(Material.CHEST_MINECART);
					} else {
						dReward = new ItemStack(Material.MINECART);
					}
					ItemMeta dMeta = dReward.getItemMeta();
					dMeta.setDisplayName(plugin.colourMessage("&e&lDaily Reward"));
					ArrayList<String> lore = new ArrayList<>();
					if(!hasCollected) {
						lore.add(plugin.colourMessage("&aClick here to collect your reward!"));
					} else {
						lore.add(plugin.colourMessage("&cYou've already collected today!"));
					}
					lore.add("");
					lore.add(plugin.colourMessage("&7Current Streak: &f&l" + currStreak));
					if(highestStreak != currStreak) {
						lore.add(plugin.colourMessage("&7Highest Streak: &f&l" + highestStreak));
					}
					dMeta.setLore(lore);
					dReward.setItemMeta(dMeta);
					dailyGUI.setItem(i, dReward);
					break;
				default:
					dailyGUI.setItem(i, pane);
					break;
			}
		}
		player.openInventory(dailyGUI);
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
