package net.skyprison.skyprisoncore.listeners.minecraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlayerItemConsume implements Listener {
    private final SkyPrisonCore plugin;
    public PlayerItemConsume(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if(item.getType().equals(Material.POTION) && item.hasItemMeta() && !item.getItemMeta().getPersistentDataContainer().isEmpty()) {
            PotionMeta itemMeta = (PotionMeta) item.getItemMeta();
            PersistentDataContainer itemPers = itemMeta.getPersistentDataContainer();
            NamespacedKey gregKey = new NamespacedKey(plugin, "greg");
            if (itemPers.has(gregKey, PersistentDataType.STRING)) {
                String potion = itemPers.get(gregKey, PersistentDataType.STRING);
                if (potion != null && potion.contains(";")) {
                    String[] types = potion.split(";"); // works, onYou
                    Player player = event.getPlayer();
                    if (Boolean.parseBoolean(types[0])) {
                        if (!Boolean.parseBoolean(types[1])) {
                            ItemStack potionThrow = new ItemStack(Material.SPLASH_POTION);
                            potionThrow.setItemMeta(itemMeta);
                            ThrownPotion thrownPotion = player.launchProjectile(ThrownPotion.class);
                            thrownPotion.setItem(potionThrow);
                            player.sendMessage(Component.text("You accidently throw the bottle..", NamedTextColor.GRAY, TextDecoration.ITALIC));
                            event.setCancelled(true);
                            player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
                        }
                    } else {
                        event.setCancelled(true);
                        player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
                        player.sendMessage(Component.text("The potion appears to be defective..", NamedTextColor.GRAY, TextDecoration.ITALIC));
                    }
                }
            }
        }
    }
}
