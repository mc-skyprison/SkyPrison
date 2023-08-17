package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerSwapHandItems implements Listener {
    private final SkyPrisonCore plugin;
    public PlayerSwapHandItems(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        ItemStack mainHand = event.getMainHandItem();
        if(mainHand != null && mainHand.getType().equals(Material.WRITABLE_BOOK)) {
            Player player = event.getPlayer();
            if(plugin.writingMail.containsKey(player.getUniqueId())) {
                if(mainHand.getPersistentDataContainer().has(plugin.writingMail.get(player.getUniqueId()).getKey())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
