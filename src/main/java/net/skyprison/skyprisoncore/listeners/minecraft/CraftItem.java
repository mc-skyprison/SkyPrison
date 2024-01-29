package net.skyprison.skyprisoncore.listeners.minecraft;

import net.skyprison.skyprisoncore.utils.Recipes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingRecipe;

public class CraftItem implements Listener {
    public CraftItem() {
    }
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if(!(event.getRecipe() instanceof CraftingRecipe recipe) || !Recipes.infoRecipes.contains(recipe.getKey())) return;
        event.setCancelled(true);
    }
}
