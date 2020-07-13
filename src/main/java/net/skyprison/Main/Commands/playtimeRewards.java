package net.skyprison.Main.Commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.Statistics.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class playtimeRewards implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
					.getDataFolder() + "/playtimeRewards.yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileConfiguration file = YamlConfiguration.loadConfiguration(f);
			Set<String> players = file.getKeys(false);

			Player player = (Player) sender;
			CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
			if(!players.contains(user.getUniqueId().toString())) {
				file.set(user.getUniqueId().toString() + ".time", System.currentTimeMillis());
				try {
					file.save(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				long timeLeft = System.currentTimeMillis() - file.getLong(user.getUniqueId().toString() + ".time");
				long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
				user.sendMessage(String.valueOf(timeLeft));

				Date date = new Date();
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
				dateFormat.format(date);
				System.out.println(dateFormat.format(date));

				try {
					if(dateFormat.parse(dateFormat.format(date)).after(dateFormat.parse("21:12"))) {
						user.sendMessage("Time is past");
					}else{
						user.sendMessage("time is before");
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

		}
		return true;
	}
}
