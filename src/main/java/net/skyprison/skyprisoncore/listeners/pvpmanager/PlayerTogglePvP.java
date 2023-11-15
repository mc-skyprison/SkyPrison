package net.skyprison.skyprisoncore.listeners.pvpmanager;

import me.NoChance.PvPManager.Events.PlayerTogglePvPEvent;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTogglePvP  implements Listener {
    public PlayerTogglePvP() {}

    @EventHandler
    public void onPlayerTogglePvP(PlayerTogglePvPEvent event) {
        if(TabAPI.getInstance().getNameTagManager() instanceof UnlimitedNameTagManager nametagManager) {
            TabPlayer tPlayer = TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId());
            if (event.getPvPState()) {
                nametagManager.setLine(tPlayer, "belowname", null);
            } else {
                nametagManager.setLine(tPlayer, "belowname", "<red>PVP OFF</red>");
            }
        }
    }
}
