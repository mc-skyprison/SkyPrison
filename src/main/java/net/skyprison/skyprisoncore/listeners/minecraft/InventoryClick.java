package net.skyprison.skyprisoncore.listeners.minecraft;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.ParticleStyle;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.inventories.claims.ClaimFlags;
import net.skyprison.skyprisoncore.inventories.claims.ClaimFlagsMobs;
import net.skyprison.skyprisoncore.inventories.claims.ClaimMembers;
import net.skyprison.skyprisoncore.inventories.claims.ClaimPending;
import net.skyprison.skyprisoncore.inventories.economy.BountiesList;
import net.skyprison.skyprisoncore.inventories.economy.BuyBack;
import net.skyprison.skyprisoncore.inventories.economy.EconomyCheck;
import net.skyprison.skyprisoncore.inventories.economy.MoneyHistory;
import net.skyprison.skyprisoncore.inventories.economy.tokens.TokensCheck;
import net.skyprison.skyprisoncore.inventories.economy.tokens.TokensHistory;
import net.skyprison.skyprisoncore.inventories.mail.*;
import net.skyprison.skyprisoncore.inventories.misc.*;
import net.skyprison.skyprisoncore.inventories.recipes.BlockedRecipes;
import net.skyprison.skyprisoncore.inventories.recipes.CustomMain;
import net.skyprison.skyprisoncore.inventories.recipes.CustomRecipe;
import net.skyprison.skyprisoncore.inventories.recipes.CustomRecipes;
import net.skyprison.skyprisoncore.inventories.secrets.Secrets;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsCategoryEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsEdit;
import net.skyprison.skyprisoncore.inventories.secrets.SecretsHistory;
import net.skyprison.skyprisoncore.inventories.smith.BlacksmithTrimmer;
import net.skyprison.skyprisoncore.inventories.smith.EndBlacksmithUpgrade;
import net.skyprison.skyprisoncore.inventories.smith.GrassBlacksmithUpgrade;
import net.skyprison.skyprisoncore.inventories.tags.TagsEdit;
import net.skyprison.skyprisoncore.inventories.tags.TagsView;
import net.skyprison.skyprisoncore.items.Vouchers;
import net.skyprison.skyprisoncore.utils.*;
import net.skyprison.skyprisoncore.utils.claims.ClaimFlag;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import net.skyprison.skyprisoncore.utils.secrets.SecretsUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.skyprison.skyprisoncore.utils.NewsUtils.saveNewsMessage;

public class InventoryClick implements Listener {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PlayerParticlesAPI particles;

