package net.skyprison.skyprisoncore.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class PostOffice {
    public static ItemStack getItemFromType(SkyPrisonCore plugin, String type, int amount) {
        ItemStack item = null;
        switch (type.toLowerCase()) {
            case "mailbox" -> item = getMailBox(plugin, amount);
        }
        return item;
    }
    public static ItemStack getMailBox(SkyPrisonCore plugin, int amount) {
        ItemStack mailBox = new ItemStack(Material.CHEST, amount);
        mailBox.editMeta(meta -> {
            meta.displayName(Component.text("Mailbox", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Can be placed in Free", NamedTextColor.GRAY));
            meta.lore(lore);
            NamespacedKey key = new NamespacedKey(plugin, "mailbox");
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, -1);
        });
        return mailBox;
    }
}
