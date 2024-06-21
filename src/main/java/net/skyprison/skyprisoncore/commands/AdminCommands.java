package net.skyprison.skyprisoncore.commands;

import com.destroystokyo.paper.MaterialSetTag;
import com.destroystokyo.paper.MaterialTags;
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
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.ItemLore;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.incendo.cloud.Command;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.bukkit.parser.WorldParser.worldParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class AdminCommands {
    private final SkyPrisonCore plugin;
    private final PaperCommandManager<CommandSourceStack> manager;
    public AdminCommands(SkyPrisonCore plugin, PaperCommandManager<CommandSourceStack> manager) {
        this.plugin = plugin;
        this.manager = manager;
        createAdminCommands();
    }
    private void createAdminCommands() {
        manager.command(manager.commandBuilder("itemlore", "ilore")
                .permission("skyprisoncore.command.itemlore")
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    if(!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
                        return;
                    }
                    new ItemLore(plugin).displayLore(player);
                }));

        Command.Builder<CommandSourceStack> rename = manager.commandBuilder("rename")
                .permission("skyprisoncore.command.rename");

        manager.command(rename.literal("remove")
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    if(!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
                        return;
                    }
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (!heldItem.getType().isItem()) {
                        player.sendMessage(Component.text("You're not holding an item!", NamedTextColor.RED));
                        return;
                    }
                    if(!heldItem.hasDisplayName()) {
                        player.sendMessage(Component.text("This item doesn't have a display name!", NamedTextColor.RED));
                        return;
                    }
                    heldItem.editMeta(meta -> meta.displayName(null));
                    player.sendMessage(Component.text("Successfully removed display name!", NamedTextColor.YELLOW));

                }));
        manager.command(rename.required("name", greedyStringParser())
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    if(!(sender instanceof Player player)) {
                        sender.sendMessage(Component.text("You must be a player to use this command!", NamedTextColor.RED));
                        return;
                    }
                    ItemStack heldItem = player.getInventory().getItemInMainHand();
                    if (!heldItem.getType().isItem()) {
                        player.sendMessage(Component.text("You're not holding an item!", NamedTextColor.RED));
                        return;
                    }
                    String name = c.get("name");
                    Component displayName = MiniMessage.miniMessage().deserialize(name).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
                    heldItem.editMeta(meta -> meta.displayName(displayName));
                    player.sendMessage(Component.text("Successfully changed display name to ", NamedTextColor.YELLOW).append(displayName.colorIfAbsent(NamedTextColor.WHITE)));
                }));


        manager.command(manager.commandBuilder("minereset")
                .permission("skyprisoncore.command.minereset")
                .required("world", worldParser())
                .required("mine", stringParser())
                .required("colour", stringParser())
                .required("pattern", stringParser())
                .required("player", playerParser())
                .handler(c -> {
                    World w = c.get("world");
                    String mine = c.get("mine");
                    long cooldown = 0;
                    if (plugin.mineCools.containsKey(mine)) {
                        cooldown = plugin.mineCools.get(mine);
                    }

                    Player player = c.get("player");
                    if (cooldown != 0 && System.currentTimeMillis() < cooldown) {
                        boolean onCooldown = true;
                        if (player.getInventory().contains(Material.PAPER)) {
                            NamespacedKey key = new NamespacedKey(plugin, "voucher");

                            ItemStack voucher = Arrays.stream(player.getInventory().getContents()).filter(item -> {
                                if (item == null || !item.getType().equals(Material.PAPER)) return false;
                                PersistentDataContainer itemPers = item.getItemMeta().getPersistentDataContainer();
                                return itemPers.has(key, PersistentDataType.STRING) && itemPers.get(key, PersistentDataType.STRING).equalsIgnoreCase("mine-reset");
                            }).findFirst().orElse(null);

                            if (voucher != null) {
                                ItemStack remVoucher = voucher.clone();
                                remVoucher.setAmount(1);
                                if(player.getInventory().removeItem(remVoucher).isEmpty()) onCooldown = false;
                            }
                        }
                        if (onCooldown) {
                            long timeTill = cooldown - System.currentTimeMillis();
                            int minutes = (int) Math.floor((timeTill % (1000.0 * 60.0 * 60.0)) / (1000.0 * 60.0));
                            int seconds = (int) Math.floor((timeTill % (1000.0 * 60.0)) / 1000.0);
                            player.sendMessage(Component.text("Mine Reset is on cooldown! Available in: " + minutes + "m " + seconds + "s", NamedTextColor.RED));
                            return;
                        }
                    }
                    String colour = c.get("colour");
                    String pattern = c.get("pattern");
                    com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(w);
                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                    RegionManager regions = container.get(weWorld);
                    if (regions == null) {
                        player.sendMessage(Component.text("Mine region not found! Contact an admin.", NamedTextColor.RED));
                        return;
                    }
                    ProtectedRegion region = regions.getRegion(mine);
                    ParserContext context = new ParserContext();
                    Actor actor = BukkitAdapter.adapt(player);
                    context.setWorld(weWorld);
                    context.setActor(actor);
                    Region rg = WorldEditRegionConverter.convertToRegion(region);
                    try {
                        WorldEdit we = WorldEdit.getInstance();
                        Pattern wPattern = we.getPatternFactory().parseFromInput(pattern, context);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "regiontp tp " + mine + " " + mine + " " + w.getName() + " -s");
                        try (EditSession editSession = we.newEditSession(weWorld)) {
                            editSession.setBlocks(rg, wPattern);
                            editSession.commit();
                        } catch (WorldEditException e) {
                            e.printStackTrace();
                        }
                        String mineName = mine.replace("-", " ");
                        mineName = StringUtils.capitalize(mineName);
                        Audience audiences = plugin.getServer().filterAudience(audience -> audience instanceof Player oPlayer && oPlayer.getWorld().equals(w));
                        Component prefix = Component.text("[", NamedTextColor.WHITE).append(Component.text("Mines", NamedTextColor.RED)
                                .append(Component.text("] ", NamedTextColor.WHITE)));

                        if (!mine.contains("donor")) {
                            audiences.sendMessage(prefix.append(Component.text(mineName, TextColor.fromHexString(colour))
                                    .append(Component.text(" has been reset!", NamedTextColor.GRAY))));
                        } else {
                            record Donor(String mine, String perm){}

                            Donor donor = switch (mine) {
                                case "donor-mine1" -> new Donor("First", "group.donor1");
                                case "donor-mine2" -> new Donor("Second", "group.donor2");
                                case "donor-mine3" -> new Donor("Third", "group.donor3");
                                default -> null;
                            };

                            if(donor == null) {
                                player.sendMessage(Component.text("Invalid donor mine!", NamedTextColor.RED));
                                return;
                            }
                            Component donorName = Component.text(donor.mine + " Donor Mine", NamedTextColor.LIGHT_PURPLE);
                            audiences.filterAudience(audience -> audience instanceof Player oPlayer && oPlayer.hasPermission(donor.perm))
                                    .sendMessage(prefix.append(donorName).append(Component.text(" has been reset!", NamedTextColor.GRAY)));
                        }
                        plugin.mineCools.put(mine, System.currentTimeMillis() + 1800000);
                    } catch (InputParseException e) {
                        e.printStackTrace();
                    }
                }));

        manager.command(manager.commandBuilder("randomgive")
                .permission("skyprisoncore.command.randomgive")
                .required("player", playerParser())
                .required("item", stringParser(), SuggestionProvider.suggestingStrings(List.of("candle", "concrete")))
                .required("amount", integerParser(1))
                .flag(manager.flagBuilder("silent").withAliases("s"))
                .handler(c -> {
                    CommandSender sender = c.sender().getSender();
                    Player player = c.get("player");
                    String item = c.get("item");
                    int amount = c.get("amount");
                    boolean silent = c.flags().isPresent("silent");

                    ItemStack randomItem;
                    TextComponent senderMessage = Component.text("Successfully gave ", NamedTextColor.GREEN)
                            .append(Component.text(player.getName(), NamedTextColor.GREEN, TextDecoration.BOLD))
                            .append(Component.text(" " + amount + " randomly", NamedTextColor.GREEN));
                    TextComponent playerMessage = Component.text("You've received " + amount + " randomly", NamedTextColor.GREEN);
                    TextComponent itemText;
                    switch (item.toLowerCase()) {
                        case "candle" -> {
                            List<Material> candles = new ArrayList<>(MaterialSetTag.CANDLES.getValues());
                            Collections.shuffle(candles);
                            randomItem = new ItemStack(candles.getFirst(), amount);
                            itemText = Component.text("coloured candles!", NamedTextColor.GREEN);
                        }
                        case "concrete" -> {
                            List<Material> concretes = new ArrayList<>(MaterialTags.CONCRETES.getValues());
                            Collections.shuffle(concretes);
                            randomItem = new ItemStack(concretes.getFirst(), amount);
                            itemText = Component.text("coloured concrete!", NamedTextColor.GREEN);
                        }
                        default -> {
                            sender.sendMessage(Component.text("Item not found!", NamedTextColor.RED));
                            return;
                        }
                    }
                    PlayerManager.giveItems(player, randomItem);
                    if (!silent) {
                        sender.sendMessage(senderMessage.appendSpace().append(itemText));
                        player.sendMessage(playerMessage.appendSpace().append(itemText));
                    }
                }));
    }
}
