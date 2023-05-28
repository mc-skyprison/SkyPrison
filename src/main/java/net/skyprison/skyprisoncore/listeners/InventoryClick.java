package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.*;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.esophose.playerparticles.api.PlayerParticlesAPI;
import dev.esophose.playerparticles.particles.ParticleEffect;
import dev.esophose.playerparticles.styles.ParticleStyle;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.*;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.secrets.SecretsGUI;
import net.skyprison.skyprisoncore.inventories.ClaimFlags;
import net.skyprison.skyprisoncore.inventories.ClaimFlagsMobs;
import net.skyprison.skyprisoncore.inventories.ClaimMembers;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.claims.AvailableFlags;
import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InventoryClick implements Listener {
    private final SkyPrisonCore plugin;
    private final EconomyCheck econCheck;
    private final DropChest chestDrop;
    private final Bounty bounty;
    private final SecretsGUI secretsGUI;
    private final Daily daily;
    private final MoneyHistory moneyHistory;
    private final EndUpgrade endUpgrade;
    private final BuyBack buyBack;
    private final SkyPlot skyPlot;
    private final DatabaseHook db;
    private final Tags tag;
    private final PlayerParticlesAPI particles;
    private final CustomRecipes customRecipes;

    public InventoryClick(SkyPrisonCore plugin, EconomyCheck econCheck, DropChest dropChest, Bounty bounty,
                          SecretsGUI secretsGUI, Daily daily, MoneyHistory moneyHistory, EndUpgrade endUpgrade,
                          BuyBack buyBack, SkyPlot skyPlot, DatabaseHook db, Tags tag, PlayerParticlesAPI particles, CustomRecipes customRecipes) {
        this.plugin = plugin;
        this.econCheck = econCheck;
        this.chestDrop = dropChest;
        this.bounty = bounty;
        this.secretsGUI = secretsGUI;
        this.daily = daily;
        this.moneyHistory = moneyHistory;
        this.endUpgrade = endUpgrade;
        this.buyBack = buyBack;
        this.skyPlot = skyPlot;
        this.db = db;
        this.tag = tag;
        this.particles = particles;
        this.customRecipes = customRecipes;
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
    public void invClick(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player player) {

            if (event.getClickedInventory() instanceof PlayerInventory) {
                InvStickFix(player);
            }
            if(event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof CustomInventory customInv) {

                switch (customInv.defaultClickBehavior()) {
                    case DISABLE_ALL, ENABLE_SPECIFIC -> event.setCancelled(true);
                }

                if (customInv instanceof ClaimFlags inv) {
                    Component prefix = new Claim(plugin, db).prefix;
                    if(event.getCurrentItem() != null) {
                        Material clickedMat = event.getCurrentItem().getType();
                        switch (event.getSlot()) {
                            case 47 -> {
                                if (clickedMat.equals(Material.PAPER)) {
                                    player.openInventory(new ClaimFlags(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage() - 1).getInventory());
                                }
                            }
                            case 48 -> {
                                if (clickedMat.equals(Material.WRITABLE_BOOK)) {
                                    player.openInventory(new ClaimFlags(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getNextCategory(inv.getCategory()), 1).getInventory());
                                }
                            }
                            case 50 -> {
                                if (clickedMat.equals(Material.ZOMBIE_SPAWN_EGG)) {
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), true, "", 1).getInventory());
                                }
                            }
                            case 51 -> {
                                if (clickedMat.equals(Material.PAPER)) {
                                    player.openInventory(new ClaimFlags(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage() + 1).getInventory());
                                }
                            }
                            default -> {
                                if(!clickedMat.isEmpty() && !clickedMat.name().endsWith("GLASS_PANE")) {
                                    NamespacedKey flagKey = new NamespacedKey(plugin, "flag");
                                    AvailableFlags flag = AvailableFlags.valueOf(event.getCurrentItem().getPersistentDataContainer().get(flagKey, PersistentDataType.STRING));
                                    RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                    RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(inv.getWorld()))));
                                    if(regionManager != null) {
                                        ProtectedRegion region = regionManager.getRegion(inv.getClaimId());
                                        if(region != null) {
                                            List<Flag<?>> flags = flag.getFlags();
                                            if(region.getFlag(flags.get(0)) != null) {
                                                if(flags.get(0) instanceof StateFlag stateFlag) {
                                                    if(Objects.equals(region.getFlag(stateFlag), StateFlag.State.ALLOW)) {
                                                        flags.forEach(wgFlag -> region.setFlag((StateFlag) wgFlag,
                                                                flag.getNotSet().equalsIgnoreCase("disabled") ? null : StateFlag.State.DENY));
                                                    } else if(Objects.equals(region.getFlag(stateFlag), StateFlag.State.DENY)) {
                                                        flags.forEach(wgFlag -> region.setFlag((StateFlag) wgFlag, null));
                                                    }
                                                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                                    player.openInventory(new ClaimFlags(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage()).getInventory());
                                                } else if(flags.get(0) instanceof StringFlag stringFlag) {
                                                    plugin.chatLock.put(player.getUniqueId(), Arrays.asList(flag, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage()));
                                                    player.closeInventory();
                                                    if(!stringFlag.equals(Flags.TIME_LOCK)) {
                                                        player.sendMessage(prefix.append(Component.text("Enter the new "
                                                                        + WordUtils.capitalize(stringFlag.getName().replace("-", " ")), TextColor.fromHexString("#20df80"))));
                                                    } else {
                                                        player.sendMessage(prefix.append(Component.text("Enter the time you want in 24:00 hour time (Enter null to unset)",
                                                                TextColor.fromHexString("#20df80"))));
                                                    }
                                                } else if(flags.get(0) instanceof RegistryFlag<?>) {
                                                    plugin.chatLock.put(player.getUniqueId(), Arrays.asList(flag, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage()));
                                                    player.closeInventory();
                                                    player.sendMessage(Component.text("Enter the weather you want (Available types are 'Clear', 'Rain', 'Thunder', Enter null to unset)",
                                                            TextColor.fromHexString("#20df80")));
                                                }
                                            } else {
                                                if(flags.get(0) instanceof StateFlag) {
                                                    flags.forEach(wgFlag -> region.setFlag((StateFlag) wgFlag, flag.getNotSet().isEmpty() ? StateFlag.State.DENY : StateFlag.State.ALLOW));
                                                    player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                                    player.openInventory(new ClaimFlags(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage()).getInventory());
                                                } else if(flags.get(0) instanceof StringFlag stringFlag) {
                                                    plugin.chatLock.put(player.getUniqueId(), Arrays.asList(flag, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage()));
                                                    player.closeInventory();
                                                    if(!stringFlag.equals(Flags.TIME_LOCK)) {
                                                        player.sendMessage(prefix.append(Component.text("Enter the new "
                                                                + WordUtils.capitalize(stringFlag.getName().replace("-", " ")), TextColor.fromHexString("#20df80"))));
                                                    } else {
                                                        player.sendMessage(prefix.append(Component.text("Enter the time you want in 24:00 hour time",
                                                                TextColor.fromHexString("#20df80"))));
                                                    }
                                                } else if(flags.get(0) instanceof RegistryFlag<?>) {
                                                    plugin.chatLock.put(player.getUniqueId(), Arrays.asList(flag, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getCategory(), inv.getPage()));
                                                    player.closeInventory();
                                                    player.sendMessage(Component.text("Enter the weather you want (Available types are 'Clear', 'Rain', 'Thunder'",
                                                            TextColor.fromHexString("#20df80")));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (customInv instanceof ClaimFlagsMobs inv) {
                    if(event.getCurrentItem() != null) {
                        Material clickedMat = event.getCurrentItem().getType();
                        switch (event.getSlot()) {
                            case 45 -> {
                                if (clickedMat.equals(Material.PLAYER_HEAD)) {
                                    player.openInventory(new ClaimFlags(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), "", 1).getInventory());
                                }
                            }
                            case 47 -> {
                                if (clickedMat.equals(Material.PAPER)) {
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getIsAllowed(), inv.getCategory(), inv.getPage() - 1).getInventory());
                                }
                            }
                            case 48 -> {
                                if (clickedMat.equals(Material.SPAWNER)) {
                                    RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                    RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(inv.getWorld()))));
                                    if (regionManager != null) {
                                        ProtectedRegion region = regionManager.getRegion(inv.getClaimId());
                                        if (region != null) {
                                            if(region.getFlag(Flags.MOB_SPAWNING) != null) {
                                                region.setFlag(Flags.MOB_SPAWNING, null);
                                            } else {
                                                region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
                                            }
                                        }
                                    }
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getIsAllowed(), inv.getCategory(), inv.getPage()).getInventory());
                                }
                            }
                            case 49 -> {
                                if(clickedMat.equals(Material.LIME_CONCRETE)) {
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), false, inv.getCategory(), inv.getPage()).getInventory());
                                } else if(clickedMat.equals(Material.RED_CONCRETE)) {
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), true, inv.getCategory(), inv.getPage()).getInventory());

                                }
                            }
