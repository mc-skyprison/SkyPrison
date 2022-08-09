package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.Daily;
import net.skyprison.skyprisoncore.commands.SkyPlot;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.secrets.SecretsGUI;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
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
import java.io.IOException;
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
    private final DatabaseHook hook;

    public InventoryClick(SkyPrisonCore plugin, EconomyCheck econCheck, DropChest dropChest, Bounty bounty,
                          SecretsGUI secretsGUI, Daily daily, MoneyHistory moneyHistory, EndUpgrade endUpgrade,
                          BuyBack buyBack, SkyPlot skyPlot, DatabaseHook hook) {
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
        this.hook = hook;
    }

    public boolean isStick(ItemStack i) {
        if (i != null) {
            return i.getType() == Material.STICK && i.getItemMeta().hasDisplayName()
                    && i.getItemMeta().getDisplayName().contains("Santa's")
                    && (i.getItemMeta().hasEnchant(Enchantment.KNOCKBACK) && (i.getItemMeta().getEnchantLevel(Enchantment.KNOCKBACK) > 1));
        }
        return false;
    }

    public void InvStickFix(Player player) {
        for (int n = 0; n < player.getInventory().getSize(); n++) {
            ItemStack i = player.getInventory().getItem(n);
            if (isStick(i)) {
                ItemMeta asd = i.getItemMeta();
                asd.removeEnchant(Enchantment.KNOCKBACK);
                asd.addEnchant(Enchantment.KNOCKBACK, 1, true);
                i.setItemMeta(asd);
            }
        }
    }

    @EventHandler
    public void invClick(InventoryClickEvent event) throws IOException {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if(event.getClickedInventory() instanceof PlayerInventory) {
                InvStickFix(player);
            }

            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
            user.getCMIPlayTime().getPlayDayOfToday().getTotalTime();
            Inventory clickInv = event.getClickedInventory();
            if (clickInv != null && !clickInv.isEmpty() && clickInv.getItem(0) != null) {
                ItemStack fItem = clickInv.getItem(0);
                ItemMeta fMeta = Objects.requireNonNull(fItem).getItemMeta();
                PersistentDataContainer fData = fMeta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(plugin, "stop-click");
                NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
                if (fData.has(key, PersistentDataType.INTEGER) && fData.has(key1, PersistentDataType.STRING)) {
                    int clickCheck = fData.get(key, PersistentDataType.INTEGER);
                    String guiType = fData.get(key1, PersistentDataType.STRING);
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
                                if(alcData.has(alcKey, PersistentDataType.STRING)) {
                                    String alcType = alcData.get(alcKey, PersistentDataType.STRING);
                                    int price = yamlf.getInt("grass." + alcType + ".price");
                                    if(user.getBalance() >= price) {
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
                                if(buyItem != null) {
                                    ItemMeta buyMeta = buyItem.getItemMeta();
                                    PersistentDataContainer buyData = buyMeta.getPersistentDataContainer();
                                    if(buyData.has(typeKey, PersistentDataType.STRING)) {
                                        NamespacedKey amKey = new NamespacedKey(plugin, "sold-amount");
                                        NamespacedKey priKey = new NamespacedKey(plugin, "sold-price");
                                        String itemType = buyData.get(typeKey, PersistentDataType.STRING);
                                        int itemAmount = buyData.get(amKey, PersistentDataType.INTEGER);
                                        Double itemPrice = buyData.get(priKey, PersistentDataType.DOUBLE);
                                        ItemStack iSold = new ItemStack(Objects.requireNonNull(Material.getMaterial(Objects.requireNonNull(itemType))), itemAmount);
                                        if(user.getInventory().canFit(iSold)) {
                                            if(user.getBalance() >= itemPrice) {
                                                NamespacedKey posKey = new NamespacedKey(plugin, "sold-id");
                                                int buyId = buyData.get(posKey, PersistentDataType.INTEGER);

                                                String sql = "DELETE FROM recent_sells WHERE recent_id = ?";
                                                List<Object> params = new ArrayList<Object>() {{
                                                    add(buyId);
                                                }};
                                                hook.sqlUpdate(sql, params);
                                                plugin.asConsole("give " + player.getName() + " " + itemType + " " + itemAmount);
                                                plugin.asConsole("money take " + player.getName() + " " + itemPrice);
                                                buyBack.openGUI(player);
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
                                if(event.getSlot() == 11) {
                                    ItemStack pMain = player.getInventory().getItemInMainHand();
                                    if(pMain.getType() != Material.AIR) {
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
                                } else if(event.getSlot() == 15) {
                                    player.closeInventory();
                                }
                                break;
                            case "endupgrade":
                                ItemStack clickedItem = clickInv.getItem(event.getSlot());
                                if(event.getSlot() == 20) {
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
                                } else if(event.getSlot() == 24) {
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
                                }  else if(event.getSlot() == 31) {
                                    ItemStack pMain = player.getInventory().getItemInMainHand();
                                    if(pMain.getType() != Material.AIR) {
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
                                if(event.getSlot() == 11) {
                                    ItemStack pMain = player.getInventory().getItemInMainHand();
                                    if (pMain.getType() != Material.AIR) {
                                        ItemStack confirmItem = clickInv.getItem(11);
                                        PersistentDataContainer confirmData = Objects.requireNonNull(confirmItem).getPersistentDataContainer();
                                        NamespacedKey enchKey = new NamespacedKey(plugin, "ench-state");
                                        int enchCheck = confirmData.get(enchKey, PersistentDataType.INTEGER);

                                        NamespacedKey repKey = new NamespacedKey(plugin, "repair-state");
                                        int repCheck = confirmData.get(repKey, PersistentDataType.INTEGER);

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

                                        if(!player.hasPermission("skyprisoncore.command.endupgrade.first-time")) {
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
                                if(event.getClickedInventory().getItem(event.getSlot()) != null) {
                                    Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();

                                    NamespacedKey tKey2 = new NamespacedKey(plugin, "page");
                                    int transPage = fData.get(tKey2, PersistentDataType.INTEGER);
                                    if (clickedMat.equals(Material.PAPER)) {
                                        if (event.getSlot() == 46) {
                                            bounty.openGUI(player, transPage - 1);
                                        } else if (event.getSlot() == 52) {
                                            bounty.openGUI(player, transPage + 1);
                                        }
                                    }
                                }
                            case "transaction-history":
                                if(event.getClickedInventory().getItem(event.getSlot()) != null) {
                                    Material clickedMat = event.getClickedInventory().getItem(event.getSlot()).getType();

                                    NamespacedKey tKey = new NamespacedKey(plugin, "sort");
                                    NamespacedKey tKey1 = new NamespacedKey(plugin, "toggle");
                                    NamespacedKey tKey2 = new NamespacedKey(plugin, "page");
                                    Boolean transSort = Boolean.parseBoolean(fData.get(tKey, PersistentDataType.STRING));
                                    String transToggle = fData.get(tKey1, PersistentDataType.STRING);
                                    int transPage = fData.get(tKey2, PersistentDataType.INTEGER);
                                    if (clickedMat.equals(Material.PAPER)) {
                                        if (event.getSlot() == 45) {
                                            moneyHistory.openGUI(player, transSort, transToggle, transPage - 1);
                                        } else if (event.getSlot() == 53) {
                                            moneyHistory.openGUI(player, transSort, transToggle, transPage + 1);
                                        }
                                    } else if (clickedMat.equals(Material.CLOCK)) {
                                        if (transSort)
                                            moneyHistory.openGUI(player, false, transToggle, transPage);
                                        else
                                            moneyHistory.openGUI(player, true, transToggle, transPage);
                                    } else if (clickedMat.equals(Material.COMPASS)) {
                                        if (transToggle.equalsIgnoreCase("null")) {
                                            moneyHistory.openGUI(player, transSort, "true", 1);
                                        } else if (transToggle.equalsIgnoreCase("true")) {
                                            moneyHistory.openGUI(player, transSort, "false", 1);
                                        } else if (transToggle.equalsIgnoreCase("false")) {
                                            moneyHistory.openGUI(player, transSort, "null", 1);

                                        }

                                    }
                                }
                                break;
                            case "skyplot-gui":
                                NamespacedKey skyKey = new NamespacedKey(plugin, "skyplot-type");
                                String page = fData.get(skyKey, PersistentDataType.STRING);
                                switch(page.toLowerCase()) {
                                    case "main":
                                        switch(event.getSlot()) {
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
                                        if(event.getSlot() == 11) {
                                            skyPlot.skyPlotGUI(player, "banned", 1);
                                        } else if(event.getSlot() == 15) {
                                            skyPlot.setVisit(player);
                                            skyPlot.skyPlotGUI(player, "settings", 1);
                                        } else if(event.getSlot() == 22) {
                                            skyPlot.skyPlotGUI(player, "main", 1);
                                        }
                                        break;
                                    case "expand":
                                        clickedItem = event.getClickedInventory().getItem(event.getSlot());
                                        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
                                        ApplicableRegionSet regionList = regions.getApplicableRegions(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
                                        ProtectedRegion region = regionList.getRegions().iterator().next();
                                        if(clickedItem.getType().equals(Material.PLAYER_HEAD)) {
                                            switch (event.getSlot()) { // Default Size = 14 x 24 x 14
                                                case 10: // increase to 20 x 30 x 20
                                                    ProtectedRegion newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(3, -3 ,-3), region.getMaximumPoint().add(-3, 3 ,3));
                                                    newRegion.copyFrom(region);
                                                    regions.removeRegion(region.getId());
                                                    regions.addRegion(newRegion);
                                                    break;
                                                case 11: // increase by 30 x 40 x 30
                                                    newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1 ,-1), region.getMaximumPoint().add(-1, 1 ,1));
                                                    newRegion.copyFrom(region);
                                                    regions.removeRegion(region.getId());
                                                    regions.addRegion(newRegion);
                                                    break;
                                                case 12: // increase by 6
                                                    newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1 ,-1), region.getMaximumPoint().add(-1, 1 ,1));
                                                    newRegion.copyFrom(region);
                                                    regions.removeRegion(region.getId());
                                                    regions.addRegion(newRegion);
                                                    break;
                                                case 13: // increase by 8
                                                    newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1 ,-1), region.getMaximumPoint().add(-1, 1 ,1));
                                                    newRegion.copyFrom(region);
                                                    regions.removeRegion(region.getId());
                                                    regions.addRegion(newRegion);
                                                    break;
                                                case 14: // increase by 10
                                                    newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1 ,-1), region.getMaximumPoint().add(-1, 1 ,1));
                                                    newRegion.copyFrom(region);
                                                    regions.removeRegion(region.getId());
                                                    regions.addRegion(newRegion);
                                                    break;
                                                case 15: // increase by 12
                                                    newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1 ,-1), region.getMaximumPoint().add(-1, 1 ,1));
                                                    newRegion.copyFrom(region);
                                                    regions.removeRegion(region.getId());
                                                    regions.addRegion(newRegion);
                                                    break;
                                                case 16: // increase by 14
                                                    newRegion = new ProtectedCuboidRegion(region.getId(), region.getMaximumPoint().add(1, -1 ,-1), region.getMaximumPoint().add(-1, 1 ,1));
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
                                        if(event.getClickedInventory().getItem(event.getSlot()) != null) {
                                            ItemStack clickItem = event.getClickedInventory().getItem(event.getSlot());
                                            PersistentDataContainer clickData = clickItem.getPersistentDataContainer();

                                            NamespacedKey pageKey = new NamespacedKey(plugin, "skyplot-page");
                                            int pageNum = fData.get(pageKey, PersistentDataType.INTEGER);

                                            if(clickItem.getType().equals(Material.PLAYER_HEAD)) {
                                                NamespacedKey isleKey = new NamespacedKey(plugin, "skyplot-owner");
                                                String isleOwner = clickData.get(isleKey, PersistentDataType.STRING);
                                                Location loc = skyPlot.getIsleLoc(isleOwner);
                                                player.teleportAsync(loc);

                                            } else if(event.getSlot() == 48 && clickItem.getType().equals(Material.PAPER)) {
                                                skyPlot.skyPlotGUI(player, "main", pageNum-1);
                                            } else if(event.getSlot() == 49) {
                                                skyPlot.skyPlotGUI(player, "main", 1);
                                            } else if(event.getSlot() == 50 && clickItem.getType().equals(Material.PAPER)) {
                                                skyPlot.skyPlotGUI(player, "main", pageNum+1);
                                            }
                                        }
                                        break;
                                }

                                break;
                            case "daily-reward":
                                if(event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.MINECART)) {
                                    player.sendMessage(plugin.colourMessage("&cYou've already collected the daily reward!"));
                                } else if(event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.CHEST_MINECART)) {
                                    int currStreak = 0;
                                    int highestStreak = 0;
                                    int totalCollected = 0;

                                    try {
                                        Connection conn = hook.getSQLConnection();
                                        PreparedStatement ps = conn.prepareStatement("SELECT current_streak, highest_streak, total_collected FROM dailies WHERE user_id = '" + player.getUniqueId() + "'");
                                        ResultSet rs = ps.executeQuery();
                                        while(rs.next()) {
                                            currStreak = rs.getInt(1);
                                            highestStreak = rs.getInt(2);
                                            totalCollected = rs.getInt(3);
                                        }
                                        hook.close(ps, rs, conn);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }

                                    Random rand = new Random();
                                    int tReward = rand.nextInt(25) + 25;

                                    if((currStreak + 1) % 7 == 0) {
                                        tReward = 250;
                                    }

                                    Random rand2 = new Random();
                                    int randInt = rand2.nextInt(1000) + 1;
                                    if(randInt == 666) {
                                        tReward = randInt;
                                    }

                                    plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), tReward);

                                    int nCurrStreak = currStreak + 1;
                                    int nTotalCollected = totalCollected + 1;

                                    Date date = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    String currDate = formatter.format(date);

                                    String sql;
                                    List<Object> params;

                                    if(currStreak >= highestStreak) {
                                        sql = "UPDATE dailies SET current_streak = ?, highest_streak = ?, last_collected = ?, total_collected = ? WHERE user_id = ?";
                                        params = new ArrayList<Object>() {{
                                            add(nCurrStreak);
                                            add(nCurrStreak);
                                            add(currDate);
                                            add(nTotalCollected);
                                            add(user.getUniqueId().toString());
                                        }};
                                    } else {
                                        sql = "UPDATE dailies SET current_streak = ?, last_collected = ?, total_collected = ? WHERE user_id = ?";
                                        params = new ArrayList<Object>() {{
                                            add(nCurrStreak);
                                            add(currDate);
                                            add(nTotalCollected);
                                            add(user.getUniqueId().toString());
                                        }};
                                    }

                                    hook.sqlUpdate(sql, params);
                                    daily.openGUI(player);
                                }
                                break;
                            case "plotteleport":
                                if(clickInv.getItem(event.getSlot()) != null) {
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
                        }
                    } else if(clickCheck == 0) {
                        if ("blacksmith-gui".equals(Objects.requireNonNull(guiType))) {
                            if (event.getSlot() != 13) {
                                event.setCancelled(true);
                                if (event.getSlot() == 22) {
                                    ItemStack item = clickInv.getItem(13);
                                    if (item != null && !item.getType().isAir()) {
                                        NamespacedKey enchKey = new NamespacedKey(plugin, "telekinesis");
                                        List<Component> lore = new ArrayList<>();
                                        ItemMeta iMeta = item.getItemMeta();
                                        iMeta.getPersistentDataContainer().set(enchKey, PersistentDataType.INTEGER, 1);
                                        if (iMeta.lore() != null)
                                            lore = iMeta.lore();
                                        lore.add(Component.text(plugin.colourMessage("&7Telekinesis")));
                                        iMeta.lore(lore);
                                        item.setItemMeta(iMeta);
                                    }
                                }
                            } else if (event.getSlot() == 13) {
                                ItemStack item = clickInv.getItem(13);
                                if (item != null && item.getType().isAir()) {
                                    ArrayList<Material> allowedItems = new ArrayList<>();
                                    allowedItems.add(Material.WOODEN_AXE);
                                    allowedItems.add(Material.WOODEN_PICKAXE);
                                    allowedItems.add(Material.WOODEN_HOE);
                                    allowedItems.add(Material.WOODEN_SHOVEL);
                                    allowedItems.add(Material.WOODEN_SWORD);
                                    allowedItems.add(Material.STONE_AXE);
                                    allowedItems.add(Material.STONE_PICKAXE);
                                    allowedItems.add(Material.STONE_HOE);
                                    allowedItems.add(Material.STONE_SHOVEL);
                                    allowedItems.add(Material.STONE_SWORD);
                                    allowedItems.add(Material.IRON_AXE);
                                    allowedItems.add(Material.IRON_PICKAXE);
                                    allowedItems.add(Material.IRON_HOE);
                                    allowedItems.add(Material.IRON_SHOVEL);
                                    allowedItems.add(Material.IRON_SWORD);
                                    allowedItems.add(Material.DIAMOND_AXE);
                                    allowedItems.add(Material.DIAMOND_PICKAXE);
                                    allowedItems.add(Material.DIAMOND_HOE);
                                    allowedItems.add(Material.DIAMOND_SHOVEL);
                                    allowedItems.add(Material.DIAMOND_SWORD);
                                    allowedItems.add(Material.NETHERITE_AXE);
                                    allowedItems.add(Material.NETHERITE_PICKAXE);
                                    allowedItems.add(Material.NETHERITE_HOE);
                                    allowedItems.add(Material.NETHERITE_SHOVEL);
                                    allowedItems.add(Material.NETHERITE_SWORD);
                                    if (!allowedItems.contains(item.getType())) {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        String[] pageCheck = ChatColor.stripColor(event.getView().getTitle()).split(" ");
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
                            Player player = Bukkit.getPlayer(human.getName());
                            assert player != null;
                            File rewardsDataFile = new File(plugin.getDataFolder() + File.separator
                                    + "rewardsdata.yml");
                            FileConfiguration rData = YamlConfiguration.loadConfiguration(rewardsDataFile);

                            ItemStack currItem = event.getCurrentItem();
                            NamespacedKey key = new NamespacedKey(plugin, "reward");
                            ItemMeta itemMeta = currItem.getItemMeta();
                            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
                            String foundValue;
                            if(container.has(key, PersistentDataType.STRING)) {
                                foundValue = container.get(key, PersistentDataType.STRING);
                                if(Objects.requireNonNull(rData.getString(foundValue + ".reward-type")).equalsIgnoreCase("tokens")) {
                                    int tokenAmount = rData.getInt(foundValue + ".reward");
                                    plugin.tokens.addTokens(CMI.getInstance().getPlayerManager().getUser(player), tokenAmount);

                                    String sql = "UPDATE rewards_data SET reward_collected = ? WHERE user_id = ? AND reward_name = ?";
                                    List<Object> params = new ArrayList<Object>() {{
                                        add(1);
                                        add(player.getUniqueId().toString());
                                        add(foundValue);
                                    }};
                                    hook.sqlUpdate(sql, params);

                                    player.sendMessage(plugin.colourMessage("&f[&eSecrets&f] &aYou received " + tokenAmount + " tokens!"));
                                    secretsGUI.openGUI(player, "rewards");
                                }
                            }
                        }
                        break;
                    case "main":
                        switch(event.getSlot()) {
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
        } else if (pageCheck[0].equalsIgnoreCase("Shop") && pageCheck[1].equalsIgnoreCase("Log")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(pageCheck[4])-1;
                        econCheck.openGUI((Player) event.getWhoClicked(), page, "default");
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(pageCheck[4])+1;
                        econCheck.openGUI((Player) event.getWhoClicked(), page, "default");
                    }
                } else if(event.getCurrentItem().getType() == Material.BOOK) {
                    if(event.getSlot() == 47) {
                        int page = Integer.parseInt(pageCheck[4]);
                        econCheck.openGUI((Player) event.getWhoClicked(), page, "amounttop");
                    } else if(event.getSlot() == 48) {
                        int page = Integer.parseInt(pageCheck[4]);
                        econCheck.openGUI((Player) event.getWhoClicked(), page, "amountbottom");
                    } else if(event.getSlot() == 49) {
                        event.getWhoClicked().closeInventory();
                        event.getWhoClicked().sendMessage(ChatColor.RED + "/econcheck player <player>");
                    } else if(event.getSlot() == 50) {
                        int page = Integer.parseInt(pageCheck[4]);
                        econCheck.openGUI((Player) event.getWhoClicked(), page, "moneybottom");
                    } else if(event.getSlot() == 51) {
                        int page = Integer.parseInt(pageCheck[4]);
                        econCheck.openGUI((Player) event.getWhoClicked(), page, "moneytop");
                    }
                }
            }
        }
        String[] dropChest = ChatColor.stripColor(event.getView().getTitle()).split(" ");
        if(dropChest[0].equalsIgnoreCase("Drop") && dropChest[1].equalsIgnoreCase("Party")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(dropChest[4])-1;
                        chestDrop.openGUI((Player) event.getWhoClicked(), page);
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(dropChest[4])+1;
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

        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("skyprisoncore.contraband.itembypass")) {
            if(event.getClickedInventory() instanceof PlayerInventory) {
                plugin.InvGuardGearDelPlyr(player);
            }
        }
    }
}
