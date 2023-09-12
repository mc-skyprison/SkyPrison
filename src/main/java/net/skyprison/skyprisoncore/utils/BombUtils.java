package net.skyprison.skyprisoncore.utils;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.apache.commons.lang.WordUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;

public class BombUtils {
    public static ItemStack getBomb(SkyPrisonCore plugin, String bombName, int amount) {
        if(bombName.contains("_")) {
            bombName = bombName.split("_")[0];
        }

        bombName = bombName.toLowerCase();

        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        ItemStack item = hAPI.getItemHead(getBombHdb(bombName));
        String finalBombName = bombName;
        item.editMeta(SkullMeta.class, meta -> {
            meta.displayName(Component.text(WordUtils.capitalize(finalBombName) + " Bomb", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            NamespacedKey key = new NamespacedKey(plugin, "bomb-type");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, finalBombName);
        });
        item.setAmount(amount);
        return item;
    }

    public static String getBombHdb(String bombType) {
        HashMap<String, String> bombTypes = new HashMap<>();
        bombTypes.put("small", "29488");
        bombTypes.put("medium", "11556");
        bombTypes.put("large", "11567");
        bombTypes.put("massive", "11555");
        bombTypes.put("nuke", "11564");

        return bombTypes.get(bombType);
    }

}
