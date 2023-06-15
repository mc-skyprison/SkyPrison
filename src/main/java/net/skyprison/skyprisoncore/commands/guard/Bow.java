package net.skyprison.skyprisoncore.commands.guard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Bow implements CommandExecutor {
	public Bow() {}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof Player guard) {
			if(args.length == 1) {
				Player target = Bukkit.getPlayer(args[0]);
				if(target != null) {
					if(!guard.equals(target)) {
						ArrayList<String> contrabands = new ArrayList<>();
						contrabands.add("BOW");
						contrabands.add("CROSSBOW");
						boolean containsCB = false;
						for (String contraband : contrabands) {
							Material cb = Material.getMaterial(contraband);
							if (target.getInventory().contains(Objects.requireNonNull(cb))) {
								containsCB = true;
								Timer t = new Timer();
								t.scheduleAtFixedRate(new TimerTask() {
									int i = 0;
									@Override
									public void run() {
										int timeLeft = 5 - i;
										guard.sendMessage(Component.text("[", NamedTextColor.WHITE).append(Component.text("Contraband", TextColor.fromHexString("#564387")))
												.append(Component.text("]", NamedTextColor.WHITE)).append(Component.text("They have ", TextColor.fromHexString("#4dabdd")))
												.append(Component.text(timeLeft, NamedTextColor.YELLOW, TextDecoration.BOLD)).append(Component.text(" seconds to hand over their bow!",TextColor.fromHexString("#4dabdd"))));

										target.sendMessage(Component.text("[", NamedTextColor.WHITE).append(Component.text("Contraband", TextColor.fromHexString("#564387")))
												.append(Component.text("]", NamedTextColor.WHITE)).append(Component.text("You have ", TextColor.fromHexString("#4dabdd")))
												.append(Component.text(timeLeft, NamedTextColor.YELLOW, TextDecoration.BOLD)).append(Component.text(" seconds to hand over your bow!",TextColor.fromHexString("#4dabdd"))));
										if(i == 5)
											t.cancel();
										i++;
									}
								}, 0, 1000);
								break;
							}
						}
						if(!containsCB) {
							guard.sendMessage(Component.text("Player doesnt have any bows!", NamedTextColor.RED));
						}
					} else {
						guard.sendMessage(Component.text("You can't /bow yourself!", NamedTextColor.RED));
					}
				} else {
					guard.sendMessage(Component.text("/bow <player>", NamedTextColor.RED));
				}
			} else {
				guard.sendMessage(Component.text("/bow <player>", NamedTextColor.RED));
			}
		}
		return true;
	}
}

