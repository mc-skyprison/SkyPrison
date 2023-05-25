package net.skyprison.skyprisoncore.listeners.excellentcrates;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import su.nightexpress.excellentcrates.api.event.CrateObtainRewardEvent;

import java.util.ArrayList;
import java.util.List;

public class CrateObtainReward implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public CrateObtainReward(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    @EventHandler
    public void onCrateObtainReward(CrateObtainRewardEvent event) {
        Player player = event.getPlayer();
        String sql = "INSERT INTO casino_opens (user_id, casino_name, opens_amount) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT(user_id, casino_name) DO UPDATE SET opens_amount = opens_amount + excluded.opens_amount";
        List<Object> params = new ArrayList<>() {{
            add(player.getUniqueId().toString());
            add(event.getCrate().getId());
            add(1);
        }};
        hook.sqlUpdate(sql, params);
    }
}
