package net.skyprison.skyprisoncore.listeners;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.Tags;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

public class AsyncPlayerChat implements Listener {
    private final SkyPrisonCore plugin;
    private final DiscordApi discApi;
    private final DatabaseHook hook;
    private final Tags tag;

    public AsyncPlayerChat(SkyPrisonCore plugin, DiscordApi discApi, DatabaseHook hook, Tags tag) {
        this.plugin = plugin;
        this.discApi = discApi;
        this.hook = hook;
        this.tag = tag;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(plugin.chatLock.containsKey(player.getUniqueId())) {
            String finalMessage1 = message;
            Bukkit.getScheduler().runTask(plugin, () -> {
                List tags = plugin.chatLock.get(player.getUniqueId());
                event.setCancelled(true);
                String lockType = (String) tags.get(0);
                String sql;
                List<Object> params;
                String finalMessage = finalMessage1;
                switch(lockType.toLowerCase()) {
                    case "tags-display":
                        sql = "UPDATE tags SET tags_display = ? WHERE tags_id = ?";
                        params = new ArrayList<Object>() {{
                            add(finalMessage);
                            add(Integer.parseInt("" + tags.get(1)));
                        }};
                        hook.sqlUpdate(sql, params);
                        player.sendMessage(plugin.colourMessage("&aUpdated tag display!"));
                        tag.openSpecificGUI(player, Integer.parseInt((String) tags.get(1)));
                        break;
                    case "tags-lore":
                        sql = "UPDATE tags SET tags_lore = ? WHERE tags_id = ?";
                        params = new ArrayList<Object>() {{
                            add(finalMessage);
                            add(Integer.parseInt("" + tags.get(1)));
                        }};
                        hook.sqlUpdate(sql, params);
                        player.sendMessage(plugin.colourMessage("&aUpdated tag lore!"));
                        tag.openSpecificGUI(player, Integer.parseInt((String) tags.get(1)));
                        break;
                    case "tags-effect":
                        sql = "UPDATE tags SET tags_effect = ? WHERE tags_id = ?";
                        params = new ArrayList<Object>() {{
                            add(finalMessage);
                            add(Integer.parseInt("" + tags.get(1)));
                        }};
                        hook.sqlUpdate(sql, params);
                        player.sendMessage(plugin.colourMessage("&aUpdated tag effect!"));
                        tag.openSpecificGUI(player, Integer.parseInt("" + tags.get(1)));
                        break;
                    case "tags-new-display":
                        player.sendMessage(plugin.colourMessage("&aSet the tag display!"));
                        tag.openNewGUI(player, finalMessage1, (String) tags.get(2), (String) tags.get(3));
                        break;
                    case "tags-new-lore":
                        player.sendMessage(plugin.colourMessage("&aSet the tag lore!"));
                        tag.openNewGUI(player, (String) tags.get(1), finalMessage1, (String) tags.get(3));
                        break;
                    case "tags-new-effect":
                        player.sendMessage(plugin.colourMessage("&aSet the tag effect!"));
                        tag.openNewGUI(player, (String) tags.get(1), (String) tags.get(2), finalMessage1);
                        break;
                }
                plugin.chatLock.remove(player.getUniqueId());
            });
        }

        if(!event.isCancelled()) {
            File lang = new File(plugin.getDataFolder() + File.separator
                    + "lang" + File.separator + plugin.getConfig().getString("lang-file"));
            FileConfiguration langConf = YamlConfiguration.loadConfiguration(lang);

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
