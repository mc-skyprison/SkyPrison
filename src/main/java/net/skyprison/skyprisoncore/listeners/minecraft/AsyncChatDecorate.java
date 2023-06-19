package net.skyprison.skyprisoncore.listeners.minecraft;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncChatDecorate implements Listener {
    private final SkyPrisonCore plugin;

    public AsyncChatDecorate(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChatDecorate(AsyncChatDecorateEvent event) {
        Component msg = event.originalMessage();
        event.result(plugin.getParsedMessage(event.player(), PlainTextComponentSerializer.plainText().serialize(msg)));
    }
}
