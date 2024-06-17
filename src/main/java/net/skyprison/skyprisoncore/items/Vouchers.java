package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

public class Vouchers {
    public static ItemStack getVoucherFromType(SkyPrisonCore plugin, String type, int amount) {
        ItemStack voucherItem = null;
        switch(type.toLowerCase()) {
            case "token-shop" -> voucherItem = Vouchers.getTokenShopVoucher(plugin, amount);
            case "mine-reset" -> voucherItem = Vouchers.getMineResetVoucher(plugin, amount);
            case "single-use-enderchest" -> voucherItem = Vouchers.getSingleUseEnderchest(plugin, amount);
        }
        return voucherItem;
    }
    public static ItemStack getTokenShopVoucher(SkyPrisonCore plugin, int amount) {
        ItemStack voucher = new ItemStack(Material.PAPER, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        vMeta.displayName(Component.text( "Token Shop Voucher", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Can be used instead of tokens to buy", NamedTextColor.GRAY));
        lore.add(Component.text("items & perks from the Token Shop.", NamedTextColor.GRAY));
        vMeta.addEnchant(Enchantment.FEATHER_FALLING, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "voucher");
        vouchData.set(key, PersistentDataType.STRING, "token-shop");
        voucher.setItemMeta(vMeta);
        return voucher;
    }
    public static ItemStack getMineResetVoucher(SkyPrisonCore plugin, int amount) {
        ItemStack voucher = new ItemStack(Material.PAPER, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        vMeta.displayName(Component.text( "Mine Reset Voucher", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Use on a cooldown Mine Reset", NamedTextColor.GRAY));
        lore.add(Component.text("sign to instantly reset the mine.", NamedTextColor.GRAY));
        vMeta.addEnchant(Enchantment.FEATHER_FALLING, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "voucher");
        vouchData.set(key, PersistentDataType.STRING, "mine-reset");
        voucher.setItemMeta(vMeta);
        return voucher;
    }
    public static ItemStack getSingleUseEnderchest(SkyPrisonCore plugin, int amount) {
        ItemStack voucher = new ItemStack(Material.ENDER_CHEST, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        vMeta.displayName(Component.text( "Single-Use Enderchest", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Right click to open your ender chest!", NamedTextColor.GRAY));
        vMeta.addEnchant(Enchantment.FEATHER_FALLING, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "voucher");
        vouchData.set(key, PersistentDataType.STRING, "single-use-enderchest");
        voucher.setItemMeta(vMeta);
        return voucher;
    }
}
