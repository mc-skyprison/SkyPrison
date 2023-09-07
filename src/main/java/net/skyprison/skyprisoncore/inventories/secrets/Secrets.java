package net.skyprison.skyprisoncore.inventories.secrets;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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

public class Secrets implements CustomInventory {
    private final List<Secret> secrets = new ArrayList<>();
    private final List<Integer> positions = Arrays.asList(11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33);
    private final Inventory inventory;
    private int page = 1;
    private final boolean canEditSecrets;
    private final boolean canEditCategories;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack redPane;
    private boolean showNotFound = false;
    private final ItemStack categoryItem = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack typeItem = new ItemStack(Material.COMPARATOR);
    private final ItemStack notFoundItem = new ItemStack(Material.COMPASS);
    private final List<SecretCategory> categories = new ArrayList<>();
    private final List<String> types = Arrays.asList("all", "secret", "parkour", "puzzle");
    private int catPos = 0;
    private int typePos = 0;

    public void updateCategory(Boolean direction) {
        System.out.println(catPos);
        if(direction != null) catPos = direction ? (catPos + 1) % categories.size() : (catPos - 1 + categories.size()) % categories.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        categoryItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            categories.forEach(category -> lore.add(Component.text(WordUtils.capitalize(category.name()).replace("-", " "),
                    getCategory().name().equalsIgnoreCase(category.name()) ? selectedColor : color).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        inventory.setItem(39, categoryItem);
        inventory.setItem(4, getCategory().displayItem());
    }
    public void updateType(Boolean direction) {
        if(direction != null) typePos = direction ? (typePos + 1) % types.size() : (typePos - 1 + types.size()) % types.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        typeItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            types.forEach(type -> lore.add(Component.text(WordUtils.capitalize(type).replace("-", " "),
                    getType().equalsIgnoreCase(type) ? selectedColor : color).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        inventory.setItem(41, typeItem);
    }
    public void toggleNotFound() {
        showNotFound = !showNotFound;
        notFoundItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Currently ", NamedTextColor.GRAY).append(Component.text(showNotFound ? "SHOWING" : "HIDING",
                            showNotFound ? NamedTextColor.GREEN : NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(" secrets you haven't found", NamedTextColor.GRAY))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(40, notFoundItem);
    }
    public void updatePage(int page) {
        List<Secret> secretsToShow = new ArrayList<>(secrets);
        secretsToShow = secretsToShow.stream().filter(secret -> (getCategory().name().equalsIgnoreCase("all") || secret.category().equalsIgnoreCase(getCategory().name()))
                && (getType().equalsIgnoreCase("all") || secret.type().equalsIgnoreCase(getType()))
                && (showNotFound || secret.cooldown() != null)).toList();
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
    private int getCatPosFromRegion(String worldName, List<String> regions) {
        for (int i = 0; i < categories.size(); i++) {
            Map<String, List<String>> regionMap = categories.get(i).regions();
            List<String> worldRegions = regionMap.get(worldName);
            if (worldRegions != null && !Collections.disjoint(worldRegions, regions)) {
                return i;
            }
        }
        return 0;
    }
    private List<String> getApplicableRegions(Player player, RegionManager rm) {
        Location loc = player.getLocation();
        ApplicableRegionSet regionList = rm.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
        return regionList.getRegions().stream().map(ProtectedRegion::getId).toList();
    }
    public Secrets(SkyPrisonCore plugin, DatabaseHook db, Player player, final String category, boolean canEditSecrets, boolean canEditCategories) {
        this.canEditSecrets = canEditSecrets;
        this.canEditCategories = canEditCategories;
        ItemStack allSecrets = new ItemStack(Material.MAGENTA_CONCRETE);
        allSecrets.editMeta(meta -> meta.displayName(Component.text("All Secrets", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false)));
        categories.add(new SecretCategory("all", "Shows all Secrets", allSecrets, "", "", new HashMap<>()));
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT name, description, display_item, regions FROM secrets_categories")) {
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
                Arrays.stream(regions.split(";")).forEach(region -> {
                    String[] split = region.split(":"); // region : world
                    List<String> worldRegions = regionMap.getOrDefault(split[0], new ArrayList<>(Collections.singleton(split[1])));
                    regionMap.put(split[0], worldRegions);
                });
                categories.add(new SecretCategory(name, description, displayItem, "", "", regionMap));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, display_item, category, type, reward_type, reward, cooldown, max_uses FROM secrets")) {
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
                Component coolText = SecretsUtils.getTimeLeft(id, cooldown, player.getUniqueId());
                int found = SecretsUtils.getFoundAmount(id, player.getUniqueId().toString());
                if (found > 0 || canEditSecrets) {
                    displayItem.editMeta(meta -> {
                        meta.displayName(MiniMessage.miniMessage().deserialize(name));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("You've " + (type.equals("parkour") ? "done this parkour " : type.equals("puzzle") ?
                                        "completed this puzzle " : "found this secret "), NamedTextColor.GRAY)
                                .append(Component.text(found + (maxUses > 0 ? "/" + maxUses : ""), NamedTextColor.AQUA)).append(Component.text(" time" + (found > 1 ? "s" : ""), NamedTextColor.GRAY))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Cooldown: ", NamedTextColor.RED).append(coolText).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.empty());
                        lore.add(Component.text("Reward: ", NamedTextColor.AQUA).append(Component.text(reward + " " +
                                        WordUtils.capitalize(rewardType.replace("-", " ")), NamedTextColor.GRAY))
                                .decoration(TextDecoration.ITALIC, false));
                        if(canEditSecrets) {
                            lore.add(Component.empty());
                            lore.add(Component.text("SHIFT CLICK TO EDIT", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
                        }
                        meta.lore(lore);

                        NamespacedKey key = new NamespacedKey(plugin, "secret-id");
                        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, id);
                    });
                } else {
                    ItemStack notFound = new ItemStack(Material.BOOK);
                    notFound.editMeta(meta -> {
                        meta.displayName(Component.text("???", NamedTextColor.RED, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                        meta.lore(Collections.singletonList(Component.text("Find this secret to unlock it..", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)));
                    });
                    displayItem = notFound;
                }
                Secret secret = new Secret(id, name, displayItem, sCategory, type, rewardType, reward, null);
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
                List<String> regions = getApplicableRegions(player, rm);
                catPos = getCatPosFromRegion(world.getName(), regions);
            }
        }

        this.inventory = plugin.getServer().createInventory(this, 45, Component.text("Secrets", NamedTextColor.GREEN));

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

        categoryItem.editMeta(meta -> meta.displayName(Component.text("Switch Category", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)));
        notFoundItem.editMeta(meta -> meta.displayName(Component.text("Toggle Not Found Secrets", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)));
        typeItem.editMeta(meta -> meta.displayName(Component.text("Switch Type", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)));
        updateCategory(null);
        updateType(null);
        toggleNotFound();
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
