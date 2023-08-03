package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MailboxMembers implements CustomInventory {
    private final List<ItemStack> members = new ArrayList<>();
    private final Inventory inventory;
    private int page = 1;
    private final DatabaseHook db;
    private final int mailBox;
    private String name = "";
    private final boolean isOwner;
    private final int totalPages;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack blackPane;

    public void updatePage(int page) {
        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }
        for(int i = 0; i < 45; i++) inventory.setItem(i, null);
        inventory.setItem(45, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(53, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        List<ItemStack> membersToShow = new ArrayList<>(members);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            membersToShow = membersToShow.subList(toRemove, membersToShow.size());
        }
        Iterator<ItemStack> membersIterator = membersToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(membersIterator.hasNext()) {
                inventory.setItem(i, membersIterator.next());
            }
        }
    }
    public MailboxMembers(SkyPrisonCore plugin, DatabaseHook db, boolean isOwner, int mailBox, int page) {
        this.mailBox = mailBox;
        this.isOwner = isOwner;
        this.db = db;
        this.page = page;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Mailbox Members", TextColor.fromHexString("#0fc3ff")));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.name = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        UUID ownerId = null;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT owner_id FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ownerId = UUID.fromString(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM mail_boxes_users WHERE mailbox_id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID memberId = UUID.fromString(rs.getString(1));
                if(ownerId != null && !ownerId.equals(memberId)) {
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    item.editMeta(SkullMeta.class, meta -> {
                        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(memberId)));
                        meta.displayName(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(memberId), "ERROR"),
                                NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                        if(isOwner) {
                            List<Component> lore = new ArrayList<>();
                            lore.add(Component.empty());
                            lore.add(Component.text("Remove Member from Mailbox", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                            meta.lore(lore);
                        }
                    });
                    members.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        totalPages = (int) Math.ceil((double) members.size() / 45);

        for (int i = 45; i < 54; i++) {
            if(i == 49 && isOwner) {
                ItemStack inviteMember = new ItemStack(Material.OAK_SIGN);
                inviteMember.editMeta(meta -> meta.displayName(Component.text("Invite Player to Mailbox", NamedTextColor.GREEN,
                        TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, blackPane);
            } else inventory.setItem(i, blackPane);
        }
        updatePage(0);
    }
    public void kickMember(UUID memberId) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM mail_boxes_users WHERE mailbox_id = ? AND user_id = ?")) {
            ps.setInt(1, mailBox);
            ps.setString(1, memberId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean isMember(UUID memberId) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM mail_boxes_users WHERE mailbox_id = ? AND user_id = ?")) {
            ps.setInt(1, mailBox);
            ps.setString(2, memberId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void addMember(UUID memberId) {
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO mail_box_users (mailbox_id, user_id, preferred) VALUES (?, ?, ?)")) {
            ps.setInt(1, mailBox);
            ps.setString(2, memberId.toString());
            ps.setInt(3, 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
    public boolean isOwner() {
        return this.isOwner;
    }
    public String getName() {
        return this.name;
    }
}
