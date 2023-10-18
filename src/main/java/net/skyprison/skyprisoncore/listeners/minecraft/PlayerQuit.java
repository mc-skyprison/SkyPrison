package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.mail.MailBoxSend;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

            if(plugin.mailSend.containsKey(player.getUniqueId())) {
                MailBoxSend inv = plugin.mailSend.get(player.getUniqueId());
                inv.cancelMail();
            }

            if(discApi != null) {
                EmbedBuilder embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " left the server", "", "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.RED);
                if(discApi.getTextChannelById("788108242797854751").isPresent()) {
                    discApi.getTextChannelById("788108242797854751").get().sendMessage(embedJoin);
                }
            }

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET blocks_mined = ?, tokens = ?, logout_world = ? WHERE user_id = ?")) {
                ps.setInt(1, plugin.blockBreaks.get(player.getUniqueId()));
                ps.setInt(2, plugin.tokensData.get(player.getUniqueId()));
                ps.setString(3, player.getWorld().getName());
                ps.setString(4, player.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for(String mission : dm.getMissions(player)) {
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE daily_missions SET amount = ?, completed = ? WHERE user_id = ? AND type = ?")) {
                    ps.setInt(1, dm.getMissionAmount(player, mission));
                    ps.setInt(2, dm.isCompleted(player, mission) ? 1 : 0);
                    ps.setString(3, player.getUniqueId().toString());
                    ps.setString(4, mission);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
