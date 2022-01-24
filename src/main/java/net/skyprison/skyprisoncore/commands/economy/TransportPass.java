package net.skyprison.skyprisoncore.commands.economy;

import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransportPass implements CommandExecutor {
    private final SkyPrisonCore plugin;

    @Inject
    public TransportPass(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
/*		File f = new File(plugin.getDataFolder() + File.separator + "public-transport.yml");
		FileConfiguration dailyConf = YamlConfiguration.loadConfiguration(f);
		Inventory dailyGUI = Bukkit.createInventory(null, 45, ChatColor.RED + "Transport Passes");
		ItemStack pane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta paneMeta = pane.getItemMeta();
		paneMeta.setDisplayName(" ");
		pane.setItemMeta(paneMeta);


		if(dailyConf.isConfigurationSection(player.getUniqueId().toString())) {

		} else {

		}
		for (int i = 0; i < 27; i++) {
			switch(i) {
				case 0:
					ItemStack startPane = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
					ItemMeta startMeta = startPane.getItemMeta();
					startMeta.setDisplayName(" ");
					NamespacedKey key = new NamespacedKey(plugin, "stop-click");
					startMeta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);
					NamespacedKey key1 = new NamespacedKey(plugin, "gui-type");
					startMeta.getPersistentDataContainer().set(key1, PersistentDataType.STRING, "daily-reward");
					startPane.setItemMeta(startMeta);
					dailyGUI.setItem(i, startPane);
					break;
				case 10:
					dailyGUI.setItem(i, day1);
					break;
				case 11:
					dailyGUI.setItem(i, day2);
					break;
				case 12:
					dailyGUI.setItem(i, day3);
					break;
				case 13:
					dailyGUI.setItem(i, day4);
					break;
				case 14:
					dailyGUI.setItem(i, day5);
					break;
				case 15:
					dailyGUI.setItem(i, day6);
					break;
				case 16:
					dailyGUI.setItem(i, day7);
					break;
				default:
					dailyGUI.setItem(i, pane);
					break;
			}
		}
		player.openInventory(dailyGUI);*/
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            openGUI((Player) sender);
        }
        return true;
    }
}
