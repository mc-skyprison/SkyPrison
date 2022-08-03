package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.CooldownManager;
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
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Bounty implements CommandExecutor {
	private final DatabaseHook hook;
	private final SkyPrisonCore plugin;

	public Bounty(DatabaseHook hook, SkyPrisonCore plugin) {
		this.hook = hook;
		this.plugin = plugin;
	}

	public void openGUI(Player player, int page) {
		HashMap<UUID, Double> bountyPlayers = new HashMap<>();
		try {
			Connection conn = hook.getSQLConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT user_id, prize FROM bounties");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				bountyPlayers.put(UUID.fromString(rs.getString(1)), rs.getDouble(2));
			}
			hook.close(ps, rs, conn);
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

		Inventory bounties = Bukkit.createInventory(null, 54, ChatColor.RED + "Bounties | Page " + page);
		int j = 0;
		for (UUID bountyPlayer : sortedMap.keySet()) {
			if(j == 45) break;
			ArrayList<String> lore = new ArrayList<>();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(bountyPlayer));
			meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Bukkit.getOfflinePlayer(bountyPlayer).getName());
			lore.add(ChatColor.YELLOW + "Prize: $" + sortedMap.get(bountyPlayer));
			meta.setLore(lore);

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
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		nextPage.setItemMeta(nextMeta);
		ItemStack prevPage = new ItemStack(Material.PAPER);
		ItemMeta prevMeta = prevPage.getItemMeta();
		prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
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

	private final CooldownManager cooldownManager = new CooldownManager();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

			//  /bounty set <player> <prize>
			String bountyHelp = ChatColor.WHITE + "----====" + ChatColor.RED + " Bounties " + ChatColor.WHITE + "====----" + ChatColor.YELLOW + "\n/bounty set <player> <amount> " + ChatColor.WHITE + "- Set a bounty on a player" + ChatColor.YELLOW + "\n/bounty help " + ChatColor.WHITE + "- Shows this" + ChatColor.YELLOW + "\n/bounty list " + ChatColor.WHITE + "- Shows all players with bounties" + ChatColor.YELLOW + "\n/bounty mute " + ChatColor.WHITE + "- Mutes/Unmutes bounty messages except for bounties towards yourself";
			if(args.length < 1) {
				player.sendMessage(bountyHelp);
			}else if(args[0].equalsIgnoreCase("set")) {
				long timeLeft = System.currentTimeMillis() - cooldownManager.getCooldown(player.getUniqueId());
				if(TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= CooldownManager.DEFAULT_COOLDOWN) {
					if (!(args.length < 3)) {
						if (Bukkit.getPlayer(args[1]) != null) {
							if (Double.parseDouble(args[2]) >= 100) {
								Player bountiedPlayer = Bukkit.getPlayer(args[1]);
								String bountyTarget = bountiedPlayer.getUniqueId().toString();
								if (!player.equals(Bukkit.getPlayer(args[1]))) {
									if (!bountiedPlayer.hasPermission("skyprisoncore.command.bounty.bypass")) {
										String bountiedBy = "";
										boolean hasBounty = false;

										try {
											Connection conn = hook.getSQLConnection();
											PreparedStatement ps = conn.prepareStatement("SELECT bountied_by FROM bounties WHERE user_id = '" + bountyTarget + "'");
											ResultSet rs = ps.executeQuery();
											while(rs.next()) {
												hasBounty = true;
												bountiedBy = rs.getString(1);
												bountiedBy = bountiedBy.replace("[", "");
												bountiedBy = bountiedBy.replace("]", "");
												bountiedBy = bountiedBy.replace(" ", "");
											}
											hook.close(ps, rs, conn);
										} catch (SQLException e) {
											e.printStackTrace();
										}

										double bountyPrize = round(Double.parseDouble(args[2]), 2);
										if (user.getBalance() >= bountyPrize) {
											if (hasBounty) {
												for (Player online : Bukkit.getServer().getOnlinePlayers()) {
													if (!online.hasPermission("skyprisoncore.command.bounty.silent")) {
														online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + player.getName() + " has increased the bounty on " + bountiedPlayer.getName() + " by " + ChatColor.GREEN + "$" + bountyPrize + "!");
													}
												}
												bountiedPlayer.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + player.getName() + " has increased the bounty on you!!");
												Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "money take " + player.getName() + " " + bountyPrize);
												cooldownManager.setCooldown(player.getUniqueId(), System.currentTimeMillis());

												String sql = "UPDATE bounties SET prize = prize + ?, bountied_by = ? WHERE user_id = ?";
												bountiedBy += "," + player.getUniqueId();
												String finalBountiedBy = bountiedBy;
												List<Object> params = new ArrayList<Object>() {{
													add(bountyPrize);
													add(finalBountiedBy);
													add(bountyTarget);
												}};
												hook.sqlUpdate(sql, params);
											} else {
												for (Player online : Bukkit.getServer().getOnlinePlayers()) {
													if (!online.hasPermission("skyprisoncore.command.bounty.silent")) {
														online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "]" + ChatColor.YELLOW + " " + player.getName() + " has put a " + ChatColor.GREEN + "$" + bountyPrize + ChatColor.YELLOW + " bounty on " + bountiedPlayer.getName() + "!");
													}
												}
												bountiedPlayer.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + player.getName() + " has put a bounty on you!");
												Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "money take " + player.getName() + " " + bountyPrize);
												cooldownManager.setCooldown(player.getUniqueId(), System.currentTimeMillis());

												String sql = "INSERT INTO bounties (user_id, prize, bountied_by) VALUES (?, ?, ?)";
												List<Object> params = new ArrayList<Object>() {{
													add(bountyTarget);
													add(bountyPrize);
													add(player.getUniqueId().toString());
												}};
												hook.sqlUpdate(sql, params);
											}
										} else {
											player.sendMessage(ChatColor.RED + "You do not have enough money..");
										}
									} else {
										player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.RED + "You can't put a bounty on this player!");
									}
								} else {
									player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.RED + "You can't put a bounty on yourself!");
								}
							} else {
								player.sendMessage(ChatColor.RED + "Bounty must be equal or higher than $100!");
							}
						} else {
							player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.RED + "Player is not online or doesn't exist..");
						}
					} else {
						player.sendMessage(ChatColor.RED + "/bounty set <player> <amount>");
					}
				} else {
					Long timeRem = CooldownManager.DEFAULT_COOLDOWN - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
					player.sendMessage(ChatColor.RED+ "" + timeRem + " seconds before you can use this again.");
				}
			} else if(args[0].equalsIgnoreCase("list")) {
				openGUI(player, 1);
			} else if(args[0].equalsIgnoreCase("help")) {
				player.sendMessage(bountyHelp);
			} else if(args[0].equalsIgnoreCase("mute")) {
				if(!player.hasPermission("skyprisoncore.command.bounty.silent")) {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.command.bounty.silent true");
					player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Bounty messages muted!");
				} else {
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getName() + " permission set skyprisoncore.command.bounty.silent false");
					player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Bounty messages unmuted!");
				}
			}
		}
		return true;
	}
}

