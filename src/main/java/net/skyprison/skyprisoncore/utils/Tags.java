package net.skyprison.skyprisoncore.utils;

import dev.esophose.playerparticles.styles.DefaultStyles;
import dev.esophose.playerparticles.styles.ParticleStyle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.inventories.tags.TagsEdit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.mariadb.jdbc.Statement;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Tags {
    private static final List<ParticleStyle> effectStyles = new ArrayList<>();
    private static final List<Tag> tags = new ArrayList<>();
    public record Tag(int id, String display, String lore, String effectType, String effectStyle, String permission, ItemStack item, ItemStack adminItem) {}
    public static List<ParticleStyle> effectStyles() {
        return effectStyles;
    }
    public static void removeTag(Tag tag) {
        tags.remove(tag);
    }
    public static List<Tag> tags() {
        return tags;
    }
    public static Tag getTag(int id) {
        return tags.stream().filter(tag -> tag.id() == id).findFirst().orElse(null);
    }
    public void loadTags(DatabaseHook db) {
        Field[] fields = DefaultStyles.class.getFields();
        for (Field field : fields) {
            try {
                if (ParticleStyle.class.isAssignableFrom(field.getType())) {
                    effectStyles.add((ParticleStyle) field.get(null));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT tags_id, tags_display, tags_lore, tags_effect_type, tags_effect_style, tags_permission FROM tags")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt(1);
                String display = rs.getString(2);
                String tagLore = rs.getString(3);
                String effectType = rs.getString(4);
                String effectStyle = rs.getString(5);
                String permission = rs.getString(6);
                ItemStack item = new ItemStack(Material.NAME_TAG);
                item.editMeta(meta -> {
                    meta.displayName(MiniMessage.miniMessage().deserialize(display).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    List<Component> lore = new ArrayList<>();
                    if(effectType != null && effectStyle != null) {
                        lore.add(Component.text("Effect Type: ", NamedTextColor.YELLOW).append(Component.text(effectType, NamedTextColor.GRAY))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Effect Style: ", NamedTextColor.YELLOW).append(Component.text(effectStyle, NamedTextColor.GRAY))
                                .decoration(TextDecoration.ITALIC, false));
                    }
                    if(tagLore != null) lore.add(MiniMessage.miniMessage().deserialize(tagLore).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                    meta.lore(lore);
                });
                ItemStack adminItem = item.clone();
                adminItem.editMeta(meta -> {
                    List<Component> lore = meta.lore();
                    lore.add(Component.empty());
                    lore.add(Component.text("Tag ID: ", NamedTextColor.YELLOW).append(Component.text(id, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                tags.add(new Tag(id, display, tagLore, effectType, effectStyle, permission, item, adminItem));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean saveTag(TagsEdit tag, DatabaseHook db) {
        if(tag.name() == null) return false;
        String sql = tag.isEdit ? "UPDATE tags SET tags_display = ?, tags_lore = ?, tags_effect_type = ?, tags_effect_style = ? WHERE tags_id = ?" :
                "INSERT INTO tags (tags_display, tags_lore, tags_effect_type, tags_effect_style) VALUES (?, ?, ?, ?)";
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tag.name());
            ps.setString(2, tag.lore());
            ps.setString(3, tag.effectType());
            ps.setString(4, tag.effectStyle());
            if(tag.isEdit) ps.setInt(5, tag.tag().id());
            ps.executeUpdate();

            int tagId;
            if(tag.isEdit) tagId = tag.tag().id();
            else {
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                tagId = rs.getInt(1);
            }

            if(tag.tag() != null) tags().remove(tag.tag());
            ItemStack item = new ItemStack(Material.NAME_TAG);
            item.editMeta(meta -> {
                meta.displayName(MiniMessage.miniMessage().deserialize(tag.name()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                List<Component> lore = new ArrayList<>();
                if(tag.effectType() != null && tag.effectStyle() != null) {
                    lore.add(Component.text("Effect Type: ", NamedTextColor.YELLOW).append(Component.text(tag.effectType(), NamedTextColor.GRAY))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Effect Style: ", NamedTextColor.YELLOW).append(Component.text(tag.effectStyle(), NamedTextColor.GRAY))
                            .decoration(TextDecoration.ITALIC, false));
                }
                if(tag.lore() != null) lore.add(MiniMessage.miniMessage().deserialize(tag.lore()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(lore);
            });
            ItemStack adminItem = item.clone();
            adminItem.editMeta(meta -> {
                List<Component> lore = meta.lore();
                lore.add(Component.empty());
                lore.add(Component.text("Tag ID: ", NamedTextColor.YELLOW).append(Component.text(tagId, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                meta.lore(lore);
            });

            tags.add(new Tag(tagId, tag.name(), tag.lore(), tag.effectType(), tag.effectStyle(), tag.isEdit ? tag.tag().permission : null, item, adminItem));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
