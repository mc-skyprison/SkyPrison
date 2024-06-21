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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TreeFeller {
    public static ItemStack getAxe(SkyPrisonCore plugin, int amount) {
        ItemStack axe = new ItemStack(Material.IRON_AXE, amount);
        axe.editMeta(meta -> {
            meta.displayName(Component.text("Treefeller Axe", TextColor.fromHexString("#adc17a")).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Cooldown: ", NamedTextColor.GRAY).append(Component.text("10s", TextColor.fromHexString("#c4c6c5")))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey treefellerKey = new NamespacedKey(plugin, "treefeller");
            NamespacedKey treefellerCooldownKey = new NamespacedKey(plugin, "treefeller-cooldown");
            axePers.set(treefellerKey, PersistentDataType.INTEGER, 1);
            axePers.set(treefellerCooldownKey, PersistentDataType.INTEGER, 10);
        });
        return axe;
    }

    public static ItemStack getUpgradedAxe(SkyPrisonCore plugin, ItemStack left, ItemStack right) {
        PersistentDataContainer leftPers = left.getItemMeta().getPersistentDataContainer();
        ItemStack axe;
        ItemStack upgradeItem;

        NamespacedKey treefellerKey = new NamespacedKey(plugin, "treefeller");
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "treefeller-upgrade");
        if(leftPers.has(treefellerKey)) {
            axe = left.clone();
        } else {
            axe = right.clone();
        }

        if(leftPers.has(upgradeKey)) {
            upgradeItem = left.clone();
        } else {
            upgradeItem = right.clone();
        }

        NamespacedKey treefellerCooldownKey = new NamespacedKey(plugin, "treefeller-cooldown");
        PersistentDataContainer upgradePers = upgradeItem.getItemMeta().getPersistentDataContainer();
        String type = upgradePers.get(upgradeKey, PersistentDataType.STRING);
        switch (Objects.requireNonNull(type)) {
            case "speed" -> {
                int enchLvl = 1;
                if(axe.hasEnchant(Enchantment.EFFICIENCY)) {
                    enchLvl += axe.getEnchantLevel(Enchantment.EFFICIENCY);
                }
                axe.addEnchant(Enchantment.EFFICIENCY, enchLvl, false);
            }
            case "cooldown" -> {
                ItemMeta meta = axe.getItemMeta();
                PersistentDataContainer axePers = meta.getPersistentDataContainer();
                int cooldown = axePers.get(treefellerCooldownKey, PersistentDataType.INTEGER);
                int newCooldown = cooldown - 1;
                axePers.set(treefellerCooldownKey, PersistentDataType.INTEGER, newCooldown);
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Cooldown: ", NamedTextColor.GRAY).append(Component.text(newCooldown + "s", TextColor.fromHexString("#c4c6c5")))
                        .decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
                axe.setItemMeta(meta);
            }
            case "durability" -> {
                int enchLvl = 1;
                if(axe.hasEnchant(Enchantment.UNBREAKING)) {
                    enchLvl += axe.getEnchantLevel(Enchantment.UNBREAKING);
                }
                axe.addEnchant(Enchantment.UNBREAKING, enchLvl, false);
            }
            case "repair" -> axe.setDamage(0);
        }
        return axe;
    }
    public static ItemStack getRepairItem(SkyPrisonCore plugin, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        upgrade.editMeta(meta -> {
            meta.displayName(Component.text("Axe Repair", TextColor.fromHexString("#ea7125"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey upgradeKey = new NamespacedKey(plugin, "treefeller-upgrade");
            axePers.set(upgradeKey, PersistentDataType.STRING, "repair");
        });
        return upgrade;
    }
    public static ItemStack getUpgradeItem(SkyPrisonCore plugin, String type, int amount) {
        ItemStack upgrade = new ItemStack(Material.ENCHANTED_BOOK, amount);
        upgrade.editMeta(meta -> {
            meta.displayName(Component.text("Axe Upgrade", TextColor.fromHexString("#09f755"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            switch (type) {
                case "speed" -> lore.add(Component.text("Break Speed", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
                case "cooldown" -> lore.add(Component.text("Cooldown Reduction", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
                case "durability" -> lore.add(Component.text("Durability", TextColor.fromHexString("#e7ebe0")).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
            PersistentDataContainer axePers = meta.getPersistentDataContainer();
            NamespacedKey upgradeKey = new NamespacedKey(plugin, "treefeller-upgrade");
            axePers.set(upgradeKey, PersistentDataType.STRING, type);
        });
        return upgrade;
    }
}
