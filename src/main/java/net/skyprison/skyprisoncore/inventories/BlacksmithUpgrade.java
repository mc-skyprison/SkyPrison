package net.skyprison.skyprisoncore.inventories;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.items.TreeFeller;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class BlacksmithUpgrade implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;

    private final Timer timer = new Timer();

    private ItemStack currLeft = new ItemStack(Material.AIR);
    private ItemStack currRight = new ItemStack(Material.AIR);

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
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.empty());
        redPane.setItemMeta(redMeta);

        ItemStack greenPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta greenMeta = greenPane.getItemMeta();
        greenMeta.displayName(Component.empty());
        greenPane.setItemMeta(greenMeta);

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

        if(left && right) {
            inventory.setItem(11, greenPane);
            inventory.setItem(12, greenPane);
            inventory.setItem(14, greenPane);
            inventory.setItem(15, greenPane);
            setResult(inventory.getItem(10), inventory.getItem(16));
        } else if(left) {
            inventory.setItem(11, greenPane);
            inventory.setItem(12, greenPane);
            inventory.setItem(14, redPane);
            inventory.setItem(15, redPane);
        } else if(right) {
            inventory.setItem(11, redPane);
            inventory.setItem(12, redPane);
            inventory.setItem(14, greenPane);
            inventory.setItem(15, greenPane);
        } else {
            inventory.setItem(11, redPane);
            inventory.setItem(12, redPane);
            inventory.setItem(14, redPane);
            inventory.setItem(15, redPane);
        }
    }

    public void cancelTimer() {
        timer.cancel();
    }

    public void setResult(ItemStack left, ItemStack right) {
        inventory.setItem(13, TreeFeller.getUpgradedAxe(plugin, left, right));
    }

    public void resultTaken() {
        inventory.setItem(10, new ItemStack(Material.AIR));
        inventory.setItem(16, new ItemStack(Material.AIR));
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
        }
        return false;
    }

    public BlacksmithUpgrade(SkyPrisonCore plugin) {
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, 27, Component.text("Smithy", TextColor.fromHexString("#0fc3ff")));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = blackPane.getItemMeta();
        paneMeta.displayName(Component.empty());
        blackPane.setItemMeta(paneMeta);
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.empty());
        redPane.setItemMeta(redMeta);
        ItemStack blackArrow = new HeadDatabaseAPI().getItemHead("10307");
        ItemMeta arrowMeta = blackArrow.getItemMeta();
        arrowMeta.displayName(Component.text("Insert item below", NamedTextColor.GRAY));
        blackArrow.setItemMeta(arrowMeta);

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
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }

    @Override
    public List<Object> customClickList() {
        return null;
    }

    @Override
    public int getPage() {
        return 1;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
