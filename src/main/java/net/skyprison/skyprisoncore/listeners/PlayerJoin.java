package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
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
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerJoin implements Listener {

    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private DiscordApi discApi;
    private DailyMissions dailyMissions;

    public PlayerJoin(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi, DailyMissions dailyMissions) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getPlayer();

            EmbedBuilder embedJoin;

            if(!player.hasPlayedBefore()) {
                embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " joined the server for the first time!", "",  "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.YELLOW);

                String sqls = "INSERT INTO users (user_id, current_name, first_join) VALUES (?, ?, ?)";
                List<Object> params = new ArrayList<Object>() {{
                    add(player.getUniqueId().toString());
                    add(player.getName());
                    add(player.getFirstPlayed());
                }};
                db.sqlUpdate(sqls, params);
            } else {
                embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " joined the server", "",  "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.GREEN);
            }

            if(discApi != null)
                discApi.getTextChannelById("788108242797854751").get().sendMessage(embedJoin);


            if(player.getName().equalsIgnoreCase("DrakePork") && dailyMissions.getPlayerMissions(player).isEmpty()) {
                dailyMissions.setPlayerMissions(player);
            }


            File f = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
            FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String currDate = formatter.format(date);

            if(dailyConf.contains("players." + player.getUniqueId() + ".last-collected")) {
                String lastDay = dailyConf.getString("players." + player.getUniqueId() + ".last-collected");
                if (!lastDay.equalsIgnoreCase(currDate)) {
                    player.sendMessage(plugin.colourMessage("&aYou can collect your &l/daily&l!"));
                }
            } else {
                player.sendMessage(plugin.colourMessage("&aYou can collect your &l/daily&l!"));
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            com.sk89q.worldedit.util.Location locWE = BukkitAdapter.adapt(player.getLocation());
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                player.setAllowFlight(query.testState(locWE, localPlayer, plugin.claimPlugin.FLY));
            }

            Connection conn;
            PreparedStatement ps;
            ResultSet rs;

            String pUUID = event.getPlayer().getUniqueId().toString();

            try {
                conn = db.getSQLConnection();
                ps = conn.prepareStatement("SELECT blocks_mined FROM users WHERE user_id = '" + pUUID + "'");
                rs = ps.executeQuery();
                while(rs.next()) {
                    plugin.blockBreaks.put(pUUID, rs.getInt(1));
                }
                db.close(ps, rs, conn);
            } catch (SQLException e) {
                plugin.blockBreaks.put(pUUID, 0);
                e.printStackTrace();
            }

            try {
                conn = db.getSQLConnection();
                ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = '" + pUUID + "'");
                rs = ps.executeQuery();
                while(rs.next()) {
                    plugin.tokensData.put(pUUID, rs.getInt(1));
                }
                db.close(ps, rs, conn);
            } catch (SQLException e) {
                plugin.tokensData.put(pUUID, 0);
                e.printStackTrace();
            }

            if(player.getWorld().getName().equalsIgnoreCase("world_prison") || player.getWorld().getName().equalsIgnoreCase("world_event") || player.getWorld().getName().equalsIgnoreCase("world_war")) {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
            } else {
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
            }

        });
    }
}
