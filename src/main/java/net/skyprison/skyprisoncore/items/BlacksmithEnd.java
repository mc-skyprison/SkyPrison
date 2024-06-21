package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BlacksmithEnd {
    public static ItemStack getItemFromType(SkyPrisonCore plugin, String type, String extraData, int amount) {
        ItemStack item = null;
        switch(type.toLowerCase()) {
            case "upgrade-template" -> item = BlacksmithEnd.getUpgradeTemplate(plugin, extraData, amount);
            case "reset-repair" -> item = BlacksmithEnd.getRepairCostReset(plugin, amount);
            case "keep-enchants" -> item = BlacksmithEnd.getKeepEnchants(plugin, amount);
            case "keep-trims" -> item = BlacksmithEnd.getKeepTrims(plugin, amount);
        }
        return item;
    }
    public static ItemStack getUpgradeTemplate(SkyPrisonCore plugin, String type, int amount) {
        ItemStack upgrade = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, amount);
        upgrade.editMeta(meta -> {
            meta.displayName(Component.text(StringUtils.capitalize(type) + " Template", TextColor.fromHexString("#09f755"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-upgrade");
            axePers.set(upgradeKey, PersistentDataType.STRING, type);
        });
        return upgrade;
    }
    public static ItemStack getRepairCostReset(SkyPrisonCore plugin, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        upgrade.editMeta(meta -> {
            meta.displayName(Component.text("Netherite Addon", TextColor.fromHexString("#9131a0"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Reset Repair Cost", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
            axePers.set(upgradeKey, PersistentDataType.STRING, "reset-repair");
        });
        return upgrade;
    }
    public static ItemStack getKeepEnchants(SkyPrisonCore plugin, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        upgrade.editMeta(meta -> {
            meta.displayName(Component.text("Netherite Addon", TextColor.fromHexString("#9131a0"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Keep Enchants", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
            axePers.set(upgradeKey, PersistentDataType.STRING, "keep-enchants");
        });
        return upgrade;
    }
    public static ItemStack getKeepTrims(SkyPrisonCore plugin, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        upgrade.editMeta(meta -> {
            meta.displayName(Component.text("Netherite Addon", TextColor.fromHexString("#9131a0"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Keep Armour Trims", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
            axePers.set(upgradeKey, PersistentDataType.STRING, "keep-trims");
        });
        return upgrade;
    }
}
