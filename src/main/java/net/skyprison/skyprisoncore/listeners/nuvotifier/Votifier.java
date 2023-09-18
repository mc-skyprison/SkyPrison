package net.skyprison.skyprisoncore.listeners.nuvotifier;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.old.economy.Tokens;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mariadb.jdbc.Statement;
import su.nightexpress.excellentcrates.ExcellentCratesAPI;

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
        long time = System.currentTimeMillis();

        UUID pUUID = PlayerManager.getPlayerId(playerName);
        if(pUUID != null) {
            int tokens = net.skyprison.skyprisoncore.utils.Vote.getVoteTokens(pUUID);
            int id = -1;
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO votes (user_id, time, service, address, tokens) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, pUUID.toString());
                ps.setLong(2, time);
                ps.setString(3, serviceName);
                ps.setString(4, serviceAddress);
                ps.setInt(5, tokens);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    id = rs.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if(id != -1) {
                Component noBold = Component.text("");
                Component voteMsg = noBold.append(Component.text("Vote", TextColor.fromHexString("#37a452"), TextDecoration.BOLD)).append(Component.text(" » ", NamedTextColor.DARK_GRAY));
                Component everyoneMsg = voteMsg.append(Component.text(playerName, TextColor.fromHexString("#e02957"), TextDecoration.BOLD))
                        .append(Component.text(" voted and received ", TextColor.fromHexString("#0fff87")))
                        .append(Component.text(tokens + " Tokens", TextColor.fromHexString("#e02957"), TextDecoration.BOLD))
                        .append(Component.text("! ", TextColor.fromHexString("#0fff87"), TextDecoration.BOLD))
                        .append(Component.text("/Vote", TextColor.fromHexString("#2db4e1"), TextDecoration.BOLD));
                Component playerMsg = voteMsg.append(Component.text("You have received ", TextColor.fromHexString("#0fff87")))
                        .append(Component.text(tokens + " Tokens", TextColor.fromHexString("#e02957"), TextDecoration.BOLD))
                        .append(Component.text("!", TextColor.fromHexString("#0fff87"), TextDecoration.BOLD));
                if(tokens == -1) {
                    tokens = 50;
                    everyoneMsg = voteMsg.append(Component.text(playerName, TextColor.fromHexString("#e02957"), TextDecoration.BOLD))
                            .append(Component.text(" voted for the first time and received ", TextColor.fromHexString("#0fff87")))
                            .append(Component.text(tokens + " Tokens", TextColor.fromHexString("#e02957"), TextDecoration.BOLD))
                            .append(Component.text("! ", TextColor.fromHexString("#0fff87"), TextDecoration.BOLD))
                            .append(Component.text("/Vote", TextColor.fromHexString("#2db4e1"), TextDecoration.BOLD));
                }
                new Tokens(plugin, db).addTokens(pUUID, tokens, "voting", serviceName);
                Player player = plugin.getServer().getPlayer(pUUID);
                Audience receivers =  player != null ? plugin.getServer().filterAudience(audience -> !audience.equals(player)) : plugin.getServer();
                receivers.sendMessage(everyoneMsg);

                if (player != null) {
                    player.sendMessage(playerMsg);
                } else {
                    NotificationsUtils.createNotification("vote", String.valueOf(id), pUUID.toString(), playerMsg, null, true);
                }

                net.skyprison.skyprisoncore.utils.Vote.checkVoteMilestones(pUUID);
                net.skyprison.skyprisoncore.utils.Vote.onAllSites(pUUID);

                if(net.skyprison.skyprisoncore.utils.Vote.getVoteParty() == 0) {
                    Component partyMsg = Component.text("Vote Party has been hit! Everyone online gets ", TextColor.fromHexString("#2db4e1"))
                            .append(Component.text("3 Vote Keys", TextColor.fromHexString("#e02957"), TextDecoration.BOLD))
                            .append(Component.text("!", TextColor.fromHexString("#2db4e1"), TextDecoration.BOLD));
                    receivers.sendMessage(partyMsg);
                    if(player != null) player.sendMessage(partyMsg);
                    plugin.getServer().getOnlinePlayers().forEach(oPlayer -> ExcellentCratesAPI.getKeyManager().giveKey(oPlayer, ExcellentCratesAPI.getKeyManager().getKeyById("crate_vote"), 3));
                }
            } else {
                plugin.getLogger().warning("Failed to get vote ID from vote made by " + playerName + " on site " + serviceName + "!");
            }
        } else {
            plugin.getLogger().info("Received a vote from " + serviceName + "! Player with the name " + playerName + " has never joined the server! Ignoring vote..");
        }
    }
}
