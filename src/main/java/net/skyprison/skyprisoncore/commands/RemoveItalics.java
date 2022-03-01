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

import java.util.HashMap;
import java.util.UUID;

public class RemoveItalics implements CommandExecutor {
	private final SkyPrisonCore plugin;

	public RemoveItalics(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}

	private HashMap<UUID, ItemStack> confirmItalics = new HashMap<>();

	public void doTheThing(Player player, ItemStack item) {
		if(player.getInventory().contains(item)) {
			TextComponent newName = Component.text(item.getDisplayName()).decoration(TextDecoration.ITALIC, false);
			ItemMeta iMeta = item.getItemMeta();
			iMeta.displayName(newName);
			item.setItemMeta(iMeta);
			plugin.asConsole("money take " + player.getName() + " " + 50000);
			player.sendMessage(plugin.colourMessage("&aSuccessfully removed italics from item name!"));
		} else {
			player.sendMessage(plugin.colourMessage("&cItem no longer in your inventory! Cancelling..."));
		}
		confirmItalics.remove(player.getUniqueId());
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length == 0) {
				if(confirmItalics.containsKey(player.getUniqueId())) {
					player.sendMessage(plugin.colourMessage("&cYou're already removing italics from an item!"));
				} else {
					CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
					ItemStack item = player.getInventory().getItemInMainHand();
					if (item.hasDisplayName()) {
						if (user.getBalance() >= 50000) {
							ItemMeta iMeta = item.getItemMeta();
							if (!iMeta.displayName().hasDecoration(TextDecoration.ITALIC)) {
								confirmItalics.put(player.getUniqueId(), item);
								Component confirm = Component.text("Click here to confirm italics removal")
										.color(NamedTextColor.YELLOW)
										.decorate(TextDecoration.BOLD)
										.clickEvent(ClickEvent.runCommand("/removeitalics confirm"))
										.hoverEvent(Component.text(plugin.colourMessage("&eClick me!")));
								player.sendMessage(confirm);
							} else {
								player.sendMessage(plugin.colourMessage("&cItalics has already been removed from this item!"));
							}
						} else {
							player.sendMessage(plugin.colourMessage("&cYou need $50,000 to use this!"));
						}
					} else {
						player.sendMessage(plugin.colourMessage("&cThis item doesnt have a custom name!"));
					}
				}
			} else if(args[0].equalsIgnoreCase("confirm")) {
				if(confirmItalics.containsKey(player.getUniqueId())) {
					doTheThing(player, confirmItalics.get(player.getUniqueId()));
				} else {
					player.sendMessage(plugin.colourMessage("&cYou don't have a pending italics removal!"));
				}
			} else {
				player.sendMessage(plugin.colourMessage("&cCorrect Usage: /removeitalics"));
			}
		}
		return true;
	}
}