/*                            case 50 -> {
                                if (clickedMat.equals(Material.WRITABLE_BOOK)) {
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getIsAllowed(), inv.getNextCategory(inv.getCategory()), 1).getInventory());
                                }
                            }*/
                            case 51 -> {
                                if (clickedMat.equals(Material.PAPER)) {
                                    player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getIsAllowed(), inv.getCategory(), inv.getPage() + 1).getInventory());
                                }
                            }
                            default -> {
                                if(!clickedMat.isEmpty() && !clickedMat.name().endsWith("GLASS_PANE")) {
                                    NamespacedKey mobKey = new NamespacedKey(plugin, "mob");
                                    String mobName = event.getCurrentItem().getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
                                    if(mobName != null) {
                                        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(inv.getWorld()))));
                                        if(regionManager != null) {
                                            ProtectedRegion region = regionManager.getRegion(inv.getClaimId());
                                            if(region != null) {
                                                Set<EntityType> deniedMobs = region.getFlag(Flags.DENY_SPAWN);
                                                if(deniedMobs == null || deniedMobs.isEmpty()) deniedMobs = new HashSet<>();
                                                EntityType mob = BukkitAdapter.adapt(org.bukkit.entity.EntityType.valueOf(mobName));
                                                if (inv.getIsAllowed()) {
                                                    deniedMobs.add(mob);
                                                } else {
                                                    deniedMobs.remove(mob);
                                                }
                                                region.setFlag(Flags.DENY_SPAWN, deniedMobs);
                                                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                                                player.openInventory(new ClaimFlagsMobs(plugin, inv.getClaimId(), inv.getWorld(), inv.getCanEdit(), inv.getIsAllowed(), inv.getCategory(), inv.getPage()).getInventory());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (customInv instanceof ClaimMembers inv) {
                    if(event.getCurrentItem() != null) {
                        Material clickedMat = event.getCurrentItem().getType();
                        switch (event.getSlot()) {
                            case 47 -> {
                                if (clickedMat.equals(Material.PAPER)) {
                                    player.openInventory(new ClaimMembers(plugin, inv.getClaimName(), inv.getMembers(), inv.getCategory(), inv.getPage() - 1).getInventory());
                                }
                            }
                            case 49 -> {
                                if (clickedMat.equals(Material.WRITABLE_BOOK)) {
                                    player.openInventory(new ClaimMembers(plugin, inv.getClaimName(), inv.getMembers(), inv.getNextCategory(inv.getCategory()), 1).getInventory());
                                }
                            }
                            case 51 -> {
                                if (clickedMat.equals(Material.PAPER)) {
                                    player.openInventory(new ClaimMembers(plugin, inv.getClaimName(), inv.getMembers(), inv.getCategory(), inv.getPage() + 1).getInventory());
                                }
                            }
                        }
                    }
                }
            } else {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                user.getCMIPlayTime().getPlayDayOfToday().getTotalTime();
                Inventory clickInv = event.getClickedInventory();
                if (clickInv != null && !clickInv.isEmpty()) {
                    ItemStack fItem = clickInv.getItem(0);

                    NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                    NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                    if (fItem == null) {
                        fItem = getItemStack(clickInv, null, key, key1);
                    } else {
                        ItemMeta fMeta = Objects.requireNonNull(fItem).getItemMeta();
                        PersistentDataContainer fData = fMeta.getPersistentDataContainer();
                        if (!fData.has(key, PersistentDataType.INTEGER) || !fData.has(key1, PersistentDataType.STRING)) {
                            fItem = getItemStack(clickInv, fItem, key, key1);
                        }
                    }
                    if (fItem != null) {
                        ItemMeta fMeta = fItem.getItemMeta();
                        PersistentDataContainer fData = fMeta.getPersistentDataContainer();
                        if (fData.has(key, PersistentDataType.INTEGER) && fData.has(key1, PersistentDataType.STRING)) {
                            int clickCheck = fData.get(key, PersistentDataType.INTEGER);
                            String guiType = fData.get(key1, PersistentDataType.STRING);
                            int page = 1;
                            NamespacedKey pageKey = new NamespacedKey(plugin, "page");
                            if (fData.has(pageKey, PersistentDataType.INTEGER)) {
                                page = fData.get(pageKey, PersistentDataType.INTEGER);
                            }

                            if (clickCheck == 1) {
                                event.setCancelled(true);
                                switch (Objects.requireNonNull(guiType)) {
                                    case "bartender-grass":
                                        File f = new File(plugin.getDataFolder() + File.separator + "bartender.yml");
                                        FileConfiguration yamlf = YamlConfiguration.loadConfiguration(f);
                                        ItemStack alc = event.getCurrentItem();
                                        ItemMeta alcMeta = Objects.requireNonNull(alc).getItemMeta();
                                        PersistentDataContainer alcData = alcMeta.getPersistentDataContainer();
                                        NamespacedKey alcKey = new NamespacedKey(plugin, "alc-type");
                                        if (alcData.has(alcKey, PersistentDataType.STRING)) {
                                            String alcType = alcData.get(alcKey, PersistentDataType.STRING);
                                            int price = yamlf.getInt("grass." + alcType + ".price");
                                            if (user.getBalance() >= price) {
                                                if (user.getInventory().getFreeSlots() != 0) {
                                                    if (!alc.getType().equals(Material.MILK_BUCKET)) {
                                                        int quality = yamlf.getInt("grass." + alcType + ".quality");
                                                        String type = yamlf.getString("grass." + alcType + ".type");
                                                        player.sendMessage(plugin.colourMessage("&f[{#green}Bartender&f] &eYou bought " + type + "!"));
                                                        plugin.asConsole("brew create " + type + " " + quality + " " + player.getName());
                                                    } else {
                                                        player.sendMessage(plugin.colourMessage("&f[{#green}Bartender&f] &eYou bought Milk!"));
                                                        plugin.asConsole("give " + player.getName() + " milk_bucket");
                                                    }
                                                    plugin.asConsole("money take " + player.getName() + " " + price);
                                                    // Bartender.openGUI(player, "bartender-grass");
                                                } else {
                                                    player.sendMessage(plugin.colourMessage("&cYou do not have enough space in your inventory!"));
                                                }
                                            } else {
                                                player.sendMessage(plugin.colourMessage("&cYou do not have enough money!"));
                                            }
                                        }
                                        break;
                                    case "buyback":
                                        NamespacedKey typeKey = new NamespacedKey(plugin, "sold-type");
                                        ItemStack buyItem = event.getCurrentItem();
                                        if (buyItem != null) {
                                            ItemMeta buyMeta = buyItem.getItemMeta();
                                            PersistentDataContainer buyData = buyMeta.getPersistentDataContainer();
                                            if (buyData.has(typeKey, PersistentDataType.STRING)) {
                                                NamespacedKey amKey = new NamespacedKey(plugin, "sold-amount");
                                                NamespacedKey priKey = new NamespacedKey(plugin, "sold-price");
                                                String itemType = buyData.get(typeKey, PersistentDataType.STRING);
                                                int itemAmount = buyData.get(amKey, PersistentDataType.INTEGER);
                                                Double itemPrice = buyData.get(priKey, PersistentDataType.DOUBLE);
                                                ItemStack iSold = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(itemType))), itemAmount);
                                                if (user.getInventory().canFit(iSold)) {
                                                    if (user.getBalance() >= itemPrice) {
                                                        NamespacedKey posKey = new NamespacedKey(plugin, "sold-id");
                                                        int buyId = buyData.get(posKey, PersistentDataType.INTEGER);

                                                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM recent_sells WHERE recent_id = ?")) {
                                                            ps.setInt(1, buyId);
                                                            ps.executeUpdate();
                                                            plugin.asConsole("give " + player.getName() + " " + itemType + " " + itemAmount);
                                                            plugin.asConsole("money take " + player.getName() + " " + itemPrice);
                                                            buyBack.openGUI(player);
                                                        } catch (SQLException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
                                                        player.sendMessage(plugin.colourMessage("&cYou do not have enough money!"));
                                                    }
                                                } else {
                                                    player.sendMessage(plugin.colourMessage("&cYou do not have enough space in your inventory!"));
                                                }
                                            }
                                        }
                                        break;
                                    case "netheriteupgrade":
                                        if (event.getSlot() == 11) {
                                            ItemStack pMain = player.getInventory().getItemInMainHand();
                                            if (pMain.getType() != Material.AIR) {
                                                if (clickInv.getItem(event.getSlot()).getType() == Material.GREEN_CONCRETE) {
                                                    plugin.asConsole("cmi money take " + user.getName() + " 500000");
                                                    pMain.setRepairCost(0);
                                                    player.sendMessage(plugin.colourMessage("&f[&aBlacksmith&f] &7Your &3" + clickInv.getItem(13).getType() + " &7has had its repair cost reset for &a$500,000&7!"));
                                                    player.closeInventory();
                                                } else {
                                                    player.sendMessage(plugin.colourMessage("&cYou can't afford this!"));
                                                }
                                            } else {
                                                player.closeInventory();
                                                player.sendMessage(plugin.colourMessage("&f[&aBlacksmith&f] &cYou are not holding anything in your hand!"));
                                            }
                                        } else if (event.getSlot() == 15) {
                                            player.closeInventory();
                                        }
                                        break;
                                    case "endupgrade":
                                        ItemStack clickedItem = clickInv.getItem(event.getSlot());
                                        if (event.getSlot() == 20) {
                                            ItemStack repItem = clickInv.getItem(24);
                                            PersistentDataContainer repData = Objects.requireNonNull(repItem).getPersistentDataContainer();
                                            NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
                                            int repCheck = repData.get(repKey, PersistentDataType.INTEGER);

                                            PersistentDataContainer clickData = Objects.requireNonNull(clickedItem).getPersistentDataContainer();
                                            NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
                                            int enchCheck = clickData.get(enchKey, PersistentDataType.INTEGER);
                                            if (enchCheck != 1) {
                                                endUpgrade.openGUI(player, true, repCheck == 1);
                                            } else {
                                                endUpgrade.openGUI(player, false, repCheck == 1);
                                            }
                                        } else if (event.getSlot() == 24) {
                                            ItemStack enchItem = clickInv.getItem(20);
                                            PersistentDataContainer enchData = Objects.requireNonNull(enchItem).getPersistentDataContainer();
                                            NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
                                            int enchCheck = enchData.get(enchKey, PersistentDataType.INTEGER);

                                            PersistentDataContainer clickData = Objects.requireNonNull(clickedItem).getPersistentDataContainer();
                                            NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
                                            int repCheck = clickData.get(repKey, PersistentDataType.INTEGER);
                                            if (repCheck != 1) {
                                                endUpgrade.openGUI(player, enchCheck == 1, true);
                                            } else {
                                                endUpgrade.openGUI(player, enchCheck == 1, false);
                                            }
                                        } else if (event.getSlot() == 31) {
                                            ItemStack pMain = player.getInventory().getItemInMainHand();
                                            if (pMain.getType() != Material.AIR) {
                                                if (clickInv.getItem(event.getSlot()).getType() == Material.GREEN_CONCRETE) {
                                                    ItemStack enchState = clickInv.getItem(20);
                                                    assert enchState != null;
                                                    PersistentDataContainer enchData = enchState.getPersistentDataContainer();
                                                    NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
                                                    int enchCheck = enchData.get(enchKey, PersistentDataType.INTEGER);

                                                    ItemStack repItem = clickInv.getItem(24);
                                                    PersistentDataContainer repData = Objects.requireNonNull(repItem).getPersistentDataContainer();
                                                    NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
                                                    int repCheck = repData.get(repKey, PersistentDataType.INTEGER);

                                                    endUpgrade.confirmGUI(player, enchCheck == 1, repCheck == 1);
                                                } else {
                                                    player.sendMessage(plugin.colourMessage("&cYou can't afford this!"));
                                                }
                                            } else {
                                                player.closeInventory();
                                                player.sendMessage(plugin.colourMessage("&f[&aBlacksmith&f] &cYou are not holding anything in your hand!"));
                                            }
                                        }
                                        break;
                                    case "confirm-endupgrade":
                                        if (event.getSlot() == 11) {
                                            ItemStack pMain = player.getInventory().getItemInMainHand();
                                            if (pMain.getType() != Material.AIR) {
                                                ItemStack confirmItem = clickInv.getItem(11);
                                                PersistentDataContainer confirmData = Objects.requireNonNull(confirmItem).getPersistentDataContainer();
                                                NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
                                                int enchCheck = confirmData.get(enchKey, PersistentDataType.INTEGER);

                                                NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
                                                int repCheck = confirmData.get(repKey, PersistentDataType.INTEGER);

                                                if (pMain.getRepairCost() >= 1000) {
                                                    repCheck = 0;
                                                }

                                                int cost = endUpgrade.upgradeCost(player, enchCheck == 1, repCheck == 1);

                                                switch (pMain.getType().toString()) {
                                                    case "DIAMOND_AXE":
                                                        pMain.setType(Material.NETHERITE_AXE);
                                                        break;
                                                    case "DIAMOND_PICKAXE":
                                                        pMain.setType(Material.NETHERITE_PICKAXE);
                                                        break;
                                                    case "DIAMOND_SHOVEL":
                                                        pMain.setType(Material.NETHERITE_SHOVEL);
                                                        break;
                                                    case "DIAMOND_HOE":
                                                        pMain.setType(Material.NETHERITE_HOE);
                                                        break;
                                                    case "DIAMOND_HELMET":
                                                        pMain.setType(Material.NETHERITE_HELMET);
                                                        break;
                                                    case "DIAMOND_CHESTPLATE":
                                                        pMain.setType(Material.NETHERITE_CHESTPLATE);
                                                        break;
                                                    case "DIAMOND_LEGGINGS":
                                                        pMain.setType(Material.NETHERITE_LEGGINGS);
                                                        break;
                                                    case "DIAMOND_BOOTS":
                                                        pMain.setType(Material.NETHERITE_BOOTS);
                                                        break;
                                                }
                                                if (enchCheck != 1) {
                                                    if (pMain.hasEnchants()) {
                                                        for (Enchantment ench : pMain.getEnchants().keySet()) {
                                                            pMain.removeEnchant(ench);
                                                        }
                                                    }
                                                }
                                                if (repCheck == 1) {
                                                    pMain.setRepairCost(0);
                                                }

                                                if (!player.hasPermission("skyprisoncore.command.endupgrade.first-time")) {
                                                    plugin.asConsole("money take " + player.getName() + " " + cost);
                                                    player.sendMessage(plugin.colourMessage("&f[&aBlacksmith&f] &7Your &3" + clickInv.getItem(4).getType() + " &7has been upgraded for &a$" + plugin.formatNumber(cost) + "&7!"));
                                                } else {
                                                    plugin.asConsole("lp user " + player.getName() + " permission unset skyprisoncore.command.endupgrade.first-time");
                                                    player.sendMessage(plugin.colourMessage("&f[&aBlacksmith&f] &7Your &3" + clickInv.getItem(4).getType() + " &7has been upgraded!"));
                                                }
                                                player.closeInventory();
                                            }
                                        } else if (event.getSlot() == 15) {
                                            player.closeInventory();
                                        }
                                        break;
                                    case "bounties":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();
                                            if (clickedMat.equals(Material.PAPER)) {
                                                if (event.getSlot() == 46) {
                                                    bounty.openGUI(player, page - 1);
                                                } else if (event.getSlot() == 52) {
                                                    bounty.openGUI(player, page + 1);
                                                }
                                            }
                                        }
                                        break;
                                    case "recipes-main":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();
                                            if (clickedMat.equals(Material.BARRIER)) {
                                                customRecipes.openDisabledGUI(player);
                                            } else if (clickedMat.equals(Material.KNOWLEDGE_BOOK)) {
                                                customRecipes.openCustomGUI(player);
                                            }
                                        }
                                        break;
                                    case "recipes-custom":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            ItemStack cItem = event.getClickedInventory().getItem(event.getSlot());
                                            ItemMeta cMeta = cItem.getItemMeta();
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();
                                            if (clickedMat.equals(Material.PAPER)) {
                                                customRecipes.openMainGUI(player);
                                            } else {
                                                NamespacedKey recipeKey = new NamespacedKey(plugin, "custom-recipe");
                                                if (cMeta.getPersistentDataContainer().has(recipeKey, PersistentDataType.STRING)) {
                                                    String recipe = cMeta.getPersistentDataContainer().get(recipeKey, PersistentDataType.STRING);
                                                    customRecipes.openSpecificGUI(player, recipe);
                                                }
                                            }
                                        }
                                        break;
                                    case "recipes-disabled":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();
                                            if (clickedMat.equals(Material.PAPER)) {
                                                customRecipes.openMainGUI(player);
                                            }
                                        }
                                        break;
                                    case "recipes-specific":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();
                                            if (clickedMat.equals(Material.PAPER)) {
                                                customRecipes.openCustomGUI(player);
                                            }
                                        }
                                        break;
                                    case "token-history":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();

                                            NamespacedKey tKey = new NamespacedKey(plugin, "sort");
                                            NamespacedKey tKey1 = new NamespacedKey(plugin, "toggle");
                                            NamespacedKey tKey3 = new NamespacedKey(plugin, "lookup-user");
                                            Boolean transSort = Boolean.parseBoolean(fData.get(tKey, PersistentDataType.STRING));
                                            Integer transToggle = fData.get(tKey1, PersistentDataType.INTEGER);
                                            String userId = fData.get(tKey3, PersistentDataType.STRING);
                                            if (clickedMat.equals(Material.PAPER)) {
                                                if (event.getSlot() == 45) {
                                                    plugin.tokens.openHistoryGUI(player, transSort, transToggle, page - 1, userId);
                                                } else if (event.getSlot() == 53) {
                                                    plugin.tokens.openHistoryGUI(player, transSort, transToggle, page + 1, userId);
                                                }
                                            } else if (clickedMat.equals(Material.CLOCK)) {
                                                plugin.tokens.openHistoryGUI(player, !transSort, transToggle, page, userId);
                                            } else if (clickedMat.equals(Material.COMPASS)) {
                                                if (transToggle == 6) {
                                                    plugin.tokens.openHistoryGUI(player, transSort, 1, 1, userId);
                                                } else {
                                                    plugin.tokens.openHistoryGUI(player, transSort, transToggle + 1, 1, userId);
                                                }

                                            }
                                        }
                                        break;
                                    case "transaction-history":
                                        if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();

                                            NamespacedKey tKey = new NamespacedKey(plugin, "sort");
                                            NamespacedKey tKey1 = new NamespacedKey(plugin, "toggle");
                                            NamespacedKey tKey3 = new NamespacedKey(plugin, "lookup-user");
                                            Boolean transSort = Boolean.parseBoolean(fData.get(tKey, PersistentDataType.STRING));
                                            String transToggle = fData.get(tKey1, PersistentDataType.STRING);
                                            String userId = fData.get(tKey3, PersistentDataType.STRING);
                                            if (clickedMat.equals(Material.PAPER)) {
                                                if (event.getSlot() == 45) {
                                                    moneyHistory.openGUI(player, transSort, transToggle, page - 1, userId);
                                                } else if (event.getSlot() == 53) {
                                                    moneyHistory.openGUI(player, transSort, transToggle, page + 1, userId);
                                                }
                                            } else if (clickedMat.equals(Material.CLOCK)) {
                                                moneyHistory.openGUI(player, !transSort, transToggle, page, userId);
                                            } else if (clickedMat.equals(Material.COMPASS)) {
                                                if (transToggle.equalsIgnoreCase("null")) {
                                                    moneyHistory.openGUI(player, transSort, "true", 1, userId);
                                                } else if (transToggle.equalsIgnoreCase("true")) {
                                                    moneyHistory.openGUI(player, transSort, "false", 1, userId);
                                                } else if (transToggle.equalsIgnoreCase("false")) {
                                                    moneyHistory.openGUI(player, transSort, "null", 1, userId);

                                                }

                                            }
                                        }
                                        break;
                                    case "skyplot-gui":
                                        NamespacedKey skyKey = new NamespacedKey(plugin, "skyplot-type");
                                        String pageType = fData.get(skyKey, PersistentDataType.STRING);
                                        switch (pageType.toLowerCase()) {
                                            case "main":
                                                switch (event.getSlot()) {
                                                    case 13:
                                                        break;
                                                    case 20:
                                                        skyPlot.skyPlotGUI(player, "expand", 1);
                                                        break;
                                                    case 24:
                                                        skyPlot.skyPlotGUI(player, "other", 1);
                                                        break;
                                                    case 31:
                                                        skyPlot.skyPlotGUI(player, "settings", 1);
                                                        break;
                                                }
                                                break;
                                            case "settings":
                                                if (event.getSlot() == 11) {
                                                    skyPlot.skyPlotGUI(player, "banned", 1);
                                                } else if (event.getSlot() == 15) {
                                                    skyPlot.setVisit(player);
                                                    skyPlot.skyPlotGUI(player, "settings", 1);
                                                } else if (event.getSlot() == 22) {
                                                    skyPlot.skyPlotGUI(player, "main", 1);
                                                }
                                                break;
                                            case "expand":
                                                clickedItem = event.getClickedInventory().getItem(event.getSlot());
                                                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                                RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
                                                ApplicableRegionSet regionList = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
                                                ProtectedRegion region = regionList.getRegions().iterator().next();
                                                if (clickedItem.getType().equals(Material.PLAYER_HEAD)) {
                                                    switch (event.getSlot()) { // Default Size = 14 x 24 x 14
                                                        case 10: // increase to 20 x 30 x 20
                                                            ProtectedRegion newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(3, -3, -3), region.getMaximumPoint().add(-3, 3, 3));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                        case 11: // increase by 30 x 40 x 30
                                                            newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1, -1), region.getMaximumPoint().add(-1, 1, 1));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                        case 12: // increase by 6
                                                            newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1, -1), region.getMaximumPoint().add(-1, 1, 1));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                        case 13: // increase by 8
                                                            newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1, -1), region.getMaximumPoint().add(-1, 1, 1));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                        case 14: // increase by 10
                                                            newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1, -1), region.getMaximumPoint().add(-1, 1, 1));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                        case 15: // increase by 12
                                                            newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1, -1), region.getMaximumPoint().add(-1, 1, 1));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                        case 16: // increase by 14
                                                            newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1, -1), region.getMaximumPoint().add(-1, 1, 1));
                                                            newRegion.copyFrom(region);
                                                            regions.removeRegion(region.getId());
                                                            regions.addRegion(newRegion);
                                                            break;
                                                    }
                                                }
                                                break;
                                            case "banned":
                                                break;
                                            case "other":
                                                if (event.getClickedInventory().getItem(event.getSlot()) != null) {
                                                    ItemStack clickItem = event.getClickedInventory().getItem(event.getSlot());
                                                    PersistentDataContainer clickData = clickItem.getPersistentDataContainer();

                                                    if (clickItem.getType().equals(Material.PLAYER_HEAD)) {
                                                        NamespacedKey isleKey = new NamespacedKey(plugin, "skyplot-owner");
                                                        String isleOwner = clickData.get(isleKey, PersistentDataType.STRING);
                                                        Location loc = skyPlot.getIsleLoc(isleOwner);
                                                        player.teleportAsync(loc);

                                                    } else if (event.getSlot() == 48 && clickItem.getType().equals(Material.PAPER)) {
                                                        skyPlot.skyPlotGUI(player, "main", page - 1);
                                                    } else if (event.getSlot() == 49) {
                                                        skyPlot.skyPlotGUI(player, "main", 1);
                                                    } else if (event.getSlot() == 50 && clickItem.getType().equals(Material.PAPER)) {
                                                        skyPlot.skyPlotGUI(player, "main", page + 1);
                                                    }
                                                }
                                                break;
                                        }

                                        break;
                                    case "daily-reward":
                                        if (event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.MINECART)) {
                                            player.sendMessage(plugin.colourMessage("&cYou've already collected the daily reward!"));
                                        } else if (event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.CHEST_MINECART)) {
                                            int currStreak = 0;
                                            int highestStreak = 0;
                                            int totalCollected = 0;
                                            String lastColl = "";

                                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_streak, highest_streak, total_collected, last_collected FROM dailies WHERE user_id = ?")) {
                                                ps.setString(1, player.getUniqueId().toString());
                                                ResultSet rs = ps.executeQuery();
                                                while (rs.next()) {
                                                    currStreak = rs.getInt(1);
                                                    highestStreak = rs.getInt(2);
                                                    totalCollected = rs.getInt(3);
                                                    lastColl = rs.getString(4);
                                                }
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }

                                            Random rand = new Random();
                                            int tReward = rand.nextInt(25) + 25;

                                            if ((currStreak + 1) % 7 == 0) {
                                                tReward = 250;
                                            }

                                            Random rand2 = new Random();
                                            int randInt = rand2.nextInt(1000) + 1;
                                            if (randInt == 666) {
                                                tReward = randInt;
                                            }

                                            plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), tReward, "Daily Reward", currStreak + " Days");

                                            int nCurrStreak = currStreak + 1;
                                            int nTotalCollected = totalCollected + 1;

                                            Date date = new Date();
                                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                            String currDate = formatter.format(date);

                                            String sql;
                                            List<Object> params;

                                            if (!lastColl.isEmpty()) {
                                                if (currStreak >= highestStreak) {
                                                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE dailies SET current_streak = ?, highest_streak = ?, last_collected = ?, total_collected = ? WHERE user_id = ?")) {
                                                        ps.setInt(1, nCurrStreak);
                                                        ps.setInt(2, nCurrStreak);
                                                        ps.setString(3, currDate);
                                                        ps.setInt(4, nTotalCollected);
                                                        ps.setString(5, user.getUniqueId().toString());
                                                        ps.executeUpdate();
                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE dailies SET current_streak = ?, last_collected = ?, total_collected = ? WHERE user_id = ?")) {
                                                        ps.setInt(1, nCurrStreak);
                                                        ps.setString(2, currDate);
                                                        ps.setInt(3, nTotalCollected);
                                                        ps.setString(4, user.getUniqueId().toString());
                                                        ps.executeUpdate();
                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            } else {
                                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO dailies (user_id, current_streak, total_collected, highest_streak, last_collected) VALUES (?, ?, ?, ?, ?)")) {
                                                    ps.setString(1, user.getUniqueId().toString());
                                                    ps.setInt(2, nCurrStreak);
                                                    ps.setInt(3, nTotalCollected);
                                                    ps.setInt(3, nCurrStreak);
                                                    ps.setString(3, currDate);
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            daily.openGUI(player);
                                        }
                                        break;
                                    case "plotteleport":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            ItemStack itemClick = clickInv.getItem(event.getSlot());
                                            PersistentDataContainer plotData = Objects.requireNonNull(itemClick).getPersistentDataContainer();
                                            NamespacedKey plotKey = new NamespacedKey(plugin, "x");
                                            if (plotData.has(plotKey, PersistentDataType.DOUBLE)) {
                                                NamespacedKey plotKey1 = new NamespacedKey(plugin, "y");
                                                NamespacedKey plotKey2 = new NamespacedKey(plugin, "z");
                                                NamespacedKey plotKey3 = new NamespacedKey(plugin, "world");
                                                double x = plotData.get(plotKey, PersistentDataType.DOUBLE);
                                                double y = plotData.get(plotKey1, PersistentDataType.DOUBLE);
                                                double z = plotData.get(plotKey2, PersistentDataType.DOUBLE);
                                                World world = Bukkit.getWorld(plotData.get(plotKey3, PersistentDataType.STRING));
                                                Location loc = new Location(world, x, y, z);
                                                if (player.getWorld().getName().equalsIgnoreCase("world_skycity") || player.hasPermission("cmi.command.tpa.warmupbypass")) {
                                                    player.teleportAsync(loc);
                                                    player.sendMessage(plugin.colourMessage("&aTeleported to plot!"));
                                                } else {
                                                    player.closeInventory();
                                                    player.sendMessage(plugin.colourMessage("&aTeleporting to your plot in 5 seconds, Don't move!"));
                                                    BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                                        plugin.teleportMove.remove(player.getUniqueId());
                                                        player.teleport(loc);
                                                        player.sendMessage(plugin.colourMessage("&aTeleported to plot!"));
                                                    }, 100L);
                                                    plugin.teleportMove.put(player.getUniqueId(), task.getTaskId());

                                                }
                                            }
                                        }
                                        break;
                                    case "tags":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            ItemStack clickItem = event.getClickedInventory().getItem(event.getSlot());
                                            PersistentDataContainer clickData = clickItem.getPersistentDataContainer();
                                            if (clickItem.getType().equals(Material.NAME_TAG)) {
                                                NamespacedKey tagKey = new NamespacedKey(plugin, "tag-id");
                                                int tag_id = clickData.get(tagKey, PersistentDataType.INTEGER);
                                                String tagsDisplay = "";
                                                String tagsEffect = "";

                                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT tags_display, tags_effect FROM tags WHERE tags_id = ?")) {
                                                    ps.setInt(1, tag_id);
                                                    ResultSet rs = ps.executeQuery();
                                                    while (rs.next()) {
                                                        tagsDisplay = rs.getString(1);
                                                        tagsEffect = rs.getString(2);
                                                    }
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }

                                                particles.resetActivePlayerParticles(player);
                                                if (tagsEffect != null && !tagsEffect.isEmpty()) {
                                                    particles.addActivePlayerParticle(player, ParticleEffect.CLOUD, ParticleStyle.fromInternalName(tagsEffect));
                                                }

                                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET active_tag = ? WHERE user_id = ?")) {
                                                    ps.setInt(1, tag_id);
                                                    ps.setString(2, user.getUniqueId().toString());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }

                                                plugin.userTags.put(player.getUniqueId(), tagsDisplay);
                                                player.sendMessage(plugin.colourMessage("&aSelected Tag: &r" + tagsDisplay));
                                                tag.openGUI(player, page);
                                            } else if (event.getSlot() == 46 && clickItem.getType().equals(Material.PAPER)) {
                                                tag.openGUI(player, page - 1);
                                            } else if (event.getSlot() == 49 && clickItem.getType().equals(Material.BARRIER)) {
                                                plugin.userTags.remove(player.getUniqueId());
                                                particles.resetActivePlayerParticles(player);
                                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET active_tag = ? WHERE user_id = ?")) {
                                                    ps.setInt(1, 0);
                                                    ps.setString(2, user.getUniqueId().toString());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                                tag.openGUI(player, page);
                                            } else if (event.getSlot() == 52 && clickItem.getType().equals(Material.PAPER)) {
                                                tag.openGUI(player, page + 1);
                                            }
                                        }
                                        break;
                                    case "tags-edit-all":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            ItemStack clickItem = event.getClickedInventory().getItem(event.getSlot());
                                            PersistentDataContainer clickData = clickItem.getPersistentDataContainer();


                                            if (clickItem.getType().equals(Material.NAME_TAG)) {
                                                NamespacedKey tagKey = new NamespacedKey(plugin, "tag-id");
                                                int tag_id = clickData.get(tagKey, PersistentDataType.INTEGER);
                                                tag.openSpecificGUI(player, tag_id);
                                            } else if (event.getSlot() == 46 && clickItem.getType().equals(Material.PAPER)) {
                                                tag.openEditGUI(player, page - 1);
                                            } else if (event.getSlot() == 52 && clickItem.getType().equals(Material.PAPER)) {
                                                tag.openEditGUI(player, page + 1);
                                            }
                                        }
                                        break;
                                    case "tags-edit-specific":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            NamespacedKey tagKey = new NamespacedKey(plugin, "tag-id");
                                            int tag_id = fData.get(tagKey, PersistentDataType.INTEGER);

                                            if (event.getSlot() == 22) {
                                                tag.openEditGUI(player, page);
                                            } else if (event.getSlot() == 11) {
                                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("tags-display", tag_id));
                                                player.closeInventory();
                                                player.sendMessage(plugin.colourMessage("&aType the new tag display:"));
                                            } else if (event.getSlot() == 13) {
                                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("tags-lore", tag_id));
                                                player.closeInventory();
                                                player.sendMessage(plugin.colourMessage("&aType the new tag lore:"));
                                            } else if (event.getSlot() == 15) {
                                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("tags-effect", tag_id));
                                                player.closeInventory();
                                                StringBuilder cosmetics = new StringBuilder();
                                                int i = 0;
                                                player.sendMessage(plugin.colourMessage("&aType the new tag effect (Write null to remove effect): \n&eAvailable Effects: " + cosmetics));
                                            } else if (event.getSlot() == 17) {
                                                tag.openEditGUI(player, page);
                                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tags WHERE tags_id = ?")) {
                                                    ps.setInt(1, tag_id);
                                                    ps.executeUpdate();
                                                    player.sendMessage(plugin.colourMessage("&aSuccessfully deleted tag!"));
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        break;
                                    case "tokencheck":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            if (event.getCurrentItem().getType() == Material.PAPER) {
                                                if (event.getSlot() == 46) {
                                                    plugin.tokens.openCheckGUI((Player) event.getWhoClicked(), page - 1, "default");
                                                } else if (event.getSlot() == 52) {
                                                    plugin.tokens.openCheckGUI((Player) event.getWhoClicked(), page + 1, "default");
                                                }
                                            } else if (event.getCurrentItem().getType() == Material.BOOK) {
                                                if (event.getSlot() == 47) {
                                                    plugin.tokens.openCheckGUI((Player) event.getWhoClicked(), page, "amounttop");
                                                } else if (event.getSlot() == 48) {
                                                    plugin.tokens.openCheckGUI((Player) event.getWhoClicked(), page, "amountbottom");
                                                } else if (event.getSlot() == 49) {
                                                    event.getWhoClicked().closeInventory();
                                                    event.getWhoClicked().sendMessage(ChatColor.RED + "/token check (player)");
                                                } else if (event.getSlot() == 50) {
                                                    plugin.tokens.openCheckGUI((Player) event.getWhoClicked(), page, "usagebottom");
                                                } else if (event.getSlot() == 51) {
                                                    plugin.tokens.openCheckGUI((Player) event.getWhoClicked(), page, "usagetop");
                                                }
                                            }
                                        }
                                        break;
                                    case "econcheck":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            if (event.getCurrentItem().getType() == Material.PAPER) {
                                                if (event.getSlot() == 46) {
                                                    econCheck.openGUI((Player) event.getWhoClicked(), page - 1, "default");
                                                } else if (event.getSlot() == 52) {
                                                    econCheck.openGUI((Player) event.getWhoClicked(), page + 1, "default");
                                                }
                                            } else if (event.getCurrentItem().getType() == Material.BOOK) {
                                                if (event.getSlot() == 47) {
                                                    econCheck.openGUI((Player) event.getWhoClicked(), page, "amounttop");
                                                } else if (event.getSlot() == 48) {
                                                    econCheck.openGUI((Player) event.getWhoClicked(), page, "amountbottom");
                                                } else if (event.getSlot() == 49) {
                                                    event.getWhoClicked().closeInventory();
                                                    event.getWhoClicked().sendMessage(ChatColor.RED + "/econcheck player <player>");
                                                } else if (event.getSlot() == 50) {
                                                    econCheck.openGUI((Player) event.getWhoClicked(), page, "moneybottom");
                                                } else if (event.getSlot() == 51) {
                                                    econCheck.openGUI((Player) event.getWhoClicked(), page, "moneytop");
                                                }
                                            }
                                        }
                                    case "tags-new":
                                        if (clickInv.getItem(event.getSlot()) != null) {
                                            NamespacedKey key2 = new NamespacedKey(plugin, "tags-display");
                                            NamespacedKey key3 = new NamespacedKey(plugin, "tags-lore");
                                            NamespacedKey key4 = new NamespacedKey(plugin, "tags-effect");
                                            String display = fData.get(key2, PersistentDataType.STRING);
                                            String lore = fData.get(key3, PersistentDataType.STRING);
                                            String effect = fData.get(key4, PersistentDataType.STRING);

                                            if (event.getSlot() == 0) {
                                                tag.openEditGUI(player, page);
                                            } else if (event.getSlot() == 11) {
                                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("tags-new-display", display, lore, effect));
                                                player.closeInventory();
                                                player.sendMessage(plugin.colourMessage("&aType the tag display:"));
                                            } else if (event.getSlot() == 13) {
                                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("tags-new-lore", display, lore, effect));
                                                player.closeInventory();
                                                player.sendMessage(plugin.colourMessage("&aType the tag lore:"));
                                            } else if (event.getSlot() == 15) {
                                                plugin.chatLock.put(player.getUniqueId(), Arrays.asList("tags-new-effect", display, lore, effect));
                                                player.closeInventory();
                                                StringBuilder cosmetics = new StringBuilder();
                                                player.sendMessage(plugin.colourMessage("&aType the tag effect (Write null to remove effect): \n&eAvailable Effects: " + cosmetics));
                                            } else if (event.getSlot() == 22) {
                                                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO tags (tags_display, tags_lore, tags_effect) VALUES (?, ?, ?)")) {
                                                    ps.setString(1, display);
                                                    ps.setString(2, lore);
                                                    ps.setString(3, effect);
                                                    ps.executeUpdate();
                                                    player.sendMessage(plugin.colourMessage("&aSuccessfully created the tag!"));
                                                    player.closeInventory();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                        break;
                                }
                            } else if (clickCheck == 0) {
                                if (guiType.equalsIgnoreCase("tokenshop-edit")) {
                                    if (event.isShiftClick()) {
                                        if (event.getCurrentItem() != null && !event.getCurrentItem().getType().isAir()) {
                                            ItemStack clickedItem = event.getCurrentItem();
                                            ItemMeta cMeta = clickedItem.getItemMeta();
                                            PersistentDataContainer cData = fMeta.getPersistentDataContainer();
                                            NamespacedKey idKey = new NamespacedKey(plugin, "id");
                                            if (cData.has(idKey, PersistentDataType.STRING)) {
                                                event.setCancelled(true);
                                                String itemId = cData.get(idKey, PersistentDataType.STRING);

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (ChatColor.stripColor(event.getView().getTitle()).contains("Secrets")) {
                    if (event.getCurrentItem() != null) {
                        event.setCancelled(true);
                    }
                    String[] title = event.getView().getTitle().split(" - ");
                    HumanEntity human = event.getWhoClicked();
                    if (human instanceof Player) {
                        switch (title[1].toLowerCase()) {
                            case "grass":
                            case "desert":
                            case "nether":
                            case "snow":
                            case "skycity":
                            case "marina":
                            case "skycity-other":
                            case "prison-other":
                                if (event.getSlot() == 40) {
                                    secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                                }
                                break;
                            case "all":
                                switch (event.getSlot()) {
                                    case 31:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                                        break;
                                    case 11:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "grass");
                                        break;
                                    case 12:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "desert");
                                        break;
                                    case 13:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "nether");
                                        break;
                                    case 14:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "snow");
                                        break;
                                    case 15:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "prison-other");
                                        break;
                                    case 22:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "skycity");
                                        break;
                                }
                                break;
                            case "rewards":
                                if (event.getCurrentItem() == null) {
                                    break;
                                }
                                if (event.getSlot() == 49) {
                                    secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "main-menu");
                                } else if (event.getCurrentItem().getType().equals(Material.CHEST_MINECART)) {
                                    assert player != null;
                                    File rewardsDataFile = new File(plugin.getDataFolder() + File.separator
                                            + "rewardsdata.yml");
                                    FileConfiguration rData = YamlConfiguration.loadConfiguration(rewardsDataFile);

                                    ItemStack currItem = event.getCurrentItem();
                                    NamespacedKey key = new NamespacedKey(plugin, "reward");
                                    ItemMeta itemMeta = currItem.getItemMeta();
                                    PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                                    String foundValue;
                                    if (container.has(key, PersistentDataType.STRING)) {
                                        foundValue = container.get(key, PersistentDataType.STRING);
                                        if (Objects.requireNonNull(rData.getString(foundValue + ".reward-type")).equalsIgnoreCase("tokens")) {
                                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE rewards_data SET reward_collected = ? WHERE user_id = ? AND reward_name = ?")) {
                                                ps.setInt(1, 1);
                                                ps.setString(2, player.getUniqueId().toString());
                                                ps.setString(3, foundValue);
                                                ps.executeUpdate();
                                                int tokenAmount = rData.getInt(foundValue + ".reward");
                                                plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), tokenAmount, "Secret Region Found", foundValue);
                                                player.sendMessage(plugin.colourMessage("&f[&eSecrets&f] &aYou received " + tokenAmount + " tokens!"));
                                                secretsGUI.openGUI(player, "rewards");
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                break;
                            case "main":
                                switch (event.getSlot()) {
                                    case 13:
                                        break;
                                    case 20:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "secrets");
                                        break;
                                    case 24:
                                        secretsGUI.openGUI(Bukkit.getPlayer(human.getName()), "rewards");
                                        break;
                                }
                        }
                    }
                }
                String[] dropChest = ChatColor.stripColor(event.getView().getTitle()).split(" ");
                if (dropChest[0].equalsIgnoreCase("Drop") && dropChest[1].equalsIgnoreCase("Party")) {
                    if (event.getCurrentItem() != null) {
                        event.setCancelled(true);
                        if (event.getCurrentItem().getType() == Material.PAPER) {
                            if (event.getSlot() == 46) {
                                int page = Integer.parseInt(dropChest[4]) - 1;
                                chestDrop.openGUI((Player) event.getWhoClicked(), page);
                            } else if (event.getSlot() == 52) {
                                int page = Integer.parseInt(dropChest[4]) + 1;
                                chestDrop.openGUI((Player) event.getWhoClicked(), page);
                            }
                        }
                    }
                }

                if (ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("Referral List")) {
                    if (event.getCurrentItem() != null) {
                        event.setCancelled(true);
                    }
                }

                if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
                    if (event.getClickedInventory() instanceof PlayerInventory) {
                        plugin.InvGuardGearDelPlyr(player);
                    }
                }
            }
        }
    }

    private ItemStack getItemStack(Inventory clickInv, ItemStack fItem, NamespacedKey key, NamespacedKey key1) {
        for(ItemStack item : clickInv.getContents()) {
            if(item != null && item.getItemMeta() != null) {
                ItemMeta iMeta = item.getItemMeta();
                PersistentDataContainer iData = iMeta.getPersistentDataContainer();
                if (iData.has(key, PersistentDataType.INTEGER) && iData.has(key1, PersistentDataType.STRING)) {
                    fItem = item;
                }
            }
        }
        return fItem;
    }
}
