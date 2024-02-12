package net.skyprison.skyprisoncore.listeners.minecraft;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.TokenUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class EntityDeath implements Listener {
    private final DatabaseHook db;
    public EntityDeath(DatabaseHook db) {
        this.db = db;
    }
    @EventHandler
    public void playerDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity instanceof Player player) {
            SkyPrisonCore.safezoneViolators.remove(player.getUniqueId());
        }

        if(entity.getKiller() instanceof Player player && (entity instanceof Monster || entity instanceof Animals)) {
            DailyMissions.updatePlayerMissions(player.getUniqueId(), entity instanceof Monster ? "kill" : "slaughter", event.getEntityType());
        }

        if(event.getEntity() instanceof Player killed && killed.getKiller() != null) {
            Player killer = killed.getKiller();
            if(!killed.equals(killer)) {
                if(!killed.getWorld().getName().equalsIgnoreCase("world_event") && !killed.getWorld().getName().equalsIgnoreCase("world_war")) {
                    //
                    // Bounty Stuff
                    //
                    double bounty = 0.0;
                    boolean hasBounty = false;
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT prize FROM bounties WHERE user_id = ?")) {
                        ps.setString(1, killed.getUniqueId().toString());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            bounty = rs.getDouble(1);
                            hasBounty = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if(hasBounty) {
                        Component bountyPrefix = Component.text("Bounties", NamedTextColor.RED).append(Component.text(" | ", NamedTextColor.WHITE));
                        Component bountyMsg = bountyPrefix.append(Component.text(killer.getName() + " has killed " + killed.getName() + " and claimed the ", NamedTextColor.YELLOW)
                                .append(Component.text("$" + ChatUtils.formatNumber(bounty), NamedTextColor.GREEN))
                                .append(Component.text(" bounty on them!", NamedTextColor.YELLOW)));

                        Component targetMsg = bountyPrefix.append(Component.text(killer.getName() + " has claimed the ", NamedTextColor.YELLOW)
                                    .append(Component.text("$" + ChatUtils.formatNumber(bounty), NamedTextColor.GREEN))
                                    .append(Component.text(" bounty on you!", NamedTextColor.YELLOW)));

                        Audience receivers = Bukkit.getServer().filterAudience(audience -> {
                            if(audience instanceof Player onlinePlayer) {
                                return !onlinePlayer.hasPermission("skyprisoncore.command.bounty.silent") && !onlinePlayer.getUniqueId().equals(killed.getUniqueId());
                            }
                            return true;
                        });
                        receivers.sendMessage(bountyMsg);
                        killed.sendMessage(targetMsg);

                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM bounties WHERE user_id = ?")) {
                            ps.setString(1, killed.getUniqueId().toString());
                            ps.executeUpdate();
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi money give " + killer.getName() + " " + bounty);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    //
                    // Token Kills Stuff
                    //

                    long killedOn = 0;
                    boolean hasKilled = false;
                    int deaths = 0;
                    int kills = 0;
                    int killstreak = 0;
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT pvp_kills, pvp_killstreak FROM users WHERE user_id = ?")) {
                        ps.setString(1, killer.getUniqueId().toString());

                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            kills = rs.getInt(1);
                            killstreak = rs.getInt(2);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT killed_on FROM recently_killed WHERE killer_id = ? AND killed_id = ?")) {
                        ps.setString(1, killer.getUniqueId().toString());
                        ps.setString(2, killed.getUniqueId().toString());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            hasKilled = true;
                            killedOn = rs.getLong(1);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }


                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT pvp_deaths FROM users WHERE user_id = ?")) {
                        ps.setString(1, killed.getUniqueId().toString());
                        ResultSet rs = ps.executeQuery();
                        while(rs.next()) {
                            deaths = rs.getInt(1);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    CMIUser userK = CMI.getInstance().getPlayerManager().getUser(killer);
                    CMIUser userD = CMI.getInstance().getPlayerManager().getUser(killed);
                    if (!userD.getLastIp().equalsIgnoreCase(userK.getLastIp())) {
                        if (hasKilled) {
                            long timeLeft = System.currentTimeMillis() - killedOn;
                            if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 300) {
                                pvpSet(killed, killer);
                            } else {
                                if (killer.getWorld().getName().equalsIgnoreCase("world_prison")
                                        || killer.getWorld().getName().equalsIgnoreCase("world_free")
                                        || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")
                                        || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")) {


                                    long timeRem = 300 - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                    killer.sendMessage(Component.text("You have to wait ", NamedTextColor.GRAY).append(Component.text(timeRem, NamedTextColor.RED))
                                            .append(Component.text(" seconds before receiving tokens from this player!", NamedTextColor.GRAY)));
                                }
                            }
                        } else {
                            pvpSet(killed, killer);
                        }
                        updateKills(killer, killed, kills, deaths, killstreak);
                    }
                }
            }
        }
    }

    public void updateKills(Player killer, Player killed, int kills, int deaths, int killstreak) {
        int pKills = kills + 1;
        int pDeaths = deaths + 1;
        int pKillStreak = killstreak + 1;

        if(pDeaths == 1000) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + killed.getName() + " permission set skyprisoncore.tag.52");
            killer.sendMessage(Component.text("You have died a whopping", NamedTextColor.GRAY).append(Component.text(" 1,000 ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("times! Therefore, you get a special tag!", NamedTextColor.GRAY))));
        }

        if(pKills == 1000) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + killer.getName() + " permission set skyprisoncore.tag.53");
            killer.sendMessage(Component.text("You have killed a whopping", NamedTextColor.GRAY).append(Component.text(" 1,000 ", NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("players! Therefore, you get a special tag!", NamedTextColor.GRAY))));
        }

        if(pKillStreak % 5 == 0 && pKillStreak <= 100) {
            killer.sendMessage(Component.text("You've hit a kill streak of ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(pKillStreak), NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("! You have received", NamedTextColor.GRAY).append(Component.text(" 15 tokens ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text("as a reward!", NamedTextColor.GRAY))))));
            TokenUtils.addTokens(killer.getUniqueId(), 15, "Kill Streak", String.valueOf(pKillStreak));
        } else if(pKillStreak % 50 == 0) {
            killer.sendMessage(Component.text("You've hit a kill streak of ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(pKillStreak), NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text("! You have received", NamedTextColor.GRAY).append(Component.text(" 30 tokens ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text("as a reward!", NamedTextColor.GRAY))))));
            TokenUtils.addTokens(killer.getUniqueId(), 30, "Kill Streak", String.valueOf(pKillStreak));
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET pvp_kills = pvp_kills + ?, pvp_killstreak = pvp_killstreak + ? WHERE user_id = ?")) {
            ps.setInt(1, 1);
            ps.setInt(2, 1);
            ps.setString(3, killer.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET pvp_deaths = pvp_deaths + ?, pvp_killstreak = 0 WHERE user_id = ?")) {
            ps.setInt(1, 1);
            ps.setString(2, killed.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void pvpSet(Player killed, Player killer) {
        if(killed.hasPermission("skyprisoncore.guard.onduty")) {
            killer.sendMessage(Component.text("You killed ", NamedTextColor.GRAY).append(Component.text(killed.getName(), NamedTextColor.RED))
                    .append(Component.text(" and received ", NamedTextColor.GRAY)).append(Component.text("15", NamedTextColor.RED)).append(Component.text(" tokens!", NamedTextColor.GRAY)));
            TokenUtils.addTokens(killer.getUniqueId(), 15, "Guard Kill", killed.getName());
        } else {
            killer.sendMessage(Component.text("You killed ", NamedTextColor.GRAY).append(Component.text(killed.getName(), NamedTextColor.RED))
                    .append(Component.text(" and received ", NamedTextColor.GRAY)).append(Component.text("1", NamedTextColor.RED)).append(Component.text(" token!", NamedTextColor.GRAY)));
            TokenUtils.addTokens(killer.getUniqueId(), 1, "Player Kill", killed.getName());
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO recently_killed (killer_id, killed_id, killed_on) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE killed_on = VALUE(killed_on)")) {
            ps.setString(1, killer.getUniqueId().toString());
            ps.setString(2, killed.getUniqueId().toString());
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
