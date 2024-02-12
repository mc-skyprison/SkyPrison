package net.skyprison.skyprisoncore.inventories.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.claims.ClaimData;
import net.skyprison.skyprisoncore.utils.claims.ClaimFlagMob;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ClaimFlagsMobs implements CustomInventory {
    private final Inventory inventory;
    private int page = 1;
    private final List<ClaimFlagMob> mobs = new ArrayList<>();
    private final List<ItemStack> mobsToDisplay = new ArrayList<>();
    private final List<EntityType> deniedMobs = new ArrayList<>();
    private final ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private final ItemStack nextPage = new ItemStack(Material.PAPER);
    private final ItemStack prevPage = new ItemStack(Material.PAPER);
    private final ItemStack typePane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
    private final ItemStack typeItem = new ItemStack(Material.LIME_CONCRETE);
    private final ItemStack allSpawn = new ItemStack(Material.SPAWNER);
    private final ProtectedRegion region;
    private boolean isAllowed = true;
    private final ClaimData claim;
    private final boolean canEdit;
    private final boolean hasPurchased;
    public void updatePage(int page) {
        List<ItemStack> mobsToShow = mobsToDisplay;

        int totalPages = (int) Math.ceil((double) mobsToShow.size() / 45);

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
            mobsToShow = mobsToShow.subList(toRemove, mobsToShow.size());
        }
        Iterator<ItemStack> itemIterator = mobsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(itemIterator.hasNext()) {
                ItemStack item = inventory.getItem(i);
                if(item == null)
                    inventory.setItem(i, itemIterator.next());
            } else break;
        }
    }
    public void updateType(boolean changeDirection) {
        if(changeDirection) isAllowed = !isAllowed;
        typeItem.editMeta(meta -> meta.displayName(Component.text("Switch view to ", NamedTextColor.GRAY)
                .append(Component.text(isAllowed ? "DISABLED" : "ENABLED", isAllowed ? NamedTextColor.RED : NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" mob spawns", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false)));
        inventory.setItem(49, typeItem);
        for(int i = 36; i < 45; i++) {
            typePane.setType(!isAllowed ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
            inventory.setItem(i, typePane);
        }
        mobsToDisplay.clear();

        mobsToDisplay.addAll(mobs.stream().filter(mob -> mob.isEnabled() == isAllowed).map(ClaimFlagMob::getItem).toList());

        if(changeDirection) page = 1;
        updatePage(0);
    }
    public void updateMob(ItemStack item) {
        ClaimFlagMob mob = mobs.stream().filter(m -> m.getItem().equals(item)).findFirst().orElse(null);
        if(mob == null) return;
        boolean isAllowed = !mob.isEnabled();
        mob.setEnabled(isAllowed);
        ItemStack displayItem = mob.getItem();
        displayItem.editMeta(meta -> {
            String mobName = StringUtils.capitalize(mob.getType().name().toLowerCase().replace("_", " "));
            meta.displayName(Component.text(mobName, isAllowed ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            Component mobState = Component.text(isAllowed ? "DISABLE" : "ENABLE", isAllowed ? NamedTextColor.RED : NamedTextColor.GREEN, TextDecoration.BOLD);
            lore.add(Component.text("Click to ", NamedTextColor.GRAY).append(mobState)
                    .append(Component.text(" spawning for ", NamedTextColor.GRAY))
                    .append(Component.text(mobName, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        mob.setItem(displayItem);
        if(isAllowed) deniedMobs.remove(mob.getType());
        else deniedMobs.add(mob.getType());
        region.setFlag(Flags.DENY_SPAWN, deniedMobs.stream().map(BukkitAdapter::adapt).collect(Collectors.toSet()));
        updateType(false);
    }
    public void updateAllSpawn() {
        allSpawn.editMeta(meta -> {
            meta.displayName(Component.text("All Mobs", NamedTextColor.GRAY, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Enable/disable ", NamedTextColor.GRAY).append(Component.text("ALL", NamedTextColor.GRAY, TextDecoration.BOLD))
                    .append(Component.text(" mob spawning", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Disclaimer: Will override per mob spawning if disabled!", NamedTextColor.RED));
            lore.add(Component.empty());
            Object allSpawning = StateFlag.State.ALLOW;
            if(region.getFlag(Flags.MOB_SPAWNING) != null) allSpawning = region.getFlag(Flags.MOB_SPAWNING);
            TextColor allSpawnColor = Objects.equals(allSpawning, StateFlag.State.ALLOW) ? NamedTextColor.GREEN : NamedTextColor.RED;
            String allSpawnState = Objects.equals(allSpawning, StateFlag.State.ALLOW) ? "ENABLED" : "DISABLED";
            lore.add(Component.text("Mob spawning is ", allSpawnColor)
                    .append(Component.text(allSpawnState, allSpawnColor, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        });
        inventory.setItem(48, allSpawn);
    }
    public ClaimFlagsMobs(SkyPrisonCore plugin, ClaimData claim, boolean canEdit, boolean hasPurchased) {
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(claim.getWorld()))));
        assert regionManager != null;
        region = regionManager.getRegion(claim.getId());
        this.claim = claim;
        this.canEdit = canEdit;
        this.hasPurchased = hasPurchased;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Mob Spawns", TextColor.fromHexString("#0fc3ff")));

        if(region == null) return;

        List<EntityType> entities = Arrays.stream(EntityType.values()).filter(entity -> entity.isAlive() && entity.isSpawnable()
                && !entity.equals(EntityType.PLAYER) && !entity.equals(EntityType.ARMOR_STAND)).toList();
        Set<com.sk89q.worldedit.world.entity.EntityType> deniedEntities = region.getFlag(Flags.DENY_SPAWN);
        if(deniedEntities != null && !deniedEntities.isEmpty()) {
            deniedMobs.addAll(deniedEntities.stream().map(BukkitAdapter::adapt).toList());
        }
        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        for(EntityType entity : entities) {
            ItemStack mobItem = getMobHead(entity) != null ? hAPI.getItemHead(getMobHead(entity)) : new ItemStack(Material.PLAYER_HEAD);
            boolean isAllowed = !deniedMobs.contains(entity);
            mobItem.editMeta(meta -> {
                String mobName = StringUtils.capitalize(entity.name().toLowerCase().replace("_", " "));
                meta.displayName(Component.text(mobName, isAllowed ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.empty());
                Component mobState = Component.text(isAllowed ? "DISABLE" : "ENABLE", isAllowed ? NamedTextColor.RED : NamedTextColor.GREEN, TextDecoration.BOLD);
                lore.add(Component.text("Click to ", NamedTextColor.GRAY).append(mobState)
                        .append(Component.text(" spawning for ", NamedTextColor.GRAY))
                        .append(Component.text(mobName, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });
            mobs.add(new ClaimFlagMob(mobItem, claim, entity, isAllowed));
        }

        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        typePane.editMeta(meta -> meta.displayName(Component.text(" ")));
        ItemStack returnItem = hAPI.getItemHead("10306");
        returnItem.editMeta(meta -> meta.displayName(Component.text("Return to Flags", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        for(int i = 45; i < 54; i++) {
            if(i == 45) inventory.setItem(i, returnItem);
            else inventory.setItem(i, blackPane);
        }
        updateAllSpawn();
        updateType(false);
    }
    public boolean getIsAllowed() {
        return this.isAllowed;
    }
    public boolean getCanEdit() {
        return this.canEdit;
    }
    public boolean getHasPurchased() {
        return this.hasPurchased;
    }
    public ClaimData getClaim() {
        return this.claim;
    }
    public ProtectedRegion getRegion() {
        return this.region;
    }
    public String getMobHead(EntityType entity) {
        return switch (entity.name().toUpperCase()) {
            case "ELDER_GUARDIAN" -> "25357";
            case "WITHER_SKELETON" -> "22400";
            case "STRAY" -> "3244";
            case "HUSK" -> "3245";
            case "ZOMBIE_VILLAGER" -> "31537";
            case "SKELETON_HORSE" -> "6013";
            case "ZOMBIE_HORSE" -> "2913";
            case "DONKEY" -> "24934";
            case "MULE" -> "38016";
            case "EVOKER" -> "26087";
            case "VEX" -> "3080";
            case "VINDICATOR" -> "28323";
            case "ILLUSIONER" -> "23766";
            case "CREEPER" -> "4169";
            case "SKELETON" -> "8188";
            case "SPIDER" -> "32706";
            case "GIANT" -> "11665";
            case "ZOMBIE" -> "41528";
            case "SLIME" -> "30399";
            case "GHAST" -> "321";
            case "ZOMBIFIED_PIGLIN" -> "36388";
            case "ENDERMAN" -> "318";
            case "CAVE_SPIDER" -> "315";
            case "SILVERFISH" -> "3936";
            case "BLAZE" -> "322";
            case "MAGMA_CUBE" -> "323";
            case "ENDER_DRAGON" -> "53493";
            case "WITHER" -> "32347";
            case "BAT" -> "6607";
            case "WITCH" -> "3864";
            case "ENDERMITE" -> "7375";
            case "GUARDIAN" -> "666";
            case "SHULKER" -> "30627";
            case "PIG" -> "25778";
            case "SHEEP" -> "49688";
            case "COW" -> "335";
            case "CHICKEN" -> "27974";
            case "SQUID" -> "27089";
            case "WOLF" -> "38471";
            case "MUSHROOM_COW" -> "339";
            case "SNOWMAN" -> "342";
            case "OCELOT" -> "340";
            case "IRON_GOLEM" -> "341";
            case "HORSE" -> "1154";
            case "RABBIT" -> "49677";
            case "POLAR_BEAR" -> "6398";
            case "LLAMA" -> "49646";
            case "PARROT" -> "49659";
            case "VILLAGER" -> "30560";
            case "TURTLE" -> "17929";
            case "PHANTOM" -> "18091";
            case "COD" -> "17898";
            case "SALMON" -> "31623";
            case "PUFFERFISH" -> "45707";
            case "TROPICAL_FISH" -> "53856";
            case "DROWNED" -> "15967";
            case "DOLPHIN" -> "16799";
            case "CAT" -> "4167";
            case "PANDA" -> "19438";
            case "PILLAGER" -> "25149";
            case "RAVAGER" -> "28196";
            case "TRADER_LLAMA" -> "53242";
            case "WANDERING_TRADER" -> "25676";
            case "FOX" -> "630";
            case "BEE" -> "31260";
            case "HOGLIN" -> "34783";
            case "PIGLIN" -> "36066";
            case "STRIDER" -> "48212";
            case "ZOGLIN" -> "35932";
            case "PIGLIN_BRUTE" -> "40777";
            case "AXOLOTL" -> "41592";
            case "GLOW_SQUID" -> "47965";
            case "GOAT" -> "45810";
            case "ALLAY" -> "51367";
            case "FROG" -> "51343";
            case "TADPOLE" -> "50682";
            case "WARDEN" -> "47668";
            default -> null;
        };
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
