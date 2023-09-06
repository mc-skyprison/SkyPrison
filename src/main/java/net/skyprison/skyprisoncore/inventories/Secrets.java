package net.skyprison.skyprisoncore.inventories;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.Secret;
import net.skyprison.skyprisoncore.utils.SecretsUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Secrets implements CustomInventory {
    private final HashMap<String, List<Secret>> secrets = new HashMap<>();
    private final Inventory inventory;
    private String category;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack redPane;
    private final ItemStack blackPane;
    private final DatabaseHook db;

    public void changeCategory(String category) {
        this.category = category;
        secrets.get(category).forEach(secret -> inventory.setItem(secret.position(), secret.displayItem()));
    }
    private String getCategoryFromRegion(RegionManager rm, String worldName, Set<ProtectedRegion> regions) {
        if (!worldName.equalsIgnoreCase("world_prison")) {
            return worldName.equalsIgnoreCase("world_skycity") ? "skycity" : "main";
        }

        Map<String, String> categoryRegions = new HashMap<>();
        categoryRegions.put("grass-welcome", "grass");
        categoryRegions.put("desert-welcome", "desert");
        categoryRegions.put("nether-welcome", "nether");
        categoryRegions.put("snow-welcome", "snow");

        for (Map.Entry<String, String> entry : categoryRegions.entrySet()) {
            if (regions.contains(rm.getRegion(entry.getKey()))) {
                return entry.getValue();
            }
        }

        return "prison-other";
    }
    private Set<ProtectedRegion> getApplicableRegions(Player player, RegionManager rm) {
        Location loc = player.getLocation();
        ApplicableRegionSet regionList = rm.getApplicableRegions(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()));
        return regionList.getRegions();
    }
    public Secrets(SkyPrisonCore plugin, DatabaseHook db, Player player, String category) {
        this.db = db;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT id, name, display_item, category, type, position, reward_type, reward, cooldown FROM secrets")) {
            ResultSet rs = ps.executeQuery();
            long currTime = System.currentTimeMillis();
            while(rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                ItemStack displayItem = ItemStack.deserializeBytes(rs.getBytes(3));
                String sCategory = rs.getString(4);
                String type = rs.getString(5);
                int position = rs.getInt(6);
                String rewardType = rs.getString(7);
                int reward = rs.getInt(8);

                long cooldown = rs.getLong(9);
                Component coolText = SecretsUtils.getCooldownText(cooldown, currTime);
                int found = SecretsUtils.getFoundAmount(id, player.getUniqueId().toString());
                if (found > 0) {
                    displayItem.editMeta(meta -> {
                        meta.displayName(MiniMessage.miniMessage().deserialize(name));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("You've " + (type.equals("parkour") ? "done this parkour " : type.equals("puzzle") ?
                                        "completed this puzzle " : "found this secret "), NamedTextColor.GRAY)
                                .append(Component.text(found, NamedTextColor.AQUA)).append(Component.text(" time" + (found > 1 ? "s" : ""), NamedTextColor.GRAY))
                                .decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Cooldown: ", NamedTextColor.RED).append(coolText).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.empty());
                        lore.add(Component.text("Reward: ", NamedTextColor.AQUA).append(Component.text(reward + " " +
                                        WordUtils.capitalize(rewardType.replace("-", " ")), NamedTextColor.GRAY))
                                .decoration(TextDecoration.ITALIC, false));
                        meta.lore(lore);
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

                Secret secret = new Secret(id, name, displayItem, sCategory, type, position, rewardType, reward, cooldown);
                List<Secret> categorySecrets = secrets.getOrDefault(sCategory, new ArrayList<>());
                categorySecrets.add(secret);
                secrets.put(sCategory, categorySecrets);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (category == null || category.isEmpty()) {
            World world = player.getWorld();
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager rm = container.get(BukkitAdapter.adapt(world));

            if (rm == null) {
                category = "all";
            } else {
                Set<ProtectedRegion> regions = getApplicableRegions(player, rm);
                if (regions.isEmpty()) {
                    category = "all";
                } else {
                    category = getCategoryFromRegion(rm, world.getName(), regions);
                }
            }
        }

        this.inventory = plugin.getServer().createInventory(this, 45, Component.text("Secrets", NamedTextColor.GREEN));

        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        redPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        for (int i = 0; i < 45; i++) {
            if(i == 0 || i == 9 || i == 10 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44)
                inventory.setItem(i, redPane);
            else if(i < 9 || i > 36)
                inventory.setItem(i, blackPane);
        }
        changeCategory(category);
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
    public String getCategory() {
        return category;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
