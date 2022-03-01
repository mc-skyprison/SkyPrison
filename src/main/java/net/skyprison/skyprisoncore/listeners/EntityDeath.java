package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.guard.Safezone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class EntityDeath implements Listener {
    private SkyPrisonCore plugin;
    private Safezone safezone;

    public EntityDeath(SkyPrisonCore plugin, Safezone safezone) {
        this.plugin = plugin;
        this.safezone = safezone;
    }
    @EventHandler
    public void playerDeath(EntityDeathEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(safezone.safezoneViolators.containsKey(player.getUniqueId())) {
                safezone.safezoneViolators.remove(player.getUniqueId());
            }
        }

        if(event.getEntity() instanceof Player && event.getEntity().getKiller() != null) {
            Player killed = (Player) event.getEntity();
            Player killer = killed.getKiller();
            if(!killed.equals(killer)) {
                if(!killed.getWorld().getName().equalsIgnoreCase("world_event") && !killed.getWorld().getName().equalsIgnoreCase("world_war")) {
                    //
                    // Bounty Stuff
                    //
                    File f = new File(plugin.getDataFolder() + File.separator + "bounties.yml");
                    FileConfiguration bounty = YamlConfiguration.loadConfiguration(f);
                    Set<String> bountyList = bounty.getKeys(false);
                    for (String bountyPlayer : bountyList) {
                        if (killed.getUniqueId().equals(UUID.fromString(bountyPlayer))) {
                            try {
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "money give " + killer.getName() + " " + bounty.getDouble(bountyPlayer + ".bounty-prize"));
                                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "cmi usermeta " + killer.getName() + " increment bounties_collected +1 -s");
                                bounty.set(bountyPlayer, null);
                                bounty.save(f);
                                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                    if (!online.hasPermission("skyprisoncore.bounty.silent")) {
                                        online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "]" + ChatColor.YELLOW + " " + killer.getName() + " has claimed the bounty on " + killed.getName() + "!");
                                    }
                                }
                            } catch (final IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    //
                    // Token Kills Stuff
                    //
                    f = new File(plugin.getDataFolder() + File.separator + "recentkills.yml");
                    FileConfiguration kills = YamlConfiguration.loadConfiguration(f);
                    CMIUser userK = CMI.getInstance().getPlayerManager().getUser(killer);
                    CMIUser userD = CMI.getInstance().getPlayerManager().getUser(killed);
                    if (!userD.getLastIp().equalsIgnoreCase(userK.getLastIp())) {
                        if (kills.contains(killer.getUniqueId() + ".kills")) {
                            Set<String> killsList = Objects.requireNonNull(kills.getConfigurationSection(killer.getUniqueId() + ".kills")).getKeys(false);
                            if (killsList.contains(killed.getUniqueId().toString())) {
                                for (String killedPlayer : killsList) {
                                    if (killed.getUniqueId().equals(UUID.fromString(killedPlayer))) {
                                        long time = kills.getLong(killer.getUniqueId() + ".kills." + killedPlayer + ".time");
                                        long timeLeft = System.currentTimeMillis() - time;
                                        if (TimeUnit.MILLISECONDS.toSeconds(timeLeft) >= 300) {
                                            plugin.PvPSet(killed, killer);
                                        } else {
                                            if (killer.getWorld().getName().equalsIgnoreCase("world_prison")
                                                    || killer.getWorld().getName().equalsIgnoreCase("world_free")
                                                    || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")
                                                    || killer.getWorld().getName().equalsIgnoreCase("world_free_nether")) {
                                                int pKills = kills.getInt(killer.getUniqueId() + ".pvpkills") + 1;
                                                int pDeaths = kills.getInt(killed.getUniqueId() + ".pvpdeaths") + 1;
                                                int pKillStreak = kills.getInt(killer.getUniqueId() + ".pvpkillstreak") + 1;
                                                kills.set(killer.getUniqueId() + ".pvpkills", pKills);
                                                kills.set(killer.getUniqueId() + ".pvpkillstreak", pKillStreak);

                                                kills.set(killed.getUniqueId() + ".pvpkillstreak", 0);
                                                kills.set(killed.getUniqueId() + ".pvpdeaths", pDeaths);

                                                if(pDeaths == 1000) {
                                                    plugin.asConsole("lp user " + killed.getName() + " permission set deluxetags.tag.death");
                                                    killer.sendMessage(plugin.colourMessage("&7You have died a whopping &c&l1000 &7times! Therefore, you get a special tag!"));
                                                }

                                                if(pKills == 1000) {
                                                    plugin.asConsole("lp user " + killer.getName() + " permission set deluxetags.tag.kills");
                                                    killed.sendMessage(plugin.colourMessage("&7You have killed &c&l1000 &7players! Therefore, you get a special tag!"));
                                                }

                                                if(pKillStreak % 5 == 0 && pKillStreak <= 100) {
                                                    killer.sendMessage(plugin.colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l15 &7tokens as a reward!"));
                                                    plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 15);

                                                } else if(pKillStreak % 50 == 0 && pKillStreak > 100) {
                                                    killer.sendMessage(plugin.colourMessage("&7You've hit a kill streak of &c&l" + pKillStreak + "&7! You have received &c&l30 &7tokens as a reward!"));
                                                    plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(killer), 30);
                                                }

                                                try {
                                                    kills.save(f);
                                                    long timeRem = 300 - TimeUnit.MILLISECONDS.toSeconds(timeLeft);
                                                    killer.sendMessage(ChatColor.GRAY + "You have to wait " + ChatColor.RED + timeRem + ChatColor.GRAY + " seconds before receiving tokens from this player!");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                plugin.PvPSet(killed, killer);
                            }
                        } else {
                            plugin.PvPSet(killed, killer);
                        }
                    }
                }
            }
        }
    }
}
