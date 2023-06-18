package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class OldName implements CommandExecutor { // /oldname
    private final SkyPrisonCore plugin;
    public OldName(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }


    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        Map<UUID, CMIUser> users = CMI.getInstance().getPlayerManager().getAllUsers();

        new BukkitRunnable() {
            int i = 1;
            @Override
            public void run() {
                for(CMIUser user : users.values()) {
                    if(i == 39) break;
                    if(user.getNickName() == null) continue;
                    plugin.getLogger().info(i + ". Added: " + user.getName());
                    Component disComp = LegacyComponentSerializer.legacySection().deserialize(user.getNickName());
                    String disString = MiniMessage.miniMessage().serialize(disComp);

                    String[] disSplit = disString.split("((?=<))");
                    StringBuilder oldDisplayBuilder = new StringBuilder();
                    for (String split : disSplit) {
                        if (!split.startsWith("</")) oldDisplayBuilder.append(split);
                    }

                    String oldDisplay = oldDisplayBuilder.toString();
                    Component dis = MiniMessage.miniMessage().deserialize(oldDisplay);
                    plugin.scheduleForOnline(user.getUniqueId().toString(), "namecolour", GsonComponentSerializer.gson().serialize(dis));
                    i++;
                }

            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
