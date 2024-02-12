package net.skyprison.skyprisoncore.inventories.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Daily implements CustomInventory {
    private final Inventory inventory;
    private int currStreak = 0;
    private int highestStreak = 0;
    private int totalCollected = 0;
    private String lastCollected = "";
    public Daily(DatabaseHook db, Player player) {
        inventory = Bukkit.getServer().createInventory(this, 27, Component.text("Daily Reward", TextColor.fromHexString("#0fc3ff")));

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                "SELECT current_streak, highest_streak, total_collected, last_collected FROM dailies WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                currStreak = rs.getInt(1);
                highestStreak = rs.getInt(2);
                totalCollected = rs.getInt(3);
                lastCollected = rs.getString(4);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        pane.editMeta(meta -> meta.displayName(Component.empty()));

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String currDate = formatter.format(date);

        boolean hasCollected = !lastCollected.isEmpty() && currDate.equalsIgnoreCase(lastCollected);

        ItemStack[] contents = new ItemStack[inventory.getSize()];
        Arrays.fill(contents, pane);
        inventory.setContents(contents);

        int finalCurrStreak = currStreak;
        int finalHighestStreak = highestStreak;

        ItemStack dReward = hasCollected ? new ItemStack(Material.MINECART) : new ItemStack(Material.CHEST_MINECART);
        dReward.editMeta(meta -> {
            meta.displayName(Component.text("Daily Reward", NamedTextColor.YELLOW, TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            ArrayList<Component> lore = new ArrayList<>();
            if (hasCollected) {
                lore.add(Component.text("You've already collected today!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("Click here to collect your reward!", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            }
            lore.add(Component.empty());
            lore.add(Component.text("Current Streak: ", NamedTextColor.GRAY).append(Component.text(finalCurrStreak, NamedTextColor.WHITE, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Highest Streak: ", NamedTextColor.GRAY).append(Component.text(finalHighestStreak, NamedTextColor.WHITE, TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);
        });


        inventory.setItem(13, dReward);
    }
    public int getCurrStreak() {
        return currStreak;
    }
    public int getHighestStreak() {
        return highestStreak;
    }
    public int getTotalCollected() {
        return totalCollected;
    }
    public String getLastCollected() {
        return lastCollected;
    }
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
