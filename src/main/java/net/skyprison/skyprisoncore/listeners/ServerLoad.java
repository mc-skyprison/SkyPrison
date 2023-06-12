package net.skyprison.skyprisoncore.listeners;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.ShinyGrassTask;
import net.skyprison.skyprisoncore.utils.SpongeTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.concurrent.TimeUnit;

public class ServerLoad implements Listener {

    private final SkyPrisonCore plugin;
    private final PlayerParticlesAPI particles;
    private final DatabaseHook db;

    public ServerLoad(SkyPrisonCore plugin, PlayerParticlesAPI particles, DatabaseHook db) {
        this.plugin = plugin;
        this.particles = particles;
        this.db = db;
    }


    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.spongeTimer.schedule(new SpongeTask(plugin, db), TimeUnit.MINUTES.toMillis(10));

        int radius = 300;
        World prisonWorld = Bukkit.getWorld("world_prison");
        for(int x = -radius; x <= radius; x++) {
            for(int z = -radius; z <= radius; z++) {
                for(int y = 130; y <= 174; y++) {
                    Block block = prisonWorld.getBlockAt(x, y, z);
                    Material bType = block.getType();
                    if (bType.equals(Material.TALL_GRASS) || bType.equals(Material.GRASS) || bType.equals(Material.LARGE_FERN) || bType.equals(Material.FERN)) {
                        plugin.grassLocations.add(block);
                    }
                }
            }
        }

        plugin.shinyTimer.schedule(new ShinyGrassTask(plugin, particles), TimeUnit.MINUTES.toMillis(15));
    }
}
