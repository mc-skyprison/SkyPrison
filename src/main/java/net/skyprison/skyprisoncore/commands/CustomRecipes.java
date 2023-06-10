package net.skyprison.skyprisoncore.commands;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.AbstractRecipeShaped;
import me.wolfyscript.customcrafting.recipes.CraftingRecipe;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.RecipeType;
import me.wolfyscript.customcrafting.recipes.items.Ingredient;
import me.wolfyscript.customcrafting.recipes.settings.AdvancedRecipeSettings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomRecipes implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public CustomRecipes(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }


    public void openMainGUI(Player player) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        Inventory recipeGUI = Bukkit.createInventory(null, 27, Component.text("Recipes - Main").color(TextColor.fromHexString("#3B5998")));
        for(int i = 0; i < recipeGUI.getSize(); i++) {
            if(i == 0) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                redMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                redMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "recipes-main");
                redPane.setItemMeta(redMeta);
                recipeGUI.setItem(i, redPane);
            } else if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26) {
                recipeGUI.setItem(i, redPane);
            } else if(i < 8 || i > 18 && i < 26) {
                recipeGUI.setItem(i, blackPane);
            } else if(i == 12) {
                ItemStack disabled = new ItemStack(Material.BARRIER, 1);
                ItemMeta dMeta = disabled.getItemMeta();
                dMeta.displayName(Component.text("Blocked Recipes", TextColor.fromHexString("#e51b1e")).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Click here to see all blocked recipes", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                dMeta.lore(lore);
                disabled.setItemMeta(dMeta);
                recipeGUI.setItem(i, disabled);
            } else if(i == 14) {
                ItemStack custom = new ItemStack(Material.KNOWLEDGE_BOOK, 1);
                ItemMeta cMeta = custom.getItemMeta();
                cMeta.displayName(Component.text("Custom Recipes", TextColor.fromHexString("#50C878")).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Click here to see all custom recipes", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                cMeta.lore(lore);
                custom.setItemMeta(cMeta);
                recipeGUI.setItem(i, custom);
            }
        }
        player.openInventory(recipeGUI);
    }

    public void openCustomGUI(Player player) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        Inventory recipeGUI = Bukkit.createInventory(null, 54, Component.text("Recipes - Custom").color(TextColor.fromHexString("#3B5998")));
        List<CustomRecipe<?>> recipes = CustomCrafting.inst().getRegistries().getRecipes().getAvailable();

        int b = 0;
        for(int i = 0; i < recipeGUI.getSize(); i++) {
            if(i == 0) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                redMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                redMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "recipes-custom");
                redPane.setItemMeta(redMeta);
                recipeGUI.setItem(i, redPane);
            } else if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 53) {
                recipeGUI.setItem(i, redPane);
            } else if(i < 8 || i > 45 && i < 53) {
                recipeGUI.setItem(i, blackPane);
            } else if(i == 45) {
                ItemStack back = new ItemStack(Material.PAPER, 1);
                ItemMeta backMeta = back.getItemMeta();
                backMeta.displayName(Component.text("Back to Main Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Click here to go back to main recipes page", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                backMeta.lore(lore);
                back.setItemMeta(backMeta);
                recipeGUI.setItem(i, back);
            } else {
                if(recipes.size() > b) {
                    CustomRecipe<?> recipe = recipes.get(b);
                    ItemStack item = recipe.getResult().getItemStack();
                    ItemMeta iMeta = item.getItemMeta();
                    NamespacedKey key1 = new NamespacedKey(plugin, "custom-recipe");
                    iMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, recipe.getNamespacedKey().toString());
                    item.setItemMeta(iMeta);
                    item.setAmount(1);
                    recipeGUI.setItem(i, item);
                    b++;
                }
            }
        }
        player.openInventory(recipeGUI);
    }

    public void openSpecificGUI(Player player, String recipeStringKey) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        Inventory recipeGUI = Bukkit.createInventory(null, 45, Component.text("Recipes - Custom").color(TextColor.fromHexString("#3B5998")));
        for(int i = 0; i < recipeGUI.getSize(); i++) {
            if(i == 0) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                redMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                redMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "recipes-specific");
                redPane.setItemMeta(redMeta);
                recipeGUI.setItem(i, redPane);
            } else if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 44) {
                recipeGUI.setItem(i, redPane);
            } else if(i < 8 || i > 36 && i < 44 || i == 10 || i > 13 && i < 17 || i == 19 || i == 23 || i == 25 || i == 28 || i > 31 && i < 35) {
                recipeGUI.setItem(i, blackPane);
            } else if(i == 36) {
                ItemStack back = new ItemStack(Material.PAPER, 1);
                ItemMeta backMeta = back.getItemMeta();
                backMeta.displayName(Component.text("Back to Custom Recipes", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Click here to go back to all custom recipes").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                backMeta.lore(lore);
                back.setItemMeta(backMeta);
                recipeGUI.setItem(i, back);
            }
        }

        me.wolfyscript.utilities.util.NamespacedKey recipeKey = me.wolfyscript.utilities.util.NamespacedKey.of(recipeStringKey);

        CraftingRecipe<?, AdvancedRecipeSettings> recipe = (CraftingRecipe<?, AdvancedRecipeSettings>) CustomCrafting.inst().getRegistries().getRecipes().get(recipeKey);

        List<Integer> ingPos = new ArrayList<>(Arrays.asList(11, 12, 13, 20, 21, 22, 29, 30, 31));
        if(recipe.getRecipeType().getType().equals(RecipeType.Type.CRAFTING_SHAPED)) {
            AbstractRecipeShaped<?,?> shapedRecipe = (AbstractRecipeShaped<?, ?>) recipe;
            String[] shape = shapedRecipe.getShape();
            ingPos = new ArrayList<>();
            for(String shapeRow : shape) {
                String[] shapeSlots = shapeRow.split("");
                for(String shapeSlot : shapeSlots) {
                    switch (shapeSlot.toLowerCase()) {
                        case "a" -> ingPos.add(11);
                        case "b" -> ingPos.add(12);
                        case "c" -> ingPos.add(13);
                        case "d" -> ingPos.add(20);
                        case "e" -> ingPos.add(21);
                        case "f" -> ingPos.add(22);
                        case "g" -> ingPos.add(29);
                        case "h" -> ingPos.add(30);
                        case "i" -> ingPos.add(31);
                    }
                }
            }
        }
        int i = 0;
        for (Ingredient ing : recipe.getIngredients()) {
            if(!ing.getItemStack().getType().isAir()) {
                recipeGUI.setItem(ingPos.get(i), ing.getItemStack());
                i++;
            }
        }

        recipeGUI.setItem(24, recipe.getResult().getItemStack());
        player.openInventory(recipeGUI);
    }

    public void openDisabledGUI(Player player) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        Inventory recipeGUI = Bukkit.createInventory(null, 54, Component.text("Recipes - Blocked").color(TextColor.fromHexString("#3B5998")));
        List<Recipe> disabledRecipes = CustomCrafting.inst().getDisableRecipesHandler().getCachedVanillaRecipes();
        List<CustomRecipe<?>> customRecipes = CustomCrafting.inst().getRegistries().getRecipes().getAvailable();

        List<Recipe> recipes = new ArrayList<>();
        List<ItemStack> customStacks = new ArrayList<>();
        customRecipes.forEach(cr -> customStacks.add(cr.getResult().getItemStack()));
        disabledRecipes.forEach(dc -> {
            if(!customStacks.contains(dc.getResult()) && !dc.getResult().getType().name().startsWith("NETHERITE_")) recipes.add(dc);
        });

        int b = 0;
        for(int i = 0; i < recipeGUI.getSize(); i++) {
            if(i == 0) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                redMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                redMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "recipes-disabled");
                redPane.setItemMeta(redMeta);
                recipeGUI.setItem(i, redPane);
            } else if(i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 53) {
                recipeGUI.setItem(i, redPane);
            } else if(i < 8 || i > 45 && i < 53) {
                recipeGUI.setItem(i, blackPane);
            } else if(i == 45) {
                ItemStack back = new ItemStack(Material.PAPER, 1);
                ItemMeta backMeta = back.getItemMeta();
                backMeta.displayName(Component.text("Back to Main Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Click here to go back to main recipes page", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                backMeta.lore(lore);
                back.setItemMeta(backMeta);
                recipeGUI.setItem(i, back);
            } else {
                if(recipes.size() > b) {
                    Recipe recipe = recipes.get(b);
                    ItemStack item = recipe.getResult();
                    item.setAmount(1);

                    recipeGUI.setItem(i, item);
                    b++;
                }
            }
        }
        player.openInventory(recipeGUI);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player player) {
            openMainGUI(player);
        }
        return true;
    }
}
