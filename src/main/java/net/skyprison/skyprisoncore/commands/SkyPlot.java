package net.skyprison.skyprisoncore.commands;

import com.google.inject.Inject;
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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class SkyPlot implements CommandExecutor {
	private SkyPrisonCore plugin;

	HashMap<UUID, Boolean> createConfirm = new HashMap<>();

	@Inject
	public SkyPlot(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	public void createSkyPlot(Player player) {
		World w = Bukkit.getWorld("world_skyplots");
		Clipboard clipboard = null;
		File file = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("WorldEdit")).getDataFolder() + File.separator + "schematics" + File.separator + "test2.schem");

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

						Location loc = new Location(w, i, 66, j);
						player.teleportAsync(loc);
						player.sendMessage(plugin.colourMessage("&aSkyPlots &f&l>> &eYour skyplot has successfully been created! Use /skyplot to open the SkyPlot Menu."));
					} catch (WorldEditException e) {
						e.printStackTrace();
					}
					break outerloop;
				}
			}
		}
	}

	public void skyPlotGUI(Player player, String page) {
		switch(page.toLowerCase()) {
			case "main":
				break;
			case "players":
				break;
			case "settings":
				break;
			case "other":
				break;
			case "banned":
				break;
			case "members":
				break;

		}
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;

			File f = new File(plugin.getDataFolder() + File.separator + "skyplots.yml");
			FileConfiguration fData = YamlConfiguration.loadConfiguration(f);

			if (fData.isConfigurationSection(player.getUniqueId().toString())) {
				if(player.getWorld().getName().equalsIgnoreCase("world_skyplots")) {

				} else {
					player.sendMessage(plugin.colourMessage("&cYou can only use this command on your Sky Plot!"));
				}
			} else {
				if(args.length == 0) {
					TextComponent confirmMsg = Component.text(plugin.colourMessage("&eClick here to confirm the creation of your Sky Plot!"))
							.hoverEvent(HoverEvent.showText(Component.text(plugin.colourMessage("&7Click me to confirm Sky Plot Creation.."))))
							.clickEvent(ClickEvent.runCommand("skyplot confirm"));
					player.sendMessage(confirmMsg);
				} else if(args.length == 1) {
					if(args[0].equalsIgnoreCase("confirm")) {
						if(createConfirm.containsKey(player.getUniqueId())) {
							createSkyPlot(player);
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
