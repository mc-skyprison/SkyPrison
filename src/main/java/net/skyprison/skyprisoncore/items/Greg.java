package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Random;

public class Greg {
    public static ItemStack getItemFromType(SkyPrisonCore plugin, String type, int amount) {
        ItemStack item = null;
        switch(type.toLowerCase()) {
            case "grease" -> item = Greg.getGrease(plugin, amount);
            case "allay-dust" -> item = Greg.getAllayDust(plugin, amount);
            case "strength" -> item = Greg.getPotion(plugin, "Potion of Strength", "strength", false, amount);
            case "speed" -> item = Greg.getPotion(plugin, "Potion of Swiftness", "speed", false, amount);
            case "fire-resistance" -> item = Greg.getPotion(plugin, "Potion of Fire Resistance", "fire_resistance", false, amount);
            case "instant-health" -> item = Greg.getPotion(plugin, "Potion of Healing", "instant_heal", false, amount);
            case "instant-damage" -> item = Greg.getPotion(plugin, "Splash Potion of Harming", "instant_damage", true, amount);
        }
        return item;
    }
    public static ItemStack getGrease(SkyPrisonCore plugin, int amount) {
        ItemStack voucher = new ItemStack(Material.GREEN_DYE, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        vMeta.displayName(Component.text( "Grease", TextColor.fromHexString("#569041"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Greg told me to smear this on the", NamedTextColor.GRAY));
        lore.add(Component.text("enchant table to make it work properly.", NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("I don't think I should ask where he got it from.", NamedTextColor.DARK_GRAY));
        vMeta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "greg");
        vouchData.set(key, PersistentDataType.STRING, "grease");
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
        NamespacedKey key = new NamespacedKey(plugin, "greg");
        vouchData.set(key, PersistentDataType.STRING, "allay-dust");
        voucher.setItemMeta(vMeta);
        return voucher;
    }
    public static ItemStack getPotion(SkyPrisonCore plugin, String name, String type, boolean splash, int amount) {
        ItemStack voucher = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION, amount);
        PotionMeta vMeta = (PotionMeta) voucher.getItemMeta();
        vMeta.displayName(Component.text(name, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> lore = new ArrayList<>();
        lore.add(Component.text("Unknown Effects", TextColor.fromHexString("#e7ebe0")));
        lore.add(Component.text("                  ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        lore.add(Component.text("Greg Inc is not liable for", NamedTextColor.DARK_GRAY));
        lore.add(Component.text("any side effects. Sold as is.", NamedTextColor.DARK_GRAY));
        PotionType potionType = PotionType.valueOf(type.toUpperCase());
        vMeta.setColor(getColour(type));
        vMeta.addCustomEffect(getRandomizedEffect(potionType.getEffectType()), true);
        vMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "greg");
        boolean works = new Random().nextFloat() < 0.9;
        boolean onYou;
        if(splash) {
            onYou = new Random().nextFloat() >= 0.98;
        } else {
            onYou = new Random().nextFloat() < 0.98;
        }
        vouchData.set(key, PersistentDataType.STRING, works + ";" + onYou);
        voucher.setItemMeta(vMeta);
        return voucher;
    }
    private static Color getColour(String type) {
        switch (type) {
            case "strength" -> {
                return Color.fromRGB(252, 197, 0);
            }
            case "speed" -> {
                return Color.AQUA;
            }
            case "fire_resistance" -> {
                return Color.ORANGE;
            }
            case "instant_heal" -> {
                return Color.RED;
            }
            case "instant_damage" -> {
                return Color.fromRGB(167, 100, 105);
            }
        }
        return Color.RED;
    }
    private static int biasedRandom(int min, int max) {
        Random r = new Random();
        double rawRandom = r.nextDouble();
        double biasedRandom = 1 - Math.sqrt(rawRandom);
        return (int) (min + (biasedRandom * (max - min)));
    }
    private static PotionEffect getRandomizedEffect(PotionEffectType effectType) {
        int amplifier = biasedRandom(0, 2);
        int duration;
        if(amplifier == 0) {
            duration = biasedRandom(200, 5000);
        } else {
            duration = biasedRandom(200, 1800);
        }
        return effectType.createEffect(duration, amplifier);
    }
}
