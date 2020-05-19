package net.skyprison.Main.Commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Cheese implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore").getDataFolder() + "/cheese.txt");
		ArrayList arr = new ArrayList();
		try {
			Scanner file = new Scanner(f);
			while(file.hasNextLine()) {
				arr.add(file.nextLine());
			}
			Random randomGen = new Random();
			int i = randomGen.nextInt(arr.size());
			sender.sendMessage("Here you have a cheese: " + arr.get(i));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(sender.equals(Bukkit.getPlayer("DrakePork"))) {
			CMIUser user = CMI.getInstance().getPlayerManager().getUser((OfflinePlayer) sender);
			user.sendMessage(user.getTotalPlayTime() + " ");
		}
		return true;
	}
}
