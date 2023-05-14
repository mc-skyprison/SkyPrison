package net.skyprison.skyprisoncore.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.ParticleStyle;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerJoin implements Listener {

    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final DiscordApi discApi;
    private final DailyMissions dailyMissions;
    private final PlayerParticlesAPI particles;

    public PlayerJoin(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi, DailyMissions dailyMissions, PlayerParticlesAPI particles) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
        this.dailyMissions = dailyMissions;
        this.particles = particles;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getPlayer();

            EmbedBuilder embedJoin;
            Connection conn;
            PreparedStatement ps;
            ResultSet rs;
            if(!player.hasPlayedBefore()) {
                embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " joined the server for the first time!", "",  "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.YELLOW);

                String sqls = "INSERT INTO users (user_id, current_name, first_join) VALUES (?, ?, ?)";
                List<Object> params = new ArrayList<>() {{
                    add(player.getUniqueId().toString());
                    add(player.getName());
                    add(player.getFirstPlayed());
                }};
                db.sqlUpdate(sqls, params);
            } else {
                embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " joined the server", "",  "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.GREEN);
                boolean noData = false;
                try {
                    conn = db.getSQLConnection();
                    ps = conn.prepareStatement("SELECT * FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                    rs = ps.executeQuery();
                    if(!rs.isBeforeFirst()) {
                        noData = true;
                    }
                    db.close(ps, rs, conn);
                } catch (SQLException ignored) {
                }

                if(noData) {
                    String sqls = "INSERT INTO users (user_id, current_name, first_join) VALUES (?, ?, ?)";
                    List<Object> params = new ArrayList<>() {{
                        add(player.getUniqueId().toString());
                        add(player.getName());
                        add(player.getFirstPlayed());
                    }};
                    db.sqlUpdate(sqls, params);
                }
            }

            if(discApi != null)
                discApi.getTextChannelById("788108242797854751").get().sendMessage(embedJoin);


            if(dailyMissions.getMissions(player).isEmpty()) {
                dailyMissions.setPlayerMissions(player);
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            com.sk89q.worldedit.util.Location locWE = BukkitAdapter.adapt(player.getLocation());
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                player.setAllowFlight(query.testState(locWE, localPlayer, plugin.claimPlugin.FLY));
            }

            if(!plugin.userTags.containsKey(player.getUniqueId())) {
                int tag_id = 0;
                try {
                    conn = db.getSQLConnection();
                    ps = conn.prepareStatement("SELECT active_tag FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                    rs = ps.executeQuery();
                    while(rs.next()) {
                        tag_id = rs.getInt(1);
                    }
                    db.close(ps, rs, conn);
                } catch (SQLException ignored) {
                }

                if(tag_id != 0) {
                    String tagsDisplay = "";
                    String tagsEffect = "";
                    try {
                        conn = db.getSQLConnection();
                        ps = conn.prepareStatement("SELECT tags_display, tags_effect FROM tags WHERE tags_id = '" + tag_id + "'");
                        rs = ps.executeQuery();
                        while(rs.next()) {
                            tagsDisplay = rs.getString(1);
                            tagsEffect = rs.getString(2);
                        }
                        db.close(ps, rs, conn);
                    } catch (SQLException ignored) {
                    }
                    plugin.userTags.put(player.getUniqueId(), tagsDisplay);
                    particles.resetActivePlayerParticles(player);
                    particles.addActivePlayerParticle(player, ParticleEffect.CLOUD, ParticleStyle.fromInternalName(tagsEffect));
                }
            }

            plugin.blockBreaks.put(player.getUniqueId(), 0);
            try {
                conn = db.getSQLConnection();
                ps = conn.prepareStatement("SELECT blocks_mined FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                rs = ps.executeQuery();
                while(rs.next()) {
                    plugin.blockBreaks.put(player.getUniqueId(), rs.getInt(1));
                }
                db.close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            plugin.tokensData.put(player.getUniqueId(), 0);
            try {
                conn = db.getSQLConnection();
                ps = conn.prepareStatement("SELECT tokens FROM users WHERE user_id = '" + player.getUniqueId() + "'");
                rs = ps.executeQuery();
                while(rs.next()) {
                    plugin.tokensData.put(player.getUniqueId(), rs.getInt(1));
                }
                db.close(ps, rs, conn);
            } catch (SQLException e) {
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
