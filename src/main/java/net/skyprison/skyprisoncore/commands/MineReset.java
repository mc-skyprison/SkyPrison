package net.skyprison.skyprisoncore.commands;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.util.WorldEditRegionConverter;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class MineReset implements CommandExecutor { // /minereset <world> <mine> <colour> <pattern> <player>
    private final SkyPrisonCore plugin;

    public MineReset(SkyPrisonCore plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) {
            if (args.length == 5) {
                World w = Bukkit.getWorld(args[0]);
                String mine = args[1];
                long cooldown = Long.parseLong("0");
                if(plugin.mineCools.containsKey(mine)) {
                    cooldown = plugin.mineCools.get(mine);
                }

                CMIUser user = CMI.getInstance().getPlayerManager().getUser(args[4]);
                if(cooldown != 0 && System.currentTimeMillis() < cooldown) {
                    boolean onCooldown = true;
                    if(user.getPlayer().getInventory().contains(Material.PAPER)) {
                        NamespacedKey key = new NamespacedKey(plugin, "voucher");
                        int i = 0;
                        for(ItemStack item : user.getPlayer().getInventory().getContents()) {
                            if(item != null) {
                                if (item.getType().equals(Material.PAPER)) {
                                    ItemMeta iMeta = item.getItemMeta();
                                    if (iMeta.getPersistentDataContainer().has(key)) {
                                        String voucherType = iMeta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                                        if (voucherType.equalsIgnoreCase("mine-reset")) {
                                            item.setAmount(item.getAmount()-1);
                                            user.getPlayer().getInventory().setItem(i, item);
                                            onCooldown = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            i++;
                        }
                    }
                    if(onCooldown) {
                        long timeTill = cooldown - System.currentTimeMillis();
                        int minutes = (int) Math.floor((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                        int seconds = (int) Math.floor((timeTill % (1000.0 * 60.0)) / 1000.0);
                        user.sendMessage(plugin.colourMessage("&cMine Reset is on cooldown! Available in: " + minutes + "m " + seconds + "s"));
                        return true;
                    }
                }
                String colour = args[2];
                String pattern = args[3];
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager regions = container.get(BukkitAdapter.adapt(w));
                ProtectedRegion region = regions.getRegion(mine);
                ParserContext context = new ParserContext();
                Actor actor = BukkitAdapter.adapt(sender);
                context.setWorld(BukkitAdapter.adapt(w));
                context.setActor(actor);
                Region rg = WorldEditRegionConverter.convertToRegion(region);
                try {
                    Pattern wPattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(pattern, context);
                    plugin.asConsole("regiontp tp " + mine + " " + mine + " " + args[0] + " -s");
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(w))) {
                        editSession.setBlocks(rg, wPattern);
                        editSession.commit();
                    } catch (WorldEditException e) {
                        e.printStackTrace();
                    }
                    String mineName = mine.replace("-", " ");
                    mineName = WordUtils.capitalize(mineName);
                    if (!mine.contains("donor")) {
                        plugin.asConsole("broadcast !§f[§cMines§f] " + colour + mineName + " §7has been reset! -w:" + args[0]);
                    } else {
                        String donor = "";
                        String perm = "";
                        switch (mine) {
                            case "donor-mine1":
                                donor = "&dFirst";
                                perm = "group.donor1";
                                break;
                            case "donor-mine2":
                                donor = "&dSecond";
                                perm = "group.donor2";
                                break;
                            case "donor-mine3":
                                donor = "&dThird";
                                perm = "group.donor3";
                                break;
                        }
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.hasPermission(perm) && player.getWorld().getName().equalsIgnoreCase("world_prison")) {
                                player.sendMessage(plugin.colourMessage("&f[&cMines&f] " + donor + " Donor Mine &7has been reset!"));
                            }
                        }
                    }

                    plugin.mineCools.put(mine, System.currentTimeMillis() + 1800000);
                } catch (InputParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
