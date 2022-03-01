package net.skyprison.skyprisoncore.listeners;

import com.gmail.nossr50.events.chat.McMMOPartyChatEvent;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

public class McMMOPartyChat implements Listener {
    @EventHandler
    public void onMcMMOPartyChat(McMMOPartyChatEvent event) {
        TextChannel channel = DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName("party-chats");
        channel.sendMessage("(**" + event.getAuthorParty().getName() + "**) "
                + Objects.requireNonNull(Bukkit.getPlayer(event.getPartyChatMessage().getAuthor().uuid())).getName() + " » " + event.getRawMessage()).queue();
    }
}
