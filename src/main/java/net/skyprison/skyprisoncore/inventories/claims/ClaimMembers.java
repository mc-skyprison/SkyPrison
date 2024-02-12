package net.skyprison.skyprisoncore.inventories.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.claims.ClaimData;
import net.skyprison.skyprisoncore.utils.claims.ClaimMember;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimMembers implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<Member> members = new ArrayList<>();
    private final List<ItemStack> membersToDisplay = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack typeItem = new ItemStack(Material.COMPASS);
    private final List<String> types = Arrays.asList("All Members", "Owner", "Co-owners", "Members");
    private final ClaimData claim;
    public record Member(ItemStack item, ClaimMember memberData) {}
    private int typePos = 0;
    public void updatePage(int page) {
        List<ItemStack> membersToShow = membersToDisplay;

        int totalPages = (int) Math.ceil((double) membersToShow.size() / 45);

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
            membersToShow = membersToShow.subList(toRemove, membersToShow.size());
        }
        Iterator<ItemStack> itemIterator = membersToShow.iterator();
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
        membersToDisplay.clear();
        if(getType().equalsIgnoreCase("All Members")) {
            membersToDisplay.addAll(members.stream().map(Member::item).toList());
        } else {
            membersToDisplay.addAll(members.stream().filter(member -> switch (getType()) {
                case "Owner" -> member.memberData.getRank().equalsIgnoreCase("owner");
                case "Co-owners" -> member.memberData.getRank().equalsIgnoreCase("co-owner");
                case "Members" -> member.memberData.getRank().equalsIgnoreCase("member");
                default -> false;
            }).map(Member::item).toList());
        }
        page = 1;
        updatePage(0);
    }
    public ClaimMembers(SkyPrisonCore plugin, ClaimData claim) {
        this.claim = claim;
        this.inventory = plugin.getServer().createInventory(this, 54,
                Component.text(claim.getName() + " - Members", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD));

        for(ClaimMember member : claim.getMembers()) {
            ItemStack displayItem = new ItemStack(Material.PLAYER_HEAD);
            displayItem.editMeta(SkullMeta.class, meta -> {
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(member.getUniqueId());
                String name = oPlayer.getName();
                if(name != null) {
                    meta.setOwningPlayer(oPlayer);
                } else {
                    name = PlayerManager.getPlayerName(member.getUniqueId());
                }
                meta.displayName(Component.text(Objects.requireNonNullElse(name, "Name Not Found.."), TextColor.fromHexString("#0fffc3"), TextDecoration.BOLD)
                        .append(Component.text(" (", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
                        .append(Component.text(StringUtils.capitalize(member.getRank()), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                        .append(Component.text(")", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
            });
            members.add(new Member(displayItem, member));
        }

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        typeItem.editMeta(meta -> meta.displayName(Component.text("Toggle Members", TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        for(int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updateType(null);
    }
    public String getType() {
        return types.get(typePos);
    }
    public ClaimData getClaim() {
        return this.claim;
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
