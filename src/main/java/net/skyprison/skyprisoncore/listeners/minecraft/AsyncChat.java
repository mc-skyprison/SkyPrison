package net.skyprison.skyprisoncore.listeners.minecraft;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegistryFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.Claim;
import net.skyprison.skyprisoncore.commands.ItemLore;
import net.skyprison.skyprisoncore.commands.Tags;
import net.skyprison.skyprisoncore.inventories.ClaimFlags;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.claims.AvailableFlags;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsyncChat implements Listener {
    private final SkyPrisonCore plugin;
    private final DiscordApi discApi;
    private final DatabaseHook db;
    private final Tags tag;
    private final ItemLore itemLore;

    public AsyncChat(SkyPrisonCore plugin, DiscordApi discApi, DatabaseHook db, Tags tag, ItemLore itemLore) {
        this.plugin = plugin;
        this.discApi = discApi;
        this.db = db;
        this.tag = tag;
        this.itemLore = itemLore;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String msg = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
        String miniMsg = MiniMessage.miniMessage().serialize(event.originalMessage());
        if(plugin.chatLock.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                boolean removeChatLock = true;
                List<Object> chatLock = plugin.chatLock.get(player.getUniqueId());
                Object lockType = chatLock.get(0);
                if(lockType instanceof AvailableFlags flag) {
                    // flag, claimId, world, canEdit, category, page
                    RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld((String) chatLock.get(2)))));
                    if(regionManager != null) {
                        ProtectedRegion region = regionManager.getRegion((String) chatLock.get(1));
                        Component prefix = new Claim(plugin, db).prefix;
                        if(region != null) {
                            Flag<?> wgFlag = flag.getFlags().get(0);
                            if (wgFlag instanceof StringFlag stringFlag) {
                                if (stringFlag.equals(Flags.GREET_MESSAGE)) {
                                    region.setFlag(Flags.GREET_MESSAGE, msg);
                                } else if (stringFlag.equals(Flags.GREET_TITLE)) {
                                    region.setFlag(Flags.GREET_TITLE, msg);
                                } else if (stringFlag.equals(Flags.FAREWELL_MESSAGE)) {
                                    region.setFlag(Flags.FAREWELL_MESSAGE, msg);
                                } else if (stringFlag.equals(Flags.FAREWELL_TITLE)) {
                                    region.setFlag(Flags.FAREWELL_TITLE, msg);
                                } else if (stringFlag.equals(Flags.TIME_LOCK)) {
                                    String regex = "([01]?[0-9]|2[0-3]):[0-5][0-9]";
                                    Pattern p = Pattern.compile(regex);
                                    if(p.matcher(msg).matches()) {
                                        region.setFlag(Flags.TIME_LOCK, String.valueOf(plugin.timeToTicks(msg)));
                                    } else {
                                        if(!msg.equalsIgnoreCase("cancel")) {
                                            player.sendMessage(prefix.append(Component.text("Incorrect Time! Time must be specified in 24:00 format. Type 'cancel' to cancel.")
                                                    .color(NamedTextColor.RED)));
                                            removeChatLock = false;
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("Cancelling..").color(NamedTextColor.GRAY)));
                                        }
                                    }
                                }
                            } else if (wgFlag instanceof RegistryFlag<?> registryFlag) {
                                if (registryFlag.equals(Flags.WEATHER_LOCK)) {
                                    if(WeatherTypes.get(msg) != null) {
                                        region.setFlag(Flags.WEATHER_LOCK, WeatherTypes.get(msg));
                                    } else {
                                        if(!msg.equalsIgnoreCase("cancel")) {
                                            player.sendMessage(prefix.append(
                                                    Component.text("Incorrect Weather Type! Available types are 'Clear', 'Rain', 'Thunder'. Type 'cancel' to cancel.")
                                                            .color(NamedTextColor.RED)));
                                            removeChatLock = false;
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("Cancelling..").color(NamedTextColor.GRAY)));
                                        }
                                    }
                                }
                            }
                            if(removeChatLock) {
                                ClaimFlags claimFlags = new ClaimFlags(plugin, (String) chatLock.get(1), (String) chatLock.get(2), (boolean) chatLock.get(3), (String) chatLock.get(4), (int) chatLock.get(5));
                                player.openInventory(claimFlags.getInventory());
                            }
                        }
                    }
                } else if(lockType instanceof String lockedString) {
                    switch (lockedString.toLowerCase()) {
                        case "tags-display" -> { // ID
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE tags SET tags_display = ? WHERE tags_id = ?")) {
                                ps.setString(1, miniMsg);
                                ps.setInt(2, (int) chatLock.get(1));
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(Component.text("Updated tag display!", NamedTextColor.GREEN));
                            tag.openSpecificGUI(player, (Integer) chatLock.get(1));
                        }
                        case "tags-lore" -> { // ID
                            String lore = miniMsg;
                            if (msg.equalsIgnoreCase("null")) {
                                lore = null;
                            }

                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE tags SET tags_lore = ? WHERE tags_id = ?")) {
                                ps.setString(1, lore);
                                ps.setInt(2, (int) chatLock.get(1));
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(Component.text("Updated tag lore!", NamedTextColor.GREEN));
                            tag.openSpecificGUI(player, (Integer) chatLock.get(1));
                        }
                        case "tags-effect" -> { // ID
                            String effect = msg;
                            if (effect.equalsIgnoreCase("null")) {
                                effect = null;
                            }
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE tags SET tags_effect = ? WHERE tags_id = ?")) {
                                ps.setString(1, effect);
                                ps.setInt(2, (int) chatLock.get(1));
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            player.sendMessage(Component.text("Updated tag effect!", NamedTextColor.GREEN));
                            tag.openSpecificGUI(player, (Integer) chatLock.get(1));
                        }
                        case "tags-new-display" -> { // DISPLAY, LORE, EFFECT
                            player.sendMessage(Component.text("Set the tag display!", NamedTextColor.GREEN));
                            tag.openNewGUI(player, miniMsg, (String) chatLock.get(2), (String) chatLock.get(3));
                        }
                        case "tags-new-lore" -> { // DISPLAY, LORE, EFFECT
                            player.sendMessage(Component.text("Set the tag lore!", NamedTextColor.GREEN));
                            tag.openNewGUI(player, (String) chatLock.get(1), miniMsg, (String) chatLock.get(3));
                        }
                        case "tags-new-effect" -> { // DISPLAY, LORE, EFFECT
                            player.sendMessage(Component.text("Set the tag effect!", NamedTextColor.GREEN));
                            tag.openNewGUI(player, (String) chatLock.get(1), (String) chatLock.get(2), msg);
                        }
                        case "new-lore" -> {
                            if(!msg.equalsIgnoreCase("cancel")) {
                                ItemStack heldItem = (ItemStack) chatLock.get(1);
                                ItemStack currHeldItem = player.getInventory().getItemInMainHand();
                                List<Component> lore = new ArrayList<>();
                                if(player.getInventory().getItemInMainHand().hasLore()) {
                                    lore = player.getInventory().getItemInMainHand().lore();
                                }

                                assert lore != null;
                                lore.add(MiniMessage.miniMessage().deserialize(miniMsg));

                                if (heldItem.equals(currHeldItem)) {
                                    player.getInventory().getItemInMainHand().lore(lore);
                                    itemLore.displayLore(player);
                                } else {
                                    player.sendMessage(Component.text("Item in hand has changed! Cancelling..", NamedTextColor.RED));
                                }
                            } else {
                                itemLore.displayLore(player);
                            }
                        }
                        case "edit-lore" -> {
                            if(!msg.equalsIgnoreCase("cancel")) {
                                ItemStack heldItem = (ItemStack) chatLock.get(1);
                                ItemStack currHeldItem = player.getInventory().getItemInMainHand();
                                int line = (int) chatLock.get(2);
                                if(heldItem.equals(currHeldItem)) {
                                    List<Component> lore = player.getInventory().getItemInMainHand().lore();
                                    assert lore != null;
                                    lore.set(line - 1, MiniMessage.miniMessage().deserialize(miniMsg));
                                    player.getInventory().getItemInMainHand().lore(lore);
                                    itemLore.displayLore(player);
                                } else {
                                    player.sendMessage(Component.text("Item in hand has changed! Cancelling..", NamedTextColor.RED));
                                }
                            } else {
                                itemLore.displayLore(player);
                            }
                        }
                    }
                }
                if(removeChatLock) plugin.chatLock.remove(player.getUniqueId());
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
                String msgContent = format.replaceAll("\\[message]", Matcher.quoteReplacement(msg));
                Component formatMsg = player.isOp() ? MiniMessage.miniMessage().deserialize(msgContent) : plugin.playerMsgBuilder.deserialize(msgContent);

                for (Player online : Bukkit.getServer().getOnlinePlayers()) {
                    if (online.hasPermission("skyprisoncore.command." + split[0])) {
                        online.sendMessage(formatMsg);
                    }
                }

                Bukkit.getConsoleSender().sendMessage(plugin.playerMsgBuilder.deserialize(msgContent));
                if(discApi != null) {
                    String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
                    String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(msg));
                    if(discApi.getTextChannelById(split[1]).isPresent()) {
                        TextChannel channel = discApi.getTextChannelById(split[1]).get();
                        channel.sendMessage(dMessage);
                    }
                }
            } else {
                if(discApi != null) {
                    String dFormat = Objects.requireNonNull(langConf.getString("chat.discordSRV.format")).replaceAll("\\[name]", Matcher.quoteReplacement(player.getName()));
                    String dMessage = dFormat.replaceAll("\\[message]", Matcher.quoteReplacement(msg));
                    if (discApi.getTextChannelById("788108242797854751").isPresent()) {
                        TextChannel channel = discApi.getTextChannelById("788108242797854751").get();
                        channel.sendMessage(dMessage);
                    }
                }
                event.renderer((source, sourceDisplayName, message, viewer) -> {
                    LuckPerms luckAPI = LuckPermsProvider.get();
                    User user = luckAPI.getPlayerAdapter(Player.class).getUser(source);
                    Component prefix = Component.empty();
                    if(user.getCachedData().getMetaData().getPrefix() != null) {
                        prefix = MiniMessage.miniMessage().deserialize(Objects.requireNonNull(user.getCachedData().getMetaData().getPrefix())).appendSpace();
                    }
                    Component separator = Component.text(" » ", NamedTextColor.DARK_GRAY);

                    Component userTag = MiniMessage.miniMessage().deserialize(plugin.userTags.getOrDefault(player.getUniqueId(), ""));

                    if(userTag.equals(Component.text(""))) separator = Component.text("» ", NamedTextColor.DARK_GRAY);

                    return Component.empty().append(prefix).append(Objects.requireNonNullElse(source.customName(), source.displayName())).appendSpace().append(userTag).append(separator).append(message);
                });
            }
        }
    }
}
