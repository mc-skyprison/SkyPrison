package net.skyprison.skyprisoncore.inventories;

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
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimFlagsMobs implements CustomInventory {

    private final Inventory inventory;

    private final String category;

    private final boolean isAllowed;

    private final boolean canEdit;

    private final String claimId;

    private final String world;

    private final int page;

    public ClaimFlagsMobs(SkyPrisonCore plugin, String claimId, String world, boolean canEdit, boolean isAllowed, String category, int page) {
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
        this.isAllowed = isAllowed;
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Mob Spawns", TextColor.fromHexString("#0fc3ff")));
        ItemStack redPane = new ItemStack(isAllowed ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextMeta);
        ItemStack prevPage = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        prevPage.setItemMeta(prevMeta);

        List<EntityType> mobs = new ArrayList<>();
        List<EntityType> deniedMobs = new ArrayList<>();
        Set<com.sk89q.worldedit.world.entity.EntityType> deniedEntities = region.getFlag(Flags.DENY_SPAWN);
        if(deniedEntities != null && !deniedEntities.isEmpty()) {
            deniedEntities.forEach(entity -> deniedMobs.add(BukkitAdapter.adapt(entity)));
        }
        if(isAllowed) {
            EntityType[] allEntities = EntityType.values();
            for (EntityType entity : allEntities) {
                if(!deniedMobs.contains(entity) && entity.isAlive() && entity.isSpawnable() && !entity.equals(EntityType.PLAYER) && !entity.equals(EntityType.ARMOR_STAND))
                    mobs.add(entity);
            }
        } else {
            mobs = deniedMobs;
        }

        int totalPages = (int) Math.ceil((double) mobs.size() / 36);

        if(page > totalPages) {
            page = 1;
        }
        int toRemove = 36 * (page - 1);
        if(toRemove != 0) {
            mobs = mobs.subList(toRemove, mobs.size()-1);
        }

        Iterator<EntityType> mobsIterate = mobs.iterator();

        TextColor mobColor = !isAllowed ? NamedTextColor.GREEN : NamedTextColor.RED;
        Component mobState = Component.text(!isAllowed ? "ENABLE" : "DISABLE", mobColor, TextDecoration.BOLD);
        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        for(int i = 0; i < this.inventory.getSize();i++) {
            if (i == 47 && page != 1) {
                this.inventory.setItem(i, prevPage);
            } else if (i == 51 && page != totalPages) {
                this.inventory.setItem(i, nextPage);
            } else if (i == 45) {
                ItemStack returnItem = hAPI.getItemHead("10306");
                ItemMeta returnMeta = returnItem.getItemMeta();
                TextColor color = NamedTextColor.GRAY;
                returnMeta.displayName(Component.text("Return to Flags", color).decoration(TextDecoration.ITALIC, false));
                returnItem.setItemMeta(returnMeta);
                this.inventory.setItem(i, returnItem);
            } else if (i == 48) {
                ItemStack allItem = new ItemStack(Material.SPAWNER);
                ItemMeta allMeta = allItem.getItemMeta();
                TextColor color = NamedTextColor.GRAY;
                allMeta.displayName(Component.text("All Mobs", color).decoration(TextDecoration.ITALIC, false));

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
                allMeta.lore(lore);

                allMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

                allItem.setItemMeta(allMeta);
                this.inventory.setItem(i, allItem);
            } else if (i == 49) {
                ItemStack returnItem = new ItemStack(isAllowed ? Material.LIME_CONCRETE : Material.RED_CONCRETE);
                ItemMeta returnMeta = returnItem.getItemMeta();
                returnMeta.displayName(Component.text("Switch view to ", NamedTextColor.GRAY)
                        .append(Component.text(!isAllowed ? "ENABLED" : "DISABLED", mobColor, TextDecoration.BOLD))
                        .append(Component.text(" mob spawns", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));

                returnItem.setItemMeta(returnMeta);
                this.inventory.setItem(i, returnItem);
            }
            else if (i > 35 && i < 45) {
                this.inventory.setItem(i, redPane);
            } else if (i > 45 && i < 54) {
                this.inventory.setItem(i, blackPane);
            } else {
                if (mobsIterate.hasNext()) {
                    EntityType mob = mobsIterate.next();
                    ItemStack mobItem = getMobHead(mob) != null ? hAPI.getItemHead(getMobHead(mob)) : new ItemStack(Material.PLAYER_HEAD);
                    ItemMeta mobMeta = mobItem.getItemMeta();

                    String mobName = WordUtils.capitalize(mob.name().toLowerCase().replace("_", " "));
                    mobMeta.displayName(Component.text(mobName, mobColor, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text(""));
                    lore.add(Component.text("Click to ", NamedTextColor.GRAY).append(mobState)
                            .append(Component.text(" spawning for ", NamedTextColor.GRAY))
                            .append(Component.text(mobName, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
                    mobMeta.lore(lore);

                    NamespacedKey mobKey = new NamespacedKey(plugin, "mob");
                    mobMeta.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, mob.name());

                    mobItem.setItemMeta(mobMeta);
                    this.inventory.setItem(i, mobItem);
                }
            }
        }
    }

    public String getNextCategory(String category) {
        String nextCat = "";
        switch (category) {
            case "" -> nextCat = "monsters";
            case "monsters" -> nextCat = "animals";
            case "animals" -> nextCat = "underwater";
            case "underwater" -> nextCat = "";
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
    public boolean getIsAllowed() {
        return this.isAllowed;
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
}
