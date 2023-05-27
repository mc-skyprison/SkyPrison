package net.skyprison.skyprisoncore.inventories;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
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
import net.skyprison.skyprisoncore.utils.claims.AvailableFlags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimFlags implements CustomInventory {

    private final Inventory inventory;

    private final String category;

    private final boolean canEdit;

    private final String claimId;

    private final String world;

    private final int page;

    public ClaimFlags(SkyPrisonCore plugin, String claimId, String world, boolean canEdit, String category, int page) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(world))));
        assert regionManager != null;
        ProtectedRegion region = regionManager.getRegion(claimId);
        assert region != null;
        this.claimId = claimId;
        this.world = world;
        this.page = page;
        this.category = category;
        this.canEdit = canEdit;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Claim Flags").color(TextColor.fromHexString("#0fc3ff")).decoration(TextDecoration.ITALIC, false));
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("Next Page").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextMeta);
        ItemStack prevPage = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.displayName(Component.text("Previous Page").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        prevPage.setItemMeta(prevMeta);
        List<AvailableFlags> flags;
        if(!category.isEmpty()) {
            flags = Arrays.stream(AvailableFlags.values()).filter(flag -> flag.getGroup().equalsIgnoreCase(category)).toList();
        } else {
            flags = Arrays.stream(AvailableFlags.values()).toList();
        }

        int totalPages = (int) Math.ceil((double) flags.size() / 28);

        if(page > totalPages) {
            page = 1;
        }

        int toRemove = 28 * (page - 1);
        if(toRemove != 0) {
            flags = flags.subList(toRemove, flags.size()-1);
        }

        Iterator<AvailableFlags> flagIterate = flags.iterator();

        for(int i = 0; i < this.inventory.getSize();i++) {
            if (i == 47 && page != 1) {
                this.inventory.setItem(i, prevPage);
            } else if (i == 51 && page != totalPages) {
                this.inventory.setItem(i, nextPage);
            } else if (i == 48) {
                ItemStack cat = new ItemStack(Material.WRITABLE_BOOK);
                ItemMeta catMeta = cat.getItemMeta();
                TextColor color = NamedTextColor.GRAY;
                TextColor selectedColor = TextColor.fromHexString("#0fffc3");
                catMeta.displayName(Component.text("Toggle Flags").color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("All Flags").color(category.equalsIgnoreCase("") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("General Flags").color(category.equalsIgnoreCase("general") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Protection Flags").color(category.equalsIgnoreCase("protection") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Terrain Flags").color(category.equalsIgnoreCase("terrain") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Purchased Flags").color(category.equalsIgnoreCase("purchased") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                catMeta.lore(lore);

                cat.setItemMeta(catMeta);
                this.inventory.setItem(i, cat);
            } else if (i == 50) {
                ItemStack cat = new ItemStack(Material.ZOMBIE_SPAWN_EGG);
                ItemMeta catMeta = cat.getItemMeta();
                TextColor color = NamedTextColor.GRAY;
                catMeta.displayName(Component.text("Mob Spawning").color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Open the mob spawning GUI").color(color).decoration(TextDecoration.ITALIC, false));
                catMeta.lore(lore);

                cat.setItemMeta(catMeta);
                this.inventory.setItem(i, cat);
            } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45 || i == 53) {
                this.inventory.setItem(i, redPane);
            } else if (i < 8 || i > 45 && i < 53) {
                this.inventory.setItem(i, blackPane);
            } else {
                if (flagIterate.hasNext()) {
                    AvailableFlags flag = flagIterate.next();

                    ItemStack flagItem = new ItemStack(flag.getType());
                    ItemMeta flagMeta = flagItem.getItemMeta();
                    flagMeta.displayName(Component.text(flag.getTitle()).color(TextColor.fromHexString("#0fffc3")).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text(flag.getDescription()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text(""));
                    Component flagStatus = Component.text("");
                    AvailableFlags.FlagState flagState = flag.getFlagState();
                    Object currFlagState = region.getFlag(flag.getFlags().get(0));
                    boolean isSet = currFlagState != null;
                    switch (flagState) {
                        case MESSAGE -> {
                            if(isSet) {
                                if(flag.getFlags().get(0).equals(Flags.TIME_LOCK)) {
                                    flagStatus = Component.text(plugin.ticksToTime(Integer.parseInt(flagState.toString()))).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
                                } else {
                                    flagStatus = LegacyComponentSerializer.legacyAmpersand().deserialize(flagState.toString());
                                }
                            } else {
                                flagStatus = Component.text(flag.getNotSet()).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
                            }
                        }
                        case BOOLEAN -> {
                            if(isSet) {
                                if(currFlagState.equals(StateFlag.State.ALLOW)) {
                                    flagStatus = Component.text(flag.getAllowed()).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
                                } else {
                                    flagStatus = Component.text(flag.getDenied()).color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
                                }
                            } else {
                                flagStatus = Component.text(flag.getNotSet()).color(flag.getNotSet().equalsIgnoreCase("enabled") ? NamedTextColor.GREEN : NamedTextColor.RED).decorate(TextDecoration.BOLD);
                            }
                        }
                        case OTHER -> {
                            if(isSet) {
                                flagStatus = Component.text(currFlagState.toString()).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
                            } else {
                                flagStatus = Component.text(flag.getNotSet()).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD);
                            }
                        }
                    }
                    lore.add(flagStatus.decoration(TextDecoration.ITALIC, false));
                    flagMeta.lore(lore);
                    StringBuilder combinedFlag = new StringBuilder();
                    for (Iterator<Flag<?>> it = flag.getFlags().iterator(); it.hasNext();) {
                        Flag<?> combFlag = it.next();
                        combinedFlag.append(combFlag.getName());
                        if(it.hasNext()) {
                            combinedFlag.append(",");
                        }
                    }
                    NamespacedKey key = new NamespacedKey(plugin, "flags");
                    flagMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, combinedFlag.toString());

                    flagItem.setItemMeta(flagMeta);
                    this.inventory.setItem(i, flagItem);
                }
            }
        }
    }

    public String getNextCategory(String category) {
        String nextCat = "";
        switch (category) {
            case "" -> nextCat = "general";
            case "general" -> nextCat = "protection";
            case "protection" -> nextCat = "terrain";
            case "terrain" -> nextCat = "purchased";
            case "purchased" -> nextCat = "";
        }
        return nextCat;
    }


    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public String getClaimId() {
        return this.claimId;
    }

    public String getWorld() {
        return this.world;
    }

    public String getCategory() {
        return this.category;
    }

    public boolean getCanEdit() {
        return this.canEdit;
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