package net.skyprison.skyprisoncore.commands;

import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.file14.procosmetics.ProCosmetics;
import se.file14.procosmetics.api.ProCosmeticsProvider;
import se.file14.procosmetics.cosmetic.AbstractCosmetic;
import se.file14.procosmetics.cosmetic.AbstractCosmeticType;
import se.file14.procosmetics.cosmetic.CosmeticCategory;
import se.file14.procosmetics.cosmetic.balloon.BalloonType;
import se.file14.procosmetics.cosmetic.particleeffect.ParticleEffectType;
import se.file14.procosmetics.v1_8.Particle;

import java.util.List;

public class Tags implements CommandExecutor {
    private final SkyPrisonCore plugin;

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
            ProCosmetics api = ProCosmeticsProvider.get();
            AbstractCosmeticType asd = (AbstractCosmeticType) CosmeticCategory.PARTICLE_EFFECTS.getCosmeticTypes().get(0);
            asd.equip(api.getUserManager().getUser(player), true);
            if(args.length == 0) {
                openGUI(player, 1);
            } else {
                if(args.length == 1 && args[0].equalsIgnoreCase("edit")) {
                    if(player.hasPermission("skyprisoncore.command.tags.edit"))
                        openEditGUI(player, 1);
                    else
                        player.sendMessage("&cYou do not have access to this command!");
                }
            }
        }
        return true;
    }
}
