package net.skyprison.skyprisoncore.listeners.discord;

import org.bukkit.Bukkit;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;

import java.util.UUID;

import static net.skyprison.skyprisoncore.utils.PlayerManager.getIdFromDiscord;
import static net.skyprison.skyprisoncore.utils.PlayerManager.getPlayerName;

public class UserRoleRemove implements UserRoleRemoveListener {
    public UserRoleRemove() {
    }

    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        if(event.getRole().getId() != 799644093742448711L) return;
        UUID pUUID = getIdFromDiscord(event.getUser().getId());

        if (pUUID == null) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getPlayerName(pUUID) + " permission unset deluxetags.tag.serverbooster");
    }
}
