package net.skyprison.skyprisoncore.inventories.smith;

import com.destroystokyo.paper.MaterialSetTag;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlacksmithTrimmer implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final Player player;
    private final Timer timer = new Timer();
    private ItemStack currLeft = new ItemStack(Material.AIR);
    private ItemStack currMiddle = new ItemStack(Material.AIR);
    private ItemStack currRight = new ItemStack(Material.AIR);
    private ItemStack armourPiece = new ItemStack(Material.AIR);
    public void updateInventory() {
        ItemStack left = inventory.getItem(10);
        ItemStack middle = inventory.getItem(11);
        ItemStack right = inventory.getItem(12);
        boolean leftValid = false;
        boolean middleValid = false;
        boolean rightValid = false;

        if(left != null && isItemValid(left)) {
            leftValid = true;
        }
        if(middle != null && isItemValid(middle)) {
            middleValid = true;
        }
        if(right != null && isItemValid(right)) {
            rightValid = true;
        }
        if(leftValid && middleValid && rightValid) {
            if(areAllValid(left.getType(), middle.getType(), right.getType()) && canTrim(left, middle, right)) {
                setColour(true, true, true);
            } else {
                setColour(false, false, false);
            }
        } else setColour(leftValid, middleValid, rightValid);
    }
    private void setColour(boolean left, boolean middle, boolean right) {
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
        if(this.currLeft == null && left) {
            this.currLeft = leftItem;
        }
        if(this.currMiddle == null && middle) {
            this.currMiddle = middleItem;
        }
        if(this.currRight == null && right) {
            this.currRight = rightItem;
        }

        if((this.currLeft != null && !this.currLeft.equals(leftItem)) || (this.currMiddle != null && !this.currMiddle.equals(middleItem)) || (this.currRight != null && !this.currRight.equals(rightItem))) {
            resetResult();
            this.currLeft = leftItem;
            this.currMiddle = middleItem;
            this.currRight = rightItem;
        }

        inventory.setItem(19, left ? greenPane : redPane);
        inventory.setItem(20, middle ? greenPane : redPane);
        inventory.setItem(21, right ? greenPane : redPane);

        if(left && middle && right) {
            setResult(leftItem, middleItem, rightItem);
        }
    }
    public void cancelTimer() {
        timer.cancel();
    }
    public void setResult(ItemStack left, ItemStack middle, ItemStack right) {
        armourPiece = getArmourPieceItem(left, middle, right);
        ItemStack trimPattern = getTrimTemplateItem(left, middle, right);
        ItemStack trimMaterial = getTrimMaterialitem(left, middle, right);
        if(armourPiece != null && trimPattern != null && trimMaterial != null) {
            double price = getPrice();
            double hasMoney = hasMoney(price);
            if(hasMoney == 0) {
                ItemStack trimmedArmour = armourPiece.clone();
                ArmorMeta armorMeta = (ArmorMeta) trimmedArmour.getItemMeta();
                ArmorTrim trim = new ArmorTrim(getTrimMaterial(trimMaterial.getType()), getTrimPattern(trimPattern.getType()));
                armorMeta.setTrim(trim);

                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(price), TextColor.fromHexString("#52fc28"), TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                armorMeta.lore(lore);
                trimmedArmour.setItemMeta(armorMeta);
                inventory.setItem(16, trimmedArmour);
            } else {
                ItemStack needMoney = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta needMeta = needMoney.getItemMeta();
                needMeta.displayName(Component.text("Not Enough Money", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("Price: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(price), TextColor.fromHexString("#52fc28"), TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Missing: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(hasMoney), NamedTextColor.RED, TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("                  ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Balance: ", NamedTextColor.GRAY).append(Component.text("$" + plugin.formatNumber(PlayerManager.getBalance(player)), NamedTextColor.RED, TextDecoration.BOLD))
                        .decoration(TextDecoration.ITALIC, false));
                needMeta.lore(lore);
                needMoney.setItemMeta(needMeta);
                inventory.setItem(16, needMoney);
            }
        } else {
            inventory.setItem(16, new ItemStack(Material.AIR));
            setColour(false, false, false);
        }
    }
    private ItemStack getArmourPieceItem(ItemStack... items) {
        List<Material> chainmailTypes = Arrays.asList(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
        return Arrays.stream(items)
                .filter(item -> isTrimmableArmor(item, chainmailTypes))
                .findFirst()
                .orElse(null);
    }
    private boolean isTrimmableArmor(ItemStack item, List<Material> excludedTypes) {
        Material itemType = item.getType();
        return MaterialSetTag.ITEMS_TRIMMABLE_ARMOR.isTagged(itemType) && !excludedTypes.contains(itemType);
    }
    private ItemStack getTrimTemplateItem(ItemStack left, ItemStack middle, ItemStack right) {
        if(MaterialSetTag.ITEMS_TRIM_TEMPLATES.isTagged(left.getType())) return left;
        if(MaterialSetTag.ITEMS_TRIM_TEMPLATES.isTagged(middle.getType())) return middle;
        if(MaterialSetTag.ITEMS_TRIM_TEMPLATES.isTagged(right.getType())) return right;
        return null;
    }
    private ItemStack getTrimMaterialitem(ItemStack left, ItemStack middle, ItemStack right) {
        if(MaterialSetTag.ITEMS_TRIM_MATERIALS.isTagged(left.getType())) return left;
        if(MaterialSetTag.ITEMS_TRIM_MATERIALS.isTagged(middle.getType())) return middle;
        if(MaterialSetTag.ITEMS_TRIM_MATERIALS.isTagged(right.getType())) return right;
        return null;
    }
    public void resultTaken() {
        currLeft.setAmount(currLeft.getAmount()-1);
        currMiddle.setAmount(currMiddle.getAmount()-1);
        currRight.setAmount(currRight.getAmount()-1);
    }
    public void resetResult() {
        inventory.setItem(16, new ItemStack(Material.AIR));
    }
    public boolean isItemValid(ItemStack item) {
        return !item.hasItemMeta() && (MaterialSetTag.ITEMS_TRIMMABLE_ARMOR.isTagged(item.getType()) || MaterialSetTag.ITEMS_TRIM_TEMPLATES.isTagged(item.getType()) || MaterialSetTag.ITEMS_TRIM_MATERIALS.isTagged(item.getType()));
    }
    public boolean areAllValid(Material... materials) {
        boolean hasTrimmableItem = false;
        boolean hasTrimTemplate = false;
        boolean hasTrimMaterial = false;

        for (Material material : materials) {
            if (MaterialSetTag.ITEMS_TRIMMABLE_ARMOR.isTagged(material)) {
                hasTrimmableItem = true;
            } else if (MaterialSetTag.ITEMS_TRIM_TEMPLATES.isTagged(material)) {
                hasTrimTemplate = true;
            } else if (MaterialSetTag.ITEMS_TRIM_MATERIALS.isTagged(material)) {
                hasTrimMaterial = true;
            }
        }

        return hasTrimmableItem && hasTrimTemplate && hasTrimMaterial;
    }
    public boolean canTrim(ItemStack left, ItemStack middle, ItemStack right) {
        ItemStack armourPiece = null;

        if (MaterialSetTag.ITEMS_TRIMMABLE_ARMOR.isTagged(left.getType())) {
            armourPiece = left;
        } else if (MaterialSetTag.ITEMS_TRIMMABLE_ARMOR.isTagged(middle.getType())) {
            armourPiece = middle;
        } else if (MaterialSetTag.ITEMS_TRIMMABLE_ARMOR.isTagged(right.getType())) {
            armourPiece = right;
        }

        if(armourPiece != null) {
            ArmorMeta armorMeta = (ArmorMeta) armourPiece.getItemMeta();
            return !armorMeta.hasTrim();
        }
        return false;
    }
    public double hasMoney(double cost) {
        double money = PlayerManager.getBalance(player);
        return (money >= cost) ? 0 : cost - money;
    }
    public double getPrice() {
        double price = 0;
        if(armourPiece != null) {
            switch (armourPiece.getType()) {
                case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> price = 1000;

                case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS -> price = 5000;

                case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS -> price = 7500;

                case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> price = 15000;

                case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> price = 50000;

                case TURTLE_HELMET -> price = 25000;
            }
        }
        return price;
    }
    public TrimMaterial getTrimMaterial(Material type) {
        TrimMaterial mat = null;
        switch (type) {
            case QUARTZ -> mat = TrimMaterial.QUARTZ;
            case IRON_INGOT -> mat = TrimMaterial.IRON;
            case NETHERITE_INGOT -> mat = TrimMaterial.NETHERITE;
            case REDSTONE -> mat = TrimMaterial.REDSTONE;
            case COPPER_INGOT -> mat = TrimMaterial.COPPER;
            case GOLD_BLOCK -> mat = TrimMaterial.GOLD;
            case EMERALD -> mat = TrimMaterial.EMERALD;
            case DIAMOND -> mat = TrimMaterial.DIAMOND;
            case LAPIS_LAZULI -> mat = TrimMaterial.LAPIS;
            case AMETHYST_SHARD -> mat = TrimMaterial.AMETHYST;
        }
        return mat;
    }
    public TrimPattern getTrimPattern(Material type) {
        TrimPattern pattern = null;
        switch (type) {
            case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SENTRY;
            case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.DUNE;
            case COAST_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.COAST;
            case WILD_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.WILD;
            case WARD_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.WARD;
            case EYE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.EYE;
            case VEX_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.VEX;
            case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.TIDE;
            case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SNOUT;
            case RIB_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.RIB;
            case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SPIRE;
            case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.WAYFINDER;
            case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SHAPER;
            case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.SILENCE;
            case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.RAISER;
            case HOST_ARMOR_TRIM_SMITHING_TEMPLATE -> pattern = TrimPattern.HOST;
        }
        return pattern;
    }
    public BlacksmithTrimmer(SkyPrisonCore plugin, Player player) {
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

        for(int i = 0; i < inventory.getSize(); i++) {
            if(i > 0 && i < 4) {
                inventory.setItem(i, blackArrow);
            } else if(!(i > 9 && i < 13) && i != 16) {
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
