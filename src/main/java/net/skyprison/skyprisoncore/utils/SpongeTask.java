package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

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
        List<Location> locs = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT world, x, y, z FROM sponge_locations")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                locs.add(new Location(Bukkit.getWorld(rs.getString(1)), rs.getInt(2), rs.getInt(3), rs.getInt(4)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(!locs.isEmpty()) {
            Collections.shuffle(locs);
            new BukkitRunnable() {
                @Override
                public void run() {
                    locs.get(0).getBlock().setType(Material.SPONGE);
                }
            }.runTask(plugin);
        }

        int randTime = ThreadLocalRandom.current().nextInt(20, 41);
        plugin.spongeTimer.schedule(new SpongeTask(plugin, db), TimeUnit.MINUTES.toMillis(randTime));
    }
}
