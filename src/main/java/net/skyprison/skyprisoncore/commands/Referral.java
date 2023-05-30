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
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Referral implements CommandExecutor {
	private final SkyPrisonCore plugin;
	private final DatabaseHook db;

	public Referral(SkyPrisonCore plugin, DatabaseHook db) {
		this.plugin = plugin;
		this.db = db;
	}

	public void openGUI(Player player) {
		HashMap<String, String> reffedBy = new HashMap<>();
		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT referred_by, refer_date FROM referrals WHERE user_id = ?")) {
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				reffedBy.put(rs.getString(1), rs.getString(2));
			}
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

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
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
					try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM referrals WHERE referred_by = ?")) {
						ps.setString(1, player.getUniqueId().toString());
						ResultSet rs = ps.executeQuery();
						if(rs.next()) {
							hasReferred = true;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

					if(!hasReferred) {
						if(playtime >= 1 && playtime < 24) { // Checks that the player has played more than an hour on the server but less than 24 hours.
							if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
								CMIUser reffedPlayer = CMI.getInstance().getPlayerManager().getUser(args[0]);
								if(!player.getLastIp().equalsIgnoreCase(reffedPlayer.getLastIp())) {
									try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO referrals (user_id, referred_by, refer_date) VALUES (?, ?, ?)")) {
										ps.setString(1, reffedPlayer.getUniqueId().toString());
										ps.setString(2, player.getUniqueId().toString());
										ps.setLong(3, System.currentTimeMillis());
										ps.executeUpdate();
									} catch (SQLException e) {
										e.printStackTrace();
									}

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
