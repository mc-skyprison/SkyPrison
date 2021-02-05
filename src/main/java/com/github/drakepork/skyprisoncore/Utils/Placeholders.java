package com.github.drakepork.skyprisoncore.Utils;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
	private Core plugin;

	@Inject
	public Placeholders(Core plugin) {
		this.plugin = plugin;
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
			if(user.getWorld().getTime() > 0 && user.getWorld().getTime() < 12300) {
				return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "$" + dist;
			} else {
				int nightDist = (int) (dist*1.5);
				return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "$" + nightDist;
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
			if(user.getWorld().getTime() > 0 && user.getWorld().getTime() < 12300) {
				return String.valueOf(dist);
			} else {
				int nightDist = (int) (dist*1.5);
				return String.valueOf(nightDist);
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
			return ChatColor.GRAY + "Price: " + ChatColor.YELLOW + "$" + dist;
		}
	}

	private String getTrainPriceInt(Player player, String identifier) {
		CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
		Location warpLoc = CMI.getInstance().getWarpManager().getWarp(identifier).getLoc();
		int dist = (int) user.getLocation().distance(warpLoc);
		if(dist < 50) {
			return "0";
		} else {
			return String.valueOf(dist);
		}
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier){

		if(player == null) {
			return "";
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

		if(identifier.equalsIgnoreCase("silence_private")) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if(!user.isAcceptingPM()) {
				String availableMessage = ChatColor.GRAY + "Private Chat is " + ChatColor.DARK_RED + "DISABLED";
				return availableMessage;
			} else {
				String availableMessage = ChatColor.GRAY + "Private Chat is " + ChatColor.GREEN + "ENABLED";
				return availableMessage;
			}
		}

		if(identifier.equalsIgnoreCase("silence_bounty")) {
			if(player.hasPermission("skyprisoncore.command.bounty.silent")) {
				String availableMessage = ChatColor.GRAY + "Bounty Notifications are " + ChatColor.DARK_RED + "DISABLED";
				return availableMessage;
			} else {
				String availableMessage = ChatColor.GRAY + "Bounty Notifications are " + ChatColor.GREEN + "ENABLED";
				return availableMessage;
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

		for(int i = 1; i <= 8; i++) {
			if(identifier.equalsIgnoreCase("parkour"+i)) {
				String parkourPlaceholder = PlaceholderAPI.setPlaceholders(player, "%parkour_course_prize_delay_parkour"+ i +"%");
				String availableMessage = ChatColor.GREEN + "Available Now";
				if(parkourPlaceholder.equalsIgnoreCase("0")) {
					return availableMessage;
				} else {
					return parkourPlaceholder;
				}
			}
		}

		return null;
	}
}
