package net.skyprison.skyprisoncore.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class SkyPlot implements CommandExecutor {
	private final SkyPrisonCore plugin;

	HashMap<UUID, Boolean> createConfirm = new HashMap<>();

	public SkyPlot(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void createSkyPlot(Player player) {
		World w = Bukkit.getWorld("world_skyplots");
		Clipboard clipboard = null;
		File file = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + "test2.schem");


		File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
		FileConfiguration fData = YamlConfiguration.loadConfiguration(f);

		ClipboardFormat format = ClipboardFormats.findByFile(file);
		try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
			clipboard = reader.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		assert w != null;
		RegionManager regions = container.get(BukkitAdapter.adapt(w));
		outerloop:
		for (int i = -300; i < 300; i += 100) {
			for (int j = -300; j < 300; j += 100) {
				if (regions.getApplicableRegions(BlockVector3.at(i, 64, j)).getRegions().isEmpty() || regions.getApplicableRegions(BlockVector3.at(i, 64, j)).getRegions() == null) {
					try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(w))) {
						Operation operation = new ClipboardHolder(clipboard)
								.createPaste(editSession)
								.to(BlockVector3.at(i, 64, j))
								.build();
						Operations.complete(operation);
						ProtectedRegion pRegion = new ProtectedCuboidRegion(player.getUniqueId() + "_main" + j + 50 + "-" + i + 50, BlockVector3.at(i + 50, 0, j + 50), BlockVector3.at(i - 50, 255, j - 50));
						final DefaultDomain owner = pRegion.getOwners();
						owner.addPlayer(player.getUniqueId());
						pRegion.setOwners(owner);
						regions.addRegion(pRegion);

						fData.set(player.getUniqueId() + ".x", i-0.5);
						fData.set(player.getUniqueId() + ".y", 66);
						fData.set(player.getUniqueId() + ".z", j-8.5);
						fData.set(player.getUniqueId() + ".visit", false);
						fData.set(player.getUniqueId() + ".banned", new ArrayList<>());

						fData.save(f);
						Location loc = new Location(w, i-0.5, 66, j-8.5);
						player.teleportAsync(loc);
						player.sendMessage(plugin.colourMessage("&aSkyPlots &f&l>> &eYour skyplot has successfully been created! Use /skyplot to open the SkyPlot Menu."));
					} catch (WorldEditException | IOException e) {
						e.printStackTrace();
					}
					break outerloop;
				}
			}
		}
	}

	public void skyPlotGUI(Player player, String page, Integer pageNum) {
		ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta whiteMeta = whitePane.getItemMeta();
		whiteMeta.displayName(Component.text(" "));
		whitePane.setItemMeta(whiteMeta);

		ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta grayMeta = grayPane.getItemMeta();
		grayMeta.displayName(Component.text(" "));
		grayPane.setItemMeta(grayMeta);
		HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
		Inventory skyplotInv = null;

		switch(page.toLowerCase()) {
			case "main":
				skyplotInv = Bukkit.createInventory(null, 45, Component.text(plugin.colourMessage("&bSky Plots")));
				for(int i = 0; i < skyplotInv.getSize(); i++) {
					if(i <= 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35) {
						skyplotInv.setItem(i, whitePane);
					} else if (i > 35) {
						skyplotInv.setItem(i, grayPane);
					} else if(i == 24) {
						ItemStack visit = hAPI.getItemHead("38200");
						ItemMeta visitMeta = visit.getItemMeta();
						visitMeta.displayName(Component.text(plugin.colourMessage("&eVisit Public SkyPlots")));
						visit.setItemMeta(visitMeta);
						skyplotInv.setItem(i, visit);
					} else if(i == 20) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 13) {
						ItemStack plotStats = new ItemStack(Material.PLAYER_HEAD);
						SkullMeta plotMeta = (SkullMeta) plotStats.getItemMeta();
						plotMeta.setOwningPlayer(player);
						plotMeta.displayName(Component.text(plugin.colourMessage("&eYour SkyPlot")));
						plotStats.setItemMeta(plotMeta);
						skyplotInv.setItem(i, plotStats);
					} else if(i == 31) {
						ItemStack settings = new ItemStack(Material.REPEATER);
						ItemMeta setMeta = settings.getItemMeta();
						setMeta.displayName(Component.text(plugin.colourMessage("&eSkyPlot Settings")));
						settings.setItemMeta(setMeta);
						skyplotInv.setItem(i, settings);
					}
				}
				break;
			case "expand":
				skyplotInv = Bukkit.createInventory(null, 27, Component.text(plugin.colourMessage("&bYour SkyPlot")));
				for(int i = 0; i < skyplotInv.getSize(); i++) {
					if(i <= 9) {
						skyplotInv.setItem(i, whitePane);
					} else if (i == 17) {
						skyplotInv.setItem(i, whitePane);
					} else if (i > 17 && i != 22) {
						skyplotInv.setItem(i, grayPane);
					} else if(i == 10) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 11) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 12) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 13) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 14) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 15) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 16) {
						ItemStack expand = hAPI.getItemHead("18243");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eExpand SkyPlot")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else {
						ItemStack backButton = new ItemStack(Material.NETHER_STAR);
						ItemMeta backMeta = backButton.getItemMeta();
						backMeta.displayName(Component.text(plugin.colourMessage("&cPrevious Page")));
					}
				}
				break;
			case "settings":
				skyplotInv = Bukkit.createInventory(null, 27, Component.text(plugin.colourMessage("&bSkyPlot Settings")));
				for(int i = 0; i < skyplotInv.getSize(); i++) {
					if(i <= 9) {
						skyplotInv.setItem(i, whitePane);
					} else if (i == 17) {
						skyplotInv.setItem(i, whitePane);
					} else if (i > 17 && i != 22) {
						skyplotInv.setItem(i, grayPane);
					} else if(i == 11) {
						ItemStack expand = new ItemStack(Material.BARRIER);
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&eBanned Players")));
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 15) {
						ItemStack expand = hAPI.getItemHead("38200");
						ItemMeta expMeta = expand.getItemMeta();
						expMeta.displayName(Component.text(plugin.colourMessage("&ePlot Visit Status")));
						ArrayList<Component> lore = new ArrayList<>();
						if(canVisit(player)) {
							lore.add(Component.text(plugin.colourMessage("&7Visitng is &2ENABLED")));
						} else {
							lore.add(Component.text(plugin.colourMessage("&7Visitng is &4DISABLED")));
						}
						expand.setItemMeta(expMeta);
						skyplotInv.setItem(i, expand);
					} else if(i == 22) {
						ItemStack backButton = new ItemStack(Material.NETHER_STAR);
						ItemMeta backMeta = backButton.getItemMeta();
						backMeta.displayName(Component.text(plugin.colourMessage("&cPrevious Page")));
					}
				}
				break;
			case "other":
				skyplotInv = Bukkit.createInventory(null, 54, Component.text(plugin.colourMessage("&bOther SkyPlots")));
				for(int i = 0; i < skyplotInv.getSize(); i++) {
					if(i <= 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44) {
						skyplotInv.setItem(i, whitePane);
					} else if (i > 44 && i != 49) {
						skyplotInv.setItem(i, grayPane);
					}else if(i == 49) {
						ItemStack backButton = new ItemStack(Material.NETHER_STAR);
						ItemMeta backMeta = backButton.getItemMeta();
						backMeta.displayName(Component.text(plugin.colourMessage("&cPrevious Page")));
					}
				}
				break;
			case "banned":
				skyplotInv = Bukkit.createInventory(null, 54, Component.text(plugin.colourMessage("&bBanned Players")));
				for(int i = 0; i < skyplotInv.getSize(); i++) {
					if(i <= 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44) {
						skyplotInv.setItem(i, whitePane);
					} else if (i > 44 && i != 49) {
						skyplotInv.setItem(i, grayPane);
					}else if(i == 49) {
						ItemStack backButton = new ItemStack(Material.NETHER_STAR);
						ItemMeta backMeta = backButton.getItemMeta();
						backMeta.displayName(Component.text(plugin.colourMessage("&cPrevious Page")));
					}
				}
				break;
		}
		assert skyplotInv != null;

		ItemStack blankPane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta blankMeta = blankPane.getItemMeta();

		NamespacedKey key = new NamespacedKey(plugin, "stop-click");
		blankMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
		NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
		blankMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "skyplot-gui");
		NamespacedKey key2 = new NamespacedKey(plugin, "skyplot-type");
		blankMeta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, page);
		NamespacedKey key3 = new NamespacedKey(plugin, "skyplot-page");
		blankMeta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, pageNum);
		blankPane.setItemMeta(blankMeta);

		skyplotInv.setItem(0, blankPane);
		player.openInventory(skyplotInv);
	}

	public Location getIsleLoc(String player) {
		File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
		FileConfiguration fData = YamlConfiguration.loadConfiguration(f);
		Location loc = new Location(Bukkit.getWorld("world_skplots"), fData.getDouble(player + ".x"), fData.getDouble(player + ".y"), fData.getDouble(player + ".z"));
		return loc;
	}

	public void setVisit(Player player) {
		File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
		FileConfiguration fData = YamlConfiguration.loadConfiguration(f);

		try {
			fData.set(player.getUniqueId() + ".visit", !fData.getBoolean(player.getUniqueId() + ".visit"));
			fData.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean canVisit(Player player) {
		File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
		FileConfiguration fData = YamlConfiguration.loadConfiguration(f);

		return fData.getBoolean(player.getUniqueId() + ".visit");
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;

			File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
			FileConfiguration fData = YamlConfiguration.loadConfiguration(f);

			if (fData.isConfigurationSection(player.getUniqueId().toString())) {
				if(player.getWorld().getName().equalsIgnoreCase("world_skyplots")) {
					skyPlotGUI(player, "main", 0);
				} else {
					if(player.hasPermission("skyprisoncore.command.skyplot.teleport")) {
						double x = fData.getDouble(player.getUniqueId() + ".x");
						double y = fData.getDouble(player.getUniqueId() + ".y");
						double z = fData.getDouble(player.getUniqueId() + ".z");
						Location loc = new Location(Bukkit.getWorld("world_skyplots"), x, y, z);
						player.teleport(loc);
						player.sendMessage(plugin.colourMessage("&aSkyPlots &f&l>> &eYou've been teleported to your SkyPlot!"));
					} else {
						player.sendMessage(plugin.colourMessage("&cYou can only use this command on your SkyPlot!"));
					}
				}
			} else {
				if(args.length == 0) {
						TextComponent confirmMsg = Component.text(plugin.colourMessage("&eClick here to confirm the creation of your Sky Plot!"))
								.hoverEvent(HoverEvent.showText(Component.text(plugin.colourMessage("&7Click me to confirm Sky Plot Creation.."))))
								.clickEvent(ClickEvent.runCommand("/skyplot confirm"));
						player.sendMessage(confirmMsg);
						createConfirm.put(player.getUniqueId(), true);
				} else if(args.length == 1) {
					if(args[0].equalsIgnoreCase("confirm")) {
						if(createConfirm.containsKey(player.getUniqueId())) {
							createSkyPlot(player);
							createConfirm.remove(player.getUniqueId());
						} else {
							player.sendMessage(plugin.colourMessage("&cCorrect Usage: /skyplot"));
						}
					}
				}
			}
		}
		return true;
	}
}
