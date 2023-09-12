package net.skyprison.skyprisoncore.inventories.claims;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClickBehavior;
import net.skyprison.skyprisoncore.inventories.CustomInventory;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ClaimPending implements CustomInventory {
    private final Inventory inventory;
    private final String category;
    private final HashMap<String, HashMap<String, Object>> claimIds;
    private final int page;
    public ClaimPending(SkyPrisonCore plugin, HashMap<String, HashMap<String, Object>> claimIds, String category, int page) {
        this.claimIds = claimIds;
        this.category = category;
        this.page = page;

        this.inventory = plugin.getServer().createInventory(this, 54, Component.text("Pending Claim Descisions", TextColor.fromHexString("#0fc3ff")));
        ItemStack redPane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta redMeta = redPane.getItemMeta();
        redMeta.displayName(Component.text(" "));
        redPane.setItemMeta(redMeta);

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        blackMeta.displayName(Component.text(" "));
        blackPane.setItemMeta(blackMeta);

        ItemStack nextPage = new ItemStack(Material.PAPER);
        ItemMeta nextMeta = nextPage.getItemMeta();
        nextMeta.displayName(Component.text("Next Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        nextPage.setItemMeta(nextMeta);
        ItemStack prevPage = new ItemStack(Material.PAPER);
        ItemMeta prevMeta = prevPage.getItemMeta();
        prevMeta.displayName(Component.text("Previous Page", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        prevPage.setItemMeta(prevMeta);


        HashMap<Integer, List<String>> pending = NotificationsUtils.getNotificationsFromExtra(claimIds.keySet().stream().toList());

        int totalPages = (int) Math.ceil((double) pending.size() / 28);

        if(page > totalPages) {
            page = 1;
        }

        List<Integer> pendingToShow = new ArrayList<>(pending.keySet());
        if(!category.isEmpty()) pendingToShow.removeIf(notif -> !pending.get(notif).get(0).equalsIgnoreCase(category));

        int toRemove = 28 * (page - 1);
        if(toRemove != 0) {
            pendingToShow = pendingToShow.subList(toRemove, pendingToShow.size());
        }
        Iterator<Integer> pendingNotifs = pendingToShow.iterator();
        int finalPage = page;
        new BukkitRunnable() {
            @Override
            public void run() {
                for(int i = 0; i < inventory.getSize();i++) {
                    if (i == 47 && finalPage != 1) {
                        inventory.setItem(i, prevPage);
                    } else if (i == 51 && totalPages > 1  && finalPage != totalPages) {
                        inventory.setItem(i, nextPage);
                    } else if (i == 49) {
                        ItemStack itemSort = new ItemStack(Material.WRITABLE_BOOK);
                        ItemMeta sortMeta = itemSort.getItemMeta();
                        TextColor color = NamedTextColor.GRAY;
                        TextColor selectedColor = TextColor.fromHexString("#0fffc3");
                        sortMeta.displayName(Component.text("Toggle Type", TextColor.fromHexString("#20df80")).decoration(TextDecoration.ITALIC, false));
                        List<Component> lore = new ArrayList<>();
                        lore.add(Component.text("All Pending", category.equalsIgnoreCase("") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Invites", category.equalsIgnoreCase("claim-invite") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                        lore.add(Component.text("Transfers", category.equalsIgnoreCase("claim-transfer") ? selectedColor : color).decoration(TextDecoration.ITALIC, false));
                        sortMeta.lore(lore);
                        itemSort.setItemMeta(sortMeta);
                        inventory.setItem(i, itemSort);
                    } else if (i == 0 || i == 8 || i == 9 || i == 17 || i == 18 || i == 26 || i == 27 || i == 35 || i == 36 || i == 44 || i == 45 || i == 53) {
                        inventory.setItem(i, redPane);
                    } else if (i < 8 || i > 45 && i < 53) {
                        inventory.setItem(i, blackPane);
                    } else {
                        if (pendingNotifs.hasNext()) {
                            int notif = pendingNotifs.next();
                            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(UUID.fromString(pending.get(notif).get(2)));
                            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                            SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
                            itemMeta.setOwningPlayer(oPlayer);
                            itemMeta.displayName(Component.text(Objects.requireNonNull(oPlayer.getName()), TextColor.fromHexString("#0fffc3"), TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
                            List<Component> lore = new ArrayList<>();
                            lore.add(Component.text((pending.get(notif).get(0).equalsIgnoreCase("claim-transfer") ? "Pending transfer for " : "Pending invite to ") +
                                    claimIds.get(pending.get(notif).get(1)).get("name").toString(), TextColor.fromHexString("#ffba75")).decoration(TextDecoration.ITALIC, false));
                            itemMeta.lore(lore);
                            item.setItemMeta(itemMeta);
                            inventory.setItem(i, item);
                        }
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public String getNextCategory(String category) {
        String nextCat = "";
        switch (category) {
            case "" -> nextCat = "claim-invite";
            case "claim-invite" -> nextCat = "claim-transfer";
            case "claim-transfer" -> nextCat = "";
        }
        return nextCat;
    }
    public HashMap<String, HashMap<String, Object>> getClaimIds() {
        return this.claimIds;
    }

    public String getCategory() {
        return this.category;
    }

    @Override
    public ClickBehavior defaultClickBehavior() {
        return ClickBehavior.DISABLE_ALL;
    }

    @Override
    public List<Object> customClickList() {
        return null;
    }

    public int getPage() {
        return this.page;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
