package net.skyprison.skyprisoncore.utils.claims;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum AvailableFlags {
    ENTRY(
        Collections.singletonList(Flags.ENTRY),
        "Entry",
        "",
        "Non-members CAN enter",
        "Non-members CAN'T enter",
        "",
        "general",
        Material.BARRIER,
        FlagState.BOOLEAN
    ),
    ITEM_PICKUP(
        Arrays.asList(Flags.ITEM_PICKUP, Flags.ITEM_DROP),
        "Item Pickup",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "general",
        Material.DIAMOND,
        FlagState.BOOLEAN
    ),
    GREET_MESSAGE(
        Collections.singletonList(Flags.GREET_MESSAGE),
        "Greet Message",
        "",
        "",
        "",
        "NOT SET",
        "general",
        Material.WARPED_SIGN,
        FlagState.MESSAGE
    ),
    FAREWELL_MESSAGE(
        Collections.singletonList(Flags.FAREWELL_MESSAGE),
        "Farewell Message",
        "",
        "",
        "",
        "NOT SET",
        "general",
        Material.CRIMSON_SIGN,
        FlagState.MESSAGE
    ),
    FALL_DAMAGE(
        Collections.singletonList(Flags.FALL_DAMAGE),
        "Fall Damage",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.FEATHER,
        FlagState.BOOLEAN
    ),
    PVP(
        Collections.singletonList(Flags.PVP),
        "PvP",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.IRON_SWORD,
        FlagState.BOOLEAN
    ),
    CREEPER_EXPLOSION(
        Collections.singletonList(Flags.CREEPER_EXPLOSION),
        "Creeper Explosions",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.CREEPER_HEAD,
        FlagState.BOOLEAN
    ),
    TNT(
        Collections.singletonList(Flags.TNT),
        "TNT Explosions",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.TNT,
        FlagState.BOOLEAN
    ),
    MOB_DAMAGE(
        Collections.singletonList(Flags.MOB_DAMAGE),
        "Mob Damage",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.ROTTEN_FLESH,
        FlagState.BOOLEAN
    ),
    LAVA_FLOW(
        Collections.singletonList(Flags.LAVA_FLOW),
        "Lava Flow",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.LAVA_BUCKET,
        FlagState.BOOLEAN
    ),
    WATER_FLOW(
        Collections.singletonList(Flags.WATER_FLOW),
        "Water Flow",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.WATER_BUCKET,
        FlagState.BOOLEAN
    ),
    SNOW_MELT(
        Collections.singletonList(Flags.SNOW_MELT),
        "Snow Melt",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.SNOWBALL,
        FlagState.BOOLEAN
    ),
    SNOW_FALL(
        Collections.singletonList(Flags.SNOW_FALL),
        "Snow Fall",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.SNOW,
        FlagState.BOOLEAN
    ),
    ICE_FORM(
        Collections.singletonList(Flags.ICE_FORM),
        "Ice Form",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.ICE,
        FlagState.BOOLEAN
    ),
    ICE_MELT(
        Collections.singletonList(Flags.ICE_MELT),
        "Ice Melt",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.BLUE_ICE,
        FlagState.BOOLEAN
    ),
    LEAF_DECAY(
        Collections.singletonList(Flags.LEAF_DECAY),
        "Leaf Decay",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.OAK_LEAVES,
        FlagState.BOOLEAN
    ),
    GRASS_SPREAD(
        Collections.singletonList(Flags.GRASS_SPREAD),
        "Grass Spread",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.GRASS_BLOCK,
        FlagState.BOOLEAN
    ),
    MYCELIUM_SPREAD(
        Collections.singletonList(Flags.MYCELIUM_SPREAD),
        "Mycelium Spread",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.MYCELIUM,
        FlagState.BOOLEAN
    ),
    VINE_GROWTH(
        Collections.singletonList(Flags.VINE_GROWTH),
        "Vine Growth",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.VINE,
        FlagState.BOOLEAN
    ),
    CHORUS_TELEPORT(
        Collections.singletonList(Flags.CHORUS_TELEPORT),
        "Chorus Teleportation",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "general",
        Material.CHORUS_FRUIT,
        FlagState.BOOLEAN
    ),
    ENDERPEARL(
        Collections.singletonList(Flags.ENDERPEARL),
        "Enderpearls",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "general",
        Material.ENDER_PEARL,
        FlagState.BOOLEAN
    ),
    PLACE_VEHICLES(
        Collections.singletonList(Flags.PLACE_VEHICLE),
        "Place Vehicles",
        "",
        "All players CAN place vehicles",
        "All players CAN'T place vehicles",
        "Only members CAN place vehicles",
        "general",
        Material.MINECART,
        FlagState.BOOLEAN
    ),
    DESTROY_VEHICLE(
        Collections.singletonList(Flags.DESTROY_VEHICLE),
        "Break Vehicles",
        "",
        "All players CAN break vehicles",
        "All players CAN'T break vehicles",
        "Only members CAN break vehicles",
        "general",
        Material.TNT_MINECART,
        FlagState.BOOLEAN
    ),
    ENDER_BUILD(
        Collections.singletonList(Flags.ENDER_BUILD),
        "Enderman Build",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.ENDERMAN_SPAWN_EGG,
        FlagState.BOOLEAN
    ),
    FIRE_SPREAD(
        Collections.singletonList(Flags.FIRE_SPREAD),
        "Fire Spread",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.FLINT_AND_STEEL,
        FlagState.BOOLEAN
    ),
    LIGHTNING(
        Collections.singletonList(Flags.LIGHTNING),
        "Lightning Strikes",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "protection",
        Material.LIGHTNING_ROD,
        FlagState.BOOLEAN
    ),
    CORAL_FADE(
        Collections.singletonList(Flags.CORAL_FADE),
        "Coral Fade",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.BRAIN_CORAL,
        FlagState.BOOLEAN
    ),
    SNOWMAN_TRAILS(
        Collections.singletonList(Flags.SNOWMAN_TRAILS),
        "Snowman Trails",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "terrain",
        Material.SNOW_GOLEM_SPAWN_EGG,
        FlagState.BOOLEAN
    ),
    TRAMPLE_BLOCKS(
        Collections.singletonList(Flags.TRAMPLE_BLOCKS),
        "Crop Trampling",
        "",
        "ENABLED",
        "DISABLED",
        "",
        "purchased",
        Material.WHEAT_SEEDS,
        FlagState.BOOLEAN
    ),
    FLY(
        Collections.singletonList(SkyPrisonCore.FLY),
        "Flight",
        "",
        "ENABLED",
        "DISABLED",
        "DISABLED",
        "purchased",
        Material.ELYTRA,
        FlagState.BOOLEAN
    ),
    GREET_TITLE(
        Collections.singletonList(Flags.GREET_TITLE),
        "Greet Title",
        "",
        "",
        "",
        "NOT SET",
        "purchased",
        Material.WARPED_SIGN,
        FlagState.MESSAGE
    ),
    FAREWELL_TITLE(
        Collections.singletonList(Flags.FAREWELL_TITLE),
        "Farewell Title",
        "",
        "",
        "",
        "NOT SET",
        "purchased",
        Material.CRIMSON_SIGN,
        FlagState.MESSAGE
    ),
    WEATHER_LOCK(
        Collections.singletonList(Flags.WEATHER_LOCK),
        "Weather Lock",
        "",
        "",
        "",
        "NOT SET",
        "purchased",
        Material.WATER_BUCKET,
        FlagState.OTHER
    ),
    TIME_LOCK(
        Collections.singletonList(Flags.TIME_LOCK),
        "Time Lock",
        "",
        "",
        "",
        "NOT SET",
        "purchased",
        Material.CLOCK,
        FlagState.MESSAGE
    );

    public enum FlagState {
        MESSAGE,
        BOOLEAN,
        OTHER
    }

    private final List<Flag<?>> flags;
    private final String title;
    private final String desc;
    private final String allowed;
    private final String denied;
    private final String notSet;
    private final String group;
    private final Material type;
    private final FlagState flagState;

    AvailableFlags(List<Flag<?>> flags, String title, String desc, String allowed, String denied, String notSet, String group, Material type, FlagState flagState) {
        this.flags = flags;
        this.title = title;
        this.desc = desc;
        this.allowed = allowed;
        this.denied = denied;
        this.notSet = notSet;
        this.group = group;
        this.type = type;
        this.flagState = flagState;
    }
    public List<Flag<?>> getFlags() {
        return this.flags;
    }
    public String getTitle() {
        return this.title;
    }
    public String getDescription() {
        return this.desc;
    }
    
    public String getAllowed() {
        return this.allowed;
    }
    
    public String getDenied() {
        return this.denied;
    }
    
    public String getNotSet() {
        return this.notSet;
    }

    public FlagState getFlagState() {
        return this.flagState;
    }
    public String getGroup() {
        return this.group;
    }

    public Material getType() {
        return this.type;
    }
}
