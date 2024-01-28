package net.skyprison.skyprisoncore.inventories.recipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.Recipes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlockedRecipes implements CustomInventory {
    private final Inventory inventory;
    public BlockedRecipes() {
        inventory = Bukkit.getServer().createInventory(this, 54, Component.text("Recipes - Blocked", TextColor.fromHexString("#0fc3ff")));

        int b = 0;
        List<Material> blockedRecipes = new ArrayList<>(Recipes.blockedRecipes);
        for(int i = 0; i < inventory.getSize(); i++) {
            if(i == 45) {
                ItemStack back = new ItemStack(Material.PAPER);
                back.editMeta(meta -> meta.displayName(Component.text("Back to Main Page", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)));
                inventory.setItem(i, back);
            } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 53) {
                ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
                inventory.setItem(i, redPane);
            } else if (i < 8 || i > 45 && i < 53) {
                ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
                inventory.setItem(i, blackPane);
            } else {
                if(blockedRecipes.size() > b) {
                    Material blocked = blockedRecipes.get(b);
                    inventory.setItem(i, new ItemStack(blocked));
                    b++;
                }
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
