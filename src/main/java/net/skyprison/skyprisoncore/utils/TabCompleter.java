package net.skyprison.skyprisoncore.utils;

import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    private final SkyPrisonCore plugin;

    @Inject public TabCompleter(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            ArrayList<String> options = new ArrayList<>();
            ArrayList<String> commands = new ArrayList<>();
            if (command.getName().equalsIgnoreCase("tokens") || command.getName().equalsIgnoreCase("token")) {
                if (args.length == 1) {
                    commands.add("balance");
                    commands.add("shop");
                    commands.add("top");;
                    commands.add("help");
                    if(player.hasPermission("skyprisoncore.command.tokens.admin")) {
                        commands.add("add");
                        commands.add("set");
                        commands.add("remove");
                        commands.add("giveall");
                    }
                    StringUtil.copyPartialMatches(args[0], commands, options);
                } else if (args.length == 2) {
                    switch (args[0].toLowerCase()) {
                        case "balance":
                        case "add":
                        case "remove":
                        case "set":
                            for(Player oPlayer : Bukkit.getOnlinePlayers()) {
                                commands.add(oPlayer.getName());
                            }
                            break;
                    }
                    StringUtil.copyPartialMatches(args[1], commands, options);
                }
            }
            Collections.sort(options);
            return options;
        }
        return null;
    }
}

