package net.skyprison.skyprisoncore.inventories.smith;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.items.TreeFeller;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GrassBlacksmithUpgrade implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final Player player;
    private final Timer timer = new Timer();

    private ItemStack currLeft = new ItemStack(Material.AIR);
    private ItemStack currRight = new ItemStack(Material.AIR);
    private ItemStack axeItem = new ItemStack(Material.AIR);
    private ItemStack bookItem = new ItemStack(Material.AIR);

    public void updateInventory() {
        ItemStack left = inventory.getItem(10);
        ItemStack right = inventory.getItem(16);
        boolean leftValid = false;
        boolean rightValid = false;

        if(left != null && isItemValid(left)) {
            leftValid = true;
        }

        if(right != null && isItemValid(right)) {
            rightValid = true;
        }
        if(leftValid && rightValid) {
            if(areBothValid(left, right) && canUpgrade(left, right)) {
                setColour(true, true);
            } else {
                setColour(false, false);
            }
        } else setColour(leftValid, rightValid);
    }

    private void setColour(boolean left, boolean right) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack greenPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        greenPane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack leftItem = inventory.getItem(10);
        ItemStack rightItem = inventory.getItem(16);
        if(this.currLeft == null && left) {
            this.currLeft = leftItem;
        }
        if(this.currRight == null && right) {
            this.currRight = rightItem;
        }

        if((this.currLeft != null && !this.currLeft.equals(leftItem)) || (this.currRight != null && !this.currRight.equals(rightItem))) {
            resetResult();
            this.currLeft = leftItem;
            this.currRight = rightItem;
        }
        inventory.setItem(11, left ? greenPane : redPane);
        inventory.setItem(12, left ? greenPane : redPane);
        inventory.setItem(14, right ? greenPane : redPane);
        inventory.setItem(15, right ? greenPane : redPane);
        if(left && right) {
            setResult(leftItem, rightItem);
        }
    }

    public void cancelTimer() {
        timer.cancel();
    }

    public void setResult(ItemStack left, ItemStack right) {
        if(left != null && right != null) {
            ItemMeta leftMeta = left.getItemMeta();
            PersistentDataContainer leftPers = leftMeta.getPersistentDataContainer();
            NamespacedKey treefellerKey = new NamespacedKey(plugin, "treefeller");
            if (leftPers.has(treefellerKey)) {
                axeItem = left;
                bookItem = right;
            } else {
                axeItem = right;
                bookItem = left;
            }
            double price = getPrice();
            double hasMoney = hasMoney(price);
            if (hasMoney == 0) {
                ItemStack treeAxe = TreeFeller.getUpgradedAxe(plugin, left, right);
                treeAxe.editMeta(meta -> {
                    List<Component> lore = meta.lore();
                    if (lore != null) {
                        lore.add(0, Component.text("Price: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(price),
                                TextColor.fromHexString("#52fc28"), TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                        lore.add(1, Component.text("                  ").decoration(TextDecoration.ITALIC, false));
                        meta.lore(lore);
                    } else inventory.close();
                });
                inventory.setItem(13, treeAxe);
            } else {
                ItemStack needMoney = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                needMoney.editMeta(meta -> {
                    meta.displayName(Component.text("Not Enough Money", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(price),
                            TextColor.fromHexString("#52fc28"), TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Missing: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(hasMoney),
                            NamedTextColor.RED, TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("                  ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Balance: ", NamedTextColor.GRAY).append(Component.text("$" + ChatUtils.formatNumber(PlayerManager.getBalance(player)),
                            NamedTextColor.RED, TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                    });
                inventory.setItem(13, needMoney);
            }
        } else {
            inventory.setItem(13, new ItemStack(Material.AIR));
            setColour(false, false);
        }
    }

    public void resultTaken() {
        currLeft.setAmount(currLeft.getAmount()-1);
        currRight.setAmount(currRight.getAmount()-1);
    }

    public double hasMoney(double cost) {
        double money = PlayerManager.getBalance(player);
        return (money >= cost) ? 0 : cost - money;
    }

    public double getPrice() {
        double price = 0;
        if(axeItem != null) {
            if(bookItem != null && Objects.requireNonNull(bookItem.getPersistentDataContainer().get(new NamespacedKey(plugin, "treefeller-upgrade"), PersistentDataType.STRING))
                    .equalsIgnoreCase("repair")) {
                return 0;
            }
            int upgradedAmounts = 0;
            ItemMeta axeMeta = axeItem.getItemMeta();
            PersistentDataContainer axePers = axeMeta.getPersistentDataContainer();
            NamespacedKey treefellerCooldownKey = new NamespacedKey(plugin, "treefeller-cooldown");
            int cooldown = axePers.get(treefellerCooldownKey, PersistentDataType.INTEGER);
            upgradedAmounts += (10 - cooldown);
            if(axeMeta.hasEnchant(Enchantment.DURABILITY)) {
                upgradedAmounts += axeMeta.getEnchantLevel(Enchantment.DURABILITY);
            }
            if(axeMeta.hasEnchant(Enchantment.DIG_SPEED)) {
                upgradedAmounts += axeMeta.getEnchantLevel(Enchantment.DIG_SPEED);
            }
            price = 50 * upgradedAmounts;
        }
        return price;
    }

    public void resetResult() {
        inventory.setItem(13, new ItemStack(Material.AIR));
    }

    public boolean isItemValid(ItemStack item) {
        if(item.hasItemMeta() && item.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller"))) {
            return true;
        } else return item.getType().equals(Material.ENCHANTED_BOOK) && item.hasItemMeta() && item.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller-upgrade"));
    }

    public boolean areBothValid(ItemStack left, ItemStack right) {
        boolean hasAxe = false;
        boolean hasBook = false;

        if(left.hasItemMeta() && left.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller"))) {
            hasAxe = true;
        } else if(left.getType().equals(Material.ENCHANTED_BOOK) && left.hasItemMeta() && left.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller-upgrade"))) {
            hasBook = true;
        }
        if(right.hasItemMeta() && right.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller"))) {
            hasAxe = true;
        } else if(right.getType().equals(Material.ENCHANTED_BOOK) && right.hasItemMeta() && right.getPersistentDataContainer().has(new NamespacedKey(plugin, "treefeller-upgrade"))) {
            hasBook = true;
        }

        return hasAxe && hasBook;
    }

    public boolean canUpgrade(ItemStack left, ItemStack right) {
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
                if(axeMeta.hasEnchant(Enchantment.DIG_SPEED)) {
                    int enchLvl = axeMeta.getEnchantLevel(Enchantment.DIG_SPEED);
                    if(enchLvl < Enchantment.DIG_SPEED.getMaxLevel()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            case "cooldown" -> {
                NamespacedKey treefellerCooldownKey = new NamespacedKey(plugin, "treefeller-cooldown");
                int cooldown = axePers.get(treefellerCooldownKey, PersistentDataType.INTEGER);
                if(cooldown > 1) {
                    return true;
                }
            }
            case "durability" -> {
                if(axeMeta.hasEnchant(Enchantment.DURABILITY)) {
                    int enchLvl = axeMeta.getEnchantLevel(Enchantment.DURABILITY);
                    if(enchLvl < Enchantment.DURABILITY.getMaxLevel()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            case "repair" -> {
                return true;
            }
        }
        return false;
    }

    public GrassBlacksmithUpgrade(SkyPrisonCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = plugin.getServer().createInventory(this, 27, Component.text("Smithy", TextColor.fromHexString("#0fc3ff")));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack blackArrow = new HeadDatabaseAPI().getItemHead("10307");
        blackArrow.editMeta(meta -> meta.displayName(Component.text("Insert item below", NamedTextColor.GRAY)));

        for(int i = 0; i < inventory.getSize(); i++) {
            if(i == 1 || i == 7) {
                inventory.setItem(i, blackArrow);
            } else if(!(i > 9 && i < 17)) {
                inventory.setItem(i, blackPane);
            } else if(i == 11 || i == 12 || i == 14 || i == 15) {
                inventory.setItem(i, redPane);
            }
        }

        TimerTask update = new TimerTask() {

            @Override
            public void run() {
                updateInventory();
            }
        };
        timer.scheduleAtFixedRate(update, 0, 50);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
