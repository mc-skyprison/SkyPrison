package net.skyprison.skyprisoncore.listeners.discord;

import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.party.PartyManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.List;

public class MessageCreate implements MessageCreateListener {

    private final SkyPrisonCore plugin;
    private final ChatUtils chatUtils;
    private final DiscordApi discApi;
    public MessageCreate(SkyPrisonCore plugin, ChatUtils chatUtils, DiscordApi discApi) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.discApi = discApi;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(!event.getMessageAuthor().isYourself()) {
            TextChannel channel = event.getChannel();

            String channelId = String.valueOf(channel.getId());

            if (event.getMessageAuthor().asUser().isPresent()) {
                User user = event.getMessageAuthor().asUser().get();
                if (discApi.getServerById("782795465632251955").isPresent()) {
                    Server server = discApi.getServerById("782795465632251955").get();
                    String userName = user.getDisplayName(server);

                    String message = event.getMessageContent();

                    switch (channelId) {
                        case "788108242797854751" -> { // global
                            if(server.getHighestRole(user).isPresent() && server.getHighestRole(user).get().getColor().isPresent()) {
                                int r = server.getHighestRole(user).get().getColor().get().getRed();
                                int g = server.getHighestRole(user).get().getColor().get().getGreen();
                                int b = server.getHighestRole(user).get().getColor().get().getBlue();
                                String hexColor = String.format("#%02x%02x%02x", r, g, b);
                                Component newMessage = Component.text("[", NamedTextColor.WHITE).append(Component.text("Discord", NamedTextColor.AQUA))
                                        .append(Component.text(" |", NamedTextColor.WHITE)).append(Component.text(server.getHighestRole(user).get().getName(), TextColor.fromHexString(hexColor)))
                                                .append(Component.text("] ", NamedTextColor.WHITE)).append(Component.text(userName, NamedTextColor.GRAY))
                                                        .append(Component.text(" » ", NamedTextColor.GRAY)).append(Component.text(message, NamedTextColor.GOLD));
                                plugin.getServer().sendMessage(newMessage);
                            }
                        }
                        case "791054229136605194" -> { // admin
                            chatUtils.discordChatSend(message, userName, "admin", "791054229136605194");
                            event.getMessage().delete();
                        }
                        case "791054021338464266" -> { // guard
                            chatUtils.discordChatSend(message, userName, "guard", "791054021338464266");
                            event.getMessage().delete();
                        }
                        case "791054076787163166" -> { // staff
                            chatUtils.discordChatSend(message, userName, "staff", "791054076787163166");
                            event.getMessage().delete();
                        }
                        case "800885673732997121" -> { // build
                            chatUtils.discordChatSend(message, userName, "build", "800885673732997121");
                            event.getMessage().delete();
                        }
                        case "811643634562367498" -> { // party
                            String[] splitMsg = message.split(" ", 2);
                            Component nMessage = Component.text("(P) ", NamedTextColor.GREEN).append(Component.text(userName, NamedTextColor.WHITE))
                                    .append(Component.text(" → ", NamedTextColor.GREEN)).append(Component.text(splitMsg[1], NamedTextColor.WHITE));
                            Party p;
                            if (Bukkit.getPlayer(splitMsg[0]) != null) {
                                p = PartyManager.getParty(Bukkit.getPlayer(splitMsg[0]));
                            } else {
                                p = PartyManager.getParty(splitMsg[0]);
                            }
                            if (p != null) {
                                List<Player> pMembers = p.getOnlineMembers();
                                Audience receivers = Audience.audience(pMembers.stream().collect(Audience.toAudience()), plugin.getServer().getConsoleSender());
                                receivers.sendMessage(nMessage);
                                String dMessage = "(**" + p.getName() + "**) " + userName + " » " + splitMsg[1];
                                channel.sendMessage(dMessage);
                            }
                            event.getMessage().delete();
                        }
                    }
                }
            }
        }
    }
}
