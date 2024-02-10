package net.skyprison.skyprisoncore.utils;

import com.Zrips.CMI.CMI;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.DefaultStyles;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.skyprison.skyprisoncore.inventories.tags.TagsEdit;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class PlayerManager {
    private static final List<PlayerTag> playerTags = new ArrayList<>();
    public record PlayerTag(UUID playerId, Tags.Tag tag) {}
    public static final HashMap<UUID, HashMap<Tags.Tag, TagsEdit>> tagsEdit = new HashMap<>();
    public static PlayerTag getPlayerTag(UUID pUUID) {
        return playerTags.stream().filter(tag -> tag.playerId().equals(pUUID)).findFirst().orElse(null);
    }
    public static void addPlayerTags(PlayerTag ...tags) {
        Collections.addAll(playerTags, tags);

        Arrays.stream(tags).toList().forEach(playerTag -> {
            Player player = Bukkit.getPlayer(playerTag.playerId);
            if(player != null && playerTag.tag().effectType() != null && playerTag.tag().effectStyle() != null) {
                PlayerParticlesAPI particles = PlayerParticlesAPI.getInstance();
                particles.resetActivePlayerParticles(player);
                particles.addActivePlayerParticle(player, ParticleEffect.fromInternalName(playerTag.tag().effectType()),
                        Tags.effectStyles().stream().filter(style -> style.getName().equalsIgnoreCase(playerTag.tag().effectStyle())).findFirst().orElse(DefaultStyles.NORMAL));
            }
        });
    }
    public static void removePlayerTags(PlayerTag ...tags) {
        if(tags != null) playerTags.removeAll(Arrays.stream(tags).toList());
    }
    public static UUID getPlayerId(String playerName) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE current_name = ?")) {
            ps.setString(1, playerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    return UUID.fromString(rs.getString(1));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (SQLException ignored) {}
        return null;
    }
    public static String getPlayerName(UUID pUUID) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_name FROM users WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException ignored) {}
        return null;
    }
    public static long getPlayerDiscord(UUID pUUID) {
        long discordId = 0;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT discord_id FROM users WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                discordId = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return discordId;
    }
    public static void giveItems(HumanEntity player, ItemStack... items) {
        HashMap<Integer, ItemStack> didntFit = player.getInventory().addItem(Arrays.stream(items).filter(item -> item != null && item.getType().isItem()).toArray(ItemStack[]::new));
        for(ItemStack dropItem : didntFit.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), dropItem).setOwner(player.getUniqueId());
        }
    }
    public static String toBase64(Inventory inv) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inv.getSize());

            for (int i = 0; i < inv.getSize(); i++) {
                dataOutput.writeObject(inv.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode inventory!", e);
        }
    }
    public static Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inv = dataInput.readInt() != 41 ? Bukkit.getServer().createInventory(null, InventoryType.ENDER_CHEST) :
                    Bukkit.getServer().createInventory(null, InventoryType.PLAYER);

            try {
                for (int i = 0; i < inv.getSize(); i++) {
                    try {
                        ItemStack item = (ItemStack) dataInput.readObject();
                        inv.setItem(i, item);
                    } catch (EOFException e) {
                        break;
                    }
                }
            } finally {
                dataInput.close();
            }
            return inv;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to decode inventory!", e);
        }
    }
    public static boolean isPrisonWorld(String world) {
        return world.equals("world_prison") || world.equals("world_free") || world.equals("world_free_nether")
                || world.equals("world_free_the_end") || world.equals("world_skycity") || world.equals("world_prison_tutorial");
    }
    public static void changeInventory(Player player, boolean fromPrison, boolean toPrison) {
        if(fromPrison || toPrison) {
            PlayerInventory pInv = player.getInventory();
            if(fromPrison && !toPrison) {
                try {
                    String inv = toBase64(pInv);
                    String ender = toBase64(player.getEnderChest());
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO player_inventories (user_id, exp, level, health, hunger, inventory, ender_chest) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE exp = VALUE(exp), level = VALUE(level), health = VALUE(health), hunger = VALUE(hunger), " +
                                    "inventory = VALUE(inventory), ender_chest = VALUE(ender_chest)")) {
                        ps.setString(1, player.getUniqueId().toString());
                        ps.setFloat(2, player.getExp());
                        ps.setInt(3, player.getLevel());
                        ps.setDouble(4, player.getHealth());
                        ps.setInt(5, player.getFoodLevel());
                        ps.setString(6, inv);
                        ps.setString(7, ender);
                        ps.executeUpdate();

                        player.setLevel(0);
                        player.setExp(0);
                        player.setHealth(20);
                        player.setFoodLevel(20);
                        player.getInventory().clear();
                        player.getEnderChest().clear();
                    } catch (SQLException e) {
                        throw new IllegalStateException("Failed to save inventory!", e);
                    }
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("Failed to encode inventory!", e);
                }
            } else if(!fromPrison) {
                float exp = 0;
                int level = 0;
                double health = 0;
                int hunger = 0;
                String inv = "";
                String ender = "";
                boolean hasInv = false;
                try(Connection conn = db.getConnection();
                    PreparedStatement ps = conn.prepareStatement("SELECT exp, level, health, hunger, inventory, ender_chest FROM player_inventories WHERE user_id = ?")) {
                    ps.setString(1, player.getUniqueId().toString());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        exp = rs.getFloat(1);
                        level = rs.getInt(2);
                        health = rs.getDouble(3);
                        hunger = rs.getInt(4);
                        inv = rs.getString(5);
                        ender = rs.getString(6);
                        hasInv = true;
                    }
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to get inventory!", e);
                }
                if(!hasInv) return;
                try {
                    player.getInventory().clear();
                    player.getEnderChest().clear();
                    player.setLevel(level);
                    player.setExp(exp);
                    player.setHealth(health);
                    player.setFoodLevel(hunger);
                    player.getInventory().setContents(fromBase64(inv).getContents());
                    player.getEnderChest().setContents(fromBase64(ender).getContents());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to decode inventory!", e);
                }
            }
        }
    }
    public static void checkTotalPurchases(Player player, double total) {
        if (total >= 10.0) {
            if (!player.hasPermission("group.donor1")) {
                Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " parent add donor1");
            } else if (total >= 50.0) {
                if (!player.hasPermission("group.donor2")) {
                    Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " parent add donor2");
                } else if (total >= 100.0) {
                    if (!player.hasPermission("group.donor3")) {
                        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + player.getUniqueId() + " parent add donor3");
                    }
                }
            }
        }
    }
    public static void sendMessage(UUID pUUID, Component msg, String notifType) {
        sendMessage(pUUID, msg, notifType, null, null, true);
    }
    public static void sendMessage(UUID pUUID, Component msg, String notifType, String notifData) {
        sendMessage(pUUID, msg, notifType, notifData, null, true);
    }
    public static void sendMessage(UUID pUUID, Component msg, String notifType, boolean deleteOnView) {
        sendMessage(pUUID, msg, notifType, null, null, deleteOnView);
    }
    public static void sendMessage(UUID pUUID, Component msg, String notifType, String notifData, boolean deleteOnView) {
        sendMessage(pUUID, msg, notifType, notifData, null, deleteOnView);
    }
    public static void sendMessage(UUID pUUID, Component msg, String notifType, String notifData, String notifId, boolean deleteOnView) {
        Player isOnline = Bukkit.getPlayer(pUUID);
        if (isOnline != null) {
            isOnline.sendMessage(msg);
        } else {
            NotificationsUtils.createNotification(notifType, notifData, pUUID, msg, notifId, deleteOnView);
        }
    }
    public static boolean hasPermission(UUID pUUID, String permission) {
        boolean hasPerm = false;
        Player isOnline = Bukkit.getPlayer(pUUID);
        if(isOnline != null) {
            if(isOnline.hasPermission(permission)) {
                hasPerm = true;
            }
        } else {
            LuckPerms luckAPI = LuckPermsProvider.get();
            UserManager userManager = luckAPI.getUserManager();
            CompletableFuture<User> userFuture = userManager.loadUser(pUUID);
            try {
                hasPerm = userFuture.thenApplyAsync(user -> {
                    CachedPermissionData permissionData = user.getCachedData().getPermissionData();
                    return permissionData.checkPermission(permission).asBoolean();
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return hasPerm;
    }
    public static Double getBalance(Player player) {
        return CMI.getInstance().getPlayerManager().getUser(player).getBalance();
    }
    public static long getPlaytime(Player player) {
        return CMI.getInstance().getPlayerManager().getUser(player).getTotalPlayTime();
    }
    public static String getLastIp(Player player) {
        return CMI.getInstance().getPlayerManager().getUser(player).getLastIp();
    }
    public static String getLastIp(UUID player) {
        return CMI.getInstance().getPlayerManager().getUser(player).getLastIp();
    }
    public static String getPrisonRank(Player player) {
        return CMI.getInstance().getPlayerManager().getUser(player).getRank().getName();
    }
    public static String getPrisonRank(UUID player) {
        return CMI.getInstance().getPlayerManager().getUser(player).getRank().getName();
    }
    public static boolean isGuardGear(ItemStack item) {
        boolean isGuardGear = false;
        String name = item.hasDisplayName() ? item.displayName().toString() : "";
        switch (item.getType()) {
            case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS, DIAMOND_SWORD -> isGuardGear = true;
            case BOW -> {
                if(!name.isEmpty() & name.contains("Guard Bow") && item.isUnbreakable()) {
                    isGuardGear = true;
                }
            }
            case SHIELD -> {
                if(!name.isEmpty() & name.contains("Guard Shield") && item.isUnbreakable()) {
                    isGuardGear = true;
                }
            }
        }
        return isGuardGear;
    }

    public static void checkGuardGear(Player player) {
        PlayerInventory pInv = player.getInventory();
        pInv.forEach(item -> {
            if (item != null && isGuardGear(item)) {
                item.setAmount(0);
            }
        });
    }
}
