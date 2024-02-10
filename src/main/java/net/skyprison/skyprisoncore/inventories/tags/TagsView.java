package net.skyprison.skyprisoncore.inventories.tags;

import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.Tags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagsView implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final boolean isAdmin;
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final List<Tags.Tag> tags;
    private final List<ItemStack> tagItems = new ArrayList<>();
    private final ItemStack currentTag = new ItemStack(Material.BARRIER);
    private final Player player;
    public void updatePage(int page) {
        List<ItemStack> tagsToShow = tagItems;

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
        Iterator<ItemStack> itemIterator = tagsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                inventory.setItem(i, itemIterator.next());
            } else break;
        }
    }
    public void updateCurrentTag(PlayerManager.PlayerTag tag) {
        PlayerManager.removePlayerTags(PlayerManager.getPlayerTag(player.getUniqueId()));
        PlayerParticlesAPI.getInstance().resetActivePlayerParticles(player);
        if(tag != null) PlayerManager.addPlayerTags(tag);
        currentTag.editMeta(meta -> meta.lore(List.of(Component.text("Current Tag: ", NamedTextColor.YELLOW)
                .append(tag != null ? MiniMessage.miniMessage().deserialize(tag.tag().display()).colorIfAbsent(NamedTextColor.WHITE) : Component.text("NONE", NamedTextColor.GRAY))
                .decoration(TextDecoration.ITALIC, false))));
        inventory.setItem(49, currentTag);
    }
    public TagsView(Player player) {
        this.inventory = Bukkit.getServer().createInventory(this, 54, Component.text("Tags", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD));
        isAdmin = player.hasPermission("skyprisoncore.command.tags.admin");
        this.player = player;
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        currentTag.editMeta(meta -> meta.displayName(Component.text("Remove Tag", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
        tags = isAdmin ? Tags.tags() : Tags.tags().stream().filter(tag -> player.hasPermission("skyprisoncore.tag." + tag.id())
                || tag.permission() != null && player.hasPermission(tag.permission())).toList();

        tagItems.addAll(tags.stream().map(tag -> isAdmin ? tag.adminItem() : tag.item()).toList());
        if(isAdmin) {
            ItemStack newTag = new ItemStack(Material.LIME_CONCRETE);
            newTag.editMeta(meta -> {
                meta.displayName(Component.text("Create New Tag", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
                meta.lore(List.of(Component.text("Click to create a new tag", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            });
            inventory.setItem(53, newTag);
        } else {
            inventory.setItem(53, blackPane);
        }
        for(int i = 45; i < 53; i++) {
            inventory.setItem(i, blackPane);
        }
        updateCurrentTag(PlayerManager.getPlayerTag(player.getUniqueId()));
        updatePage(0);
    }
    public boolean isAdmin() {
        return isAdmin;
    }
    public Tags.Tag getTag(ItemStack tagItem) {
        return tags.stream().filter(tag -> (isAdmin ? tag.adminItem() : tag.item()).equals(tagItem)).findFirst().orElse(null);
    }
    public int getPage() {
        return this.page;
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
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
