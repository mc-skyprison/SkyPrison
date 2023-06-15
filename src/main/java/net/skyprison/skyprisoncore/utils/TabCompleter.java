package net.skyprison.skyprisoncore.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    public TabCompleter() {}

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if(sender instanceof Player player) {
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
                        case "balance", "add", "remove", "set" -> {
                            for (Player oPlayer : Bukkit.getOnlinePlayers()) {
                                commands.add(oPlayer.getName());
                            }
                        }
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

