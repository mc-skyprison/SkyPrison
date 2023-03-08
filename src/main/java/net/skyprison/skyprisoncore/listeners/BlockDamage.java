package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import java.io.File;
import java.util.*;

public class BlockDamage implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private DailyMissions dailyMissions;

    public BlockDamage(SkyPrisonCore plugin, DatabaseHook db, DailyMissions dailyMissions) {
        this.plugin = plugin;
        this.db = db;
        this.dailyMissions = dailyMissions;
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        Player player = event.getPlayer();

        if (b.getType() == Material.SPONGE) {

            for (String mission : dailyMissions.getPlayerMissions(player)) {
                String[] missSplit = mission.split("-");
                if (missSplit[0].equalsIgnoreCase("sponge")) {
                    int currAmount = Integer.parseInt(missSplit[4]) + 1;
                    String nMission = missSplit[0] + "-" + missSplit[1] + "-" + missSplit[2] + "-" + missSplit[3] + "-" + currAmount;
                    dailyMissions.updatePlayerMission(player, mission, nMission);
                }
            }

            if (loc.getWorld().getName().equalsIgnoreCase("world_prison")) {
                File f = new File(plugin.getDataFolder() + File.separator + "spongelocations.yml");
                FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                Set<String> setList = Objects.requireNonNull(yamlf.getConfigurationSection("locations")).getKeys(false);
                for (int i = 0; i < setList.size(); i++) {
                    if (yamlf.contains("locations." + i)) {
                        World w = Bukkit.getServer().getWorld(Objects.requireNonNull(yamlf.getString("locations." + i + ".world")));
                        Location spongeLoc = new Location(w, yamlf.getDouble("locations." + i + ".x"),
                                yamlf.getDouble("locations." + i + ".y"), yamlf.getDouble("locations." + i + ".z"));
                        spongeLoc = spongeLoc.getBlock().getLocation();
                        if (loc.equals(spongeLoc)) {
                            loc.getBlock().setType(Material.AIR);
                            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                online.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW + "Sponge" + ChatColor.WHITE + "] "
                                        + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW
                                        + " has found the sponge! A new one will be hidden somewhere in prison.");
                            }

                            String sql = "UPDATE users SET sponges_found = sponges_found + 1 WHERE user_id = ?";
                            List<Object> params = new ArrayList<Object>() {{
                                add(player.getUniqueId());
                            }};
                            db.sqlUpdate(sql, params);

                            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(event.getPlayer()), 25);
                            break;
                        }
                    }
                }
            }
        }
    }
}
