package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.guard.Safezone;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
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
    private final SkyPrisonCore plugin;
    private final Safezone safezone;
    private final DatabaseHook db;
    private final DailyMissions dailyMissions;

    public EntityDeath(SkyPrisonCore plugin, Safezone safezone, DatabaseHook db, DailyMissions dailyMissions) {
        this.plugin = plugin;
        this.safezone = safezone;
        this.db = db;
        this.dailyMissions = dailyMissions;
    }
    @EventHandler
    public void playerDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            safezone.safezoneViolators.remove(player.getUniqueId());
        }

        if(!(event.getEntity() instanceof Player) && event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            for (String mission : dailyMissions.getMissions(player)) {
                if(!dailyMissions.isCompleted(player, mission)) {
                    String[] missSplit = mission.split("-");
                    if (missSplit[0].equalsIgnoreCase("kill")) {
                        switch (missSplit[1].toLowerCase()) {
                            case "any":
                                if (event.getEntity() instanceof Monster) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                                break;
                            case "zombie":
                                if (event.getEntityType().equals(EntityType.ZOMBIE)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                                break;
                            case "skeleton":
                                if (event.getEntityType().equals(EntityType.SKELETON)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                                break;
                        }
                    } else if (missSplit[0].equalsIgnoreCase("slaughter")) {
                        switch (missSplit[1].toLowerCase()) {
                            case "any":
                                if (event.getEntity() instanceof Animals) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                                break;
                            case "pig":
                                if (event.getEntityType().equals(EntityType.PIG)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                                break;
                            case "cow":
                                if (event.getEntityType().equals(EntityType.COW)) {
                                    dailyMissions.updatePlayerMission(player, mission);
                                }
                                break;
                        }
                    }
                }
            }
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
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "money give " + killer.getName() + " " + bounty);
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi usermeta " + killer.getName() + " increment bounties_collected +1 -s");
                        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                            if (!online.hasPermission("skyprisoncore.bounty.silent")) {
                                online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "]" + ChatColor.YELLOW + " " + killer.getName() + " has claimed the bounty on " + killed.getName() + "!");
                            }
                        }
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM bounties WHERE user_id = ?")) {
                            ps.setString(1, killed.getUniqueId().toString());
                            ps.executeUpdate();
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
                                pvpSet(killed, killer, true);
                            } else {
                                if (killer.getWorld().getName().equalsIgnoreCase("world_prison")
                                        || killer.getWorld().getName().equalsIgnoreCase("world_free")
                                        || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")
                                        || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")) {


                                    long timeRem = 300 - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                    killer.sendMessage(ChatColor.GRAY + "You have to wait " + ChatColor.RED + timeRem + ChatColor.GRAY + " seconds before receiving tokens from this player!");
                                }
                            }
                        } else {
                            pvpSet(killed, killer, false);
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
            plugin.asConsole("lp user " + killed.getName() + " permission set skyprisoncore.tag.52");
            killer.sendMessage(plugin.colourMessage("&7You have died a whopping &c&l1000 &7times! Therefore, you get a special tag!"));
        }

        if(pKills == 1000) {
            plugin.asConsole("lp user " + killer.getName() + " permission set skyprisoncore.tag.53");
            killed.sendMessage(plugin.colourMessage("&7You have killed &c&l1000 &7players! Therefore, you get a special tag!"));
        }

        if(pKillStreak % 5 == 0 && pKillStreak <= 100) {
            killer.sendMessage(plugin.colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l15 &7tokens as a reward!"));
            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 15, "Kill Streak", String.valueOf(pKillStreak));
        } else if(pKillStreak % 50 == 0) {
            killer.sendMessage(plugin.colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l30 &7tokens as a reward!"));
            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 30, "Kill Streak", String.valueOf(pKillStreak));
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

    public void pvpSet(Player killed, Player killer, boolean hasKilled) {
        if(killed.hasPermission("skyprisoncore.guard.onduty")) {
            killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "15" + ChatColor.GRAY + " token!");
            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 15, "Guard Kill", killed.getName());
        } else {
            killer.sendMessage(ChatColor.GRAY + "You killed " + ChatColor.RED + killed.getName() + ChatColor.GRAY + " and received " + ChatColor.RED + "1" + ChatColor.GRAY + " token!");
            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 1, "Player Kill", killed.getName());
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
