package net.skyprison.skyprisoncore.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemLore {
    private final SkyPrisonCore plugin;
    public ItemLore(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }
    private void deleteLoreLine(Player player, ItemStack heldItem, int line) {
        ItemStack currHeldItem = player.getInventory().getItemInMainHand();
        if(currHeldItem.equals(heldItem)) {
            player.sendMessage(Component.text("Item in hand has changed! Cancelling..", NamedTextColor.RED));
            return;
        }
        player.getInventory().getItemInMainHand().editMeta(meta -> {
            List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
            lore.remove(line - 1);
            meta.lore(lore);
        });
        displayLore(player);
    }
    private void moveLoreLine(Player player, ItemStack heldItem, int position, boolean moveUp) {
        ItemStack currHeldItem = player.getInventory().getItemInMainHand();
        if (currHeldItem.equals(heldItem)) {
            player.sendMessage(Component.text("Item in hand has changed! Cancelling..", NamedTextColor.RED));
            return;
        }
        int actualPosition = position - 1;
        int newPos = moveUp ? actualPosition - 1 : actualPosition + 1;
        player.getInventory().getItemInMainHand().editMeta(meta -> {
            List<Component> lore = Objects.requireNonNullElse(meta.lore(), new ArrayList<>());
            if(newPos >= 0 && newPos < lore.size()) {
                Component lore1 = lore.get(newPos);
                Component lore2 = lore.get(actualPosition);
                lore.set(actualPosition, lore1);
                lore.set(newPos, lore2);
                meta.lore(lore);
            }
        });
        displayLore(player);
    }
    private void editLoreLine(Player player, ItemStack heldItem, int line) {
        ItemStack currHeldItem = player.getInventory().getItemInMainHand();
        if (!currHeldItem.equals(heldItem)) {
            player.sendMessage(Component.text("Item in hand has changed! Cancelling..", NamedTextColor.RED));
            return;
        }
        List<Object> loreInfo = new ArrayList<>();
        loreInfo.add("edit-lore");
        loreInfo.add(heldItem);
        loreInfo.add(line);
        String loreLine = MiniMessage.miniMessage().serialize(Objects.requireNonNull(currHeldItem.lore()).get(line - 1));
        if(loreLine.startsWith("<!italic>")) {
            loreLine = loreLine.substring(9);
        }
        plugin.chatLock.put(player.getUniqueId(), loreInfo);
        player.sendMessage(Component.text("Type new lore line in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(Component.text("Click to paste current line to chat", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.suggestCommand(loreLine)));
    }
    private void newLoreLine(Player player, ItemStack heldItem) {
        ItemStack currHeldItem = player.getInventory().getItemInMainHand();
        if (!currHeldItem.equals(heldItem)) {
            player.sendMessage(Component.text("Item in hand has changed! Cancelling..", NamedTextColor.RED));
            return;
        }
        List<Object> loreInfo = new ArrayList<>();
        loreInfo.add("new-lore");
        loreInfo.add(heldItem);
        plugin.chatLock.put(player.getUniqueId(), loreInfo);
        player.sendMessage(Component.text("Type new lore line in chat: (Type 'cancel' to cancel)", NamedTextColor.YELLOW));
    }
    public void displayLore(Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!heldItem.getType().isItem()) {
            player.sendMessage(Component.text("You're not holding an item!", NamedTextColor.RED));
            return;
        }
        Component solidLine = Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH);
        Component prefix = solidLine.append(Component.text(" Item Lore ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD)).append(solidLine);

        ItemMeta heldMeta = heldItem.getItemMeta();
        Component msg = prefix;

        if (heldMeta.hasLore()) {
            List<Component> lore = heldMeta.lore();
            if(lore == null) return;

            int i = 1;
            for (Component loreLine : lore) {
                int pos = i;
                msg = msg.appendNewline().append(Component.text("[X]", NamedTextColor.RED)
                                .hoverEvent(HoverEvent.showText(Component.text("Delete line ", NamedTextColor.RED)
                                        .append(Component.text( pos + ".", NamedTextColor.RED, TextDecoration.BOLD))))
                                .clickEvent(ClickEvent.callback(audience -> deleteLoreLine(player, heldItem, pos))))

                        .appendSpace().append(Component.text(pos + ". ", NamedTextColor.GRAY))

                        .append(Component.text("↑", NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(Component.text("Move Up", NamedTextColor.YELLOW)))
                                .clickEvent(ClickEvent.callback(audience -> moveLoreLine(player, heldItem, pos, true)))).appendSpace()

                        .append(Component.text("↓", NamedTextColor.GOLD).hoverEvent(HoverEvent.showText(Component.text("Move Down", NamedTextColor.YELLOW)))
                                .clickEvent(ClickEvent.callback(audience -> moveLoreLine(player, heldItem, pos, false)))).appendSpace()

                        .appendSpace().append(loreLine.hoverEvent(HoverEvent.showText(Component.text("Edit line ", NamedTextColor.RED)
                                        .append(Component.text( pos + ".", NamedTextColor.RED, TextDecoration.BOLD))))
                                .clickEvent(ClickEvent.callback(audience -> editLoreLine(player, heldItem, pos))));
                i++;
            }
        }

        msg = msg.appendNewline().append(Component.text("[+]", NamedTextColor.DARK_GREEN).hoverEvent(HoverEvent.showText(Component.text("Add a new line", NamedTextColor.RED)))
                .clickEvent(ClickEvent.callback(audience -> newLoreLine(player, heldItem))));

        player.sendMessage(msg);
    }
}
