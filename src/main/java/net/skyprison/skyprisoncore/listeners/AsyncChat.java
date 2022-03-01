package net.skyprison.skyprisoncore.listeners;

import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Objects;
import java.util.regex.Matcher;

public class AsyncChat implements Listener {
    private SkyPrisonCore plugin;

    public AsyncChat(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.stickyChat.containsKey(player.getUniqueId())) {
            File lang = new File(plugin.getDataFolder() + File.separator
                    + "lang" + File.separator + plugin.getConfig().getString("lang-file"));
            FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);
            event.setCancelled(true);
            String stickiedChat = plugin.stickyChat.get(player.getUniqueId());
            String[] split = stickiedChat.split("-");

            String message = event.message().toString();
            String format = Objects.requireNonNull(langConf.getString("chat." + split[0] + ".format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
            message = format.replaceAll("\\[message]", Matcher.quoteReplacement(message));
            for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                if (online.hasPermission("skyprisoncore.command." + split[0])) {
                    online.sendMessage(plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));
                }
            }

            Bukkit.getConsoleSender().sendMessage(plugin.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', message)));

            String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
            String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(event.message().toString()));
            TextChannel channel = github.scarsz.discordsrv.DiscordSRV.getPlugin().getDestinationTextChannelForGameChannelName(split[0] + "-chat");
            channel.sendMessage(dMessage).queue();
        }
    }
}
