package net.skyprison.skyprisoncore.commands;

import net.alex9849.arm.AdvancedRegionMarket;
import net.alex9849.arm.regions.Region;
import net.alex9849.arm.regions.SellType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import java.util.LinkedList;
import java.util.List;

public class PlotTeleport implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public PlotTeleport(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void openGUI(Player player) {
		Inventory plotsGUI = Bukkit.createInventory(null, 27, Component.text("Plots Teleport", NamedTextColor.GOLD));
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		ItemMeta grayMeta = grayPane.getItemMeta();
		whiteMeta.displayName(Component.empty());
		whitePane.setItemMeta(whiteMeta);
		grayMeta.displayName(Component.empty());
		grayPane.setItemMeta(grayMeta);
		for (int i = 0; i < 27; i++) {
			if(i == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				whiteMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				whiteMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "plotteleport");
				whitePane.setItemMeta(whiteMeta);
				plotsGUI.setItem(i, whitePane);
			} else if(i >= 17 || i == 9) {
				plotsGUI.setItem(i, grayPane);
			} else if(i <= 8 || i == 10 || i == 16) {
				plotsGUI.setItem(i, whitePane);
			}
		}
		List<Integer> availableNums = new LinkedList<>(Arrays.asList(11, 12, 13, 14, 15));
		List<Region> regions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByOwner(player.getUniqueId());
		if(regions != null && !regions.isEmpty()) {
			List<Region> plots = new ArrayList<>();
			for (Region region : regions) {
				if (region.getSellType() == SellType.SELL) {
					plots.add(region);
				}
			}

			for (Region region : plots) {
				ItemStack plotItem = new ItemStack(Material.OAK_SIGN, 1);
				ItemMeta plotMeta = plotItem.getItemMeta();
				plotMeta.displayName(Component.text(region.getRegion().getId(), NamedTextColor.GOLD)
						.decoration(TextDecoration.ITALIC, false));
				List<Component> lore = new ArrayList<>();
				lore.add(Component.text("Location: ", NamedTextColor.YELLOW)
						.append(Component.text("X: " + region.getRegion().getMinPoint().getBlockX() + ", Z: " + region.getRegion().getMinPoint().getBlockZ(), NamedTextColor.GRAY))
						.decoration(TextDecoration.ITALIC, false));
				plotMeta.lore(lore);
				plotItem.setItemMeta(plotMeta);


				double xCoord = region.getRegion().getMinPoint().getBlockX() - 0.5;
				double yCoord = 0;
				double zCoord = region.getRegion().getMinPoint().getBlockZ() - 0.5;
				NamespacedKey key = new NamespacedKey(plugin, "x");
				plotMeta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, xCoord);
				NamespacedKey key1 = new NamespacedKey(plugin, "y");
				for(int i = 65; i < 113; i++) {
					Location newLoc = new Location(Bukkit.getWorld("world_skycity"), xCoord, i, zCoord);
					if(newLoc.getBlock().getType().isAir()) {
						Location upLoc = new Location(Bukkit.getWorld("world_skycity"), xCoord, i+1, zCoord);
						Location downLoc = new Location(Bukkit.getWorld("world_skycity"), xCoord, i-1, zCoord);
						if(upLoc.getBlock().getType().isAir() && downLoc.getBlock().getType().isBlock()) {
							yCoord = i + 0.5;
							break;
						}
					}
				}
				plotMeta.getPersistentDataContainer().set(key1, PersistentDataType.DOUBLE, yCoord);
				NamespacedKey key2 = new NamespacedKey(plugin, "z");
				plotMeta.getPersistentDataContainer().set(key2, PersistentDataType.DOUBLE, zCoord);
				NamespacedKey key3 = new NamespacedKey(plugin, "world");
				plotMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, "world_skycity");
				plotItem.setItemMeta(plotMeta);
				plotsGUI.setItem(availableNums.get(0), plotItem);
				availableNums.remove(0);
			}

			player.openInventory(plotsGUI);
		} else {
			player.sendMessage(Component.text("You do not own any plots!", NamedTextColor.RED));
		}
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if(!player.getWorld().getName().equalsIgnoreCase("world_prison")) {
				openGUI((Player) sender);
			} else {
				player.sendMessage(Component.text("&cCan't use this command here!", NamedTextColor.RED));
			}
		}
		return true;
	}
}


