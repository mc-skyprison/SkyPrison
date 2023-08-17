package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.CooldownManager;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bounty implements CommandExecutor {
	private final DatabaseHook db;
	private final SkyPrisonCore plugin;

	public Bounty(DatabaseHook db, SkyPrisonCore plugin) {
		this.db = db;
		this.plugin = plugin;
	}

	public void openGUI(Player player, int page) {
		HashMap<UUID, Double> bountyPlayers = new HashMap<>();
		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, prize FROM bounties")) {
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				bountyPlayers.put(UUID.fromString(rs.getString(1)), rs.getDouble(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		LinkedHashMap<UUID, Double> sortedMap = new LinkedHashMap<>();

		bountyPlayers.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

		double totalPages = Math.ceil(bountyPlayers.size() / 45.0);

		int toRemove = 45 * (page - 1);
		if(toRemove != 0) {
			toRemove -= 1;
		}
		int b = 0;
		ArrayList<UUID> toBeRemoved = new ArrayList<>();

		for(UUID bountiedPlayer : sortedMap.keySet()) {
			if(b == toRemove) break;
			toBeRemoved.add(bountiedPlayer);
			b++;
		}

		for(UUID beGone : toBeRemoved) {
			sortedMap.remove(beGone);
		}

		Inventory bounties = Bukkit.createInventory(null, 54, Component.text("Bounties | Page " + page, NamedTextColor.RED));
		int j = 0;
		for (UUID bountyPlayer : sortedMap.keySet()) {
			if(j == 45) break;
			ArrayList<Component> lore = new ArrayList<>();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(bountyPlayer));
			meta.displayName(Component.text(Objects.requireNonNull(Bukkit.getOfflinePlayer(bountyPlayer).getName()), NamedTextColor.YELLOW, TextDecoration.BOLD));
			lore.add(0, Component.text("Price: ", NamedTextColor.YELLOW).append(Component.text("$" + sortedMap.get(bountyPlayer), NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));

			meta.lore(lore);

			if(j == 0) {
				NamespacedKey key = new NamespacedKey(plugin, "stop-click");
				meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
				NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
				meta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "bounties");

				NamespacedKey key4 = new NamespacedKey(plugin, "page");
				meta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);
			}

			head.setItemMeta(meta);
			bounties.setItem(j, head);
			j++;
		}


		ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack nextPage = new ItemStack(Material.PAPER);
		ItemMeta nextMeta = nextPage.getItemMeta();
		nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN));
		nextPage.setItemMeta(nextMeta);
		ItemStack prevPage = new ItemStack(Material.PAPER);
		ItemMeta prevMeta = prevPage.getItemMeta();
		prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN));
		prevPage.setItemMeta(prevMeta);
		for(int i = 45; i < 54; i++) {
			bounties.setItem(i, pane);
		}

		if(page == totalPages && page > 1) {
			bounties.setItem(46, prevPage);
		} else if(page != totalPages && page == 1) {
			bounties.setItem(52, nextPage);
		} else if (page != 1) {
			bounties.setItem(46, prevPage);
			bounties.setItem(52, nextPage);
		}

		player.openInventory(bounties);
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private final Component prefix = Component.text( "[", NamedTextColor.WHITE).append(Component.text("Bounties", NamedTextColor.RED).append(Component.text("] ", NamedTextColor.WHITE)));

	private final CooldownManager cooldownManager = new CooldownManager();

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

			//  /bounty set <player> <prize>
			Component helpMsg = Component.textOfChildren(Component.text("----==== ", NamedTextColor.WHITE)
					.append(Component.text("Bounties", NamedTextColor.RED))
					.append(Component.text("====----", NamedTextColor.WHITE)))
					.append(Component.text("\n/bounty set <player> <amount>", NamedTextColor.YELLOW)
							.append(Component.text(" - Set a bounty on a player", NamedTextColor.WHITE)))
					.append(Component.text("\n/bounty help", NamedTextColor.YELLOW)
							.append(Component.text(" - Shows this", NamedTextColor.WHITE)))
					.append(Component.text("\n/bounty list", NamedTextColor.YELLOW)
							.append(Component.text(" - Shows all players with bountiesr", NamedTextColor.WHITE)))
					.append(Component.text("\n/bounty mute", NamedTextColor.YELLOW)
							.append(Component.text(" - Mutes/Unmutes bounty messages except for bounties towards yourself", NamedTextColor.WHITE)));

			if(args.length < 1) {
				player.sendMessage(helpMsg);
			}else if(args[0].equalsIgnoreCase("set")) {
				long timeLeft = System.currentTimeMillis() - cooldownManager.getCooldown(player.getUniqueId());
				if(TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= CooldownManager.DEFAULT_COOLDOWN) {
					if (!(args.length < 3)) {
						if (Bukkit.getPlayer(args[1]) != null) {
							if (Double.parseDouble(args[2]) >= 100) {
								Player bountiedPlayer = Bukkit.getPlayer(args[1]);
								String bountyTarget = Objects.requireNonNull(bountiedPlayer).getUniqueId().toString();
								if (!player.equals(Bukkit.getPlayer(args[1]))) {
									if (!bountiedPlayer.hasPermission("skyprisoncore.command.bounty.bypass")) {
										String bountiedBy = "";
										boolean hasBounty = false;

										try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT bountied_by FROM bounties WHERE user_id = ?")) {
											ps.setString(1, bountyTarget);
											ResultSet rs = ps.executeQuery();
											while(rs.next()) {
												hasBounty = true;
												bountiedBy = rs.getString(1);
												bountiedBy = bountiedBy.replace("[", "");
												bountiedBy = bountiedBy.replace("]", "");
												bountiedBy = bountiedBy.replace(" ", "");
											}
										} catch (SQLException e) {
											e.printStackTrace();
										}

										double bountyPrize = round(Double.parseDouble(args[2]), 2);
										if (user.getBalance() >= bountyPrize) {
											if (hasBounty) {
												for (Player online : Bukkit.getServer().getOnlinePlayers()) {
													if (!online.hasPermission("skyprisoncore.command.bounty.silent")) {
														online.sendMessage(prefix.append(Component.text(player.getName() + " has increased the bounty on " + bountiedPlayer.getName() + " by ", NamedTextColor.YELLOW)
																.append(Component.text("$" + plugin.formatNumber(bountyPrize) + "!", NamedTextColor.GREEN))));
													}
												}
												bountiedPlayer.sendMessage(prefix.append(Component.text(player.getName() + " has increased the bounty on you!", NamedTextColor.YELLOW)));
												Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi money take " + player.getName() + " " + plugin.formatNumber(bountyPrize));
												cooldownManager.setCooldown(player.getUniqueId(), System.currentTimeMillis());

												try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE bounties SET prize = prize + ?, bountied_by = ? WHERE user_id = ?")) {
													ps.setDouble(1, bountyPrize);
													ps.setString(2, bountiedBy);
													ps.setString(3, bountyTarget);
													ps.executeUpdate();
												} catch (SQLException e) {
													e.printStackTrace();
												}
											} else {
												for (Player online : Bukkit.getServer().getOnlinePlayers()) {
													if (!online.hasPermission("skyprisoncore.command.bounty.silent")) {
														online.sendMessage(prefix.append(Component.text(player.getName() + " has put a ", NamedTextColor.YELLOW)
																.append(Component.text("$" + plugin.formatNumber(bountyPrize), NamedTextColor.GREEN))
																.append(Component.text(" bounty on " + bountiedPlayer.getName() + "!", NamedTextColor.YELLOW))));
													}
												}
												bountiedPlayer.sendMessage(prefix.append(Component.text(player.getName() + " has put a bounty on you!", NamedTextColor.YELLOW)));
												Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi money take " + player.getName() + " " + bountyPrize);
												cooldownManager.setCooldown(player.getUniqueId(), System.currentTimeMillis());

												try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO bounties (user_id, prize, bountied_by) VALUES (?, ?, ?)")) {
													ps.setString(1, bountyTarget);
													ps.setDouble(2, bountyPrize);
													ps.setString(3, player.getUniqueId().toString());
													ps.executeUpdate();
												} catch (SQLException e) {
													e.printStackTrace();
												}
											}
										} else {
											player.sendMessage(prefix.append(Component.text("You do not have enough money..", NamedTextColor.RED)));
										}
									} else {
										player.sendMessage(prefix.append(Component.text("You can't put a bounty on this player!", NamedTextColor.RED)));
									}
								} else {
									player.sendMessage(prefix.append(Component.text("You can't put a bounty on yourself!", NamedTextColor.RED)));
								}
							} else {
								player.sendMessage(prefix.append(Component.text("Bounty must be equal or higher than $100!", NamedTextColor.RED)));
							}
						} else {
							player.sendMessage(prefix.append(Component.text("Player is not online or doesn't exist..", NamedTextColor.RED)));
						}
					} else {
						player.sendMessage(Component.text("Incorrect Usage! /bounty set <player> <amount>", NamedTextColor.RED));
					}
				} else {
					Long timeRem = CooldownManager.DEFAULT_COOLDOWN - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
					player.sendMessage(prefix.append(Component.text(timeRem + " seconds before you can use this again.", NamedTextColor.RED)));
				}
			} else if(args[0].equalsIgnoreCase("list")) {
				openGUI(player, 1);
			} else if(args[0].equalsIgnoreCase("help")) {
				player.sendMessage(helpMsg);
			} else if(args[0].equalsIgnoreCase("mute")) {
				if(!player.hasPermission("skyprisoncore.command.bounty.silent")) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.command.bounty.silent true");
					player.sendMessage(prefix.append(Component.text("Bounty messages muted!", NamedTextColor.YELLOW)));
				} else {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.command.bounty.silent false");
					player.sendMessage(prefix.append(Component.text("Bounty messages unmuted!", NamedTextColor.YELLOW)));
				}
			}
		}
		return true;
	}
}

