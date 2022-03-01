package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.clip.placeholderapi.PlaceholderAPI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerJoin implements Listener {

    private SkyPrisonCore plugin;
    private DatabaseHook db;

    public PlayerJoin(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File f = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
            FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String currDate = formatter.format(date);
            Player player = event.getPlayer();

            if(dailyConf.contains("players." + player.getUniqueId() + ".last-collected")) {
                String lastDay = dailyConf.getString("players." + player.getUniqueId() + ".last-collected");
                if (!lastDay.equalsIgnoreCase(currDate)) {
                    player.sendMessage(plugin.colourMessage("&aYou can collect your &l/daily&l!"));
                }
            } else {
                player.sendMessage(plugin.colourMessage("&aYou can collect your &l/daily&l!"));
            }


            File fData = new File(plugin.getDataFolder() + File.separator + "firstjoindata.yml");
            FileConfiguration firstJoinConf = YamlConfiguration.loadConfiguration(fData);
            String pUUID = event.getPlayer().getUniqueId().toString();
            if(!firstJoinConf.isConfigurationSection(pUUID)) {
                String firstJoinString = PlaceholderAPI.setPlaceholders(event.getPlayer(), "%player_first_join_date%");
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                Date firstJoinDate;
                try {
                    firstJoinDate = sdf.parse(firstJoinString);
                    Long firstJoinMilli = firstJoinDate.getTime();
                    firstJoinConf.set(pUUID + ".firstjoin", firstJoinMilli);
                    firstJoinConf.save(fData);
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                }
            }
            File tokenMine = new File(plugin.getDataFolder() + File.separator + "blocksmined.yml");
            FileConfiguration mineConf = YamlConfiguration.loadConfiguration(tokenMine);

            if(mineConf.contains(pUUID)) {
                plugin.blockBreaks.put(pUUID, mineConf.getInt(pUUID));
            } else {
                plugin.blockBreaks.put(pUUID, 0);
            }

            File tData = new File(plugin.getDataFolder() + File.separator + "tokensdata.yml");
            FileConfiguration tokenConf = YamlConfiguration.loadConfiguration(tData);
            plugin.tokensData.put(pUUID, tokenConf.getInt("players." + pUUID));

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            com.sk89q.worldedit.util.Location locWE = BukkitAdapter.adapt(player.getLocation());
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                player.setAllowFlight(query.testState(locWE, localPlayer, plugin.claimPlugin.FLY));
            }

            if(player.getWorld().getName().equalsIgnoreCase("world_prison") || player.getWorld().getName().equalsIgnoreCase("world_event") || player.getWorld().getName().equalsIgnoreCase("world_war")) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
            } else {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
            }

        });
    }

}
