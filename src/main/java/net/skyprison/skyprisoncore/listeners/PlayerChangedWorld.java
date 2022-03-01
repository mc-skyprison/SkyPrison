package net.skyprison.skyprisoncore.listeners;

import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorld implements Listener {
    @EventHandler
    public void worldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fWorld = event.getFrom();
        World tWorld = player.getWorld();
        if(tWorld.getName().equalsIgnoreCase("world_prison") || tWorld.getName().equalsIgnoreCase("world_event") || player.getWorld().getName().equalsIgnoreCase("world_war")) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(16);
        } else if(fWorld.getName().equalsIgnoreCase("world_prison")) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue());
        }
    }
}
