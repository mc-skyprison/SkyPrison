package net.skyprison.skyprisoncore.listeners.cmi;

import com.Zrips.CMI.events.CMIPlayerTeleportRequestEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CMIPlayerTeleportRequest implements Listener {
    @EventHandler
    public void onCMIPlayerTeleportRequest(CMIPlayerTeleportRequestEvent event) {
        Player sender = event.getWhoOffers();
        Player receiver = event.getWhoAccepts();

        PlayerManager.Ignore ignoring = PlayerManager.getPlayerIgnore(sender.getUniqueId(), receiver.getUniqueId());
        if(ignoring != null && ignoring.ignoreTeleport()) {
            sender.sendMessage(Component.text("Can't send teleport requests to players you're ignoring!", NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }

        PlayerManager.Ignore ignored = PlayerManager.getPlayerIgnore(receiver.getUniqueId(), sender.getUniqueId());
        if(ignored != null && ignored.ignoreTeleport()) {
            sender.sendMessage(receiver.displayName().colorIfAbsent(NamedTextColor.RED).append(Component.text(" is ignoring your teleport requests!", NamedTextColor.RED)));
            event.setCancelled(true);
        }
    }
}
