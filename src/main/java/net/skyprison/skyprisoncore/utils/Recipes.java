package net.skyprison.skyprisoncore.utils;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;

public class Recipes {
    public final static List<Recipe> customRecipes = new ArrayList<>();
    private final SkyPrisonCore plugin;
    public Recipes(SkyPrisonCore plugin) {
        this.plugin = plugin;
        loadRecipes();
        removeRecipes();
    }
    private void loadRecipes() {
        ShapedRecipe ironSword = new ShapedRecipe(new NamespacedKey(plugin, "iron_sword"), new ItemStack(Material.IRON_SWORD));
        ironSword.shape("I", "I", "S");
        ironSword.setIngredient('I', Material.IRON_BLOCK);
        ironSword.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(ironSword);
        customRecipes.add(ironSword);
    }
    private void removeRecipes() {
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_sword"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_helmet"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_chestplate"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_leggings"));
        plugin.getServer().removeRecipe(NamespacedKey.minecraft("iron_boots"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("anvil"));

        plugin.getServer().removeRecipe(NamespacedKey.minecraft("diamond_sword"));
    }
}
