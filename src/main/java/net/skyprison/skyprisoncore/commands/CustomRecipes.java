package net.skyprison.skyprisoncore.commands;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import net.kyori.adventure.text.Component;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CustomRecipes implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public CustomRecipes(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        Inventory recipeGUI = Bukkit.createInventory(null, 54, Component.text(plugin.colourMessage("&cCustom Recipes")));
        List<CustomRecipe<?>> recipes = CustomCrafting.inst().getRegistries().getRecipes().getAvailable();
        int i = 0;
        for(CustomRecipe<?> recipe : recipes) {
            ItemStack item = recipe.getResult().getItemStack();
            item.setAmount(1);
            recipeGUI.setItem(i, item);
            i++;
        }
        player.openInventory(recipeGUI);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            openGUI(player);
        }
        return true;
    }
}
