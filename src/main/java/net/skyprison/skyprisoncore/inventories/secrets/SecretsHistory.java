package net.skyprison.skyprisoncore.inventories.secrets;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.secrets.Secret;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SecretsHistory implements CustomInventory {
    private final List<Secret> secrets = new ArrayList<>();
    private final Inventory inventory;
    private int page = 1;
    private int totalPages;
    private boolean sort = true;
    private final ItemStack nextPage;
    private final ItemStack prevPage;
    private final ItemStack blackPane;
    private final ItemStack categoryItem = new ItemStack(Material.WRITABLE_BOOK);
    private final ItemStack typeItem = new ItemStack(Material.COMPARATOR);
    private final List<String> categories = new ArrayList<>();
    private final List<String> types = Arrays.asList("all", "secret", "parkour", "puzzle");
    private int catPos = 0;
    private int typePos = 0;

    public void updateSort() {
        this.sort = !this.sort;
        Collections.reverse(secrets);
        ItemStack sortItem = new ItemStack(Material.CLOCK);
        sortItem.editMeta(meta -> {
            meta.displayName(Component.text("Sort Transactions", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
            ArrayList<Component> lore = new ArrayList<>();
            lore.add(Component.text("Current Sort: ", NamedTextColor.GOLD)
                    .append(Component.text(sort ? "Oldest -> Newest" : "Newest -> Oldest", NamedTextColor.YELLOW, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });
        inventory.setItem(49, sortItem);
        updatePage(0);
    }
    public void updateCategory(Boolean direction) {
        if(direction != null) catPos = direction ? (catPos + 1) % categories.size() : (catPos - 1 + categories.size()) % categories.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        categoryItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            categories.forEach(category -> lore.add(Component.text(WordUtils.capitalize(category).replace("-", " "),
                    getCategory().equalsIgnoreCase(category) ? selectedColor : color).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        updatePage(0);
    }
    public void updateType(Boolean direction) {
        if(direction != null) typePos = direction ? (typePos + 1) % types.size() : (typePos - 1 + types.size()) % types.size();
        TextColor color = NamedTextColor.GRAY;
        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
        typeItem.editMeta(meta -> {
            List<Component> lore = new ArrayList<>();
            types.forEach(type -> lore.add(Component.text(WordUtils.capitalize(type).replace("-", " "),
                    getCategory().equalsIgnoreCase(type) ? selectedColor : color).decoration(TextDecoration.ITALIC, false)));
            meta.lore(lore);
        });
        updatePage(0);
    }
    public void updatePage(int page) {
        List<Secret> secretsToShow = new ArrayList<>(secrets);
        secretsToShow = secretsToShow.stream().filter(secret -> (getCategory().equalsIgnoreCase("all") || secret.category().equalsIgnoreCase(getCategory())) &&
                (getType().equalsIgnoreCase("all") || secret.type().equalsIgnoreCase(getType()))).toList();
        totalPages = (int) Math.ceil((double) secretsToShow.size() / 45);

        this.page += page;
        if(this.page > totalPages) {
            this.page = 1;
        }
        for(int i = 0; i < 45; i++) inventory.setItem(i, null);
        inventory.setItem(45, this.page == 1 ? blackPane : prevPage);
        inventory.setItem(53, totalPages < 2 || this.page == totalPages ? blackPane : nextPage);
        int toRemove = 45 * (this.page - 1);
        if(toRemove != 0) {
            secretsToShow = secretsToShow.subList(toRemove, secretsToShow.size());
        }
        Iterator<Secret>  secretsIterator = secretsToShow.iterator();
        for(int i = 0; i < 45; i++) {
            if(secretsIterator.hasNext()) {
                inventory.setItem(i, secretsIterator.next().displayItem());
            }
        }
    }

    public SecretsHistory(SkyPrisonCore plugin, DatabaseHook db, UUID pUUID) {
        categories.add("all");
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT name FROM secrets_categories")) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                categories.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Secrets History", NamedTextColor.RED));

        blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        blackPane.editMeta(meta -> meta.displayName(Component.text(" ")));
        nextPage = new ItemStack(Material.PAPER);
        nextPage.editMeta(meta -> meta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));
        prevPage = new ItemStack(Material.PAPER);
        prevPage.editMeta(meta -> meta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)));

        categoryItem.editMeta(meta -> meta.displayName(Component.text("Toggle Category", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)));
        typeItem.editMeta(meta -> meta.displayName(Component.text("Toggle Type", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false)));

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT s.id, s.name, s.category, s.type, s.reward_type, s.reward, s.cooldown, su.collect_time FROM secrets_userdata su " +
                     "JOIN secrets s ON s.id = su.secret_id WHERE su.user_id = ? ORDER BY su.collect_time ASC")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFor = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            while (rs.next()) {
                int id = rs.getInt("id");
                String secretName = rs.getString("name");
                String secretCategory = rs.getString("category");
                String type = rs.getString("type");
                String rewardType = rs.getString("reward_type");
                int reward = rs.getInt("reward");
                String cooldown = rs.getString("cooldown");
                Date date = new Date(rs.getLong("collect_time"));
                String itemName = dateFor.format(date);
                ItemStack item = new ItemStack(Material.BAMBOO_SIGN);
                item.editMeta(meta -> {
                    meta.displayName(Component.text(itemName, NamedTextColor.GOLD, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                    ArrayList<Component> lore = new ArrayList<>();
                    lore.add(Component.text("Secret: ", NamedTextColor.GRAY).append(Component.text(secretName, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text(type, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                    lore.add(Component.text("Reward: ", NamedTextColor.GRAY).append(Component.text(reward + " " + rewardType, NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false));
                    meta.lore(lore);
                });
                secrets.add(new Secret(id, itemName, item, secretCategory, type, rewardType, reward, cooldown));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        updateSort();
        updateCategory(null);
        updateType(null);
        updatePage(0);
    }

    public String getCategory() {
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
        return this.page;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}

