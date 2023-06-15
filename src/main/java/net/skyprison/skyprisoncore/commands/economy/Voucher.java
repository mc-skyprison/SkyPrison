package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Voucher implements CommandExecutor { // /voucher give <player> <voucher> <amount>

    public Voucher() {
    }


    public static ItemStack getVoucher(String voucherType, int amount) {
        SkyPrisonCore plugin = JavaPlugin.getPlugin(SkyPrisonCore.class);
        ItemStack voucher = new ItemStack(Material.PAPER, amount);
        ItemMeta vMeta = voucher.getItemMeta();
        String voucherName = WordUtils.capitalize(voucherType.toLowerCase().replace("-", " "));
        vMeta.displayName(Component.text(plugin.colourMessage("&e" + voucherName + " Voucher")));
        ArrayList<Component> lore = new ArrayList<>();
        if(voucherType.equalsIgnoreCase("mine-reset")) {
            lore.add(Component.text(plugin.colourMessage("&7&oUsing an on cooldown Mine Rest sign will")));
            lore.add(Component.text(plugin.colourMessage("&7&oredeem this voucher.")));
        } else if(voucherType.equalsIgnoreCase("token-shop")) {
            lore.add(Component.text(plugin.colourMessage("&7&oBuying something from the TokenShop")));
            lore.add(Component.text(plugin.colourMessage("&7&owith this voucher in your inventory")));
            lore.add(Component.text(plugin.colourMessage("&7&owill redeem the voucher.")));
        }
        vMeta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
        vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        vMeta.lore(lore);
        PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "voucher");
        vouchData.set(key, PersistentDataType.STRING, voucherType.toLowerCase());

        voucher.setItemMeta(vMeta);
        return voucher;
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
         if(args.length == 4) {
            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);

                ItemStack voucher = getVoucher(args[2], Integer.parseInt(args[3]));

                if(user.getInventory().canFit(voucher)) {
                    user.getInventory().addItem(voucher);
                } else {
                    user.dropItemNearPlayer(voucher);
                }
            }
         } else {
             sender.sendMessage(Component.text("Incorrect Usage! /voucher give <player> <voucher> <amount>", NamedTextColor.RED));
         }
        return true;
    }
}
