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

	public ItemStack getItemType(int currStreak, int b, boolean hasCollected, boolean canCollect) {
		ItemStack alreadyCollected = new ItemStack(Material.BARRIER);
		ItemMeta alreadyMeta = alreadyCollected.getItemMeta();
		alreadyMeta.setLore(Arrays.asList(plugin.colourMessage("&c&oYou've already collected this reward!")));

		ItemStack collectReward = new ItemStack(Material.CHEST_MINECART);
		ItemMeta collectMeta = collectReward.getItemMeta();
		collectMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, false);
		collectMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		ArrayList<String> cLore = new ArrayList<>();
		cLore.add(plugin.colourMessage("&aClick me to collect todays reward!"));

		ItemStack soonCollect = new ItemStack(Material.MINECART);
		if(currStreak == b) {
			if(!hasCollected && canCollect)
				return collectReward;
			else if (!canCollect)
				return soonCollect;
			else
				return alreadyCollected;
		} else if(currStreak > b) {
			return alreadyCollected;
		} else {
			return soonCollect;
		}
	}

	public void openGUI(Player player) {
		File f = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
		FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
		Inventory dailyGUI = Bukkit.createInventory(null, 27, ChatColor.RED + "Daily Rewards");

		ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta paneMeta = pane.getItemMeta();
		paneMeta.setDisplayName(" ");
		pane.setItemMeta(paneMeta);
		int currStreak = 1;
		boolean hasCollected = false;
		boolean canCollect = true;
		if(dailyConf.isConfigurationSection("players." + player.getUniqueId())) {
			currStreak = dailyConf.getInt("players." + player.getUniqueId() + ".current-streak");
			String collectedDay = dailyConf.getString("players." + player.getUniqueId() + ".last-collected");
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			String currDate = formatter.format(date);
			if(currDate.equalsIgnoreCase(collectedDay)) {
				hasCollected = true;
				canCollect = false;
			}
		}

		ItemStack day1 = null;
		ItemStack day2 = null;
		ItemStack day3 = null;
		ItemStack day4 = null;
		ItemStack day5 = null;
		ItemStack day6 = null;
		ItemStack day7 = null;
		ArrayList<String> lore = new ArrayList<>();

		for(int b = 1; b < 8; b++) {
			switch(b) {
				case 1:
					day1 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta1 = day1.getItemMeta();
					meta1.setDisplayName(plugin.colourMessage("&eDay 1 Reward"));
					lore.add("Rewards:");
					lore.add("- 25 Tokens");
					day1.setItemMeta(meta1);
					break;
				case 2:
					day2 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta2 = day2.getItemMeta();
					meta2.setDisplayName(plugin.colourMessage("&eDay 2 Reward"));
					lore.clear();
					lore.add("Rewards:");
					lore.add("- 50 Tokens");
					day2.setItemMeta(meta2);
					break;
				case 3:
					day3 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta3 = day3.getItemMeta();
					meta3.setDisplayName(plugin.colourMessage("&eDay 3 Reward"));
					lore.clear();
					lore.add("Rewards:");
					lore.add("- 75 Tokens");
					day3.setItemMeta(meta3);
					break;
				case 4:
					day4 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta4 = day4.getItemMeta();
					meta4.setDisplayName(plugin.colourMessage("&eDay 4 Reward"));
					lore.clear();
					lore.add("Rewards:");
					lore.add("- 100 Tokens");
					day4.setItemMeta(meta4);
					break;
				case 5:
					day5 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta5 = day5.getItemMeta();
					meta5.setDisplayName(plugin.colourMessage("&eDay 5 Reward"));
					lore.clear();
					lore.add("Rewards:");
					lore.add("- 125 Tokens");
					day5.setItemMeta(meta5);
					break;
				case 6:
					day6 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta6 = day6.getItemMeta();
					meta6.setDisplayName(plugin.colourMessage("&eDay 6 Reward"));
					lore.clear();
					lore.add("Rewards:");
					lore.add("- 150 Tokens");
					day6.setItemMeta(meta6);
					break;
				case 7:
					day7 = getItemType(currStreak, b, hasCollected, canCollect);
					ItemMeta meta7 = day7.getItemMeta();
					meta7.setDisplayName(plugin.colourMessage("&eDay 7 Reward"));
					lore.clear();
					lore.add("Rewards:");
					lore.add("- 175 Tokens");
					day7.setItemMeta(meta7);
					break;
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
				case 10:
					dailyGUI.setItem(i, day1);
					break;
				case 11:
					dailyGUI.setItem(i, day2);
					break;
				case 12:
					dailyGUI.setItem(i, day3);
					break;
				case 13:
					dailyGUI.setItem(i, day4);
					break;
				case 14:
					dailyGUI.setItem(i, day5);
					break;
				case 15:
					dailyGUI.setItem(i, day6);
					break;
				case 16:
					dailyGUI.setItem(i, day7);
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
			// player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
			openGUI(player);
		}
		return true;
	}
}
