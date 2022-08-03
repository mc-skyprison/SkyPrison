package net.skyprison.skyprisoncore.commands;

import net.kyori.adventure.text.Component;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class CustomEnchant implements CommandExecutor {
    private final SkyPrisonCore plugin;

    public CustomEnchant(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public void enchantGUI(Player player) {
        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta whiteMeta = whitePane.getItemMeta();
        whiteMeta.displayName(Component.text(" "));
        whitePane.setItemMeta(whiteMeta);

        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayPane.getItemMeta();
        grayMeta.displayName(Component.text(" "));
        grayPane.setItemMeta(grayMeta);
        Inventory blacksmithInv = Bukkit.createInventory(null, 27, Component.text(plugin.colourMessage("&bBlacksmith")));
        for(int i = 0; i < blacksmithInv.getSize(); i++) {
            if(i <= 12) {
                blacksmithInv.setItem(i, whitePane);
            } else if (i >= 14 && i <= 17) {
                blacksmithInv.setItem(i, whitePane);
            } else if (i > 17 && i != 22) {
                blacksmithInv.setItem(i, grayPane);
            } else {
                ItemStack confirmItem = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta confMeta = confirmItem.getItemMeta();
                confMeta.displayName(Component.text(plugin.colourMessage("&aGive Item Telekinesis")));
                confirmItem.setItemMeta(confMeta);
                blacksmithInv.setItem(i, confirmItem);
            }
        }

        ItemStack blankPane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta blankMeta = blankPane.getItemMeta();

        NamespacedKey key = new NamespacedKey(plugin, "stop-click");
        blankMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 0);
        NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
        blankMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "blacksmith-gui");
        blankPane.setItemMeta(blankMeta);

        blacksmithInv.setItem(0, blankPane);
        player.openInventory(blacksmithInv);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            enchantGUI(player);
        }
        return true;
    }
}
