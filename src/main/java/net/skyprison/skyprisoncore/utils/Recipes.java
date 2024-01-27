package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.ArrayList;
import java.util.List;

public class Recipes {
    public final static List<CraftingRecipe> customRecipes = new ArrayList<>();
    public final static List<Material> blockedRecipes = new ArrayList<>();
    private final SkyPrisonCore plugin;
    public Recipes(SkyPrisonCore plugin) {
        this.plugin = plugin;
        loadRecipes();
        removeRecipes();
        addBlockedRecipes();
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

        // Iron Gear

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_boots"));

        // Diamond Gear

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_boots"));

        // Netherite Gear

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_boots"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_pickaxe"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_axe"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_shovel"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("netherite_hoe"));

        // Misc Items

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("anvil"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("hopper"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("beacon"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("recovery_compass"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("shulker_box"));

        // Netherite Templates

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

        // Trim Templates

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
        blockedRecipes.add(Material.IRON_SWORD);
        blockedRecipes.add(Material.IRON_HELMET);
        blockedRecipes.add(Material.IRON_CHESTPLATE);
        blockedRecipes.add(Material.IRON_LEGGINGS);
        blockedRecipes.add(Material.IRON_BOOTS);

        blockedRecipes.add(Material.DIAMOND_SWORD);
        blockedRecipes.add(Material.DIAMOND_HELMET);
        blockedRecipes.add(Material.DIAMOND_CHESTPLATE);
        blockedRecipes.add(Material.DIAMOND_LEGGINGS);
        blockedRecipes.add(Material.DIAMOND_BOOTS);

        blockedRecipes.add(Material.ANVIL);
        blockedRecipes.add(Material.HOPPER);
        blockedRecipes.add(Material.BEACON);
        blockedRecipes.add(Material.RECOVERY_COMPASS);
        blockedRecipes.add(Material.SHULKER_BOX);
    }
}
