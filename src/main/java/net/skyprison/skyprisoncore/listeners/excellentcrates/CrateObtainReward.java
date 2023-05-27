package net.skyprison.skyprisoncore.listeners.excellentcrates;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.nightexpress.excellentcrates.api.event.CrateObtainRewardEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CrateObtainReward implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public CrateObtainReward(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onCrateObtainReward(CrateObtainRewardEvent event) {
        Player player = event.getPlayer();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO casino_opens (user_id, casino_name, opens_amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE opens_amount = opens_amount + VALUE(opens_amount)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, event.getCrate().getId());
            ps.setInt(3, 1);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
