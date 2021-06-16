package net.skyprison.skyprisoncore.Commands.referral;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReferralList implements CommandExecutor {
	private SkyPrisonCore plugin;
	@Inject
	public ReferralList(SkyPrisonCore plugin) {
		this.plugin = plugin;
	}


	public void openGUI(Player player, int page) {
		File f = new File(plugin.getDataFolder() +  File.separator + "referrals.yml");
		FileConfiguration refer = YamlConfiguration.loadConfiguration(f);
		List<String> reffedBy = refer.getStringList(player.getUniqueId().toString() + ".reffedBy");
		Inventory referred = Bukkit.createInventory(null, 54, ChatColor.RED + "Referral List");
		int i = 0;
		for (String reffedPlayer : reffedBy) {
			String[] reffed = reffedPlayer.split(":");
			ArrayList lore = new ArrayList();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(reffed[0])));
			meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Bukkit.getOfflinePlayer(UUID.fromString(reffed[0])).getName());

			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(Long.parseLong(reffed[1]));
			lore.add(ChatColor.YELLOW + "Referred you on: " + df.format(calendar.getTime()));
			meta.setLore(lore);
			head.setItemMeta(meta);
			referred.setItem(i, head);
			i++;
		}
		ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		for (int b = 45; b < 54; b++) {
			referred.setItem(b, pane);
		}
		player.openInventory(referred);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			File f = new File(plugin.getDataFolder() +  File.separator + "referrals.yml");
			FileConfiguration refer = YamlConfiguration.loadConfiguration(f);
			if(refer.isConfigurationSection(player.getUniqueId().toString())) {
					openGUI(player, 0);
			} else {
				player.sendMessage(plugin.colourMessage("&cNo one has referred you yet!"));
			}
		} else {
			plugin.tellConsole("This command can only be run in game!");
		}
		return true;
	}
}
