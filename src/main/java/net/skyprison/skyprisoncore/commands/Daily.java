package net.skyprison.skyprisoncore.commands;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Daily implements CommandExecutor {

	private final SkyPrisonCore plugin;
	private final DatabaseHook hook;

	public Daily(SkyPrisonCore plugin, DatabaseHook hook) {
		this.plugin = plugin;
		this.hook = hook;
	}

	public void openGUI(Player player) {
		int currStreak = 0;
		int highestStreak = 0;
		String lastCollected = "";

		try {
			Connection conn = hook.getSQLConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT current_streak, highest_streak, last_collected FROM dailies WHERE user_id = '" + player.getUniqueId() + "'");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				currStreak = rs.getInt(1);
				highestStreak = rs.getInt(2);
				lastCollected = rs.getString(3);
			}
			hook.close(ps, rs, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Inventory dailyGUI = Bukkit.createInventory(null, 27, ChatColor.RED + "Daily Reward");

		ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta paneMeta = pane.getItemMeta();
		paneMeta.setDisplayName(" ");
		pane.setItemMeta(paneMeta);
		boolean hasCollected = false;
		if(!lastCollected.isEmpty()) {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			String currDate = formatter.format(date);
			if(currDate.equalsIgnoreCase(lastCollected)) {
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
					lore.add(plugin.colourMessage("&7Highest Streak: &f&l" + highestStreak));
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
		} else if (args.length == 1) {
			hook.convertToSql();
		} else {
			hook.createDatabase();
		}
		return true;
	}
}
