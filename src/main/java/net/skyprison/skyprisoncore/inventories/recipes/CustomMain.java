package net.skyprison.skyprisoncore.inventories.recipes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomMain implements CustomInventory {
    private final Inventory inventory;
    public CustomMain() {
        inventory = Bukkit.getServer().createInventory(this, 27, Component.text("Recipes - Main", TextColor.fromHexString("#0fc3ff")));

        for(int i = 0; i < inventory.getSize(); i++) {
            if (i == 12) {
                ItemStack blocked = new ItemStack(Material.BARRIER);
                blocked.editMeta(meta -> {
                    meta.displayName(Component.text("Blocked Recipes", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    meta.lore(List.of(Component.text("Click to view blocked recipes", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                });
                inventory.setItem(i, blocked);
            } else if (i == 14) {
                ItemStack custom = new ItemStack(Material.BARRIER);
                custom.editMeta(meta -> {
                    meta.displayName(Component.text("Blocked Recipes", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    meta.lore(List.of(Component.text("Click to view blocked recipes", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                });
                inventory.setItem(i, custom);
            } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26) {
                ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
                inventory.setItem(i, redPane);
            } else if (i < 8 || i > 45 && i < 53) {
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
