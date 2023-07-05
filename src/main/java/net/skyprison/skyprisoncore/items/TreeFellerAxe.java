package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class TreeFellerAxe {
    public static ItemStack getAxe(SkyPrisonCore plugin, Material axeMat, int amount) {
        ItemStack axe = new ItemStack(axeMat, amount);
        ItemMeta axeMeta = axe.getItemMeta();
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Wham?", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        axeMeta.lore(lore);
        PersistentDataContainer axePers = axeMeta.getPersistentDataContainer();
        NamespacedKey treefellerKey = new NamespacedKey(plugin, "treefeller");
        axePers.set(treefellerKey, PersistentDataType.INTEGER, 1);
        axe.setItemMeta(axeMeta);
        return axe;
    }
}
