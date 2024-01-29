package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.Recipes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class EntityPickupItem implements Listener {
    private final SkyPrisonCore plugin;
    public EntityPickupItem(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
                if (PlayerManager.isGuardGear(event.getItem().getItemStack())) {
                    event.setCancelled(true);
                }
                PlayerManager.checkGuardGear(player);
            }

            if (!event.isCancelled()) {
                ItemStack item = event.getItem().getItemStack();
                switch (item.getType()) {
                    case IRON_INGOT, IRON_BLOCK -> {
                        if(!player.hasDiscoveredRecipe(new NamespacedKey(plugin, "iron_sword"))) {
                            Recipes.discoverIronRecipes(player);
                        }
                    }
                    case NETHER_WART -> {
                        if(!player.hasDiscoveredRecipe(new NamespacedKey(plugin, "nether_wart"))) {
                            Recipes.discoverRecipe(player, new NamespacedKey(plugin, "nether_wart"));
                        }
                    }
                }

            }
        }
    }
}
