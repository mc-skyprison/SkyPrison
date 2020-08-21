package net.skyprison.Main.Commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class RegionTP implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			File f = new File("plugins/SkyPrisonCore/regionLocations.yml");
			YamlConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
			if (args.length < 1 || args[0].equalsIgnoreCase("help")) {
				player.sendMessage(ChatColor.GREEN + "-----===" + ChatColor.AQUA + " RegionTP " + ChatColor.GREEN + "===-----" +
						"\n"+ ChatColor.DARK_GREEN + "/regiontp <region> <spawn> (-s)" + ChatColor.WHITE + " - " + ChatColor.GREEN +"Teleport all players within a region to the specified location" +
						"\n"+ ChatColor.DARK_GREEN + "/regiontp setspawn <name>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Create new regiontp location" +
						"\n"+ ChatColor.DARK_GREEN + "/regiontp delspawn <name>" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Delete a regiontp location" +
						"\n"+ ChatColor.DARK_GREEN + "/regiontp list" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Lists all RegionTP Locations");
			} else if (args[0].equalsIgnoreCase("setspawn")) {
				if (args.length < 2) {
					player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.RED + " Please specify a location name... ");
				} else {
					yamlf.set(args[1] + ".world", player.getLocation().getWorld().getName());
					yamlf.set(args[1] + ".x", Double.valueOf(player.getLocation().getX()));
					yamlf.set(args[1] + ".y", Double.valueOf(player.getLocation().getY()));
					yamlf.set(args[1] + ".z", Double.valueOf(player.getLocation().getZ()));
					yamlf.set(args[1] + ".yaw", Float.valueOf(player.getLocation().getYaw()));
					yamlf.set(args[1] + ".pitch", Float.valueOf(player.getLocation().getPitch()));
					try {
						yamlf.save(f);
					} catch (IOException e) {
						e.printStackTrace();
					}
					player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " Spawn location with name '" + args[1] + "' set at your location");
				}
			} else if (args[0].equalsIgnoreCase("delspawn")) {
				if (args.length < 2) {
					player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.RED + " Please specify a spawn name... ");
				} else if (!yamlf.contains(args[1])) {
					player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.RED + " Spawn location with name '" + args[1] + "' does not exist...");
				} else {
					yamlf.set(args[1], null);
					try {
						yamlf.save(f);
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (!yamlf.contains(args[1])) {
						player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " Spawn location with name '" + args[1] + "' successfully deleted...");
					} else {
						player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.DARK_RED + " ERROR DELETING LOCATION: DELETE LOCATIONS.TXT AND RELOAD PLUGIN...");
					}
				}
			} else if (args[0].equalsIgnoreCase("list")) {
				player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " --Spawn Locations-- ");
				for (String key : yamlf.getKeys(false)) {
					player.sendMessage(ChatColor.GREEN + "  *" + key);
				}
			} else {
				if (args.length < 2) {
					player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.RED + " Please reference /regiontp for help... ");
				} else {
					if (!yamlf.contains(args[1])) {
						player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.RED + " Spawn location with name '" + args[1] + "' does not exist...");
					} else {
						RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionManager regions = container.get(BukkitAdapter.adapt(player).getWorld());
						if (regions.getRegion(args[0]) == null) {
							player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.RED + " Error finding '" + args[0] + "'! Either region does not exist or you are not in the world containing this region...");
						} else {
							int teleported = 0;
							World pw = player.getWorld();
							for (Player ponline : Bukkit.getServer().getOnlinePlayers()) {
								if (args[0].equalsIgnoreCase("__global__") && ponline.getWorld() == pw) {
									World w = Bukkit.getServer().getWorld(yamlf.getString(args[1] + ".world"));
									float yaw = (float) yamlf.getDouble(args[1] + ".yaw");
									float pitch = (float) yamlf.getDouble(args[1] + ".pitch");
									Location location = new Location(w, yamlf.getDouble(args[1] + ".x"), yamlf.getDouble(args[1] + ".y"), yamlf.getDouble(args[1] + ".z"));
									location.setYaw(yaw);
									location.setPitch(pitch);
									ponline.teleport(location);
									teleported++;
									continue;
								} else {
									Location location = ponline.getLocation();
									BlockVector3 v = BlockVector3.at(location.getX(), location.getY(), location.getZ());
									World world = ponline.getWorld();
									RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
									ApplicableRegionSet set = rm.getApplicableRegions(v);
									for (ProtectedRegion r : set) {
										if (r.getId().equalsIgnoreCase(args[0])) {
											World w = Bukkit.getServer().getWorld(yamlf.getString(args[1] + ".world"));
											float yaw = (float) yamlf.getDouble(args[1] + ".yaw");
											float pitch = (float) yamlf.getDouble(args[1] + ".pitch");
											Location Nlocation = new Location(w, yamlf.getDouble(args[1] + ".x"), yamlf.getDouble(args[1] + ".y"), yamlf.getDouble(args[1] + ".z"));
											Nlocation.setYaw(yaw);
											Nlocation.setPitch(pitch);
											ponline.teleport(Nlocation);
											teleported++;
										}
									}
								}
							}
							if(args.length > 3) {
								if(args[2].equalsIgnoreCase(("-s"))) {
								} else {
									player.sendMessage(ChatColor.DARK_GREEN + "/regiontp <region> <spawn> (-s)" + ChatColor.WHITE + " - " + ChatColor.GREEN +"Teleport all players within a region to the specified location.");
								}
							} else {
								player.sendMessage(ChatColor.DARK_RED + "[" + ChatColor.AQUA + "RegionTP" + ChatColor.DARK_RED + "]" + ChatColor.GREEN + " " + teleported + " player(s) sent from region '" + args[0] + "' to location '" + args[1] + "'...");
							}
						}
					}
				}
			}
		}
		return true;
	}
}