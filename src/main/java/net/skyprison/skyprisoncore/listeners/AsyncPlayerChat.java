package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.io.File;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;

public class AsyncPlayerChat implements Listener {
    private final SkyPrisonCore plugin;
    private DiscordApi discApi;

    public AsyncPlayerChat(SkyPrisonCore plugin, DiscordApi discApi) {
        this.plugin = plugin;
        this.discApi = discApi;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if(!event.isCancelled()) {
            File lang = new File(plugin.getDataFolder() + File.separator
                    + "lang" + File.separator + plugin.getConfig().getString("lang-file"));
            FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

            Player player = event.getPlayer();
            String message = event.getMessage();

            if (plugin.stickyChat.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
                String stickiedChat = plugin.stickyChat.get(player.getUniqueId());
                String[] split = stickiedChat.split("-");

                String format = Objects.requireNonNull(langConf.getString("chat." + split[0] + ".format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
                message = format.replaceAll("\\[message]", Matcher.quoteReplacement(message));
                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("skyprisoncore.command." + split[0])) {
                        online.sendMessage(plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));
                    }
                }
                Bukkit.getConsoleSender().sendMessage(plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));

                String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
                String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(event.getMessage()));
                TextChannel channel = discApi.getTextChannelById(split[1]).get();
                channel.sendMessage(dMessage);
            } else {
                String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
                String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(message));
                Collection<TextChannel> channels = discApi.getTextChannelsByName("global-chat");
                channels.iterator().next().sendMessage(dMessage);
            }
        }
    }
}
