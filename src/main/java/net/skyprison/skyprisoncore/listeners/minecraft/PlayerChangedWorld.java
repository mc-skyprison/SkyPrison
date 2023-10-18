package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import static net.skyprison.skyprisoncore.utils.PlayerInvUtils.changeInventory;
import static net.skyprison.skyprisoncore.utils.PlayerInvUtils.isPrisonWorld;

public class PlayerChangedWorld implements Listener {
    private final DatabaseHook db;
    public PlayerChangedWorld(DatabaseHook db) {
        this.db = db;
    }
    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if(!player.hasPermission("skyprisoncore.invchange.bypass")) {
            World fromWorld = event.getFrom();
            String fromName = fromWorld.getName();

            boolean fromPrison = isPrisonWorld(fromName);

            World toWorld = player.getWorld();
            String toName = toWorld.getName();

            boolean toPrison = isPrisonWorld(toName);

            changeInventory(player, fromPrison, toPrison);
        }
    }
}
