package net.skyprison.skyprisoncore.listeners;

import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncChatDecorate implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public AsyncChatDecorate(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatDecorateEvent event) {
        Component msg = event.originalMessage();
        event.result(plugin.getParsedMessage(event.player(), PlainTextComponentSerializer.plainText().serialize(msg)));
    }
}