    public InventoryClick(SkyPrisonCore plugin, DatabaseHook db, PlayerParticlesAPI particles) {
        this.plugin = plugin;
        this.db = db;
        this.particles = particles;
    }
    public boolean isStick(ItemStack i) {
        if (i != null) {
            return i.getType() == Material.STICK && i.getItemMeta().hasDisplayName()
                    && PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(i.getItemMeta().displayName())).contains("Santa's")
                    && (i.getItemMeta().hasEnchant(Enchantment.KNOCKBACK) && (i.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK) > 1));
        }
        return false;
    }
    public void InvStickFix(Player player) {
        for (int n = 0; n < player.getInventory().getSize(); n++) {
            ItemStack i = player.getInventory().getItem(n);
            if (isStick(i)) {
                ItemMeta asd = Objects.requireNonNull(i).getItemMeta();
                asd.removeEnchant(Enchantment.KNOCKBACK);
                asd.addEnchant(Enchantment.KNOCKBACK, 1, true);
                i.setItemMeta(asd);
            }
        }
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if(event.getInventory().getHolder(false) instanceof CustomInventory) {
            int size = event.getInventory().getSize();
            List<Integer> slots = event.getRawSlots().stream().filter(slot -> slot < size).toList();
            if(!slots.isEmpty()) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0) {
            return;
        }
        if(!(event.getInventory().getHolder(false) instanceof CustomInventory customInv)) {
            if (event.getClickedInventory() instanceof PlayerInventory) {
                if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
                    PlayerManager.checkGuardGear(player);
                }
                InvStickFix(player);

                if (plugin.writingMail.containsKey(player.getUniqueId())) {
                    ItemStack currItem = event.getCurrentItem();
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    boolean isBook = (currItem != null && currItem.getType().equals(Material.WRITABLE_BOOK)) || (offHand.getType().equals(Material.WRITABLE_BOOK));
                    if (isBook) {
                        NamespacedKey key = new NamespacedKey(plugin, "mail-book");
                        if (event.getClick().equals(ClickType.SWAP_OFFHAND)) {
                            if (offHand.getPersistentDataContainer().has(key)) {
                                event.setCancelled(true);
                            }
                        } else if (currItem != null) {
                            if (currItem.hasItemMeta() && currItem.getPersistentDataContainer().has(key)) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
            return;
        }
        ItemStack currItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        boolean isPaper = currItem != null && currItem.getType().equals(Material.PAPER);
        switch (customInv.defaultClickBehavior()) {
            case DISABLE_ALL, ENABLE_SPECIFIC -> event.setCancelled(true);
        }
        if(event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR)) {
            event.setCancelled(true);
            return;
        }
        switch (customInv) {
            case ClaimFlags inv -> {
                Component prefix = ClaimUtils.getPrefix();
                if (currItem != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> inv.updateType(event.isLeftClick());
                        case 50 -> player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaim(), inv.getCanEdit(), inv.getHasPurchased()).getInventory());
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                        default -> {
                            boolean isPane = MaterialTags.STAINED_GLASS_PANES.isTagged(currItem);
                            if (isPane || currItem.getType().isAir() || !inv.getCanEdit()) return;
                            ClaimFlag flagData = inv.getFlag(currItem);

                            if (flagData.getFlag().getGroup().equalsIgnoreCase("purchased") && !inv.getHasPurchased()) {
                                player.sendMessage(prefix.append(Component.text("You need to purchase this flag to use it!", NamedTextColor.RED)));
                                return;
                            }
                            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(inv.getClaim().getWorld()))));
                            if (regionManager == null) {
                                return;
                            }
                            ProtectedRegion region = regionManager.getRegion(inv.getClaim().getId());
                            if (region == null) {
                                return;
                            }
                            List<Flag<?>> flags = flagData.getFlag().getFlags();
                            Flag<?> flag = flags.getFirst();
                            String notSet = flagData.getFlag().getNotSet();
                            boolean regionHas = region.getFlag(flag) != null;
                            boolean isSet = notSet.isEmpty();
                            boolean isDisabled = notSet.equalsIgnoreCase("disabled");
                            if (flag instanceof StateFlag stateFlag) {
                                boolean isAllowed = Objects.equals(region.getFlag(stateFlag), StateFlag.State.ALLOW);
                                StateFlag.State flagState = regionHas ? (isAllowed ? (isDisabled ? null : StateFlag.State.DENY) : null) : (isSet ? StateFlag.State.DENY : StateFlag.State.ALLOW);
                                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                inv.updateFlag(flagData, flagState);
                                flags.forEach(f -> region.setFlag((StateFlag) f, flagState));
                                return;
                            }

                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList(inv, flagData));
                            player.closeInventory();
                            if (flag.equals(Flags.TIME_LOCK)) {
                                player.sendMessage(prefix.append(Component.text("Enter the time you want in 24:00 hour time (Type 'unset' to unset)",
                                        TextColor.fromHexString("#20df80"))));
                                return;
                            }
                            if (flag.equals(Flags.WEATHER_LOCK)) {
                                player.sendMessage(Component.text("Enter the weather you want (Available types are 'Clear', 'Rain', 'Thunder'. Type 'unset' to unset)",
                                        TextColor.fromHexString("#20df80")));
                                return;
                            }
                            player.sendMessage(prefix.append(Component.text("Enter the new " + StringUtils.capitalize(flag.getName().replace("-", " ")) + " value (Type 'unset' to unset)",
                                    TextColor.fromHexString("#20df80"))));
                        }
                    }
                }
            }
            case ClaimFlagsMobs inv -> {
                if (currItem != null) {
                    switch (event.getSlot()) {
                        case 45 -> player.openInventory(new ClaimFlags(plugin, inv.getClaim(), inv.getCanEdit(), inv.getHasPurchased()).getInventory());
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> {
                            if(!inv.getCanEdit()) return;
                            ProtectedRegion region = inv.getRegion();
                            if (region.getFlag(Flags.MOB_SPAWNING) != null) {
                                region.setFlag(Flags.MOB_SPAWNING, null);
                            } else {
                                region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
                            }
                            inv.updateAllSpawn();
                        }
                        case 49 -> inv.updateType(true);
                        case 50 -> player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaim(), inv.getCanEdit(), inv.getHasPurchased()).getInventory());
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                        default -> {
                            if (!currItem.getType().equals(Material.PLAYER_HEAD) || !inv.getCanEdit()) return;
                            inv.updateMob(currItem);
                            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                    }
                }
            }
            case ClaimMembers inv -> {
                if (event.getCurrentItem() != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 49 -> inv.updateType(event.isLeftClick());
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case ClaimPending inv -> {
                if (event.getCurrentItem() != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 49 -> inv.updateType(event.isLeftClick());
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case NewsMessages inv -> {
                if (event.getCurrentItem() != null) {
                    Material clickedMat = event.getCurrentItem().getType();
                    switch (event.getSlot()) {
                        case 47 -> {
                            if (isPaper) {
                                player.openInventory(new NewsMessages(plugin, inv.getDatabase(), inv.getCanEdit(), inv.getPage() - 1).getInventory());
                            }
                        }
                        case 51 -> {
                            if (isPaper) {
                                player.openInventory(new NewsMessages(plugin, inv.getDatabase(), inv.getCanEdit(), inv.getPage() + 1).getInventory());
                            }
                        }
                        case 49 -> {
                            if (clickedMat.equals(Material.LIME_CONCRETE)) {
                                if (plugin.newsEditing.containsKey(player.getUniqueId()) && plugin.newsEditing.get(player.getUniqueId()).containsKey(0)) {
                                    player.openInventory(plugin.newsEditing.get(player.getUniqueId()).get(0).getInventory());
                                } else {
                                    player.openInventory(new NewsMessageEdit(plugin, inv.getDatabase(), player.getUniqueId(), 0).getInventory());
                                }
                            }
                        }
                        default -> {
                            if (!clickedMat.isEmpty() && clickedMat.equals(Material.WRITABLE_BOOK)) {
                                NamespacedKey newsKey = new NamespacedKey(plugin, "news-message");
                                PersistentDataContainer newsPersist = event.getCurrentItem().getPersistentDataContainer();
                                if (newsPersist.has(newsKey, PersistentDataType.INTEGER)) {
                                    int newsMessage = newsPersist.get(newsKey, PersistentDataType.INTEGER);
                                    if (event.isShiftClick() && player.hasPermission("skyprisoncore.command.news.edit")) {
                                        if (plugin.newsEditing.containsKey(player.getUniqueId()) && plugin.newsEditing.get(player.getUniqueId()).containsKey(newsMessage)) {
                                            player.openInventory(plugin.newsEditing.get(player.getUniqueId()).get(newsMessage).getInventory());
                                        } else {
                                            player.openInventory(new NewsMessageEdit(plugin, inv.getDatabase(), player.getUniqueId(), newsMessage).getInventory());
                                        }
                                    } else {
                                        NewsUtils.sendNewsMessage(player, newsMessage);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case NewsMessageEdit inv -> {
                if (event.getCurrentItem() != null) {
                    Material clickedMat = event.getCurrentItem().getType();
                    switch (clickedMat) {
                        case NAME_TAG -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-title", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news title in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current title to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getTitle())));
                        }
                        case WRITABLE_BOOK -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-content", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news content in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current content to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getContent())));
                        }
                        case BOOK -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-hover", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news hover in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current hover to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getHover())));
                        }
                        case DAYLIGHT_DETECTOR -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-permission", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news permission in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current permission to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getPermission())));
                        }
                        case HOPPER -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-priority", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news priority in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current priority to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getPriority()))));
                        }
                        case CHAIN_COMMAND_BLOCK -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-click-type", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news click type in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current click type to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getClickType()))
                                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Options: " +
                                            "<gold><b><click:suggest_command:OPEN_URL><hover:show_text:'<gray>Click to paste to chat'>OPEN_URL</hover></click> " +
                                            "<click:suggest_command:RUN_COMMAND><hover:show_text:'<gray>Click to paste to chat'>RUN_COMMAND</hover></click> " +
                                            "<click:suggest_command:SUGGEST_COMMAND><hover:show_text:'<gray>Click to paste to chat'>SUGGEST_COMMAND</hover></click> " +
                                            "<click:suggest_command:COPY_TO_CLIPBOARD><hover:show_text:'<gray>Click to paste to chat'>COPY_TO_CLIPBOARD</hover></click>")));
                        }
                        case COMMAND_BLOCK -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-click-data", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news click data in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current click data to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getClickData())));
                        }
                        case LIME_CANDLE -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-limited-start", inv));
                            player.closeInventory();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                            Date date = new Date();
                            date.setTime(inv.getLimitedStart());
                            player.sendMessage(Component.text("Type new news start date in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current start date to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(formatter.format(date)))
                                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Format: <gold><b>yyyy/MM/dd (Ex: 2023/01/24)")));
                        }
                        case CLOCK -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-limited-time", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news time limit option in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current time limit option to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getLimitedTime() == 1 ? "ENABLED" : "DISABLED"))
                                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Options: <green><b>ENABLED <white>| <red><b>DISABLED")));
                        }
                        case RED_CANDLE -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("news-limited-end", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new news end date in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current title to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getTitle()))
                                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Format: <gold><b>yyyy/MM/dd (Ex: 2023/01/24)")));
                        }
                        case PLAYER_HEAD ->
                                player.openInventory(new NewsMessages(plugin, db, player.hasPermission("skyprisoncore.command.news.edit"), 1)
                                        .getInventory());
                        case RED_CONCRETE -> {
                            player.closeInventory();
                            plugin.newsMessageChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to delete this news message?", NamedTextColor.GRAY)
                                    .append(Component.text("\nDELETE NEWS MESSAGE", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.newsMessageChanges.contains(player.getUniqueId())) {
                                            plugin.newsMessageChanges.remove(player.getUniqueId());
                                            HashMap<Integer, NewsMessageEdit> newsEdits = plugin.newsEditing.get(player.getUniqueId());
                                            newsEdits.remove(inv.getNewsMessage());
                                            plugin.newsEditing.put(player.getUniqueId(), newsEdits);
                                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM news WHERE id = ?")) {
                                                ps.setInt(1, inv.getNewsMessage());
                                                ps.executeUpdate();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                            audience.sendMessage(Component.text("News message has been deleted!", NamedTextColor.RED));
                                            player.openInventory(new NewsMessages(plugin, db, player.hasPermission("skyprisoncore.command.news.edit"), 1)
                                                    .getInventory());
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        plugin.newsMessageChanges.remove(player.getUniqueId());
                                        audience.sendMessage(Component.text("News message deletion cancelled!", NamedTextColor.GRAY));
                                        player.openInventory(inv.getInventory());
                                    })));
                            player.sendMessage(msg);
                        }
                        case GRAY_CONCRETE -> {
                            player.closeInventory();
                            plugin.newsMessageChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to discard your changes?", NamedTextColor.GRAY)
                                    .append(Component.text("\nDISCARD CHANGES", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.newsMessageChanges.contains(player.getUniqueId())) {
                                            plugin.newsMessageChanges.remove(player.getUniqueId());
                                            HashMap<Integer, NewsMessageEdit> newsEdits = plugin.newsEditing.get(player.getUniqueId());
                                            newsEdits.remove(inv.getNewsMessage());
                                            plugin.newsEditing.put(player.getUniqueId(), newsEdits);
                                            audience.sendMessage(Component.text("Changes have been discarded!", NamedTextColor.RED));
                                            player.openInventory(new NewsMessages(plugin, db, player.hasPermission("skyprisoncore.command.news.edit"), 1)
                                                    .getInventory());
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        plugin.newsMessageChanges.remove(player.getUniqueId());
                                        audience.sendMessage(Component.text("Discard changes cancelled!", NamedTextColor.GRAY));
                                        player.openInventory(inv.getInventory());
                                    })));
                            player.sendMessage(msg);
                        }
                        case LIME_CONCRETE -> {
                            player.closeInventory();
                            plugin.newsMessageChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to save this news message?", NamedTextColor.GRAY)
                                    .append(Component.text("\nSAVE NEWS MESSAGE", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.newsMessageChanges.contains(player.getUniqueId())) {
                                            if (saveNewsMessage(inv, db)) {
                                                plugin.newsMessageChanges.remove(player.getUniqueId());
                                                HashMap<Integer, NewsMessageEdit> newsEdits = plugin.newsEditing.get(player.getUniqueId());
                                                newsEdits.remove(inv.getNewsMessage());
                                                plugin.newsEditing.put(player.getUniqueId(), newsEdits);
                                                audience.sendMessage(Component.text("News message has been saved!", NamedTextColor.GREEN));
                                                player.openInventory(new NewsMessages(plugin, db, player.hasPermission("skyprisoncore.command.news.edit"), 1)
                                                        .getInventory());
                                            } else {
                                                audience.sendMessage(Component.text("Something went wrong when saving! Cancelling..", NamedTextColor.RED));
                                                player.openInventory(inv.getInventory());
                                            }
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        plugin.newsMessageChanges.remove(player.getUniqueId());
                                        audience.sendMessage(Component.text("News message saving cancelled!", NamedTextColor.GRAY));
                                        player.openInventory(inv.getInventory());
                                    })));
                            player.sendMessage(msg);
                        }
                    }
                }
            }
            case DatabaseInventory inv -> {
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    event.setCancelled(false);
                    return;
                }
                if (event.getCurrentItem() == null && inv.getCanEdit()) {
                    player.openInventory(new DatabaseInventoryEdit(plugin, db, player.getUniqueId(), -1, event.getSlot(), inv.getCategory()).getInventory());
                } else if (event.getCurrentItem() != null) {
                    Material clickedMat = event.getCurrentItem().getType();
                    if (!clickedMat.isAir() && clickedMat.isItem() && !clickedMat.equals(Material.BARRIER)) {
                        HashMap<String, Object> item = inv.getItem(event.getSlot());
                        if (item != null) {
                            String voucherType = (String) item.get("price_voucher_type");
                            boolean useMoney = ((int) item.get("price_money") != 0);
                            boolean useTokens = ((int) item.get("price_tokens") != 0);
                            boolean useVouchers = ((int) item.get("price_voucher") != 0 && !item.get("price_voucher_type").toString().equalsIgnoreCase("none"));

                            if (event.isShiftClick() && inv.getCanEdit() && !(useMoney && useTokens && useVouchers)) {
                                player.openInventory(new DatabaseInventoryEdit(plugin, db, player.getUniqueId(), (int) item.get("id"), event.getSlot(),
                                        inv.getCategory()).getInventory());
                            } else if (event.isShiftClick() && event.isLeftClick() && inv.getCanEdit() && (useMoney && useTokens && useVouchers)) {
                                player.openInventory(new DatabaseInventoryEdit(plugin, db, player.getUniqueId(), (int) item.get("id"), event.getSlot(),
                                        inv.getCategory()).getInventory());
                            } else {
                                boolean runCommands = true;

                                int moneyCost = (int) item.get("price_money");
                                int tokenCost = (int) item.get("price_tokens");
                                int voucherCost = (int) item.get("price_voucher");


                                boolean removeMoney = false;
                                boolean removeTokens = false;
                                boolean removeVoucher = false;

                                if (useMoney) {
                                    boolean usingMoney = (!useTokens && !useVouchers) || event.isLeftClick() && !event.isShiftClick();
                                    if (usingMoney) {
                                        if (PlayerManager.getBalance(player) >= moneyCost) {
                                            removeMoney = true;
                                        } else {
                                            player.sendMessage(Component.text("You can't afford this!", NamedTextColor.RED));
                                            runCommands = false;
                                        }
                                    }
                                }

                                if (useTokens) {
                                    boolean usingTokens = false;
                                    if (useMoney && event.isRightClick()) {
                                        usingTokens = true;
                                    } else if (useVouchers && event.isLeftClick()) {
                                        usingTokens = true;
                                    }
                                    if (usingTokens) {
                                        int tokens = TokenUtils.getTokens(player.getUniqueId());
                                        if (tokenCost <= tokens) {
                                            removeTokens = true;
                                        } else {
                                            player.sendMessage(Component.text("You can't afford this!", NamedTextColor.RED));
                                            runCommands = false;
                                        }
                                    }
                                }

                                if (useVouchers) {
                                    boolean usingVouchers = !useMoney && !useTokens
                                            || useMoney && !useTokens && event.isRightClick() && !event.isShiftClick()
                                            || !useMoney && event.isRightClick() && !event.isShiftClick()
                                            || useMoney && useTokens && event.isRightClick() && event.isShiftClick();

                                    if (usingVouchers) {
                                        if (plugin.hasVoucher(player, voucherType, voucherCost)) {
                                            removeVoucher = true;
                                        } else {
                                            player.sendMessage(Component.text("You can't afford this!", NamedTextColor.RED));
                                            runCommands = false;
                                        }
                                    }
                                }

                                if (runCommands) {
                                    String commandString = (String) item.get("commands");
                                    if (commandString != null && !commandString.isEmpty() && !commandString.isBlank()) {
                                        if (commandString.contains("give %player_name%") || commandString.contains("brew create")) {
                                            if (player.getInventory().firstEmpty() == -1) {
                                                player.sendMessage(Component.text("No available space in your inventory!", NamedTextColor.RED));
                                                return;
                                            }
                                        }

                                        List<String> commands = new ArrayList<>();
                                        if (commandString.contains("<new_command>")) {
                                            commands = Arrays.stream(commandString.split("<new_command>")).toList();
                                        } else {
                                            commands.add(commandString);
                                        }

                                        commands = commands.stream().filter(c -> !c.isEmpty()).toList();

                                        commands.forEach(command -> {
                                            command = PlaceholderAPI.setPlaceholders(player, command);
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                                        });
                                    }

                                    if (removeMoney) {
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + moneyCost);
                                    }
                                    if (removeTokens) {
                                        TokenUtils.removeTokens(player.getUniqueId(), tokenCost, inv.getCategory(), clickedMat.toString());
                                    }

                                    if (removeVoucher) {
                                        ItemStack voucher = Vouchers.getVoucherFromType(plugin, voucherType, voucherCost);
                                        player.getInventory().removeItem(voucher);
                                    }

                                    CustomInvUtils.addUses(player.getUniqueId(), (int) item.get("id"), db);
                                    inv.updateUsage(player, event.getSlot());
                                    plugin.getServer().getScheduler().runTask(plugin, () -> inv.updateInventory(player));
                                }
                            }
                        } else {
                            player.sendMessage(Component.text("ERROR! Couldn't find the item that was clicked!", NamedTextColor.RED));
                        }
                    }
                }
            }
            case DatabaseInventoryEdit inv -> {
                if (event.getClickedInventory() instanceof PlayerInventory && !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    event.setCancelled(false);
                    return;
                } else if (event.getCurrentItem() == null) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getCurrentItem() != null) {
                    Material clickedMat = event.getCurrentItem().getType();
                    int clickedSlot = event.getSlot();
                    switch (clickedSlot) {
                        case 10 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-permission", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new item permission in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current permission to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getPermission())));
                        }
                        case 11 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-permission-message", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new permission message in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current permission message to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getPermissionMessage())));
                        }
                        case 13 -> {
                            ItemStack newPreview = player.getItemOnCursor();
                            if (!newPreview.getType().isAir()) {
                                event.setCancelled(false);
                                inv.setItem(newPreview);
                                player.openInventory(inv.getInventory());
                            } else {
                                player.setItemOnCursor(ItemStack.deserializeBytes(inv.getItem()));
                            }
                        }
                        case 15 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-price-money", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new money cost in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current money cost to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getPriceMoney()))));
                        }
                        case 16 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-price-tokens", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new tokens cost in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current tokens cost to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getPriceTokens()))));
                        }
                        case 19 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-commands", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type the command to add in chat: (Type 'cancel' to cancel, type command number to remove existing one)",
                                    NamedTextColor.YELLOW));
                        }
                        case 20 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-max-uses", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new max uses in chat: (Type 'cancel' to cancel, Type 0 for infinite)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current max uses to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getMaxUses()))));
                        }
                        case 21 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-usage-lore", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new usage lore in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current usage lore to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getUsageLore())));
                        }
                        case 24 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-voucher-type", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type the voucher type to set in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current voucher type to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getPriceVoucherType()))
                                    .appendNewline().append(MiniMessage.miniMessage().deserialize("<gray>Options: " +
                                            "<gold><b><click:suggest_command:none><hover:show_text:'<gray>Click to paste to chat'>none</hover></click> " +
                                            "<click:suggest_command:token-shop><hover:show_text:'<gray>Click to paste to chat'>token-shop</hover></click> ")));
                        }
                        case 25 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("item-price-voucher", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new voucher cost in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current voucher cost to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getPriceVoucher()))));
                        }
                        case 27 -> player.openInventory(new DatabaseInventory(plugin, db, player,
                                player.hasPermission("skyprisoncore.inventories." + inv.getCategory() + ".editing"), inv.getCategory()).getInventory());
                        case 30 -> {
                            if (clickedMat.equals(Material.RED_CONCRETE)) {
                                player.closeInventory();
                                plugin.customItemChanges.add(player.getUniqueId());
                                Component msg = Component.text("Are you sure you want to delete this item?", NamedTextColor.GRAY)
                                        .append(Component.text("\nDELETE ITEM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.customItemChanges.contains(player.getUniqueId())) {
                                                plugin.customItemChanges.remove(player.getUniqueId());
                                                HashMap<Integer, DatabaseInventoryEdit> itemEdits = plugin.itemEditing.get(player.getUniqueId());
                                                itemEdits.remove(inv.getItemId());
                                                plugin.itemEditing.put(player.getUniqueId(), itemEdits);
                                                try (Connection conn = db.getConnection(); PreparedStatement ps =
                                                        conn.prepareStatement("DELETE FROM gui_items WHERE id = ?")) {
                                                    ps.setInt(1, inv.getItemId());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                audience.sendMessage(Component.text("Item has been deleted!", NamedTextColor.RED));
                                                player.openInventory(new DatabaseInventory(plugin, db, player,
                                                        player.hasPermission("skyprisoncore.inventories." + inv.getCategory() + ".editing"), inv.getCategory())
                                                        .getInventory());
                                            }
                                        })))
                                        .append(Component.text("     "))
                                        .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            plugin.customItemChanges.remove(player.getUniqueId());
                                            audience.sendMessage(Component.text("Item deletion cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        })));
                                player.sendMessage(msg);
                            }
                        }
                        case 31 -> {
                            player.closeInventory();
                            plugin.customItemChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to discard your changes?", NamedTextColor.GRAY)
                                    .append(Component.text("\nDISCARD CHANGES", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.customItemChanges.contains(player.getUniqueId())) {
                                            plugin.customItemChanges.remove(player.getUniqueId());
                                            HashMap<Integer, DatabaseInventoryEdit> itemEdits = plugin.itemEditing.get(player.getUniqueId());
                                            itemEdits.remove(inv.getItemId());
                                            plugin.itemEditing.put(player.getUniqueId(), itemEdits);
                                            audience.sendMessage(Component.text("Changes have been discarded!", NamedTextColor.RED));
                                            player.openInventory(new DatabaseInventory(plugin, db, player,
                                                    player.hasPermission("skyprisoncore.inventories." + inv.getCategory() + ".editing"), inv.getCategory()).getInventory());
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        plugin.customItemChanges.remove(player.getUniqueId());
                                        audience.sendMessage(Component.text("Discard changes cancelled!", NamedTextColor.GRAY));
                                        player.openInventory(inv.getInventory());
                                    })));
                            player.sendMessage(msg);
                        }
                        case 32 -> {
                            player.closeInventory();
                            plugin.customItemChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to save this item?", NamedTextColor.GRAY)
                                    .append(Component.text("\nSAVE ITEM", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.customItemChanges.contains(player.getUniqueId())) {
                                            if (CustomInvUtils.saveItem(inv)) {
                                                plugin.customItemChanges.remove(player.getUniqueId());
                                                HashMap<Integer, DatabaseInventoryEdit> itemEdits = plugin.itemEditing.get(player.getUniqueId());
                                                itemEdits.remove(inv.getItemId());
                                                plugin.itemEditing.put(player.getUniqueId(), itemEdits);
                                                audience.sendMessage(Component.text("Item has been saved!", NamedTextColor.GREEN));
                                                player.openInventory(new DatabaseInventory(plugin, db, player,
                                                        player.hasPermission("skyprisoncore.inventories." + inv.getCategory() + ".editing"),
                                                        inv.getCategory()).getInventory());
                                            } else {
                                                audience.sendMessage(Component.text("Something went wrong when saving! Cancelling..", NamedTextColor.RED));
                                                player.openInventory(inv.getInventory());
                                            }
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        plugin.customItemChanges.remove(player.getUniqueId());
                                        audience.sendMessage(Component.text("Item saving cancelled!", NamedTextColor.GRAY));
                                        player.openInventory(inv.getInventory());
                                    })));
                            player.sendMessage(msg);
                        }
                    }
                }
            }
            case GrassBlacksmithUpgrade inv -> {
                int clickedSlot = event.getRawSlot();
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    event.setCancelled(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY));
                } else {
                    if (currItem == null && clickedSlot != 10 && clickedSlot != 16) {
                        event.setCancelled(true);
                    } else {
                        switch (clickedSlot) {
                            case 10, 16 -> event.setCancelled(false);
                            case 13 -> {
                                InventoryAction invAction = event.getAction();
                                if (invAction.equals(InventoryAction.PICKUP_ALL) || invAction.equals(InventoryAction.PICKUP_SOME)
                                        || invAction.equals(InventoryAction.PICKUP_HALF) || invAction.equals(InventoryAction.PICKUP_ONE)) {
                                    double price = inv.getPrice();
                                    if (inv.hasMoney(price) == 0) {
                                        event.setCancelled(false);
                                        currItem.editMeta(meta -> {
                                            List<Component> lore = meta.lore();
                                            if (lore != null) {
                                                lore.removeFirst();
                                                lore.removeFirst();
                                                meta.lore(lore);
                                            }
                                        });
                                        if (cursor != null) {
                                            cursor.editMeta(meta -> {
                                                List<Component> lore = meta.lore();
                                                if (lore != null) {
                                                    lore.removeFirst();
                                                    lore.removeFirst();
                                                    meta.lore(lore);
                                                }
                                            });
                                        }
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + price);
                                        inv.resultTaken();
                                    } else {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case BlacksmithTrimmer inv -> {
                int clickedSlot = event.getRawSlot();
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    event.setCancelled(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY));
                } else {
                    if (currItem == null && clickedSlot != 10 && clickedSlot != 11 && clickedSlot != 12) {
                        event.setCancelled(true);
                    } else {
                        switch (clickedSlot) {
                            case 10, 11, 12 -> event.setCancelled(false);
                            case 16 -> {
                                if (currItem.getType().isArmor()) {
                                    InventoryAction invAction = event.getAction();
                                    if (invAction.equals(InventoryAction.PICKUP_ALL) || invAction.equals(InventoryAction.PICKUP_SOME)
                                            || invAction.equals(InventoryAction.PICKUP_HALF) || invAction.equals(InventoryAction.PICKUP_ONE)) {
                                        double price = inv.getPrice();
                                        if (inv.hasMoney(price) == 0) {
                                            event.setCancelled(false);
                                            currItem.editMeta(meta -> meta.lore(null));
                                            if (cursor != null) {
                                                cursor.editMeta(meta -> meta.lore(null));
                                            }
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + price);
                                            inv.resultTaken();
                                        } else {
                                            event.setCancelled(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case EndBlacksmithUpgrade inv -> {
                int clickedSlot = event.getRawSlot();
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    event.setCancelled(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY));
                } else {
                    if (currItem == null && clickedSlot != 10 && clickedSlot != 11 && clickedSlot != 12 && clickedSlot != 13 && clickedSlot != 14) {
                        event.setCancelled(true);
                    } else {
                        switch (clickedSlot) {
                            case 10, 11, 12, 13, 14 -> event.setCancelled(false);
                            case 16 -> {
                                if (!currItem.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
                                    InventoryAction invAction = event.getAction();
                                    if (invAction.equals(InventoryAction.PICKUP_ALL) || invAction.equals(InventoryAction.PICKUP_SOME)
                                            || invAction.equals(InventoryAction.PICKUP_HALF) || invAction.equals(InventoryAction.PICKUP_ONE)) {
                                        double price = inv.getPrice();
                                        if (inv.hasMoney(price) == 0) {
                                            event.setCancelled(false);
                                            currItem.editMeta(meta -> meta.lore(null));
                                            if (cursor != null) {
                                                cursor.editMeta(meta -> meta.lore(null));
                                            }
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + price);
                                            inv.resultTaken();
                                        } else {
                                            event.setCancelled(true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case VoteHistory inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    switch (slot) {
                        case 49 -> inv.updateSort();
                        case 45 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 53 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case MailHistory inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    if (slot >= 0 && slot < 45) {
                        if (currItem.getType().equals(Material.WRITTEN_BOOK)) {
                            player.openBook(currItem);
                        }
                    } else {
                        switch (slot) {
                            case 49 -> inv.updateSort();
                            case 45 -> {
                                if (isPaper) {
                                    inv.updatePage(-1);
                                }
                            }
                            case 53 -> {
                                if (isPaper) {
                                    inv.updatePage(1);
                                }
                            }
                        }
                    }
                }
            }
            case MailBoxSettings inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    switch (slot) {
                        case 10 -> {
                            if (inv.isOwner()) {
                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("mailbox-rename", inv));
                                player.closeInventory();
                                player.sendMessage(Component.text("Type new mailbox name in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste current mailbox name to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(inv.getName())));
                            }
                        }
                        case 12 ->
                                player.openInventory(new MailBoxMembers(plugin, db, inv.isOwner(), inv.getMailBox(), 1).getInventory());
                        case 14 -> {
                            if (inv.isOwner()) {
                                inv.pickupMailbox();
                            }
                        }
                        case 16 -> {
                            if (inv.isOwner()) {
                                if (inv.isNoMail()) {
                                    player.closeInventory();
                                    plugin.deleteMailbox.add(player.getUniqueId());
                                    Component msg = Component.text("Are you sure you want to delete this mailbox?", NamedTextColor.GRAY)
                                            .append(Component.text("\nDELETE MAILBOX", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                if (plugin.deleteMailbox.contains(player.getUniqueId())) {
                                                    inv.deleteMailBox();
                                                    plugin.deleteMailbox.remove(player.getUniqueId());
                                                    player.sendMessage(Component.text("Mailbox has been deleted", NamedTextColor.GRAY));
                                                }
                                            })))
                                            .append(Component.text("     "))
                                            .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                if (plugin.deleteMailbox.contains(player.getUniqueId())) {
                                                    plugin.deleteMailbox.remove(player.getUniqueId());
                                                    audience.sendMessage(Component.text("Mailbox deletion cancelled!", NamedTextColor.GRAY));
                                                    player.openInventory(inv.getInventory());
                                                }
                                            })));
                                    player.sendMessage(msg);
                                } else {
                                    player.sendMessage(Component.text("You can't delete a mailbox with mail in it!", NamedTextColor.RED));
                                }
                            }
                        }
                        case 18 -> {
                            player.openInventory(new MailBox(plugin, db, player, inv.isOwner(), inv.getMailBox(), 1).getInventory());
                        }
                    }
                }
            }
            case MailBoxMembers inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    if (slot == 49 && inv.isOwner()) {
                        plugin.chatLock.put(player.getUniqueId(), Arrays.asList("mailbox-invite", inv));
                        player.closeInventory();
                        player.sendMessage(Component.text("Type player to invite in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW));
                    } else if (currItem.getType().equals(Material.PLAYER_HEAD) && inv.isOwner() && slot >= 0 && slot < 45) {
                        OfflinePlayer member = ((SkullMeta) currItem.getItemMeta()).getOwningPlayer();
                        if (member != null) {
                            UUID memberId = member.getUniqueId();
                            String memberName = PlayerManager.getPlayerName(memberId);
                            if (memberName != null && !memberId.equals(player.getUniqueId())) {
                                player.closeInventory();
                                plugin.kickMemberMailbox.add(player.getUniqueId());
                                Component msg = Component.text("Are you sure you want to kick ", NamedTextColor.GRAY)
                                        .append(Component.text(memberName, NamedTextColor.GRAY, TextDecoration.BOLD)).append(Component.text(" from this mailbox?",
                                                NamedTextColor.GRAY))
                                        .append(Component.text("\nKICK MEMBER", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.kickMemberMailbox.contains(player.getUniqueId())) {
                                                inv.kickMember(memberId);
                                                plugin.kickMemberMailbox.remove(player.getUniqueId());
                                                player.sendMessage(Component.text(memberName + " has been kicked from the mailbox!", NamedTextColor.GRAY));
                                                Component kickMsg = Component.text("You've been kicked from the mailbox ", NamedTextColor.RED)
                                                        .append(Component.text(inv.getName(), NamedTextColor.RED, TextDecoration.BOLD));
                                                Player memberOnline = member.getPlayer();
                                                if (memberOnline != null) {
                                                    memberOnline.sendMessage(kickMsg);
                                                } else {
                                                    NotificationsUtils.createNotification("mailbox-kicked", null, memberId, kickMsg, null, true);
                                                }
                                            }
                                        })))
                                        .append(Component.text("     "))
                                        .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.kickMemberMailbox.contains(player.getUniqueId())) {
                                                plugin.kickMemberMailbox.remove(player.getUniqueId());
                                                audience.sendMessage(Component.text("Mailbox member kicking cancelled!", NamedTextColor.GRAY));
                                                player.openInventory(inv.getInventory());
                                            }
                                        })));
                                player.sendMessage(msg);
                            }
                        }
                    } else if (slot == 47) {
                        inv.updatePage(-1);
                    } else if (slot == 51) {
                        inv.updatePage(1);
                    } else if (slot == 45) {
                        player.openInventory(new MailBoxSettings(plugin, db, inv.getMailBox(), inv.isOwner(), player).getInventory());
                    }
                }
            }
            case MailBox inv -> {
                int clickedSlot = event.getRawSlot();
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    event.setCancelled(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY));
                } else {
                    if (clickedSlot < 45) {
                        InventoryAction invAction = event.getAction();
                        if (currItem != null && invAction.equals(InventoryAction.PICKUP_ALL)) {
                            event.setCurrentItem(inv.getMailItem(currItem));
                            event.setCancelled(false);
                        }
                    } else {
                        if (currItem != null) {
                            switch (clickedSlot) {
                                case 45 -> {
                                    if (isPaper) inv.updatePage(-1);
                                }
                                case 53 -> {
                                    if (isPaper) inv.updatePage(1);
                                }
                                case 48 -> {
                                    if (!currItem.getType().equals(Material.BLACK_STAINED_GLASS_PANE)) {
                                        player.openInventory(new MailBoxSettings(plugin, db, inv.getMailBox(), inv.isOwner(), player).getInventory());
                                    }
                                }
                                case 49 -> {
                                    if (currItem.getType().equals(Material.RED_CONCRETE)) {
                                        inv.setPreferred(true);
                                    }
                                }
                                case 50 -> {
                                    if (!plugin.writingMail.containsKey(player.getUniqueId())) {
                                        if (plugin.mailSend.containsKey(player.getUniqueId())) {
                                            MailBoxSend mailSend = plugin.mailSend.get(player.getUniqueId());
                                            if (!mailSend.getCanSendItems()) mailSend.setCanSendItems(true);
                                            player.openInventory(mailSend.getInventory());
                                        } else {
                                            player.openInventory(new MailBoxSend(plugin, db, player, true).getInventory());
                                        }
                                    } else {
                                        player.sendMessage(Component.text("You can't send another mail while you're currently writing one!", NamedTextColor.RED));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case MailBoxSend inv -> {
                int clickedSlot = event.getRawSlot();
                if (event.getClickedInventory() instanceof PlayerInventory) {
                    event.setCancelled(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY));
                } else {
                    if (currItem == null && clickedSlot != 10) {
                        event.setCancelled(true);
                    } else {
                        switch (clickedSlot) {
                            case 10 -> {
                                if (inv.getSendingType()) {
                                    if (cursor != null && inv.isBlacklistedItem(cursor.getType())) {
                                        player.sendMessage(Component.text("You can't send this item!", NamedTextColor.RED));
                                        event.setCancelled(true);
                                    } else {
                                        plugin.getServer().getScheduler().runTask(plugin, inv::updateCost);
                                        event.setCancelled(false);
                                    }
                                }
                            }
                            case 12 -> {
                                if (inv.getCanSendItems()) {
                                    if (inv.getSendingType() && inv.getSendItem() != null) {
                                        HashMap<Integer, ItemStack> didntFit = player.getInventory().addItem(inv.getSendItem());
                                        for (ItemStack dropItem : didntFit.values()) {
                                            player.getWorld().dropItemNaturally(player.getLocation(), dropItem).setOwner(player.getUniqueId());
                                        }
                                    }
                                    inv.toggleSendingItem();
                                }
                            }
                            case 13 -> {
                                boolean canAdd = true;
                                if (inv.getSendingType()) {
                                    if (inv.getSendToSize() >= 1) canAdd = false;
                                } else if (inv.getSendToSize() >= 5) canAdd = false;
                                if (canAdd) {
                                    plugin.chatLock.put(player.getUniqueId(), Arrays.asList("mailbox-sendto", inv));
                                    player.closeInventory();
                                    player.sendMessage(Component.text("Type a player to send the mail to in chat: " +
                                            "(Type 'cancel' to cancel & type player's name again to remove)", NamedTextColor.YELLOW));
                                } else {
                                    if (inv.getSendingType()) {
                                        UUID receiver = inv.getSendTo().keySet().stream().toList().getFirst();
                                        String name = inv.getSendTo().get(receiver);
                                        inv.removeSendTo(receiver);
                                        player.sendMessage(Component.text("Removed " + name + " from send list!", NamedTextColor.GREEN));
                                    } else {
                                        player.sendMessage(Component.text("You've reached the limit of players you can send this mail to!", NamedTextColor.RED));
                                    }
                                }
                            }
                            case 14 -> {
                                if (inv.getSendToSize() > 0) {
                                    if (inv.getSendingType()) {
                                        if (inv.canAfford()) {
                                            player.closeInventory();
                                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + inv.getCost());
                                            inv.sendMail(inv.getSendItem());
                                        } else {
                                            player.sendMessage(Component.text("You don't have enough money to send this mail!", NamedTextColor.RED));
                                        }
                                    } else {
                                        PlayerInventory pInv = player.getInventory();
                                        if (pInv.containsAtLeast(new ItemStack(Material.WRITABLE_BOOK), inv.getSendToSize())) {
                                            plugin.writingMail.put(player.getUniqueId(), inv);
                                            HashMap<Integer, ItemStack> notRemoved = pInv.removeItemAnySlot(new ItemStack(Material.WRITABLE_BOOK, inv.getSendToSize()));
                                            if (notRemoved.isEmpty()) {
                                                player.closeInventory();
                                                ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
                                                book.editMeta(meta -> {
                                                    meta.displayName(Component.text("Mail", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                                                    List<Component> lore = new ArrayList<>();
                                                    lore.add(Component.text("Will be sent to:", NamedTextColor.YELLOW)
                                                            .decoration(TextDecoration.ITALIC, false));
                                                    inv.getSendTo().forEach((uuid, name) -> lore.add(Component.text(name, NamedTextColor.GRAY)
                                                            .decoration(TextDecoration.ITALIC, false)));
                                                    meta.lore(lore);
                                                    NamespacedKey key = new NamespacedKey(plugin, "mail-book");
                                                    meta.getPersistentDataContainer().set(key, PersistentDataType.LONG,
                                                            System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
                                                });
                                                inv.saveBook(book);
                                                inv.saveOffHand(pInv.getItemInOffHand());
                                                pInv.setItemInOffHand(book);
                                                inv.startTimer();
                                                player.sendMessage(Component.text("Write your message in the book and then sign it to send the mail.",
                                                                NamedTextColor.YELLOW)
                                                        .append(Component.text("\nNOTICE: Book will be deleted in 10 minutes!", NamedTextColor.RED)));
                                            } else {
                                                player.sendMessage(Component.text("You need a book and quill for each player " +
                                                                "you're sending the mail to! You need ", NamedTextColor.RED)
                                                        .append(Component.text(notRemoved.size(), NamedTextColor.RED, TextDecoration.BOLD))
                                                        .append(Component.text(" more Book & Quill(s)!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            int need = inv.getSendToSize() - pInv.all(Material.WRITABLE_BOOK).size();
                                            player.sendMessage(Component.text("You need a book and quill for each player " +
                                                            "you're sending the mail to! You need ", NamedTextColor.RED)
                                                    .append(Component.text(need, NamedTextColor.RED, TextDecoration.BOLD))
                                                    .append(Component.text(" more Book & Quill(s)!", NamedTextColor.RED)));
                                        }
                                    }
                                } else {
                                    player.sendMessage(Component.text("You need to add at least one player to send this mail to!", NamedTextColor.RED));
                                }
                            }
                            case 16 -> {
                                player.closeInventory();
                                plugin.cancelMailSendConfirm.add(player.getUniqueId());
                                Component msg = Component.text("Are you sure you want to delete the mail progress?", NamedTextColor.GRAY)
                                        .append(Component.text("\nDELETE MAIL PROGRESS", NamedTextColor.GREEN, TextDecoration.BOLD)
                                                .clickEvent(ClickEvent.callback(audience -> {
                                                    if (plugin.cancelMailSendConfirm.contains(player.getUniqueId())) {
                                                        plugin.cancelMailSendConfirm.remove(player.getUniqueId());
                                                        inv.cancelMail();
                                                        player.sendMessage(Component.text("In progress mail has been deleted!", NamedTextColor.GRAY));
                                                    }
                                                })))
                                        .append(Component.text("     "))
                                        .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            plugin.cancelMailSendConfirm.remove(player.getUniqueId());
                                            audience.sendMessage(Component.text("Mail progress deletion cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        })));
                                player.sendMessage(msg);
                            }
                        }
                    }
                }
            }
            case Referral inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    switch (slot) {
                        case 49 -> inv.updateSort();
                        case 45 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 53 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case SecretsHistory inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    switch (slot) {
                        case 45 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> inv.updateCategory(event.isLeftClick());
                        case 49 -> inv.updateSort();
                        case 50 -> inv.updateType(event.isLeftClick());
                        case 53 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case Secrets inv -> {
                if (currItem != null) {
                    int slot = event.getRawSlot();
                    if (slot == 36) {
                        if (isPaper) {
                            inv.updatePage(-1);
                        }
                    } else if (slot == 44) {
                        if (isPaper) {
                            inv.updatePage(1);
                        }
                    } else if (slot == 39) {
                        inv.updateCategory(event.isLeftClick());
                        inv.updatePage(0);
                    } else if (slot == 40) {
                        inv.updateType(event.isLeftClick());
                        inv.updatePage(0);
                    } else if (slot == 41) {
                        inv.updateShowing(event.isLeftClick());
                        inv.updatePage(0);
                    } else if (slot == 4) {
                        if (event.isShiftClick() && inv.canEditCategories() && !inv.getCategory().name().equalsIgnoreCase("all")) {
                            player.openInventory(new SecretsCategoryEdit(plugin, db, player.getUniqueId(), inv.getCategory().name()).getInventory());
                        }
                    } else if (inv.getPositions().contains(slot)) {
                        if (event.isShiftClick() && inv.canEditSecrets()) {
                            NamespacedKey key = new NamespacedKey(plugin, "secret-id");
                            int secretId = currItem.getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, -1);
                            if (secretId != -1) {
                                player.openInventory(new SecretsEdit(plugin, db, player.getUniqueId(), secretId).getInventory());
                            }
                        }
                    }
                }
            }
            case SecretsEdit inv -> {
                if (event.getClickedInventory() instanceof PlayerInventory && !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    event.setCancelled(false);
                    return;
                } else if (event.getCurrentItem() == null) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getCurrentItem() != null) {
                    Material clickedMat = event.getCurrentItem().getType();
                    int clickedSlot = event.getSlot();
                    switch (clickedSlot) {
                        case 10 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-name", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new secret name in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current name to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getName())));
                        }
                        case 11 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-location", inv));
                            player.closeInventory();
                            Component signMsg = Component.text("Type what type of sign to get in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .append(Component.text("\nOptions: ", NamedTextColor.GRAY));
                            for (Material sign : MaterialSetTag.STANDING_SIGNS.getValues()) {
                                signMsg = signMsg.append(Component.text("   ")).append(Component.text(sign.toString(), NamedTextColor.GOLD, TextDecoration.BOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(sign.toString())));
                            }
                            for (Material sign : MaterialSetTag.ITEMS_HANGING_SIGNS.getValues()) {
                                signMsg = signMsg.append(Component.text("   ")).append(Component.text(sign.toString(), NamedTextColor.GOLD, TextDecoration.BOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(sign.toString())));
                            }
                            player.sendMessage(signMsg);
                        }
                        case 13 -> {
                            ItemStack newPreview = player.getItemOnCursor();
                            if (!newPreview.getType().isAir()) {
                                event.setCancelled(false);
                                inv.setDisplayItem(newPreview);
                                player.openInventory(inv.getInventory());
                            } else {
                                player.setItemOnCursor(ItemStack.deserializeBytes(inv.getDisplayItem()));
                            }
                        }
                        case 15 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-cooldown", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new secret cooldown in chat: (Type 'cancel' to cancel, Example: 1d, Can only be in WHOLE days)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current cooldown to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getCooldown())));
                        }
                        case 16 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-max-uses", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new secret max uses in chat: (Type 'cancel' to cancel, type 0 for infinite)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current max uses to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getMaxUses()))));
                        }
                        case 19 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category", inv));
                            player.closeInventory();
                            Component categoryMsg = Component.text("Type the new secret category in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current category to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getCategory()))
                                    .append(Component.text("\nOptions: ", NamedTextColor.GRAY));
                            for (String category : SecretsUtils.getCategoryNames()) {
                                categoryMsg = categoryMsg.appendSpace().append(Component.text(category, NamedTextColor.GOLD, TextDecoration.BOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(category)));
                            }
                            player.sendMessage(categoryMsg);
                        }
                        case 20 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-type", inv));
                            player.closeInventory();
                            Component typeMsg = Component.text("Type the new secret type in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current type to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getType()))
                                    .append(Component.text("\nOptions: ", NamedTextColor.GRAY));
                            for (String type : SecretsUtils.getTypes()) {
                                typeMsg = typeMsg.appendSpace().append(Component.text(type, NamedTextColor.GOLD, TextDecoration.BOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(type)));
                            }
                            player.sendMessage(typeMsg);
                        }
                        case 24 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-reward-type", inv));
                            player.closeInventory();
                            Component typeMsg = Component.text("Type the new reward type in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current reward type to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getRewardType()))
                                    .append(Component.text("\nOptions: ", NamedTextColor.GRAY));
                            for (String type : SecretsUtils.getRewardTypes()) {
                                typeMsg = typeMsg.appendSpace().append(Component.text(type, NamedTextColor.GOLD, TextDecoration.BOLD)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(type)));
                            }
                            player.sendMessage(typeMsg);
                        }
                        case 25 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-reward-amount", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new reward amount in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current reward amount to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getRewardAmount()))));
                        }
                        case 27 ->
                                player.openInventory(new Secrets(plugin, db, player, inv.getCategory(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                        player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                        case 30 -> {
                            if (clickedMat.equals(Material.RED_CONCRETE)) {
                                player.closeInventory();
                                plugin.secretChanges.add(player.getUniqueId());
                                Component msg = Component.text("Are you sure you want to delete this secret?", NamedTextColor.GRAY)
                                        .append(Component.text("\nDELETE SECRET", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.secretChanges.contains(player.getUniqueId())) {
                                                plugin.secretChanges.remove(player.getUniqueId());
                                                HashMap<Integer, SecretsEdit> edits = plugin.secretsEditing.get(player.getUniqueId());
                                                edits.remove(inv.getSecretsId());
                                                plugin.secretsEditing.put(player.getUniqueId(), edits);
                                                try (Connection conn = db.getConnection(); PreparedStatement ps =
                                                        conn.prepareStatement("UPDATE secrets SET deleted = 1 WHERE id = ?")) {
                                                    ps.setInt(1, inv.getSecretsId());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                audience.sendMessage(Component.text("Secret has been deleted!", NamedTextColor.RED));
                                                player.openInventory(new Secrets(plugin, db, player, inv.getCategory(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                                        player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                                            }
                                        })))
                                        .append(Component.text("     "))
                                        .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.secretChanges.contains(player.getUniqueId())) {
                                                plugin.secretChanges.remove(player.getUniqueId());
                                                audience.sendMessage(Component.text("Secret deletion cancelled!", NamedTextColor.GRAY));
                                                player.openInventory(inv.getInventory());
                                            }
                                        })));
                                player.sendMessage(msg);
                            }
                        }
                        case 31 -> {
                            player.closeInventory();
                            plugin.secretChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to discard your changes?", NamedTextColor.GRAY)
                                    .append(Component.text("\nDISCARD CHANGES", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretChanges.contains(player.getUniqueId())) {
                                            plugin.secretChanges.remove(player.getUniqueId());
                                            HashMap<Integer, SecretsEdit> edits = plugin.secretsEditing.get(player.getUniqueId());
                                            edits.remove(inv.getSecretsId());
                                            plugin.secretsEditing.put(player.getUniqueId(), edits);
                                            audience.sendMessage(Component.text("Changes have been discarded!", NamedTextColor.RED));
                                            player.openInventory(new Secrets(plugin, db, player, inv.getCategory(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                                    player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretChanges.contains(player.getUniqueId())) {
                                            plugin.secretChanges.remove(player.getUniqueId());
                                            audience.sendMessage(Component.text("Discard changes cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        }
                                    })));
                            player.sendMessage(msg);
                        }
                        case 32 -> {
                            player.closeInventory();
                            plugin.secretChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to save this secret?", NamedTextColor.GRAY)
                                    .append(Component.text("\nSAVE SECRET", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretChanges.contains(player.getUniqueId())) {
                                            if (inv.saveSecret()) {
                                                plugin.secretChanges.remove(player.getUniqueId());
                                                HashMap<Integer, SecretsEdit> edits = plugin.secretsEditing.get(player.getUniqueId());
                                                edits.remove(inv.getSecretsId());
                                                plugin.secretsEditing.put(player.getUniqueId(), edits);
                                                audience.sendMessage(Component.text("Secret has been saved!", NamedTextColor.GREEN));
                                                player.openInventory(new Secrets(plugin, db, player, inv.getCategory(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                                        player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                                            } else {
                                                audience.sendMessage(Component.text("Something went wrong during saving! Cancelling..", NamedTextColor.RED));
                                                player.openInventory(inv.getInventory());
                                            }
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretChanges.contains(player.getUniqueId())) {
                                            plugin.secretChanges.remove(player.getUniqueId());
                                            audience.sendMessage(Component.text("Secret saving cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        }
                                    })));
                            player.sendMessage(msg);
                        }
                    }
                }
            }
            case SecretsCategoryEdit inv -> {
                if (event.getClickedInventory() instanceof PlayerInventory && !event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                    event.setCancelled(false);
                    return;
                } else if (event.getCurrentItem() == null) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getCurrentItem() != null) {
                    Material clickedMat = event.getCurrentItem().getType();
                    int clickedSlot = event.getSlot();
                    switch (clickedSlot) {
                        case 10 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category-name", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new category name in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current name to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getName())));
                        }
                        case 11 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category-description", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new category description in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current description to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getDescription())));
                        }
                        case 13 -> {
                            ItemStack newPreview = player.getItemOnCursor();
                            if (!newPreview.getType().isAir()) {
                                event.setCancelled(false);
                                inv.setDisplayItem(newPreview);
                                player.openInventory(inv.getInventory());
                            } else {
                                player.setItemOnCursor(ItemStack.deserializeBytes(inv.getDisplayItem()));
                            }
                        }
                        case 16 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category-regions", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type the region & world to add in chat: (Format: <region>:<world>, type 'cancel' to cancel, type region number to remove existing one)",
                                    NamedTextColor.YELLOW));
                        }
                        case 19 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category-order", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new category order in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current order to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(String.valueOf(inv.getOrder()))));
                        }
                        case 24 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category-permission", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new category permission in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current permission to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getPermission())));
                        }
                        case 25 -> {
                            plugin.chatLock.put(player.getUniqueId(), Arrays.asList("secret-category-permission-message", inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type new category permission message in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste current permission message to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(inv.getPermissionMessage())));
                        }
                        case 27 ->
                                player.openInventory(new Secrets(plugin, db, player, inv.getCategoryId(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                        player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                        case 30 -> {
                            if (clickedMat.equals(Material.RED_CONCRETE)) {
                                player.closeInventory();
                                plugin.secretCategoryChanges.add(player.getUniqueId());
                                Component msg = Component.text("Are you sure you want to delete this Secrets Category?", NamedTextColor.GRAY)
                                        .append(Component.text("\nDELETE CATEGORY", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.secretCategoryChanges.contains(player.getUniqueId())) {
                                                plugin.secretCategoryChanges.remove(player.getUniqueId());
                                                HashMap<String, SecretsCategoryEdit> edits = plugin.secretsCatEditing.get(player.getUniqueId());
                                                edits.remove(inv.getCategoryId());
                                                plugin.secretsCatEditing.put(player.getUniqueId(), edits);
                                                try (Connection conn = db.getConnection(); PreparedStatement ps =
                                                        conn.prepareStatement("UPDATE secrets_categories SET deleted = 1 WHERE name = ?")) {
                                                    ps.setString(1, inv.getCategoryId());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                audience.sendMessage(Component.text("Secrets Category has been deleted!", NamedTextColor.RED));
                                                player.openInventory(new Secrets(plugin, db, player, inv.getCategoryId(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                                        player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                                            }
                                        })))
                                        .append(Component.text("     "))
                                        .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                            if (plugin.secretCategoryChanges.contains(player.getUniqueId())) {
                                                plugin.secretCategoryChanges.remove(player.getUniqueId());
                                                audience.sendMessage(Component.text("Secrets Category deletion cancelled!", NamedTextColor.GRAY));
                                                player.openInventory(inv.getInventory());
                                            }
                                        })));
                                player.sendMessage(msg);
                            }
                        }
                        case 31 -> {
                            player.closeInventory();
                            plugin.secretCategoryChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to discard your changes?", NamedTextColor.GRAY)
                                    .append(Component.text("\nDISCARD CHANGES", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretCategoryChanges.contains(player.getUniqueId())) {
                                            plugin.secretCategoryChanges.remove(player.getUniqueId());
                                            HashMap<String, SecretsCategoryEdit> edits = plugin.secretsCatEditing.get(player.getUniqueId());
                                            edits.remove(inv.getCategoryId());
                                            plugin.secretsCatEditing.put(player.getUniqueId(), edits);
                                            audience.sendMessage(Component.text("Changes have been discarded!", NamedTextColor.RED));
                                            player.openInventory(new Secrets(plugin, db, player, inv.getCategoryId(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                                    player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretCategoryChanges.contains(player.getUniqueId())) {
                                            plugin.secretCategoryChanges.remove(player.getUniqueId());
                                            audience.sendMessage(Component.text("Discard changes cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        }
                                    })));
                            player.sendMessage(msg);
                        }
                        case 32 -> {
                            player.closeInventory();
                            plugin.secretCategoryChanges.add(player.getUniqueId());
                            Component msg = Component.text("Are you sure you want to save this secrets category?", NamedTextColor.GRAY)
                                    .append(Component.text("\nSAVE CATEGORY", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretCategoryChanges.contains(player.getUniqueId())) {
                                            if (inv.saveCategory()) {
                                                plugin.secretCategoryChanges.remove(player.getUniqueId());
                                                HashMap<String, SecretsCategoryEdit> edits = plugin.secretsCatEditing.get(player.getUniqueId());
                                                edits.remove(inv.getCategoryId());
                                                plugin.secretsCatEditing.put(player.getUniqueId(), edits);
                                                audience.sendMessage(Component.text("Secrets Category has been saved!", NamedTextColor.GREEN));
                                                player.openInventory(new Secrets(plugin, db, player, inv.getCategoryId(), player.hasPermission("skyprisoncore.command.secrets.create.secret"),
                                                        player.hasPermission("skyprisoncore.command.secrets.create.category")).getInventory());
                                            } else {
                                                audience.sendMessage(Component.text("Something went wrong during saving! Cancelling..", NamedTextColor.RED));
                                                player.openInventory(inv.getInventory());
                                            }
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (plugin.secretCategoryChanges.contains(player.getUniqueId())) {
                                            plugin.secretCategoryChanges.remove(player.getUniqueId());
                                            audience.sendMessage(Component.text("Secrets Category saving cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        }
                                    })));
                            player.sendMessage(msg);
                        }
                    }
                }
            }
            case BountiesList inv -> {
                if (event.getCurrentItem() != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case BuyBack inv -> {
                if (currItem != null && Arrays.asList(11, 12, 13, 14, 15).contains(event.getSlot())) {
                    NamespacedKey key = new NamespacedKey(plugin, "sold-id");
                    PersistentDataContainer soldData = currItem.getPersistentDataContainer();
                    int itemId = soldData.getOrDefault(key, PersistentDataType.INTEGER, -1);
                    if (itemId == -1) return;
                    BuyBack.SoldItem itemData = inv.getSoldItem(itemId);
                    if (PlayerManager.getBalance(player) >= itemData.price()) {
                        ItemStack item = new ItemStack(itemData.itemType(), itemData.amount());
                        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
                        if (!leftovers.isEmpty()) {
                            leftovers.values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left).setOwner(player.getUniqueId()));
                            player.sendMessage(Component.text("Not enough inventory space! Dropping remaining items..", NamedTextColor.RED));
                        }
                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                                "UPDATE logs_shop SET bought_back = ? WHERE id = ?")) {
                            ps.setInt(1, 1);
                            ps.setInt(2, itemId);
                            ps.executeUpdate();
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + itemData.price());
                            inv.updateInventory(itemId);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        player.sendMessage(Component.text("You do not have enough money!", NamedTextColor.RED));
                    }
                }
            }
            case EconomyCheck inv -> {
                if (event.getCurrentItem() != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> inv.updateSort(event.isLeftClick());
                        case 50 -> {
                            player.closeInventory();
                            player.sendMessage(Component.text("/econcheck <player>", NamedTextColor.RED));
                        }
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case MoneyHistory inv -> {
                if (event.getCurrentItem() != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> inv.updateSort();
                        case 50 -> inv.updateType(event.isLeftClick());
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case TokensCheck inv -> {
                if (currItem != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> inv.updateSort(event.isLeftClick());
                        case 50 -> {
                            player.closeInventory();
                            player.sendMessage(Component.text("/econcheck <player>", NamedTextColor.RED));
                        }
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case TokensHistory inv -> {
                if (currItem != null) {
                    switch (event.getSlot()) {
                        case 46 -> {
                            if (isPaper) {
                                inv.updatePage(-1);
                            }
                        }
                        case 48 -> inv.updateSort();
                        case 50 -> inv.updateType(event.isLeftClick());
                        case 52 -> {
                            if (isPaper) {
                                inv.updatePage(1);
                            }
                        }
                    }
                }
            }
            case CustomMain inv -> {
                if (currItem != null) {
                    switch (event.getSlot()) {
                        case 12 -> player.openInventory(new BlockedRecipes().getInventory());
                        case 14 -> player.openInventory(new CustomRecipes().getInventory());
                    }
                }
            }
            case BlockedRecipes ignored -> {
                if (currItem != null) {
                    if (event.getSlot() == 45) {
                        player.openInventory(new CustomMain().getInventory());
                    }
                }
            }
            case CustomRecipes inv -> {
                if (currItem != null) {
                    if(event.getSlot() == 45) {
                        player.openInventory(new CustomMain().getInventory());
                    } else if(!MaterialTags.STAINED_GLASS_PANES.isTagged(currItem.getType())) {
                        CraftingRecipe recipe = inv.getRecipe(currItem);
                        if(recipe != null) {
                            player.openInventory(new CustomRecipe(recipe).getInventory());
                        }

                    }
                }
            }
            case CustomRecipe ignored -> {
                if (currItem != null) {
                    if (event.getSlot() == 36) {
                        player.openInventory(new CustomRecipes().getInventory());
                    }
                }
            }
            case Daily inv -> {
                if(event.getSlot() != 13 || currItem == null || !currItem.getType().equals(Material.CHEST_MINECART)) return;

                int currStreak = inv.getCurrStreak();
                int highestStreak = inv.getHighestStreak();
                int totalCollected = inv.getTotalCollected();
                String lastColl = inv.getLastCollected();

                int tReward = new Random().nextInt(25) + 25;
                if ((currStreak + 1) % 7 == 0) {
                    tReward = 250;
                }

                int randInt = new Random().nextInt(1000);
                if (randInt == 666) {
                    tReward = randInt;
                }

                int nCurrStreak = currStreak + 1;
                int nTotalCollected = totalCollected + 1;

                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String currDate = formatter.format(date);

                boolean newHigh = lastColl.isEmpty() || currStreak >= highestStreak;
                String sql = !lastColl.isEmpty() ? "UPDATE dailies SET current_streak = ?, total_collected = ?, last_collected = ?" +
                        (newHigh ? ", highest_streak = ?" : "") + " WHERE user_id = ?"
                        : "INSERT INTO dailies (current_streak, total_collected, last_collected, highest_streak, user_id) VALUES (?, ?, ?, ?, ?)";

                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, nCurrStreak);
                    ps.setInt(2, nTotalCollected);
                    ps.setString(3, currDate);
                    if(newHigh) ps.setInt(4, nCurrStreak);
                    ps.setString(newHigh ? 5 : 4, player.getUniqueId().toString());
                    ps.executeUpdate();
                    TokenUtils.addTokens(player.getUniqueId(), tReward, "Daily Reward", currStreak + " Days");
                    player.openInventory(new Daily(db, player).getInventory());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            case PlotTeleport inv -> {
                if (currItem != null && !MaterialTags.STAINED_GLASS_PANES.isTagged(currItem.getType())) {
                    inv.teleport(currItem);
                }
            }
            case TagsView inv -> {
                if (currItem == null || !currItem.getType().isItem()) return;
                switch (event.getSlot()) {
                    case 46 -> {
                        if (isPaper) {
                            inv.updatePage(-1);
                        }
                    }
                    case 49 -> inv.updateCurrentTag(null);
                    case 52 -> {
                        if (isPaper) {
                            inv.updatePage(1);
                        }
                    }
                    case 53 -> {
                        if (inv.isAdmin()) {
                            if (PlayerManager.tagsEdit.containsKey(player.getUniqueId()) && PlayerManager.tagsEdit.get(player.getUniqueId()).containsKey(null)) {
                                player.openInventory(PlayerManager.tagsEdit.get(player.getUniqueId()).get(null).getInventory());
                            } else {
                                player.openInventory(new TagsEdit(player.getUniqueId(), null).getInventory());
                            }
                        }
                    }
                    default -> {
                        if (currItem.getType().equals(Material.NAME_TAG)) {
                            if(event.getClick().isShiftClick() && inv.isAdmin()) {
                                player.openInventory(new TagsEdit(player.getUniqueId(), inv.getTag(currItem)).getInventory());
                            } else {
                                inv.updateCurrentTag(new PlayerManager.PlayerTag(player.getUniqueId(), inv.getTag(currItem)));
                                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            }
                        }
                    }
                }
            }
            case TagsEdit inv -> {
                if (currItem == null || !currItem.getType().isItem()) return;
                switch (event.getSlot()) {
                    case 10 -> {
                        plugin.chatLock.put(player.getUniqueId(), Arrays.asList(inv, "name"));
                        player.closeInventory();
                        player.sendMessage(Component.text("Type new tag name in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(Component.text("Click to paste current name to chat", NamedTextColor.GRAY)))
                                .clickEvent(ClickEvent.suggestCommand(inv.hasName() ? inv.name() : "")));
                    }
                    case 11 -> {
                        plugin.chatLock.put(player.getUniqueId(), Arrays.asList(inv, "lore"));
                        player.closeInventory();
                        player.sendMessage(Component.text("Type new news content in chat: (Type 'cancel' to cancel, 'null' to reset)", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(Component.text("Click to paste current lore to chat", NamedTextColor.GRAY)))
                                .clickEvent(ClickEvent.suggestCommand(inv.hasLore() ? inv.lore() : "")));
                    }
                    case 15 -> {
                        plugin.chatLock.put(player.getUniqueId(), Arrays.asList(inv, "effect-type"));
                        player.closeInventory();
                        List<ParticleEffect> particleList = ParticleEffect.getEnabledEffects();
                        List<String> particleNames = particleList.stream().map(ParticleEffect::getName).toList();
                        Component availParticles = Component.empty();
                        for(String name : particleNames) {
                            availParticles = availParticles.append(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(name))).appendSpace();
                        }
                        player.sendMessage(availParticles.append(Component.text("\nAvailable Effect Types Above", NamedTextColor.YELLOW))
                                .append(Component.text("\nType new tag effect in chat: (Type 'cancel' to cancel, 'null' to reset)", NamedTextColor.YELLOW)
                                .hoverEvent(HoverEvent.showText(Component.text("Click to paste current effect to chat", NamedTextColor.GRAY)))
                                .clickEvent(ClickEvent.suggestCommand(inv.hasEffectType() ? inv.effectType() : ""))));
                    }
                    case 16 -> {
                        plugin.chatLock.put(player.getUniqueId(), Arrays.asList(inv, "effect-style"));
                        player.closeInventory();
                        List<ParticleStyle> particleList = Tags.effectStyles();
                        List<String> particleNames = particleList.stream().map(ParticleStyle::getName).toList();
                        Component availParticles = Component.empty();
                        for(String name : particleNames) {
                            availParticles = availParticles.append(Component.text(name, NamedTextColor.GOLD, TextDecoration.BOLD)
                                    .hoverEvent(HoverEvent.showText(Component.text("Click to paste to chat", NamedTextColor.GRAY)))
                                    .clickEvent(ClickEvent.suggestCommand(name))).appendSpace();
                        }
                        player.sendMessage(availParticles.append(Component.text("\nAvailable Effect Styles Above", NamedTextColor.YELLOW))
                                .append(Component.text("\nType new tag effect in chat: (Type 'cancel' to cancel, 'null' to reset)", NamedTextColor.YELLOW)
                                        .hoverEvent(HoverEvent.showText(Component.text("Click to paste current effect to chat", NamedTextColor.GRAY)))
                                        .clickEvent(ClickEvent.suggestCommand(inv.hasEffectStyle() ? inv.effectStyle() : ""))));
                    }
                    case 18 -> player.openInventory(new TagsView(player).getInventory());
                    case 21 -> {
                        if(isPaper) return;
                        player.closeInventory();
                        inv.setChangeLock(true);
                        Component msg = Component.text("Are you sure you want to delete this tag?", NamedTextColor.GRAY)
                                .append(Component.text("\nDELETE TAG", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                    if (inv.changeLock()) {
                                        inv.setChangeLock(false);
                                        HashMap<Tags.Tag, TagsEdit> tagEdits = PlayerManager.tagsEdit.get(player.getUniqueId());
                                        tagEdits.remove(inv.tag());
                                        PlayerManager.tagsEdit.put(player.getUniqueId(), tagEdits);
                                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tags WHERE tags_id = ?")) {
                                            ps.setInt(1, inv.tag().id());
                                            ps.executeUpdate();
                                            Tags.removeTag(inv.tag());
                                            audience.sendMessage(Component.text("Tag has been deleted!", NamedTextColor.RED));
                                            player.openInventory(new TagsView(player).getInventory());
                                        } catch (SQLException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })))
                                .append(Component.text("     "))
                                .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                    inv.setChangeLock(false);
                                    audience.sendMessage(Component.text("Tag deletion cancelled!", NamedTextColor.GRAY));
                                    player.openInventory(inv.getInventory());
                                })));
                        player.sendMessage(msg);
                    }
                    case 22 -> {
                        player.closeInventory();
                        inv.setChangeLock(true);
                        Component msg = Component.text("Are you sure you want to discard your changes?", NamedTextColor.GRAY)
                                .append(Component.text("\nDISCARD CHANGES", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                    if (inv.changeLock()) {
                                        inv.setChangeLock(false);
                                        HashMap<Tags.Tag, TagsEdit> tagEdits = PlayerManager.tagsEdit.get(player.getUniqueId());
                                        tagEdits.remove(inv.tag());
                                        PlayerManager.tagsEdit.put(player.getUniqueId(), tagEdits);
                                        audience.sendMessage(Component.text("Changes have been discarded!", NamedTextColor.RED));
                                        player.openInventory(new TagsView(player).getInventory());
                                    }
                                })))
                                .append(Component.text("     "))
                                .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                    inv.setChangeLock(false);
                                    audience.sendMessage(Component.text("Discard changes cancelled!", NamedTextColor.GRAY));
                                    player.openInventory(inv.getInventory());
                                })));
                        player.sendMessage(msg);
                    }
                    case 23 -> {
                        if(!inv.hasName()) {
                            player.sendMessage(Component.text("Tag name is required!", NamedTextColor.RED));
                            return;
                        }
                        player.closeInventory();
                        inv.setChangeLock(true);
                        Component msg = Component.text("Are you sure you want to save this tag?", NamedTextColor.GRAY)
                                .append(Component.text("\nSAVE TAG", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                    if (inv.changeLock()) {
                                        inv.setChangeLock(false);
                                        if (Tags.saveTag(inv, db)) {
                                            HashMap<Tags.Tag, TagsEdit> tagEdits = PlayerManager.tagsEdit.get(player.getUniqueId());
                                            tagEdits.remove(inv.tag());
                                            PlayerManager.tagsEdit.put(player.getUniqueId(), tagEdits);
                                            audience.sendMessage(Component.text("Tag has been saved!", NamedTextColor.GREEN));
                                            player.openInventory(new TagsView(player).getInventory());
                                        } else {
                                            audience.sendMessage(Component.text("Something went wrong when saving! Cancelling..", NamedTextColor.RED));
                                            player.openInventory(inv.getInventory());
                                        }
                                    }
                                })))
                                .append(Component.text("     "))
                                .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                    inv.setChangeLock(false);
                                    audience.sendMessage(Component.text("Tag saving cancelled!", NamedTextColor.GRAY));
                                    player.openInventory(inv.getInventory());
                                })));
                        player.sendMessage(msg);
                    }
                }
            }
            case Ignore inv -> {
                if (currItem != null && !MaterialTags.STAINED_GLASS_PANES.isTagged(currItem.getType())) {
                    if(isPaper) {
                        inv.updatePage(event.getSlot() == 46 ? -1 : 1);
                    } else {
                        if(event.getSlot() == 53) {
                            plugin.chatLock.put(player.getUniqueId(), List.of(inv));
                            player.closeInventory();
                            player.sendMessage(Component.text("Type the player to add to your ignore list: (Type 'cancel' to cancel)", NamedTextColor.YELLOW));
                        } else {
                            player.openInventory(new IgnoreEdit(player, inv.getIgnore(currItem), db).getInventory());
                        }
                    }
                }
            }
            case IgnoreEdit inv -> {
                if (currItem != null && !MaterialTags.STAINED_GLASS_PANES.isTagged(currItem.getType())) {
                    switch (event.getSlot()) {
                        case 20 -> inv.setIgnorePrivate();
                        case 24 -> inv.setIgnoreTeleport();
                        case 27 -> player.openInventory(new Ignore(player).getInventory());
                        case 31 -> {
                            player.closeInventory();
                            inv.setDeleteLock(true);
                            Component msg = Component.text("Are you sure you want to fully unignore & remove " + inv.targetName() + " from /ignore?", NamedTextColor.GRAY)
                                    .append(Component.text("\nI AM SURE", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (inv.deleteLock()) {
                                            inv.setDeleteLock(false);
                                            inv.deleteIgnore();
                                            player.openInventory(new Ignore(player).getInventory());
                                        }
                                    })))
                                    .append(Component.text("     "))
                                    .append(Component.text("CANCEL", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                        if (inv.deleteLock()) {
                                            inv.setDeleteLock(false);
                                            audience.sendMessage(Component.text("Successfully cancelled!", NamedTextColor.GRAY));
                                            player.openInventory(inv.getInventory());
                                        }
                                    })));
                            player.sendMessage(msg);
                        }
                    }
                }
            }
            default -> {}
        }
    }
}
