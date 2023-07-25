package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TreeFeller {
    public static ItemStack getAxe(SkyPrisonCore plugin, int amount) {
        ItemStack axe = new ItemStack(Material.IRON_AXE, amount);
        ItemMeta axeMeta = axe.getItemMeta();
        axeMeta.displayName(Component.text("Treefeller Axe", TextColor.fromHexString("#adc17a")).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Cooldown: ", NamedTextColor.GRAY).append(Component.text("10s", TextColor.fromHexString("#c4c6c5"))).decoration(TextDecoration.ITALIC, false));
        axeMeta.lore(lore);
        PersistentDataContainer axePers = axeMeta.getPersistentDataContainer();
        NamespacedKey treefellerKey = new NamespacedKey(plugin, "treefeller");
        NamespacedKey treefellerCooldownKey = new NamespacedKey(plugin, "treefeller-cooldown");
        axePers.set(treefellerKey, PersistentDataType.INTEGER, 1);
        axePers.set(treefellerCooldownKey, PersistentDataType.INTEGER, 10);
        axe.setItemMeta(axeMeta);
        return axe;
    }

    public static ItemStack getUpgradedAxe(SkyPrisonCore plugin, ItemStack left, ItemStack right) {
        ItemMeta leftMeta = left.getItemMeta();
        PersistentDataContainer leftPers = leftMeta.getPersistentDataContainer();
        ItemStack axe;
        ItemStack upgradeItem;

        NamespacedKey treefellerKey = new NamespacedKey(plugin, "treefeller");
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "treefeller-upgrade");
        if(leftPers.has(treefellerKey)) {
            axe = left.clone();
        } else {
            axe = right.clone();
        }
        ItemMeta axeMeta = axe.getItemMeta();
        PersistentDataContainer axePers = axeMeta.getPersistentDataContainer();

        if(leftPers.has(upgradeKey)) {
            upgradeItem = left.clone();
        } else {
            upgradeItem = right.clone();
        }

        PersistentDataContainer upgradePers = upgradeItem.getPersistentDataContainer();
        String type = upgradePers.get(upgradeKey, PersistentDataType.STRING);

        switch (Objects.requireNonNull(type)) {
            case "speed" -> {
                int enchLvl = 1;
                if(axeMeta.hasEnchant(Enchantment.DIG_SPEED)) {
                    enchLvl += axeMeta.getEnchantLevel(Enchantment.DIG_SPEED);
                }
                axeMeta.addEnchant(Enchantment.DIG_SPEED, enchLvl, false);
            }
            case "cooldown" -> {
                NamespacedKey treefellerCooldownKey = new NamespacedKey(plugin, "treefeller-cooldown");
                int cooldown = axePers.get(treefellerCooldownKey, PersistentDataType.INTEGER);
                int newCooldown = cooldown - 1;
                axePers.set(treefellerCooldownKey, PersistentDataType.INTEGER, newCooldown);
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Cooldown: ", NamedTextColor.GRAY).append(Component.text(newCooldown + "s", TextColor.fromHexString("#c4c6c5"))).decoration(TextDecoration.ITALIC, false));
                axeMeta.lore(lore);
            }
            case "durability" -> {
                int enchLvl = 1;
                if(axeMeta.hasEnchant(Enchantment.DURABILITY)) {
                    enchLvl += axeMeta.getEnchantLevel(Enchantment.DURABILITY);
                }
                axeMeta.addEnchant(Enchantment.DURABILITY, enchLvl, false);
            }
            case "repair" -> ((Damageable) axeMeta).setDamage(0);
        }
        axe.setItemMeta(axeMeta);
        return axe;
    }
    public static ItemStack getRepairItem(SkyPrisonCore plugin, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        upgradeMeta.displayName(Component.text("Axe Repair", TextColor.fromHexString("#ea7125"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        PersistentDataContainer axePers = upgradeMeta.getPersistentDataContainer();
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "treefeller-upgrade");
        axePers.set(upgradeKey, PersistentDataType.STRING, "repair");
        upgrade.setItemMeta(upgradeMeta);
        return upgrade;
    }
    public static ItemStack getUpgradeItem(SkyPrisonCore plugin, String type, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        ItemMeta upgradeMeta = upgrade.getItemMeta();
        upgradeMeta.displayName(Component.text("Axe Upgrade", TextColor.fromHexString("#09f755"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        switch (type) {
            case "speed" -> lore.add(Component.text("Break Speed", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
            case "cooldown" -> lore.add(Component.text("Cooldown Reduction", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
            case "durability" -> lore.add(Component.text("Durability", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
        }
        upgradeMeta.lore(lore);
        PersistentDataContainer axePers = upgradeMeta.getPersistentDataContainer();
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "treefeller-upgrade");
        axePers.set(upgradeKey, PersistentDataType.STRING, type);
        upgrade.setItemMeta(upgradeMeta);
        return upgrade;
    }
}
