package net.skyprison.skyprisoncore.inventories.tags;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.Tags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TagsEdit implements CustomInventory {
    private final Inventory inventory;
    private final Tags.Tag tag;
    public final boolean isEdit;
    private String name = null;
    private String lore = null;
    private String effectType = null;
    private String effectStyle = null;
    private boolean changeLock = false;
    private final ItemStack tagName = new ItemStack(Material.OAK_SIGN);
    private final ItemStack tagLore = new ItemStack(Material.SPRUCE_SIGN);
    private final ItemStack tagEffectType = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack tagEffectStyle = new ItemStack(Material.ENCHANTED_BOOK);
    private final ItemStack tagPreview = new ItemStack(Material.NAME_TAG);
    private final Component preview = Component.text("Preview: ", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false);
    public void updatePreview() {
        tagPreview.editMeta(meta -> {
            meta.displayName(hasName() ? MiniMessage.miniMessage().deserialize(name).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) :
                    Component.text("Tag Name", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            if(hasEffectType() && hasEffectStyle()) {
                lore.add(Component.text("Effect Type: ", NamedTextColor.YELLOW).append(Component.text(effectType, NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("Effect Style: ", NamedTextColor.YELLOW).append(Component.text(effectStyle, NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false));
            }
            if (hasLore()) lore.add(MiniMessage.miniMessage().deserialize(this.lore).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
        });
        inventory.setItem(13, tagPreview);
    }
    public TagsEdit(UUID pUUID, Tags.Tag tag) {
        this.tag = tag;
        isEdit = tag != null;

        if(isEdit) {
            name = tag.display();
            lore = tag.lore();
            effectType = tag.effectType();
            effectStyle = tag.effectStyle();
        }

        HashMap<Tags.Tag, TagsEdit> edits = new HashMap<>();
        if(PlayerManager.tagsEdit.containsKey(pUUID)) edits = PlayerManager.tagsEdit.get(pUUID);
        edits.put(tag, this);
        PlayerManager.tagsEdit.put(pUUID, edits);

        this.inventory = Bukkit.getServer().createInventory(this, 27, Component.text("Tags Edit", TextColor.fromHexString("#0fc3ff")));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        for(int i = 0; i < inventory.getSize(); i++) {
            if (i == 0 || i == 8 || i == 9 || i == 17 || i == 26) {
                inventory.setItem(i, redPane);
            } else if (i < 8 || i == 19 || i == 20 || i == 24 || i == 25 || i == 21 && !isEdit) {
                inventory.setItem(i, blackPane);
            } else if (i == 18) {
                HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
                ItemStack item = hAPI.getItemHead("10306");
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Back to Tags", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 21) {
                ItemStack item = new ItemStack(Material.RED_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text("Delete Tag", NamedTextColor.DARK_RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 22) {
                ItemStack item = new ItemStack(Material.GRAY_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(isEdit ? "Discard Changes" : "Discard Tag", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            } else if (i == 23) {
                ItemStack item = new ItemStack(Material.LIME_CONCRETE);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.displayName(Component.text(isEdit ? "Save Changes" : "Create Tag", NamedTextColor.GREEN, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                item.setItemMeta(itemMeta);
                inventory.setItem(i, item);
            }
        }

        tagName.editMeta(meta -> {
            meta.displayName(Component.text("Tag Name", NamedTextColor.AQUA).append(Component.text(" (REQUIRED)", NamedTextColor.RED, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(preview);
            if(hasName()) lore.add(MiniMessage.miniMessage().deserialize(name).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
        });
        tagLore.editMeta(meta -> {
            meta.displayName(Component.text("Tag Lore", NamedTextColor.AQUA).append(Component.text(" (OPTIONAL)", NamedTextColor.GRAY, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> newLore = new ArrayList<>();
            newLore.add(preview);
            if (hasLore()) newLore.add(MiniMessage.miniMessage().deserialize(this.lore).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(newLore);
        });
        tagEffectType.editMeta(meta -> {
            meta.displayName(Component.text("Effect Type", NamedTextColor.AQUA).append(Component.text(" (OPTIONAL)", NamedTextColor.GRAY, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(preview);
            if (hasEffectType()) lore.add(Component.text(effectType, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        tagEffectStyle.editMeta(meta -> {
            meta.displayName(Component.text("Effect Style", NamedTextColor.AQUA).append(Component.text(" (OPTIONAL)", NamedTextColor.GRAY, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(preview);
            if (hasEffectStyle()) lore.add(Component.text(effectStyle, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
        inventory.setItem(10, tagName);
        inventory.setItem(11, tagLore);
        inventory.setItem(15, tagEffectType);
        inventory.setItem(16, tagEffectStyle);
        updatePreview();
    }
    public Tags.Tag tag() {
        return tag;
    }
    public String name() {
        return this.name;
    }
    public String lore() {
        return this.lore;
    }
    public String effectType() {
        return this.effectType;
    }
    public String effectStyle() {
        return this.effectStyle;
    }
    public boolean changeLock() {
        return this.changeLock;
    }
    public void setChangeLock(boolean lock) {
        this.changeLock = lock;
    }
    public void setName(String name) {
        this.name = name;
        tagName.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(preview);
            if(hasName()) lore.add(MiniMessage.miniMessage().deserialize(name).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(lore);
        });
        inventory.setItem(10, tagName);
        updatePreview();
    }
    public void setLore(String lore) {
        this.lore = lore;
        tagLore.editMeta(meta -> {
            List<Component> newLore = new ArrayList<>();
            newLore.add(preview);
            if (hasLore()) newLore.add(MiniMessage.miniMessage().deserialize(this.lore).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            meta.lore(newLore);
        });
        inventory.setItem(11, tagLore);
        updatePreview();
    }
    public void setEffectType(String type) {
        this.effectType = type;
        tagEffectType.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(preview);
            if (hasEffectType()) lore.add(Component.text(type, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(15, tagEffectType);
        updatePreview();
    }
    public void setEffectStyle(String style) {
        this.effectStyle = style;
        tagEffectStyle.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(preview);
            if (hasEffectStyle()) lore.add(Component.text(style, NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(16, tagEffectStyle);
        updatePreview();
    }
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }
    public boolean hasEffectType() {
        return effectType != null && !effectType.isEmpty();
    }
    public boolean hasEffectStyle() {
        return effectStyle != null && !effectStyle.isEmpty();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
