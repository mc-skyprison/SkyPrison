package net.skyprison.skyprisoncore.listeners.nuvotifier;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.economy.Tokens;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mariadb.jdbc.Statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Votifier implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    public Votifier(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler
    public void onVotifier(VotifierEvent event) {
        Vote vote = event.getVote();
        String playerName = vote.getUsername();
        String serviceName = vote.getServiceName();
        String serviceAddress = vote.getAddress();
        long time = Long.parseLong(vote.getTimeStamp());

        UUID pUUID = PlayerManager.getPlayerId(playerName);
        if(pUUID != null) {
            int id = -1;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO votes (user_id, time, service, tokens) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pUUID.toString());
                ps.setLong(2, time);
                ps.setString(3, serviceName);
                ps.setString(4, serviceAddress);
                ps.setInt(5, 0);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(id != -1) {
                int tokens = plugin.getVoteTokens(pUUID);
                Component voteMsg = Component.text("Vote", NamedTextColor.DARK_GREEN, TextDecoration.BOLD)
                        .append(Component.text("  » ", NamedTextColor.DARK_GRAY));
                Component everyoneMsg = voteMsg.append(Component.text(playerName, NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(" voted and received ", NamedTextColor.GREEN))
                        .append(Component.text(tokens, NamedTextColor.YELLOW, TextDecoration.BOLD)).append(Component.text(" tokens!", NamedTextColor.GREEN));
                if(tokens == -1) {
                    tokens = 50;
                    everyoneMsg = voteMsg.append(Component.text(playerName, NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(" voted for the first time and received ", NamedTextColor.GREEN))
                            .append(Component.text(tokens, NamedTextColor.YELLOW, TextDecoration.BOLD)).append(Component.text(" tokens!", NamedTextColor.GREEN));
                }
                new Tokens(plugin, db).addTokens(pUUID, tokens, "voting", serviceName);
                Audience receivers =  Bukkit.getServer();
                receivers.sendMessage(everyoneMsg);
                if(plugin.getVoteParty() == 0) {
                    Component partyMsg = voteMsg.append(Component.text("Vote Party has been reached! Everyone online gets ", NamedTextColor.DARK_AQUA))
                            .append(Component.text("3", NamedTextColor.YELLOW, TextDecoration.BOLD)).append(Component.text(" Vote Keys!", NamedTextColor.DARK_AQUA));
                    receivers.sendMessage(partyMsg);
                    plugin.asConsole("crates key give * crate_vote 3");
                }
                plugin.checkVoteMilestones(pUUID);

                if (Bukkit.getPlayer(pUUID) == null) {
                    plugin.createNotification("vote", String.valueOf(id), pUUID.toString(), everyoneMsg, null, true);
                }
                plugin.onAllSites(pUUID);
            } else {
                plugin.getLogger().warning("Failed to get vote ID from vote made by " + playerName + " on site " + serviceName + "!");
            }
        } else {
            plugin.getLogger().info("Received a vote from " + serviceName + "! Player with the name " + playerName + " has never joined the server! Ignoring vote..");
        }
    }
}
