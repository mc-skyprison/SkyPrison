package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CMIPlayerTeleportRequest implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public CMIPlayerTeleportRequest(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    @EventHandler
    public void onCMIPlayerTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        Player askedPlayer = event.getWhoAccepts();
        Player askingPlayer = event.getWhoOffers();

        ArrayList<String> ignoredPlayers = new ArrayList<>();
        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT ignore_id FROM teleport_ignore WHERE user_id = '" + askedPlayer.getUniqueId() + "'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ignoredPlayers.add(rs.getString(1));
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(ignoredPlayers.contains(askingPlayer.getUniqueId().toString())) {
            askingPlayer.sendMessage(plugin.colourMessage(askedPlayer.getDisplayName() + "&eis ignoring your teleport requests!"));
            event.setCancelled(true);
        }
    }
}
