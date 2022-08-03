package net.skyprison.skyprisoncore.listeners;

import com.dre.brewery.api.events.brew.BrewDrinkEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BrewDrink implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public BrewDrink(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    @EventHandler
    public void onBrewDrink(BrewDrinkEvent event) {
        Player player = event.getPlayer();

        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT brews_drank FROM users WHERE user_id = '" + player.getUniqueId() + "'");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String sql = "UPDATE users SET brews_drank = ? WHERE user_id = ?";
                int nBrews = rs.getInt(1) + 1;
                List<Object> params = new ArrayList<Object>() {{
                    add(nBrews);
                    add(player.getUniqueId().toString());
                }};
                hook.sqlUpdate(sql, params);
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
