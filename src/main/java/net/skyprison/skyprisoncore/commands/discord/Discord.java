package net.skyprison.skyprisoncore.commands.discord;

import com.google.inject.Inject;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Discord implements CommandExecutor {
    private final SkyPrisonCore plugin;
    @Inject
    public Discord(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            if(args.length == 0) {
                player.sendMessage(plugin.colourMessage("&3Join our discord at &9&lhttp://discord.gg/T9DwRcPpgj&3!"));
            } else if(args.length == 1) {
                switch(args[0].toLowerCase()) {
                    case "link":
                        break;
                    case "unlink":
                        break;
                    default:
                        player.sendMessage(plugin.colourMessage("&3Join our discord at &9&lhttp://discord.gg/T9DwRcPpgj&3!"));
                        break;
                }
            }
        }
        return true;
    }
}
