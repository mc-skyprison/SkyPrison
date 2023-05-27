package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class SpongeLoc implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public SpongeLoc(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {

			Component help = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY)
					.append(Component.text("Spongeloc Commands").color(TextColor.fromHexString("#FFFF00")))
					.append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY))
					.append(Component.text("\n/spongeloc set").color(TextColor.fromHexString("#008000")))
					.append(Component.text("\n/spongeloc list").color(TextColor.fromHexString("#008000")))
					.append(Component.text("\n/spongeloc delete <id>").color(TextColor.fromHexString("#008000")))
					.append(Component.text("\n/spongeloc tp <id>").color(TextColor.fromHexString("#008000")))
					.decoration(TextDecoration.ITALIC, false);

			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("set")) {
					double x = player.getLocation().getBlockX();
					double y = player.getLocation().getBlockY();
					double z = player.getLocation().getBlockZ();
					World world = player.getWorld();

					String loc = world.getName() + ";" + x + ";" + y + ";" + z;

					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM sponge_locations WHERE location = ?")) {
						ps.setString(1, loc);
						ResultSet rs = ps.executeQuery();
						if(rs.next()) {
							Component msg = Component.text("[").color(NamedTextColor.WHITE)
									.append(Component.text("Sponge").color(TextColor.fromHexString("#FFFF00")))
									.append(Component.text("] ").color(NamedTextColor.WHITE))
									.append(Component.text("There's already a sponge location here!").color(NamedTextColor.RED)).decoration(TextDecoration.ITALIC, false);
							player.sendMessage(msg);
							return true;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

					int max = 0;
					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT MAX(loc_order) FROM sponge_locations")) {
						ResultSet rs = ps.executeQuery();
						if(rs.next()) max = rs.getInt(1);
					} catch(SQLException e) {
							e.printStackTrace();
					}

					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO sponge_locations (location, loc_order) VALUES (?, ?)")) {
						ps.setString(1, loc);
						ps.setInt(2, max + 1);
						ps.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}

					Component msg = Component.text("[").color(NamedTextColor.WHITE)
							.append(Component.text("Sponge").color(TextColor.fromHexString("#FFFF00")))
							.append(Component.text("] ").color(NamedTextColor.WHITE))
							.append(Component.text("Successfully created a sponge location!").color(TextColor.fromHexString("#008000"))).decoration(TextDecoration.ITALIC, false);
					player.sendMessage(msg);
				} else if (args[0].equalsIgnoreCase("list")) {
					HashMap<Integer, String> locs = new HashMap<>();
					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT location, loc_order FROM sponge_locations")) {
						ResultSet rs = ps.executeQuery();
						while(rs.next()) {
							String loc = rs.getString(1);
							int loc_order = rs.getInt(2);
							locs.put(loc_order, loc);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					int page = 1;
					int totalPages = (int) Math.ceil(locs.size() / 10.0);
					if (args.length > 1) {
						if (plugin.isInt(args[1])) {
							page = Integer.parseInt(args[1]);
							if (totalPages < page) {
								page = 1;
							}
						} else {
							Component msg = Component.text("Correct Usage: /spongeloc list (page)").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
							player.sendMessage(msg);
						}
					}

					int prevPage = page - 1;
					int nextPage = page + 1;

					int pageStart = (page * 10) - 9;
					Component msg = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY)
							.append(Component.text("Sponge Locations").color(TextColor.fromHexString("#FFFF00")))
							.append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY));
					for (int i = pageStart; i < pageStart + 10; i++) {
						if(!locs.containsKey(i)) {
							break;
						}
						String[] loc = locs.get(i).split(";");
						msg = msg.append(Component.text("\n" + i + ". ").color(TextColor.fromHexString("#cea916"))
								.append(Component.text("X:" + loc[1] + " Y: " + loc[2] + " Z: " + loc[3]).color(TextColor.fromHexString("#008000")))
								.hoverEvent(HoverEvent.showText(Component.text("Click to teleport to this location").color(NamedTextColor.GRAY)))
								.clickEvent(ClickEvent.runCommand("/spongeloc tp " + i)));
					}
					if (page == 1) {
						if(locs.size() > 10) {
							msg = msg.append(Component.text("\n" + page).color(TextColor.fromHexString("#266d27"))
									.append(Component.text("/").color(NamedTextColor.GRAY)
											.append(Component.text(totalPages).color(TextColor.fromHexString("#266d27"))))
									.append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY)))
											.clickEvent(ClickEvent.runCommand("/spongeloc list " + nextPage))));
						}
					} else if (page == totalPages) {
						msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
								.clickEvent(ClickEvent.runCommand("/spongeloc list " + prevPage))
								.append(Component.text(page).color(TextColor.fromHexString("#266d27"))
										.append(Component.text("/").color(NamedTextColor.GRAY)
												.append(Component.text(totalPages).color(TextColor.fromHexString("#266d27"))))));
					} else {
						msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
								.clickEvent(ClickEvent.runCommand("/spongeloc list " + prevPage))
								.append(Component.text(page).color(TextColor.fromHexString("#266d27"))
										.append(Component.text("/").color(NamedTextColor.GRAY)
												.append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))))
								.append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))))
								.clickEvent(ClickEvent.runCommand("/spongeloc list " + nextPage)));
					}

					msg = msg.decoration(TextDecoration.ITALIC, false);
					player.sendMessage(msg);
				} else if (args[0].equalsIgnoreCase("delete")) {
					// spongeloc delete <id>
					if (args.length > 1) {
						if (plugin.isInt(args[1])) {
							int spongeId = Integer.parseInt(args[1]);
							try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM sponge_locations WHERE loc_order = ?")) {
								ps.setInt(1, spongeId);
								ResultSet rs = ps.executeQuery();
								if (!rs.next()) {
									Component msg = Component.text("[").color(NamedTextColor.WHITE)
											.append(Component.text("Sponge").color(TextColor.fromHexString("#FFFF00")))
											.append(Component.text("] ").color(NamedTextColor.WHITE))
											.append(Component.text("Id doesn't exist!").color(NamedTextColor.RED)).decoration(TextDecoration.ITALIC, false);
									player.sendMessage(msg);
									return true;
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}

							try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM sponge_locations WHERE loc_order = ?")) {
								ps.setInt(1, spongeId);
								ps.executeUpdate();
							} catch (SQLException e) {
								e.printStackTrace();
							}

							try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE sponge_locations SET loc_order = loc_order - 1 WHERE loc_order > ?")) {
								ps.setInt(1, spongeId);
								ps.executeUpdate();
							} catch (SQLException e) {
								e.printStackTrace();
							}

							Component msg = Component.text("[").color(NamedTextColor.WHITE)
									.append(Component.text("Sponge").color(TextColor.fromHexString("#FFFF00")))
									.append(Component.text("] ").color(NamedTextColor.WHITE))
									.append(Component.text("Successfully deleted sponge location!").color(TextColor.fromHexString("#008000"))).decoration(TextDecoration.ITALIC, false);
							player.sendMessage(msg);
						} else {
							Component msg = Component.text("Correct Usage: /spongeloc delete <id>").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
							player.sendMessage(msg);
						}
					} else {
						Component msg = Component.text("Correct Usage: /spongeloc delete <id>").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
						player.sendMessage(msg);
					}
				} else if (args[0].equalsIgnoreCase(("tp"))) {
					if (args.length > 1) {
						if (plugin.isInt(args[1])) {
							int spongeId = Integer.parseInt(args[1]);
							String[] loc = new String[0];
							try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT location FROM sponge_locations WHERE loc_order = ?")) {
								ps.setInt(1, spongeId);
								ResultSet rs = ps.executeQuery();
								if (rs.next()) {
									loc = rs.getString(1).split(";");
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
							Location sLoc = new Location(Bukkit.getWorld(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));

							if (player.teleport(sLoc)) {
								Component msg = Component.text("Successfully teleported to location!").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false);
								player.sendMessage(msg);
							} else {
								Component msg = Component.text("Teleport failed!").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
								player.sendMessage(msg);
							}
						} else {
							Component msg = Component.text("Correct Usage: /spongeloc tp <id>").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
							player.sendMessage(msg);
						}
					} else {
						Component msg = Component.text("Correct Usage: /spongeloc tp <id>").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
						player.sendMessage(msg);
					}
				} else {
					player.sendMessage(help);
				}
			} else {
				player.sendMessage(help);
			}
		}
		return true;
	}
}
