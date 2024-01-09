package net.skyprison.skyprisoncore.inventories.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.claims.AvailableFlags;
import net.skyprison.skyprisoncore.utils.claims.ClaimData;
import net.skyprison.skyprisoncore.utils.claims.ClaimFlag;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimFlags implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<ClaimFlag> flags = new ArrayList<>();
    private final List<ItemStack> flagsToDisplay = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack redPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack typeItem = new ItemStack(Material.COMPASS);
    private final List<String> types = Arrays.asList("All Flags", "General Flags", "Protection Flags", "Terrain Flags", "Purchased Flags");
    private final ClaimData claim;
    private final boolean canEdit;
    private final boolean hasPurchased;
    private int typePos = 0;
    public void updatePage(int page) {
        List<ItemStack> flagsToShow = flagsToDisplay;

        int totalPages = (int) Math.ceil((double) flagsToShow.size() / 28);

        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }

        for(int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if(item != null && !item.equals(blackPane) && !item.equals(redPane))
                inventory.setItem(i, null);
        }

        inventory.setItem(46, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(52, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);

        int toRemove = 28 * (this.page - 1);
        if(toRemove != 0) {
            flagsToShow = flagsToShow.subList(toRemove, flagsToShow.size());
        }
        Iterator<ItemStack> itemIterator = flagsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                ItemStack item = inventory.getItem(i);
                if(item == null)
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
                lore.add(Component.text((selected ? " " : "") + WordUtils.capitalize(type), selected ? selectedColor : color)
                        .decoration(TextDecoration.BOLD, selected).decoration(TextDecoration.ITALIC, false));
            });
            meta.lore(lore);
        });
        inventory.setItem(48, typeItem);
        flagsToDisplay.clear();
        if(getType().equalsIgnoreCase("All Flags")) {
            flagsToDisplay.addAll(flags.stream().map(ClaimFlag::getItem).toList());
        } else {
            flagsToDisplay.addAll(flags.stream().filter(pending -> switch (getType()) {
                case "General Flags" -> pending.getFlag().getGroup().equalsIgnoreCase("general");
                case "Protection Flags" -> pending.getFlag().getGroup().equalsIgnoreCase("protection");
                case "Terrain Flags" -> pending.getFlag().getGroup().equalsIgnoreCase("terrain");
                case "Purchased Flags" -> pending.getFlag().getGroup().equalsIgnoreCase("purchased");
                default -> false;
            }).map(ClaimFlag::getItem).toList());
        }
        if(direction != null) page = 1;
        updatePage(0);
    }
    public void updateFlag(ClaimFlag flag, Object flagValue) {
        AvailableFlags.FlagState flagState = flag.getFlag().getFlagState();
        flag.setFlagValue(flagValue);
        ItemStack displayItem = flag.getItem();
        displayItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(flag.getFlag().getDescription(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            lore.add(getFlagStatus(flag.getFlag(), flagState, flagValue).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        flag.setItem(displayItem);
        updateType(null);
    }
    private Component getFlagStatus(AvailableFlags flag, AvailableFlags.FlagState flagState, Object currFlagState) {
        boolean isSet = currFlagState != null;
        boolean isBoolean = flagState.equals(AvailableFlags.FlagState.BOOLEAN);
        String allowed = flag.getAllowed();
        String denied = flag.getDenied();
        String notSet = flag.getNotSet();
        if(!isSet) {
            NamedTextColor color = isBoolean ? notSet.isEmpty() ? NamedTextColor.GREEN : notSet.equalsIgnoreCase("disabled") ? NamedTextColor.RED : NamedTextColor.GREEN : NamedTextColor.GRAY;
            Component booleanMsg = Component.text(notSet.isEmpty() ? allowed : notSet, color, TextDecoration.BOLD);
            Component otherMsg = Component.text(notSet, color, TextDecoration.BOLD);
            return isBoolean ? booleanMsg : otherMsg;
        }
        boolean isAllowed = currFlagState.equals(StateFlag.State.ALLOW);
        boolean isEnabled = allowed.equalsIgnoreCase("enabled");
        boolean isTimeLock = flag.getFlags().getFirst().equals(Flags.TIME_LOCK);
        return switch (flagState) {
            case MESSAGE -> isTimeLock ? Component.text(ChatUtils.ticksToTime(Integer.parseInt(currFlagState.toString())), NamedTextColor.GREEN, TextDecoration.BOLD) :
                    LegacyComponentSerializer.legacyAmpersand().deserialize(currFlagState.toString());
            case BOOLEAN -> isAllowed ? Component.text(allowed, isEnabled ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD) :
                    Component.text(denied, NamedTextColor.RED, TextDecoration.BOLD);
            case OTHER -> Component.text(currFlagState.toString(), NamedTextColor.GREEN, TextDecoration.BOLD);
        };
    }
    public ClaimFlags(SkyPrisonCore plugin, ClaimData claim, boolean canEdit, boolean hasPurchased) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(claim.getWorld()))));
        assert regionManager != null;
        ProtectedRegion region = regionManager.getRegion(claim.getId());
        assert region != null;
        this.canEdit = canEdit;
        this.claim = claim;
        this.hasPurchased = hasPurchased;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Claim Flags", TextColor.fromHexString("#0fc3ff")));

        List<AvailableFlags> availableFlags = Arrays.stream(AvailableFlags.values()).toList();

        for(AvailableFlags flag : availableFlags) {
            AvailableFlags.FlagState flagState = flag.getFlagState();
            Object currFlagState = region.getFlag(flag.getFlags().getFirst());

            ItemStack displayItem = new ItemStack(flag.getType());
            displayItem.editMeta(meta -> {
                meta.displayName(Component.text(flag.getTitle(), TextColor.fromHexString("#0fffc3"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                if(!hasPurchased) lore.add(Component.text("This claim doesn't have access to this flag!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text(flag.getDescription(), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.empty());
                lore.add(getFlagStatus(flag, flagState, currFlagState).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            flags.add(new ClaimFlag(claim, flag, currFlagState, displayItem));
        }

        ItemStack mobSpawn = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
        mobSpawn.editMeta(meta -> {
            meta.displayName(Component.text("Mob Spawning", TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Open the mob spawning GUI", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        typeItem.editMeta(meta -> meta.displayName(Component.text("Toggle Type", TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        for(int i = 0; i < inventory.getSize() ;i++) {
            if (i == 50) {
                this.inventory.setItem(i, mobSpawn);
            } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45 || i == 53) {
                this.inventory.setItem(i, redPane);
            } else if (i < 8 || i > 45 && i < 53) {
                this.inventory.setItem(i, blackPane);
            }
        }
        updateType(null);
    }
    public ClaimData getClaim() {
        return this.claim;
    }
    public ClaimFlag getFlag(ItemStack item) {
        return flags.stream().filter(flag -> flag.getItem().equals(item)).findFirst().orElse(null);
    }
    public String getType() {
        return types.get(typePos);
    }
    public boolean getCanEdit() {
        return this.canEdit;
    }
    public boolean getHasPurchased() {
        return this.hasPurchased;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
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
}
