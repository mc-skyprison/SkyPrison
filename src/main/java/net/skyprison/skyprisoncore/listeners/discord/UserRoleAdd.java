package net.skyprison.skyprisoncore.listeners.discord;

import org.bukkit.Bukkit;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.listener.server.role.UserRoleAddListener;

import java.util.UUID;

import static net.skyprison.skyprisoncore.utils.PlayerManager.getIdFromDiscord;
import static net.skyprison.skyprisoncore.utils.PlayerManager.getPlayerName;


public class UserRoleAdd implements UserRoleAddListener {
    public UserRoleAdd() {
    }

    @Override
    public void onUserRoleAdd(UserRoleAddEvent event) {
        if(event.getRole().getId() != 799644093742448711L) return;
        UUID pUUID = getIdFromDiscord(event.getUser().getId());

        if (pUUID == null) return;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + getPlayerName(pUUID) + " permission set deluxetags.tag.serverbooster");
    }
}
