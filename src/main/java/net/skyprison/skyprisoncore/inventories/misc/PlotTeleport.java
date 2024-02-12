package net.skyprison.skyprisoncore.inventories.misc;

import net.alex9849.arm.AdvancedRegionMarket;
import net.alex9849.arm.adapters.WGRegion;
import net.alex9849.arm.exceptions.NoSaveLocationException;
import net.alex9849.arm.regions.Region;
import net.alex9849.arm.regions.SellType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlotTeleport implements CustomInventory {
    private final Inventory inventory;
    private final Player player;
    private final List<Plot> plots = new ArrayList<>();
    private record Plot(Region region, ItemStack itemStack) {}
    public PlotTeleport(Player player) {
        inventory = Bukkit.getServer().createInventory(this, 27, Component.text("Plots Teleport", NamedTextColor.GOLD));
        this.player = player;
        ItemStack whitePane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        whitePane.editMeta(meta -> meta.displayName(Component.empty()));

        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        grayPane.editMeta(meta -> meta.displayName(Component.empty()));

        for (int i = 0; i < 27; i++) {
            if(i >= 17 || i == 9) {
                inventory.setItem(i, grayPane);
            } else if(i <= 8 || i == 10 || i == 16) {
                inventory.setItem(i, whitePane);
            }
        }

        List<Region> plotRegions = AdvancedRegionMarket.getInstance().getRegionManager().getRegionsByOwner(player.getUniqueId())
                .stream().filter(region -> region.getSellType() == SellType.SELL).toList();

        plotRegions.forEach(plot -> {
            ItemStack itemStack = new ItemStack(Material.OAK_SIGN);
            WGRegion region = plot.getRegion();
            itemStack.editMeta(meta -> {
                meta.displayName(Component.text(region.getId(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
                meta.lore(Collections.singletonList(Component.text("Location: ", NamedTextColor.YELLOW)
                        .append(Component.text("X: " + region.getMinPoint().getBlockX() + ", Z: " + region.getMinPoint().getBlockZ(), NamedTextColor.GRAY))
                        .decoration(TextDecoration.ITALIC, false)));
            });
            plots.add(new Plot(plot, itemStack));
        });

        inventory.addItem(plots.stream().map(plot -> plot.itemStack).toArray(ItemStack[]::new));
    }
    public void teleport(ItemStack item) {
        plots.stream().filter(p -> p.itemStack.equals(item)).findFirst().ifPresent(plot -> {
            try {
                plot.region.teleport(player, true);
            } catch (NoSaveLocationException e) {
                player.sendMessage(Component.text("Could not teleport to plot! No safe location found..", NamedTextColor.RED));
            }
        });
    }
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}

