package net.skyprison.skyprisoncore.listeners.minecraft;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.DefaultStyles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.JailCommands;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

import static net.skyprison.skyprisoncore.utils.PlayerManager.*;

public class PlayerJoin implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final DiscordApi discApi;
    private final DailyMissions dailyMissions;
    private final PlayerParticlesAPI particles;
    public PlayerJoin(SkyPrisonCore plugin, DatabaseHook db, DiscordApi discApi, DailyMissions dailyMissions, PlayerParticlesAPI particles) {
        this.plugin = plugin;
        this.db = db;
        this.discApi = discApi;
        this.dailyMissions = dailyMissions;
        this.particles = particles;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AttributeInstance attackSpeed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if(attackSpeed != null && attackSpeed.getValue() != attackSpeed.getDefaultValue()) {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(attackSpeed.getDefaultValue());
        }

        Location loc = player.getLocation();
        World world = player.getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
        if(ClaimUtils.customClaimShape.contains(player.getUniqueId())) {
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
            RegionSelector newSelector = new CuboidRegionSelector(session.getRegionSelector(weWorld));
            session.setDefaultRegionSelector(RegionSelectorType.CUBOID);
            session.setRegionSelector(weWorld, newSelector);
        }
        if(world.getName().equals("world_prison")) {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(weWorld);
            assert regions != null;
            ApplicableRegionSet regionList = regions.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
            for(ProtectedRegion region : regionList.getRegions()) {
                if(region.getId().contains("mine")) {
                    if(loc.getBlock().isSolid() || loc.clone().offset(0, 1, 0).toLocation(world).getBlock().isSolid()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp " + region.getId() + " " + player.getName());
                    }
                    break;
                } else if(region.getId().equalsIgnoreCase("guard-secretview")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp prison " + player.getName());
                }
            }
        }

        List<String> ids = new ArrayList<>();
        boolean hasSchedules = false;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT type, content FROM schedule_online WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                hasSchedules = true;
                String type = rs.getString(1);
                if(type.equalsIgnoreCase("namecolour")) {
                    Component content = null;
                    if(!rs.getString(2).equalsIgnoreCase("remove")) {
                        content = GsonComponentSerializer.gson().deserialize(rs.getString(2));
                    }
                    player.customName(content);
                } else if(type.equalsIgnoreCase("purchase-total-check")) {
                    checkTotalPurchases(player, Double.parseDouble(rs.getString(2)));
                } else if(type.equalsIgnoreCase("mail-item")) {
                    ItemStack item = ItemStack.deserializeBytes(Base64.getDecoder().decode(rs.getString(2)));
                    HashMap<Integer, ItemStack> didntFit = player.getInventory().addItem(item);
                    for (ItemStack dropItem : didntFit.values()) {
                        world.dropItemNaturally(player.getLocation(), dropItem).setOwner(player.getUniqueId());
                    }
                } else if(type.equalsIgnoreCase("mail-offhand")) {
                    ItemStack item = ItemStack.deserializeBytes(Base64.getDecoder().decode(rs.getString(2)));
                    player.getInventory().setItemInOffHand(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(hasSchedules) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM schedule_online WHERE user_id = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Component messages = Component.text("");
        messages = messages.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Messages ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        messages = messages.append(Component.text("\nYou've received some messages while you were offline!", NamedTextColor.GRAY));

        int i = 1;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, message FROM notifications WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                messages = messages.append(Component.newline().appendNewline().append(Component.text(i + ".", NamedTextColor.GRAY, TextDecoration.BOLD))
                        .appendNewline().append(GsonComponentSerializer.gson().deserialize(rs.getString(2))));
                i++;
                if(!rs.getString(1).equalsIgnoreCase("claim-invite") && !rs.getString(2).equalsIgnoreCase("claim-transfer")) {
                    ids.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(!ids.isEmpty()) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE delete_on_view = ? AND id IN " + SkyPrisonCore.getQuestionMarks(ids))) {
                ps.setInt(1, 1);
                for (int b = 0; b < ids.size(); b++) {
                    ps.setString(b + 2, ids.get(b));
                }
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            messages = messages.appendNewline();
            player.sendMessage(messages);
        }

        List<String> mails = MailUtils.getBoxesWithMail(player.getUniqueId());
        if(!mails.isEmpty()) {
            for(String mail : mails) {
                boolean postOffice = mail.equalsIgnoreCase("post office");
                messages = messages.append(Component.newline().appendNewline().append(Component.text(i + ".", NamedTextColor.GRAY, TextDecoration.BOLD))
                        .appendNewline().append(Component.text(postOffice ? "Someone has sent you mail! Collect it at the Post Office in SkyCity."
                                        : "Someone has sent mail to the mailbox ", NamedTextColor.GREEN)).append(Component.text(postOffice ? "" : mail, NamedTextColor.GREEN, TextDecoration.BOLD)));
                i++;
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            EmbedBuilder embedJoin;
            plugin.blockBreaks.put(player.getUniqueId(), 0);
            TokenUtils.getTokensData().put(player.getUniqueId(), 0);
            if(!player.hasPlayedBefore()) {
                embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " joined the server for the first time!", "",  "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.YELLOW);
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO users (user_id, current_name, first_join) VALUES (?, ?, ?)")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ps.setString(2, player.getName());
                    ps.setLong(3, player.getFirstPlayed());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                embedJoin = new EmbedBuilder()
                        .setAuthor(player.getName() + " joined the server", "",  "https://minotar.net/helm/" + player.getName())
                        .setColor(Color.GREEN);
                String currentName = "";
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_name FROM users WHERE user_id = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    if(rs.next()) {
                        currentName = rs.getString(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if(currentName.isEmpty()) {
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO users (user_id, current_name, first_join) VALUES (?, ?, ?)")) {
                        ps.setString(1, player.getUniqueId().toString());
                        ps.setString(2, player.getName());
                        ps.setLong(3, player.getFirstPlayed());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else if(!player.getName().equalsIgnoreCase(currentName)) {
                    embedJoin.setDescription("They have changed their name! They were previously named " + currentName);
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET current_name = ? WHERE user_id = ?")) {
                        ps.setString(1, player.getName());
                        ps.setString(2, player.getUniqueId().toString());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(discApi != null && discApi.getTextChannelById("788108242797854751").isPresent())
                discApi.getTextChannelById("788108242797854751").get().sendMessage(embedJoin);


            if(dailyMissions.getMissions(player).isEmpty()) {
                dailyMissions.setPlayerMissions(player);
            }

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

            com.sk89q.worldedit.util.Location locWE = BukkitAdapter.adapt(player.getLocation());
            LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
            RegionQuery query = container.createQuery();
            if(!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR)) {
                player.setAllowFlight(query.testState(locWE, localPlayer, ClaimUtils.FLY));
            }

            int tag_id = 0;
            String logoutWorld = "";
            try(Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT blocks_mined, tokens, active_tag, logout_world FROM users WHERE user_id = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    plugin.blockBreaks.put(player.getUniqueId(), rs.getInt(1));
                    TokenUtils.getTokensData().put(player.getUniqueId(), rs.getInt(2));
                    tag_id = rs.getInt(3);
                    logoutWorld = rs.getString(4);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if(logoutWorld != null && !logoutWorld.isEmpty() && !player.hasPermission("skyprisoncore.invchange.bypass")) {
                if(!logoutWorld.equals(world.getName())) {
                    boolean fromPrison = isPrisonWorld(logoutWorld);
                    boolean toPrison = isPrisonWorld(world.getName());
                    changeInventory(player, fromPrison, toPrison);
                }
            }


            long jailTime = 0;
            String jailReason = "";
            try(Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT reason, length_total, length_served FROM logs_jail WHERE target_id = ? AND active = 1")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    jailReason = rs.getString(1);
                    jailTime = rs.getLong(2) - rs.getLong(3);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            if(jailTime != 0) {
                JailCommands.setJail(Bukkit.getConsoleSender(), player, jailReason, jailTime, false);
            }

            if(PlayerManager.getPlayerTag(player.getUniqueId()) == null && tag_id != 0) {
                Tags.Tag tagObject = Tags.getTag(tag_id);
                if(tagObject == null) return;
                PlayerManager.addPlayerTags(new PlayerTag(player.getUniqueId(), tagObject));
                particles.resetActivePlayerParticles(player);
                if(tagObject.effectType() != null && tagObject.effectStyle() != null)
                    particles.addActivePlayerParticle(player, ParticleEffect.fromInternalName(tagObject.effectType()),
                            Tags.effectStyles().stream().filter(style -> style.getName().equalsIgnoreCase(tagObject.effectStyle())).findFirst().orElse(DefaultStyles.NORMAL));
            }
        });
    }
}
