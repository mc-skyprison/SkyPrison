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
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class plots implements CommandExecutor {
	private SkyPrisonCore plugin;

	@Inject
	public plots(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
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
						player.sendMessage("wham1");
						try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(w))) {
							Location loc = new Location(w, i+50, 100, j+50);
							player.teleportAsync(loc);
							player.sendMessage("wham2");
							Operation operation = new ClipboardHolder(clipboard)
									.createPaste(editSession)
									.to(BlockVector3.at(i, 64, j))
									.build();
							Operations.complete(operation);
							ProtectedRegion pRegion = new ProtectedCuboidRegion(player.getUniqueId() + "_main" + j+50 + "-" + i+50, BlockVector3.at(i+50, 0, j+50), BlockVector3.at(i-50, 255, j-50));
							final DefaultDomain owner = pRegion.getOwners();
							owner.addPlayer(player.getUniqueId());
							pRegion.setOwners(owner);
							regions.addRegion(pRegion);
						} catch (WorldEditException e) {
							e.printStackTrace();
						}
						break outerloop;
					}
				}
			}
		}
		return true;
	}
}
