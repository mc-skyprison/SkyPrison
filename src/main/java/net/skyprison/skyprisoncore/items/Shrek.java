package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public class Shrek {
    public static ItemStack getItemFromType(SkyPrisonCore plugin, String type, int amount) {
        ItemStack item = null;
        switch(type.toLowerCase()) {
            case "shrek-grease" -> item = Shrek.getShrekGrease(plugin, amount);
            case "allay-dust" -> item = Shrek.getAllayDust(plugin, amount);
        }
        return item;
    }
    public static ItemStack getShrekGrease(SkyPrisonCore plugin, int amount) {
        ItemStack voucher = new ItemStack(Material.GREEN_DYE, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        vMeta.displayName(Component.text( "Grease", TextColor.fromHexString("<#569041>"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Shrek told me to smear this on the", NamedTextColor.GRAY));
        lore.add(Component.text("enchant table to make it work properly.", NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("I don't think I should ask where he got it from.", NamedTextColor.DARK_GRAY));
        vMeta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "shrek");
        vouchData.set(key, PersistentDataType.STRING, "shrek-grease");
        voucher.setItemMeta(vMeta);
        return voucher;
    }
    public static ItemStack getAllayDust(SkyPrisonCore plugin, int amount) {
        ItemStack voucher = new ItemStack(Material.LAPIS_LAZULI, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        vMeta.displayName(Component.text( "Allay Dust", TextColor.fromHexString("#2DD4DC"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Ecologically Sourced Allay Powder", NamedTextColor.GRAY));
        vMeta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "shrek");
        vouchData.set(key, PersistentDataType.STRING, "allay-dust");
        voucher.setItemMeta(vMeta);
        return voucher;
    }
}
