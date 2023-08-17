package net.skyprison.skyprisoncore.inventories;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MailBox implements CustomInventory {
    private final HashMap<Integer, List<ItemStack>> mails = new HashMap<>();
    private final Inventory inventory;
    private int page;
    private final DatabaseHook db;
    private final int mailBox;
    private final NamespacedKey key;
    private String name = "";
    private final int totalPages;
    private final boolean isOwner;
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
        List<Integer> mailsToShow = new ArrayList<>(mails.keySet().stream().toList());
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            mailsToShow = mailsToShow.subList(toRemove, mailsToShow.size());
        }
        Iterator<Integer> mailsIterator = mailsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(mailsIterator.hasNext()) {
                inventory.setItem(i, mails.get(mailsIterator.next()).get(0));
            }
        }
    }
    public MailBox(SkyPrisonCore plugin, DatabaseHook db, Player player, boolean isOwner, int mailBox, int page) {
        this.mailBox = mailBox;
        this.db = db;
        this.page = page;
        this.isOwner = isOwner;
        this.key = new NamespacedKey(plugin, "mail_id");
        boolean postOffice = mailBox == -1;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT name FROM mail_boxes WHERE id = ?")) {
            ps.setInt(1, mailBox);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.name = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Mailbox: " + name, TextColor.fromHexString("#0fc3ff")));
        SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        if(postOffice) {
            try (Connection conn = db.getConnection(); PreparedStatement ps =
                    conn.prepareStatement("SELECT id, sender_id, receiver_id, item, sent_at FROM mails WHERE mailbox_id = -1 AND collected = 0 AND receiver_id = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String sender = rs.getString(2);
                    String receiver = rs.getString(3);
                    Date sentAt = new Date(rs.getLong(5));
                    List<ItemStack> mailItems = new ArrayList<>();
                    ItemStack mail = ItemStack.deserializeBytes(rs.getBytes(4));
                    ItemStack displayMail = mail.clone();
                    displayMail.editMeta(meta -> {
                        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("Sent to: ", NamedTextColor.GRAY).append(Component.text(receiver, NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Sent by: ", NamedTextColor.GRAY).append(Component.text(sender, NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("                         ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
                        lore.add(Component.text("Sent at: ", NamedTextColor.GRAY).append(Component.text(dateFor.format(sentAt), NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                        meta.lore(lore);
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
                    });
                    mailItems.add(displayMail);
                    mailItems.add(mail);
                    mails.put(id, mailItems);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try (Connection conn = db.getConnection(); PreparedStatement ps =
                    conn.prepareStatement("SELECT id, sender_id, receiver_id, item, sent_at FROM mails WHERE mailbox_id = ? AND collected = 0")) {
                ps.setInt(1, mailBox);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String sender = rs.getString(2);
                    String receiver = rs.getString(3);
                    Date sentAt = new Date(rs.getLong(5));
                    List<ItemStack> mailItems = new ArrayList<>();
                    ItemStack mail = ItemStack.deserializeBytes(rs.getBytes(4));
                    ItemStack displayMail = mail.clone();
                    displayMail.editMeta(meta -> {
                        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("Sent to: ", NamedTextColor.GRAY).append(Component.text(receiver, NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Sent by: ", NamedTextColor.GRAY).append(Component.text(sender, NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("                         ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
                        lore.add(Component.text("Sent at: ", NamedTextColor.GRAY).append(Component.text(dateFor.format(sentAt), NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                        meta.lore(lore);
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
                    });
                    mailItems.add(displayMail);
                    mailItems.add(mail);
                    mails.put(id, mailItems);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        totalPages = (int) Math.ceil((double) mails.size() / 45);

        for (int i = 45; i < 54; i++) {
            if(i == 48 && mailBox != -1) {
                ItemStack openSettings = new ItemStack(Material.REPEATER);
                openSettings.editMeta(meta -> meta.displayName(Component.text("View Mailbox Settings", NamedTextColor.GREEN,
                        TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, openSettings);
            } else if(i == 50) {
                ItemStack sendMail = new ItemStack(Material.WRITABLE_BOOK);
                sendMail.editMeta(meta -> meta.displayName(Component.text("Send Mail", NamedTextColor.GREEN,
                        TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, sendMail);
            } else inventory.setItem(i, blackPane);
        }
        updatePage(0);
    }
    public ItemStack getMailItem(ItemStack displayItem) {
        int id = displayItem.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        ItemStack mailItem = mails.get(id).get(1);
        mails.remove(id);
        updatePage(0);
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE mails SET collected = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mailItem;
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
    public String getName() {
        return this.name;
    }
    public boolean isOwner() {
        return this.isOwner;
    }
    public int getMailBox() {
        return this.mailBox;
    }
    public NamespacedKey getKey() {
        return this.key;
    }
}
