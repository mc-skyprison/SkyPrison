package net.skyprison.skyprisoncore.commands.old.donations;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Purchases implements CommandExecutor {
	private final DatabaseHook db;
	private final SkyPrisonCore plugin;

	public Purchases(DatabaseHook db, SkyPrisonCore plugin) {
		this.db = db;
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (sender instanceof Player player) {
			int page = 1;
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if(args.length > 0) {
				if(plugin.isInt(args[0])) {
					page = Integer.parseInt(args[0]);
				} else if(player.hasPermission("skyprisoncore.command.purchases.others")) {
					if(CMI.getInstance().getPlayerManager().getUser(args[0]) != null) {
						user = CMI.getInstance().getPlayerManager().getUser(args[0]);
						if(args.length > 1) {
							if(plugin.isInt(args[1])) {
								page = Integer.parseInt(args[1]);
							}
						}
					}
				}
			}

			List<HashMap<String, String>> donations = new ArrayList<>();
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT item_bought, price, date, currency FROM donations WHERE user_id = ?")) {
				ps.setString(1, user.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					HashMap<String, String> donation = new HashMap<>();
					donation.put("name", rs.getString(1));
					donation.put("price", String.valueOf(rs.getDouble(2)));
					donation.put("date", rs.getString(3));
					donation.put("currency", rs.getString(4));
					donations.add(donation);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			int totalPages = (int) Math.ceil((double) donations.size() / 10);
			if (page > totalPages) {
				page = 1;
			}

			int toDelete = 10 * (page - 1);
			if (toDelete != 0) {
				donations = donations.subList(toDelete, donations.size());
			}

			Component msg = Component.empty();
			msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯", TextColor.fromHexString("#303050"), TextDecoration.STRIKETHROUGH)
					.append(Component.text(" Purchases ", TextColor.fromHexString("#ff0000"), TextDecoration.BOLD).decoration(TextDecoration.STRIKETHROUGH, false))
					.append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯", TextColor.fromHexString("#303050"), TextDecoration.STRIKETHROUGH)));

			int i = 1;
			for(HashMap<String, String> donation : donations) {
				if (i == 11) break;
				msg = msg.append(Component.text("\n" + i + ". ", NamedTextColor.GRAY)
						.append(Component.text(donation.get("name") + " ", TextColor.fromHexString("#ea6c6c"))
						.append(Component.text(" - ", NamedTextColor.WHITE, TextDecoration.BOLD)
						.append(Component.text(donation.get("currency") + " ", TextColor.fromHexString("#565bf0"))
						.append(Component.text(donation.get("price") + " ", TextColor.fromHexString("#565bf0")))).decoration(TextDecoration.BOLD, false)))
						.hoverEvent(HoverEvent.showText(Component.text("Purchased " + donation.get("date"), TextColor.fromHexString("#b83d3d")))));
				i++;
			}
			int prevPage = page - 1;
			int nextPage = page + 1;
			String prevCommand =  Objects.equals(player.getUniqueId(), user.getUniqueId()) ? String.valueOf(prevPage) : user.getName() + " " + prevPage;
			String nextCommand =  Objects.equals(player.getUniqueId(), user.getUniqueId()) ? String.valueOf(nextPage) : user.getName() + " " + nextPage;

			if (page == 1 && page < totalPages) {
				msg = msg.append(Component.text("\n" + page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
						.append(Component.text(totalPages, TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->", NamedTextColor.GRAY)
						.hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/purchases " + nextCommand))));
			} else if (page != 1 && page == totalPages) {
				msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
						.clickEvent(ClickEvent.runCommand("/purchases " + prevCommand))).append(Component.text(page, TextColor.fromHexString("#266d27"))
								.append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27")))));
			} else if (page > 1) {
				msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
						.clickEvent(ClickEvent.runCommand("/purchases " + prevCommand)).append(Component.text(page, TextColor.fromHexString("#266d27"))
								.append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27")))))
						.append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))))
						.clickEvent(ClickEvent.runCommand("/purchases " + nextCommand)));
			}
			player.sendMessage(msg);
		}
		return true;
	}
}
