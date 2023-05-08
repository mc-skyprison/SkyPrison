package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerQuit implements Listener {

    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final DiscordApi discApi;
    private final DailyMissions dm;

    public PlayerQuit(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi, DailyMissions dm) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
        this.dm = dm;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Player player = event.getPlayer();

            if(discApi != null) {
                EmbedBuilder embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " left the server", "", "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.RED);

                discApi.getTextChannelById("788108242797854751").get().sendMessage(embedJoin);
            }

            String sql;
            List<Object> params;
            sql = "UPDATE users SET blocks_mined = ? WHERE user_id = ?";
            params = new ArrayList<>() {{
                add(plugin.blockBreaks.get(player.getUniqueId()));
                add(player.getUniqueId().toString());
            }};
            db.sqlUpdate(sql, params);

            sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
            params = new ArrayList<>() {{
                add(plugin.tokensData.get(player.getUniqueId()));
                add(player.getUniqueId().toString());
            }};
            db.sqlUpdate(sql, params);

            for(String mission : dm.getMissions(player)) {
                sql = "UPDATE daily_missions SET amount = ?, completed = ? WHERE user_id = ? AND type = ?";
                params = new ArrayList<>() {{
                    add(dm.getMissionAmount(player, mission));
                    add(dm.isCompleted(player, mission) ? 1 : 0);
                    add(player.getUniqueId().toString());
                    add(mission);
                }};
                db.sqlUpdate(sql, params);
            }
        });
    }
}
