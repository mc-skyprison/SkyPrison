package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
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

    public PlayerQuit(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
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

            String pUUID = player.getUniqueId().toString();

            String sql;
            List<Object> params;
            sql = "UPDATE users SET blocks_mined = ? WHERE user_id = ?";
            params = new ArrayList<>() {{
                add(plugin.blockBreaks.get(pUUID));
                add(pUUID);
            }};
            db.sqlUpdate(sql, params);

            sql = "UPDATE users SET tokens = ? WHERE user_id = ?";
            params = new ArrayList<>() {{
                add(plugin.tokensData.get(pUUID));
                add(pUUID);
            }};
            db.sqlUpdate(sql, params);
        });
    }
}
