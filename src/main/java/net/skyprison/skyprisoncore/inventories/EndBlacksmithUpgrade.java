package net.skyprison.skyprisoncore.inventories;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.destroystokyo.paper.MaterialTags;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EndBlacksmithUpgrade implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final Player player;
    private final Timer timer = new Timer();
    private ItemStack currLeft = new ItemStack(Material.AIR);
    private ItemStack currMiddle = new ItemStack(Material.AIR);
    private ItemStack currRight = new ItemStack(Material.AIR);
    private ItemStack currBookLeft = new ItemStack(Material.AIR);
    private ItemStack currBookRight = new ItemStack(Material.AIR);
    private final List<Material> diamondItems = new ArrayList<>(Arrays.asList(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BLOCK,
            Material.DIAMOND_AXE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE));
    public void updateInventory() {
        ItemStack left = inventory.getItem(10);
        ItemStack middle = inventory.getItem(11);
        ItemStack right = inventory.getItem(12);
        ItemStack bookLeft = inventory.getItem(13);
        ItemStack bookRight = inventory.getItem(14);
        boolean leftValid = false;
        boolean middleValid = false;
        boolean rightValid = false;
        boolean bookLeftValid = false;
        boolean bookRightValid = false;
        if(left != null && isItemValid(left)) {
            leftValid = true;
        }
        if(middle != null && isItemValid(middle)) {
            middleValid = true;
        }
        if(right != null && isItemValid(right)) {
            rightValid = true;
        }
        if(bookLeft != null && isBookValid(bookLeft)) {
            bookLeftValid = true;
        }
        if(bookRight != null && isBookValid(bookRight)) {
            bookRightValid = true;
        }
        if(leftValid && middleValid && rightValid) {
            System.out.println("all valido");
            if(areAllValid(left, middle, right)) {
                System.out.println("all valido dos");
                if(bookLeftValid && bookRightValid) {
                    System.out.println("books valido");
                    if(areBooksValid(bookLeft, bookRight)) {
                        System.out.println("books valido dos");
                        setColour(true, true, true, true, true);
                    } else {
                        System.out.println("books NOT valido");
                        setColour(false, false, false, false, false);
                    }
                } else {
                    System.out.println("book left " + bookLeftValid + " book right " + bookRightValid);
                    setColour(true, true, true, bookLeftValid, bookRightValid);
                }
            } else {
                System.out.println("all not valido");
                setColour(false, false, false, false, false);
            }
        } else setColour(leftValid, middleValid, rightValid, bookLeftValid, bookRightValid);
    }
    private void setColour(boolean left, boolean middle, boolean right, boolean bookLeft, boolean bookRight) {
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.empty());
        redPane.setItemMeta(redMeta);

        ItemStack greenPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta greenMeta = greenPane.getItemMeta();
        greenMeta.displayName(Component.empty());
        greenPane.setItemMeta(greenMeta);

        ItemStack leftItem = inventory.getItem(10);
        ItemStack middleItem = inventory.getItem(11);
        ItemStack rightItem = inventory.getItem(12);
        ItemStack bookLeftItem = inventory.getItem(13);
        ItemStack bookRightItem = inventory.getItem(14);
        if(this.currLeft == null && left) {
            this.currLeft = leftItem;
        }
        if(this.currMiddle == null && middle) {
            this.currMiddle = middleItem;
        }
        if(this.currRight == null && right) {
            this.currRight = rightItem;
        }
        if(this.currBookLeft == null && bookLeft) {
            this.currBookLeft = bookLeftItem;
        }
        if(this.currBookRight == null && bookRight) {
            this.currBookRight = bookRightItem;
        }

        if((this.currLeft != null && !this.currLeft.equals(leftItem)) || (this.currMiddle != null && !this.currMiddle.equals(middleItem)) || (this.currRight != null && !this.currRight.equals(rightItem))
                || (this.currBookLeft != null && !this.currBookLeft.equals(bookLeftItem)) || (this.currBookRight != null && !this.currBookRight.equals(bookRightItem))) {
            resetResult();
            this.currLeft = leftItem;
            this.currMiddle = middleItem;
            this.currRight = rightItem;
            this.currBookLeft = bookLeftItem;
            this.currBookRight = bookRightItem;
        }

        inventory.setItem(19, left ? greenPane : redPane);
        inventory.setItem(20, middle ? greenPane : redPane);
        inventory.setItem(21, right ? greenPane : redPane);
        inventory.setItem(22, bookLeft ? greenPane : redPane);
        inventory.setItem(23, bookRight ? greenPane : redPane);

        if(left && middle && right) {
            if(!upgradingArmour()) {
                if(!hasKeepTrims()) {
                    System.out.println("keepy trimmy: " + hasKeepTrims());
                    setResult(leftItem, middleItem, rightItem);
                }
            } else {
                System.out.println("IS ARMOUR: " + upgradingArmour());
                setResult(leftItem, middleItem, rightItem);
            }
        }
    }
    public void cancelTimer() {
        timer.cancel();
    }
    public void setResult(ItemStack left, ItemStack middle, ItemStack right) {
        ItemStack upgradingItem = getUpgradingItem(left, middle, right);
        ItemStack upgradeTemplate = getUpgradeTemplateItem(left, middle, right);
        ItemStack netheriteIngot = getNetheriteItem(left, middle, right);
        if(upgradingItem != null && upgradeTemplate != null && netheriteIngot != null) {
            double price = getPrice();
            double hasMoney = hasMoney(price);
            if(hasMoney == 0) {
                ItemStack upgrade = upgradingItem.clone();
                switch (upgrade.getType()) {
                    case DIAMOND_HELMET -> upgrade.setType(Material.NETHERITE_HELMET);
                    case DIAMOND_CHESTPLATE -> upgrade.setType(Material.NETHERITE_CHESTPLATE);
                    case DIAMOND_LEGGINGS -> upgrade.setType(Material.NETHERITE_LEGGINGS);
                    case DIAMOND_BOOTS -> upgrade.setType(Material.NETHERITE_BOOTS);
                    case DIAMOND_AXE -> upgrade.setType(Material.NETHERITE_AXE);
                    case DIAMOND_PICKAXE -> upgrade.setType(Material.NETHERITE_PICKAXE);
                    case DIAMOND_SHOVEL -> upgrade.setType(Material.NETHERITE_SHOVEL);
                    case DIAMOND_HOE -> upgrade.setType(Material.NETHERITE_HOE);
                    default -> upgrade = null;
                }
                if(upgrade != null) {
                    ItemMeta upgradeMeta = upgrade.getItemMeta();
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(price),
                            TextColor.fromHexString("#52fc28"), TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                    upgradeMeta.lore(lore);

                    if(hasResetRepairCost()) {
                        upgrade.setRepairCost(0);
                    }

                    if(!hasKeepEnchants()) {
                        upgradeMeta.getEnchants().keySet().forEach(upgradeMeta::removeEnchant);
                    }

                    if(upgradingArmour() && !hasKeepTrims()) {
                        if(((ArmorMeta) upgradeMeta).hasTrim()) {
                            ((ArmorMeta) upgradeMeta).setTrim(null);
                        }
                    }

                    upgrade.setItemMeta(upgradeMeta);
                }
                inventory.setItem(16, upgrade);
            } else {
                ItemStack needMoney = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta needMeta = needMoney.getItemMeta();
                needMeta.displayName(Component.text("Not Enough Money", NamedTextColor.RED));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(price), TextColor.fromHexString("#52fc28"), TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Missing: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(hasMoney), NamedTextColor.RED, TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("                  ").decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Balance: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(getBalance(player)), NamedTextColor.RED, TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                needMeta.lore(lore);
                needMoney.setItemMeta(needMeta);
                inventory.setItem(16, needMoney);
            }
        } else {
            inventory.setItem(16, new ItemStack(Material.AIR));
            setColour(false, false, false, false, false);
        }
    }
    private ItemStack getUpgradingItem(ItemStack... items) {
        return Arrays.stream(items)
                .filter(item -> diamondItems.contains(item.getType()))
                .findFirst()
                .orElse(null);
    }
    private ItemStack getUpgradeTemplateItem(ItemStack left, ItemStack middle, ItemStack right) {
        if(left.getType().equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) return left;
        if(middle.getType().equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) return middle;
        if(right.getType().equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) return right;
        return null;
    }
    private ItemStack getNetheriteItem(ItemStack left, ItemStack middle, ItemStack right) {
        if(left.getType().equals(Material.NETHERITE_INGOT)) return left;
        if(middle.getType().equals(Material.NETHERITE_INGOT)) return middle;
        if(right.getType().equals(Material.NETHERITE_INGOT)) return right;
        return null;
    }

    public boolean upgradingArmour() {
        return (currLeft != null && MaterialTags.ARMOR.isTagged(currLeft)) || (currMiddle != null && MaterialTags.ARMOR.isTagged(currMiddle))
                || (currRight != null && MaterialTags.ARMOR.isTagged(currRight));
    }

    public void resultTaken() {
        currLeft.setAmount(currLeft.getAmount()-1);
        currMiddle.setAmount(currMiddle.getAmount()-1);
        currRight.setAmount(currRight.getAmount()-1);
        if(currBookLeft != null) currBookLeft.setAmount(currBookLeft.getAmount()-1);
        if(currBookRight != null) currBookRight.setAmount(currBookRight.getAmount()-1);
    }
    public void resetResult() {
        inventory.setItem(16, new ItemStack(Material.AIR));
    }
    public boolean isBookValid(ItemStack item) {
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
        if(item.getType().equals(Material.ENCHANTED_BOOK) && item.hasItemMeta()
                && !item.getPersistentDataContainer().isEmpty() && item.getPersistentDataContainer().has(upgradeKey, PersistentDataType.STRING)) {
            if(Objects.requireNonNull(item.getPersistentDataContainer().get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("keep-trims")) {
                return upgradingArmour();
            } else {
                return true;
            }
        }
        return false;
    }
    public boolean hasResetRepairCost() {
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
        return (currBookLeft != null && isBookValid(currBookLeft) && Objects.requireNonNull(currBookLeft.getPersistentDataContainer()
                .get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("reset-repair"))
                || (currBookRight != null && isBookValid(currBookRight) && Objects.requireNonNull(currBookRight.getPersistentDataContainer()
                .get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("reset-repair"));
    }
    public boolean hasKeepEnchants() {
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
        return (currBookLeft != null && isBookValid(currBookLeft) && Objects.requireNonNull(currBookLeft.getPersistentDataContainer()
                .get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("keep-enchants"))
                || (currBookRight != null && isBookValid(currBookRight) && Objects.requireNonNull(currBookRight.getPersistentDataContainer()
                .get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("keep-enchants"));
    }
    public boolean hasKeepTrims() {
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
        return (currBookLeft != null && isBookValid(currBookLeft) && Objects.requireNonNull(currBookLeft.getPersistentDataContainer()
                .get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("keep-trims"))
                || (currBookRight != null && isBookValid(currBookRight) && Objects.requireNonNull(currBookRight.getPersistentDataContainer()
                .get(upgradeKey, PersistentDataType.STRING)).equalsIgnoreCase("keep-trims"));
    }
    public boolean areBooksValid(ItemStack leftBook, ItemStack rightBook) {
        NamespacedKey upgradeKey = new NamespacedKey(plugin, "blacksmith-end-addon");
        if(leftBook != null && leftBook.getType().equals(Material.ENCHANTED_BOOK) && leftBook.hasItemMeta()
                && !leftBook.getPersistentDataContainer().isEmpty() && leftBook.getPersistentDataContainer().has(upgradeKey, PersistentDataType.STRING)
        && rightBook != null && rightBook.getType().equals(Material.ENCHANTED_BOOK) && rightBook.hasItemMeta()
                && !rightBook.getPersistentDataContainer().isEmpty() && rightBook.getPersistentDataContainer().has(upgradeKey, PersistentDataType.STRING)) {
            String leftType = leftBook.getPersistentDataContainer().get(upgradeKey, PersistentDataType.STRING);
            String rightType = leftBook.getPersistentDataContainer().get(upgradeKey, PersistentDataType.STRING);
            if(leftType != null & rightType != null) {
                boolean keepEnchants = leftType.equalsIgnoreCase("keep-enchants") || rightType.equalsIgnoreCase("keep-enchants");
                boolean resetRepair = leftType.equalsIgnoreCase("reset-repair") || rightType.equalsIgnoreCase("reset-repair");
                boolean keepTrims = leftType.equalsIgnoreCase("keep-trims") || rightType.equalsIgnoreCase("keep-trims");
                return (keepEnchants && resetRepair) || (keepEnchants && keepTrims) || (resetRepair && keepTrims);
            }
        }
        return false;
    }
    public boolean isItemValid(ItemStack item) {
        Material mat = item.getType();
        return (item.getPersistentDataContainer().isEmpty() && (diamondItems.contains(mat))) || (mat.equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE) && item.hasItemMeta()
                && !item.getPersistentDataContainer().isEmpty() && item.getPersistentDataContainer().has(new NamespacedKey(plugin, "blacksmith-end-upgrade")))
                || (item.getPersistentDataContainer().isEmpty() && mat.equals(Material.NETHERITE_INGOT));
    }
    public boolean areAllValid(ItemStack... items) {
        ItemStack upgradeItem = null;
        ItemStack templateItem = null;
        ItemStack ingotItem = null;

        NamespacedKey key = new NamespacedKey(plugin, "blacksmith-end-upgrade");
        for (ItemStack item : items) {
            Material mat = item.getType();
            if (diamondItems.contains(mat)) {
                upgradeItem = item;
            } else if (mat.equals(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) {
                if(item.hasItemMeta() && !item.getPersistentDataContainer().isEmpty() && item.getPersistentDataContainer().has(key)) {
                    templateItem = item;
                }
            } else if (mat.equals(Material.NETHERITE_INGOT)) {
                ingotItem = item;
            }
        }

        if(upgradeItem != null && templateItem != null && ingotItem != null) {
            String type = templateItem.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if(type != null) {
                String itemType = upgradeItem.getType().toString().split("_")[1];
                return type.equalsIgnoreCase(itemType);
            }
        }
        return false;
    }
    public double hasMoney(double cost) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
        if(user.hasMoney(cost)) {
            return 0;
        } else {
            double money = user.getBalance();
            return cost - money;
        }
    }
    public double getPrice() {
        return 50000;
    }
    public double getBalance(Player player) {
        CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
        return user.getBalance();
    }
    public EndBlacksmithUpgrade(SkyPrisonCore plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
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
        ItemStack purpleArrow = new HeadDatabaseAPI().getItemHead("9443");
        ItemMeta purpleMeta = purpleArrow.getItemMeta();
        purpleMeta.displayName(Component.text("Insert item below", NamedTextColor.GRAY));
        purpleArrow.setItemMeta(purpleMeta);

        for(int i = 0; i < inventory.getSize(); i++) {
            if(i > 0 && i < 4) {
                inventory.setItem(i, blackArrow);
            } else if(i > 3 && i < 6) {
                inventory.setItem(i, purpleArrow);
            } else if(!(i > 9 && i < 15) && i != 16) {
                inventory.setItem(i, blackPane);
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
