package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.TokenUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlockDamage implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public BlockDamage(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (!world.getName().equalsIgnoreCase("world_prison") || !block.getType().equals(Material.SPONGE)) return;

        Player player = event.getPlayer();

        Location sLoc = null;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM sponge_locations WHERE world = ? AND x = ? AND y = ? And z = ?")) {
            ps.setString(1, world.getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                sLoc = loc;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(sLoc == null) return;

        block.setType(Material.AIR);
        plugin.getServer().sendMessage(Component.text(player.getName(), TextColor.fromHexString("#E97C07"), TextDecoration.BOLD)
                .append(Component.text(" has found the ", TextColor.fromHexString("#FFFF00"))).append(Component.text("SPONGE!", TextColor.fromHexString("#FFFF00"), TextDecoration.BOLD))
                .append(Component.text(" A new one will be hidden somewhere in prison.", TextColor.fromHexString("#FFFF00"))));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET sponges_found = sponges_found + 1 WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.executeUpdate();
            TokenUtils.addTokens(player.getUniqueId(), 25, "Found Sponge", "");
            DailyMissions.updatePlayerMissions(player.getUniqueId(), "sponge", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
