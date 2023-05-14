package net.skyprison.skyprisoncore.utils;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.DefaultStyles;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ShinyGrassTask extends TimerTask {
    private final SkyPrisonCore plugin;
    private final PlayerParticlesAPI particles;

    public ShinyGrassTask(SkyPrisonCore plugin, PlayerParticlesAPI particles) {
        this.plugin = plugin;
        this.particles = particles;
    }

    @Override
    public void run() {
        Random rand = new Random();
        World world = Bukkit.getWorld("world_prison");

        if(!plugin.grassLocations.isEmpty() && plugin.grassLocations.size() > 2) {
            int amount = 3;

            if(rand.nextInt(100) > 79) {
                amount = rand.nextInt(5) + 1;
            }

            Collections.shuffle(plugin.grassLocations);
            List<Block> blocks = new ArrayList<>();

            for(int i = 0; i < amount; i++) {
                blocks.add(plugin.grassLocations.get(i));
            }

            if (!plugin.shinyGrass.isEmpty()) {
                particles.removeFixedEffectsInRange(plugin.shinyGrass.get(0), 1000);
                plugin.shinyGrass = new ArrayList<>();
            }


            for(Block block : blocks) {
                Location loc = block.getLocation();
                plugin.shinyGrass.add(loc);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(Location loc : plugin.shinyGrass) {
                        Location newLoc = loc.clone().offset(0.5, 0, 0.5).toLocation(world);
                        particles.createFixedParticleEffect(Bukkit.getConsoleSender(),
                                newLoc,
                                ParticleEffect.ELECTRIC_SPARK,
                                DefaultStyles.NORMAL);
                    }
                }
            }.runTaskLater(plugin, 20*5);

            int randTime = 15;

            if(rand.nextInt(100) < 50) {
                randTime = ThreadLocalRandom.current().nextInt(5, 16);
            }

            plugin.shinyTimer.schedule(new ShinyGrassTask(plugin, particles), TimeUnit.MINUTES.toMillis(randTime));
        }
    }
}
