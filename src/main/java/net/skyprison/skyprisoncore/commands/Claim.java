package net.skyprison.skyprisoncore.commands;

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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.ClaimFlags;
import net.skyprison.skyprisoncore.inventories.ClaimMembers;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
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

    public final Component prefix = Component.text("Claims", TextColor.fromHexString("#0fc3ff")).append(Component.text(" | ", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false);

    public Claim(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }
    public HashMap<String, UUID> getClaimOwners(List<String> claimIds) {
        HashMap<String, UUID> userUUIDs = new HashMap<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id FROM claims_members WHERE claim_id IN (" + getQuestionMarks(claimIds) + ") AND user_rank = ?")) {
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
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, claim_name, parent_id, world, blocks_used FROM claims WHERE claim_id IN (" + getQuestionMarks(claimIds) + ")")) {
            for (int i = 0; i < claimIds.size(); i++) {
                ps.setString(i + 1, claimIds.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> claimInfo = new HashMap<>();
                claimInfo.put("name", rs.getString(2));
                claimInfo.put("parent", rs.getString(3));
                claimInfo.put("world", rs.getString(4));
                claimInfo.put("blocks", rs.getInt(5));
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
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_name, claim_id FROM claims WHERE claim_id IN (SELECT claim_id FROM claims_members WHERE user_id IN ("
                + getQuestionMarks(playerIds) + ") AND user_rank IN (" + getQuestionMarks(ranks) + "))")) {
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

    public HashMap<UUID, HashMap<String, Integer>> getPlayersBlocks(List<OfflinePlayer> players) {
        HashMap<UUID, HashMap<String, Integer>>  playerBlocks = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_blocks, claim_blocks_used FROM users WHERE user_id IN (" + getQuestionMarks(playerIds) + ")")) {
            for (int i = 0; i < playerIds.size(); i++) {
                ps.setString(i + 1, playerIds.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID user = UUID.fromString(rs.getString(1));
                HashMap<String, Integer> blocks = new HashMap<>();
                blocks.put("total", rs.getInt(2));
                blocks.put("used", rs.getInt(3));
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
    public int hasNeededBlocks(OfflinePlayer player, int amount) {
        int pClaimBlocks = 0;
        int pClaimBlocksUsed = 0;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?")) {
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pClaimBlocks = rs.getInt(1);
                pClaimBlocksUsed = rs.getInt(2);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        int pClaimBlocksLeft = pClaimBlocks - pClaimBlocksUsed;

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
                if(rs.getString(1) != null) {
                    childClaims.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return childClaims;
    }

    public List<String> hasNotifications(String type, List<String> extraData, OfflinePlayer player) {
        List<String> notifications = new ArrayList<>();
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT extra_data FROM notifcations WHERE type = ? AND user_id = ? AND extra_data IN (" + getQuestionMarks(extraData) + ")")) {
            ps.setString(1, type);
            ps.setString(2, player.getUniqueId().toString());

            for (int i = 0; i < extraData.size(); i++) {
                ps.setString(i + 3, extraData.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifications;
    }
    public void createNotification(String type, String extraData, OfflinePlayer player, Component msg) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO notifications (type, extra_data, user_id, message) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, type);
            ps.setString(2, extraData);
            ps.setString(3, player.getUniqueId().toString());
            ps.setString(4, GsonComponentSerializer.gson().serialize(msg));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void deleteNotification(String type, String extraData, OfflinePlayer player) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM notifications WHERE extra_data = ? AND user_id = ? AND type = ?")) {
            ps.setString(1, extraData);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public boolean removeClaim(Player player, String claimName, RegionManager regionManager) {
        List<String> claimIds = getClaimIdsFromNames(player, claimName, Collections.singletonList("owner"));
        if(!claimIds.isEmpty() && regionManager.hasRegion(claimIds.get(0))) {
            String claimId = claimIds.get(0);
            HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
            int claimBlocksUsed = (int) claimData.get("blocks");

            List<String> childClaims = getChildren(claimId);

            HashMap<String, HashMap<String, Object>> childClaimData = getClaimData(childClaims);

            for(String childClaim : childClaimData.keySet()) {
                claimBlocksUsed += (int) childClaimData.get(childClaim).get("blocks");
            }

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM claims WHERE claim_id = ? OR parent_id = ?")) {
                ps.setString(1, claimId);
                ps.setString(2, claimId);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            int playerClaimBlocksUsed = getPlayersBlocks(Collections.singletonList(player)).get(player.getUniqueId()).get("used");
            int newClaimBlocksUsed = playerClaimBlocksUsed - claimBlocksUsed;

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
                ps.setInt(1, newClaimBlocksUsed);
                ps.setString(2, player.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            regionManager.removeRegion(claimId);
            return true;
        } else {
            return false;
        }
    }

    public void createClaim(Player player, String claimName, RegionManager regionManager, RegionSelector regionSelector, int playerClaimBlocks, int playerClaimBlocksUsed) {
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
                int claimBlocks;

                if (plugin.customClaimShape.containsKey(player.getUniqueId())) {
                    Polygonal2DRegion regionSel = (Polygonal2DRegion) regionSelector.getRegion();
                    region = new ProtectedPolygonalRegion(claimId, regionSel.getPoints(), minY, maxY);
                    claimBlocks = (int) new Polygonal2DRegion(BukkitAdapter.adapt(player.getWorld()), regionSel.getPoints(), 1, 1).getVolume();
                } else {
                    BlockVector3 p1 = regionSelector.getRegion().getMinimumPoint();
                    BlockVector3 p2 = regionSelector.getRegion().getMaximumPoint();
                    region = new ProtectedCuboidRegion(claimId, BlockVector3.at(p1.getBlockX(), minY, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), maxY, p2.getBlockZ()));
                    claimBlocks = (int) new CuboidRegion(BlockVector3.at(p1.getBlockX(), 1, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), 1, p2.getBlockZ())).getVolume();
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

                        int newClaimBlocksUsed = playerClaimBlocksUsed + claimBlocks;
                        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
                            ps.setInt(1, newClaimBlocksUsed);
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
                            ps.setInt(5, claimBlocks);
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
                        player.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this! You need ")
                                .append(Component.text(claimBlocks - playerClaimBlocksUsed, Style.style(TextDecoration.BOLD))).append(Component.text(" blocks more", NamedTextColor.RED))));
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

    public void expandClaim(Player player, String claimId, int amount, boolean isChild) {
        if(player.getFacing().isCartesian() && !player.getFacing().equals(BlockFace.UP) && !player.getFacing().equals(BlockFace.DOWN)) {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(player.getWorld()));
            if(regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(claimId);
                if(region != null) {
                    if(region instanceof ProtectedCuboidRegion) {
                        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);

                        BlockVector3 p1 = region.getMinimumPoint();
                        BlockVector3 p2 = region.getMaximumPoint();
                        switch (player.getFacing()) {
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
                                    player.sendMessage(prefix.append(Component.text("Can't expand a child claim to be outside of the parent claim!", NamedTextColor.RED)));
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
                                        if (isChild) {
                                            overlapIds.add(overlapId);
                                        } else if (!Objects.equals(overlapClaim.getParent(), region)) {
                                            overlapIds.add(overlapId);
                                        }
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Can't Expand! Claim would overlap an admin claim!", NamedTextColor.RED)));
                                    return;
                                }
                            }
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

                        int currentClaimBlocks = (int) new CuboidRegion(region.getMinimumPoint().withY(1), region.getMinimumPoint().withY(1)).getVolume();
                        int expandedClaimBlocks = (int) new CuboidRegion(expandedRegion.getMinimumPoint().withY(1), expandedRegion.getMinimumPoint().withY(1)).getVolume();

                        int additionalBlocksUsed = expandedClaimBlocks - currentClaimBlocks;

                        HashMap<String, Integer> pClaimBlocks = getPlayersBlocks(Collections.singletonList(player)).get(player.getUniqueId());

                        int blocksLeft = pClaimBlocks.get("total") - pClaimBlocks.get("used");

                        if(blocksLeft - additionalBlocksUsed >= 0) {
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = claim_blocks_used + ? WHERE user_id = ?")) {
                                ps.setInt(1, additionalBlocksUsed);
                                ps.setString(2, player.getUniqueId().toString());
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            expandedRegion.copyFrom(region);
                            regionManager.addRegion(expandedRegion);

                            player.sendMessage(prefix.append(Component.text("The claim ").append(Component.text(claimData.get("name").toString(), Style.style(TextDecoration.BOLD)))
                                            .append(Component.text(" has successfully been expanded by ")).append(Component.text(amount, Style.style(TextDecoration.BOLD))))
                                    .append(Component.text(" blocks " + player.getFacing().name(), TextColor.fromHexString("#20df80"))));
                        } else {
                            player.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this! You need ")
                                    .append(Component.text(additionalBlocksUsed - blocksLeft, Style.style(TextDecoration.BOLD))).append(Component.text(" blocks more", NamedTextColor.RED))));
                        }
                    } else {
                        player.sendMessage(prefix.append(Component.text("Can't expand custom shaped claims!", NamedTextColor.RED)));
                    }
                }
            }
        } else {
            player.sendMessage(prefix.append(Component.text("Can't expand custom shaped claims!", NamedTextColor.RED)));

        }
    }

    public void helpMessage(Player player, int page) {
        Component msg = Component.text("");
                msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" SkyPrison Claims ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        if (page == 1) {
            msg = msg
                .append(Component.text("\n/claim list (page)", TextColor.fromHexString("#20df80"))).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("List of all claims you're in", TextColor.fromHexString("#dbb976")))

                .append(Component.text("\n/claim info (claim)", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Get info about a claim", TextColor.fromHexString("#dbb976"))))

/*                .append(Component.text("\n/claim blocks", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Get info about your", TextColor.fromHexString("#dbb976"))))*/

                .append(Component.text("\n/claim buyblocks <amount>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Buy more claimblocks", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim create <claim>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Create a new claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim remove <claim>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Remove a claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim flags (claim)", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("View/edit flags", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim invite <player> (claim)", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Invite a player to your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim kick <player> (claim)", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
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
                .append(Component.text("\n/claim promote <player> (claim)", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Promote a member of your claim", TextColor.fromHexString("#dbb976")))).decoration(TextDecoration.STRIKETHROUGH, false)

                .append(Component.text("\n/claim demote <player> (claim)", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Demote a co-owner of your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim transfer <claim> <player>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Transfer claim to another player", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim rename <claim> <newName>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Rename your claim", TextColor.fromHexString("#dbb976"))))

                .append(Component.text("\n/claim expand <amount>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text("Expand a claim in the direction you are facing", TextColor.fromHexString("#dbb976"))))

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

    public void claimList(Player player, RegionManager regionManager, int playerBlocks, int playerBlocksUsed, int page) {
        HashMap<String, String> userClaims = getAllUserClaims(player, Arrays.asList("owner", "co-owner", "member"));
        Component msg = Component.text("");
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .append(Component.text(" Claims List ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
            .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
            .append(Component.text("\nTotal Blocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)
            .append(Component.text(playerBlocksUsed + "/" + playerBlocks, TextColor.fromHexString("#ffba75")))));
        if(!userClaims.isEmpty()) {
            // claim_name, parent_id, blocks_used, user_rank

            List<String> claimsIds = userClaims.keySet().stream().toList();

            HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimsIds);

            int totalPages = (int) Math.ceil((double) claimsData.size() / 10);

            if (page > totalPages) {
                page = 1;
            }



            for (String claimId : claimsData.keySet()) {
                String name = claimsData.get(claimId).get("name").toString();
                String userRank = WordUtils.capitalize(userClaims.get(claimId));
                int claimX = Objects.requireNonNull(regionManager.getRegion(claimId)).getMaximumPoint().getBlockX();
                int claimZ = Objects.requireNonNull(regionManager.getRegion(claimId)).getMaximumPoint().getBlockZ();
                int blocksUsed = (int) claimsData.get(claimId).get("blocks");

                Component parentInfo = Component.text("");
                if (claimsData.get(claimId).get("parent") != null) {
                    String parentId = (String) claimsData.get(claimId).get("parent");
                    String parentName = getClaimData(Collections.singletonList(parentId)).get(parentId).get("name").toString();

                    name = name + " (Child)";
                    parentInfo = Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(parentName, TextColor.fromHexString("#ffba75")));
                }
                msg = msg.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(name, TextColor.fromHexString("#0fffc3"))
                                .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(userRank, TextColor.fromHexString("#ffba75"))))
                        .hoverEvent(HoverEvent.showText(Component.text("").append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))
                                .append(Component.text("\nYour Rank", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(userRank, TextColor.fromHexString("#ffba75"))))
                                .append(parentInfo)
                                .append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                .append(Component.text("X " + claimX + " Y " + claimZ, TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nBlocks Used", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text(blocksUsed, TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\n⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH)))));
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
            } else {
                msg = msg.append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page, TextColor.fromHexString("#266d27"))
                        .append(Component.text("/", NamedTextColor.GRAY).append(Component.text(totalPages, TextColor.fromHexString("#266d27")))))
                        .append(Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY))))
                        .clickEvent(ClickEvent.runCommand("/claim list " + nextPage)));
            }

        }
        msg = msg.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(msg);
    }

    public void claimOverlapMultiple(Player player, List<String> claimIds) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Overlaps ", TextColor.fromHexString("#a49a2b"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nCan't expand! Claim would overlap these claims: ", TextColor.fromHexString("#a49a2b")));

        HashMap<String, UUID> ownersData = getClaimOwners(claimIds);
        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

        for(String claimId : ownersData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(ownersData.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimsData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName(), TextColor.fromHexString("#ffba75"))))
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
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ", TextColor.fromHexString("#a49a2b")));

        HashMap<String, UUID> ownersData = getClaimOwners(claimIds);
        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

        for(String claimId : ownersData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(ownersData.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimsData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3"))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName(), TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to show info for " + claimsData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void claimInfo(Player player, String claimId) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Info ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        String claimName = (String) claimData.get("name");
        String parentId = (String) claimData.get("parent");
        int blocksUsed = (int) claimData.get("blocks");

        info = info.append(Component.text("\nName", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                .append(Component.text(claimName, TextColor.fromHexString("#ffba75"))));

        if(parentId != null && !parentId.isEmpty()) {
            String parentName = (String) getClaimData(Collections.singletonList(parentId)).get(claimId).get("name");
            info = info.append(Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(parentName, TextColor.fromHexString("#ffba75"))).clickEvent(ClickEvent.runCommand("/claim info " + parentName)));
        }
        HashMap<String, HashMap<UUID, String>> claimsMembers = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner", "member"));
        if(!claimsMembers.isEmpty()) {
            HashMap<UUID, String> members = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner", "member")).get(claimId);
            boolean canEdit = members.containsKey(player.getUniqueId()) && (members.get(player.getUniqueId()).equalsIgnoreCase("owner") || members.get(player.getUniqueId()).equalsIgnoreCase("co-owner"));

            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(getClaimOwners(Collections.singletonList(claimId)).get(claimId));
            info = info.append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(blocksUsed, TextColor.fromHexString("#ffba75"))));

            info = info.append(Component.text("\nOwner", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(Objects.requireNonNull(oPlayer.getName()), TextColor.fromHexString("#ffba75"))));

            info = info.append(Component.text("\n\nVIEW MEMBERS", TextColor.fromHexString("#0fffc3"))
                    .clickEvent(ClickEvent.callback(audience -> player.openInventory(new ClaimMembers(plugin, claimName, members, "", 1).getInventory())))
                    .decorate(TextDecoration.BOLD));

            info = info.append(Component.text("\nVIEW FLAGS", TextColor.fromHexString("#0fffc3"))
                    .clickEvent(ClickEvent.callback(audience -> player.openInventory(new ClaimFlags(plugin, claimId, claimData.get("world").toString(), canEdit, "", 1).getInventory())))
                    .decorate(TextDecoration.BOLD));

            info = info.decoration(TextDecoration.ITALIC, false);
        }
        player.sendMessage(info);
    }

    public void invitePlayerMultiple(Player player, CMIUser iUser, List<String> claimIds) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Invite ", TextColor.fromHexString("#a49a2b"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ", TextColor.fromHexString("#a49a2b")));

        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);

        for(String claimId : claimData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(owners.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName(), TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to invite player to " + claimData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> invitePlayer(player, iUser, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void invitePlayer(Player player, CMIUser iUser, String claimId) {
        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");
        Component msg = prefix.append(Component.text("You've been invited to the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)))
                .append(Component.text("\nACCEPT INVITE", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> inviteAccept(iUser.getPlayer(), claimId))))
                .append(Component.text("               "))
                .append(Component.text("\nDECLINE INVITE", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> inviteDecline(iUser.getPlayer(), claimId))));

        if(iUser.isOnline()) {

            iUser.getPlayer().sendMessage(msg);
            player.sendMessage(prefix.append(Component.text("Successfully invited " + iUser.getName() + " to the claim!", TextColor.fromHexString("#20df80"))));
        } else {
            createNotification("claim-invite", claimId, iUser.getOfflinePlayer(), msg);
            player.sendMessage(prefix.append(Component.text("Successfully invited " + iUser.getName() + " to the claim! They'll get an invite once they're online.", TextColor.fromHexString("#20df80"))));
        }
    }

    public void inviteDecline(Player player, String claimId) {
        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");
        HashMap<UUID, String> toNotify = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner")).get(claimId);

        deleteNotification("claim-invite", claimId, player);

        player.sendMessage(prefix.append(Component.text("You've successfully declined the invite to join the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

        for(UUID pUUID : toNotify.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(pUUID);
            Component msg = prefix.append(Component.text("Player ", TextColor.fromHexString("#20df80"))
                    .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                    .append(Component.text(" has REJECTED the invite to join the claim ", TextColor.fromHexString("#20df80")))
                    .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
            if(oPlayer.isOnline()) {
                Objects.requireNonNull(oPlayer.getPlayer()).sendMessage(msg);
            } else {
                createNotification("claim-invite-declined", claimId, oPlayer, msg);
            }
        }
    }

    public void inviteAccept(Player player, String claimId) {
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld((String) claimData.get("world")))));
        assert regionManager != null;
        Objects.requireNonNull(regionManager.getRegion(claimId)).getMembers().addPlayer(player.getUniqueId());
        String claimName = (String) claimData.get("name");
        HashMap<UUID, String> toNotify = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner")).get(claimId);

        deleteNotification("claim-invite", claimId, player);


        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claimd_id, user_rank) VALUES (?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, claimId);
            ps.setString(3, "member");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        player.sendMessage(prefix.append(Component.text("You've successfully joined the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

        for(UUID pUUID : toNotify.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(pUUID);
                Component msg = prefix.append(Component.text("Player ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" has accepted the invite to join the claim ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
            if(oPlayer.isOnline()) {
                Objects.requireNonNull(oPlayer.getPlayer()).sendMessage(msg);
            } else {
                createNotification("claim-invite-accepted", claimId, oPlayer, msg);
            }
        }
    }
    public void kickPlayerMultiple(Player player, CMIUser kickedPlayer, List<String> claimIds) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Kick ", TextColor.fromHexString("#a49a2b"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to kick player from: ", TextColor.fromHexString("#a49a2b")));

        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);


        for(String claimId : claimData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(owners.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName(), TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to kick player from " + claimData.get(claimId).get("name"), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> kickPlayer(player, kickedPlayer, claimId))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void kickPlayer(Player player, CMIUser kickedPlayer, String claimId) {
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

        player.sendMessage(prefix.append(Component.text("Successfully kicked ", TextColor.fromHexString("#20df80"))
                .append(Component.text(kickedPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" from the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("You've been kicked from the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
        if(kickedPlayer.isOnline()) {
            kickedPlayer.getPlayer().sendMessage(msg);
        } else {
            createNotification("claim-kick", claimId, kickedPlayer.getOfflinePlayer(), msg);
        }
    }

    public void promotePlayer(Player player, CMIUser promotedPlayer, String claimId) {
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
                .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));

        player.sendMessage(prefix.append(Component.text("Successfully promoted ", TextColor.fromHexString("#20df80"))
                .append(Component.text(promotedPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" in the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        if(promotedPlayer.isOnline()) {
            promotedPlayer.getPlayer().sendMessage(msg);
        } else {
            createNotification("claim-promote", claimId, promotedPlayer.getOfflinePlayer(), msg);
        }
    }

    public void demotePlayer(Player player, CMIUser demotedPlayer, String claimId) {
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
                .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));

        player.sendMessage(prefix.append(Component.text("Successfully demoted ", TextColor.fromHexString("#20df80"))
                .append(Component.text(demotedPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" in the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        if(demotedPlayer.isOnline()) {
            demotedPlayer.getPlayer().sendMessage(msg);
        } else {
            createNotification("claim-demote", claimId, demotedPlayer.getOfflinePlayer(), msg);
        }
    }

    public void transferClaim(Player player, CMIUser transferPlayer, String claimId) {
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        int claimBlocks = (int) claimData.get("blocks");

        HashMap<UUID, HashMap<String, Integer>> pBlocks = getPlayersBlocks(Collections.singletonList(transferPlayer.getOfflinePlayer()));
        if(!pBlocks.isEmpty()) {
            HashMap<String, Integer> tBlocks = pBlocks.get(transferPlayer.getUniqueId());
            int pClaimBlocks = tBlocks.get("total");
            int pClaimBlocksUsed = tBlocks.get("used");

            int pClaimBlocksLeft = pClaimBlocks - pClaimBlocksUsed;

            if (claimBlocks > 0 && claimBlocks < pClaimBlocksLeft) {
                Component msg = prefix.append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                                .append(Component.text(" wants to transfer the claim ", TextColor.fromHexString("#20df80")))
                                .append(Component.text(claimData.get("name").toString(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                                .append(Component.text(" to you!", TextColor.fromHexString("#20df80"))))
                        .append(Component.text("\nACCEPT TRANSFER", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> transferAccept(player, transferPlayer.getPlayer(), claimId))))
                        .append(Component.text("               "))
                        .append(Component.text("\nDECLINE TRANSFER", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> transferDecline(player, transferPlayer.getPlayer(), claimId))));

                if (transferPlayer.isOnline()) {
                    transferPlayer.getPlayer().sendMessage(msg);
                    player.sendMessage(prefix.append(Component.text("Successfully sent a transfer request to " + transferPlayer.getName() + "!", TextColor.fromHexString("#20df80"))));
                } else {
                    createNotification("claim-transfer", claimId, transferPlayer.getOfflinePlayer(), msg);
                    player.sendMessage(prefix.append(Component.text("Successfully created transfer request! " + transferPlayer.getName() + " will receive the request when they log on. ", TextColor.fromHexString("#20df80"))));
                }
            } else {
                player.sendMessage(prefix.append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD)
                        .append(Component.text(" doesn't have enough claim blocks!", NamedTextColor.RED))));
            }
        } else {
            player.sendMessage(prefix.append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text(" doesn't have enough claim blocks!", NamedTextColor.RED))));
        }
    }

    public void transferDecline(OfflinePlayer player, Player transferPlayer, String claimId) {
        deleteNotification("claim-transfer", claimId, transferPlayer);

        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");

        transferPlayer.sendMessage(prefix.append(Component.text("You've successfully declined the transfer to become the owner of the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("Player ", NamedTextColor.RED)
                .append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" has declined the transfer request to become the owner of the claim ", NamedTextColor.RED))
                .append(Component.text(claimName, NamedTextColor.RED, TextDecoration.BOLD)));
        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendMessage(msg);
        } else {
            createNotification("claim-transfer-declined", claimId, player, msg);
        }
    }

    public void transferAccept(OfflinePlayer player, Player transferPlayer, String claimId) {
        HashMap<String, HashMap<String, Object>> claims = getClaimData(Collections.singletonList(claimId));
        if(!claims.isEmpty()) {
            HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
            int claimBlocks = (int) claimData.get("blocks");
            String claimName = (String) claimData.get("name");
            int pBlocks = hasNeededBlocks(transferPlayer, claimBlocks);
            if (pBlocks != -1) {
                String oldClaimName = claimName;
                List<String> nameTaken = getClaimIdsFromNames(player, claimName, Collections.singletonList("owner"));
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

                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
                    ps.setInt(1, pBlocks);
                    ps.setString(2, transferPlayer.getUniqueId().toString());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = blocks_used - ? WHERE user_id = ?")) {
                    ps.setInt(1, claimBlocks);
                    ps.setString(2, player.getUniqueId().toString());
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
                    ps.setString(2, player.getUniqueId().toString());
                    ps.setString(3, claimId);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (!nameTaken.isEmpty()) {
                    transferPlayer.sendMessage(prefix.append(Component.text("You already own a claim with the name ", TextColor.fromHexString("#20df80"))
                            .append(Component.text(oldClaimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                            .append(Component.text("! Renaming transferred claim to ", TextColor.fromHexString("#20df80")))
                            .append(Component.text(claimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
                }
                transferPlayer.sendMessage(prefix.append(Component.text("You're now the owner of the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(claimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));

                Component msg = prefix.append(Component.text("Your claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(oldClaimName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                        .append(Component.text(" was successfully transferred to ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(transferPlayer.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));
                if (player.isOnline()) {
                    Objects.requireNonNull(player.getPlayer()).sendMessage(msg);
                } else {
                    createNotification("claim-transfer-accepted", claimId, player, msg);
                }
            } else {
                transferPlayer.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this claim transfer! Cancelling transfer..", NamedTextColor.RED)));
            }
        } else {
            transferPlayer.sendMessage(prefix.append(Component.text("Couldn't find the claim to transfer! Cancelling transfer..", NamedTextColor.RED)));
        }
        deleteNotification("claim-transfer", claimId, transferPlayer);
    }

    public void claimFlagsMultiple(Player player, HashMap<String, String> userRanks) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Promote ", TextColor.fromHexString("#a49a2b"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to promote the player in: ", TextColor.fromHexString("#a49a2b")));
        List<String> claimIds = userRanks.keySet().stream().toList();
        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);

        for(String claimId : claimData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(owners.get(claimId));
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString(), TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName(), TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to view flags for " + claimData.get(claimId).get("name").toString(), NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.callback(audience -> claimFlags(player, claimId, claimData.get(claimId).get("world").toString(), userRanks.get(claimId)))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void claimFlags(Player player, String claimId, String world, String userRank) {
        boolean canEdit = userRank.equalsIgnoreCase("owner") || userRank.equalsIgnoreCase("co-owner");
        ClaimFlags claimFlags = new ClaimFlags(plugin, claimId, world, canEdit, "", 1);
        player.openInventory(claimFlags.getInventory());
    }

    public String getQuestionMarks(List<String> list) {
        StringBuilder questionMarks = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            questionMarks.append('?');
            if (i < list.size() - 1) {
                questionMarks.append(",");
            }
        }
        return  questionMarks.toString();
    }

    public HashMap<String, HashMap<UUID, String>> getClaimUsers(List<String> claimIds, List<String> ranks) {
        HashMap<String, HashMap<UUID, String>> userClaims = new HashMap<>();
        if(claimIds != null) {
            if(!claimIds.isEmpty()) {
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members WHERE claim_id IN ("
                        + getQuestionMarks(claimIds) + ") AND user_rank IN (" + getQuestionMarks(ranks) + ")")) {
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
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members WHERE user_rank IN (" + getQuestionMarks(ranks) + ")")) {
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

    public HashMap<String, String> getUserClaims(OfflinePlayer player, List<String> claimIds, RegionManager regionManager, List<String> ranks, boolean getLocationRegardless) {
        HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, ranks, getLocationRegardless);
        if(!userClaims.isEmpty()) {
            return userClaims.get(player.getUniqueId());
        }
        return new HashMap<>();
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
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN ("
                + getQuestionMarks(playerIds) + ") AND user_rank IN (" + getQuestionMarks(ranks) + ")")) {
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

        public HashMap<UUID, HashMap<String, String>> getUserClaims(List<OfflinePlayer> players, List<String> claimIds, RegionManager regionManager, List<String> ranks, boolean getLocationRegardless) {
        HashMap<UUID, HashMap<String, String>> userClaims = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        if(!claimIds.isEmpty()) {
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN ("
                    + getQuestionMarks(playerIds) + ") AND claim_id IN (" + getQuestionMarks(claimIds) + ") AND user_rank IN (" + getQuestionMarks(ranks) + ")")) {
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
                ApplicableRegionSet regionList = regionManager.getApplicableRegions(BlockVector3.at(Objects.requireNonNull(player.getLocation()).getX(), player.getLocation().getY(), player.getLocation().getZ()));
                if (!regionList.getRegions().isEmpty()) {
                    List<String> regionIds = new ArrayList<>();
                    for (final ProtectedRegion rg : regionList) {
                        if (rg.getId().startsWith("claim_")) {
                            regionIds.add(rg.getId());
                        }
                    }
                    if (!regionIds.isEmpty()) {
                        if(!getLocationRegardless) {
                            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN ("
                                    + getQuestionMarks(playerIds) + ") AND claim_id IN (" + getQuestionMarks(regionIds) + ") AND user_rank IN (" + getQuestionMarks(ranks) + ")")) {
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

            int claimBlocks = 0;
            int claimBlocksUsed = 0;
            List<String> claimIds = new ArrayList<>();

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?")) {
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    claimBlocks = rs.getInt(1);
                    claimBlocksUsed = rs.getInt(2);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "remove" -> { // /claim remove <claimname>
                        if (args.length > 1) {
                            if (removeClaim(player, args[1], regionManager)) {
                                player.sendMessage(prefix.append(Component.text("Successfully removed the claim with the name ", TextColor.fromHexString("#20df80"))
                                        .append(Component.text(args[1], TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
                            } else {
                                player.sendMessage(prefix.append(Component.text("You don't have any claims with that name!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Correct usage: /claim remove <claimname>", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
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
                    case "list" -> { // claim list (page)
                        int page = 1;
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                page = Integer.parseInt(args[1]);
                                claimList(player, regionManager, claimBlocks, claimBlocksUsed, page);
                            } else {
                                claimList(player, regionManager, claimBlocks, claimBlocksUsed, 1);
                            }
                        } else {
                            claimList(player, regionManager, claimBlocks, claimBlocksUsed, page);
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
                    case "info" -> { // claim info (claim)
                        if(args.length > 1) claimIds = getClaimIdsFromNames(player, args[1], Arrays.asList("owner", "co-owner", "member"));
                        HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, Arrays.asList("owner", "co-owner", "member"), true);
                        if(!userClaims.isEmpty()) {
                            claimIds = userClaims.keySet().stream().toList();
                            if(claimIds.size() == 1) {
                                claimInfo(player, claimIds.get(0));
                            } else {
                                claimInfoMultiple(player, claimIds);
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                        }
                    }
                    case "invite" -> { // claim invite <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(player, args[2], Arrays.asList("owner", "co-owner"));
                                HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, Arrays.asList("owner", "co-owner"), false);

                                HashMap<String, String> alreadyMember = getUserClaims(iUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member", "banned"), false);

                                if (!userClaims.isEmpty()) {
                                    claimIds = new ArrayList<>(userClaims.keySet());
                                    claimIds.removeAll(alreadyMember.keySet());
                                    if (!claimIds.isEmpty()) {
                                        List<String> alreadyInvited = hasNotifications("claim-invite", claimIds, iUser.getOfflinePlayer());
                                        claimIds.removeAll(alreadyInvited);
                                        if(!claimIds.isEmpty()) {
                                            if (claimIds.size() == 1) {
                                                invitePlayer(player, iUser, claimIds.get(0));
                                            } else {
                                                invitePlayerMultiple(player, iUser, claimIds);
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
                                    player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim invite <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "kick" -> { // claim kick <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(player, args[2], Arrays.asList("owner", "co-owner"));
                                HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, Arrays.asList("owner", "co-owner"), false);
                                HashMap<String, String> isMember = getUserClaims(iUser.getOfflinePlayer(), claimIds, null, Arrays.asList("owner", "co-owner", "member"),false);

                                if(!userClaims.isEmpty()) {
                                    if(!isMember.isEmpty()) {
                                        if (isMember.size() == 1) {
                                            String claimId = isMember.keySet().stream().toList().get(0);
                                            if(isMember.containsValue("member") || userClaims.get(claimId).equalsIgnoreCase("owner")) {
                                                kickPlayer(player, iUser, claimId);
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
                                                kickPlayerMultiple(player, iUser, kickableClaimIds);
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("You can't kick this player!", NamedTextColor.RED)));
                                            }
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim kick <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "promote" -> { // claim promote <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(player, args[2], List.of("owner"));
                                HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, List.of("owner"), false);


                                if(!userClaims.isEmpty()) {
                                    claimIds = userClaims.keySet().stream().toList();
                                    HashMap<String, String> promoteClaims = getUserClaims(iUser.getOfflinePlayer(), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member"), false);
                                    if(!promoteClaims.isEmpty()) {
                                        String userRank = promoteClaims.get(claimIds.get(0));
                                        if (userRank.equalsIgnoreCase("member")) {
                                            promotePlayer(player, iUser, claimIds.get(0));
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("This player has already been promoted!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim promote <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "demote" -> { // claim demote <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(player, args[2], List.of("owner"));
                                HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    claimIds = userClaims.keySet().stream().toList();
                                    HashMap<String, String> demoteClaims = getUserClaims(iUser.getOfflinePlayer(), claimIds,
                                            regionManager, Arrays.asList("owner", "co-owner", "member"), false);
                                    if(!demoteClaims.isEmpty()) {
                                        String userRank = demoteClaims.get(claimIds.get(0));
                                        if (userRank.equalsIgnoreCase("co-owner")) {
                                            demotePlayer(player, iUser, claimIds.get(0));
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("This player can't be demoted!", NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim demote <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "transfer" -> { // claim transfer <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(player, args[2], List.of("owner"));
                                HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    claimIds = userClaims.keySet().stream().toList();
                                    HashMap<String, String> transferClaims = getUserClaims(iUser.getOfflinePlayer(), claimIds,
                                            regionManager, List.of("co-owner"), false);
                                    if(!transferClaims.isEmpty()) {
                                        transferClaim(player, iUser, claimIds.get(0));
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player must be a promoted member of the claim!", NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("You don't have any claims with that name!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim demote <player> (claim)", NamedTextColor.RED)));
                        }
                    }
                    case "flags" -> { // claim flags (claim)
                            if(args.length > 1) claimIds = getClaimIdsFromNames(player, args[1], List.of("owner", "co-owner", "member"));
                            HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, List.of("owner", "co-owner", "member"), true);

                            if(!userClaims.isEmpty()) {
                                if(userClaims.size() == 1) {
                                    String claimId = userClaims.keySet().stream().toList().get(0);
                                    String world = getClaimData(Collections.singletonList(claimId)).get(claimId).get("world").toString();
                                    claimFlags(player, claimId, world, userClaims.get(claimId));
                                } else {
                                    claimFlagsMultiple(player, userClaims);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                            }
                    }
                    case "wand" -> player.performCommand("//wand");
                    case "buyblocks" -> { // claim buyblocks <amount>
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                int blocks = Integer.parseInt(args[1]);
                                double price = 40 * blocks;
                                if(user.getBalance() >= price) {
                                    user.withdraw(price);

                                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?")) {
                                        ps.setInt(1, blocks);
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
                                    int needed = (int) (price - user.getBalance());
                                    player.sendMessage(prefix.append(Component.text("You don't have enough money! You need $" + plugin.formatNumber(needed) + " more..", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim buyblocks <amount>", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim buyblocks <amount>", NamedTextColor.RED)));
                        }
                    }
                    case "expand" -> { // claim expand <amount>
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                HashMap<String, String> userClaims = getUserClaims(player, claimIds, regionManager, List.of("owner"), false);

                                if (!userClaims.isEmpty()) {
                                    List<String> claims = userClaims.keySet().stream().toList();

                                    if (claims.size() == 1) {
                                        expandClaim(player, claims.get(0), Integer.parseInt(args[1]), false);
                                    } else {
                                        String childClaim = "";
                                        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claims);
                                        for(String claim : claims) {
                                            if(claimData.get(claim).get("parent") != null) {
                                                childClaim = claim;
                                            }
                                        }

                                        expandClaim(player, childClaim, Integer.parseInt(args[1]), true);
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! Amount must be a number!", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim expand <amount>", NamedTextColor.RED)));

                        }
                    }
                    case "rename" -> { // claim rename <current> <new>
                        if(args.length > 2) {
                            List<String> nameTaken = getClaimIdsFromNames(player, args[1], Collections.singletonList("owner"));
                            if(!nameTaken.isEmpty()) {
                                if(updateClaimName(getClaimIdsFromNames(player, args[1], Collections.singletonList("owner")).get(0), args[2])) {
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
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim rename <curren namet> <new name>", NamedTextColor.RED)));
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
                                        player.sendMessage(prefix.append(Component.text("No claims found!", TextColor.fromHexString("#20df80"))));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Incorrect Usage! Max radius is 200 blocks.", NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim nearby <radius>", NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim nearby <radius>", NamedTextColor.RED)));
                        }
                    }
                    default -> {
                        if (args.length > 1) {
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
