package net.skyprison.Main.Commands;

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
import java.io.IOException;
import java.util.*;

public class Bounty implements CommandExecutor {
	public void openGUI(Player player, int page) {
		File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
				.getDataFolder() + "/bounties.yml");
		FileConfiguration bounty = YamlConfiguration.loadConfiguration(f);
		Set<String> bountyList = bounty.getKeys(false);
		ArrayList<String> arr = new ArrayList();
		for(String bountyPlayer : bountyList) {
			if(bounty.getInt(bountyPlayer + ".page") == page) {
				arr.add(bountyPlayer);
			}
		}
		Inventory bounties = Bukkit.createInventory(null, 54, ChatColor.RED + "Bounties");
		int i = 0;
		for (String bountyPlayer : arr) {
			ArrayList lore = new ArrayList();
			ItemStack head = new ItemStack(Material.PLAYER_HEAD);
			SkullMeta meta = (SkullMeta) head.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(bountyPlayer)));
			meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + Bukkit.getOfflinePlayer(UUID.fromString(bountyPlayer)).getName());
			lore.add(ChatColor.YELLOW + "Prize: " + bounty.getInt(bountyPlayer + ".bounty-prize"));
			meta.setLore(lore);
			head.setItemMeta(meta);
			bounties.setItem(i, head);
			i++;
		}
		player.openInventory(bounties);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			File f = new File(Bukkit.getServer().getPluginManager().getPlugin("SkyPrisonCore")
					.getDataFolder() + "/bounties.yml");
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileConfiguration bounty = YamlConfiguration.loadConfiguration(f);
			Set<String> bountyList = bounty.getKeys(false);
			//  /bounty set <player> <prize>
			if(args.length < 1) {
				player.sendMessage(ChatColor.WHITE + "----====" + ChatColor.RED + " Bounties " + ChatColor.WHITE + "====----" + ChatColor.YELLOW + "\n/bounty set <player> <amount> " + ChatColor.WHITE + "- Set a bounty on a player" + ChatColor.YELLOW + "\n/bounty help " + ChatColor.WHITE + "- Shows this" + ChatColor.YELLOW + "\n/bounty list " + ChatColor.WHITE + "- Shows all players with bounties");
			}else if(args[0].equalsIgnoreCase("set")) {
				if(Bukkit.getPlayer(args[1]) != null) {
					String bountyTarget = Bukkit.getPlayer(args[1]).getUniqueId().toString();
					if(!player.equals(Bukkit.getPlayer(args[1]))) {
						if (!Bukkit.getPlayer(args[1]).hasPermission("skyprisoncore.bounty.bypass")) {
							if (bountyList.contains(bountyTarget)) {
								ArrayList arr = (ArrayList) bounty.getList(bountyTarget + ".bounty-contributors");
								if (!arr.contains(player.getName())) {
									arr.add(player.getName());
									bounty.set(bountyTarget + ".bounty-contributors", arr);
								}
								bounty.set(bountyTarget + ".bounty-prize", bounty.getInt(bountyTarget + ".bounty-prize") + Integer.parseInt(args[2]));
								try {
									bounty.save(f);
									for (Player online : Bukkit.getServer().getOnlinePlayers()) {
										online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "]" + ChatColor.YELLOW + " " + player.getName() + " has increased the bounty on " + Bukkit.getPlayer(args[1]).getName() + " by " + ChatColor.GREEN + "$" + args[2] + "!");
									}
									player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your bounty has been put on " + Bukkit.getPlayer(args[1]).getName());
								} catch (final IOException e) {
									e.printStackTrace();
								}
							} else if (!bountyList.contains(bountyTarget)) {
								int page = 0;
								for (int i = 0; i < bountyList.size(); ) {
									ArrayList arr = new ArrayList();
									for (String bountyPlayer : bountyList) {
										if (bounty.getInt(bountyPlayer + ".page") == i) {
											arr.add(bountyPlayer);
										}
									}
									if (arr.size() <= 45) {
										page = i;
										break;
									} else {
										i++;
										continue;
									}
								}
								bounty.set(bountyTarget + ".bounty-prize", Integer.parseInt(args[2]));
								bounty.set(bountyTarget + ".page", page);
								bounty.set(bountyTarget + ".bounty-contributors", new ArrayList(Collections.singleton(player.getUniqueId().toString())));
								try {
									bounty.save(f);
									for (Player online : Bukkit.getServer().getOnlinePlayers()) {
										online.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "]" + ChatColor.YELLOW + " " + player.getName() + " has put a " + ChatColor.GREEN + "$" + args[2] + ChatColor.YELLOW + " bounty on " + Bukkit.getPlayer(args[1]).getName() + "!");
									}
									player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.YELLOW + "Your bounty has been put on " + Bukkit.getPlayer(args[1]).getName());
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}
						} else {
							player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.RED + "You can't put a bounty on this player!");
						}
					} else {
						player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.RED + "You can't put a bounty on yourself!");
					}
				} else {
					player.sendMessage(ChatColor.WHITE + "[" + ChatColor.RED + "Bounties" + ChatColor.WHITE + "] " + ChatColor.RED + "Player is not online or doesn't exist..");
				}
			} else if(args[0].equalsIgnoreCase("list")) {
				openGUI(player, 0);
			} else if(args[0].equalsIgnoreCase("help")) {
				player.sendMessage(ChatColor.WHITE + "----====" + ChatColor.RED + " Bounties " + ChatColor.WHITE + "====----" + ChatColor.YELLOW + "\n/bounty set <player> <amount> " + ChatColor.WHITE + "- Set a bounty on a player" + ChatColor.YELLOW + "\n/bounty help " + ChatColor.WHITE + "- Shows this" + ChatColor.YELLOW + "\n/bounty list " + ChatColor.WHITE + "- Shows all players with bounties");
			}
		}
		return true;
	}
}

