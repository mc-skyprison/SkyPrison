package net.skyprison.skyprisoncore.listeners.cmi;

import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CMIPlayerTeleportRequest implements Listener {
    private final DatabaseHook db;

    public CMIPlayerTeleportRequest(DatabaseHook db) {
        this.db = db;
    }

    @EventHandler
    public void onCMIPlayerTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        Player askedPlayer = event.getWhoAccepts();
        Player askingPlayer = event.getWhoOffers();

        ArrayList<String> ignoredPlayers = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT ignore_id FROM teleport_ignore WHERE user_id = ?")) {
            ps.setString(1, askedPlayer.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ignoredPlayers.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(ignoredPlayers.contains(askingPlayer.getUniqueId().toString())) {
            askingPlayer.sendMessage(askedPlayer.displayName().colorIfAbsent(NamedTextColor.GOLD).append(Component.text(" is ignoring your teleport requests!", NamedTextColor.YELLOW)));
            event.setCancelled(true);
        }
    }
}
