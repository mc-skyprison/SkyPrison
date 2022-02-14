package net.skyprison.skyprisoncore.commands;

import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Tags  implements CommandExecutor {
    private final SkyPrisonCore plugin;

    @Inject
    public Tags(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, Integer page) {

    }


    public void openEditGUI(Player player, Integer page) {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                openGUI(player, 1);
            } else {
                if(args.length == 1 && args[0].equalsIgnoreCase("edit")) {
                    
                }
            }
        }
        return true;
    }
}
