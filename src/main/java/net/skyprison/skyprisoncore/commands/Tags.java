package net.skyprison.skyprisoncore.commands;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import se.file14.procosmetics.ProCosmetics;
import se.file14.procosmetics.api.ProCosmeticsProvider;
import se.file14.procosmetics.cosmetic.AbstractCosmetic;
import se.file14.procosmetics.cosmetic.AbstractCosmeticType;
import se.file14.procosmetics.cosmetic.CosmeticCategory;
import se.file14.procosmetics.cosmetic.balloon.BalloonType;
import se.file14.procosmetics.cosmetic.particleeffect.ParticleEffectType;
import se.file14.procosmetics.v1_8.Particle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Tags implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook hook;

    public Tags(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }

    public void openGUI(Player player, Integer page) {
        ArrayList<Integer> pTags = new ArrayList<>();
        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT tags_id FROM tags_player WHERE user_id = '" + player.getUniqueId() + "'");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                pTags.add(rs.getInt(1));
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<List> tags = new ArrayList<>();

        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT tags_id, tags_display, tags_lore, tags_effect FROM tags");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                if(pTags.contains(rs.getInt(1))) {
                    tags.add(Arrays.asList(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                }
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double totalPages = Math.ceil(tags.size() / 45.0);

        int toRemove = 45 * (page - 1);
        if(toRemove != 0) {
            toRemove -= 1;
        }
        int b = 0;
        ArrayList<List> toBeRemoved = new ArrayList<>();

        for(List tag : tags) {
            if(b == toRemove) break;
            toBeRemoved.add(tag);
            b++;
        }

        for(List beGone : toBeRemoved) {
            tags.remove(beGone);
        }

        Inventory bounties = Bukkit.createInventory(null, 54, ChatColor.RED + "Tags | Page " + page);
        int j = 0;
        for (List tag : tags) {  // id, display, lore, effect
            if(j == 45) break;
            ArrayList<String> lore = new ArrayList<>();
            ItemStack head = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName((String) tag.get(1));
            lore.add((String) tag.get(2));
            meta.setLore(lore);

            NamespacedKey key3 = new NamespacedKey(plugin, "tag-id");
            meta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, (Integer) tag.get(0));

            if(j == 0) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                meta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tags");

                NamespacedKey key4 = new NamespacedKey(plugin, "page");
                meta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);
            }

            head.setItemMeta(meta);
            bounties.setItem(j, head);
            j++;
        }


        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        nextPage.setItemMeta(nextMeta);
        ItemStack prevPage = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        prevPage.setItemMeta(prevMeta);
        for(int i = 45; i < 54; i++) {
            bounties.setItem(i, pane);
        }

        if(page == totalPages && page > 1) {
            bounties.setItem(46, prevPage);
        } else if(page != totalPages && page == 1) {
            bounties.setItem(52, nextPage);
        } else if (page != 1) {
            bounties.setItem(46, prevPage);
            bounties.setItem(52, nextPage);
        }

        player.openInventory(bounties);
    }


    public void openEditGUI(Player player, Integer page) {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            ProCosmetics api = ProCosmeticsProvider.get();
            AbstractCosmeticType asd = (AbstractCosmeticType) CosmeticCategory.PARTICLE_EFFECTS.getCosmeticTypes().get(0);
            asd.equip(api.getUserManager().getUser(player), true);
            if(args.length == 0) {
                openGUI(player, 1);
            } else {
                if(args.length == 1 && args[0].equalsIgnoreCase("edit")) {
                    if(player.hasPermission("skyprisoncore.command.tags.edit"))
                        openEditGUI(player, 1);
                    else
                        player.sendMessage("&cYou do not have access to this command!");
                }
            }
        }
        return true;
    }
}
