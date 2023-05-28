package net.skyprison.skyprisoncore.listeners.mcmmo;

import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.util.Objects;

public class McMMOPartyChat implements Listener {

    private DiscordApi discApi;

    public McMMOPartyChat (DiscordApi discApi) {
        this.discApi = discApi;
    }

    @EventHandler
    public void onMcMMOPartyChat(McMMOPartyChatEvent event) {
        if(discApi != null && discApi.getTextChannelById("811643634562367498").isPresent()) {
            TextChannel channel = discApi.getTextChannelById("811643634562367498").get();
            channel.sendMessage("(**" + event.getAuthorParty().getName() + "**) "
                    + Objects.requireNonNull(Bukkit.getPlayer(event.getPartyChatMessage().getAuthor().uuid())).getName() + " » " + event.getRawMessage());
        }
    }
}
