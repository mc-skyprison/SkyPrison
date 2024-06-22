package net.skyprison.skyprisoncore.inventories.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Ignore implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<IgnoreItem> ignoreDisplay = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private record IgnoreItem(UUID targetId, ItemStack item, PlayerManager.Ignore ignore) {}
    public void updatePage(int page) {
        List<ItemStack> ignoresToShow = ignoreDisplay.stream().map(IgnoreItem::item).toList();

        int totalPages = (int) Math.ceil((double) ignoresToShow.size() / 45);

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
            ignoresToShow = ignoresToShow.subList(toRemove, ignoresToShow.size());
        }
        Iterator<ItemStack> itemIterator = ignoresToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                inventory.setItem(i, itemIterator.next());
            } else break;
        }
    }
    public Ignore(Player player) {
        this.inventory = Bukkit.getServer().createInventory(this, 54,
                Component.text("Ignored Players", NamedTextColor.GRAY, TextDecoration.BOLD));
        List<PlayerManager.Ignore> ignores = PlayerManager.getPlayerIgnores(player.getUniqueId());
        Component allowed = Component.text("ALLOWED", NamedTextColor.GREEN, TextDecoration.BOLD);
        Component ignored = Component.text("IGNORED", NamedTextColor.RED, TextDecoration.BOLD);
        ignores.forEach(ignore -> {
            ItemStack ignoreHead = new ItemStack(Material.PLAYER_HEAD);
            ignoreHead.editMeta(SkullMeta.class, meta -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ignore.targetId());
                String name = offlinePlayer.getName();
                if(name != null) {
                    meta.setOwningPlayer(offlinePlayer);
                } else {
                    name = PlayerManager.getPlayerName(ignore.targetId());
                }
                meta.displayName(Component.text(Objects.requireNonNullElse(name, "Name Not Found.."),
                        NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Private Messages are ", NamedTextColor.GRAY).append(ignore.ignorePrivate() ? ignored : allowed)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Teleport Requests are ", NamedTextColor.GRAY).append(ignore.ignoreTeleport() ? ignored : allowed)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(Component.text("Click to edit options", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            ignoreDisplay.add(new IgnoreItem(ignore.targetId(), ignoreHead, ignore));
        });

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        for(int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        ItemStack addIgnore = new ItemStack(Material.LIME_CONCRETE);
        addIgnore.editMeta(meta -> {
            meta.displayName(Component.text("Add Player to Ignore", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            meta.lore(Collections.singletonList(Component.text("Click to add a player to your ignore list", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false)));
        });
        inventory.setItem(53, addIgnore);
        updatePage(0);
    }
    public PlayerManager.Ignore getIgnore(ItemStack item) {
        return ignoreDisplay.stream().filter(ignore -> {
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            SkullMeta ignoreMeta = (SkullMeta) ignore.item().getItemMeta();
            return Objects.equals(meta.getOwningPlayer(), ignoreMeta.getOwningPlayer());
        }).findFirst().map(IgnoreItem::ignore).orElse(null);
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
