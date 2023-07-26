package net.skyprison.skyprisoncore.utils;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.PlayerOptions.PlayerOption;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.items.Greg;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Placeholders extends PlaceholderExpansion {
	private final SkyPrisonCore plugin;
	private final DailyMissions dailyMissions;
	private final DatabaseHook db;
	public Placeholders(SkyPrisonCore plugin, DailyMissions dailyMissions, DatabaseHook db) {
		this.plugin = plugin;
		this.dailyMissions = dailyMissions;
		this.db = db;
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
	public @NotNull String getAuthor() {
		return plugin.getPluginMeta().getAuthors().toString();
	}

	@Override
	public @NotNull String getIdentifier(){
		return "SkyPrisonCore";
	}

	@Override
	public @NotNull String getVersion(){
		return plugin.getPluginMeta().getVersion();
	}

	private String getBusPrice(Player player, String identifier) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Location warpLoc = CMI.getInstance().getWarpManager().getWarp(identifier).getLoc();
		int dist = (int) user.getLocation().distance(warpLoc);
		if(dist < 10) {
			return "&7&oYou Are Here";
		} else {
			if(!player.hasPermission("skyprisoncore.command.transportpass.bus")) {
				if (user.getWorld().getTime() > 0 && user.getWorld().getTime() < 12300) {
					return "&7Price: &e$" + dist;
				} else {
					int nightDist = (int) (dist * 1.5);
					return "&7Price: &e$" + nightDist;
				}
			} else {
				return "&7Price: &eFREE";
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
			return "&7&oYou Are Here";
		} else {
			if(!player.hasPermission("skyprisoncore.command.transportpass.train"))
				return "&7Price: &e$" + dist;
			else
				return "&7Price: &eFREE";
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

	private int getBribeAmount(Player player) {
		int amount = 0;
		if (player.hasPermission("group.end")) {
			amount = 50000;
		} else if (player.hasPermission("group.hell")) {
			amount = 25000;
		} else if (player.hasPermission("group.free")) {
			amount = 10000;
		} else if (player.hasPermission("group.nether")) {
			amount = 1000;
		} else if (player.hasPermission("group.default")) {
			amount = 250;
		}
		return amount;
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String identifier){
		if(player == null) {
			return "";
		}

		if(identifier.equalsIgnoreCase("daily_highest_streak")) {
			int highestStreak = 0;
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT highest_streak FROM dailies WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					highestStreak = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(highestStreak);
		}

		if(identifier.equalsIgnoreCase("player_rank")) {
			LuckPerms luckAPI = LuckPermsProvider.get();
			User user = luckAPI.getPlayerAdapter(Player.class).getUser(player);
			if(user.getCachedData().getMetaData().getPrefix() != null) {
				Component prefix = MiniMessage.miniMessage().deserialize(Objects.requireNonNull(user.getCachedData().getMetaData().getPrefix())).appendSpace();
				return LegacyComponentSerializer.legacySection().serialize(prefix);
			} else {
				return "";
			}
		}

		if(identifier.equalsIgnoreCase("get_bribe_cooldown")) {
			int cooldown = 0;
			if(plugin.bribeCooldown.containsKey(player.getUniqueId())) {
				long bribeCool = plugin.bribeCooldown.get(player.getUniqueId());
				if(bribeCool > System.currentTimeMillis()) {
					cooldown = 1;
				}
			}
			return String.valueOf(cooldown);
		}

		if(identifier.equalsIgnoreCase("bribe_amount")) {
			return String.valueOf(getBribeAmount(player));
		}

		if(identifier.equalsIgnoreCase("bribe_amount_formatted")) {
			return String.valueOf(plugin.formatNumber(getBribeAmount(player)));
		}

		if(identifier.equalsIgnoreCase("is_jailed")) {
			int jailed = 0;
			try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM logs_jail WHERE target_id = ? AND active = 1")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					jailed = 1;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(jailed);
		}

		if(identifier.equalsIgnoreCase("can_afford_bribe")) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			return String.valueOf(user.hasMoney((double) getBribeAmount(player)) ? 1 : 0);
		}


		if(identifier.equalsIgnoreCase("has_release_papers")) {
			ItemStack realPapers = Greg.getReleasePapers(plugin, 1);
			PlayerInventory pInv = player.getInventory();

			return String.valueOf(pInv.containsAtLeast(realPapers, 1) ? 1 : 0);
		}

		if(identifier.equalsIgnoreCase("has_fake_release_papers")) {
			ItemStack fakePapers = Greg.getFakeReleasePapers(plugin, 1);
			PlayerInventory pInv = player.getInventory();

			return String.valueOf(pInv.containsAtLeast(fakePapers, 1) ? 1 : 0);
		}

		if(identifier.equalsIgnoreCase("daily_total_collected")) {
			int totalColl = 0;
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT total_collected FROM dailies WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					totalColl = rs.getInt(1);
				}
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
				return "<#ACBED8>&m" + mSplit[2] + " &f&m" + amFormat + "/" + neeFormat;
			} else {
				return "<#ACBED8>" + mSplit[2] + " &f" + amFormat + "/" + neeFormat;
			}
		}

		if(identifier.equalsIgnoreCase("daily_mission_two")) {
			if(dailyMissions.getMissions(player).isEmpty()) {
				return "&7>-";
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
				return "<#ACBED8>&m" + mSplit[2] + " &f&m" + amFormat + "/" + neeFormat;
			} else {
				return "<#ACBED8>" + mSplit[2] + " &f" + amFormat + "/" + neeFormat;
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
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT secret_amount FROM secrets_data WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					totalFound += rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return String.valueOf(totalFound);
		}

        if(identifier.equalsIgnoreCase("user_tag")) {
			if(plugin.userTags.get(player.getUniqueId()) != null) {
				return plugin.userTags.get(player.getUniqueId());
			} else {
				return "";
			}

        }

		if(identifier.equalsIgnoreCase("silence")) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if (user.isSilenceMode()) {
				return "&7Global Chat is &4DISABLED";
			} else {
				return "&7Global Chat is &aENABLED";
			}
		}

		if(identifier.equalsIgnoreCase("brews_drank")) {
			int brewsDrank = 0;
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT brews_drank FROM users WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					brewsDrank = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(brewsDrank);
		}

		if(identifier.equalsIgnoreCase("silence_private")) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if(!user.getOptionState(PlayerOption.acceptingPM)) {
				return "&7Private Chat is &4DISABLED";
			} else {
				return "&7Private Chat is &aENABLED";
			}
		}

		if(identifier.equalsIgnoreCase("silence_bounty")) {
			if(player.hasPermission("skyprisoncore.command.bounty.silent")) {
				return "&7Bounty Notifications are &4DISABLED";
			} else {
				return "&7Bounty Notifications are &aENABLED";
			}
		}

		if(identifier.equalsIgnoreCase("sponges_found")) {
			int spongesFound = 0;
			try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT sponges_found FROM users WHERE user_id = ?")) {
				ps.setString(1, player.getUniqueId().toString());
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					spongesFound = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return String.valueOf(spongesFound);
		}

		for(int i = 1; i <= 8; i++) {
			if(identifier.equalsIgnoreCase("parkour"+i)) {
				String parkourPlaceholder = PlaceholderAPI.setPlaceholders(player, "%parkour_player_prize_delay_parkour"+ i +"%");
				String availableMessage = "&aAvailable Now";
				if(parkourPlaceholder.equalsIgnoreCase("0")) {
					return availableMessage;
				} else {
					return parkourPlaceholder;
				}
			}
		}

		if(identifier.equalsIgnoreCase("bus_pass_time")) {
			String timePlaceholder = PlaceholderAPI.setPlaceholders(player, "%luckperms_expiry_time_skyprisoncore.command.transportpass.bus%");
			String availableMessage = "&cYou don't have a bus pass!";
			String hasPassMessage = "&7Expires In: &a" + timePlaceholder;
			if(timePlaceholder.equalsIgnoreCase("")) {
				return availableMessage;
			} else {
				return hasPassMessage;
			}
		}


		if(identifier.equalsIgnoreCase("train_pass_time")) {
			String timePlaceholder = PlaceholderAPI.setPlaceholders(player, "%luckperms_expiry_time_skyprisoncore.command.transportpass.train%");
			String availableMessage = "&cYou don't have a train pass!";
			String hasPassMessage = "&7Expires In: &a" + timePlaceholder;
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
