package net.skyprison.skyprisoncore.inventories.misc;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
    private final PlayerManager.Ignore ignore;
    private boolean ignorePrivate = false;
    private boolean ignoreTeleport = false;
    private boolean deleteLock = false;
    private final ItemStack privateItem = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
    private final ItemStack targetHead = new ItemStack(Material.PLAYER_HEAD);
    private final Component ignoring;
    private final Component notIgnoring;
    public IgnoreEdit(Player player, PlayerManager.Ignore ignore, DatabaseHook db) {
        this.inventory = Bukkit.getServer().createInventory(this, 36, Component.text("Ignore Options", TextColor.fromHexString("#0fc3ff")));
        this.player = player;
        this.ignore = ignore;
        this.db = db;
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ignore.targetId());
        final String name = offlinePlayer.getName();
        targetName = Objects.requireNonNullElse(name, Objects.requireNonNullElse(PlayerManager.getPlayerName(ignore.targetId()), "Name Not Found.."));
        ignoring = Component.text(targetName, NamedTextColor.RED, TextDecoration.BOLD).append(Component.text("CAN'T SEND YOU ", NamedTextColor.RED))
                .decoration(TextDecoration.ITALIC, false);
        notIgnoring = Component.text(targetName, NamedTextColor.GREEN, TextDecoration.BOLD).append(Component.text("CAN SEND YOU ", NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false);
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        for(int i = 0; i < inventory.getSize(); i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 26 || i == 35) {
                inventory.setItem(i, redPane);
            } else if (i == 31) {
                ItemStack item = new ItemStack(Material.RED_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Unignore & remove " + targetName + " from /ignore", NamedTextColor.RED, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i < 8 || i > 27) {
                inventory.setItem(i, blackPane);
            } else if (i == 27) {
                HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
                ItemStack item = hAPI.getItemHead("10306");
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Back to Ignore List", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            }
        }

        targetHead.editMeta(SkullMeta.class, meta -> {
            if(name != null) {
                meta.setOwningPlayer(offlinePlayer);
            }
            meta.displayName(Component.text(targetName, NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        });

        privateItem.editMeta(meta -> {
            meta.displayName(Component.text("Private Messages", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can send you private messages.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add((ignorePrivate ? ignoring : notIgnoring).append(Component.text("PRIVATE MESSAGES").decorate(TextDecoration.BOLD)));
            meta.lore(lore);
        });
        teleportItem.editMeta(meta -> {
            meta.displayName(Component.text("Teleport Requests", NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can send you teleport requests.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add((ignorePrivate ? ignoring : notIgnoring).append(Component.text("TELEPORT REQUESTS").decorate(TextDecoration.BOLD)));
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

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_ignores (user_id, ignored_id, ignore_private) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ignore_private = VALUE(ignore_private)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, ignore.targetId().toString());
            ps.setBoolean(3, ignorePrivate);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PlayerManager.removePlayerIgnores(ignore);
        PlayerManager.addPlayerIgnores(new PlayerManager.Ignore(player.getUniqueId(), ignore.targetId(), ignorePrivate, ignoreTeleport));

        privateItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can send you private messages.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add((ignorePrivate ? ignoring : notIgnoring).append(Component.text("PRIVATE MESSAGES").decorate(TextDecoration.BOLD)));
            meta.lore(lore);
        });
        inventory.setItem(13, privateItem);
        player.sendMessage(Component.text("Private messages from " + targetName + " are now " + (ignorePrivate ? "IGNORED" : "ALLOWED") + "!", NamedTextColor.GRAY));
    }
    public void setIgnoreTeleport() {
        ignoreTeleport = !ignoreTeleport;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO user_ignores (user_id, ignored_id, ignore_teleports) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE ignore_teleports = VALUE(ignore_teleports)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, ignore.targetId().toString());
            ps.setBoolean(3, ignoreTeleport);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        PlayerManager.removePlayerIgnores(ignore);
        PlayerManager.addPlayerIgnores(new PlayerManager.Ignore(player.getUniqueId(), ignore.targetId(), ignorePrivate, ignoreTeleport));

        teleportItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Toggle whether " + targetName + " can send you teleport requests.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add((ignorePrivate ? ignoring : notIgnoring).append(Component.text("TELEPORT REQUESTS").decorate(TextDecoration.BOLD)));
            meta.lore(lore);
        });
        inventory.setItem(14, teleportItem);
        player.sendMessage(Component.text("Teleport requests from " + targetName + " are now " + (ignoreTeleport ? "IGNORED" : "ALLOWED") + "!", NamedTextColor.GRAY));
    }
    @Override
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }
    @Override
    public List<Object> customClickList() {
        return null;
    }
    @Override
    public int getPage() {
        return 1;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
