package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class BlockDamage implements Listener {
    private SkyPrisonCore plugin;

    public BlockDamage(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) throws IOException {
        Block b = event.getBlock();
        Location loc = b.getLocation();
        if (b.getType() == Material.SPONGE) {
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
                            for (int v = 0; v < setList.size(); v++) {
                                Random random = new Random();
                                int rand = random.nextInt(setList.size());
                                Location placeSponge = new Location(w, yamlf.getDouble("locations." + rand + ".x"),
                                        yamlf.getDouble("locations." + rand + ".y"), yamlf.getDouble("locations." + rand + ".z"));
                                placeSponge = placeSponge.getBlock().getLocation();
                                if (!placeSponge.equals(loc)) {
                                    for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                                        online.sendMessage(ChatColor.WHITE + "[" + ChatColor.YELLOW + "Sponge" + ChatColor.WHITE + "] "
                                                + ChatColor.GOLD + event.getPlayer().getName() + ChatColor.YELLOW
                                                + " has found the sponge! A new one will be hidden somewhere in prison.");
                                    }
                                    File spongeData = new File(plugin.getDataFolder() + File.separator
                                            + "spongedata.yml");
                                    FileConfiguration sDataConf = YamlConfiguration.loadConfiguration(spongeData);
                                    String pUUID = event.getPlayer().getUniqueId().toString();
                                    if(sDataConf.isConfigurationSection(pUUID)) {
                                        int spongeFound = sDataConf.getInt(pUUID + ".sponge-found") + 1;
                                        sDataConf.set(pUUID + ".sponge-found", spongeFound);
                                    } else {
                                        sDataConf.set(pUUID + ".sponge-found", 1);
                                    }
                                    sDataConf.save(spongeData);

                                    plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(event.getPlayer()), 25);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
