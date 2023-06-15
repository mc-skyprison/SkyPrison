package net.skyprison.skyprisoncore.listeners.brewery;

import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BrewDrink implements Listener {
    private final DatabaseHook db;

    public BrewDrink(DatabaseHook db) {
        this.db = db;
    }

    @EventHandler
    public void onBrewDrink(BrewDrinkEvent event) {
        Player player = event.getPlayer();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET brews_drank = brews_drank + 1 WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
