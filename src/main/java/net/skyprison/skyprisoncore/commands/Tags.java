package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
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
        if(!player.isOp()) {
            try {
                Connection conn = hook.getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT tags_id FROM tags_user WHERE user_id = '" + player.getUniqueId() + "'");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    pTags.add(rs.getInt(1));
                }
                hook.close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        ArrayList<List> tags = new ArrayList<>();

        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT tags_id, tags_display, tags_lore, tags_effect FROM tags");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                if(!player.isOp()) {
                    if (pTags.contains(rs.getInt(1))) {
                        tags.add(Arrays.asList(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
                    }
                } else {
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

        tags.removeAll(toBeRemoved);

        Inventory bounties = Bukkit.createInventory(null, 54, ChatColor.RED + "Tags | Page " + page);
        int j = 0;
        for (List tag : tags) {  // id, display, lore, effect
            if(j == 45) break;
            ArrayList<String> lore = new ArrayList<>();
            ItemStack head = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName((String) tag.get(1));
            if(!String.valueOf(tag.get(2)).isEmpty())
                lore.add((String) tag.get(2));
            meta.setLore(lore);

            NamespacedKey key3 = new NamespacedKey(plugin, "tag-id");
            meta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, (Integer) tag.get(0));

            head.setItemMeta(meta);
            bounties.setItem(j, head);
            j++;
        }

        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        pane.setItemMeta(paneMeta);
        paneMeta.setDisplayName(plugin.colourMessage("&f"));
        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        nextPage.setItemMeta(nextMeta);
        ItemStack prevPage = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
        prevPage.setItemMeta(prevMeta);
        for(int i = 45; i < 54; i++) {
            if(i == 45) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                paneMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                paneMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tags");
                NamespacedKey key4 = new NamespacedKey(plugin, "page");
                paneMeta.getPersistentDataContainer().set(key4, PersistentDataType.INTEGER, page);
                pane.setItemMeta(paneMeta);
            }
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

    public void openNewGUI(Player player, String display, String lore, String effect) {
        Inventory bounties = Bukkit.createInventory(null, 27, ChatColor.RED + "Tags | Create New");
        for (int i = 0; i < 27; i++) {
            if(i == 9) {
                ItemStack head = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&ePreview: &r" + display));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage(lore));
                loreList.add(plugin.colourMessage("&7--"));
                if (!effect.isEmpty())
                    loreList.add(plugin.colourMessage("&6Effect: " + effect));
                else
                    loreList.add(plugin.colourMessage("&6Effect: &lNONE"));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 11) {
                ItemStack head = new ItemStack(Material.OAK_SIGN);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&7Edit Tag Display"));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage("Current name: " + display));
                loreList.add(plugin.colourMessage("&7"));
                loreList.add(plugin.colourMessage("&7--"));
                loreList.add(plugin.colourMessage("&c&lREQUIRED"));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 13) {
                ItemStack head = new ItemStack(Material.SPRUCE_SIGN);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&7Edit Tag Lore"));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage("Current lore: " + lore));
                loreList.add(plugin.colourMessage("&7"));
                loreList.add(plugin.colourMessage("&7--"));
                loreList.add(plugin.colourMessage("&7&lOPTIONAL"));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 15) {
                ItemStack head = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&7Edit Tag Effect"));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage("&6Current Effect: " + effect));
                loreList.add(plugin.colourMessage("&7"));
                loreList.add(plugin.colourMessage("&7--"));
                loreList.add(plugin.colourMessage("&7&lOPTIONAL"));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 22) {
                ItemStack pane = new ItemStack(Material.GREEN_CONCRETE);
                ItemMeta paneMeta = pane.getItemMeta();
                paneMeta.setDisplayName("&cCreate Tag");
                pane.setItemMeta(paneMeta);
                bounties.setItem(i, pane);
            } else {
                ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta paneMeta = pane.getItemMeta();
                paneMeta.setDisplayName("&f");
                if(i == 0) {
                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    paneMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    paneMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tags-new");
                    NamespacedKey key2 = new NamespacedKey(plugin, "tags-display");
                    paneMeta.getPersistentDataContainer().set(key2, PersistentDataType.STRING, display);
                    NamespacedKey key3 = new NamespacedKey(plugin, "tags-lore");
                    paneMeta.getPersistentDataContainer().set(key3, PersistentDataType.STRING, lore);
                    NamespacedKey key4 = new NamespacedKey(plugin, "tags-effect");
                    paneMeta.getPersistentDataContainer().set(key4, PersistentDataType.STRING, effect);
                }
                pane.setItemMeta(paneMeta);
                bounties.setItem(i, pane);
            }
        }


        player.openInventory(bounties);
    }

    public void openSpecificGUI(Player player, Integer tag_id) {
        String name = "";
        String lore = "";
        String effect = "";

        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT tags_display, tags_lore, tags_effect FROM tags WHERE tags_id = '" + tag_id + "'");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                name = rs.getString(1);
                lore = rs.getString(2);
                effect = rs.getString(3);
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Inventory bounties = Bukkit.createInventory(null, 27, ChatColor.RED + "Tags | " + name);
        for (int i = 0; i < 27; i++) {
            if(i == 9) {
                ItemStack head = new ItemStack(Material.NAME_TAG);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&ePreview: &r" + name));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage(lore));
                loreList.add(plugin.colourMessage("&7--"));
                if (!effect.isEmpty())
                    loreList.add(plugin.colourMessage("&6Effect: " + effect));
                else
                    loreList.add(plugin.colourMessage("&6Effect: &lNONE"));
                loreList.add(" ");
                loreList.add(plugin.colourMessage("&eTag ID: &6" + tag_id));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 11) {
                ItemStack head = new ItemStack(Material.OAK_SIGN);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&7Edit Tag Display"));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage("Current name: " + name));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 13) {
                ItemStack head = new ItemStack(Material.SPRUCE_SIGN);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&7Edit Tag Lore"));
                ArrayList<String> loreList = new ArrayList<>();
                loreList.add(plugin.colourMessage("Current lore: " + name));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 15) {
                ItemStack head = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(plugin.colourMessage("&7Edit Tag Effect"));
                ArrayList<String> loreList = new ArrayList<>();
                if (!effect.isEmpty())
                    loreList.add(plugin.colourMessage("&6Current Effect: " + effect));
                else
                    loreList.add(plugin.colourMessage("&6Current Effect: &lNONE"));
                meta.setLore(loreList);
                head.setItemMeta(meta);
                bounties.setItem(i, head);
            } else if(i == 22) {
                ItemStack pane = new ItemStack(Material.NETHER_STAR);
                ItemMeta paneMeta = pane.getItemMeta();
                paneMeta.setDisplayName("&cBack to all tags");
                pane.setItemMeta(paneMeta);
                bounties.setItem(i, pane);
            } else {
                ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
                ItemMeta paneMeta = pane.getItemMeta();
                paneMeta.setDisplayName("&F");
                if(i == 0) {
                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    paneMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    paneMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tags-edit-specific");
                    NamespacedKey key3 = new NamespacedKey(plugin, "tag-id");
                    paneMeta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, tag_id);
                }
                pane.setItemMeta(paneMeta);
                bounties.setItem(i, pane);
            }
        }
        ProCosmetics api = ProCosmeticsProvider.get();
        AbstractCosmeticType asd = (AbstractCosmeticType) CosmeticCategory.PARTICLE_EFFECTS.getCosmeticTypes().get(0);
        asd.getName();
        asd.equip(api.getUserManager().getUser(player), true);


        player.openInventory(bounties);
    }

    public void openEditGUI(Player player, Integer page) {
        ArrayList<List> tags = new ArrayList<>();

        try {
            Connection conn = hook.getSQLConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT tags_id, tags_display, tags_lore, tags_effect FROM tags");
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                tags.add(Arrays.asList(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
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
        tags.removeAll(toBeRemoved);

        Inventory bounties = Bukkit.createInventory(null, 54, ChatColor.RED + "Tags | Page " + page);
        int j = 0;
        for (List tag : tags) {  // id, display, lore, effect
            if(j == 45) break;
            ArrayList<String> lore = new ArrayList<>();
            ItemStack head = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName((String) tag.get(1));
            if(!String.valueOf(tag.get(2)).isEmpty())
                lore.add((String) tag.get(2));
            if(!String.valueOf(tag.get(3)).isEmpty()) {
                lore.add(" ");
                lore.add(plugin.colourMessage(("&7--")));
                lore.add(plugin.colourMessage("&eEffect: &6" + tag.get(3)));
            }
            lore.add(" ");
            lore.add(plugin.colourMessage(("&7--")));
            lore.add(plugin.colourMessage("&eTag ID: &6" + tag.get(0)));


            meta.setLore(lore);

            NamespacedKey key3 = new NamespacedKey(plugin, "tag-id");
            meta.getPersistentDataContainer().set(key3, PersistentDataType.INTEGER, (Integer) tag.get(0));

            if(j == 0) {
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                meta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "tags-edit-all");
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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                openGUI(player, 1);
            } else {
                if(args[0].equalsIgnoreCase("edit")) {
                    if(player.hasPermission("skyprisoncore.command.tags.admin"))
                        openEditGUI(player, 1);
                    else
                        player.sendMessage(plugin.colourMessage("&cYou do not have access to this command!"));
                } else if(args[0].equalsIgnoreCase("new")) {
                    if(player.hasPermission("skyprisoncore.command.tags.admin"))
                        openNewGUI(player, "", "", "");
                    else
                        player.sendMessage(plugin.colourMessage("&cYou do not have access to this command!"));
                } else if(args[0].equalsIgnoreCase("give")) {
                    if(player.hasPermission("skyprisoncore.command.tags.admin")) {
                        if(args.length == 3) {
                            CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[1]);
                            if(user != null) {
                                if(plugin.isInt(args[2])) {
                                    Integer tag_id = Integer.parseInt(args[2]);
                                    String sql = "INSERT INTO tags_user (user_id, tags_id) VALUES (?, ?)";
                                    List<Object> params = new ArrayList<Object>() {{
                                        add(user.getUniqueId().toString());
                                        add(tag_id);
                                    }};
                                    hook.sqlUpdate(sql, params);
                                } else
                                    player.sendMessage(plugin.colourMessage("&cThat isnt a number!"));
                            } else
                                player.sendMessage(plugin.colourMessage("&cUser doesn't exist!"));
                        } else
                            player.sendMessage(plugin.colourMessage("&cCorrect Usage: /tags give <player> <tag_id>"));
                    } else
                        player.sendMessage(plugin.colourMessage("&cYou do not have access to this command!"));
                }
            }
        }
        return true;
    }
}
