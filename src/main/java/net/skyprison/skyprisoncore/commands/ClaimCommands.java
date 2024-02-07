package net.skyprison.skyprisoncore.commands;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.claims.ClaimPending;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.PlayerManager;
import net.skyprison.skyprisoncore.utils.claims.ClaimData;
import net.skyprison.skyprisoncore.utils.claims.ClaimMember;
import net.skyprison.skyprisoncore.utils.claims.ClaimUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.LongParser.longParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;

public class ClaimCommands {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;
    private final PaperCommandManager<CommandSender> manager;
    private final Component prefix = ClaimUtils.getPrefix();
    private final Component notFound = ClaimUtils.getNotFound();
    public ClaimCommands(SkyPrisonCore plugin, DatabaseHook db, PaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.db = db;
        this.manager = manager;
        createClaimCommands();
    }
    private void createClaimCommands() {
        ClaimUtils claim = new ClaimUtils(plugin, db);

        Command.Builder<CommandSender> claimMain = manager.commandBuilder("claim")
                .permission("skyprisoncore.command.claim")
                .handler(c -> claim.helpMessage(c.sender(), 1));

        manager.command(claimMain);

        // Claim Basics

        manager.command(claimMain.literal("help")
                .optional("page", integerParser(1, 2), DefaultValue.constant(1))
                .handler(c -> claim.helpMessage(c.sender(), c.get("page"))));
        manager.command(claimMain.literal("create")
                .required("name", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("name");
                    if (!player.getWorld().getName().equalsIgnoreCase("world_free")) {
                        player.sendMessage(prefix.append(Component.text("Claiming is not allowed in this world!", NamedTextColor.RED)));
                        return;
                    }

                    RegionSelector regionSelector = claim.getRegionSelector(player);
                    if (!regionSelector.isDefined()) {
                        player.sendMessage(prefix.append(Component.text("You havn't created a selection!", NamedTextColor.RED)));
                        return;
                    }

                    claim.createClaim(player, claimName, regionSelector);
                }));
        manager.command(claimMain.literal("customheight")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    if(ClaimUtils.customClaimHeight.contains(player.getUniqueId())) {
                        ClaimUtils.customClaimHeight.remove(player.getUniqueId());
                        player.sendMessage(prefix.append(Component.text("Custom height claiming disabled!", NamedTextColor.RED)));
                    } else {
                        ClaimUtils.customClaimHeight.add(player.getUniqueId());
                        player.sendMessage(prefix.append(Component.text("Custom height claiming enabled!", NamedTextColor.GREEN)));
                    }
                }));
        manager.command(claimMain.literal("customshape")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    com.sk89q.worldedit.entity.Player bPlayer = BukkitAdapter.adapt(player);
                    LocalSession session = WorldEdit.getInstance().getSessionManager().get(bPlayer);
                    final RegionSelector newSelector;
                    RegionSelector regionSelector = claim.getRegionSelector(player);
                    if(ClaimUtils.customClaimShape.contains(player.getUniqueId())) {
                        ClaimUtils.customClaimShape.remove(player.getUniqueId());
                        newSelector = new CuboidRegionSelector(regionSelector);
                        player.sendMessage(prefix.append(Component.text("Custom shape claiming disabled!", NamedTextColor.RED)));
                    } else {
                        ClaimUtils.customClaimShape.add(player.getUniqueId());
                        newSelector = new Polygonal2DRegionSelector(regionSelector);
                        player.sendMessage(prefix.append(Component.text("Custom shape claiming enabled!", NamedTextColor.GREEN)));
                    }
                    session.setRegionSelector(bPlayer.getWorld(), newSelector);
                }));
        Command.Builder<CommandSender> claimDelete = claimMain.literal("delete")
                .required("name", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("name");
                    deleteClaim(player, player.getUniqueId(), claimName, claim);
                });
        manager.command(claimDelete);
        manager.command(claimDelete
                .permission("skyprisoncore.command.claim.admin")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("name");
                    String targetPlayer = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    deleteClaim(player, targetId, claimName, claim);
                }));


        manager.command(claimMain.literal("wand")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.performCommand("/wand"));
                }));

        // Claim Infos

        Command.Builder<CommandSender> list = claimMain.literal("list")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    claim.claimList(player, player.getUniqueId(), 1);
                });
        manager.command(list);
        Command.Builder<CommandSender> listPage = list.required("page", integerParser(1))
                .handler(c -> {
                    Player player = (Player) c.sender();
                    int page = c.get("page");
                    claim.claimList(player, player.getUniqueId(), page);
                });
        manager.command(listPage);
        manager.command(listPage.required("player", stringParser())
                        .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    int page = c.get("page");
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        c.sender().sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    claim.claimList(c.sender(), targetId, page);
                }));
        manager.command(listPage.literal("all")
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    int page = c.get("page");
                    claim.claimListAll(c.sender(), page);
                }));


        Command.Builder<CommandSender> info = claimMain.literal("info")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    List<ClaimData> claimsAtLoc = claim.getClaimsFromloc(player.getLocation(), player);
                    if(claimsAtLoc.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }

                    if (claimsAtLoc.size() == 1) {
                        claim.claimInfo(player, claimsAtLoc.getFirst());
                    } else {
                        claim.claimInfoMultiple(player, claimsAtLoc);
                    }
                });
        manager.command(info);
        Command.Builder<CommandSender> infoSpecific = info.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("claim");
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), claimName, Arrays.asList("owner", "co-owner", "member"));
                    if(claims.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }
                    if (claims.size() == 1) {
                        claim.claimInfo(player, claims.getFirst());
                    } else {
                        claim.claimInfoMultiple(player, claims);
                    }
                });
        manager.command(infoSpecific);
        manager.command(infoSpecific.required("player", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        c.sender().sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetId, claimName, Arrays.asList("owner", "co-owner", "member"));
                    if(claims.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }
                    if (claims.size() == 1) {
                        claim.claimInfo(player, claims.getFirst());
                    } else {
                        claim.claimInfoMultiple(player, claims);
                    }
                }));
        manager.command(claimMain.literal("nearby")
                .required("radius", integerParser(1))
                .handler(c -> {
                    int radius = c.get("radius");
                    Player player = (Player) c.sender();
                    if(radius > 200 && !claim.hasPerm(player)) {
                        player.sendMessage(prefix.append(Component.text("Incorrect Usage! Max radius is 200 blocks.", NamedTextColor.RED)));
                        return;
                    }
                    int pX = player.getLocation().getBlockX();
                    int pZ = player.getLocation().getBlockZ();
                    ProtectedCuboidRegion scanRegion = new ProtectedCuboidRegion("nearby_scan", true,
                            BlockVector3.at(pX - radius, -64, pZ - radius),
                            BlockVector3.at(pX + radius, 319, pZ + radius));
                    List<ProtectedRegion> regions = scanRegion.getIntersectingRegions(claim.getRegionManager(player).getRegions().values());
                    if(regions.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }
                    List<String> regionIds = new ArrayList<>();
                    regions.forEach(region -> {
                        if(region.getId().startsWith("claim_")) {
                            regionIds.add(region.getId());
                        }
                    });
                    claim.claimInfoMultiple(player, claim.getClaims(regionIds));
                }));

        // Claim User Management
        Command.Builder<CommandSender> invite = claimMain.literal("invite")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);
                    invitePlayer(player, targetId, claims, claim);
                });
        manager.command(invite);
        Command.Builder<CommandSender> inviteClaim = invite.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), claimName, Arrays.asList("owner", "co-owner"));
                    invitePlayer(player, targetId, claims, claim);
                });
        manager.command(inviteClaim);
        manager.command(inviteClaim.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    String targetPlayer = c.get("target");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetPlayerId, claimName, Arrays.asList("owner", "co-owner"));
                    invitePlayer(player, targetId, claims, claim);
                }));



        Command.Builder<CommandSender> kick = claimMain.literal("kick")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);
                    kickPlayer(player, targetId, claims, claim);
                });
        manager.command(kick);
        Command.Builder<CommandSender> kickClaim = kick.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), claimName, Arrays.asList("owner", "co-owner"));
                    kickPlayer(player, targetId, claims, claim);
                });
        manager.command(kickClaim);
        manager.command(kickClaim.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    String targetPlayer = c.get("target");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetPlayerId, claimName, Arrays.asList("owner", "co-owner"));
                    kickPlayer(player, targetId, claims, claim);
                }));


        Command.Builder<CommandSender> promote = claimMain.literal("promote")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);
                    promotePlayer(player, targetId, claims, claim);
                });
        manager.command(promote);
        Command.Builder<CommandSender> promoteClaim = promote.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), claimName, Arrays.asList("owner", "co-owner"));
                    promotePlayer(player, targetId, claims, claim);
                });
        manager.command(promoteClaim);
        manager.command(promoteClaim.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    String targetPlayer = c.get("target");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetPlayerId, claimName, Arrays.asList("owner", "co-owner"));
                    promotePlayer(player, targetId, claims, claim);
                }));


        Command.Builder<CommandSender> demote = claimMain.literal("demote")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);
                    demotePlayer(player, targetId, claims, claim);
                });
        manager.command(demote);
        Command.Builder<CommandSender> demoteClaim = demote.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), claimName, Arrays.asList("owner", "co-owner"));
                    demotePlayer(player, targetId, claims, claim);
                });
        manager.command(demoteClaim);
        manager.command(demoteClaim.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    String targetPlayer = c.get("target");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetPlayerId, claimName, Arrays.asList("owner", "co-owner"));
                    demotePlayer(player, targetId, claims, claim);
                }));


        Command.Builder<CommandSender> pending = claimMain.literal("pending")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), Arrays.asList("owner", "co-owner"));
                    pendingData(player, claims, claim);
                });
        manager.command(pending);
        manager.command(pending.literal("all")
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new ClaimPending(plugin, claim.getAllClaims()).getInventory()));
                }));
        manager.command(pending.required("player", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetId, Arrays.asList("owner", "co-owner"));
                    pendingData(player, claims, claim);
                }));
        // Claim Management

        Command.Builder<CommandSender> flags = claimMain.literal("flags", "flag")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);
                    if (claims.size() == 1) {
                        claim.claimFlags(player, player.getUniqueId(), claims.getFirst());
                    } else {
                        claim.claimFlagsMultiple(player, player.getUniqueId(), claims);
                    }
                });
        manager.command(flags);
        Command.Builder<CommandSender> flagsClaim = flags.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("claim");
                    List<ClaimData> claims = claim.getPlayerClaims(player.getUniqueId(), claimName, Arrays.asList("owner", "co-owner", "member"));
                    if(claims.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }
                    if (claims.size() == 1) {
                        claim.claimFlags(player, player.getUniqueId(), claims.getFirst());
                    } else {
                        claim.claimFlagsMultiple(player, player.getUniqueId(), claims);
                    }
                });
        manager.command(flagsClaim);
        manager.command(flagsClaim.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("claim");
                    String targetPlayer = c.get("target");
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getPlayerClaims(targetPlayerId, claimName, Arrays.asList("owner", "co-owner", "member"));
                    if(claims.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }
                    if (claims.size() == 1) {
                        claim.claimFlags(player, targetPlayerId, claims.getFirst());
                    } else {
                        claim.claimFlagsMultiple(player, targetPlayerId, claims);
                    }
                }));

        manager.command(claimMain.literal("expand")
                .required("amount", integerParser(1))
                .handler(c -> {
                    int amount = c.get("amount");
                    Player player = (Player) c.sender();
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);

                    claims = claims.stream().filter(claimData -> {
                        ClaimMember member = claimData.getMember(player.getUniqueId());
                        return claim.hasPerm(player) || (member != null && member.getRank().equalsIgnoreCase("owner"));
                    }).toList();

                    if(claims.isEmpty()) {
                        player.sendMessage(notFound);
                        return;
                    }

                    if (claims.size() == 1) {
                        claim.expandClaim(player, claims.getFirst(), amount, player.getFacing());
                    } else {
                        claim.expandClaimMultiple(player, claims, amount, player.getFacing());
                    }
                }));

        Command.Builder<CommandSender> rename = claimMain.literal("rename")
                .required("claim", stringParser())
                .required("new name", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("claim");
                    String newName = c.get("new name");
                    updateName(player, player.getUniqueId(), claimName, newName, claim);
                });
        manager.command(rename);
        manager.command(rename.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String claimName = c.get("claim");
                    String newName = c.get("new name");
                    String targetPlayer = c.get("target");
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    updateName(player, targetPlayerId, claimName, newName, claim);
                }));


        Command.Builder<CommandSender> transfer = claimMain.literal("transfer")
                .required("player", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    if(targetId.equals(player.getUniqueId())) {
                        player.sendMessage(prefix.append(Component.text("You can't transfer a claim to yourself!", NamedTextColor.RED)));
                        return;
                    }
                    List<ClaimData> claims = claim.getClaimsFromloc(player.getLocation(), player);
                    claims = claims.stream().filter(claimData -> {
                        ClaimMember member = claimData.getMember(player.getUniqueId());
                        return claim.hasPerm(player) || (member != null && member.getRank().equalsIgnoreCase("owner"));
                    }).toList();
                    ClaimData parentClaim = claims.stream().filter(claimData -> claimData.getParent() == null).findFirst().orElse(null);
                    transferClaim(player, targetId, parentClaim, claim);
                });
        manager.command(transfer);
        Command.Builder<CommandSender> transferClaim = transfer.required("claim", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    if(targetId.equals(player.getUniqueId())) {
                        player.sendMessage(prefix.append(Component.text("You can't transfer a claim to yourself!", NamedTextColor.RED)));
                        return;
                    }
                    ClaimData claims = claim.getPlayerClaim(player.getUniqueId(), claimName, "owner");
                    transferClaim(player, targetId, claims, claim);
                });
        manager.command(transferClaim);
        manager.command(transferClaim.required("target", stringParser())
                .permission("skyprisoncore.command.claim.admin")
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String target = c.get("player");
                    String claimName = c.get("claim");
                    String targetPlayer = c.get("target");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    UUID targetPlayerId = PlayerManager.getPlayerId(targetPlayer);
                    if(targetPlayerId == null) {
                        player.sendMessage(prefix.append(Component.text("Target player not found!", NamedTextColor.RED)));
                        return;
                    }
                    if(targetId.equals(targetPlayerId)) {
                        player.sendMessage(prefix.append(Component.text("You can't transfer a claim to the same player!", NamedTextColor.RED)));
                        return;
                    }
                    ClaimData claimData = claim.getPlayerClaim(targetPlayerId, claimName, "owner");
                    transferClaim(player, targetId, claimData, claim);
                }));

        // Claim Blocks

        Command.Builder<CommandSender> claimBlocks = claimMain.literal("blocks")
                .handler(c -> {

                });
        manager.command(claimBlocks);
        manager.command(claimBlocks.literal("buy")
                .required("amount", longParser(1))
                .handler(c -> {
                    Player player = (Player) c.sender();
                    long blocks = c.get("amount");
                    double price = 40 * blocks;
                    if(PlayerManager.getBalance(player) < price) {
                        long needed = (long) (price - PlayerManager.getBalance(player));
                        player.sendMessage(prefix.append(Component.text("You don't have enough money! You need $" + ChatUtils.formatNumber(needed) + " more..", NamedTextColor.RED)));
                        return;
                    }
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                        ps.setLong(1, blocks);
                        ps.setString(2, player.getUniqueId().toString());
                        ps.executeUpdate();
                        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "cmi money take " + player.getName() + " " + price));
                        player.sendMessage(prefix.append(Component.text("Successfully bought ", TextColor.fromHexString("#20df80"))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" blocks for $", TextColor.fromHexString("#20df80")))
                                .append(Component.text(ChatUtils.formatNumber(price), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }));
        manager.command(claimBlocks.literal("give")
                .permission("skyprisoncore.command.claim.admin")
                .required("player", stringParser())
                .required("amount", longParser(1))
                .handler(c -> {
                    CommandSender sender = c.sender();
                    String target = c.get("player");
                    long blocks = c.get("amount");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        sender.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }

                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                        ps.setLong(1, blocks);
                        ps.setString(2, targetId.toString());
                        ps.executeUpdate();
                        sender.sendMessage(prefix.append(Component.text("Successfully gave ", TextColor.fromHexString("#20df80"))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" blocks to ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetId), "Couldn't get name!"),
                                        TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                        Component msg = prefix.append(Component.text(sender.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" has given you ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" blocks!", TextColor.fromHexString("#20df80")));
                        PlayerManager.sendMessage(targetId, msg, "claim-give");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }));
        manager.command(claimBlocks.literal("set")
                .permission("skyprisoncore.command.claim.admin")
                .required("player", stringParser())
                .required("amount", longParser(0))
                .handler(c -> {
                    CommandSender sender = c.sender();
                    long blocks = c.get("amount");
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        sender.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    ClaimUtils.ClaimBlocks pBlocks = claim.getPlayerBlocks(targetId);
                    if(pBlocks.used() < blocks) {
                        sender.sendMessage(prefix.append(Component.text("This would put the player's total blocks below their used blocks!", NamedTextColor.RED)));
                        return;
                    }
                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = ? WHERE user_id = ?")) {
                        ps.setLong(1, blocks);
                        ps.setString(2, targetId.toString());
                        ps.executeUpdate();
                        sender.sendMessage(prefix.append(Component.text("Successfully set ", TextColor.fromHexString("#20df80"))
                                        .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetId), "Couldn't get name!"),
                                                TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)))
                                .append(Component.text(" blocks to ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));
                        Component msg = prefix.append(Component.text(sender.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" set your blocks to ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text("!", TextColor.fromHexString("#20df80")));
                        PlayerManager.sendMessage(targetId, msg, "claim-set");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }));
        manager.command(claimBlocks.literal("take")
                .permission("skyprisoncore.command.claim.admin")
                .required("player", stringParser())
                .required("amount", longParser(1))
                .handler(c -> {
                    CommandSender sender = c.sender();
                    long blocks = c.get("amount");
                    String target = c.get("player");
                    UUID targetId = PlayerManager.getPlayerId(target);
                    if(targetId == null) {
                        sender.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                        return;
                    }
                    ClaimUtils.ClaimBlocks pBlocks = claim.getPlayerBlocks(targetId);
                    if(pBlocks.used() < blocks) {
                        sender.sendMessage(prefix.append(Component.text("This would put the player's total blocks below their used blocks!", NamedTextColor.RED)));
                        return;
                    }
                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks - ? WHERE user_id = ?")) {
                        ps.setLong(1, blocks);
                        ps.setString(2, targetId.toString());
                        ps.executeUpdate();
                        sender.sendMessage(prefix.append(Component.text("Successfully took ", TextColor.fromHexString("#20df80"))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" blocks from ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetId), "Couldn't get name!"),
                                        TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                        Component msg = prefix.append(Component.text(sender.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" took ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(ChatUtils.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text(" blocks from you!", TextColor.fromHexString("#20df80")));
                        PlayerManager.sendMessage(targetId, msg, "claim-take");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }));

        manager.command(claimMain.literal("accept")
                .required("type", stringParser())
                .required("id", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String type = c.get("type");
                    String id = c.get("id");
                    String claimId = NotificationsUtils.hasNotification(id, player);
                    if(claimId == null || claimId.isEmpty()) return;
                    ClaimData claimData = claim.getClaim(claimId);
                    if (type.equalsIgnoreCase("invite")) {
                        claim.inviteAccept(player, claimData, id);
                    } else if (type.equalsIgnoreCase("transfer")) {
                        claim.transferAccept(player, claimData, id);
                    }
                }));
        manager.command(claimMain.literal("decline")
                .required("type", stringParser())
                .required("id", stringParser())
                .handler(c -> {
                    Player player = (Player) c.sender();
                    String type = c.get("type");
                    String id = c.get("id");
                    String claimId = NotificationsUtils.hasNotification(id, player);
                    if(claimId == null || claimId.isEmpty()) return;
                    ClaimData claimData = claim.getClaim(claimId);
                    if (type.equalsIgnoreCase("invite")) {
                        claim.inviteDecline(player, claimData, id);
                    } else if (type.equalsIgnoreCase("transfer")) {
                        claim.transferDecline(player, claimData, id);
                    }
                }));
    }
    private void updateName(Player player, UUID targetPlayerId, String claimName, String newName, ClaimUtils claim) {
        ClaimData claimData = claim.getPlayerClaim(targetPlayerId, claimName, "owner");
        if(claimData == null) {
            player.sendMessage(notFound);
            return;
        }

        ClaimData nameTaken = claim.getPlayerClaim(targetPlayerId, newName, "owner");
        if(nameTaken != null) {
            player.sendMessage(prefix.append(Component.text("You already have a claim with that name!", NamedTextColor.RED)));
            return;
        }
        claim.updateClaimName(claimData, newName);
        player.sendMessage(prefix.append(Component.text("Successfully renamed ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimData.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                .append(Component.text(" to ", TextColor.fromHexString("#20df80")))
                .append(Component.text(newName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
    }
    private void pendingData(Player player, List<ClaimData> claims, ClaimUtils claim) {
        claims = claims.stream().filter(claimData -> {
            ClaimMember member = claimData.getMember(player.getUniqueId());
            return claim.hasPerm(player) || (member != null && !member.getRank().equalsIgnoreCase("member"));
        }).toList();

        if(claims.isEmpty()) {
            player.sendMessage(notFound);
            return;
        }

        List<ClaimData> finalClaims = claims;
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(new ClaimPending(plugin, finalClaims).getInventory()));

    }
    private void transferClaim(Player player, UUID targetId, @Nullable ClaimData claimData, ClaimUtils claim) {
        if(claimData == null) {
            player.sendMessage(notFound);
            return;
        }
        if(!NotificationsUtils.hasNotifications("claim-transfer", Collections.singletonList(claimData.getId()), targetId).isEmpty()) {
            player.sendMessage(prefix.append(Component.text("This claim already has a pending transfer!", NamedTextColor.RED)));
            return;
        }
        if(claimData.getMember(targetId) == null) {
            player.sendMessage(prefix.append(Component.text("The player is not in the specified claim!", NamedTextColor.RED)));
            return;
        }
        if(claimData.getMember(targetId) != null && !claimData.getMember(targetId).getRank().equalsIgnoreCase("co-owner")) {
            player.sendMessage(prefix.append(Component.text("The player isn't a co-owner in the specified claim!", NamedTextColor.RED)));
            return;
        }
        ClaimUtils.transferClaim.add(player.getUniqueId());
        Component msg = prefix.append(Component.text("Are you sure you want to transfer the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(claimData.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" to ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetId), "COULDN'T GET NAME"),
                                TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text("?", TextColor.fromHexString("#20df80"))))
                .append(!claimData.getChildren().isEmpty() ? Component.text("\nThis will also transfer all child claims!", NamedTextColor.GRAY) : Component.empty())
                .append(Component.text("\nTRANSFER CLAIM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                    if(ClaimUtils.transferClaim.contains(player.getUniqueId())) {
                        ClaimUtils.transferClaim.remove(player.getUniqueId());
                        claim.transferClaim(player, claimData.getMember(targetId), claimData);
                    }
                })))
                .append(Component.text("     "))
                .append(Component.text("CANCEL TRANSFER", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                    ClaimUtils.transferClaim.remove(player.getUniqueId());
                    audience.sendMessage(prefix.append(Component.text("Claim transfer cancelled!", NamedTextColor.GRAY)));
                })));
        player.sendMessage(msg);
    }
    private void promotePlayer(Player player, UUID targetId, List<ClaimData> claims, ClaimUtils claim) {
        claims = claims.stream().filter(claimData -> {
            ClaimMember member = claimData.getMember(player.getUniqueId());
            return claim.hasPerm(player) || (member != null && member.getRank().equalsIgnoreCase("owner"));
        }).toList();

        if(claims.isEmpty()) {
            player.sendMessage(notFound);
            return;
        }

        claims = claims.stream().filter(claimData -> claimData.getMember(targetId) != null).toList();
        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player is not in any of the specified claims!", NamedTextColor.RED)));
            return;
        }

        claims = claims.stream().filter(claimData -> claimData.getMember(targetId).getRank().equalsIgnoreCase("member")).toList();
        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player is already a co-owner in all specified claims!", NamedTextColor.RED)));
            return;
        }

        if (claims.size() == 1) {
            ClaimData claimData = claims.getFirst();
            claim.promotePlayer(player, claimData.getMember(targetId), claimData);
        } else {
            claim.promotePlayerMultiple(player, targetId, claims);
        }
    }
    private void demotePlayer(Player player, UUID targetId, List<ClaimData> claims, ClaimUtils claim) {
        claims = claims.stream().filter(claimData -> {
            ClaimMember member = claimData.getMember(player.getUniqueId());
            return claim.hasPerm(player) || (member != null && member.getRank().equalsIgnoreCase("owner"));
        }).toList();

        if(claims.isEmpty()) {
            player.sendMessage(notFound);
            return;
        }

        claims = claims.stream().filter(claimData -> claimData.getMember(targetId) != null).toList();
        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player is not in any of the specified claims!", NamedTextColor.RED)));
            return;
        }

        claims = claims.stream().filter(claimData -> claimData.getMember(targetId).getRank().equalsIgnoreCase("co-owner")).toList();
        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player isn't a co-owner in any of the specified claims!", NamedTextColor.RED)));
            return;
        }

        if (claims.size() == 1) {
            ClaimData claimData = claims.getFirst();
            claim.demotePlayer(player, claimData.getMember(targetId), claimData);
        } else {
            claim.promotePlayerMultiple(player, targetId, claims);
        }
    }
    private void kickPlayer(Player player, UUID targetId, List<ClaimData> claims, ClaimUtils claim) {
        claims = claims.stream().filter(claimData -> {
            ClaimMember member = claimData.getMember(player.getUniqueId());
            return claim.hasPerm(player) || (member != null && !member.getRank().equalsIgnoreCase("member"));
        }).toList();

        if(claims.isEmpty()) {
            player.sendMessage(notFound);
            return;
        }
        claims = claims.stream().filter(claimData -> claimData.getMember(targetId) != null).toList();

        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player is not in any of the specified claims!", NamedTextColor.RED)));
            return;
        }

        claims = claims.stream().filter(claimData -> claimData.getMember(targetId).getRank().equalsIgnoreCase("member")).toList();
        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("You can't kick an owner or co-owner!", NamedTextColor.RED)));
            return;
        }

        if (claims.size() == 1) {
            ClaimData claimData = claims.getFirst();
            claim.kickPlayer(player, claimData.getMember(targetId), claimData);
        } else {
            claim.kickPlayerMultiple(player, targetId, claims);
        }
    }
    private void invitePlayer(Player player, UUID targetId, List<ClaimData> claims, ClaimUtils claim) {
        claims = claims.stream().filter(claimData -> {
            ClaimMember member = claimData.getMember(player.getUniqueId());
            return claim.hasPerm(player) || (member != null && !member.getRank().equalsIgnoreCase("member"));
        }).toList();

        if(claims.isEmpty()) {
            player.sendMessage(notFound);
            return;
        }
        claims = new ArrayList<>(claims.stream().filter(claimData -> claimData.getMember(targetId) == null).toList());

        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player is already in all specified claims!", NamedTextColor.RED)));
            return;
        }
        List<String> alreadyInvited = NotificationsUtils.hasNotifications("claim-invite", claims.stream().map(ClaimData::getId).toList(), targetId);
        if(!alreadyInvited.isEmpty()) {
            claims.removeIf(claimData -> alreadyInvited.contains(claimData.getId()));
        }
        if(claims.isEmpty()) {
            player.sendMessage(prefix.append(Component.text("The player already has an invite to all specified claims!", NamedTextColor.RED)));
            return;
        }
        if (claims.size() == 1) {
            claim.invitePlayer(player, targetId, claims.getFirst());
        } else {
            claim.invitePlayerMultiple(player, targetId, claims);
        }
    }
    private void deleteClaim(Player player, UUID targetPlayer, String claimName, ClaimUtils claim) {
        ClaimData claimData = claim.getPlayerClaim(targetPlayer, claimName, "owner");
        if (claimData == null) {
            player.sendMessage(notFound);
            return;
        }
        ClaimUtils.deleteClaim.add(player.getUniqueId());
        Component msg = prefix.append(Component.text("Are you sure you want to delete the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(claimData.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text("?", TextColor.fromHexString("#20df80"))))
                .append(Component.text("\nDELETE CLAIM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                    if (ClaimUtils.deleteClaim.contains(player.getUniqueId())) {
                        ClaimUtils.deleteClaim.remove(player.getUniqueId());
                        claim.deleteClaim(player, targetPlayer, claimName);
                    }
                })))
                .append(Component.text("     "))
                .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                    ClaimUtils.deleteClaim.remove(player.getUniqueId());
                    audience.sendMessage(prefix.append(Component.text("Claim deletion cancelled!", NamedTextColor.GRAY)));
                })));
        player.sendMessage(msg);
    }
}
