package net.skyprison.skyprisoncore.listeners.pvpmanager;

import me.NoChance.PvPManager.Events.PlayerTogglePvPEvent;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.UnlimitedNametagManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerTogglePvP  implements Listener {

    private final SkyPrisonCore plugin;

    public PlayerTogglePvP(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTogglePvP(PlayerTogglePvPEvent event) {
        if(TabAPI.getInstance().getTeamManager() instanceof UnlimitedNametagManager) {
            TabPlayer tPlayer = TabAPI.getInstance().getPlayer(event.getPlayer().getUniqueId());
            UnlimitedNametagManager nametagManager = (UnlimitedNametagManager) TabAPI.getInstance().getTeamManager();
            if (event.getPvPState()) {
                nametagManager.resetLine(tPlayer, "belowname");
            } else {
                nametagManager.setLine(tPlayer, "belowname", "<red>PVP OFF</red>");
            }
        }
    }
}
