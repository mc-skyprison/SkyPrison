package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Daily implements CommandExecutor {

	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public Daily(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	public void openGUI(Player player) {
		int currStreak = 0;
		int highestStreak = 0;
		String lastCollected = "";

		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_streak, highest_streak, last_collected FROM dailies WHERE user_id = ?")) {
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				currStreak = rs.getInt(1);
				highestStreak = rs.getInt(2);
				lastCollected = rs.getString(3);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Inventory dailyGUI = Bukkit.createInventory(null, 27, Component.text("Daily Reward"));

		ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		pane.editMeta(meta -> meta.displayName(Component.empty()));
		boolean hasCollected;
		if(!lastCollected.isEmpty()) {
			Date date = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			String currDate = formatter.format(date);
			if(currDate.equalsIgnoreCase(lastCollected)) {
				hasCollected = true;
			} else {
                hasCollected = false;
            }
        } else {
            hasCollected = false;
        }

        for (int i = 0; i < 27; i++) {
			int finalCurrStreak = currStreak;
			int finalHighestStreak = highestStreak;
			switch (i) {
				case 0 -> {
					ItemStack startPane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
					startPane.editMeta(meta -> {
						meta.displayName(Component.empty());
						NamespacedKey key = new NamespacedKey(plugin, "stop-click");
						meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
						NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
						meta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "daily-reward");
					});
					dailyGUI.setItem(i, startPane);
				}
				case 13 -> {
					ItemStack dReward;
					if (!hasCollected) {
						dReward = new ItemStack(Material.CHEST_MINECART);
					} else {
						dReward = new ItemStack(Material.MINECART);
					}
					dReward.editMeta(meta -> {
						meta.displayName(Component.text("Daily Reward", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
						ArrayList<Component> lore = new ArrayList<>();
						if (!hasCollected) {
							lore.add(Component.text("Click here to collect your reward!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
						} else {
							lore.add(Component.text("You've already collected today!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
						}
						lore.add(Component.empty());
						lore.add(Component.text("Current Streak: ", NamedTextColor.GRAY).append(Component.text(finalCurrStreak, NamedTextColor.WHITE, TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
						lore.add(Component.text("Highest Streak: ", NamedTextColor.GRAY).append(Component.text(finalHighestStreak, NamedTextColor.WHITE, TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
						meta.lore(lore);
					});


					dailyGUI.setItem(i, dReward);
				}
				default -> dailyGUI.setItem(i, pane);
			}
		}
		player.openInventory(dailyGUI);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			openGUI(player);
		}
		return true;
	}
}
