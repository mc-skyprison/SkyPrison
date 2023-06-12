package net.skyprison.skyprisoncore.listeners.discord;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SlashCommandCreate implements SlashCommandCreateListener {

    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public SlashCommandCreate(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction command = event.getSlashCommandInteraction();
        if(command.getCommandName().equalsIgnoreCase("link")) {
            if(command.getOptionByIndex(0).isPresent()) {
                int code = Integer.parseInt(command.getOptionByIndex(0).get().getStringValue().get());
                if (plugin.discordLinking.containsKey(code)) {
                    plugin.tellConsole("wham4");
                    User author = command.getUser();
                    long userId = author.getId();
                    CMIUser player = CMI.getInstance().getPlayerManager().getUser(plugin.discordLinking.get(code));
                    boolean gottenReward = false;
                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
                        ps.setString(1, player.getUniqueId().toString());
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            gottenReward = true;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET discord_id = ? WHERE user_id = ?")) {
                        plugin.tellConsole("wham5");
                        ps.setLong(1, userId);
                        ps.setString(2, player.getUniqueId().toString());
                        ps.setString(3, "owner");
                        ps.executeUpdate();

                        String msg = "Successfully linked your Discord account with the minecraft Account " + player.getName() + "!";

                        if (!gottenReward) {
                            msg += "\nYou've also received 500 tokens for having linked your discord!";
                            plugin.tokens.addTokens(player, 500, "Linking Discord", "");
                        }

                        command.createImmediateResponder()
                                .setContent(msg)
                                .setFlags(MessageFlag.EPHEMERAL)
                                .respond();
                        if (player.isOnline()) {
                            player.sendMessage(plugin.colourMessage("&aYour discord account was successfully linked!"));
                        }
                        if (command.getServer().isPresent()) {
                            author.updateNickname(command.getServer().get(), player.getName());
                        }
                    } catch (SQLException e) {
                        command.createImmediateResponder()
                                .setContent("Something went wrong while linking your account! Contact an admin.")
                                .setFlags(MessageFlag.EPHEMERAL)
                                .respond();
                        e.printStackTrace();
                    }
                    plugin.discordLinking.remove(code);
                } else {
                    plugin.tellConsole("whamFAIL");
                    command.createImmediateResponder()
                            .setContent("You don't have an active discord linking attempt!")
                            .setFlags(MessageFlag.EPHEMERAL)
                            .respond();
                }
            }
        }
    }
}
