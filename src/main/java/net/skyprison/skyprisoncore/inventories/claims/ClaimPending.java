package net.skyprison.skyprisoncore.inventories.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.claims.ClaimData;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimPending implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<Pending> pendings = new ArrayList<>();
    private final List<ItemStack> pendingsToDisplay = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack typeItem = new ItemStack(Material.COMPASS);
    private final List<String> types = Arrays.asList("All Pending", "Invites", "Transfers");
    private final List<ClaimData> claims;
    public record Pending(ItemStack item, ClaimData claim, NotificationsUtils.Notification notification) {}
    private int typePos = 0;
    public void updatePage(int page) {
        List<ItemStack> pendingsToShow = pendingsToDisplay;

        int totalPages = (int) Math.ceil((double) pendingsToShow.size() / 45);

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
            pendingsToShow = pendingsToShow.subList(toRemove, pendingsToShow.size());
        }
        Iterator<ItemStack> itemIterator = pendingsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                inventory.setItem(i, itemIterator.next());
            } else break;
        }
    }
    public void updateType(Boolean direction) {
        if(direction != null) typePos = direction ? (typePos + 1) % types.size() : (typePos - 1 + types.size()) % types.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        typeItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            types.forEach(type -> {
                boolean selected = getType().equalsIgnoreCase(type);
                lore.add(Component.text((selected ? " " : "") + StringUtils.capitalize(type), selected ? selectedColor : color)
                        .decoration(TextDecoration.BOLD, selected).decoration(TextDecoration.ITALIC, false));
            });
            meta.lore(lore);
        });
        inventory.setItem(49, typeItem);
        pendingsToDisplay.clear();
        if(getType().equalsIgnoreCase("All Pending")) {
            pendingsToDisplay.addAll(pendings.stream().map(Pending::item).toList());
        } else {
            pendingsToDisplay.addAll(pendings.stream().filter(pending -> switch (getType()) {
                case "Invites" -> pending.notification.type().equalsIgnoreCase("claim-invite");
                case "Transfers" -> pending.notification.type().equalsIgnoreCase("claim-transfer");
                default -> false;
            }).map(Pending::item).toList());
        }
        page = 1;
        updatePage(0);
    }
    public ClaimPending(SkyPrisonCore plugin, List<ClaimData> claims) {
        this.claims = claims;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Pending Claim Descisions", TextColor.fromHexString("#0fc3ff")));

        List<NotificationsUtils.Notification> notifications = NotificationsUtils.getNotificationsFromExtra(claims.stream().map(ClaimData::getId).toList());

        for(NotificationsUtils.Notification notification : notifications) {
            ItemStack displayItem = new ItemStack(Material.PLAYER_HEAD);
            ClaimData claim = claims.stream().filter(claimData -> claimData.getId().equalsIgnoreCase(notification.extraData())).findFirst().orElse(null);
            if(claim == null) {
                Bukkit.getLogger().warning("Claim not found for notification " + notification.id() + "!");
                continue;
            }
            displayItem.editMeta(SkullMeta.class, meta -> {
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(notification.player());
                String name = oPlayer.getName();
                if(name != null) {
                    meta.setOwningPlayer(oPlayer);
                } else {
                    name = PlayerManager.getPlayerName(notification.player());
                }
                meta.displayName(Component.text(Objects.requireNonNullElse(name, "Name Not Found.."), TextColor.fromHexString("#0fffc3"), TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(notification.type().equalsIgnoreCase("claim-transfer") ? "Pending transfer for " : "Pending invite to ", TextColor.fromHexString("#ffba75"))
                        .append(Component.text(claim.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            pendings.add(new Pending(displayItem, claim, notification));
        }

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        typeItem.editMeta(meta -> meta.displayName(Component.text("Toggle Type", TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        for(int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updateType(null);
    }
    public String getType() {
        return types.get(typePos);
    }
    public List<ClaimData> getClaims() {
        return this.claims;
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
