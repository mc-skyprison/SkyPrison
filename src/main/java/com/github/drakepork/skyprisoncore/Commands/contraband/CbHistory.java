package com.github.drakepork.skyprisoncore.Commands.contraband;

import com.github.drakepork.skyprisoncore.Core;
import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class CbHistory implements CommandExecutor {
    private Core plugin;
    @Inject
    public CbHistory(Core plugin) {
        this.plugin = plugin;
    }


    private void retrieveInv(Player sender, String target, String guard) {
        if(plugin.cbGuards.get(guard) != null) {
            HashMap<String, Inventory> cbArchive = plugin.cbGuards.get(guard);
            if(cbArchive.get(target) == null) {
                sender.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + " Error finding Target CB Inventory...");
            }
            Inventory inv = cbArchive.get(target);
            (sender).openInventory(inv);
        } else {
            sender.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + " Error finding Guard CB Archive...");
        }
    }

    private void admin(Player sender, String[] args) {
        if(args.length > 2 || args.length < 1) {
            sender.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.YELLOW + " Use /cbhistory <target> [guard] to open archived inventory of 'target' collected by 'guard'...");
            return;
        }
        String target = args[0].toLowerCase();
        String guard = sender.getName().toLowerCase();
        if(args.length > 1) {
            guard = args[1].toLowerCase();
        }
        retrieveInv(sender, target, guard);
    }

    private void guard(Player sender, String[] args) {
        if(args.length != 1) {
            sender.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.YELLOW + " Use /cbhistory <target> <guard> to open archived inventory of [target] collected by [guard]...");
            return;
        }
        String target = args[0].toLowerCase();
        String guard = sender.getName().toLowerCase();
        retrieveInv(sender, target, guard);
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            if(sender.hasPermission("skyprisoncore.guard.adminguard")||sender.hasPermission("skyprisoncore.guard.seniorguard")) {
                admin((Player)sender, args);
            } else if(sender.hasPermission("skyprisoncore.guard.guard")||sender.hasPermission("skyprisoncore.guard.trialguard")) {
                guard((Player)sender, args);
            } else {
                sender.sendMessage("[" + ChatColor.BLUE + "Contraband" + ChatColor.WHITE + "]: " + ChatColor.RED + " You do not have permission to use this command...");
            }
        }
        return true;
    }
}
