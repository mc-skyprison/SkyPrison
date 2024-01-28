package net.skyprison.skyprisoncore.inventories.recipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class CustomRecipe implements CustomInventory {
    private final Inventory inventory;
    public CustomRecipe(CraftingRecipe recipe) {
        ItemStack result = recipe.getResult();
        inventory = Bukkit.getServer().createInventory(this, 45, Component.text("Recipe - ", TextColor.fromHexString("#0fc3ff")).append(result.displayName()));

        if (recipe instanceof ShapedRecipe shapedRecipe) {
            Map<Character, RecipeChoice> ingredientMap = shapedRecipe.getChoiceMap();
            String[] shape = shapedRecipe.getShape();
            int slotIndex = 11;
            for (String row : shape) {
                for (char ingredientChar : row.toCharArray()) {
                    RecipeChoice choice = ingredientMap.get(ingredientChar);
                    if (choice instanceof RecipeChoice.MaterialChoice matChoice) {
                        inventory.setItem(slotIndex, matChoice.getItemStack());
                    }
                    slotIndex++;
                }
                slotIndex += 7;
            }
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            int slotIndex = 11;
            for (RecipeChoice choice : shapelessRecipe.getChoiceList()) {
                if (choice instanceof RecipeChoice.MaterialChoice matChoice) {
                    inventory.setItem(slotIndex, matChoice.getItemStack());
                    slotIndex++;
                    if (slotIndex == 14 || slotIndex == 23) slotIndex += 7;
                }
            }
        }

        for(int i = 0; i < inventory.getSize(); i++) {
            if(i == 35) {
                ItemStack back = new ItemStack(Material.PAPER);
                back.editMeta(meta -> meta.displayName(Component.text("Back to Custom Recipes", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, back);
            } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 36) {
                ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
                inventory.setItem(i, redPane);
            } else if (i >= 11 && i <= 13 || i >= 20 && i <= 22 || i >= 29 && i <= 31) {
            } else if (i == 24) {
                inventory.setItem(i, recipe.getResult());
            } else {
                ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
                inventory.setItem(i, blackPane);
            }
        }
    }
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
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
        return 1;
    }
}
