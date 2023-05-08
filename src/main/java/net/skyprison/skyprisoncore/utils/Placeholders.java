package net.skyprison.skyprisoncore.utils;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Placeholders extends PlaceholderExpansion {
	private final SkyPrisonCore plugin;
	private final DailyMissions dailyMissions;
	private final DatabaseHook hook;

	public Placeholders(SkyPrisonCore plugin, DailyMissions dailyMissions, DatabaseHook hook) {
		this.plugin = plugin;
		this.dailyMissions = dailyMissions;
		this.hook = hook;
	}

	@Override
	public boolean persist(){
		return true;
	}

	@Override
	public boolean canRegister(){
		return true;
	}

	@Override
	public String getAuthor(){
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier(){
		return "SkyPrisonCore";
	}

	@Override
	public String getVersion(){
		return plugin.getDescription().getVersion();
	}

	private String getBusPrice(Player player, String identifier) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Location warpLoc = CMI.getInstance().getWarpManager().getWarp(identifier).getLoc();
		int dist = (int) user.getLocation().distance(warpLoc);
		if(dist < 10) {
			return ChatColor.GRAY + "" + ChatColor.ITALIC + "You Are Here";
		} else {
			if(!player.hasPermission("skyprisoncore.command.transportpass.bus")) {
				if (user.getWorld().getTime() > 0 && user.getWorld().getTime() < 12300) {
					return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "$" + dist;
				} else {
					int nightDist = (int) (dist * 1.5);
					return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "$" + nightDist;
				}
			} else {
				return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "FREE";
			}
		}
	}

	private String getBusPriceInt(Player player, String identifier) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Location warpLoc = CMI.getInstance().getWarpManager().getWarp(identifier).getLoc();
		int dist = (int) user.getLocation().distance(warpLoc);
		if(dist < 10) {
			return "0";
		} else {
			if(!player.hasPermission("skyprisoncore.command.transportpass.bus")) {
				if (user.getWorld().getTime() > 0 && user.getWorld().getTime() < 12300) {
					return String.valueOf(dist);
				} else {
					int nightDist = (int) (dist * 1.5);
					return String.valueOf(nightDist);
				}
			} else {
				return "0";
			}
		}
	}

	private String getTrainPrice(Player player, String identifier) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Location warpLoc = CMI.getInstance().getWarpManager().getWarp(identifier).getLoc();
		int dist = (int) user.getLocation().distance(warpLoc);
		if(dist < 50) {
			return ChatColor.GRAY + "" + ChatColor.ITALIC + "You Are Here";
		} else {
			if(!player.hasPermission("skyprisoncore.command.transportpass.train"))
				return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "$" + dist;
			else
				return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "FREE";
		}
	}

	private String getTrainPriceInt(Player player, String identifier) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Location warpLoc = CMI.getInstance().getWarpManager().getWarp(identifier).getLoc();
		int dist = (int) user.getLocation().distance(warpLoc);
		if(dist < 50) {
			return "0";
		} else {
			if(!player.hasPermission("skyprisoncore.command.transportpass.train"))
				return String.valueOf(dist);
			else
				return "0";
		}
	}


	@Override
	public String onPlaceholderRequest(Player player, String identifier){

		if(player == null) {
			return "";
		}

		if(identifier.equalsIgnoreCase("daily_highest_streak")) {
			int highestStreak = 0;
			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT highest_streak FROM dailies WHERE user_id = '" + player.getUniqueId() + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					highestStreak = rs.getInt(1);
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(highestStreak);
		}


		if(identifier.equalsIgnoreCase("daily_total_collected")) {
			int totalColl = 0;
			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT total_collected FROM dailies WHERE user_id = '" + player.getUniqueId() + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					totalColl = rs.getInt(1);
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(totalColl);
		}

		if(identifier.equalsIgnoreCase("daily_mission_one")) {
			if(dailyMissions.getMissions(player).isEmpty()) {
				return "&7-";
			}
			String mission = dailyMissions.getMissions(player).get(0);
			String[] mSplit = mission.split("-");
			int amount = dailyMissions.getMissionAmount(player, mission);
			int needed = dailyMissions.getMissionNeeded(player, mission);

			String amFormat = plugin.formatNumber(amount);
			String neeFormat = plugin.formatNumber(needed);

			if(mSplit[0].equalsIgnoreCase("money")) {
				amFormat = "$" + amFormat;
				neeFormat = "$" + neeFormat;
			}

			if(dailyMissions.isCompleted(player, mission)) {
				return plugin.colourMessage("{#ACBED8}&m" + mSplit[2] + " &f&m" + amFormat + "/" + neeFormat);
			} else {
				return plugin.colourMessage("{#ACBED8}" + mSplit[2] + " &f" + amFormat + "/" + neeFormat);
			}
		}

		if(identifier.equalsIgnoreCase("daily_mission_two")) {
			if(dailyMissions.getMissions(player).isEmpty()) {
				return "&7-";
			}
			String mission = dailyMissions.getMissions(player).get(1);
			String[] mSplit = mission.split("-");
			int amount = dailyMissions.getMissionAmount(player, mission);
			int needed = dailyMissions.getMissionNeeded(player, mission);

			String amFormat = plugin.formatNumber(amount);
			String neeFormat = plugin.formatNumber(needed);

			if(mSplit[0].equalsIgnoreCase("money")) {
				amFormat = "$" + amFormat;
				neeFormat = "$" + neeFormat;
			}

			if(dailyMissions.isCompleted(player, mission)) {
				return plugin.colourMessage("{#ACBED8}&m" + mSplit[2] + " &f&m" + amFormat + "/" + neeFormat);
			} else {
				return plugin.colourMessage("{#ACBED8}" + mSplit[2] + " &f" + amFormat + "/" + neeFormat);
			}
		}

		if(identifier.equalsIgnoreCase("token_balance_formatted")) {
			if(plugin.tokensData.containsKey(player.getUniqueId())) {
				return plugin.formatNumber(plugin.tokensData.get(player.getUniqueId()));
			} else {
				return "0";
			}
		}

		if(identifier.equalsIgnoreCase("token_balance")) {
			if(plugin.tokensData.containsKey(player.getUniqueId())) {
				return String.valueOf(plugin.tokensData.get(player.getUniqueId()));
			} else {
				return "0";
			}
		}

		if(identifier.equalsIgnoreCase("total_secrets")) {
			int totalFound = 0;
			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = '" + player.getUniqueId() + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					totalFound += rs.getInt(1);
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return String.valueOf(totalFound);
		}

        if(identifier.equalsIgnoreCase("user_tag")) {
			if(plugin.userTags.get(player.getUniqueId()) != null) {
				return plugin.colourMessage(plugin.userTags.get(player.getUniqueId()));
			} else {
				return "";
			}

        }

		if(identifier.equalsIgnoreCase("mod_tag")) {
			if (player.hasPermission("group.trmod")) {
				CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
				String tag = "";
				switch(user.getRank().getName().toLowerCase()) {
					case "grass":
						tag = "&f[{#2ee600}G&f]";
						break;
					case "desert":
						tag = "&f[{#e6b22e}D&f]";
						break;
					case "nether":
						tag = "&f[{#ff2400}N&f]";
						break;
					case "snow":
						tag = "&f[{#3dc3cc}S&f]";
						break;
					case "free":
						tag = "&f[{#f75394}F&f]";
						break;
					case "hell":
						tag = "&f[{#cc141f}H&f]";
						break;
					case "end":
						tag = "&f[{#0085e6}E&f]";
						break;
				}

				return plugin.colourMessage(tag);
			} else {
				return "";
			}
		}


		if(identifier.equalsIgnoreCase("silence")) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if (user.isSilenceMode()) {
				String availableMessage = ChatColor.GRAY + "Global Chat is " + ChatColor.DARK_RED + "DISABLED";
				return availableMessage;
			} else {
				String availableMessage = ChatColor.GRAY + "Global Chat is " + ChatColor.GREEN + "ENABLED";
				return availableMessage;
			}
		}

		if(identifier.equalsIgnoreCase("brews_drank")) {
			int brewsDrank = 0;
			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT brews_drank FROM users WHERE user_id = '" + player.getUniqueId() + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					brewsDrank = rs.getInt(1);
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(brewsDrank);
		}

		if(identifier.equalsIgnoreCase("silence_private")) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if(!user.isAcceptingPM()) {
				return ChatColor.GRAY + "Private Chat is " + ChatColor.DARK_RED + "DISABLED";
			} else {
				return ChatColor.GRAY + "Private Chat is " + ChatColor.GREEN + "ENABLED";
			}
		}

		if(identifier.equalsIgnoreCase("silence_bounty")) {
			if(player.hasPermission("skyprisoncore.command.bounty.silent")) {
				return ChatColor.GRAY + "Bounty Notifications are " + ChatColor.DARK_RED + "DISABLED";
			} else {
				return ChatColor.GRAY + "Bounty Notifications are " + ChatColor.GREEN + "ENABLED";
			}
		}

		if(identifier.equalsIgnoreCase("sponges_found")) {
			int spongesFound = 0;
			try {
				Connection conn = hook.getSQLConnection();
				PreparedStatement ps = conn.prepareStatement("SELECT sponges_found FROM users WHERE user_id = '" + player.getUniqueId() + "'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					spongesFound = rs.getInt(1);
				}
				hook.close(ps, rs, conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(spongesFound);
		}

		for(int i = 1; i <= 8; i++) {
			if(identifier.equalsIgnoreCase("parkour"+i)) {
				String parkourPlaceholder = PlaceholderAPI.setPlaceholders(player, "%parkour_player_prize_delay_parkour"+ i +"%");
				String availableMessage = ChatColor.GREEN + "Available Now";
				if(parkourPlaceholder.equalsIgnoreCase("0")) {
					return availableMessage;
				} else {
					return parkourPlaceholder;
				}
			}
		}

		if(identifier.equalsIgnoreCase("bus_pass_time")) {
			String timePlaceholder = PlaceholderAPI.setPlaceholders(player, "%luckperms_expiry_time_skyprisoncore.command.transportpass.bus%");
			String availableMessage = ChatColor.RED + "You don't have a bus pass!";
			String hasPassMessage = plugin.colourMessage("&7Expires In: &a" + timePlaceholder);
			if(timePlaceholder.equalsIgnoreCase("")) {
				return availableMessage;
			} else {
				return hasPassMessage;
			}
		}


		if(identifier.equalsIgnoreCase("train_pass_time")) {
			String timePlaceholder = PlaceholderAPI.setPlaceholders(player, "%luckperms_expiry_time_skyprisoncore.command.transportpass.train%");
			String availableMessage = ChatColor.RED + "You don't have a train pass!";
			String hasPassMessage = plugin.colourMessage("&7Expires In: &a" + timePlaceholder);
			if(timePlaceholder.equalsIgnoreCase("")) {
				return availableMessage;
			} else {
				return hasPassMessage;
			}
		}

		if(identifier.equalsIgnoreCase("bus_skycity_trainstation")) {
			return getBusPrice(player, identifier);
		}

		String substring = identifier.substring(0, identifier.length() - 5);
		if(identifier.equalsIgnoreCase("bus_skycity_trainstation_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_camostore")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_camostore_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_cinema")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_cinema_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_casino")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_casino_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_zoo")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_zoo_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_emergency")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_skycity_emergency_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_silverlake_downtown")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_silverlake_downtown_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_silverlake_uptown")) {
			return getBusPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("bus_silverlake_uptown_cost")) {
			identifier = substring;
			return getBusPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_skycity")) {
			return getTrainPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_skycity_cost")) {
			identifier = substring;
			return getTrainPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_marina_west")) {
			return getTrainPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_marina_west_cost")) {
			identifier = substring;
			return getTrainPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_silverlake")) {
			return getTrainPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_silverlake_cost")) {
			identifier = substring;
			return getTrainPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_marina_east")) {
			return getTrainPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_marina_east_cost")) {
			identifier = substring;
			return getTrainPriceInt(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_airport")) {
			return getTrainPrice(player, identifier);
		}

		if(identifier.equalsIgnoreCase("train_airport_cost")) {
			identifier = substring;
			return getTrainPriceInt(player, identifier);
		}

		return null;
	}
}
