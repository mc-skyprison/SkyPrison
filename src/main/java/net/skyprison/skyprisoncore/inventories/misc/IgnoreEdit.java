package net.skyprison.skyprisoncore.inventories.misc;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IgnoreEdit implements CustomInventory {
    private final Inventory inventory;
    private final DatabaseHook db;
    private final Player player;
    private final String targetName;
    private PlayerManager.Ignore ignore;
    private boolean ignorePrivate;
    private boolean ignoreTeleport;
    private boolean deleteLock = false;
    private final ItemStack privateItem = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
    private final Component ignoring = Component.text("IGNORING", NamedTextColor.RED, TextDecoration.BOLD);
    private final Component notIgnoring = Component.text("ALLOWING", NamedTextColor.GREEN, TextDecoration.BOLD);
    public IgnoreEdit(Player player, PlayerManager.Ignore ignore, DatabaseHook db) {
        ignorePrivate = ignore.ignorePrivate();
        ignoreTeleport = ignore.ignoreTeleport();

        this.inventory = Bukkit.getServer().createInventory(this, 36, Component.text("Ignore Options", TextColor.fromHexString("#0fc3ff")));
        this.player = player;
        this.ignore = ignore;
        this.db = db;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ignore.targetId());
        final String name = offlinePlayer.getName();
        targetName = Objects.requireNonNullElse(name, Objects.requireNonNullElse(PlayerManager.getPlayerName(ignore.targetId()), "Name Not Found.."));
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        for(int i = 0; i < inventory.getSize(); i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 35) {
                inventory.setItem(i, redPane);
            } else if (i == 31) {
                ItemStack item = new ItemStack(Material.RED_CONCRETE);
                item.editMeta(meta -> meta.displayName(Component.text("Unignore & remove " + targetName + " from /ignore", NamedTextColor.RED, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            } else if (i < 8 || i > 27) {
                inventory.setItem(i, blackPane);
            } else if (i == 27) {
                HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
                ItemStack item = hAPI.getItemHead("10306");
                item.editMeta(meta -> meta.displayName(Component.text("Back to Ignore List", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, item);
            }
        }

        ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
        targetHead.editMeta(SkullMeta.class, meta -> {
            if(name != null) {
                meta.setOwningPlayer(offlinePlayer);
            }
            meta.displayName(Component.text(targetName, NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        });

        privateItem.editMeta(meta -> {
            meta.displayName(Component.text("Private Messages", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can private message you", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Currently ", NamedTextColor.GRAY).append(ignorePrivate ? ignoring : notIgnoring)
                    .append(Component.text(" Private Messages", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        teleportItem.editMeta(meta -> {
            meta.displayName(Component.text("Teleport Requests", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can send you teleport requests", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Currently ", NamedTextColor.GRAY).append(ignoreTeleport ? ignoring : notIgnoring)
                    .append(Component.text(" Teleport Requests", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(13, targetHead);
        inventory.setItem(20, privateItem);
        inventory.setItem(24, teleportItem);
    }
    public PlayerManager.Ignore ignore() {
        return ignore;
    }
    public String targetName() {
        return targetName;
    }
    public boolean ignorePrivate() {
        return ignorePrivate;
    }
    public boolean ignoreTeleport() {
        return ignoreTeleport;
    }
    public boolean deleteLock() {
        return deleteLock;
    }
    public void setDeleteLock(boolean deleteLock) {
        this.deleteLock = deleteLock;
    }
    public void deleteIgnore() {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM user_ignores WHERE user_id = ? AND ignored_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, ignore.targetId().toString());
            ps.executeUpdate();
            PlayerManager.removePlayerIgnores(ignore);
            player.sendMessage(Component.text("You have fully unignored & removed " + targetName + " from /ignore!", NamedTextColor.GRAY));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setIgnorePrivate() {
        ignorePrivate = !ignorePrivate;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE user_ignores SET ignore_private = ? WHERE user_id = ? AND ignored_id = ?")) {
            ps.setBoolean(1, ignorePrivate);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, ignore.targetId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PlayerManager.removePlayerIgnores(ignore);
        ignore = new PlayerManager.Ignore(player.getUniqueId(), ignore.targetId(), ignorePrivate, ignoreTeleport);
        PlayerManager.addPlayerIgnores(ignore);

        privateItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can private message you", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Currently ", NamedTextColor.GRAY).append(ignorePrivate ? ignoring : notIgnoring)
                    .append(Component.text(" Private Messages", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(20, privateItem);
    }
    public void setIgnoreTeleport() {
        ignoreTeleport = !ignoreTeleport;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE user_ignores SET ignore_teleports = ? WHERE user_id = ? AND ignored_id = ?")) {
            ps.setBoolean(1, ignoreTeleport);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, ignore.targetId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PlayerManager.removePlayerIgnores(ignore);
        ignore = new PlayerManager.Ignore(player.getUniqueId(), ignore.targetId(), ignorePrivate, ignoreTeleport);
        PlayerManager.addPlayerIgnores(ignore);

        teleportItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can send you teleport requests", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(Component.text("Currently ", NamedTextColor.GRAY).append(ignoreTeleport ? ignoring : notIgnoring)
                    .append(Component.text(" Teleport Requests", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(24, teleportItem);
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
