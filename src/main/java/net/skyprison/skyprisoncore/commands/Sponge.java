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

public class Sponge implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public final Component prefix = Component.text("Sponge ", TextColor.fromHexString("#FFFF00")).append(Component.text(" | ", NamedTextColor.WHITE));

	public Sponge(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {

			Component help = Component.empty();
			help = help.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
					.append(Component.text(" Sponge Commands ", TextColor.fromHexString("#FFFF00"), TextDecoration.BOLD))
					.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
					.append(Component.text("\n/sponge set").color(TextColor.fromHexString("#7fff00")))
					.append(Component.text("\n/sponge list").color(TextColor.fromHexString("#7fff00")))
					.append(Component.text("\n/sponge delete <id>").color(TextColor.fromHexString("#7fff00")))
					.append(Component.text("\n/sponge tp <id>").color(TextColor.fromHexString("#7fff00")));
			if (args.length > 0) {
				switch(args[0].toLowerCase()) {
					case "set" -> {
						int x = player.getLocation().getBlockX();
						int y = player.getLocation().getBlockY();
						int z = player.getLocation().getBlockZ();
						String world = player.getWorld().getName();

						try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM sponge_locations WHERE world = ? AND x = ? AND y = ? and z = ?")) {
							ps.setString(1, world);
							ps.setInt(2, x);
							ps.setInt(3, y);
							ps.setInt(4, z);
							ResultSet rs = ps.executeQuery();
							if(rs.next()) {
								Component msg = prefix.append(Component.text("There's already a sponge location here!", NamedTextColor.RED));
								player.sendMessage(msg);
								return true;
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}

						int max = 0;
						try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT MAX(order_id) FROM sponge_locations")) {
							ResultSet rs = ps.executeQuery();
							if(rs.next()) max = rs.getInt(1);
						} catch(SQLException e) {
							e.printStackTrace();
						}

						try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO sponge_locations (order_id, world, x, y, z) VALUES (?, ?, ?, ?, ?)")) {
							ps.setInt(1, max + 1);
							ps.setString(2, world);
							ps.setInt(3, x);
							ps.setInt(4, y);
							ps.setInt(5, z);
							ps.executeUpdate();
							player.sendMessage(prefix.append(Component.text("Successfully created a sponge location!").color(TextColor.fromHexString("#7fff00"))));
						} catch (SQLException e) {
							e.printStackTrace();
						}

					}
					case "list" -> {
						HashMap<Integer, Location> locs = new HashMap<>();
						try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT order_id, world, x, y, z FROM sponge_locations")) {
							ResultSet rs = ps.executeQuery();
							while(rs.next()) {
								int order = rs.getInt(1);
								Location loc = new Location(Bukkit.getWorld(rs.getString(2)), rs.getInt(3), rs.getInt(4), rs.getInt(5));
								locs.put(order, loc);
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
								Component msg = Component.text("Correct Usage: /sponge list (page)", NamedTextColor.RED);
								player.sendMessage(msg);
							}
						}

						int prevPage = page - 1;
						int nextPage = page + 1;

						int pageStart = (page * 10) - 9;
						Component msg = Component.empty();
						msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
								.append(Component.text(" Sponge Locations ", TextColor.fromHexString("#FFFF00"), TextDecoration.BOLD))
								.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
						for (int i = pageStart; i < pageStart + 10; i++) {
							if(!locs.containsKey(i)) break;
							Location loc = locs.get(i);
							msg = msg.append(Component.text("\n" + i + ". ").color(TextColor.fromHexString("#cea916"))
									.append(Component.text("X " + loc.getBlockX() + " Y " + loc.getBlockY() + " Z " + loc.getBlockZ()).color(TextColor.fromHexString("#7fff00")))
									.hoverEvent(HoverEvent.showText(Component.text("Click to teleport to this location").color(NamedTextColor.GRAY)))
									.clickEvent(ClickEvent.runCommand("/sponge tp " + i)));
						}
						if (page == 1) {
							if(locs.size() > 10) {
								msg = msg.append(Component.text("\n" + page).color(TextColor.fromHexString("#266d27"))
										.append(Component.text("/").color(NamedTextColor.GRAY)
												.append(Component.text(totalPages).color(TextColor.fromHexString("#266d27"))))
										.append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY)))
												.clickEvent(ClickEvent.runCommand("/sponge list " + nextPage))));
							}
						} else if (page == totalPages) {
							msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
									.clickEvent(ClickEvent.runCommand("/sponge list " + prevPage))
									.append(Component.text(page).color(TextColor.fromHexString("#266d27"))
											.append(Component.text("/").color(NamedTextColor.GRAY)
													.append(Component.text(totalPages).color(TextColor.fromHexString("#266d27"))))));
						} else {
							msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
									.clickEvent(ClickEvent.runCommand("/sponge list " + prevPage))
									.append(Component.text(page).color(TextColor.fromHexString("#266d27"))
											.append(Component.text("/").color(NamedTextColor.GRAY)
													.append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))))
									.append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))))
									.clickEvent(ClickEvent.runCommand("/sponge list " + nextPage)));
						}
						player.sendMessage(msg);
					}
					case "delete" -> {
						// sponge delete <id>
						if (args.length > 1) {
							if (plugin.isInt(args[1])) {
								int spongeId = Integer.parseInt(args[1]);
								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT order_id FROM sponge_locations WHERE order_id = ?")) {
									ps.setInt(1, spongeId);
									ResultSet rs = ps.executeQuery();
									if (!rs.next()) {
										player.sendMessage(prefix.append(Component.text("No sponge location with that ID!", NamedTextColor.RED)));
										return true;
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM sponge_locations WHERE order_id = ?")) {
									ps.setInt(1, spongeId);
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}

								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE sponge_locations SET order_id = order_id - 1 WHERE order_id > ?")) {
									ps.setInt(1, spongeId);
									ps.executeUpdate();
								} catch (SQLException e) {
									e.printStackTrace();
								}

								Component msg = prefix.append(Component.text("Successfully deleted sponge location!").color(TextColor.fromHexString("#7fff00")));
								player.sendMessage(msg);
							} else {
								Component msg = Component.text("Correct Usage: /sponge delete <id>", NamedTextColor.RED);
								player.sendMessage(msg);
							}
						} else {
							Component msg = Component.text("Correct Usage: /sponge delete <id>", NamedTextColor.RED);
							player.sendMessage(msg);
						}
					}
					case "tp" -> {
						if (args.length > 1) {
							if (plugin.isInt(args[1])) {
								int spongeId = Integer.parseInt(args[1]);
								Location loc = null;
								try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT world, x, y, z FROM sponge_locations WHERE order_id = ?")) {
									ps.setInt(1, spongeId);
									ResultSet rs = ps.executeQuery();
									if (rs.next()) {
										loc = new Location(Bukkit.getWorld(rs.getString(1)), rs.getInt(2), rs.getInt(3), rs.getInt(4));
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
								if (loc != null && player.teleport(loc)) {
									player.sendMessage(prefix.append(Component.text("Successfully teleported to location!").color(NamedTextColor.GREEN)));
								} else {
									player.sendMessage(prefix.append(Component.text("Teleport failed!", NamedTextColor.RED)));
								}
							} else {
								player.sendMessage(prefix.append(Component.text("Correct Usage: /sponge tp <id>", NamedTextColor.RED)));
							}
						} else {
							player.sendMessage(prefix.append(Component.text("Correct Usage: /sponge tp <id>", NamedTextColor.RED)));
						}
					}
					default -> player.sendMessage(help);
				}
			} else {
				player.sendMessage(help);
			}
		}
		return true;
	}
}
