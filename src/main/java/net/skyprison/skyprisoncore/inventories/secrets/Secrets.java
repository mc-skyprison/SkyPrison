package net.skyprison.skyprisoncore.inventories.secrets;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.secrets.Secret;
import net.skyprison.skyprisoncore.utils.secrets.SecretCategory;
import net.skyprison.skyprisoncore.utils.secrets.SecretsUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Secrets implements CustomInventory {
    private final List<Secret> secrets = new ArrayList<>();
    private final HashMap<Secret, Long> cooldowns = new HashMap<>();
    private final HashMap<Secret, Integer> found = new HashMap<>();
    private final List<Integer> positions = Arrays.asList(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33);
    private final Inventory inventory;
    private int page = 1;
    private final boolean canEditSecrets;
    private final boolean canEditCategories;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack redPane;
    private final ItemStack categoryItem = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack typeItem = new ItemStack(Material.COMPARATOR);
    private final ItemStack showItem = new ItemStack(Material.COMPASS);
    private final ItemStack sortItem = new ItemStack(Material.OAK_SIGN);
    private final List<SecretCategory> categories = new ArrayList<>();
    private final List<String> types = Arrays.asList("all", "secret", "parkour", "puzzle");
    private final List<String> show = Arrays.asList("all", "available", "not_available", "found", "not_found");
    private final List<String> sort = Arrays.asList("a_-_z", "z_-_a", "least_time_left", "most_time_left", "least_found", "most_found");
    private int catPos = 0;
    private int typePos = 0;
    private int showPos = 0;
    private int sortPos = 0;

    public void updateCategory(Boolean direction) {
        if(direction != null) catPos = direction ? (catPos + 1) % categories.size() : (catPos - 1 + categories.size()) % categories.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        categoryItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            categories.forEach(category -> lore.add(Component.text(WordUtils.capitalize(category.name().replace("-", " ")),
                    getCategory().name().equalsIgnoreCase(category.name()) ? selectedColor : color).decoration(TextDecoration.BOLD,
                            getCategory().name().equalsIgnoreCase(category.name())).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        inventory.setItem(38, categoryItem);
        inventory.setItem(4, getCategory().displayItem());
    }
    public void updateType(Boolean direction) {
        if(direction != null) typePos = direction ? (typePos + 1) % types.size() : (typePos - 1 + types.size()) % types.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        typeItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            types.forEach(type -> lore.add(Component.text(WordUtils.capitalize(type).replace("-", " "),
                    getType().equalsIgnoreCase(type) ? selectedColor : color).decoration(TextDecoration.BOLD,
                    getType().equalsIgnoreCase(type)).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        inventory.setItem(39, typeItem);
    }
    public void updateShowing(Boolean direction) {
        if(direction != null) showPos = direction ? (showPos + 1) % show.size() : (showPos - 1 + show.size()) % show.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        showItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            show.forEach(show -> lore.add(Component.text(WordUtils.capitalize(show).replace("_", " "),
                    getShow().equalsIgnoreCase(show) ? selectedColor : color).decoration(TextDecoration.BOLD,
                    getShow().equalsIgnoreCase(show)).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        inventory.setItem(41, showItem);
    }

    public void updateSort(Boolean direction) {
        if(direction != null) sortPos = direction ? (sortPos + 1) % sort.size() : (sortPos - 1 + sort.size()) % sort.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        sortItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            sort.forEach(sort -> lore.add(Component.text(WordUtils.capitalize(sort).replace("_", " "),
                    getSort().equalsIgnoreCase(sort) ? selectedColor : color).decoration(TextDecoration.BOLD,
                    getSort().equalsIgnoreCase(sort)).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });

        secrets.sort((s1, s2) -> {
            if (getSort().equals("a_->_z")) {
                return s1.name().compareToIgnoreCase(s2.name());
            } else if (getSort().equals("z_->_a")) {
                return s2.name().compareToIgnoreCase(s1.name());
            } else if (getSort().equals("least_time_left")) {
                return Long.compare(cooldowns.get(s1), cooldowns.get(s2));
            } else if (getSort().equals("most_time_left")) {
                return Long.compare(cooldowns.get(s2), cooldowns.get(s1));
            } else if (getSort().equals("least_found")) {
                return Integer.compare(found.get(s1), found.get(s2));
            } else if (getSort().equals("most_found")) {
                return Integer.compare(found.get(s2), found.get(s1));
            }
            return 0;
        });

        inventory.setItem(42, sortItem);
    }
    public void updatePage(int page) {
        List<Secret> secretsToShow = new ArrayList<>(secrets);

        secretsToShow = secretsToShow.stream().filter(secret -> {
            if(!getCategory().name().equals("all") && !secret.category().equals(getCategory().name())) return false;
            if(!getType().equals("all") && !secret.type().equals(getType())) return false;
            if(!getShow().equals("all")) {
                if(getShow().equals("available") && cooldowns.containsKey(secret) && TimeUnit.MILLISECONDS.toDays(cooldowns.get(secret)) > TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) return false;
                if(getShow().equals("not-available") && !cooldowns.containsKey(secret) || TimeUnit.MILLISECONDS.toDays(cooldowns.get(secret)) < TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) return false;
                if(getShow().equals("found") && !found.containsKey(secret)) return false;
                return !getShow().equals("not-found") || !found.containsKey(secret);
            }
            return true;
        }).toList();


        int totalPages = (int) Math.ceil((double) secretsToShow.size() / 15);

        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }
        positions.forEach(pos -> inventory.setItem(pos, null));
        inventory.setItem(36, this.page == 1 ? redPane : prevPage);
        inventory.setItem(44, totalPages < 2 || this.page == totalPages ? redPane : nextPage);
        int toRemove = 15 * (this.page - 1);
        if(toRemove != 0) {
            secretsToShow = secretsToShow.subList(toRemove, secretsToShow.size());
        }
        Iterator<Secret>  secretsIterator = secretsToShow.iterator();
        positions.forEach(pos -> {
            if (secretsIterator.hasNext()) inventory.setItem(pos, secretsIterator.next().displayItem());
        });
    }
    private int getCatPosFromRegion(String worldName, HashMap<String, Integer> regions) {
        HashMap<Integer, List<SecretCategory>> selCats = new HashMap<>();
        categories.forEach(category -> category.regions().getOrDefault(worldName, new ArrayList<>()).forEach(region -> {
                    if(regions.containsKey(region)) {
                        int priority = regions.get(region);
                        List<SecretCategory> cats = selCats.getOrDefault(priority, new ArrayList<>());
                        cats.add(category);
                        selCats.put(priority, cats);
                    }
                }));
        if(!selCats.isEmpty()) {
            int priority = Collections.max(selCats.keySet());
            return categories.indexOf(selCats.get(priority).get(0));
        }
        return 0;
    }
    private HashMap<String, Integer> getApplicableRegions(Player player, RegionManager rm) {
        Location loc = player.getLocation();
        ApplicableRegionSet regionList = rm.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
        return regionList.getRegions().stream().collect(HashMap::new, (m, r) -> m.put(r.getId(), r.getPriority()), HashMap::putAll);
    }
    public Secrets(SkyPrisonCore plugin, DatabaseHook db, Player player, final String category, boolean canEditSecrets, boolean canEditCategories) {
        this.canEditSecrets = canEditSecrets;
        this.canEditCategories = canEditCategories;
        ItemStack allSecrets = new ItemStack(Material.MAGENTA_CONCRETE);
        allSecrets.editMeta(meta -> meta.displayName(Component.text("All Secrets", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false)));
        categories.add(new SecretCategory("all", "Shows all Secrets", allSecrets, "", "", new HashMap<>(), false));
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT name, description, display_item, regions FROM secrets_categories WHERE deleted = 0 ORDER BY category_order ASC")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String name = rs.getString(1);
                String description = rs.getString(2);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(3));
                if(canEditCategories) {
                    displayItem.editMeta(meta -> {
                        List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
                        lore.add(Component.empty());
                        lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        meta.lore(lore);
                    });
                }
                String regions = rs.getString(4);
                HashMap<String, List<String>> regionMap = new HashMap<>();
                if(regions != null && !regions.isEmpty()) {
                    Arrays.stream(regions.split(";")).forEach(region -> {
                        String[] split = region.split(":"); // region : world
                        List<String> worldRegions = regionMap.getOrDefault(split[1], new ArrayList<>(Collections.singleton(split[0])));
                        regionMap.put(split[1], worldRegions);
                    });
                }
                categories.add(new SecretCategory(name, description, displayItem, "", "", regionMap, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, display_item, category, type, reward_type, reward, cooldown, max_uses, deleted FROM secrets")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(3));
                String sCategory = rs.getString(4);
                String type = rs.getString(5);
                String rewardType = rs.getString(6);
                int reward = rs.getInt(7);
                String cooldown = rs.getString(8);
                int maxUses = rs.getInt(9);
                int deleted = rs.getInt(10);
                Component coolText = SecretsUtils.getTimeLeft(id, cooldown, player.getUniqueId());
                int found = SecretsUtils.getFoundAmount(id, player.getUniqueId().toString());
                long lastFound = SecretsUtils.getTimeSinceLastFound(id, player.getUniqueId().toString());
                long daysSince = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastFound);
                Secret secret;
                String doneType = type.equals("parkour") ? "done" : type.equals("puzzle") ? "completed" : "found";
                if (found > 0 || canEditSecrets) {
                    displayItem.editMeta(meta -> {
                        meta.displayName(MiniMessage.miniMessage().deserialize(name).decoration(TextDecoration.ITALIC, false));
                        List<Component> lore = new ArrayList<>();
                        if(deleted == 1) {
                            lore.add(Component.text("(No Longer Available)", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        }
                        lore.add(Component.text("Reward", NamedTextColor.GRAY).append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(reward + " " + WordUtils.capitalize(rewardType.replace("-", " ")),
                                        TextColor.fromHexString("#48e2e5"), TextDecoration.BOLD)).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.empty());
                        lore.add(coolText.decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("                       ", NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("You've " + doneType + " this " + type + " ", NamedTextColor.GRAY)
                                .append(Component.text(found + (maxUses > 0 ? "/" + maxUses : ""), TextColor.fromHexString("#48e2e5"), TextDecoration.BOLD))
                                .append(Component.text(" time" + (found > 1 ? "s" : ""), NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));

                        lore.add(Component.text("You last " + (type.equals("parkour") ? "did" : doneType) + " it ", NamedTextColor.GRAY)
                                .append(Component.text(daysSince, TextColor.fromHexString("#48e2e5"), TextDecoration.BOLD))
                                .append(Component.text(" day" + (found > 1 ? "s" : "") + " ago", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false));
                        if(canEditSecrets) {
                            lore.add(Component.empty());
                            lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        }
                        meta.lore(lore);

                        NamespacedKey key = new NamespacedKey(plugin, "secret-id");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
                    });
                    secret = new Secret(id, name, displayItem, sCategory, type, rewardType, reward, cooldown, maxUses, deleted == 1);

                    long collected = SecretsUtils.getPlayerCooldown(id, player.getUniqueId());

                    if(collected != 0) {
                        collected += SecretsUtils.coolInMillis(cooldown);
                    }
                    cooldowns.put(secret, collected);
                    this.found.put(secret, found);
                } else {
                    if(deleted == 1) continue;
                    ItemStack notFound = new ItemStack(Material.BOOK);
                    notFound.editMeta(meta -> {
                        meta.displayName(Component.text("???", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                        meta.lore(Collections.singletonList(Component.text("Find this secret to unlock it..", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)));
                    });
                    displayItem = notFound;
                    secret = new Secret(id, "???", displayItem, sCategory, type, rewardType, reward, null, maxUses, false);
                }
                secrets.add(secret);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(category != null && !category.isEmpty()) {
            catPos = categories.stream().filter(cat -> cat.name().equalsIgnoreCase(category)).findFirst().map(categories::indexOf).orElse(0);
        } else {
            World world = player.getWorld();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager rm = container.get(BukkitAdapter.adapt(world));
            if(rm != null) {
                HashMap<String, Integer> regions = getApplicableRegions(player, rm);
                catPos = getCatPosFromRegion(world.getName(), regions);
            }
        }

        this.inventory = plugin.getServer().createInventory(this, 45, Component.text("Secrets", TextColor.fromHexString("#30baa7"), TextDecoration.BOLD));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        for (int i = 0; i < 45; i++) {
            if(i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44)
                inventory.setItem(i, redPane);
            else if(i < 9 || i > 36 || i == 10 || i == 16 || i == 19 || i == 25 || i == 28 || i == 34)
                inventory.setItem(i, blackPane);
        }

        categoryItem.editMeta(meta -> meta.displayName(Component.text("Switch Category", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        showItem.editMeta(meta -> meta.displayName(Component.text("Toggle Secrets", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        typeItem.editMeta(meta -> meta.displayName(Component.text("Switch Type", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        sortItem.editMeta(meta -> meta.displayName(Component.text("Switch Sort", NamedTextColor.GOLD, TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false)));
        updateCategory(null);
        updateType(null);
        updateShowing(null);
        updateSort(null);
        updatePage(0);
    }

    public List<Integer> getPositions() {
        return positions;
    }
    public SecretCategory getCategory() {
        return this.categories.get(catPos);
    }
    public String getType() {
        return this.types.get(typePos);
    }
    public String getShow() {
        return this.show.get(showPos);
    }
    public String getSort() {
        return this.sort.get(sortPos);
    }

    @Override
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }
    @Override
    public List<Object> customClickList() {
        return null;
    }
    @Override
    public int getPage() {
        return 1;
    }

    public boolean canEditSecrets() {
        return canEditSecrets;
    }
    public boolean canEditCategories() {
        return canEditCategories;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
