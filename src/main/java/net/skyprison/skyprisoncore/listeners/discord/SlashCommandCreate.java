package net.skyprison.skyprisoncore.listeners.discord;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.TokenUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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
                if(command.getOptionByIndex(0).get().getStringValue().isPresent()) {
                    int code = Integer.parseInt(command.getOptionByIndex(0).get().getStringValue().get());
                    if (plugin.discordLinking.containsKey(code)) {
                        User author = command.getUser();
                        long userId = author.getId();
                        UUID pUUID = plugin.discordLinking.get(code);
                        String playerName = "";
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_name FROM users WHERE user_id = ?")) {
                            ps.setString(1, pUUID.toString());
                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                playerName = rs.getString(1);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }


                        OfflinePlayer player = Bukkit.getPlayer(pUUID);
                        boolean gottenReward = false;
                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
                            ps.setString(1, pUUID.toString());
                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                gottenReward = true;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET discord_id = ? WHERE user_id = ?")) {
                            ps.setLong(1, userId);
                            ps.setString(2, pUUID.toString());
                            ps.setString(3, "owner");
                            ps.executeUpdate();

                            String msg = "Successfully linked your Discord account with the minecraft Account " + playerName + "!";

                            if (!gottenReward) {
                                msg += "\nYou've also received 500 tokens for having linked your discord!";
                                TokenUtils.addTokens(pUUID, 500, "Linking Discord", "");
                            }

                            command.createImmediateResponder()
                                    .setContent(msg)
                                    .setFlags(MessageFlag.EPHEMERAL)
                                    .respond();
                            if(player != null && player.getPlayer() != null) {
                                player.getPlayer().sendMessage(Component.text("Your discord account was successfully linked!", NamedTextColor.GREEN));
                            }
                            if (command.getServer().isPresent()) {
                                author.updateNickname(command.getServer().get(), playerName);
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
                        command.createImmediateResponder()
                                .setContent("You don't have an active discord linking attempt!")
                                .setFlags(MessageFlag.EPHEMERAL)
                                .respond();
                    }
                }
            }
        }
    }
}
