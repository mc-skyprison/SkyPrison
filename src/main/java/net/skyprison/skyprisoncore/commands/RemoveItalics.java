package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class RemoveItalics implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public RemoveItalics(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	private final HashMap<UUID, ItemStack> confirmItalics = new HashMap<>();

	public void doTheThing(Player player, ItemStack item) {
		if(player.getInventory().contains(item)) {
			TextComponent newName = Component.text(item.getDisplayName()).decoration(TextDecoration.ITALIC, false);
			ItemMeta iMeta = item.getItemMeta();
			iMeta.displayName(newName);
			item.setItemMeta(iMeta);
			plugin.asConsole("cmi money take " + player.getName() + " " + 50000);
			player.sendMessage(Component.text("Successfully removed italics from item name!", NamedTextColor.GREEN));
		} else {
			player.sendMessage(Component.text("Item no longer in your inventory! Cancelling...", NamedTextColor.RED));
		}
		confirmItalics.remove(player.getUniqueId());
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player player) {
			if(args.length == 0) {
				if(confirmItalics.containsKey(player.getUniqueId())) {
					player.sendMessage(Component.text("You're already removing italics from an item!", NamedTextColor.RED));
				} else {
					CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
					ItemStack item = player.getInventory().getItemInMainHand();
					ItemMeta iMeta = item.getItemMeta();
					if (user.getBalance() >= 50000) {
						if (iMeta.hasDisplayName()) {
							if (!Objects.requireNonNull(iMeta.displayName()).hasDecoration(TextDecoration.ITALIC)) {
								confirmItalics.put(player.getUniqueId(), item);
								Component confirm = Component.text("Click here to confirm italics removal", NamedTextColor.YELLOW, TextDecoration.BOLD)
										.clickEvent(ClickEvent.runCommand("/removeitalics confirm"));
								player.sendMessage(confirm);
							} else {
								player.sendMessage(Component.text("Italics has already been removed from this item!", NamedTextColor.RED));
							}
						} else {
							player.sendMessage(Component.text("This item doesnt have a custom name!", NamedTextColor.RED));
						}
					} else {
						player.sendMessage(Component.text("You need $50,000 to use this!", NamedTextColor.RED));
					}
				}
			} else if(args[0].equalsIgnoreCase("confirm")) {
				if(confirmItalics.containsKey(player.getUniqueId())) {
					doTheThing(player, confirmItalics.get(player.getUniqueId()));
				} else {
					player.sendMessage(Component.text("You don't have a pending italics removal!", NamedTextColor.RED));
				}
			} else {
				player.sendMessage(Component.text("Correct Usage: /removeitalics", NamedTextColor.RED));
			}
		}
		return true;
	}
}
