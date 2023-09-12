package net.skyprison.skyprisoncore.commands.old;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.claims.ClaimFlags;
import net.skyprison.skyprisoncore.inventories.claims.ClaimMembers;
import net.skyprison.skyprisoncore.inventories.claims.ClaimPending;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Claim implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public final Component prefix = Component.text("Claims", TextColor.fromHexString("#0fc3ff")).append(Component.text(" | ", NamedTextColor.WHITE));

    public final Component notFound = prefix.append(Component.text("No claim(s) were found!", NamedTextColor.RED));


    public Claim(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    public UUID getClaimOwner(String claimId) {
        UUID uuid = null;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM claims_members WHERE claim_id = ? AND user_rank = ?")) {
            ps.setString(1, claimId);
            ps.setString(2, "owner");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                uuid = UUID.fromString(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuid;
    }

    public HashMap<String, UUID> getClaimOwners(List<String> claimIds) {
        HashMap<String, UUID> userUUIDs = new HashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id FROM claims_members WHERE claim_id IN "
                + SkyPrisonCore.getQuestionMarks(claimIds) + " AND user_rank = ?")) {
            int i;
            for (i = 0; i < claimIds.size(); i++) {
                ps.setString(i + 1, claimIds.get(i));
            }
            ps.setString(i + 1, "owner");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                userUUIDs.put(rs.getString(1), UUID.fromString(rs.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userUUIDs;
    }
    public HashMap<String, HashMap<String, Object>> getClaimData(List<String> claimIds) {
        HashMap<String, HashMap<String, Object>> claimInfos = new HashMap<>();
        if(claimIds.isEmpty()) return claimInfos;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, claim_name, parent_id, world, blocks_used FROM claims WHERE claim_id IN "
                + SkyPrisonCore.getQuestionMarks(claimIds))) {
            for (int i = 0; i < claimIds.size(); i++) {
                ps.setString(i + 1, claimIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> claimInfo = new HashMap<>();
                claimInfo.put("name", rs.getString(2));
                claimInfo.put("parent", rs.getString(3));
                claimInfo.put("world", rs.getString(4));
                claimInfo.put("blocks", rs.getLong(5));
                claimInfos.put(rs.getString(1), claimInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claimInfos;
    }

    public List<String> getClaimIdsFromNames(OfflinePlayer player, String claimName, List<String> ranks) {
        HashMap<String, List<String>> claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(claimName), ranks);

        if(!claimIds.isEmpty()) {
            return claimIds.get(claimName);
        }
        return new ArrayList<>();
    }

    public HashMap<String, List<String>> getClaimIdsFromNames(List<OfflinePlayer> players, List<String> claimNames, List<String> ranks) {
        HashMap<String, List<String>> claimIds = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_name, claim_id FROM claims WHERE claim_id IN (SELECT claim_id FROM claims_members WHERE user_id IN "
                + SkyPrisonCore.getQuestionMarks(playerIds) + " AND user_rank IN " + SkyPrisonCore.getQuestionMarks(ranks) + ")")) {
            int b = 0;
            for (int i = 0; i < playerIds.size() + ranks.size(); i++) {
                if(i < playerIds.size()) {
                    ps.setString(i + 1, playerIds.get(i));
                } else {
                    ps.setString(i + 1, ranks.get(b));
                    b++;
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String claimName = rs.getString(1);
                if(claimNames.contains(claimName)) {
                    List<String> claims = new ArrayList<>();
                    if(claimIds.containsKey(claimName)) claims = claimIds.get(claimName);
                    claims.add(rs.getString(2));
                    claimIds.put(claimName, claims);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claimIds;
    }

    public HashMap<String, Long> getPlayerBlocks(OfflinePlayer player) {
        HashMap<String, Long>  playerBlocks = new HashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ? ")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                playerBlocks.put("total", rs.getLong(1));
                playerBlocks.put("used", rs.getLong(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerBlocks;
    }

    public HashMap<UUID, HashMap<String, Long>> getPlayersBlocks(List<OfflinePlayer> players) {
        HashMap<UUID, HashMap<String, Long>>  playerBlocks = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_blocks, claim_blocks_used FROM users WHERE user_id IN "
                + SkyPrisonCore.getQuestionMarks(playerIds))) {
            for (int i = 0; i < playerIds.size(); i++) {
                ps.setString(i + 1, playerIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID user = UUID.fromString(rs.getString(1));
                HashMap<String, Long> blocks = new HashMap<>();
                blocks.put("total", rs.getLong(2));
                blocks.put("used", rs.getLong(3));
                playerBlocks.put(user, blocks);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerBlocks;
    }


    public boolean updateClaimName(String claimId, String newName) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims SET claim_name = ? WHERE claim_id = ?")) {
            ps.setString(1, newName);
            ps.setString(2, claimId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public long hasNeededBlocks(OfflinePlayer player, long amount) {
        long pClaimBlocks = 0;
        long pClaimBlocksUsed = 0;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pClaimBlocks = rs.getLong(1);
                pClaimBlocksUsed = rs.getLong(2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        long pClaimBlocksLeft = pClaimBlocks - pClaimBlocksUsed;

        if(pClaimBlocksLeft < amount) {
            return -1;
        } else {
            return pClaimBlocksUsed + amount;
        }
    }

    public List<String> getChildren(String claimId) {
        List<String> childClaims = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id FROM claims WHERE parent_id = ?")) {
            ps.setString(1, claimId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                childClaims.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return childClaims;
    }
    public void deleteClaim(Player executorPlayer, OfflinePlayer targetPlayer, String claimName, RegionManager regionManager) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        List<String> claimIds = new ArrayList<>();

        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && hasPerm(executorPlayer) && CMI.getInstance().getPlayerManager().getUser(targetPlayer.getUniqueId()) == null) {
            claimIds.add(claimName);
        } else {
            claimIds = getClaimIdsFromNames(targetPlayer, claimName, Collections.singletonList("owner"));
        }
        if(!claimIds.isEmpty() && regionManager.hasRegion(claimIds.get(0))) {
            String claimId = claimIds.get(0);
            HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
            long claimBlocksUsed = (long) claimData.get("blocks");

            List<String> childClaims = getChildren(claimId);
            if(!childClaims.isEmpty()) {
                HashMap<String, HashMap<String, Object>> childClaimData = getClaimData(childClaims);

                for (String childClaim : childClaimData.keySet()) {
                    claimBlocksUsed += (long) childClaimData.get(childClaim).get("blocks");
                }
            }

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM claims WHERE claim_id = ? OR parent_id = ?")) {
                ps.setString(1, claimId);
                ps.setString(2, claimId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            long playerClaimBlocksUsed = getPlayersBlocks(Collections.singletonList(targetPlayer)).get(targetPlayer.getUniqueId()).get("used");
            long newClaimBlocksUsed = playerClaimBlocksUsed - claimBlocksUsed;

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
                ps.setLong(1, newClaimBlocksUsed);
                ps.setString(2, targetPlayer.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            regionManager.removeRegion(claimId);
            executorPlayer.sendMessage(prefix.append(Component.text("Successfully deleted the claim with the name ", TextColor.fromHexString("#20df80"))
                    .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

            if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId())) {
                Component targetMsg = prefix.append(Component.text("Your claim ", TextColor.fromHexString("#20df80"))).append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" was deleted by ", TextColor.fromHexString("#20df80"))).append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD));
                if(targetPlayer.isOnline()) {
                    Player tPlayer = targetPlayer.getPlayer();
                    Objects.requireNonNull(tPlayer).sendMessage(targetMsg);
                } else {
                    NotificationsUtils.createNotification("claim-delete", null, targetPlayer.getUniqueId().toString(), targetMsg, null, true);
                }
            }
        } else {
            executorPlayer.sendMessage(prefix.append(Component.text("Failed to find your claim on deletion! Contact an admin..", NamedTextColor.RED)));
        }
    }

    public void createClaim(Player player, String claimName, RegionManager regionManager, RegionSelector regionSelector, long playerClaimBlocks, long playerClaimBlocksUsed) {
        try {
            List<String> nameTaken = getClaimIdsFromNames(player, claimName, Collections.singletonList("owner"));
            if(nameTaken.isEmpty()) {
                String claimId = "claim_" + UUID.randomUUID();

                int minY = -64;
                int maxY = 319;

                if (plugin.customClaimHeight.containsKey(player.getUniqueId())) {
                    minY = regionSelector.getRegion().getMinimumPoint().getBlockY();
                    maxY = regionSelector.getRegion().getMaximumPoint().getBlockY();
                }

                ProtectedRegion region;
                long claimBlocks;

                if (plugin.customClaimShape.containsKey(player.getUniqueId())) {
                    Polygonal2DRegion regionSel = (Polygonal2DRegion) regionSelector.getRegion();
                    region = new ProtectedPolygonalRegion(claimId, regionSel.getPoints(), minY, maxY);
                    claimBlocks = new Polygonal2DRegion(BukkitAdapter.adapt(player.getWorld()), regionSel.getPoints(), 1, 1).getVolume();
                } else {
                    BlockVector3 p1 = regionSelector.getRegion().getMinimumPoint();
                    BlockVector3 p2 = regionSelector.getRegion().getMaximumPoint();
                    region = new ProtectedCuboidRegion(claimId, BlockVector3.at(p1.getBlockX(), minY, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), maxY, p2.getBlockZ()));
                    claimBlocks = new CuboidRegion(BlockVector3.at(p1.getBlockX(), 1, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), 1, p2.getBlockZ())).getVolume();
                }

                ProtectedRegion parentRegion = null;


                List<ProtectedRegion> regionOverlaps = region.getIntersectingRegions(regionManager.getRegions().values());
                if (regionOverlaps.size() > 0) {
                    List<String> overlapIds = new ArrayList<>();
                    for (ProtectedRegion overlapClaim : regionOverlaps) {
                        String overlapId = overlapClaim.getId();
                        if(overlapId.startsWith("claim_")) {
                            if(getClaimOwners(Collections.singletonList(overlapId)).get(overlapId).equals(player.getUniqueId())) {
                                if(overlapClaim.getParent() != null) {
                                    overlapIds.add(overlapClaim.getId());
                                    return;
                                } else {
                                    BlockVector3 regionMin = region.getMinimumPoint();
                                    BlockVector3 regionMax = region.getMaximumPoint();

                                    if (overlapClaim.contains(regionMax) && overlapClaim.contains(regionMin)) {
                                        parentRegion = overlapClaim;
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Your selection is partially outside the parent claim!", NamedTextColor.RED)));
                                        return;
                                    }
                                }
                            } else {
                                overlapIds.add(overlapId);
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Can't Expand! Claim would overlap an admin claim!", NamedTextColor.RED)));
                            return;
                        }
                    }
                    if(!overlapIds.isEmpty()) {
                        if (overlapIds.size() == 1) {
                            player.sendMessage(prefix.append(Component.text("Can't Expand! Claim would overlap this claim: ", NamedTextColor.RED))
                                    .append(Component.text(getClaimData(Collections.singletonList(overlapIds.get(0))).get(overlapIds.get(0)).get("name").toString()
                                            , NamedTextColor.RED, TextDecoration.BOLD)
                                            .hoverEvent(HoverEvent.showText(Component.text("Show Claim Info", NamedTextColor.GRAY)))
                                            .clickEvent(ClickEvent.callback(audience -> claimInfo(player, overlapIds.get(0))))));
                        } else {
                            claimOverlapMultiple(player, overlapIds);
                        }
                        return;
                    }
                }
                String parentId;
                if (parentRegion != null) {
                    region.setParent(parentRegion);
                    region.setPriority(2);
                    region.setFlags(parentRegion.getFlags());
                    parentId = parentRegion.getId();
                } else {
                    region.setPriority(1);
                    parentId = null;
                }

                if (claimBlocks >= 36 && region.getMinimumPoint().distance(region.getMaximumPoint()) > 8) {
                    if (playerClaimBlocksUsed + claimBlocks <= playerClaimBlocks) {
                        regionManager.addRegion(region);
                        region.getMembers().addPlayer(player.getUniqueId());
                        Map<Flag<?>, Object> map = Maps.newHashMap();
                        map.put(Flags.PVP, StateFlag.State.DENY);
                        map.put(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
                        map.put(Flags.TNT, StateFlag.State.DENY);
                        map.put(Flags.ENDER_BUILD, StateFlag.State.DENY);
                        map.put(Flags.FIRE_SPREAD, StateFlag.State.DENY);
                        map.put(Flags.LIGHTNING, StateFlag.State.DENY);
                        region.setFlags(map);

                        long newClaimBlocksUsed = playerClaimBlocksUsed + claimBlocks;
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
                            ps.setLong(1, newClaimBlocksUsed);
                            ps.setString(2, player.getUniqueId().toString());
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims (claim_id, claim_name, parent_id, world, blocks_used) VALUES (?, ?, ?, ?, ?)")) {
                            ps.setString(1, region.getId());
                            ps.setString(2, claimName);
                            ps.setString(3, parentId);
                            ps.setString(4, player.getWorld().getName());
                            ps.setLong(5, claimBlocks);
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?)")) {
                            ps.setString(1,player.getUniqueId().toString());
                            ps.setString(2, region.getId());
                            ps.setString(3, "owner");
                            ps.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        player.sendMessage(prefix.append(Component.text("Successfully created a claim with the name ", TextColor.fromHexString("#20df80"))
                                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
                    } else {
                        player.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this! You need ", NamedTextColor.RED))
                                .append(Component.text(playerClaimBlocksUsed + claimBlocks - playerClaimBlocks, NamedTextColor.RED, TextDecoration.BOLD)).append(Component.text(" blocks more", NamedTextColor.RED)));
                    }
                } else {
                    player.sendMessage(prefix.append(Component.text("Selected area is too small! Claims must be atleast 6x6x6 blocks in size.", NamedTextColor.RED)));
                }
            } else {
                player.sendMessage(prefix.append(Component.text("You already have a claim with that name!", NamedTextColor.RED)));
            }
        } catch (final Exception e){
            e.printStackTrace();
        }
    }
    public void expandClaimMultiple(Player executorPlayer, OfflinePlayer targetPlayer, List<String> claimIds, int amount, BlockFace playerFacing) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Expand ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMultiple expandable claims found! Pick one: ", NamedTextColor.GRAY));

        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

        for(String claimId : claimsData.keySet()) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimsData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3")))
                    .append(claimsData.get(claimId).get("parent") != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to expand " + claimsData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> expandClaim(executorPlayer, targetPlayer, claimId, amount, claimsData.get(claimId).get("parent") != null, playerFacing))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }
    public void expandClaim(Player executorPlayer, OfflinePlayer targetPlayer, String claimId, int amount, boolean isChild, BlockFace playerFacing) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        if(playerFacing.isCartesian() && !playerFacing.equals(BlockFace.UP) && !playerFacing.equals(BlockFace.DOWN)) {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(executorPlayer.getWorld()));
            if(regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(claimId);
                if(region != null) {
                    if(region instanceof ProtectedCuboidRegion) {
                        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);

                        BlockVector3 p1 = region.getMinimumPoint();
                        BlockVector3 p2 = region.getMaximumPoint();
                        switch (playerFacing) {
                            case NORTH -> p1 = p1.subtract(0, 0, amount);
                            case SOUTH -> p2 = p2.add(0, 0, amount);
                            case WEST -> p1 = p1.subtract(amount, 0, 0);
                            case EAST -> p2 = p2.add(amount, 0, 0);
                        }
                        ProtectedRegion expandedRegion = new ProtectedCuboidRegion(claimId, p1, p2);

                        if (isChild) {
                            String parentId = (String) claimData.get("parent");
                            ProtectedRegion parentRegion = regionManager.getRegion(parentId);
                            if (parentRegion != null) {
                                if (!parentRegion.contains(p1) || !parentRegion.contains(p2)) {
                                    executorPlayer.sendMessage(prefix.append(Component.text("Can't expand a child claim to be outside of the parent claim!", NamedTextColor.RED)));
                                    return;
                                }
                            }
                        }

                        List<ProtectedRegion> regionOverlaps = expandedRegion.getIntersectingRegions(regionManager.getRegions().values());
                        if (regionOverlaps.size() > 0) {
                            List<String> overlapIds = new ArrayList<>();
                            for (ProtectedRegion overlapClaim : regionOverlaps) {
                                String overlapId = overlapClaim.getId();
                                if(overlapId.startsWith("claim_")) {
                                    if(!overlapClaim.equals(region)) {
                                        if (isChild && !Objects.equals(overlapClaim, region.getParent())) {
                                            overlapIds.add(overlapId);
                                        } else if (!isChild && !Objects.equals(overlapClaim.getParent(), region)) {
                                            overlapIds.add(overlapId);
                                        }
                                    }
                                } else {
                                    executorPlayer.sendMessage(prefix.append(Component.text("Can't Expand! Claim would overlap an admin claim!", NamedTextColor.RED)));
                                    return;
                                }
                            }
                            if(!overlapIds.isEmpty()) {
                                if (overlapIds.size() == 1) {
                                    executorPlayer.sendMessage(prefix.append(Component.text("Can't Expand! Claim would overlap this claim: ", NamedTextColor.RED))
                                            .append(Component.text(getClaimData(Collections.singletonList(overlapIds.get(0))).get(overlapIds.get(0)).get("name").toString()
                                                            , NamedTextColor.RED, TextDecoration.BOLD)
                                                    .hoverEvent(HoverEvent.showText(Component.text("Show Claim Info", NamedTextColor.GRAY)))
                                                    .clickEvent(ClickEvent.callback(audience -> claimInfo(executorPlayer, overlapIds.get(0))))));
                                } else {
                                    claimOverlapMultiple(executorPlayer, overlapIds);
                                }
                                return;
                            }
                        }

                        long currentClaimBlocks = new CuboidRegion(region.getMinimumPoint().withY(1), region.getMaximumPoint().withY(1)).getVolume();
                        long expandedClaimBlocks = new CuboidRegion(expandedRegion.getMinimumPoint().withY(1), expandedRegion.getMaximumPoint().withY(1)).getVolume();

                        long additionalBlocksUsed = expandedClaimBlocks - currentClaimBlocks;

                        HashMap<String, Long> pClaimBlocks = getPlayersBlocks(Collections.singletonList(targetPlayer)).get(targetPlayer.getUniqueId());

                        long blocksLeft = pClaimBlocks.get("total") - pClaimBlocks.get("used");

                        if(blocksLeft - additionalBlocksUsed >= 0) {
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = claim_blocks_used + ? WHERE user_id = ?")) {
                                ps.setLong(1, additionalBlocksUsed);
                                ps.setString(2, targetPlayer.getUniqueId().toString());
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }


                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims SET blocks_used = blocks_used + ? WHERE claim_id = ?")) {
                                ps.setLong(1, additionalBlocksUsed);
                                ps.setString(2, claimId);
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            expandedRegion.copyFrom(region);
                            regionManager.addRegion(expandedRegion);

                            executorPlayer.sendMessage(prefix.append(Component.text("The claim ", TextColor.fromHexString("#20df80")).append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                                            .append(Component.text(" has successfully been expanded by ", TextColor.fromHexString("#20df80"))).append(Component.text(amount, TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD)))
                                    .append(Component.text(" blocks " + playerFacing.name(), TextColor.fromHexString("#20df80"))));
                        } else {
                            executorPlayer.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this! You need ")
                                    .append(Component.text(additionalBlocksUsed - blocksLeft, Style.style(TextDecoration.BOLD))).append(Component.text(" blocks more", NamedTextColor.RED))));
                        }
                    } else {
                        executorPlayer.sendMessage(prefix.append(Component.text("Can't expand custom shaped claims!", NamedTextColor.RED)));
                    }
                }
            }
        } else {
            executorPlayer.sendMessage(prefix.append(Component.text("Can't expand custom shaped claims!", NamedTextColor.RED)));
        }
    }

    public void helpMessage(Player player, int page) {
        boolean hasPerm = player.hasPermission("skyprisoncore.command.claim.admin");
        Component msg = Component.text("");
                msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" SkyPrison Claims ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        if (page == 1) {
            msg = msg
                .append(Component.text("\n/claim list " + (hasPerm ? "(player) (page)" :  "(page)"), TextColor.fromHexString("#20df80"))).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("List of all claims you're in", TextColor.fromHexString("#dbb976")))

                .append(Component.text("\n/claim info (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Get info about a claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim blocks " + (hasPerm ? "buy/give/take/set <player> <amount>" : "buy <amount>"), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Buy more claimblocks", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim create <claim>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Create a new claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim delete <claim>" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("delete a claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim flags (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("View/edit flags", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim invite <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Invite a player to your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim kick <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Kick a member from your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim wand", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Get the tool used for claiming", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim nearby <radius>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Get a list of nearby claims", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n" + page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                .append(Component.text("2", TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->", NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/claim help 2"))).decorate(TextDecoration.BOLD));
        } else if (page == 2) {
            msg = msg
                .append(Component.text("\n/claim promote <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Promote a member of your claim", TextColor.fromHexString("#dbb976")))).decoration(TextDecoration.STRIKETHROUGH, false)

                .append(Component.text("\n/claim demote <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Demote a co-owner of your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim transfer <claim> <player>" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Transfer claim to another player", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim rename <claim> <newName>" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Rename your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim expand <amount>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Expand a claim in the direction you are facing", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim pending (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("See all pending invites/transfers", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim customheight", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Enable/disable custom height on claim create", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim customshape", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Enable/disable custom shape on claim create.", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.runCommand("/claim help 1")).append(Component.text(page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                .append(Component.text("2", TextColor.fromHexString("#266d27"))))).decorate(TextDecoration.BOLD));
        }

        msg = msg.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(msg);
    }

    public boolean hasPerm(Player player) {
        return player.hasPermission("skyprisoncore.command.claim.admin");
    }

    public void claimListAll(Player executorPlayer, int page) {
        if(!hasPerm(executorPlayer)) return;
        Component msg = Component.empty();
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claims List ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        List<String> claimIds = new ArrayList<>();

        long totalBlocksUsed = 0;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, blocks_used FROM claims")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                claimIds.add(rs.getString(1));
                totalBlocksUsed += rs.getLong(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        msg = msg.append(Component.text("\nTotal Blocks In Use", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)
                .append(Component.text(totalBlocksUsed, TextColor.fromHexString("#ffba75")))));
        if (!claimIds.isEmpty()) {
            HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

            int totalPages = (int) Math.ceil((double) claimsData.size() / 10);

            if (page > totalPages) {
                page = 1;
            }

            List<String> claimsToShow = new ArrayList<>(claimsData.keySet());

            int todelete = 10 * (page - 1);
            if (todelete != 0) {
                claimsToShow = claimsToShow.subList(todelete, claimsToShow.size());
            }
            int i = 0;

            for (String claimId : claimsToShow) {
                if (i == 10) break;
                String name = claimsData.get(claimId).get("name").toString();
                long blocksUsed = (long) claimsData.get(claimId).get("blocks");

                List<Integer> coords = getClaimCoords(claimId);

                Component parentInfo = Component.text("");
                if (claimsData.get(claimId).get("parent") != null) {
                    String parentId = (String) claimsData.get(claimId).get("parent");
                    String parentName = getClaimData(Collections.singletonList(parentId)).get(parentId).get("name").toString();

                    parentInfo = Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(parentName, TextColor.fromHexString("#ffba75")));
                }
                msg = msg.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(name, TextColor.fromHexString("#0fffc3"))
                                .append(claimsData.get(claimId).get("parent") != null ? Component.text(" (Child)", TextColor.fromHexString("#ffba75")) : Component.text("")))
                        .hoverEvent(HoverEvent.showText(Component.text("").append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))
                                .append(parentInfo)
                                .append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                        .append(Component.text("X " + coords.get(0) + " Y " + coords.get(1), TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(blocksUsed, TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nClick for more info", NamedTextColor.GRAY))
                                .append(Component.text("\n⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))))
                        .clickEvent(ClickEvent.callback(audience -> claimInfo(executorPlayer, claimId))));
                i++;
            }

            int nextPage = page + 1;
            int prevPage = page - 1;

            if (page == 1 && page != totalPages) {
                msg = msg.append(Component.text("\n" + page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                        .append(Component.text(totalPages, TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->", NamedTextColor.GRAY)
                        .hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/claim list " + nextPage))));
            } else if (page != 1 && page == totalPages) {
                msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page, TextColor.fromHexString("#266d27"))
                                .append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27"))))));
            } else if (page != 1) {
                msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page, TextColor.fromHexString("#266d27"))
                                .append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27")))))
                        .append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))))
                        .clickEvent(ClickEvent.runCommand("/claim list " + nextPage)));
            }

        }
        msg = msg.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(msg);
    }

    public void claimList(Player executorPlayer, OfflinePlayer targetPlayer, int page) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        Component msg = Component.empty();
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claims List ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        HashMap<String, String> userClaims = getAllUserClaims(targetPlayer, Arrays.asList("owner", "co-owner", "member"));

        HashMap<String, Long> pBlocks =  getPlayerBlocks(targetPlayer);

        msg = msg.append(Component.text("\nTotal Blocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)
                        .append(Component.text(pBlocks.get("used") + "/" + pBlocks.get("total"), TextColor.fromHexString("#ffba75")))));
        if (!userClaims.isEmpty()) {
            List<String> claimsIds = userClaims.keySet().stream().toList();

            HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimsIds);

            int totalPages = (int) Math.ceil((double) claimsData.size() / 10);

            if (page > totalPages) {
                page = 1;
            }

            List<String> claimsToShow = new ArrayList<>(claimsData.keySet());

            int todelete = 10 * (page - 1);
            if (todelete != 0) {
                claimsToShow = claimsToShow.subList(todelete, claimsToShow.size());
            }
            int i = 0;

            boolean ownClaims = !Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && hasPerm(executorPlayer);

            for (String claimId : claimsToShow) {
                if (i == 10) break;
                String name = claimsData.get(claimId).get("name").toString();
                String userRank = WordUtils.capitalize(userClaims.get(claimId));
                long blocksUsed = (long) claimsData.get(claimId).get("blocks");

                List<Integer> coords = getClaimCoords(claimId);

                Component parentInfo = Component.text("");
                if (claimsData.get(claimId).get("parent") != null) {
                    String parentId = (String) claimsData.get(claimId).get("parent");
                    String parentName = getClaimData(Collections.singletonList(parentId)).get(parentId).get("name").toString();

                    parentInfo = Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(parentName, TextColor.fromHexString("#ffba75")));
                }
                msg = msg.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(name, TextColor.fromHexString("#0fffc3"))
                                .append(claimsData.get(claimId).get("parent") != null ? Component.text(" (Child)", TextColor.fromHexString("#ffba75")) : Component.text("")))
                        .hoverEvent(HoverEvent.showText(Component.text("").append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))
                                .append(!ownClaims ? Component.text("\nRank", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                        .append(Component.text(userRank, TextColor.fromHexString("#ffba75"))) : Component.empty())
                                .append(parentInfo)
                                .append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                        .append(Component.text("X " + coords.get(0) + " Y " + coords.get(1), TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(blocksUsed, TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nClick for more info", NamedTextColor.GRAY))
                                .append(Component.text("\n⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))))
                        .clickEvent(ClickEvent.callback(audience -> claimInfo(executorPlayer, claimId))));
                i++;
            }

            int nextPage = page + 1;
            int prevPage = page - 1;

            if (page == 1 && page != totalPages) {
                msg = msg.append(Component.text("\n" + page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                        .append(Component.text(totalPages, TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->", NamedTextColor.GRAY)
                        .hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/claim list " + nextPage))));
            } else if (page != 1 && page == totalPages) {
                msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page, TextColor.fromHexString("#266d27"))
                                .append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27"))))));
            } else if (page != 1) {
                msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page, TextColor.fromHexString("#266d27"))
                                .append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27")))))
                        .append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))))
                        .clickEvent(ClickEvent.runCommand("/claim list " + nextPage)));
            }

        }
        msg = msg.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(msg);
    }

    public void claimOverlapMultiple(Player player, List<String> claimIds) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Overlaps ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nCan't expand! Claim would overlap these claims: ", NamedTextColor.GRAY));

        HashMap<String, UUID> ownersData = getClaimOwners(claimIds);
        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

        for(String claimId : ownersData.keySet()) {
            String  ownerName = getOfflineName(ownersData.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimsData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to show info for " + claimsData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void claimInfoMultiple(Player player, List<String> claimIds) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Info ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ", NamedTextColor.GRAY));

        HashMap<String, UUID> ownersData = getClaimOwners(claimIds);
        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

        for(String claimId : ownersData.keySet()) {
            String ownerName = getOfflineName(ownersData.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimsData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3")))
                    .append(claimsData.get(claimId).get("parent") != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to show info for " + claimsData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }


    public List<Integer> getClaimCoords(String claimId) {
        List<Integer> coords = new ArrayList<>();
        HashMap<String, HashMap<String, Object>> claimData = getClaimData(Collections.singletonList(claimId));
        if(!claimData.isEmpty()) {
            final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld((String) claimData.get(claimId).get("world")))));
            if(regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(claimId);
                if(region != null) {
                    coords.add(region.getPoints().get(0).getBlockX());
                    coords.add(region.getPoints().get(0).getBlockZ());
                }
            }
        }
        return coords;
    }


    public String getOfflineName(UUID playerUUID) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT current_name FROM users WHERE user_id = ?")) {
            ps.setString(1, playerUUID.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void claimInfo(Player player, String claimId) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Info ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        String claimName = (String) claimData.get("name");
        String parentId = (String) claimData.get("parent");
        long blocksUsed = (long) claimData.get("blocks");

        info = info.append(Component.text("\nName", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                .append(Component.text(claimName, TextColor.fromHexString("#ffba75"))));

        if(parentId != null && !parentId.isEmpty()) {
            String parentName = (String) getClaimData(Collections.singletonList(parentId)).get(parentId).get("name");
            info = info.append(Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(parentName, TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("View parent info", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, parentId))));
        }
        HashMap<String, HashMap<UUID, String>> claimsMembers = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner", "member"));
        if(!claimsMembers.isEmpty()) {
            HashMap<UUID, String> members = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner", "member")).get(claimId);
            boolean canEdit = members.containsKey(player.getUniqueId()) && (members.get(player.getUniqueId()).equalsIgnoreCase("owner") || members.get(player.getUniqueId()).equalsIgnoreCase("co-owner"));
            if(hasPerm(player)) canEdit = true;

            info = info.append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(blocksUsed, TextColor.fromHexString("#ffba75"))));

            info = info.append(Component.text("\nOwner", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(Objects.requireNonNull(getOfflineName(getClaimOwner(claimId))), TextColor.fromHexString("#ffba75"))));

            List<Integer> coords = getClaimCoords(claimId);

            info = info.append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text("X " + coords.get(0) + " Y " + coords.get(1), TextColor.fromHexString("#ffba75"))));

            info = info.append(Component.text("\n\nVIEW MEMBERS", TextColor.fromHexString("#0fffc3"))
                    .hoverEvent(HoverEvent.showText(Component.text("View members", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> player.openInventory(new ClaimMembers(plugin, db, claimName, members, "", 1).getInventory())))
                    .decorate(TextDecoration.BOLD));

            boolean finalCanEdit = canEdit;
            info = info.append(Component.text("\nVIEW FLAGS", TextColor.fromHexString("#0fffc3"))
                    .hoverEvent(HoverEvent.showText(Component.text("View flags", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> player.openInventory(new ClaimFlags(plugin, claimId, claimData.get("world").toString(), finalCanEdit, "", 1).getInventory())))
                    .decorate(TextDecoration.BOLD));

            info = info.decoration(TextDecoration.ITALIC, false);
        }
        player.sendMessage(info);
    }

    public void invitePlayerMultiple(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser iUser, List<String> claimIds) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Invite ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ", NamedTextColor.GRAY));

        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);

        for(String claimId : claimData.keySet()) {
            String ownerName = getOfflineName(owners.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3")))
                    .append(claimData.get(claimId).get("parent") != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to invite player to " + claimData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> invitePlayer(executorPlayer, targetPlayer, iUser, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }

    public void invitePlayer(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser iUser, String claimId) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        String notifId = UUID.randomUUID().toString();
        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");
        Component msg = prefix.append(Component.text("You've been invited to the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)))
                .append(Component.text("\nACCEPT INVITE", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim accept invite " + notifId)))
                .append(Component.text("     "))
                .append(Component.text("DECLINE INVITE", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim decline invite " + notifId)));
        NotificationsUtils.createNotification("claim-invite", claimId, iUser.getOfflinePlayer().toString(), msg, notifId, false);


        if (iUser.isOnline()) {
            iUser.getPlayer().sendMessage(msg);
            executorPlayer.sendMessage(prefix.append(Component.text("Successfully invited " + iUser.getName() + " to the claim!", TextColor.fromHexString("#20df80"))));
        } else {
            executorPlayer.sendMessage(prefix.append(Component.text("Successfully invited " + iUser.getName() + " to the claim! They'll get an invite once they're online.", TextColor.fromHexString("#20df80"))));
        }
    }

    public void inviteDecline(Player player, String claimId, String notifId) {
        if(!NotificationsUtils.hasNotification(notifId, player).isEmpty()) {
            String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");
            HashMap<UUID, String> toNotify = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner")).get(claimId);

            NotificationsUtils.deleteNotification(notifId);

            player.sendMessage(prefix.append(Component.text("You've successfully declined the invite to join the claim ", TextColor.fromHexString("#20df80"))
                    .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

            for (UUID pUUID : toNotify.keySet()) {
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(pUUID);
                Component msg = prefix.append(Component.text("Player ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" has REJECTED the invite to join the claim ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
                if (oPlayer.isOnline()) {
                    Objects.requireNonNull(oPlayer.getPlayer()).sendMessage(msg);
                } else {
                    NotificationsUtils.createNotification("claim-invite-declined", claimId, pUUID.toString(), msg, null, true);
                }
            }
        }
    }

    public void inviteAccept(Player player, String claimId, String notifId) {
        if(!NotificationsUtils.hasNotification(notifId, player).isEmpty()) {
            HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
            final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld((String) claimData.get("world")))));
            assert regionManager != null;
            Objects.requireNonNull(regionManager.getRegion(claimId)).getMembers().addPlayer(player.getUniqueId());
            String claimName = (String) claimData.get("name");
            HashMap<UUID, String> toNotify = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner")).get(claimId);

            NotificationsUtils.deleteNotification(notifId);


            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?)")) {
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, claimId);
                ps.setString(3, "member");
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            player.sendMessage(prefix.append(Component.text("You've successfully joined the claim ", TextColor.fromHexString("#20df80"))
                    .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

            for (UUID pUUID : toNotify.keySet()) {
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(pUUID);
                Component msg = prefix.append(Component.text("Player ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" has accepted the invite to join the claim ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
                if (oPlayer.isOnline()) {
                    Objects.requireNonNull(oPlayer.getPlayer()).sendMessage(msg);
                } else {
                    NotificationsUtils.createNotification("claim-invite-accepted", claimId, pUUID.toString(), msg, null, true);
                }
            }
        }
    }

    public void kickPlayerMultiple(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser kickedPlayer, List<String> claimIds) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Kick ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to kick player from: ", NamedTextColor.GRAY));

        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);


        for(String claimId : claimData.keySet()) {
            String ownerName = getOfflineName(owners.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3")))
                    .append(claimData.get(claimId).get("parent") != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to kick player from " + claimData.get(claimId).get("name"), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> kickPlayer(executorPlayer, targetPlayer, kickedPlayer, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }

    public void kickPlayer(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser kickedPlayer, String claimId) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld((String) claimData.get("world")))));
        assert regionManager != null;
        Objects.requireNonNull(regionManager.getRegion(claimId)).getMembers().removePlayer(kickedPlayer.getUniqueId());

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM claims_members WHERE claim_id = ? AND user_id = ?")) {
            ps.setString(1, claimId);
            ps.setString(2, kickedPlayer.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        executorPlayer.sendMessage(prefix.append(Component.text("Successfully kicked ", TextColor.fromHexString("#20df80"))
                .append(Component.text(kickedPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" from the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("You've been kicked from the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
        if(kickedPlayer.isOnline()) {
            kickedPlayer.getPlayer().sendMessage(msg);
        } else {
            NotificationsUtils.createNotification("claim-kick", claimId, kickedPlayer.getUniqueId().toString(), msg, null, true);
        }
    }

    public void promotePlayer(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser promotedPlayer, String claimId) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
            ps.setString(1, "co-owner");
            ps.setString(2, promotedPlayer.getUniqueId().toString());
            ps.setString(3, claimId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String claimName = getClaimData(Collections.singletonList(claimId)).get(claimId).get("name").toString();
        Component msg = prefix.append(Component.text("You've been promoted in the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));

        executorPlayer.sendMessage(prefix.append(Component.text("Successfully promoted ", TextColor.fromHexString("#20df80"))
                .append(Component.text(promotedPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" in the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        if(promotedPlayer.isOnline()) {
            promotedPlayer.getPlayer().sendMessage(msg);
        } else {
            NotificationsUtils.createNotification("claim-promote", claimId, promotedPlayer.getUniqueId().toString(), msg, null, true);
        }
    }

    public void demotePlayer(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser demotedPlayer, String claimId) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
            ps.setString(1, "member");
            ps.setString(2, demotedPlayer.getUniqueId().toString());
            ps.setString(3, claimId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String claimName = getClaimData(Collections.singletonList(claimId)).get(claimId).get("name").toString();
        Component msg = prefix.append(Component.text("You've been demoted in the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));

        executorPlayer.sendMessage(prefix.append(Component.text("Successfully demoted ", TextColor.fromHexString("#20df80"))
                .append(Component.text(demotedPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" in the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        if(demotedPlayer.isOnline()) {
            demotedPlayer.getPlayer().sendMessage(msg);
        } else {
            NotificationsUtils.createNotification("claim-demote", claimId, demotedPlayer.getUniqueId().toString(), msg, null, true);
        }
    }

    public void transferClaim(Player executorPlayer, OfflinePlayer targetPlayer, CMIUser transferPlayer, String claimId) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        long claimBlocks = (long) claimData.get("blocks");
        List<String> childClaims = getChildren(claimId);
        if(!getChildren(claimId).isEmpty()) {
            HashMap<String, HashMap<String, Object>> childData = getClaimData(childClaims);
            for(String child : childData.keySet()) {
                claimBlocks += (long) childData.get(child).get("blocks");
            }
        }

        HashMap<UUID, HashMap<String, Long>> pBlocks = getPlayersBlocks(Collections.singletonList(transferPlayer.getOfflinePlayer()));
        if(!pBlocks.isEmpty()) {
            HashMap<String, Long> tBlocks = pBlocks.get(transferPlayer.getUniqueId());
            long pClaimBlocks = tBlocks.get("total");
            long pClaimBlocksUsed = tBlocks.get("used");

            long pClaimBlocksLeft = pClaimBlocks - pClaimBlocksUsed;

            if (claimBlocks > 0 && claimBlocks < pClaimBlocksLeft) {
                String notifId = UUID.randomUUID().toString();
                Component msg = prefix.append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                                .append(Component.text(" wants to transfer the claim ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                .append(Component.text(" to you!", TextColor.fromHexString("#20df80"))))
                        .append(!childClaims.isEmpty() ? Component.text("\nThis will also transfer all child claims to you!", NamedTextColor.GRAY) : Component.empty())
                        .append(Component.text("\nACCEPT TRANSFER", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim accept transfer " + notifId)))
                        .append(Component.text("     "))
                        .append(Component.text("DECLINE TRANSFER", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim decline transfer " + notifId)));
                if (transferPlayer.isOnline()) {
                    transferPlayer.getPlayer().sendMessage(msg);
                    executorPlayer.sendMessage(prefix.append(Component.text("Successfully sent a transfer request to " + transferPlayer.getName() + "!", TextColor.fromHexString("#20df80"))));
                } else {
                    executorPlayer.sendMessage(prefix.append(Component.text("Successfully created transfer request! " + transferPlayer.getName() + " will receive the request when they log on. ", TextColor.fromHexString("#20df80"))));
                }
                NotificationsUtils.createNotification("claim-transfer", claimId, transferPlayer.getUniqueId().toString(), msg, notifId, false);
            } else {
                executorPlayer.sendMessage(prefix.append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD)
                        .append(Component.text(" doesn't have enough claim blocks!", NamedTextColor.RED))));
            }
        } else {
            executorPlayer.sendMessage(prefix.append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text(" doesn't have enough claim blocks!", NamedTextColor.RED))));
        }
    }

    public void transferDecline(Player transferPlayer, String claimId, String notifId) {
        if(!NotificationsUtils.hasNotification(notifId, transferPlayer).isEmpty()) {
            NotificationsUtils.deleteNotification(notifId);

            String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");

            transferPlayer.sendMessage(prefix.append(Component.text("You've successfully declined the transfer to become the owner of the claim ", TextColor.fromHexString("#20df80"))
                    .append(Component.text(claimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
            Component msg = prefix.append(Component.text("Player ", NamedTextColor.RED)
                    .append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD))
                    .append(Component.text(" has declined the transfer request to become the owner of the claim ", NamedTextColor.RED))
                    .append(Component.text(claimName, NamedTextColor.RED, TextDecoration.BOLD)));

            UUID owner = getClaimOwners(Collections.singletonList(claimId)).get(claimId);

            if (Bukkit.getPlayer(owner) != null) {
                Objects.requireNonNull(Bukkit.getPlayer(owner)).sendMessage(msg);
            } else {
                NotificationsUtils.createNotification("claim-transfer-declined", claimId, owner.toString(), msg, null, true);
            }
        }
    }

    public void transferAccept(Player transferPlayer, String claimId, String notifId) {
        if(!NotificationsUtils.hasNotification(notifId, transferPlayer).isEmpty()) {
            NotificationsUtils.deleteNotification(notifId);
            HashMap<String, HashMap<String, Object>> claims = getClaimData(Collections.singletonList(claimId));
            if (!claims.isEmpty()) {
                HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
                long claimBlocks = (long) claimData.get("blocks");

                List<String> childClaims = getChildren(claimId);
                List<String> childTaken = new ArrayList<>();
                if(!getChildren(claimId).isEmpty()) {
                    HashMap<String, HashMap<String, Object>> childData = getClaimData(childClaims);
                    for(String child : childData.keySet()) {
                        claimBlocks += (int) childData.get(child).get("blocks");
                        if(!getClaimIdsFromNames(transferPlayer, childData.get(child).get("name").toString(), Collections.singletonList("owner")).isEmpty()) {
                            childTaken.add(child);
                        }
                    }
                }

                String claimName = (String) claimData.get("name");
                long pBlocks = hasNeededBlocks(transferPlayer, claimBlocks);
                if (pBlocks >= 0) {
                    String oldClaimName = claimName;
                    List<String> nameTaken = getClaimIdsFromNames(transferPlayer, claimName, Collections.singletonList("owner"));
                    if (!nameTaken.isEmpty()) {
                        int leftLimit = 97; // letter 'a'
                        int rightLimit = 122; // letter 'z'
                        int targetStringLength = 10;
                        Random random = new Random();
                        claimName = random.ints(leftLimit, rightLimit + 1)
                                .limit(targetStringLength)
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString();
                        updateClaimName(claimId, claimName);
                    }

                    if(!childTaken.isEmpty()) {
                        for(String child : childTaken) {
                            int leftLimit = 97; // letter 'a'
                            int rightLimit = 122; // letter 'z'
                            int targetStringLength = 10;
                            Random random = new Random();
                            claimName = random.ints(leftLimit, rightLimit + 1)
                                    .limit(targetStringLength)
                                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                    .toString();
                            updateClaimName(child, claimName);
                        }
                    }


                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
                        ps.setLong(1, pBlocks);
                        ps.setString(2, transferPlayer.getUniqueId().toString());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    UUID owner = getClaimOwners(Collections.singletonList(claimId)).get(claimId);
                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = claim_blocks_used - ? WHERE user_id = ?")) {
                        ps.setLong(1, claimBlocks);
                        ps.setString(2, owner.toString());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
                        ps.setString(1, "owner");
                        ps.setString(2, transferPlayer.getUniqueId().toString());
                        ps.setString(3, claimId);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
                        ps.setString(1, "co-owner");
                        ps.setString(2, owner.toString());
                        ps.setString(3, claimId);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if(!childClaims.isEmpty()) {
                        for(String child : childClaims) {
                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE user_rank = VALUE(user_rank)")) {
                                ps.setString(1, transferPlayer.getUniqueId().toString());
                                ps.setString(2, child);
                                ps.setString(3, "owner");
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
                                ps.setString(1, "co-owner");
                                ps.setString(2, owner.toString());
                                ps.setString(3, child);
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                    if (!nameTaken.isEmpty()) {
                        transferPlayer.sendMessage(prefix.append(Component.text("You already own a claim with the name ", TextColor.fromHexString("#20df80"))
                                .append(Component.text(oldClaimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                .append(Component.text("! Renaming transferred claim to ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(claimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                    }
                    transferPlayer.sendMessage(prefix.append(Component.text("You're now the owner of the claim ", TextColor.fromHexString("#20df80"))
                            .append(Component.text(claimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)))
                            .append(!childClaims.isEmpty() ? Component.text(" and all of its child claims!", TextColor.fromHexString("#ffba75")) : Component.empty()));

                    Component msg = prefix.append(Component.text("Your claim ", TextColor.fromHexString("#20df80"))
                            .append(Component.text(oldClaimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                            .append(!childClaims.isEmpty() ? Component.text(" and all of its child claims", TextColor.fromHexString("#20df80")) : Component.empty())
                            .append(Component.text(" was successfully transferred to ", TextColor.fromHexString("#20df80")))
                            .append(Component.text(transferPlayer.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));
                    if (Bukkit.getPlayer(owner) != null) {
                        Objects.requireNonNull(Bukkit.getPlayer(owner)).sendMessage(msg);
                    } else {
                        NotificationsUtils.createNotification("claim-transfer-accepted", claimId, owner.toString(), msg, null, true);
                    }
                } else {
                    transferPlayer.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this claim transfer! Cancelling transfer..", NamedTextColor.RED)));
                }
            } else {
                transferPlayer.sendMessage(prefix.append(Component.text("Couldn't find the claim to transfer! Cancelling transfer..", NamedTextColor.RED)));
            }
        }
    }

    public void claimFlagsMultiple(Player executorPlayer, OfflinePlayer targetPlayer, HashMap<String, String> userRanks) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Promote ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to promote the player in: ", NamedTextColor.GRAY));
        List<String> claimIds = userRanks.keySet().stream().toList();
        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);

        for(String claimId : claimData.keySet()) {
            String ownerName = getOfflineName(owners.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3")))
                    .append(claimData.get(claimId).get("parent") != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to view flags for " + claimData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.callback(audience -> claimFlags(executorPlayer, targetPlayer, claimId, claimData.get(claimId).get("world").toString(), userRanks.get(claimId)))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }

    public void claimFlags(Player executorPlayer, OfflinePlayer targetPlayer, String claimId, String world, String userRank) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer.getUniqueId()) && !hasPerm(executorPlayer)) return;
        boolean canEdit = userRank.equalsIgnoreCase("owner") || userRank.equalsIgnoreCase("co-owner");
        if(hasPerm(executorPlayer)) canEdit = true;
        ClaimFlags claimFlags = new ClaimFlags(plugin, claimId, world, canEdit, "", 1);
        executorPlayer.openInventory(claimFlags.getInventory());
    }

    public HashMap<String, HashMap<UUID, String>> getClaimUsers(List<String> claimIds, List<String> ranks) {
        HashMap<String, HashMap<UUID, String>> userClaims = new HashMap<>();
        if(claimIds != null) {
            if(!claimIds.isEmpty()) {
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members WHERE claim_id IN "
                        + SkyPrisonCore.getQuestionMarks(claimIds) + " AND user_rank IN " + SkyPrisonCore.getQuestionMarks(ranks))) {
                    int b = 0;
                    for (int i = 0; i < claimIds.size() + ranks.size(); i++) {
                        if(i < claimIds.size()) {
                            ps.setString(i + 1, claimIds.get(i));
                        } else {
                            ps.setString(i + 1, ranks.get(b));
                            b++;
                        }
                    }
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String claimId = rs.getString(1);
                        UUID user = UUID.fromString(rs.getString(2));
                        HashMap<UUID, String> userData = new HashMap<>();
                        if(userClaims.containsKey(claimId)) userData = userClaims.get(claimId);
                        userData.put(user, rs.getString(3));
                        userClaims.put(claimId, userData);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members WHERE user_rank IN "
                        + SkyPrisonCore.getQuestionMarks(ranks))) {
                    for (int i = 0; i < claimIds.size() + ranks.size(); i++) {
                        ps.setString(i + 1, ranks.get(i));
                    }
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String claimId = rs.getString(1);
                        UUID user = UUID.fromString(rs.getString(2));
                        HashMap<UUID, String> userData = new HashMap<>();
                        if(userClaims.containsKey(claimId)) userData = userClaims.get(claimId);
                        userData.put(user, rs.getString(3));
                        userClaims.put(claimId, userData);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return userClaims;
    }

    public HashMap<String, String> getAllUserClaims(OfflinePlayer player, List<String> ranks) {
        HashMap<UUID, HashMap<String, String>> userClaims = getAllUserClaims(Collections.singletonList(player), ranks);
        if(!userClaims.isEmpty()) {
            return userClaims.get(player.getUniqueId());
        }
        return new HashMap<>();
    }

    public HashMap<UUID, HashMap<String, String>> getAllUserClaims(List<OfflinePlayer> players, List<String> ranks) {
        HashMap<UUID, HashMap<String, String>> userClaims = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN "
                + SkyPrisonCore.getQuestionMarks(playerIds) + " AND user_rank IN " + SkyPrisonCore.getQuestionMarks(ranks))) {
            int b = 0;
            for (int i = 0; i < playerIds.size() + ranks.size(); i++) {
                if(i < playerIds.size()) {
                    ps.setString(i + 1, playerIds.get(i));
                } else {
                    ps.setString(i + 1, ranks.get(b));
                    b++;
                }
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID user = UUID.fromString(rs.getString(1));
                HashMap<String, String> claims = new HashMap<>();
                if(userClaims.containsKey(user)) claims = userClaims.get(user);
                claims.put(rs.getString(2), rs.getString(3));
                userClaims.put(user, claims);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userClaims;
    }

    public HashMap<String, String> getUserClaims(Location loc, OfflinePlayer player, List<String> claimIds, RegionManager regionManager, List<String> ranks, boolean getLocationRegardless) {
        HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(loc, Collections.singletonList(player), claimIds, regionManager, ranks, getLocationRegardless);
        if(!userClaims.isEmpty()) {
            return userClaims.get(player.getUniqueId());
        }
        return new HashMap<>();
    }

    public HashMap<UUID, HashMap<String, String>> getUserClaims(Location loc, List<OfflinePlayer> players, List<String> claimIds, RegionManager regionManager, List<String> ranks, boolean getLocationRegardless) {
        HashMap<UUID, HashMap<String, String>> userClaims = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        if(!claimIds.isEmpty()) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN "
                    + SkyPrisonCore.getQuestionMarks(playerIds) + " AND claim_id IN " + SkyPrisonCore.getQuestionMarks(claimIds) + " AND user_rank IN " + SkyPrisonCore.getQuestionMarks(ranks))) {
                int b = 0;
                int x = 0;
                for (int i = 0; i < playerIds.size() + claimIds.size() + ranks.size(); i++) {
                    if(i < playerIds.size()) {
                        ps.setString(i + 1, playerIds.get(i));
                    } else if(b < claimIds.size()) {
                        ps.setString(i + 1, claimIds.get(b));
                        b++;
                    } else {
                        ps.setString(i + 1, ranks.get(x));
                        x++;
                    }
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    UUID user = UUID.fromString(rs.getString(1));
                    HashMap<String, String> claims = new HashMap<>();
                    if(userClaims.containsKey(user)) claims = userClaims.get(user);
                    claims.put(rs.getString(2), rs.getString(3));
                    userClaims.put(user, claims);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            if(players.size() == 1) {
                OfflinePlayer player = players.get(0);
                ApplicableRegionSet regionList = regionManager.getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                if (!regionList.getRegions().isEmpty()) {
                    List<String> regionIds = new ArrayList<>();
                    for (final ProtectedRegion rg : regionList) {
                        if (rg.getId().startsWith("claim_")) {
                            regionIds.add(rg.getId());
                        }
                    }
                    if (!regionIds.isEmpty()) {
                        if(!getLocationRegardless) {
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN "
                                    + SkyPrisonCore.getQuestionMarks(playerIds) + " AND claim_id IN " + SkyPrisonCore.getQuestionMarks(regionIds) + " AND user_rank IN " + SkyPrisonCore.getQuestionMarks(ranks))) {
                                int b = 0;
                                int x = 0;
                                for (int i = 0; i < playerIds.size() + regionIds.size() + ranks.size(); i++) {
                                    if(i < playerIds.size()) {
                                        ps.setString(i + 1, playerIds.get(i));
                                    } else if(b < regionIds.size()) {
                                        ps.setString(i + 1, regionIds.get(b));
                                        b++;
                                    } else {
                                        ps.setString(i + 1, ranks.get(x));
                                        x++;
                                    }
                                }
                                ResultSet rs = ps.executeQuery();
                                while (rs.next()) {
                                    UUID user = UUID.fromString(rs.getString(1));
                                    HashMap<String, String> claims = new HashMap<>();
                                    if (userClaims.containsKey(user)) claims = userClaims.get(user);
                                    claims.put(rs.getString(2), rs.getString(3));
                                    userClaims.put(user, claims);
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else {
                            HashMap<String, HashMap<UUID, String>> claims = getClaimUsers(regionIds, Arrays.asList("owner", "co-owner", "member"));
                            HashMap<String, String> relevantClaims = new HashMap<>();
                            claims.keySet().forEach(claim -> relevantClaims.put(claim, claims.get(claim).getOrDefault(player.getUniqueId(), "none")));
                            userClaims.put(player.getUniqueId(), relevantClaims);
                        }
                    }
                }
            }
        }
        return userClaims;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(sender instanceof Player player) {
            final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
            assert regionManager != null;
            final RegionSelector regionSelector = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player)).getRegionSelector(BukkitAdapter.adapt(player.getWorld()));
            long claimBlocks = 0;
            long claimBlocksUsed = 0;
            List<String> claimIds = new ArrayList<>();

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    claimBlocks = rs.getLong(1);
                    claimBlocksUsed = rs.getLong(2);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "delete" -> { // /claim delete <claimname> (player)
                        if (args.length > 1) {
                            claimIds = getClaimIdsFromNames(player, args[1], Collections.singletonList("owner"));
                            if(hasPerm(player)) {
                                if(args.length > 2) {
                                    CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                    if(tUser != null) {
                                        claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[1], Collections.singletonList("owner"));
                                        if(!claimIds.isEmpty()) {
                                            String claimId = claimIds.get(0);
                                            plugin.deleteClaim.add(player.getUniqueId());
                                            Component msg = prefix.append(Component.text("Are you sure you want to delete the claim ", TextColor.fromHexString("#20df80"))
                                                            .append(Component.text((String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name"), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                                            .append(Component.text("?", TextColor.fromHexString("#20df80"))))
                                                    .append(Component.text("\nDELETE CLAIM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                        if (plugin.deleteClaim.contains(player.getUniqueId())) {
                                                            plugin.deleteClaim.remove(player.getUniqueId());
                                                            deleteClaim(player, tUser.getOfflinePlayer(), args[1], regionManager);
                                                        }
                                                    })))
                                                    .append(Component.text("     "))
                                                    .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                        plugin.deleteClaim.remove(player.getUniqueId());
                                                        audience.sendMessage(prefix.append(Component.text("Claim deletion cancelled!", NamedTextColor.GRAY)));
                                                    })));
                                            player.sendMessage(msg);
                                            return true;
                                        } else {
                                            player.sendMessage(notFound);
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                    }
                                } else {
                                    if(regionManager.hasRegion(args[1])) {
                                        String claimId = claimIds.get(0);
                                        plugin.deleteClaim.add(player.getUniqueId());
                                        Component msg = prefix.append(Component.text("Are you sure you want to delete the claim ", TextColor.fromHexString("#20df80"))
                                                        .append(Component.text((String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name"), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                                        .append(Component.text("?", TextColor.fromHexString("#20df80")))
                                                        .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)))
                                                .append(Component.text("\nDELETE CLAIM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                    if(plugin.deleteClaim.contains(player.getUniqueId())) {
                                                        plugin.deleteClaim.remove(player.getUniqueId());
                                                        deleteClaim(player, Bukkit.getOfflinePlayer(UUID.randomUUID()), args[1], regionManager);
                                                    }
                                                })))
                                                .append(Component.text("     "))
                                                .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                    plugin.deleteClaim.remove(player.getUniqueId());
                                                    audience.sendMessage(prefix.append(Component.text("Claim deletion cancelled!", NamedTextColor.GRAY)));
                                                })));
                                        player.sendMessage(msg);
                                        return true;
                                    } else {
                                        player.sendMessage(notFound);
                                    }
                                }
                            } else {
                                if (!claimIds.isEmpty()) {
                                    String claimId = claimIds.get(0);
                                    plugin.deleteClaim.add(player.getUniqueId());
                                    Component msg = prefix.append(Component.text("Are you sure you want to delete the claim ", TextColor.fromHexString("#20df80"))
                                                    .append(Component.text((String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name"), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                                    .append(Component.text("?", TextColor.fromHexString("#20df80")))
                                                    .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)))
                                            .append(Component.text("\nDELETE CLAIM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                if (plugin.deleteClaim.contains(player.getUniqueId())) {
                                                    plugin.deleteClaim.remove(player.getUniqueId());
                                                    deleteClaim(player, player, args[1], regionManager);
                                                }
                                            })))
                                            .append(Component.text("     "))
                                            .append(Component.text("CANCEL DELETION", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                plugin.deleteClaim.remove(player.getUniqueId());
                                                audience.sendMessage(prefix.append(Component.text("Claim deletion cancelled!", NamedTextColor.GRAY)));
                                            })));
                                    player.sendMessage(msg);
                                    return true;
                                } else {
                                    player.sendMessage(notFound);
                                }
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Correct usage: /claim delete <claimname>", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
                        }
                    }
                    case "create" -> {
                        if (args.length >= 2 && args[1] != null) {
                            if (player.getWorld().getName().equalsIgnoreCase("world_free")) {
                                if (regionSelector.isDefined()) {
                                    createClaim(player, args[1], regionManager, regionSelector, claimBlocks, claimBlocksUsed);
                                } else {
                                    player.sendMessage(prefix.append(Component.text("You havn't created a selection!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Claiming is not allowed in this world!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Correct usage: /claim create <claimname>", NamedTextColor.RED)));
                        }
                    }
                    case "list" -> { // claim list (player / page)
                        int page = 1;
                        if(args.length > 1) {
                            if (plugin.isInt(args[1])) {
                                page = Integer.parseInt(args[1]);
                                claimList(player, player, page);
                            } else {
                                if(hasPerm(player)) {
                                    CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                    if(tUser != null) {
                                        if(args.length > 2 && plugin.isInt(args[2])) {
                                            page = Integer.parseInt(args[2]);
                                            claimList(player, tUser.getOfflinePlayer(), page);
                                        } else {
                                            claimList(player, tUser.getOfflinePlayer(), 1);
                                        }
                                    } else if(args[1].equalsIgnoreCase("all")) {
                                        claimListAll(player, 1);
                                    }
                                } else {
                                    claimList(player, player, 1);
                                }
                            }
                        } else {
                            claimList(player, player, page);
                        }
                    }
                    case "customheight" -> {
                        if(plugin.customClaimHeight.containsKey(player.getUniqueId())) {
                            plugin.customClaimHeight.remove(player.getUniqueId());
                            player.sendMessage(prefix.append(Component.text("Custom height claiming disabled!", NamedTextColor.RED)));
                        } else {
                            plugin.customClaimHeight.put(player.getUniqueId(), true);
                            player.sendMessage(prefix.append(Component.text("Custom height claiming enabled!", NamedTextColor.GREEN)));
                        }
                    }
                    case "customshape" -> {
                        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
                        final RegionSelector newSelector;
                        if(plugin.customClaimShape.containsKey(player.getUniqueId())) {
                            plugin.customClaimShape.remove(player.getUniqueId());
                            newSelector = new CuboidRegionSelector(regionSelector);
                            player.sendMessage(prefix.append(Component.text("Custom shape claiming disabled!", NamedTextColor.RED)));
                        } else {
                            plugin.customClaimShape.put(player.getUniqueId(), true);
                            newSelector = new Polygonal2DRegionSelector(regionSelector);
                            player.sendMessage(prefix.append(Component.text("Custom shape claiming enabled!", NamedTextColor.GREEN)));
                        }
                        session.setRegionSelector(BukkitAdapter.adapt(player.getWorld()), newSelector);
                    }
                    case "info" -> { // claim info (claim) (player)
                        CMIUser tUser = null;
                        if(args.length > 1) {
                            if(hasPerm(player) && regionManager.hasRegion(args[1])) {
                                claimIds.add(args[1]);
                            } else {
                                if(args.length > 2 && hasPerm(player)) {
                                    tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                    if(tUser != null) {
                                        claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[1], Arrays.asList("owner", "co-owner", "member"));
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                    }
                                } else {
                                    claimIds = getClaimIdsFromNames(player, args[1], Arrays.asList("owner", "co-owner", "member"));
                                }
                            }
                        }
                        HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member"), true);
                        if(!userClaims.isEmpty()) {
                            claimIds = userClaims.keySet().stream().toList();
                            if(claimIds.size() == 1) {
                                claimInfo(player, claimIds.get(0));
                            } else {
                                claimInfoMultiple(player, claimIds);
                            }
                        } else {
                            player.sendMessage(notFound);
                        }
                    }
                    case "invite" -> { // claim invite <player> (claim) (player)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                CMIUser tUser = null;
                                if(args.length > 2) {
                                    if(hasPerm(player) && regionManager.hasRegion(args[2])) {
                                        claimIds.add(args[1]);
                                    } else {
                                        if(args.length > 3 && hasPerm(player)) {
                                            tUser = CMI.getInstance().getPlayerManager().getUser(args[3]);
                                            if(tUser != null) {
                                                claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[2], Arrays.asList("owner", "co-owner"));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            claimIds = getClaimIdsFromNames(player, args[2], Arrays.asList("owner", "co-owner"));
                                        }
                                    }
                                }
                                HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner"), false);

                                HashMap<String, String> alreadyMember = getUserClaims(player.getLocation(), iUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member", "banned"), false);
                                if (!userClaims.isEmpty()) {
                                    claimIds = new ArrayList<>(userClaims.keySet());
                                    claimIds.removeAll(alreadyMember.keySet());
                                    if (!claimIds.isEmpty()) {
                                        List<String> alreadyInvited = NotificationsUtils.hasNotifications("claim-invite", claimIds, iUser.getOfflinePlayer());
                                        claimIds.removeAll(alreadyInvited);
                                        if(!claimIds.isEmpty()) {
                                            if (claimIds.size() == 1) {
                                                invitePlayer(player, tUser == null ? player : tUser.getOfflinePlayer(),  iUser, claimIds.get(0));
                                            } else {
                                                invitePlayerMultiple(player, tUser == null ? player : tUser.getOfflinePlayer(), iUser, claimIds);
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("The player already has an invite to this claim!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        if(alreadyMember.size() == 1) {
                                            if (alreadyMember.containsValue("banned")) {
                                                player.sendMessage(prefix.append(Component.text("The player is banned from this claim!", NamedTextColor.RED)));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("The player is already in this claim!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("The player is either banned or already in those claims!", NamedTextColor.RED)));
                                        }
                                    }
                                } else {
                                    player.sendMessage(notFound);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim invite <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "kick" -> { // claim kick <player> (claim) (player)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                CMIUser tUser = null;
                                if(args.length > 2) {
                                    if(hasPerm(player) && regionManager.hasRegion(args[2])) {
                                        claimIds.add(args[1]);
                                    } else {
                                        if(args.length > 3 && hasPerm(player)) {
                                            tUser = CMI.getInstance().getPlayerManager().getUser(args[3]);
                                            if(tUser != null) {
                                                claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[2], Arrays.asList("owner", "co-owner"));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            claimIds = getClaimIdsFromNames(player, args[2], Arrays.asList("owner", "co-owner"));
                                        }
                                    }
                                }
                                HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner"), false);
                                HashMap<String, String> isMember = getUserClaims(player.getLocation(), iUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member"),false);

                                if(!userClaims.isEmpty()) {
                                    if(!isMember.isEmpty()) {
                                        if (isMember.size() == 1) {
                                            String claimId = isMember.keySet().stream().toList().get(0);
                                            if(isMember.containsValue("member") || userClaims.get(claimId).equalsIgnoreCase("owner")) {
                                                kickPlayer(player, tUser == null ? player : tUser.getOfflinePlayer(), iUser, claimId);
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("You can't kick this player!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            List<String> kickableClaimIds = new ArrayList<>();
                                            userClaims.keySet().forEach(claimId -> {
                                                if(isMember.containsKey(claimId) && !isMember.get(claimId).equalsIgnoreCase("owner") && !isMember.get(claimId).equalsIgnoreCase("co-owner")) {
                                                    kickableClaimIds.add(claimId);
                                                } else {
                                                    kickableClaimIds.add(claimId);
                                                }
                                            });
                                            if(!kickableClaimIds.isEmpty()) {
                                                kickPlayerMultiple(player, tUser == null ? player : tUser.getOfflinePlayer(), iUser, kickableClaimIds);
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("You can't kick this player!", NamedTextColor.RED)));
                                            }
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(notFound);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim kick <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "promote" -> { // claim promote <player> (claim) (player)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                CMIUser tUser = null;
                                if(args.length > 2) {
                                    if(hasPerm(player) && regionManager.hasRegion(args[2])) {
                                        claimIds.add(args[1]);
                                    } else {
                                        if(args.length > 3 && hasPerm(player)) {
                                            tUser = CMI.getInstance().getPlayerManager().getUser(args[3]);
                                            if(tUser != null) {
                                                claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[2], List.of("owner"));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            claimIds = getClaimIdsFromNames(player, args[2], List.of("owner"));
                                        }
                                    }
                                }
                                HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    claimIds = userClaims.keySet().stream().toList();
                                    HashMap<String, String> promoteClaims = getUserClaims(player.getLocation(), iUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member"), true);
                                    if(promoteClaims.isEmpty()) {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                        return true;
                                    }

                                    String claimId = promoteClaims.keySet().stream().toList().get(0);

                                    ProtectedRegion region = null;
                                    if(promoteClaims.size() > 1) {
                                        for(String userClaim : promoteClaims.keySet()) {
                                            ProtectedRegion claimRegion = regionManager.getRegion(userClaim);
                                            assert claimRegion != null;
                                            if(region == null) {
                                                region = claimRegion;
                                            } else if(claimRegion.getPriority() > region.getPriority()) {
                                                region = claimRegion;
                                            }
                                        }
                                        claimId = region.getId();
                                    }

                                    String userRank = promoteClaims.get(claimId);
                                    if (userRank != null && userRank.equalsIgnoreCase("member")) {
                                        promotePlayer(player, tUser == null ? player : tUser.getOfflinePlayer(), iUser, claimId);
                                    } else if(userRank == null) {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("This player has already been promoted!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(notFound);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim promote <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "demote" -> { // claim demote <player> (claim) (player)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                CMIUser tUser = null;
                                if(args.length > 2) {
                                    if(hasPerm(player) && regionManager.hasRegion(args[2])) {
                                        claimIds.add(args[1]);
                                    } else {
                                        if(args.length > 3 && hasPerm(player)) {
                                            tUser = CMI.getInstance().getPlayerManager().getUser(args[3]);
                                            if(tUser != null) {
                                                claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[2], List.of("owner"));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            claimIds = getClaimIdsFromNames(player, args[2], List.of("owner"));
                                        }
                                    }
                                }
                                HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    claimIds = userClaims.keySet().stream().toList();
                                    HashMap<String, String> demoteClaims = getUserClaims(player.getLocation(), iUser.getOfflinePlayer(), claimIds,
                                            regionManager, Arrays.asList("owner", "co-owner", "member"), false);
                                    if(demoteClaims.isEmpty()) {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                        return true;
                                    }
                                    String claimId = demoteClaims.keySet().stream().toList().get(0);

                                    ProtectedRegion region = null;
                                    if(demoteClaims.size() > 1) {
                                        for(String userClaim : demoteClaims.keySet()) {
                                            ProtectedRegion claimRegion = regionManager.getRegion(userClaim);
                                            assert claimRegion != null;
                                            if(region == null) {
                                                region = claimRegion;
                                            } else if(claimRegion.getPriority() > region.getPriority()) {
                                                region = claimRegion;
                                            }
                                        }
                                        claimId = region.getId();
                                    }

                                    String userRank = demoteClaims.get(claimId);
                                    if (userRank != null && userRank.equalsIgnoreCase("co-owner")) {
                                        demotePlayer(player, tUser == null ? player : tUser.getOfflinePlayer(), iUser, claimId);
                                    } else if(userRank == null) {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("This player can't be demoted!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(notFound);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim demote <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "transfer" -> { // claim transfer <player> (claim) (player)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                CMIUser tUser;
                                if(args.length > 2) {
                                    if(hasPerm(player) && regionManager.hasRegion(args[2])) {
                                        tUser = null;
                                        claimIds.add(args[1]);
                                    } else {
                                        if(args.length > 3 && hasPerm(player)) {
                                            tUser = CMI.getInstance().getPlayerManager().getUser(args[3]);
                                            if(tUser != null) {
                                                claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[2], List.of("owner"));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                            }
                                        } else {
                                            tUser = null;
                                            claimIds = getClaimIdsFromNames(player, args[2], List.of("owner"));
                                        }
                                    }
                                } else {
                                    tUser = null;
                                }
                                HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    claimIds = userClaims.keySet().stream().toList();
                                    HashMap<String, String> transferClaims = getUserClaims(player.getLocation(), iUser.getOfflinePlayer(), claimIds,
                                            regionManager, List.of("co-owner"), false);
                                    if(!transferClaims.isEmpty()) {
                                        String claimId = transferClaims.keySet().stream().toList().get(0);

                                        for (String claim : transferClaims.keySet()) {
                                            ProtectedRegion rg = regionManager.getRegion(claim);
                                            assert rg != null;
                                            if(rg.getParent() != null) {
                                                player.sendMessage(prefix.append(Component.text("Can't transfer individual child claims!", NamedTextColor.RED)));
                                                return true;
                                            }
                                        }

                                        if(claimId != null) {
                                            plugin.transferClaim.add(player.getUniqueId());
                                            Component msg = prefix.append(Component.text("Are you sure you want to transfer the claim ", TextColor.fromHexString("#20df80"))
                                                            .append(Component.text((String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name"), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                                            .append(Component.text(" to ", TextColor.fromHexString("#20df80")))
                                                            .append(Component.text(iUser.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                                            .append(Component.text("?", TextColor.fromHexString("#20df80"))))
                                                    .append(!getChildren(claimId).isEmpty() ? Component.text("\nThis will also transfer all child claims!", NamedTextColor.GRAY) : Component.empty())
                                                    .append(Component.text("\nTRANSFER CLAIM", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                        if(plugin.transferClaim.contains(player.getUniqueId())) {
                                                            plugin.transferClaim.remove(player.getUniqueId());
                                                            transferClaim(player, tUser == null ? player : tUser.getOfflinePlayer(), iUser, claimId);
                                                        }
                                                    })))
                                                    .append(Component.text("     "))
                                                    .append(Component.text("CANCEL TRANSFER", NamedTextColor.GRAY, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                                                        plugin.transferClaim.remove(player.getUniqueId());
                                                        audience.sendMessage(prefix.append(Component.text("Claim transfer cancelled!", NamedTextColor.GRAY)));
                                                    })));
                                            player.sendMessage(msg);
                                        } else {
                                            player.sendMessage(notFound);
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player must be a promoted member of the claim!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(notFound);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim transfer <player> <claim>", NamedTextColor.RED)));
                        }
                    }
                    case "flags" -> { // claim flags (claim) (player)
                        CMIUser tUser = null;
                        if(args.length > 1) {
                            if(hasPerm(player) && regionManager.hasRegion(args[1])) {
                                claimIds.add(args[1]);
                            } else {
                                if(args.length > 3 && hasPerm(player)) {
                                    tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                    if(tUser != null) {
                                        claimIds = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[1], List.of("owner", "co-owner", "member"));
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                    }
                                } else {
                                    claimIds = getClaimIdsFromNames(player, args[1], List.of("owner", "co-owner", "member"));
                                }
                            }
                        }
                        HashMap<String, String> userClaims = getUserClaims(player.getLocation(), tUser == null ? player : tUser.getOfflinePlayer(), claimIds, regionManager, List.of("owner", "co-owner", "member"), true);

                        if(!userClaims.isEmpty()) {
                            if(userClaims.size() == 1) {
                                String claimId = userClaims.keySet().stream().toList().get(0);
                                String world = getClaimData(Collections.singletonList(claimId)).get(claimId).get("world").toString();
                                claimFlags(player, tUser == null ? player : tUser.getOfflinePlayer(), claimId, world, userClaims.get(claimId));
                            } else {
                                claimFlagsMultiple(player, tUser == null ? player : tUser.getOfflinePlayer(), userClaims);
                            }
                        } else {
                            player.sendMessage(notFound);
                        }
                    }
                    case "wand" -> player.performCommand("/wand");
                    case "blocks" -> { // claim blocks buy/give/set/take <player> <amount>
                        if(args.length > 1) {
                            switch(args[1].toLowerCase()) {
                                case "buy" -> {
                                    if(plugin.isInt(args[2])) {
                                        long blocks = Integer.parseInt(args[2]);
                                        double price = 40 * blocks;
                                        if(user.getBalance() >= price) {
                                            user.withdraw(price);

                                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                                                ps.setLong(1, blocks);
                                                ps.setString(2, user.getUniqueId().toString());
                                                ps.executeUpdate();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }

                                            player.sendMessage(prefix.append(Component.text("Successfully bought ", TextColor.fromHexString("#20df80"))
                                                    .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                    .append(Component.text(" blocks for $", TextColor.fromHexString("#20df80")))
                                                    .append(Component.text(plugin.formatNumber(price), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                                        } else {
                                            long needed = (long) (price - user.getBalance());
                                            player.sendMessage(prefix.append(Component.text("You don't have enough money! You need $" + plugin.formatNumber(needed) + " more..", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                                    }
                                }
                                case "give" -> {
                                    if(hasPerm(player)) {
                                        CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                        if (tUser != null) {
                                            if (plugin.isInt(args[3])) {
                                                long blocks = Integer.parseInt(args[3]);

                                                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                                                    ps.setLong(1, blocks);
                                                    ps.setString(2, tUser.getUniqueId().toString());
                                                    ps.executeUpdate();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }

                                                player.sendMessage(prefix.append(Component.text("Successfully gave ", TextColor.fromHexString("#20df80"))
                                                        .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                        .append(Component.text(" blocks to ", TextColor.fromHexString("#20df80")))
                                                        .append(Component.text(tUser.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));

                                                Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                        .append(Component.text(" has given you ", TextColor.fromHexString("#20df80")))
                                                        .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                        .append(Component.text(" blocks!", TextColor.fromHexString("#20df80")));

                                                if(tUser.isOnline()) {
                                                    tUser.getPlayer().sendMessage(msg);
                                                } else {
                                                    NotificationsUtils.createNotification("claim-give", null, tUser.getUniqueId().toString(), msg, null, true);
                                                }
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks give <player> <amount>", NamedTextColor.RED)));
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                                    }
                                }
                                case "set" -> { // /claim blocks set <player> <amount>
                                    if(hasPerm(player)) {
                                        CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                        if(tUser != null) {
                                            if (plugin.isInt(args[3])) {
                                                long blocks = Integer.parseInt(args[3]);

                                                HashMap<String, Long> tBlocksData = getPlayerBlocks(tUser.getOfflinePlayer());
                                                long tBlocks = tBlocksData.get("used");
                                                if(tBlocks >= 0) {
                                                    if(blocks - tBlocks >= 0) {
                                                        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = ? WHERE user_id = ?")) {
                                                            ps.setLong(1, blocks);
                                                            ps.setString(2, tUser.getUniqueId().toString());
                                                            ps.executeUpdate();
                                                        } catch (SQLException e) {
                                                            e.printStackTrace();
                                                        }

                                                        player.sendMessage(prefix.append(Component.text("Successfully set ", TextColor.fromHexString("#20df80"))
                                                                        .append(Component.text(tUser.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)))
                                                                .append(Component.text(" blocks to ", TextColor.fromHexString("#20df80")))
                                                                .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));

                                                        Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                                .append(Component.text(" set your blocks to ", TextColor.fromHexString("#20df80")))
                                                                .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                                .append(Component.text("!", TextColor.fromHexString("#20df80")));
                                                        if (tUser.isOnline()) {
                                                            tUser.getPlayer().sendMessage(msg);
                                                        } else {
                                                            NotificationsUtils.createNotification("claim-set", null, tUser.getUniqueId().toString(), msg, null, true);
                                                        }
                                                    } else {
                                                        player.sendMessage(prefix.append(Component.text("This would put the player's total blocks below their used blocks!", NamedTextColor.RED)));
                                                    }
                                                } else {
                                                    player.sendMessage(prefix.append(Component.text("Couldn't get that player's claim blocks!", NamedTextColor.RED)));
                                                }
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks take <plyer> <amount>", NamedTextColor.RED)));
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                                    }
                                }
                                case "take" -> {
                                    if(hasPerm(player)) {
                                        CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                        if(tUser != null) {
                                            if (plugin.isInt(args[3])) {
                                                long blocks = Integer.parseInt(args[3]);

                                                long tLeft = hasNeededBlocks(tUser.getOfflinePlayer(), blocks);
                                                if(tLeft >= 0) {
                                                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks - ? WHERE user_id = ?")) {
                                                        ps.setLong(1, blocks);
                                                        ps.setString(2, tUser.getUniqueId().toString());
                                                        ps.executeUpdate();
                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                    }

                                                    player.sendMessage(prefix.append(Component.text("Successfully took ", TextColor.fromHexString("#20df80"))
                                                            .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                            .append(Component.text(" blocks from ", TextColor.fromHexString("#20df80")))
                                                            .append(Component.text(tUser.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));

                                                    Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                            .append(Component.text(" took ", TextColor.fromHexString("#20df80")))
                                                            .append(Component.text(plugin.formatNumber(blocks), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                                            .append(Component.text(" blocks from you!", TextColor.fromHexString("#20df80")));

                                                    if(tUser.isOnline()) {
                                                        tUser.getPlayer().sendMessage(msg);
                                                    } else {
                                                        NotificationsUtils.createNotification("claim-take", null, tUser.getUniqueId().toString(), msg, null, true);
                                                    }
                                                } else {
                                                    player.sendMessage(prefix.append(Component.text("Player doesn't have enough claim blocks!", NamedTextColor.RED)));
                                                }
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks take <plyer> <amount>", NamedTextColor.RED)));
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                                    }
                                }
                                default -> player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim blocks buy <amount>", NamedTextColor.RED)));
                        }
                    }
                    case "expand" -> { // claim expand <amount>
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                boolean getLoc = hasPerm(player);
                                HashMap<String, String> userClaims = getUserClaims(player.getLocation(), player, claimIds, regionManager, List.of("owner"), getLoc);

                                if (!userClaims.isEmpty()) {
                                    List<String> claims = userClaims.keySet().stream().toList();
                                    OfflinePlayer tPlayer = Bukkit.getOfflinePlayer(getClaimOwner(claims.get(0)));
                                    if (claims.size() == 1) {
                                        expandClaim(player, tPlayer, claims.get(0), Integer.parseInt(args[1]), false, player.getFacing());
                                    } else {
                                        expandClaimMultiple(player, tPlayer, claims, Integer.parseInt(args[1]), player.getFacing());
                                    }
                                } else {
                                    player.sendMessage(notFound);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! Amount must be a number!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim expand <amount>", NamedTextColor.RED)));

                        }
                    }
                    case "rename" -> { // claim rename <current> <new> (player)
                        if(args.length > 2) {
                            List<String> nameTaken = new ArrayList<>();
                            CMIUser tUser = null;
                            if(args.length > 3 && hasPerm(player)) {
                                tUser = CMI.getInstance().getPlayerManager().getUser(args[3]);
                                if(tUser != null) {
                                    nameTaken = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[1], Collections.singletonList("owner"));
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                }
                            } else {
                                nameTaken = getClaimIdsFromNames(player, args[1], Collections.singletonList("owner"));
                            }
                            if(!nameTaken.isEmpty()) {
                                if(updateClaimName(getClaimIdsFromNames(tUser == null ? player : tUser.getOfflinePlayer(), args[1], Collections.singletonList("owner")).get(0), args[2])) {
                                    player.sendMessage(prefix.append(Component.text("Successfully renamed the claim from ", TextColor.fromHexString("#20df80"))
                                            .append(Component.text(args[1], TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                                            .append(Component.text(" to ", TextColor.fromHexString("#20df80")))
                                            .append(Component.text(args[2], TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Something went wrong during renaming! Try again later..", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("You don't own any claims with the name ", NamedTextColor.RED)
                                        .append(Component.text(args[1], NamedTextColor.RED, TextDecoration.BOLD))));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim rename <curren name> <new name>", NamedTextColor.RED)));
                        }
                    }
                    case "nearby" -> { // claim nearby <radius>
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                int radius = Integer.parseInt(args[1]);
                                if(radius <= 200) {
                                    int pX = player.getLocation().getBlockX();
                                    int pZ = player.getLocation().getBlockZ();
                                    ProtectedCuboidRegion scanRegion = new ProtectedCuboidRegion("nearby_scan", true,
                                            BlockVector3.at(pX - radius, -64, pZ - radius),
                                            BlockVector3.at(pX + radius, 319, pZ + radius));
                                    List<ProtectedRegion> regions = scanRegion.getIntersectingRegions(regionManager.getRegions().values());
                                    if(!regions.isEmpty()) {
                                        List<String> regionIds = new ArrayList<>();
                                        regions.forEach(region -> {
                                            if(region.getId().startsWith("claim_")) {
                                                regionIds.add(region.getId());
                                            }
                                        });
                                        claimInfoMultiple(player, regionIds);
                                    } else {
                                        player.sendMessage(notFound);
                                    }
                                } else {
                                    if(hasPerm(player)) {
                                        int pX = player.getLocation().getBlockX();
                                        int pZ = player.getLocation().getBlockZ();
                                        ProtectedCuboidRegion scanRegion = new ProtectedCuboidRegion("nearby_scan", true,
                                                BlockVector3.at(pX - radius, -64, pZ - radius),
                                                BlockVector3.at(pX + radius, 319, pZ + radius));
                                        List<ProtectedRegion> regions = scanRegion.getIntersectingRegions(regionManager.getRegions().values());
                                        if(!regions.isEmpty()) {
                                            List<String> regionIds = new ArrayList<>();
                                            regions.forEach(region -> {
                                                if(region.getId().startsWith("claim_")) {
                                                    regionIds.add(region.getId());
                                                }
                                            });
                                            claimInfoMultiple(player, regionIds);
                                        } else {
                                            player.sendMessage(notFound);
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Incorrect Usage! Max radius is 200 blocks.", NamedTextColor.RED)));
                                    }
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim nearby <radius>", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim nearby <radius>", NamedTextColor.RED)));
                        }
                    }
                    case "pending" -> { // /claim pending (claim) (player)
                        if(args.length > 1) {
                            List<String> claims = getClaimIdsFromNames(player, args[1], Arrays.asList("owner", "co-owner"));
                            if(args[1].equalsIgnoreCase("all") && hasPerm(player)) {
                                if(args.length > 2 && hasPerm(player)) {
                                    CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                    if(tUser != null) {
                                        claims = getAllUserClaims(tUser.getOfflinePlayer(), Arrays.asList("owner", "co-owner")).keySet().stream().toList();
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                        return true;
                                    }
                                } else {
                                    claims = new ArrayList<>();
                                    try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id FROM claims")) {
                                        ps.setString(1, player.getUniqueId().toString());
                                        ResultSet rs = ps.executeQuery();
                                        while (rs.next()) {
                                            claims.add(rs.getString(1));
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }else if(args.length > 2 && hasPerm(player)) {
                                CMIUser tUser = CMI.getInstance().getPlayerManager().getUser(args[2]);
                                if(tUser != null) {
                                    claims = getClaimIdsFromNames(tUser.getOfflinePlayer(), args[1], Arrays.asList("owner", "co-owner"));
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Player not found!", NamedTextColor.RED)));
                                    return true;
                                }
                            }


                            if(!claims.isEmpty()) {
                                player.openInventory(new ClaimPending(plugin, getClaimData(claims), "", 1).getInventory());
                            } else {
                                player.sendMessage(notFound);
                            }
                        } else {
                            player.openInventory(new ClaimPending(plugin, getClaimData(getAllUserClaims(player, Arrays.asList("owner", "co-owner")).keySet().stream().toList()), "", 1).getInventory());
                        }
                    }
                    default -> {
                        if (args.length > 1) { // /claim accept invite <id>
                            if(args.length == 3) {
                                String claimId = NotificationsUtils.hasNotification(args[2], player);
                                if(!claimId.isEmpty()) {
                                    boolean state = args[0].equalsIgnoreCase("accept");
                                    switch (args[1].toLowerCase()) {
                                        case "invite" -> {
                                            if (state) {
                                                inviteAccept(player, claimId, args[2]);
                                            } else {
                                                inviteDecline(player, claimId, args[2]);
                                            }
                                            return true;
                                        }
                                        case "transfer" -> {
                                            if (state) {
                                                transferAccept(player, claimId, args[2]);
                                            } else {
                                                transferDecline(player, claimId, args[2]);
                                            }
                                            return true;
                                        }
                                    }
                                }
                            }
                            if (plugin.isInt(args[1])) {
                                helpMessage(player, Integer.parseInt(args[1]));
                            } else {
                                helpMessage(player, 1);
                            }
                        } else {
                            helpMessage(player, 1);
                        }
                    }
                }
            } else {
                helpMessage(player, 1);
            }
        }
        return true;
    }
}
