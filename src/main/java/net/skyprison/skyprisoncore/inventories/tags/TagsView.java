package net.skyprison.skyprisoncore.inventories.tags;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.inventories.claims.ClaimMembers;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagsView implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<ClaimMembers.Member> members = new ArrayList<>();
    private final List<ItemStack> membersToDisplay = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private ItemStack currentTag;
    private final List<Tag> tags = new ArrayList<>();
    private final DatabaseHook db;
    private record Tag(int id, String display, String lore, String effect, ItemStack item) {}
    public void updatePage(int page) {
        List<Tag> tagsToShow = tags;

        int totalPages = (int) Math.ceil((double) tagsToShow.size() / 45);

        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }

        for(int i = 0; i < 45; i++) {
            inventory.setItem(i, null);
        }

        inventory.setItem(46, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(52, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            tagsToShow = tagsToShow.subList(toRemove, tagsToShow.size());
        }
        Iterator<Tag> itemIterator = tagsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                inventory.setItem(i, itemIterator.next().item);
            } else break;
        }
    }
    public TagsView(SkyPrisonCore plugin, DatabaseHook db, Player player) {
        this.db = db;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Tags", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tags_id, tags_display, tags_lore, tags_effect, tags_permission FROM tags")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt(1);
                String display = rs.getString(2);
                String tagLore = rs.getString(3);
                String effect = rs.getString(4);
                String permission = rs.getString(5);
                if(player.hasPermission("skyprisoncore.command.tags.admin") ||
                        player.hasPermission("skyprisoncore.tag." + id) ||
                        (permission != null && !permission.isEmpty() && player.hasPermission(permission))) {
                    ItemStack item = new ItemStack(Material.NAME_TAG);
                    item.editMeta(meta -> {
                        meta.displayName(MiniMessage.miniMessage().deserialize(display));
                        List<Component> lore = new ArrayList<>();
                        if(effect != null && !effect.isEmpty()) {
                            lore.add(Component.text("Tag Effect: ", NamedTextColor.YELLOW).append(Component.text(effect, NamedTextColor.GRAY)));
                        }
                        lore.add(MiniMessage.miniMessage().deserialize(tagLore));
                        if(player.hasPermission("skyprisoncore.command.tags.admin")) {
                            lore.add(Component.empty());
                            lore.add(Component.text("Tag ID: ", NamedTextColor.YELLOW).append(Component.text(String.valueOf(id), NamedTextColor.GRAY)));
                        }
                        meta.lore(lore);
                    });
                    tags.add(new Tag(id, display, tagLore, effect, item));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        for(int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updatePage(0);
    }
    @Override
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }
    @Override
    public List<Object> customClickList() {
        return null;
    }
    public int getPage() {
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
