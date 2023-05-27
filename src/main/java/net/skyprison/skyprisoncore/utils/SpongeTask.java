package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SpongeTask extends TimerTask {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public SpongeTask(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void run() {
        List<String> locs = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT location FROM sponge_locations")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                locs.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(!locs.isEmpty()) {
            Collections.shuffle(locs);
            // world ; x ; y ; z
            String[] loc = locs.get(0).split(";");

            World w = Bukkit.getWorld(loc[0]);
            Location placeSponge = new Location(w, Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]));
            placeSponge = placeSponge.getBlock().getLocation();
            placeSponge.getBlock().setType(Material.SPONGE);
        }

        int randTime = ThreadLocalRandom.current().nextInt(20, 41);
        plugin.spongeTimer.schedule(new SpongeTask(plugin, db), TimeUnit.MINUTES.toMillis(randTime));
    }
}
