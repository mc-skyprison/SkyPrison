package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.items.Shrek;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryOpen implements Listener {
    private final SkyPrisonCore plugin;
    public InventoryOpen(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryType type = event.getInventory().getType();
        switch (type) {
            case MERCHANT -> {
                Player player = (Player) event.getPlayer();
                player.sendMessage(Component.text("Villager trading has been disabled", NamedTextColor.RED));
                if(!player.isOp()) {
                    event.setCancelled(true);
                }
            }
            case SMITHING -> event.setCancelled(true);
            case ENCHANTING -> {
                Location loc = event.getInventory().getLocation();
                if(loc != null && loc.equals(new Location(Bukkit.getWorld("world_prison"), -121, 150, -175))) {
                    Player player = (Player) event.getPlayer();
                    ItemStack grease = Shrek.getShrekGrease(plugin, 1);
                    if(!player.getInventory().containsAtLeast(grease, 1)) {
                        event.setCancelled(true);
                        player.sendMessage(Component.text("The enchant table doesn't seem to be working.. I should ask Shrek about it.", NamedTextColor.GRAY, TextDecoration.ITALIC));
                    }
                }
            }
        }
    }
}
