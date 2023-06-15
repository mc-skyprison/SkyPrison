package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
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
import java.util.*;
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
		HashMap<String, String> reffedName = new HashMap<>();
		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT referred_by, refer_date FROM referrals WHERE user_id = ?")) {
			ps.setString(1, player.getUniqueId().toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				reffedBy.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		List<String> reffedIds = reffedBy.keySet().stream().toList();
		try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, current_name FROM users WHERE user_id IN " + plugin.getQuestionMarks(reffedIds))) {
			for (int i = 0; i < reffedIds.size(); i++) {
				ps.setString(i + 1, reffedIds.get(i));
			}
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				reffedName.put(rs.getString(1), rs.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		Inventory referred = Bukkit.createInventory(null, 54, Component.text("Referral List", NamedTextColor.RED));
		int i = 0;
		for (String reffedPlayer : reffedBy.keySet()) {
			ArrayList<Component> lore = new ArrayList<>();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(reffedPlayer)));
			meta.displayName(Component.text(reffedName.get(reffedPlayer), NamedTextColor.YELLOW, TextDecoration.BOLD)
					.decoration(TextDecoration.ITALIC, false));

			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.parseLong(reffedBy.get(reffedPlayer)));
			lore.add(Component.text("Referred you on: " + df.format(calendar.getTime()), NamedTextColor.YELLOW)
					.decoration(TextDecoration.ITALIC, false));
			meta.lore(lore);
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
		if (sender instanceof Player player) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("help")) {
					player.sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN));
				} else if(args[0].equalsIgnoreCase("list")) {
					openGUI(player);
				} else {
					long playtime = TimeUnit.MILLISECONDS.toHours(user.getTotalPlayTime());

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
								if(!user.getLastIp().equalsIgnoreCase(reffedPlayer.getLastIp())) {
									try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO referrals (user_id, referred_by, refer_date) VALUES (?, ?, ?)")) {
										ps.setString(1, reffedPlayer.getUniqueId().toString());
										ps.setString(2, player.getUniqueId().toString());
										ps.setLong(3, System.currentTimeMillis());
										ps.executeUpdate();
									} catch (SQLException e) {
										e.printStackTrace();
									}
									Component beenReffed = Component.text(player.getName(), NamedTextColor.AQUA).append(Component.text(" has referred you! You have received ", NamedTextColor.DARK_AQUA))
											.append(Component.text("250", NamedTextColor.YELLOW)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA));
									if(reffedPlayer.isOnline()) {
										reffedPlayer.getPlayer().sendMessage(beenReffed);
									} else {
										plugin.createNotification("referred", player.getName(), reffedPlayer.getOfflinePlayer(), beenReffed, null, true);
									}
									player.sendMessage(Component.text("You sucessfully referred ", NamedTextColor.DARK_AQUA)
											.append(Component.text(reffedPlayer.getName(), NamedTextColor.AQUA)).append(Component.text(" and have received ", NamedTextColor.DARK_AQUA))
											.append(Component.text("50", NamedTextColor.GOLD)).append(Component.text(" tokens!", NamedTextColor.DARK_AQUA)));
									plugin.tokens.addTokens(reffedPlayer, 250, "Referred Someone", player.getName());
									plugin.tokens.addTokens(user, 50, "Was Referred", reffedPlayer.getName());
								} else {
									player.sendMessage(Component.text("/referral <player>", NamedTextColor.RED));
								}
							} else {
								player.sendMessage(Component.text("/referral <player>", NamedTextColor.RED));
							}
						} else {
							if(playtime < 1) {
								player.sendMessage(Component.text("You need to play 1 hour to be able to refer someone!", NamedTextColor.RED));
							} else {
								player.sendMessage(Component.text("You have played too long to refer anyone!", NamedTextColor.RED));
							}
						}
					} else {
						player.sendMessage(Component.text("You have already referred someone!", NamedTextColor.RED));
					}
				}
			} else {
				player.sendMessage(Component.text("If a player referred you to our server, you can do \n/referral <player> to give them some tokens!", NamedTextColor.GREEN));
			}
		}
		return true;
	}
}
