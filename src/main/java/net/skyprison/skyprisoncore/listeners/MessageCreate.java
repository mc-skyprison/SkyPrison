package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.gmail.nossr50.datatypes.party.Party;
import com.gmail.nossr50.party.PartyManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageCreate implements MessageCreateListener {

    private final SkyPrisonCore plugin;
    private final ChatUtils chatUtils;
    private final DiscordApi discApi;
    private final DatabaseHook db;

    public MessageCreate(SkyPrisonCore plugin, ChatUtils chatUtils, DiscordApi discApi, DatabaseHook db) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.discApi = discApi;
        this.db = db;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if(!event.getMessageAuthor().isYourself()) {
            if (event.getChannel().getType() == ChannelType.PRIVATE_CHANNEL) {
                int code = Integer.parseInt(event.getMessageContent());
                if (plugin.discordLinking.containsKey(code)) {
                    MessageAuthor author = event.getMessageAuthor();
                    long userId = author.getId();
                    OfflinePlayer player = Bukkit.getOfflinePlayer(plugin.discordLinking.get(code));

                    String sql = "UPDATE users SET discord_id = ? WHERE user_id = ?";
                    List<Object> params = new ArrayList<Object>() {{
                        add(userId);
                        add(player.getUniqueId().toString());
                    }};

                    if(db.sqlUpdate(sql, params)) {
                        event.getChannel().sendMessage("Your account has successfully been linked with " + player.getName() + ", and you have received 500 tokens.");
                        plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), 500);

                        Server server = discApi.getServerById("782795465632251955").get();
                        author.asUser().get().updateNickname(server, player.getName());
                    } else {
                        event.getChannel().sendMessage("Uh oh, something went wrong! Contact an admin.");
                    }
                    plugin.discordLinking.remove(code);
                }
            } else {
                TextChannel channel = event.getChannel();

                String channelId = String.valueOf(channel.getId());

                if (event.getMessageAuthor().asUser().isPresent()) {
                    User user = event.getMessageAuthor().asUser().get();
                    if (discApi.getServerById("782795465632251955").isPresent()) {
                        Server server = discApi.getServerById("782795465632251955").get();
                        String userName = user.getDisplayName(server);

                        String message = event.getMessageContent();

                        switch (channelId) {
                            case "788108242797854751": // global
                                int r = server.getHighestRole(user).get().getColor().get().getRed();
                                int g = server.getHighestRole(user).get().getColor().get().getGreen();
                                int b = server.getHighestRole(user).get().getColor().get().getBlue();

                                String hexColor = String.format("#%02x%02x%02x", r, g, b);

                                String newMessage = "&f[&bDiscord &f| {" + hexColor + "}" + server.getHighestRole(user).get().getName() + "&f] &r" + userName + " &7» &6" + message;
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.sendMessage(plugin.colourMessage(newMessage));
                                }
                                break;
                            case "791054229136605194": // admin
                                chatUtils.discordChatSend(message, userName, "admin", "791054229136605194");
                                event.getMessage().delete();
                                break;
                            case "791054021338464266": // guard
                                chatUtils.discordChatSend(message, userName, "guard", "791054021338464266");
                                event.getMessage().delete();
                                break;
                            case "791054076787163166": // staff
                                chatUtils.discordChatSend(message, userName, "staff", "791054076787163166");
                                event.getMessage().delete();
                                break;
                            case "800885673732997121": // build
                                chatUtils.discordChatSend(message, userName, "build", "800885673732997121");
                                event.getMessage().delete();
                                break;
                            case "811643634562367498": // party
                                String[] splitMsg = message.split(" ", 2);
                                String nMessage = "&a(P) &f" + userName + " &a→ &f" + splitMsg[1];
                                Party p;
                                if (Bukkit.getPlayer(splitMsg[0]) != null) {
                                    p = PartyManager.getParty(Bukkit.getPlayer(splitMsg[0]));
                                } else {
                                    p = PartyManager.getParty(splitMsg[0]);
                                }

                                if (p != null) {
                                    List pMembers = p.getOnlineMembers();
                                    for (Object online : pMembers) {
                                        Player oPlayer = (Player) online;
                                        oPlayer.sendMessage(plugin.colourMessage(nMessage));
                                    }
                                    plugin.tellConsole(plugin.colourMessage(nMessage));
                                    String dMessage = "(**" + p.getName() + "**) " + userName + " » " + splitMsg[1];
                                    channel.sendMessage(dMessage);
                                }
                                event.getMessage().delete();
                                break;
                        }
                    }
                }
            }
        }
    }
}
