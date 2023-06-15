package net.skyprison.skyprisoncore.listeners.pvpmanager;

import me.NoChance.PvPManager.Events.PlayerTogglePvPEvent;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.UnlimitedNametagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTogglePvP  implements Listener {
    public PlayerTogglePvP() {}

    @EventHandler
    public void onPlayerTogglePvP(PlayerTogglePvPEvent event) {
        if(TabAPI.getInstance().getTeamManager() instanceof UnlimitedNametagManager nametagManager) {
            TabPlayer tPlayer = TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId());
            if (event.getPvPState()) {
                nametagManager.resetLine(tPlayer, "belowname");
            } else {
                nametagManager.setLine(tPlayer, "belowname", "<red>PVP OFF</red>");
            }
        }
    }
}
