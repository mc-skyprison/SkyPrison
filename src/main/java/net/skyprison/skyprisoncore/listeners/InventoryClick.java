package net.skyprison.skyprisoncore.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.commands.Daily;
import net.skyprison.skyprisoncore.commands.economy.*;
import net.skyprison.skyprisoncore.commands.secrets.SecretsGUI;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

public class InventoryClick implements Listener {
    private SkyPrisonCore plugin;
    private EconomyCheck econCheck;
    private DropChest chestDrop;
    private Bounty bounty;
    private SecretsGUI secretsGUI;
    private Daily daily;
    private MoneyHistory moneyHistory;
    private EndUpgrade endUpgrade;
    private BuyBack buyBack;

    public InventoryClick(SkyPrisonCore plugin, EconomyCheck econCheck, DropChest dropChest, Bounty bounty, SecretsGUI secretsGUI, Daily daily, MoneyHistory moneyHistory, EndUpgrade endUpgrade, BuyBack buyBack) {
        this.plugin = plugin;
        this.econCheck = econCheck;
        this.chestDrop = dropChest;
        this.bounty = bounty;
        this.secretsGUI = secretsGUI;
        this.daily = daily;
        this.moneyHistory = moneyHistory;
        this.endUpgrade = endUpgrade;
        this.buyBack = buyBack;
    }

    @EventHandler
    public void invClick(InventoryClickEvent event) throws IOException {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
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
                    if(clickInv.getItem(event.getSlot()) == null) {
                        event.setCancelled(true);
                    }
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
                                                File buyFile = new File(plugin.getDataFolder() + File.separator + "recentsells.yml");
                                                FileConfiguration buyConf = YamlConfiguration.loadConfiguration(buyFile);
                                                ArrayList<String> soldItems = (ArrayList<String>) Objects.requireNonNull(buyConf.getStringList(player.getUniqueId() + ".sold-items"));
                                                NamespacedKey posKey = new NamespacedKey(plugin, "sold-pos");
                                                int buyPos = buyData.get(posKey, PersistentDataType.INTEGER);
                                                soldItems.remove(buyPos);
                                                buyConf.set(player.getUniqueId() + ".sold-items", soldItems);
                                                buyConf.save(buyFile);
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
                            case "transaction-history":
                                Material clickedMat = Objects.requireNonNull(event.getClickedInventory().getItem(event.getSlot())).getType();

                                NamespacedKey tKey = new NamespacedKey(plugin, "sort");
                                NamespacedKey tKey1 = new NamespacedKey(plugin, "toggle");
                                NamespacedKey tKey2 = new NamespacedKey(plugin, "page");
                                Boolean transSort = Boolean.parseBoolean(fData.get(tKey, PersistentDataType.STRING));
                                String transToggle = fData.get(tKey1, PersistentDataType.STRING);
                                int transPage = fData.get(tKey2, PersistentDataType.INTEGER);
                                if(clickedMat.equals(Material.PAPER)) {
                                    if (event.getSlot() == 45) {
                                        moneyHistory.openGUI(player, transSort, transToggle, transPage-1);
                                    } else if (event.getSlot() == 53) {
                                        moneyHistory.openGUI(player, transSort, transToggle, transPage+1);
                                    }
                                } else if(clickedMat.equals(Material.CLOCK)) {
                                    if(transSort)
                                        moneyHistory.openGUI(player, false, transToggle, transPage);
                                    else
                                        moneyHistory.openGUI(player, true, transToggle, transPage);
                                } else if (clickedMat.equals(Material.COMPASS)) {
                                    if(transToggle.equalsIgnoreCase("null")) {
                                        moneyHistory.openGUI(player, transSort, "true", 1);
                                    } else if(transToggle.equalsIgnoreCase("true")) {
                                        moneyHistory.openGUI(player, transSort, "false", 1);
                                    } else if(transToggle.equalsIgnoreCase("false")) {
                                        moneyHistory.openGUI(player, transSort, "null", 1);

                                    }

                                }
                                break;
                            case "skyplot-gui":

                                break;
                            case "daily-reward":
                                if(event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.MINECART)) {
                                    player.sendMessage(plugin.colourMessage("&cYou've already collected the daily reward!"));
                                } else if(event.getClickedInventory().getItem(event.getSlot()).getType().equals(Material.CHEST_MINECART)) {
                                    File dailyFile = new File(plugin.getDataFolder() + File.separator + "dailyreward.yml");
                                    FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(dailyFile);
                                    int currStreak = dailyConf.getInt("players." + player.getUniqueId() + ".current-streak");
                                    int highestStreak = dailyConf.getInt("players." + player.getUniqueId() + ".highest-streak");
                                    int totalCollected = dailyConf.getInt("players." + player.getUniqueId() + ".total-collected");

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
                                    dailyConf.set("players." + player.getUniqueId() + ".current-streak", currStreak + 1);
                                    dailyConf.set("players." + player.getUniqueId() + ".total-collected", totalCollected + 1);

                                    if(currStreak >= highestStreak) {
                                        dailyConf.set("players." + player.getUniqueId() + ".highest-streak", currStreak + 1);
                                    }

                                    Date date = new Date();
                                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                    String currDate = formatter.format(date);

                                    dailyConf.set("players." + player.getUniqueId() + ".last-collected", currDate);
                                    dailyConf.save(dailyFile);
                                    daily.openGUI(player);
                                }
                                break;
                            case "plotteleport":
                                ItemStack itemClick = clickInv.getItem(event.getSlot());
                                PersistentDataContainer plotData = Objects.requireNonNull(itemClick).getPersistentDataContainer();
                                NamespacedKey plotKey = new NamespacedKey(plugin, "x");
                                if(plotData.has(plotKey, PersistentDataType.DOUBLE)) {
                                    NamespacedKey plotKey1 = new NamespacedKey(plugin, "y");
                                    NamespacedKey plotKey2 = new NamespacedKey(plugin, "z");
                                    NamespacedKey plotKey3 = new NamespacedKey(plugin, "world");
                                    double x = plotData.get(plotKey, PersistentDataType.DOUBLE);
                                    double y = plotData.get(plotKey1, PersistentDataType.DOUBLE);
                                    double z = plotData.get(plotKey2, PersistentDataType.DOUBLE);
                                    World world = Bukkit.getWorld(plotData.get(plotKey3, PersistentDataType.STRING));
                                    Location loc = new Location(world, x, y, z);
                                    if(player.getWorld().getName().equalsIgnoreCase("world_skycity") || player.hasPermission("cmi.command.tpa.warmupbypass")) {
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
                                break;
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
                            File secretsDataFile = new File(plugin.getDataFolder() + File.separator
                                    + "secretsdata.yml");
                            FileConfiguration pData = YamlConfiguration.loadConfiguration(secretsDataFile);
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
                                    pData.set(player.getUniqueId() + ".rewards." + foundValue + ".collected", true);
                                    try {
                                        pData.save(secretsDataFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
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

        if (ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("bounties")) {
            if (event.getCurrentItem() != null) {
                event.setCancelled(true);
                if(event.getCurrentItem().getType() == Material.PAPER) {
                    if(event.getSlot() == 46) {
                        int page = Integer.parseInt(dropChest[4])-1;
                        bounty.openGUI((Player) event.getWhoClicked(), page);
                    } else if(event.getSlot() == 52) {
                        int page = Integer.parseInt(dropChest[4])+1;
                        bounty.openGUI((Player) event.getWhoClicked(), page);
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
