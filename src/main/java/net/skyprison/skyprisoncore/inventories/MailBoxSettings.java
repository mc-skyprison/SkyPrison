package net.skyprison.skyprisoncore.inventories;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.items.PostOffice;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MailBoxSettings implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final int mailBox;
    private String name;
    private final boolean isOwner;
    private final Player player;
    private Location loc;
    private final TextDisplay boxDisplay;

    public void updateInventory() {
        ItemStack nameItem = new ItemStack(Material.OAK_SIGN);
        nameItem.editMeta(meta -> {
            meta.displayName(Component.text("Mailbox Name", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Name: ", NamedTextColor.GRAY).append(Component.text(name, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
            if(isOwner) {
                lore.add(Component.empty());
                lore.add(Component.text("CLICK TO CHANGE NAME", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        });
        inventory.setItem(10, nameItem);
    }
    public MailBoxSettings(SkyPrisonCore plugin, DatabaseHook db, int mailBox, boolean isOwner, Player player) {
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, 27, Component.text("Mailbox Settings", NamedTextColor.AQUA));
        this.db = db;
        this.mailBox = mailBox;
        this.isOwner = isOwner;
        this.player = player;

        TextDisplay tempDisplay = null;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name, x, y, z, world, display_text FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.name = rs.getString(1);
                this.loc = new Location(plugin.getServer().getWorld(rs.getString(5)), rs.getInt(2), rs.getInt(3), rs.getInt(4));
                tempDisplay = (TextDisplay) player.getWorld().getEntity(UUID.fromString(rs.getString(6)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.boxDisplay = tempDisplay;
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        for(int i = 0; i < 27; i++) {
            switch (i) {
                case 0,8,9,17,26 -> inventory.setItem(i, redPane);
                case 1,2,3,4,5,6,7,19,20,21,22,23,24,25 -> inventory.setItem(i, blackPane);
                case 10 -> {
                    ItemStack nameItem = new ItemStack(Material.OAK_SIGN);
                    nameItem.editMeta(meta -> {
                        meta.displayName(Component.text("Mailbox Name", NamedTextColor.YELLOW, TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("Name: ", NamedTextColor.GRAY).append(Component.text(name, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                        if(isOwner) {
                            lore.add(Component.empty());
                            lore.add(Component.text("CLICK TO CHANGE NAME", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        }
                        meta.lore(lore);
                    });
                    inventory.setItem(i, nameItem);
                }
                case 12 -> {
                    ItemStack members = new ItemStack(Material.PLAYER_HEAD);
                    members.editMeta(meta -> {
                        meta.displayName(Component.text("Mailbox Members", NamedTextColor.YELLOW, TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("View Members", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                        meta.lore(lore);
                    });
                    inventory.setItem(i, members);
                }
                case 14 -> {
                    if(isOwner) {
                        ItemStack pickup = new ItemStack(Material.HOPPER);
                        pickup.editMeta(meta -> meta.displayName(Component.text("Pickup Mailbox", NamedTextColor.GRAY, TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false)));
                        inventory.setItem(i, pickup);
                    }
                }
                case 16 -> {
                    if(isOwner) {
                        ItemStack delete = new ItemStack(Material.BARRIER);
                        delete.editMeta(meta -> meta.displayName(Component.text("Delete Mailbox", NamedTextColor.RED, TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false)));
                        inventory.setItem(i, delete);
                    }
                }
                case 18 -> {
                    HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
                    ItemStack back = hAPI.getItemHead("10306");
                    back.editMeta(meta -> meta.displayName(Component.text("Back to Settings", NamedTextColor.GREEN,
                            TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                    inventory.setItem(i, back);
                }
            }
        }
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
    public boolean isOwner() {
        return this.isOwner;
    }
    public boolean setName(String name) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE mail_boxes SET name = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setInt(2, mailBox);
            ps.executeUpdate();
            this.name = name;
            boxDisplay.text(Component.text("Mailbox " + name, NamedTextColor.YELLOW));
            updateInventory();
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }
    public void pickupMailbox() {
        ItemStack box = PostOffice.getMailBox(plugin, 1);
        box.editMeta(meta -> {
            NamespacedKey key = new NamespacedKey(plugin, "mailbox");
            List<Component> lore = meta.lore();
            lore.add(Component.empty());
            lore.add(Component.text("Mailbox Saved: ", NamedTextColor.GRAY).append(Component.text(name, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, mailBox);
        });
        HashMap<Integer, ItemStack> didntFit = player.getInventory().addItem(box);
        for (ItemStack item : didntFit.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item).setOwner(player.getUniqueId());
        }
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE mail_boxes SET is_placed = ? WHERE id = ?")) {
            ps.setInt(1, 0);
            ps.setInt(2, mailBox);
            ps.executeUpdate();
            loc.getBlock().setType(Material.AIR);
            boxDisplay.remove();
            player.closeInventory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isNoMail() {
        try (Connection conn = db.getConnection(); PreparedStatement ps =
                conn.prepareStatement("SELECT id FROM mails WHERE mailbox_id = ? AND collected = 0")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void deleteMailBox() {
        try (Connection conn = db.getConnection(); PreparedStatement ps =
                conn.prepareStatement("UPDATE mail_boxes SET x = ?, y = ?, z = ?, world = ?, is_placed = ?, display_text = ? WHERE id = ?")) {
            ps.setInt(1, 0);
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.setString(4, "world_event");
            ps.setInt(5, 0);
            ps.setString(6, null);
            ps.setInt(7, mailBox);
            ps.executeUpdate();
            loc.getBlock().setType(Material.AIR);
            boxDisplay.remove();
            try(Connection sConn = db.getConnection(); PreparedStatement sPs = sConn.prepareStatement("DELETE FROM mail_boxes_users WHERE mailbox_id = ?")) {
                sPs.setInt(1, mailBox);
                sPs.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String getName() {
        return this.name;
    }
    public int getMailBox() {
        return this.mailBox;
    }
}

