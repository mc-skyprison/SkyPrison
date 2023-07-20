package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.items.Greg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

public class EnchantItem implements Listener {
    private final SkyPrisonCore plugin;
    public EnchantItem(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Location loc = event.getInventory().getLocation();
        if(loc != null && loc.equals(new Location(Bukkit.getWorld("world_prison"), -121, 150, -175))) {
            Player player = event.getEnchanter();
            ItemStack grease = Greg.getGrease(plugin, 1);
            if(player.getInventory().containsAtLeast(grease, 1)) {
                boolean removedAll = player.getInventory().removeItem(grease).isEmpty();
                if(!removedAll) {
                    event.setCancelled(true);
                    player.sendMessage(Component.text("The enchant table doesn't seem to be working.. I should ask Greg about it.", NamedTextColor.GRAY, TextDecoration.ITALIC));
                }
            } else {
                event.setCancelled(true);
                player.sendMessage(Component.text("The enchant table doesn't seem to be working.. I should ask Greg about it.", NamedTextColor.GRAY, TextDecoration.ITALIC));
            }
        }
    }
}
