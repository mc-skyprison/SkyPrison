package net.skyprison.skyprisoncore.inventories.mail;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.MailUtils;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MailHistory implements CustomInventory {
    private final List<ItemStack> mails = new ArrayList<>();
    private final Inventory inventory;
    private int page = 1;
    private final int totalPages;
    private boolean sort = true;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack blackPane;

    public void updateSort() {
        this.sort = !this.sort;
        Collections.reverse(mails);
        ItemStack sortItem = new ItemStack(Material.CLOCK);
        sortItem.editMeta(meta -> {
            meta.displayName(Component.text("Sort Mails", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Oldest -> Newest" : "Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(49, sortItem);
        updatePage(0);
    }

    public void updatePage(int page) {
        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }
        for(int i = 0; i < 45; i++) inventory.setItem(i, null);
        inventory.setItem(45, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(53, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        List<ItemStack> mailsToShow = new ArrayList<>(mails);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            mailsToShow = mailsToShow.subList(toRemove, mailsToShow.size());
        }
        Iterator<ItemStack> mailsIterator = mailsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(mailsIterator.hasNext()) {
                inventory.setItem(i, mailsIterator.next());
            }
        }
    }

    public MailHistory(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID) {
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Mail History", NamedTextColor.RED));

        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.empty()));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT sender_id, item, cost, mailbox_id, sent_at FROM mails WHERE receiver_id = ? AND collected = 1 ORDER BY sent_at ASC")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            while (rs.next()) {
                UUID sender = UUID.fromString(rs.getString(1));
                ItemStack item = ItemStack.deserializeBytes(rs.getBytes(2));
                int cost = rs.getInt(3);
                int mailBox = rs.getInt(4);
                boolean deleted = MailUtils.isMailBoxDeleted(mailBox);

                String date = dateFor.format(new Date(rs.getLong(5)));
                item.editMeta(meta -> {
                    meta.displayName(Component.text(date, NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    ArrayList<Component> lore = new ArrayList<>();
                    if(item.getType().equals(Material.WRITTEN_BOOK)) {
                        lore.add(Component.text("                         ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH));
                    }
                    lore.add(Component.text("Mailbox: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNullElse(
                            MailUtils.getMailBoxName(mailBox), "COULDN'T GET MAILBOX NAME!") + (deleted ? " (Deleted)" : ""), NamedTextColor.WHITE))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Sent by: ", NamedTextColor.GRAY).append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(sender),
                                    "COULDN'T GET NAME"), NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                    if(cost != 0) {
                        lore.add(Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(cost), NamedTextColor.WHITE))
                                .decoration(TextDecoration.ITALIC, false));
                    }
                    if(item.getType().equals(Material.WRITTEN_BOOK)) {
                        lore.add(Component.empty());
                        lore.add(Component.text("CLICK TO READ", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    }
                    meta.lore(lore);
                });
                mails.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        totalPages = (int) Math.ceil((double) mails.size() / 45);

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updateSort();
        updatePage(0);
    }
    @Override
    public int page() {
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

