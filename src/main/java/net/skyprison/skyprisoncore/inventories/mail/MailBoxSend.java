package net.skyprison.skyprisoncore.inventories.mail;

import com.destroystokyo.paper.MaterialTags;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class MailBoxSend implements CustomInventory {
    private final Inventory inventory;
    private final SkyPrisonCore plugin;
    private final Timer timer = new Timer();
    private final DatabaseHook db;
    private final Player player;
    private boolean canSendItems;
    private final HashMap<UUID, String> sendTo = new HashMap<>();
    private boolean sendingItem = false;
    private final ItemStack sendItem;
    private final ItemStack blackPane;
    private ItemStack book = null;
    private ItemStack offHand = null;
    private final NamespacedKey key;

    private void updateSendTo() {
        ItemStack addReceiver = new ItemStack(Material.PLAYER_HEAD);
        addReceiver.editMeta(meta -> {
            meta.displayName(Component.text("Sending To", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            sendTo.forEach((uuid, name) -> lore.add(Component.text(name, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        inventory.setItem(13, addReceiver);
    }
    private void updateSendType() {
        ItemStack toggleSend = new ItemStack(Material.PAPER);
        toggleSend.editMeta(meta -> {
            meta.displayName(Component.text("Toggle Sending Type", NamedTextColor.YELLOW, TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Currently: ", NamedTextColor.GRAY)
                    .append(Component.text(sendingItem ? "SENDING ITEM" : "SENDING MESSAGE", NamedTextColor.WHITE, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(12, toggleSend);
        if(sendingItem) {
            inventory.setItem(1, sendItem);
            inventory.setItem(10, null);
            sendTo.clear();
            updateSendTo();
        } else {
            inventory.setItem(1, blackPane);
            inventory.setItem(10, blackPane);
        }
    }
    public void updateCost() {
        if(canAfford()) {
            ItemStack send = new ItemStack(Material.WRITTEN_BOOK);
            send.editMeta(meta -> {
                meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                meta.displayName(Component.text("Send Mail", NamedTextColor.GRAY, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                if (getCost() != 0) {
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(getCost()), NamedTextColor.GREEN))
                            .decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                }
            });
            inventory.setItem(14, send);
        } else {
            ItemStack send = new ItemStack(Material.BARRIER);
            send.editMeta(meta -> {
                meta.displayName(Component.text("Send Mail", NamedTextColor.GRAY, TextDecoration.BOLD)
                        .decoration(TextDecoration.ITALIC, false));
                if (getCost() != 0) {
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Cost: ", NamedTextColor.GRAY).append(Component.text(ChatUtils.formatNumber(getCost()), NamedTextColor.GREEN))
                            .decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("CAN'T AFFORD!", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                }
            });
            inventory.setItem(14, send);
        }
    }
    public MailBoxSend(SkyPrisonCore plugin, DatabaseHook db, Player player, boolean canSendItems) {
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, 27, Component.text("Mailbox Settings", NamedTextColor.AQUA));
        this.db = db;
        this.player = player;
        this.canSendItems = canSendItems;
        this.key = new NamespacedKey(plugin, "mail-book");

        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));

        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        HeadDatabaseAPI hAPI = new HeadDatabaseAPI();
        sendItem = hAPI.getItemHead("10307");
        sendItem.editMeta(meta -> meta.displayName(Component.text("Insert item below", NamedTextColor.GRAY)));
        for(int i = 0; i < 27; i++) {
            switch (i) {
                case 0,8,9,17,18,26 -> inventory.setItem(i, redPane);
                case 1, 2, 3, 4, 5, 6, 7, 11, 19, 20, 21, 22, 23, 24, 25, 10 -> inventory.setItem(i, blackPane);
                case 13 -> {
                    ItemStack addReceiver = new ItemStack(Material.PLAYER_HEAD);
                    addReceiver.editMeta(meta -> {
                        meta.displayName(Component.text("Sending To", NamedTextColor.YELLOW, TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false));
                        List<Component> lore = new ArrayList<>();
                        sendTo.forEach((uuid, name) -> lore.add(Component.text(name, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                        meta.lore(lore);
                    });
                    inventory.setItem(i, addReceiver);
                }
                case 14 -> {
                    ItemStack send = new ItemStack(Material.WRITTEN_BOOK);
                    send.editMeta(meta -> {
                        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
                        meta.displayName(Component.text("Send Mail", NamedTextColor.GRAY, TextDecoration.BOLD)
                                .decoration(TextDecoration.ITALIC, false));
                    });
                    inventory.setItem(i, send);
                }
                case 16 -> {
                    ItemStack cancel = new ItemStack(Material.BARRIER);
                    cancel.editMeta(meta -> meta.displayName(Component.text("Delete Mail", NamedTextColor.RED, TextDecoration.BOLD)
                            .decoration(TextDecoration.ITALIC, false)));
                    inventory.setItem(i, cancel);
                }
            }
        }
        updateSendTo();
        if(canSendItems) {
            updateSendType();
        }
        plugin.mailSend.put(player.getUniqueId(), this);
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
    public void addSendTo(UUID pUUID, String name) {
        sendTo.put(pUUID, name);
        updateSendTo();
    }
    public void removeSendTo(UUID pUUID) {
        sendTo.remove(pUUID);
        updateSendTo();
    }
    public void sendMail(ItemStack itemToSend) {
        plugin.mailSend.remove(player.getUniqueId());
        sendTo.forEach((uuid, name) -> {
            int mailBox = MailUtils.getValidMailBox(uuid);
            String mailBoxName = MailUtils.getMailBoxName(mailBox);
            try (Connection conn = db.getConnection(); PreparedStatement ps =
                    conn.prepareStatement("INSERT INTO mails (sender_id, receiver_id, item, cost, mailbox_id, sent_at, collected) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, uuid.toString());
                ps.setBytes(3, itemToSend.serializeAsBytes());
                ps.setInt(4, getCost());
                ps.setInt(5, mailBox);
                ps.setLong(6, System.currentTimeMillis());
                ps.setInt(7, 0);
                ps.executeUpdate();
                Player receiver = plugin.getServer().getPlayer(uuid);
                if(receiver != null) {
                    receiver.sendMessage(Component.text(player.getName(), NamedTextColor.GOLD).append(Component.text(" has sent you mail! Check your ", NamedTextColor.YELLOW))
                            .append(Component.text(Objects.requireNonNullElse(mailBoxName, "COULDN'T GET MAILBOX NAME"), NamedTextColor.GOLD))
                            .append(Component.text(" mailbox to collect it!", NamedTextColor.YELLOW)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        cancelTimer();
        player.sendMessage(Component.text("Mail sent to ", NamedTextColor.YELLOW).append(Component.text(String.join(", ", sendTo.values()), NamedTextColor.GOLD)));
    }
    public int getCost() {
        int cost = 0;
        if(getSendingType()) {
            ItemStack item = getSendItem();
            if(item != null) {
                cost = 50;
                int amount = item.getAmount();
                cost = cost * amount;
                Material type = item.getType();
                if(MaterialTags.DIAMOND_TOOLS.isTagged(type)) {
                    cost = cost * 10;
                } else if(MaterialTags.IRON_TOOLS.isTagged(type)) {
                    cost = cost * 5;
                } else if(getExpensiveMaterials().contains(type)) {
                    cost = cost * 15;
                }
            }
        }
        return cost;
    }

    public List<Material> getExpensiveMaterials() {
        List<Material> mats = new ArrayList<>(List.of(Material.NETHER_STAR, Material.EXPERIENCE_BOTTLE, Material.ENCHANTING_TABLE, Material.ENCHANTED_GOLDEN_APPLE, Material.GOLDEN_APPLE,
                Material.ENCHANTED_BOOK, Material.TOTEM_OF_UNDYING, Material.BEACON, Material.HOPPER, Material.HOPPER_MINECART, Material.BELL));
        mats.addAll(MaterialTags.MUSIC_DISCS.getValues());
        return mats;
    }

    public boolean alreadySending(UUID pUUID) {
        return sendTo.containsKey(pUUID);
    }
    public HashMap<UUID, String> getSendTo() {
        return this.sendTo;
    }

    public void cancelMail() {
        plugin.mailSend.remove(player.getUniqueId());
        if(getSendingType() && getSendItem() != null) {
            if(player.isOnline()) {
                HashMap<Integer, ItemStack> didntFit = player.getInventory().addItem(getSendItem());
                for (ItemStack dropItem : didntFit.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), dropItem).setOwner(player.getUniqueId());
                }
            } else {
                NotificationsUtils.scheduleForOnline(player.getUniqueId(), "mail-item", Base64.getEncoder().encodeToString(getSendItem().serializeAsBytes()));
            }
        }
    }

    public boolean isBlacklistedItem(Material type) {
        return MaterialTags.ARMOR.isTagged(type) || MaterialTags.ARROWS.isTagged(type) || MaterialTags.SWORDS.isTagged(type) || MaterialTags.INFESTED_BLOCKS.isTagged(type)
                || MaterialTags.SKULLS.isTagged(type) || MaterialTags.SPAWN_EGGS.isTagged(type) || MaterialTags.BUCKETS.isTagged(type) || MaterialTags.FISH_BUCKETS.isTagged(type)
                || MaterialTags.BOWS.isTagged(type) || type.equals(Material.TNT) || type.equals(Material.TNT_MINECART) || type.equals(Material.FIRE_CHARGE)
                || type.equals(Material.FLINT_AND_STEEL) || MaterialTags.RAW_FISH.isTagged(type) || MaterialTags.COOKED_FISH.isTagged(type) || type.equals(Material.SHULKER_BOX)
                || type.equals(Material.BLAZE_ROD) || type.equals(Material.BLAZE_POWDER) || type.equals(Material.END_CRYSTAL) || type.equals(Material.ELYTRA)
                || type.equals(Material.SPAWNER);
    }

    public void setCanSendItems(boolean newCanSendItems) {
        this.canSendItems = newCanSendItems;
        if(canSendItems) updateSendType();
        else inventory.setItem(12, null);
    }

    public void toggleSendingItem() {
        this.sendingItem = !this.sendingItem;
        updateSendType();
        updateCost();
    }
    public boolean canAfford() {
        return PlayerManager.getBalance(player) >= getCost();
    }
    public boolean getCanSendItems() {
        return this.canSendItems;
    }
    public boolean getSendingType() {
        return this.sendingItem;
    }
    public ItemStack getSendItem() {
        return inventory.getItem(10);
    }
    public int getSendToSize() {
        return this.sendTo.size();
    }
    public ItemStack getOffHand() {
        return this.offHand;
    }
    public NamespacedKey getKey() {
        return this.key;
    }
    public void saveBook(ItemStack book) {
        this.book = book;
    }
    public void saveOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }
    public void startTimer() {
        TimerTask update = new TimerTask() {
            @Override
            public void run() {
                if(player != null) {
                    PlayerInventory pInv = player.getInventory();
                    if (pInv.getItemInOffHand().hasItemMeta() && pInv.getItemInOffHand().getPersistentDataContainer().has(key)) {
                        long time = book.getPersistentDataContainer().get(key, PersistentDataType.LONG);
                        if (System.currentTimeMillis() >= time) {
                            if(player.isOnline()) {
                                pInv.setItemInOffHand(getOffHand());
                                pInv.addItem(new ItemStack(Material.WRITABLE_BOOK, getSendToSize()));
                            } else {
                                NotificationsUtils.scheduleForOnline(player.getUniqueId(), "mail-offhand", Base64.getEncoder().encodeToString(getOffHand().serializeAsBytes()));
                            }
                            cancelTimer();
                        }
                    } else cancelTimer();
                } else cancelTimer();
            }
        };
        timer.scheduleAtFixedRate(update, 15000, 15000);
    }
    public void cancelTimer() {
        plugin.writingMail.remove(player.getUniqueId());
        timer.cancel();
    }
}
