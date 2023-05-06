package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Referral implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DiscordApi discApi;
	private final DatabaseHook hook;

	public Referral(SkyPrisonCore plugin, DiscordApi discApi, DatabaseHook hook) {
		this.plugin = plugin;
		this.discApi = discApi;
		this.hook = hook;
	}

	public void openGUI(Player player) {
		HashMap<String, String> reffedBy = new HashMap<>();
		try {
			Connection conn = hook.getSQLConnection();
			PreparedStatement ps = conn.prepareStatement("SELECT referred_by, refer_date FROM referrals WHERE user_id = '" + player.getUniqueId() + "'");
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				reffedBy.put(rs.getString(1), rs.getString(2));
			}
			hook.close(ps, rs, conn);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Inventory referred = Bukkit.createInventory(null, 54, ChatColor.RED + "Referral List");
		int i = 0;
		for (String reffedPlayer : reffedBy.keySet()) {
			ArrayList<String> lore = new ArrayList<>();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(reffedPlayer)));
			meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Bukkit.getOfflinePlayer(UUID.fromString(reffedPlayer)).getName());

			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.parseLong(reffedBy.get(reffedPlayer)));
			lore.add(ChatColor.YELLOW + "Referred you on: " + df.format(calendar.getTime()));
			meta.setLore(lore);
			head.setItemMeta(meta);
			referred.setItem(i, head);
			i++;
		}
		ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		for (int b = 45; b < 54; b++) {
			referred.setItem(b, pane);
		}
		player.openInventory(referred);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			CMIUser player = CMI.getInstance().getPlayerManager().getUser((Player) sender);
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("help")) {
					player.sendMessage(ChatColor.GREEN + "If a player referred you to our server, you can do \n/referral <player> to give them some tokens!");
				} else if(args[0].equalsIgnoreCase("list")) {
					openGUI(player.getPlayer());
				} else {
					long playtime = TimeUnit.MILLISECONDS.toHours(player.getTotalPlayTime());

					boolean hasReferred = false;
					try {
						Connection conn = hook.getSQLConnection();
						PreparedStatement ps = conn.prepareStatement("SELECT * FROM referrals WHERE referred_by = '" + player.getUniqueId() + "'");
						ResultSet rs = ps.executeQuery();
						if(rs.next()) {
							hasReferred = true;
						}
						hook.close(ps, rs, conn);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					if(!hasReferred) {
						if(playtime >= 1 && playtime < 24) { // Checks that the player has played more than an hour on the server but less than 24 hours.
							if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
								CMIUser reffedPlayer = CMI.getInstance().getPlayerManager().getUser(args[0]);
								if(!player.getLastIp().equalsIgnoreCase(reffedPlayer.getLastIp())) {
									long discordId = 0;
									try {
										Connection conn = hook.getSQLConnection();
										PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = '" + reffedPlayer.getUniqueId() + "'");
										ResultSet rs = ps.executeQuery();
										while(rs.next()) {
											discordId = rs.getLong(1);
										}
										hook.close(ps, rs, conn);
									} catch (SQLException e) {
										e.printStackTrace();
									}

									if (discordId != 0) {
										try {
											User user = discApi.getUserById(discordId).get();
											int refs = 0;
											try {
												Connection conn = hook.getSQLConnection();
												PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM referrals WHERE user_id = '" + reffedPlayer.getUniqueId() + "'");
												ResultSet rs = ps.executeQuery();
												while(rs.next()) {
													refs = rs.getInt(1);
												}
												hook.close(ps, rs, conn);
											} catch (SQLException e) {
												e.printStackTrace();
											}

											if(refs == 3) {
												Role refRole = discApi.getRoleById("807052387734519858").get();
												user.addRole(refRole);
											} else if(refs == 5) {
												Role refRole = discApi.getRoleById("807052580547067964").get();
												user.addRole(refRole);
											} else if(refs == 10) {
												Role refRole = discApi.getRoleById("807052646015434792").get();
												user.addRole(refRole);
											}
										} catch (InterruptedException | ExecutionException e) {
											e.printStackTrace();
										}

									}

									String sql = "INSERT INTO referrals (user_id, referred_by, refer_date) VALUES (?, ?, ?)";
									List<Object> params = new ArrayList<>() {{
										add(reffedPlayer.getUniqueId().toString());
										add(player.getUniqueId().toString());
										add(System.currentTimeMillis());
									}};
									hook.sqlUpdate(sql, params);

									if(reffedPlayer.isOnline()) {
										reffedPlayer.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA
												+ " has referred you! You have received " + ChatColor.YELLOW + "250" + ChatColor.DARK_AQUA + " tokens!");
									} else {
										plugin.asConsole("mail send " + reffedPlayer.getName()
												+ " " + ChatColor.AQUA + player.getName() + ChatColor.DARK_AQUA
												+ " has referred you! You have received " + ChatColor.YELLOW + "250" + ChatColor.DARK_AQUA + " tokens!");
									}
									player.sendMessage(ChatColor.DARK_AQUA + "You successfully referred " + ChatColor.AQUA + reffedPlayer.getName()
											+ ChatColor.DARK_AQUA + " and have received " + ChatColor.YELLOW + "50" + ChatColor.DARK_AQUA + " tokens!");
									plugin.tokens.addTokens(reffedPlayer, 250, "Referred Someone", player.getName());
									plugin.tokens.addTokens(player, 50, "Was Referred", reffedPlayer.getName());
								} else {
									player.sendMessage(ChatColor.RED + "/referral <player>");
								}
							} else {
								player.sendMessage(ChatColor.RED + "/referral <player>");
							}
						} else {
							if(playtime < 1) {
								player.sendMessage(ChatColor.RED + "You need to play 1 hour to be able to refer someone!");
							} else {
								player.sendMessage(ChatColor.RED + "You have played too long to refer anyone!");
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "You have already referred someone!");
					}
				}
			} else {
				player.sendMessage(ChatColor.GREEN + "If a player referred you to our server, you can do \n/referral <player> to give them some tokens!");
			}
		}
		return true;
	}
}
