package net.skyprison.skyprisoncore.commands.economy;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
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

import java.util.ArrayList;

public class Voucher implements CommandExecutor { // /voucher give <player> <voucher> <amount>
    private final SkyPrisonCore plugin;

    public Voucher(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if(args.length == 4) {
            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                ItemStack voucher = new ItemStack(Material.PAPER, Integer.parseInt(args[3]));
                ItemMeta vMeta = voucher.getItemMeta();
                String voucherType = WordUtils.capitalize(args[2].toLowerCase().replace("-", " "));
                vMeta.displayName(Component.text(plugin.colourMessage("&e" + voucherType + " Voucher")));
                ArrayList<Component> lore = new ArrayList<>();
                if(args[2].equalsIgnoreCase("mine-reset")) {
                    lore.add(Component.text(plugin.colourMessage("&7&oUsing an on cooldown Mine Rest sign will")));
                    lore.add(Component.text(plugin.colourMessage("&7&oredeem this voucher.")));
                } else if(args[2].equalsIgnoreCase("token-shop")) {
                    lore.add(Component.text(plugin.colourMessage("&7&oBuying something from the TokenShop")));
                    lore.add(Component.text(plugin.colourMessage("&7&owith this voucher in your inventory")));
                    lore.add(Component.text(plugin.colourMessage("&7&owill redeem the voucher.")));
                }
                vMeta.addEnchant(Enchantment.PROTECTION_FALL, 1, true);
                vMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                vMeta.lore(lore);
                PersistentDataContainer vouchData = vMeta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(plugin, "voucher");
                vouchData.set(key, PersistentDataType.STRING, args[2].toLowerCase());

                voucher.setItemMeta(vMeta);

                if(user.getInventory().canFit(voucher)) {
                    user.getInventory().addItem(voucher);
                } else {
                    user.dropItemNearPlayer(voucher);
                }
            }
         } else {
             sender.sendMessage(plugin.colourMessage("&cCorrect Usage: /voucher give <player> <voucher> <amount>"));
         }
        return true;
    }
}
