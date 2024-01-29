package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Recipes {
    public final static List<CraftingRecipe> customRecipes = new ArrayList<>();
    public final static List<NamespacedKey> infoRecipes = new ArrayList<>();
    public final static List<ItemStack> blockedRecipes = new ArrayList<>();
    private final SkyPrisonCore plugin;
    public Recipes(SkyPrisonCore plugin) {
        this.plugin = plugin;
        loadRecipes();
        loadInfoRecipes();
        removeRecipes();
        addBlockedRecipes();
    }

    public static void discoverIronRecipes(Player player) {
        List<NamespacedKey> ironRecipes = customRecipes.stream().filter(recipe -> recipe.getKey().getKey().startsWith("iron")
                || recipe.getKey().getKey().startsWith("anvil")).map(CraftingRecipe::getKey).toList();
        player.discoverRecipes(ironRecipes);
    }

    public static void discoverRecipe(Player player, NamespacedKey key) {
        player.discoverRecipe(key);
    }

    private void loadRecipes() {

        // Iron Gear

        ShapedRecipe ironSword = new ShapedRecipe(new NamespacedKey(plugin, "iron_sword"), new ItemStack(Material.IRON_SWORD));
        ironSword.shape("I", "I", "S");
        ironSword.setIngredient('I', Material.IRON_BLOCK);
        ironSword.setIngredient('S', Material.STICK);
        ironSword.setCategory(CraftingBookCategory.EQUIPMENT);
        plugin.getServer().addRecipe(ironSword);
        customRecipes.add(ironSword);

        ShapedRecipe ironHelmet = new ShapedRecipe(new NamespacedKey(plugin, "iron_helmet"), new ItemStack(Material.IRON_HELMET));
        ironHelmet.shape("III", "I I");
        ironHelmet.setIngredient('I', Material.IRON_BLOCK);
        ironHelmet.setCategory(CraftingBookCategory.EQUIPMENT);
        plugin.getServer().addRecipe(ironHelmet);
        customRecipes.add(ironHelmet);

        ShapedRecipe ironChestplate = new ShapedRecipe(new NamespacedKey(plugin, "iron_chestplate"), new ItemStack(Material.IRON_CHESTPLATE));
        ironChestplate.shape("I I", "III", "III");
        ironChestplate.setIngredient('I', Material.IRON_BLOCK);
        ironChestplate.setCategory(CraftingBookCategory.EQUIPMENT);
        plugin.getServer().addRecipe(ironChestplate);
        customRecipes.add(ironChestplate);

        ShapedRecipe ironLeggings = new ShapedRecipe(new NamespacedKey(plugin, "iron_leggings"), new ItemStack(Material.IRON_LEGGINGS));
        ironLeggings.shape("III", "I I", "I I");
        ironLeggings.setIngredient('I', Material.IRON_BLOCK);
        ironLeggings.setCategory(CraftingBookCategory.EQUIPMENT);
        plugin.getServer().addRecipe(ironLeggings);
        customRecipes.add(ironLeggings);

        ShapedRecipe ironBoots = new ShapedRecipe(new NamespacedKey(plugin, "iron_boots"), new ItemStack(Material.IRON_BOOTS));
        ironBoots.shape("I I", "I I");
        ironBoots.setIngredient('I', Material.IRON_BLOCK);
        ironBoots.setCategory(CraftingBookCategory.EQUIPMENT);
        plugin.getServer().addRecipe(ironBoots);
        customRecipes.add(ironBoots);

        // Misc Items

        ShapedRecipe anvil = new ShapedRecipe(new NamespacedKey(plugin, "anvil"), new ItemStack(Material.ANVIL));
        anvil.shape("III", " I ", "III");
        anvil.setIngredient('I', Material.IRON_BLOCK);
        anvil.setCategory(CraftingBookCategory.MISC);
        plugin.getServer().addRecipe(anvil);
        customRecipes.add(anvil);

        ShapelessRecipe netherWart = new ShapelessRecipe(new NamespacedKey(plugin, "nether_wart"), new ItemStack(Material.NETHER_WART, 9));
        netherWart.addIngredient(Material.NETHER_WART_BLOCK);
        netherWart.setCategory(CraftingBookCategory.MISC);
        plugin.getServer().addRecipe(netherWart);
        customRecipes.add(netherWart);
    }
    private void removeRecipes() {
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_boots"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_boots"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_boots"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_pickaxe"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_axe"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_shovel"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_hoe"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("anvil"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("hopper"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("beacon"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("recovery_compass"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("shulker_box"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_upgrade_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_sword_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_pickaxe_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_axe_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_shovel_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_hoe_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_helmet_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_chestplate_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_leggings_smithing"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_boots_smithing"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("shaper_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("ward_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("silence_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("rib_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("tide_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("eye_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("wild_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("coast_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("wayfinder_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("host_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("raiser_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("snout_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("dune_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("sentry_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("vex_armor_trim_smithing_template"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("spire_armor_trim_smithing_template"));
    }
    private void addBlockedRecipes() {
        ItemStack diamondSword = new ItemStack(Material.DIAMOND_SWORD);
        diamondSword.editMeta(meta -> {
            meta.displayName(Component.text("Diamond Sword", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("(UNOBTAINABLE)", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Can only be used by guards.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        blockedRecipes.add(diamondSword);
        ItemStack diamondHelmet = new ItemStack(Material.DIAMOND_HELMET);
        diamondHelmet.editMeta(meta -> {
            meta.displayName(Component.text("Diamond Helmet", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        blockedRecipes.add(diamondHelmet);
        ItemStack diamondChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        diamondChestplate.editMeta(meta -> {
            meta.displayName(Component.text("Diamond Chestplate", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        blockedRecipes.add(diamondChestplate);
        ItemStack diamondLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        diamondLeggings.editMeta(meta -> {
            meta.displayName(Component.text("Diamond Leggings", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        blockedRecipes.add(diamondLeggings);
        ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
        diamondBoots.editMeta(meta -> {
            meta.displayName(Component.text("Diamond Boots", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        blockedRecipes.add(diamondBoots);
        ItemStack hopper = new ItemStack(Material.HOPPER);
        hopper.editMeta(meta -> {
            meta.displayName(Component.text("Hopper", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop, Vote/Event Crate & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        blockedRecipes.add(hopper);
        ItemStack beacon = new ItemStack(Material.BEACON);
        beacon.editMeta(meta -> {
            meta.displayName(Component.text("Beacon", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop, Vote/Event Crate & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        blockedRecipes.add(beacon);
        ItemStack recoveryCompass = new ItemStack(Material.RECOVERY_COMPASS);
        recoveryCompass.editMeta(meta -> {
            meta.displayName(Component.text("Recovery Compass", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from Vote Crate", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        blockedRecipes.add(recoveryCompass);
        ItemStack shulkerBox = new ItemStack(Material.SHULKER_BOX);
        shulkerBox.editMeta(meta -> {
            meta.displayName(Component.text("Shulker Box", NamedTextColor.WHITE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Acquired from /tshop, Vote Crate & Citizens+ Slots", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        blockedRecipes.add(shulkerBox);
    }
    private void loadInfoRecipes() {
        addInfoRecipe("iron_sword", "I", "I", "S");
        addInfoRecipe("iron_helmet", "III", "I I");
        addInfoRecipe("iron_chestplate", "I I", "III", "III");
        addInfoRecipe("iron_leggings", "III", "I I", "I I");
        addInfoRecipe("iron_boots", "I I", "I I");

        addInfoRecipe("diamond_sword", "D", "D", "S");
        addInfoRecipe("diamond_helmet", "DDD", "D D");
        addInfoRecipe("diamond_chestplate", "D D", "DDD", "DDD");
        addInfoRecipe("diamond_leggings", "DDD", "D D", "D D");
        addInfoRecipe("diamond_boots", "D D", "D D");

        addInfoRecipe("anvil", "BBB", " I ", "III");
        addInfoRecipe("hopper", "I I", "ICI", " I ");
        addInfoRecipe("beacon", "GGG", "GNG", "OOO");
        addInfoRecipe("recovery_compass", "EEE", "ECE", "EEE");
        addInfoRecipe("shulker_box", "L", "C", "L");
    }
    private void addInfoRecipe(String keyName, String... shape) {
        ItemStack infoItem = new ItemStack(Material.DIRT);
        infoItem.editMeta(meta -> {
            meta.displayName(Component.text("Recipe Removed!", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("This recipe has been removed.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Check /customrecipes for alternatives.", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        NamespacedKey key = new NamespacedKey(plugin, "info_" + keyName);
        ShapedRecipe infoRecipe = new ShapedRecipe(key, infoItem);
        infoRecipe.shape(shape);
        if (Arrays.stream(shape).anyMatch(s -> s.contains("I"))) {
            infoRecipe.setIngredient('I', Material.IRON_INGOT);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("B"))) {
            infoRecipe.setIngredient('B', Material.IRON_BLOCK);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("D"))) {
            infoRecipe.setIngredient('D', Material.DIAMOND);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("S"))) {
            infoRecipe.setIngredient('S', Material.STICK);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("C"))) {
            infoRecipe.setIngredient('C', Material.CHEST);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("L"))) {
            infoRecipe.setIngredient('L', Material.SHULKER_SHELL);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("G"))) {
            infoRecipe.setIngredient('G', Material.GLASS);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("O"))) {
            infoRecipe.setIngredient('O', Material.OBSIDIAN);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("N"))) {
            infoRecipe.setIngredient('N', Material.NETHER_STAR);
        }
        if (Arrays.stream(shape).anyMatch(s -> s.contains("E"))) {
            infoRecipe.setIngredient('E', Material.ECHO_SHARD);
        }
        plugin.getServer().addRecipe(infoRecipe);
        infoRecipes.add(key);
    }
}
