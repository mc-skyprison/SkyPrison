package net.skyprison.skyprisoncore.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class EntityRemoveFromWorld implements Listener {
    private SkyPrisonCore plugin;

    public EntityRemoveFromWorld(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        if (event.getEntity().getLocation().getY() < -63) {
            if(event.getEntity().getWorld().getName().equalsIgnoreCase("world_prison")) {
                if (event.getEntityType() == EntityType.DROPPED_ITEM) {
                    Item item = (Item) event.getEntity();
                    ItemStack sItem = item.getItemStack();
                    File f = new File(plugin.getDataFolder() + File.separator + "dropchest.yml");
                    FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                    if (!yamlf.isConfigurationSection("items")) {
                        yamlf.createSection("items");
                    }
                    try {
                        yamlf.save(f);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Set<String> dropList = Objects.requireNonNull(yamlf.getConfigurationSection("items")).getKeys(false);
                    int page = 0;
                    for (int i = 0; i < dropList.size() + 2; ) {
                        ArrayList<String> arr = new ArrayList<>();
                        for (String dropItem : dropList) {
                            if (yamlf.getInt("items." + dropItem + ".page") == i) {
                                arr.add(dropItem);
                            }
                        }
                        if (arr.size() <= 44) {
                            page = i;
                            break;
                        } else {
                            i++;
                        }
                    }
                    for (int i = 0; i < dropList.size() + 2; i++) {
                        if (!yamlf.contains("items." + i)) {
                            yamlf.set("items." + i + ".item", sItem);
                            yamlf.set("items." + i + ".page", page);
                            try {
                                yamlf.save(f);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
