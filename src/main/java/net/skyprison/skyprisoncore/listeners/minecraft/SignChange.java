package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

public class SignChange implements Listener {
    private final SkyPrisonCore plugin;

    public SignChange(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("skyprisoncore.sign.colours")) {
            List<Component> lines = event.lines();
            int i = 0;
            for (Component line : lines) {
                String lineString = PlainTextComponentSerializer.plainText().serialize(line);
                Component serialized = plugin.playerMsgBuilder.deserialize(lineString);
                event.line(i, serialized);
                i++;
            }

        }
    }
}
