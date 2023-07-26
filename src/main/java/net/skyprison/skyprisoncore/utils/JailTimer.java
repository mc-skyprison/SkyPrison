package net.skyprison.skyprisoncore.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JailTimer extends TimerTask {
    private final DatabaseHook db;
    private final Player player;
    private final UUID pUUID;
    private final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    private final World world = Objects.requireNonNull(Bukkit.getWorld("world_prison"));
    private final RegionManager regions = container.get(BukkitAdapter.adapt(world));
    private final Location loc = new Location(world, 0.5, 134, 16.5, -180, 0);
    private final Location leaveLoc = new Location(world, 0.5, 135, 0.5);
    private long timeLeft;
    public final BossBar timeBar;
    private float progress;
    private long totalTime;
    public final int logsId;
    public long timeServed;
    private final Component prefix = Component.text("Time Left", TextColor.fromHexString("#939292")).append(Component.text(" » ", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));

    public void increaseTime(Long millis) {
        timeLeft += millis;
        totalTime += millis;
    }

    public JailTimer(DatabaseHook db, Player player, long timeLeft, int logsId, long totalTime) {
        this.db = db;
        this.player = player;
        this.logsId = logsId;
        this.timeLeft = timeLeft + System.currentTimeMillis();
        this.totalTime = totalTime;
        this.pUUID = player.getUniqueId();
        this.progress = ((300 - ((float) TimeUnit.MILLISECONDS.toSeconds(timeLeft))) / 300);
        long currTime = System.currentTimeMillis();
        long timeTill = timeLeft - currTime;
        int minutes = (int) Math.round((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
        int seconds = (int) Math.round((timeTill % (1000.0 * 60.0)) / 1000.0);
        Component title = Component.text("Time Left: ", NamedTextColor.GRAY);
        if (minutes != 0.0) {
            title = title.append(Component.text(minutes, NamedTextColor.YELLOW)).append(Component.text(" min", NamedTextColor.GOLD))
                    .appendSpace().append(Component.text(seconds, NamedTextColor.YELLOW)).append(Component.text(" sec", NamedTextColor.GOLD));
        } else {
            title = title.append(Component.text(seconds, NamedTextColor.YELLOW)).append(Component.text(" sec", NamedTextColor.GOLD));
        }
        this.timeBar = BossBar.bossBar(title, progress, BossBar.Color.RED, BossBar.Overlay.PROGRESS);
        this.timeBar.addViewer(this.player);
    }
    @Override
    public void run() {
        long currTime = System.currentTimeMillis();
        if(timeLeft > currTime) {
            long timeTill = timeLeft - currTime;
            timeServed = totalTime - timeTill;
            if(player != null && player.isOnline()) {
                if(player.getWorld() != world) {
                    player.teleportAsync(loc);
                } else {
                    Location pLoc = player.getLocation();
                    ApplicableRegionSet regionSet = Objects.requireNonNull(regions).getApplicableRegions(BlockVector3.at(pLoc.getBlockX(),
                            pLoc.getBlockY(), pLoc.getBlockZ()));
                    if(regionSet.getRegions().stream().filter(region -> region.getId().startsWith("center-jail")).toList().isEmpty())
                        player.teleportAsync(loc);
                }
                int minutes = (int) Math.floor((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                int seconds = (int) Math.round((timeTill % (1000.0 * 60.0)) / 1000.0);
                Component title = prefix;
                if (minutes != 0.0) {
                    title = title.append(Component.text(minutes, TextColor.fromHexString("#e23857"))).append(Component.text(" min", TextColor.fromHexString("#939292")))
                            .appendSpace().append(Component.text(seconds, TextColor.fromHexString("#e23857"))).append(Component.text(" sec", TextColor.fromHexString("#939292")));
                } else {
                    title = title.append(Component.text(seconds, TextColor.fromHexString("#e23857"))).append(Component.text(" sec", TextColor.fromHexString("#939292")));
                }
                timeBar.name(title);
                progress = (float) timeServed / totalTime;
                timeBar.progress(progress);
            } else {
                this.cancel();
                SkyPrisonCore.currentlyJailed.remove(pUUID);
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE logs_jail SET length_served = ? WHERE id = ?")) {
                    ps.setLong(1, timeServed);
                    ps.setInt(2, logsId);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            this.cancel();
            player.teleportAsync(leaveLoc);
            timeBar.removeViewer(player);
            SkyPrisonCore.currentlyJailed.remove(pUUID);
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE logs_jail SET active = ?, time_finished = ?, length_served = length_total WHERE id = ?")) {
                ps.setInt(1, 0);
                ps.setLong(2, currTime);
                ps.setInt(3, logsId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
