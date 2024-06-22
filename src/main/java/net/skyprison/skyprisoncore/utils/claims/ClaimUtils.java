package net.skyprison.skyprisoncore.utils.claims;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.inventories.claims.ClaimFlags;
import net.skyprison.skyprisoncore.inventories.claims.ClaimMembers;
import net.skyprison.skyprisoncore.utils.ChatUtils;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.players.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClaimUtils {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public static StateFlag FLY;
    public static StringFlag EFFECTS;
    public static StringFlag CONSOLECMD;

    public static final List<UUID> deleteClaim = new ArrayList<>();
    public static final List<UUID> transferClaim = new ArrayList<>();
    public static final List<UUID> customClaimHeight = new ArrayList<>();
    public static final List<UUID> customClaimShape = new ArrayList<>();
    public static final List<ClaimData> claimData = new ArrayList<>();
    private static final Component prefix = Component.text("Claims", TextColor.fromHexString("#0fc3ff")).append(Component.text(" | ", NamedTextColor.WHITE));
    private static final Component notFound = prefix.append(Component.text("No claim(s) were found!", NamedTextColor.RED));

    public ClaimUtils(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    public static Component getPrefix() {
        return prefix;
    }
    public static Component getNotFound() {
        return notFound;
    }
    public RegionSelector getRegionSelector(Player player) {
        return WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player)).getRegionSelector(BukkitAdapter.adapt(player.getWorld()));
    }
    public RegionManager getRegionManager(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(player.getWorld()));
    }
    public record ClaimBlocks(UUID uuid, long total, long used) {}
    public void initializeData() {
        HashMap<String, ClaimData> claimsMap = new HashMap<>();
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT c.claim_id, c.claim_name, c.parent_id, c.world, c.blocks_used, " +
                        "(SELECT user_id FROM claims_members WHERE claim_id = c.claim_id AND user_rank = 'owner') as owner_id FROM claims c")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String claimId = rs.getString(1);
                String claimName = rs.getString(2);
                String parentId = rs.getString(3);
                String world = rs.getString(4);
                long blocksUsed = rs.getLong(5);
                UUID owner = UUID.fromString(rs.getString(6));

                ClaimData claim = new ClaimData(claimId, claimName, parentId, world, blocksUsed, owner);
                claimsMap.putIfAbsent(claimId, claim);
            }

            claimsMap.values().forEach(claim -> {
                if (claim.getParent() != null) {
                    ClaimData parent = claimsMap.get(claim.getParent());
                    if (parent != null) {
                        parent.addChild(claim);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String claimId = rs.getString("claim_id");
                ClaimData claim = claimsMap.get(claimId);
                if (claim != null) {
                    UUID memberId = UUID.fromString(rs.getString("user_id"));
                    String memberRank = rs.getString("user_rank");
                    claim.addMember(new ClaimMember(claimId, memberId, memberRank));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        claimData.addAll(claimsMap.values());
    }
    public HashMap<Flag<?>, Object> getDefaultFlags() {
        HashMap<Flag<?>, Object> defaultFlags = new HashMap<>();
        defaultFlags.put(Flags.PVP, StateFlag.State.DENY);
        defaultFlags.put(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        defaultFlags.put(Flags.TNT, StateFlag.State.DENY);
        defaultFlags.put(Flags.ENDER_BUILD, StateFlag.State.DENY);
        defaultFlags.put(Flags.FIRE_SPREAD, StateFlag.State.DENY);
        defaultFlags.put(Flags.LIGHTNING, StateFlag.State.DENY);
        return defaultFlags;
    }
    public ClaimBlocks getPlayerBlocks(UUID pUUID) {
        ClaimBlocks pBlocks = null;
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?")) {
            ps.setString(1, pUUID.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                pBlocks = new ClaimBlocks(pUUID, rs.getLong(1), rs.getLong(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pBlocks;
    }
    public void updateClaimName(ClaimData claim, String newName) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims SET claim_name = ? WHERE claim_id = ?")) {
            ps.setString(1, newName);
            ps.setString(2, claim.getId());
            ps.executeUpdate();
            claim.setName(newName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public long hasNeededBlocks(UUID player, long amount) {
        long pClaimBlocks = 0;
        long pClaimBlocksUsed = 0;

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?")) {
            ps.setString(1, player.toString());
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
    public ClaimData getClaim(String claimId) {
        return claimData.stream().filter(claim -> claim.getId().equalsIgnoreCase(claimId)).findFirst().orElse(null);
    }
    public List<ClaimData> getClaims(List<String> claimIds) {
        return claimData.stream().filter(claim -> claimIds.contains(claim.getId())).toList();
    }
    public List<ClaimData> getAllClaims() {
        return new ArrayList<>(claimData);
    }
    public ClaimData getPlayerClaim(UUID player, String claimName, String rank) {
        return claimData.stream().filter(claim -> claim.getName().equalsIgnoreCase(claimName)
                && claim.getMembers().stream().filter(member -> member.getUniqueId().equals(player)
                && member.getRank().equalsIgnoreCase(rank)).count() == 1).findFirst().orElse(null);
    }
    public List<ClaimData> getPlayerClaims(UUID player, String claimName, List<String> ranks) {
        return claimData.stream().filter(claim -> claim.getName().equalsIgnoreCase(claimName)
                && claim.getMembers().stream().filter(member -> member.getUniqueId().equals(player)
                && ranks.contains(member.getRank())).count() == 1).toList();
    }
    public List<ClaimData> getPlayerClaims(UUID player, List<String> claimNames, List<String> ranks) {
        return claimData.stream().filter(claim -> claimNames.contains(claim.getName())
                && claim.getMembers().stream().filter(member -> member.getUniqueId().equals(player)
                && ranks.contains(member.getRank())).count() == 1).toList();
    }
    public List<ClaimData> getPlayerClaims(UUID player, List<String> claimNames, String rank) {
        return claimData.stream().filter(claim -> claimNames.contains(claim.getName())
                && claim.getMembers().stream().filter(member -> member.getUniqueId().equals(player)
                && member.getRank().equalsIgnoreCase(rank)).count() == 1).toList();
    }
    public List<ClaimData> getPlayerClaims(UUID player) {
        return claimData.stream().filter(claim -> claim.getMembers().stream().filter(member -> member.getUniqueId().equals(player)).count() == 1).toList();
    }
    public List<ClaimData> getPlayerClaims(UUID player, List<String> ranks) {
        return claimData.stream().filter(claim -> claim.getMembers().stream().filter(member -> member.getUniqueId().equals(player)
                && ranks.contains(member.getRank())).count() == 1).toList();
    }
    public void deleteClaim(Player executorPlayer, UUID targetPlayer, String claimName) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer) && !hasPerm(executorPlayer)) return;
        ClaimData claim = getPlayerClaim(targetPlayer, claimName, "owner");

        RegionManager regionManager = getRegionManager(executorPlayer);
        if(claim == null || !regionManager.hasRegion(claim.getId())) {
            executorPlayer.sendMessage(prefix.append(Component.text("Failed to find your claim on deletion! Contact an admin..", NamedTextColor.RED)));
            return;
        }
        long claimBlocksUsed = claim.getBlocks() + claim.getChildren().stream().mapToLong(ClaimData::getBlocks).sum();

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM claims WHERE claim_id = ?")) {
            ps.setString(1, claim.getId());
            ps.setString(2, claim.getId());
            ps.executeUpdate();
            claimData.removeAll(claim.getChildren());
            claimData.remove(claim);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ClaimBlocks pBlocks = getPlayerBlocks(targetPlayer);
        long newClaimBlocksUsed = pBlocks.used - claimBlocksUsed;
        updateUsedBlocks(targetPlayer, newClaimBlocksUsed);

        regionManager.removeRegion(claim.getId());
        executorPlayer.sendMessage(prefix.append(Component.text("Successfully deleted the claim with the name ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer)) {
            Component targetMsg = prefix.append(Component.text("Your claim ", TextColor.fromHexString("#20df80")))
                    .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                    .append(Component.text(" was deleted by ", TextColor.fromHexString("#20df80")))
                    .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD));
            PlayerManager.sendMessage(targetPlayer, targetMsg, "claim-delete");
        }
    }
    public void createClaim(Player player, String claimName, RegionSelector regionSelector) {
        try {
            ClaimData nameTaken = getPlayerClaim(player.getUniqueId(), claimName, "owner");
            if(nameTaken != null) {
                player.sendMessage(prefix.append(Component.text("You already have a claim with that name!", NamedTextColor.RED)));
                return;
            }
            String claimId = "claim_" + UUID.randomUUID();

            int minY = -64;
            int maxY = 319;

            if (customClaimHeight.contains(player.getUniqueId())) {
                minY = regionSelector.getRegion().getMinimumPoint().getBlockY();
                maxY = regionSelector.getRegion().getMaximumPoint().getBlockY();
            }

            ProtectedRegion region;
            Region blockRegion;

            if (customClaimShape.contains(player.getUniqueId())) {
                Polygonal2DRegion regionSel = (Polygonal2DRegion) regionSelector.getRegion();
                region = new ProtectedPolygonalRegion(claimId, regionSel.getPoints(), minY, maxY);
                blockRegion = new Polygonal2DRegion(BukkitAdapter.adapt(player.getWorld()), regionSel.getPoints(), 1, 1);
            } else {
                BlockVector3 p1 = regionSelector.getRegion().getMinimumPoint();
                BlockVector3 p2 = regionSelector.getRegion().getMaximumPoint();
                region = new ProtectedCuboidRegion(claimId, BlockVector3.at(p1.getBlockX(), minY, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), maxY, p2.getBlockZ()));
                blockRegion = new CuboidRegion(BlockVector3.at(p1.getBlockX(), 1, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), 1, p2.getBlockZ()));
            }
            long claimBlocks = blockRegion.getVolume();

            ProtectedRegion parentRegion = null;

            RegionManager regionManager = getRegionManager(player);
            List<ProtectedRegion> regionOverlaps = region.getIntersectingRegions(regionManager.getRegions().values());
            if (!regionOverlaps.isEmpty()) {
                List<ClaimData> overlapIds = new ArrayList<>();
                for (ProtectedRegion overlapClaim : regionOverlaps) {
                    ClaimData overlap = getClaim(overlapClaim.getId());
                    if(!overlap.getId().startsWith("claim_")) {
                        player.sendMessage(prefix.append(Component.text("Can't create claim! Claim would overlap an admin claim!", NamedTextColor.RED)));
                        return;
                    }
                    if(!overlap.getOwner().equals(player.getUniqueId()) || overlap.getParent() != null) {
                        overlapIds.add(overlap);
                        continue;
                    }
                    BlockVector3 regionMin = region.getMinimumPoint();
                    BlockVector3 regionMax = region.getMaximumPoint();

                    if ((!overlapClaim.contains(regionMax) && overlapClaim.contains(regionMin)) || (!overlapClaim.contains(regionMin) && overlapClaim.contains(regionMax))) {
                        player.sendMessage(prefix.append(Component.text("Your selection is partially outside the parent claim!", NamedTextColor.RED)));
                        return;
                    }
                    if(!overlapClaim.contains(regionMax) || !overlapClaim.contains(regionMin)) {
                        overlapIds.add(overlap);
                        continue;
                    }
                    parentRegion = overlapClaim;
                }
                if(!overlapIds.isEmpty()) {
                    claimOverlapMessage(player, overlapIds, "create");
                    return;
                }
            }

            int height = region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY();
            if (claimBlocks < 36 || blockRegion.getMinimumPoint().distance(blockRegion.getMaximumPoint()) <= 7 || height < 5) {
                player.sendMessage(prefix.append(Component.text("Selected area is too small! Claims must be atleast 6x6x6 blocks in size.", NamedTextColor.RED)));
                return;
            }

            ClaimBlocks pBlocks = getPlayerBlocks(player.getUniqueId());
            long newUsedBlocks = pBlocks.used + claimBlocks;

            if (pBlocks.total < newUsedBlocks) {
                player.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this! You need ", NamedTextColor.RED))
                        .append(Component.text(pBlocks.used + claimBlocks - pBlocks.total, NamedTextColor.RED, TextDecoration.BOLD))
                        .append(Component.text(" blocks more", NamedTextColor.RED)));
                return;
            }

            boolean isChild = parentRegion != null;
            String parentId = isChild ? parentRegion.getId() : null;
            region.setPriority(isChild ? 2 : 1);
            region.setFlags(isChild ? parentRegion.getFlags() : getDefaultFlags());
            if (isChild) {
                region.setParent(parentRegion);
            }
            regionManager.addRegion(region);
            region.getMembers().addPlayer(player.getUniqueId());

            updateUsedBlocks(player.getUniqueId(), newUsedBlocks);

            try(Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement("INSERT INTO claims (claim_id, claim_name, parent_id, world, blocks_used) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, region.getId());
                ps.setString(2, claimName);
                ps.setString(3, parentId);
                ps.setString(4, player.getWorld().getName());
                ps.setLong(5, claimBlocks);
                ps.executeUpdate();
                ClaimData claim = new ClaimData(region.getId(), claimName, parentId, player.getWorld().getName(), claimBlocks, player.getUniqueId());
                if(isChild) getClaim(parentId).addChild(claim);
                addMember(claim, player.getUniqueId(), "owner");
                claimData.add(claim);
            } catch (SQLException e) {
                e.printStackTrace();
            }


            player.sendMessage(prefix.append(Component.text("Successfully created a claim with the name ", TextColor.fromHexString("#20df80"))
                    .append(Component.text(claimName, TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        } catch (final Exception e){
            e.printStackTrace();
        }
    }
    public void updateUsedBlocks(UUID pUUID, long newUsedBlocks) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = ? WHERE user_id = ?")) {
            ps.setLong(1, newUsedBlocks);
            ps.setString(2, pUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addMember(ClaimData claim, UUID pUUID, String rank) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?)")) {
            ps.setString(1, pUUID.toString());
            ps.setString(2, claim.getId());
            ps.setString(3, rank);
            ps.executeUpdate();

            claim.addMember(new ClaimMember(claim.getId(), pUUID, rank));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void removeMember(ClaimData claim, ClaimMember member) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM claims_members WHERE claim_id = ? AND user_id = ?")) {
            ps.setString(1, claim.getId());
            ps.setString(2, member.getUniqueId().toString());
            ps.executeUpdate();

            claim.removeMember(member);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void expandClaimMultiple(Player executorPlayer, List<ClaimData> claims, int amount, BlockFace playerFacing) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Expand ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMultiple expandable claims found! Pick one: ", NamedTextColor.GRAY));


        for(ClaimData claim : claims) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to expand " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> expandClaim(executorPlayer, claim, amount,  playerFacing))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }
    public void expandClaim(Player executorPlayer, ClaimData claim, int amount, BlockFace playerFacing) {
        UUID targetPlayer = claim.getOwner();
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer) && !hasPerm(executorPlayer)) return;
        if(playerFacing.isCartesian() && !playerFacing.equals(BlockFace.UP) && !playerFacing.equals(BlockFace.DOWN)) {
            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(executorPlayer.getWorld()));
            if(regionManager != null) {
                ProtectedRegion region = regionManager.getRegion(claim.getId());
                if(region != null) {
                    if(region instanceof ProtectedCuboidRegion) {

                        BlockVector3 p1 = region.getMinimumPoint();
                        BlockVector3 p2 = region.getMaximumPoint();
                        switch (playerFacing) {
                            case NORTH -> p1 = p1.subtract(0, 0, amount);
                            case SOUTH -> p2 = p2.add(0, 0, amount);
                            case WEST -> p1 = p1.subtract(amount, 0, 0);
                            case EAST -> p2 = p2.add(amount, 0, 0);
                        }
                        ProtectedRegion expandedRegion = new ProtectedCuboidRegion(claim.getId(), p1, p2);
                        boolean isChild = claim.getParent() != null;
                        if (isChild) {
                            ProtectedRegion parentRegion = regionManager.getRegion(claim.getParent());
                            if (parentRegion != null && (!parentRegion.contains(p1) || !parentRegion.contains(p2))) {
                                executorPlayer.sendMessage(prefix.append(Component.text("Can't expand a child claim to be outside of the parent claim!", NamedTextColor.RED)));
                                return;
                            }
                        }

                        List<ProtectedRegion> regionOverlaps = expandedRegion.getIntersectingRegions(regionManager.getRegions().values());
                        if (!regionOverlaps.isEmpty()) {
                            List<ClaimData> overlapIds = new ArrayList<>();
                            for (ProtectedRegion overlapClaim : regionOverlaps) {
                                if(!overlapClaim.getId().startsWith("claim_")) {
                                    executorPlayer.sendMessage(prefix.append(Component.text("Can't Expand! Claim would overlap an admin claim!", NamedTextColor.RED)));
                                    return;
                                }
                                ClaimData overlap = getClaim(overlapClaim.getId());
                                if(!overlapClaim.equals(region)) {
                                    if ((isChild && !Objects.equals(overlapClaim, region.getParent())) || (!isChild && !Objects.equals(overlapClaim.getParent(), region))) {
                                        overlapIds.add(overlap);
                                    }
                                }
                            }
                            if(!overlapIds.isEmpty()) {
                                claimOverlapMessage(executorPlayer, overlapIds, "expand");
                                return;
                            }
                        }

                        long currentClaimBlocks = new CuboidRegion(region.getMinimumPoint().withY(1), region.getMaximumPoint().withY(1)).getVolume();
                        long expandedClaimBlocks = new CuboidRegion(expandedRegion.getMinimumPoint().withY(1), expandedRegion.getMaximumPoint().withY(1)).getVolume();

                        long additionalBlocksUsed = expandedClaimBlocks - currentClaimBlocks;

                        ClaimBlocks pBlocks = getPlayerBlocks(targetPlayer);

                        long blocksLeft = pBlocks.total - pBlocks.used;

                        if(blocksLeft - additionalBlocksUsed >= 0) {
                            try(Connection conn = db.getConnection();
                                PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = claim_blocks_used + ? WHERE user_id = ?")) {
                                ps.setLong(1, additionalBlocksUsed);
                                ps.setString(2, targetPlayer.toString());
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }


                            try(Connection conn = db.getConnection();
                                PreparedStatement ps = conn.prepareStatement("UPDATE claims SET blocks_used = blocks_used + ? WHERE claim_id = ?")) {
                                ps.setLong(1, additionalBlocksUsed);
                                ps.setString(2, claim.getId());
                                ps.executeUpdate();
                                claim.setBlocks(claim.getBlocks() + additionalBlocksUsed);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            expandedRegion.copyFrom(region);
                            regionManager.addRegion(expandedRegion);

                            executorPlayer.sendMessage(prefix.append(Component.text("The claim ", TextColor.fromHexString("#20df80"))
                                            .append(Component.text(claim.getName(), TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                                            .append(Component.text(" has successfully been expanded by ", TextColor.fromHexString("#20df80")))
                                            .append(Component.text(amount, TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD)))
                                    .append(Component.text(" blocks " + playerFacing.name(), TextColor.fromHexString("#20df80"))));
                        } else {
                            executorPlayer.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this! You need ")
                                    .append(Component.text(additionalBlocksUsed - blocksLeft, Style.style(TextDecoration.BOLD)))
                                    .append(Component.text(" blocks more", NamedTextColor.RED))));
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
    public void helpMessage(CommandSender sender, int page) {
        boolean hasPerm = sender.hasPermission("skyprisoncore.command.claim.admin");
        Component msg = Component.text("");
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" SkyPrison Claims ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        if (page == 1) {
            msg = msg
                    .append(Component.text("\n/claim list " + (hasPerm ? "(page) (all/player)" :  "(page)"), TextColor.fromHexString("#20df80")))
                    .append(Component.text(" - ", NamedTextColor.GRAY))
                    .append(Component.text("List of all claims you're in", TextColor.fromHexString("#dbb976")))

                    .append(Component.text("\n/claim info (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Get info about a claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim blocks " + (hasPerm ? "buy/give/take/set <player> <amount>" : "buy <amount>"), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Buy more claimblocks", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim create <claim>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Create a new claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim delete <claim>" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("delete a claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim flags (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("View/edit flags", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim invite <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Invite a player to your claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim kick <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Kick a member from your claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim wand", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Get the tool used for claiming", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim nearby <radius>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Get a list of nearby claims", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n" + page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                            .append(Component.text("2", TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->", NamedTextColor.GRAY)
                            .hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY)))
                            .clickEvent(ClickEvent.runCommand("/claim help 2"))).decorate(TextDecoration.BOLD));
        } else if (page == 2) {
            msg = msg
                    .append(Component.text("\n/claim promote <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Promote a member of your claim", TextColor.fromHexString("#dbb976")))).decoration(TextDecoration.STRIKETHROUGH, false)

                    .append(Component.text("\n/claim demote <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Demote a co-owner of your claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim transfer <player> (claim)" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Transfer claim to another player", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim rename <claim> <newName>" + (hasPerm ? " (player)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Rename your claim", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim expand <amount>", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Expand a claim in the direction you are facing", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim pending" + (hasPerm ? " (player/all)" : ""), TextColor.fromHexString("#20df80"))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("See pending invites/transfers", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim customheight", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Enable/disable custom height on claim create", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n/claim customshape", TextColor.fromHexString("#20df80")).append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text("Enable/disable custom shape on claim create.", TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                            .clickEvent(ClickEvent.runCommand("/claim help 1")).append(Component.text(page, TextColor.fromHexString("#266d27"))
                                    .append(Component.text("/", NamedTextColor.GRAY)
                                    .append(Component.text("2", TextColor.fromHexString("#266d27"))))).decorate(TextDecoration.BOLD));
        }

        msg = msg.decoration(TextDecoration.ITALIC, false);
        sender.sendMessage(msg);
    }
    public boolean hasPerm(CommandSender sender) {
        return sender.hasPermission("skyprisoncore.command.claim.admin");
    }
    public void claimListAll(CommandSender sender, int page) {
        if(!hasPerm(sender)) return;
        Component msg = Component.empty();
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claims List ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        List<ClaimData> claimsToShow = new ArrayList<>(claimData);
        long totalBlocksUsed = claimsToShow.stream().mapToLong(ClaimData::getBlocks).sum();
        msg = msg.append(Component.text("\nTotal Blocks In Use", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)
                .append(Component.text(ChatUtils.formatNumber(totalBlocksUsed), TextColor.fromHexString("#ffba75")))));

        int totalPages = (int) Math.ceil((double) claimsToShow.size() / 10);

        if (page > totalPages) {
            page = 1;
        }
        int toDelete = 10 * (page - 1);
        if (toDelete != 0) {
            claimsToShow = claimsToShow.subList(toDelete, claimsToShow.size());
        }
        int i = 0;

        for (ClaimData claim : claimsToShow) {
            if (i == 10) break;
            List<Integer> coords = getClaimCoords(claim);

            Component parentInfo = Component.text("");
            if (claim.getParent() != null) {
                parentInfo = Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                        .append(Component.text(getClaim(claim.getParent()).getName(), TextColor.fromHexString("#ffba75")));
            }
            msg = msg.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3"))
                            .append(claim.getParent() != null ? Component.text(" (Child)", TextColor.fromHexString("#ffba75")) : Component.text("")))
                    .hoverEvent(HoverEvent.showText(Component.text("")
                            .append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))
                            .append(parentInfo)
                            .append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                    .append(Component.text("X " + coords.getFirst() + " Y " + coords.get(1), TextColor.fromHexString("#ffba75"))))
                            .append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                    .append(Component.text(claim.getBlocks(), TextColor.fromHexString("#ffba75"))))
                            .append(Component.text("\nClick for more info", NamedTextColor.GRAY))
                            .append(Component.text("\n⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))))
                    .clickEvent(ClickEvent.callback(audience -> {
                        if(sender instanceof Player player) claimInfo(player, claim);
                    })));
            i++;
        }

        int nextPage = page + 1;
        int prevPage = page - 1;
        Component pages = Component.text(page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                .append(Component.text(totalPages, TextColor.fromHexString("#266d27"))));
        Component next = Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> { if(sender instanceof Player player) claimListAll(player, nextPage); }));
        Component prev = Component.text("<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> { if(sender instanceof Player player) claimListAll(player, prevPage); }));

        if (page == 1 && page != totalPages) {
            msg = msg.appendNewline().append(pages).append(next);
        } else if (page != 1 && page == totalPages) {
            msg = msg.appendNewline().append(prev).append(pages);
        } else if (page != 1) {
            msg = msg.appendNewline().append(prev).append(pages).append(next);
        }
        msg = msg.decoration(TextDecoration.ITALIC, false);
        sender.sendMessage(msg);
    }
    public void claimList(CommandSender sender, UUID targetPlayer, int page) {
        if(sender instanceof Player player && !Objects.equals(player.getUniqueId(), targetPlayer) && !hasPerm(sender)) return;
        Component msg = Component.empty();
        msg = msg.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claims List ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        List<ClaimData> claims = getPlayerClaims(targetPlayer);

        ClaimBlocks pBlocks =  getPlayerBlocks(targetPlayer);

        msg = msg.append(Component.text("\nTotal Blocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY)
                .append(Component.text(ChatUtils.formatNumber(pBlocks.used) + "/" + ChatUtils.formatNumber(pBlocks.total), TextColor.fromHexString("#ffba75")))));
        if (!claims.isEmpty()) {
            int totalPages = (int) Math.ceil((double) claims.size() / 10);

            if (page > totalPages) {
                page = 1;
            }

            List<ClaimData> claimsToShow = new ArrayList<>(claims);

            int todelete = 10 * (page - 1);
            if (todelete != 0) {
                claimsToShow = claimsToShow.subList(todelete, claimsToShow.size());
            }
            int i = 0;

            boolean ownClaims = sender instanceof Player player && !Objects.equals(player.getUniqueId(), targetPlayer) && hasPerm(sender);

            for (ClaimData claim : claimsToShow) {
                if (i == 10) break;
                ClaimMember member = claim.getMembers().stream().filter(m -> m.getUniqueId().equals(targetPlayer))
                        .findFirst().orElse(new ClaimMember(claim.getId(), targetPlayer, "Unknown"));

                List<Integer> coords = getClaimCoords(claim);

                Component parentInfo = Component.text("");
                if (claim.getParent() != null) {
                    parentInfo = Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                            .append(Component.text(getClaim(claim.getParent()).getName(), TextColor.fromHexString("#ffba75")));
                }
                msg = msg.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3"))
                                .append(claim.getParent() != null ? Component.text(" (Child)", TextColor.fromHexString("#ffba75")) : Component.text("")))
                        .hoverEvent(HoverEvent.showText(Component.text("")
                                .append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))
                                .append(!ownClaims ? Component.text("\nRank", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                        .append(Component.text(member.getRank(), TextColor.fromHexString("#ffba75"))) : Component.empty())
                                .append(parentInfo)
                                .append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                        .append(Component.text("X " + coords.getFirst() + " Y " + coords.get(1), TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                                        .append(Component.text(claim.getBlocks(), TextColor.fromHexString("#ffba75"))))
                                .append(Component.text("\nClick for more info", NamedTextColor.GRAY))
                                .append(Component.text("\n⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯", NamedTextColor.WHITE, TextDecoration.STRIKETHROUGH))))
                        .clickEvent(ClickEvent.callback(audience -> {
                            if(sender instanceof Player player) claimInfo(player, claim);
                        })));
                i++;
            }

            int nextPage = page + 1;
            int prevPage = page - 1;
            Component pages = Component.text(page, TextColor.fromHexString("#266d27")).append(Component.text("/", NamedTextColor.GRAY)
                    .append(Component.text(totalPages, TextColor.fromHexString("#266d27"))));
            Component next = Component.text(" Next --->", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> { if(sender instanceof Player player) claimList(player, targetPlayer, nextPage); }));
            Component prev = Component.text("<--- Prev ", NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> { if(sender instanceof Player player) claimList(player, targetPlayer, prevPage); }));

            if (page == 1 && page != totalPages) {
                msg = msg.appendNewline().append(pages).append(next);
            } else if (page != 1 && page == totalPages) {
                msg = msg.appendNewline().append(prev).append(pages);
            } else if (page != 1) {
                msg = msg.appendNewline().append(prev).append(pages).append(next);
            }
        }
        msg = msg.decoration(TextDecoration.ITALIC, false);
        sender.sendMessage(msg);
    }
    public void claimOverlapMessage(Player player, List<ClaimData> claims, String type) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Overlaps ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text(
                "\nCan't " + (type.equalsIgnoreCase("create") ? "create claim" : "expand claim") + "! Claim would overlap these claims: ", NamedTextColor.GRAY));

        for(ClaimData claim : claims) {
            String  ownerName = PlayerManager.getPlayerName(claim.getOwner());
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to show info for " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, claim))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }
    public void claimInfoMultiple(Player player, List<ClaimData> claims) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Info ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ", NamedTextColor.GRAY));


        for(ClaimData claim : claims) {
            String ownerName = PlayerManager.getPlayerName(claim.getOwner());
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY)).append(Component.text("Owner: " + ownerName, TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to show info for " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, claim))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }
    public List<Integer> getClaimCoords(ClaimData claim) {
        List<Integer> coords = new ArrayList<>();
        final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(claim.getWorld()))));
        if(regionManager != null) {
            ProtectedRegion region = regionManager.getRegion(claim.getId());
            if(region != null) {
                coords.add(region.getPoints().getFirst().getBlockX());
                coords.add(region.getPoints().getFirst().getBlockZ());
            }
        }
        return coords;
    }
    public void claimInfo(Player player, ClaimData claim) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Info ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));

        info = info.append(Component.text("\nName", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#ffba75"))));

        if(claim.getParent() != null) {
            ClaimData parent = getClaim(claim.getParent());
            info = info.append(Component.text("\nParent", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text(parent.getName(), TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("View parent info", NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimInfo(player, parent))));
        }
        ClaimMember member = claim.getMembers().stream().filter(m -> m.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
        boolean canEdit = member != null && (member.getRank().equalsIgnoreCase("owner") || member.getRank().equalsIgnoreCase("co-owner"));
        if(hasPerm(player)) canEdit = true;

        info = info.append(Component.text("\nBlocks", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                .append(Component.text(claim.getBlocks(), TextColor.fromHexString("#ffba75"))));

        info = info.append(Component.text("\nOwner", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(claim.getOwner()), "Couldn't get name!"), TextColor.fromHexString("#ffba75"))));

        List<Integer> coords = getClaimCoords(claim);

        info = info.append(Component.text("\nCoords", TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                .append(Component.text("X " + coords.getFirst() + " Y " + coords.get(1), TextColor.fromHexString("#ffba75"))));

        info = info.append(Component.text("\n\nVIEW MEMBERS", TextColor.fromHexString("#0fffc3"))
                .hoverEvent(HoverEvent.showText(Component.text("View members", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> player.openInventory(new ClaimMembers(plugin, claim).getInventory())))
                .decorate(TextDecoration.BOLD));

        boolean finalCanEdit = canEdit;
        info = info.append(Component.text("\nVIEW FLAGS", TextColor.fromHexString("#0fffc3"))
                .hoverEvent(HoverEvent.showText(Component.text("View flags", NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.callback(audience -> ownerHasPurchasedFlags(claim).thenAcceptAsync(hasPurchasedFlags ->
                        plugin.getServer().getScheduler().runTask(plugin, () ->
                                player.openInventory(new ClaimFlags(plugin, claim, finalCanEdit, hasPurchasedFlags).getInventory())))))
                .decorate(TextDecoration.BOLD));

        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }
    public CompletableFuture<Boolean> ownerHasPurchasedFlags(ClaimData claim) {
        UUID ownerId = claim.getOwner();
        Player player = Bukkit.getPlayer(ownerId);

        if(player != null) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            future.complete(player.hasPermission("skyprisoncore.claim.flags.purchased"));
            return future;
        }

        LuckPerms luckAPI = LuckPermsProvider.get();
        UserManager userManager = luckAPI.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(ownerId);
        return userFuture.thenApplyAsync(user -> {
            CachedPermissionData permissionData = user.getCachedData().getPermissionData();
            return permissionData.checkPermission("skyprisoncore.claim.flags.purchased").asBoolean();
        });
    }
    public void invitePlayerMultiple(Player executorPlayer, UUID targetPlayer, List<ClaimData> claims) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Invite ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ", NamedTextColor.GRAY));

        for(ClaimData claim : claims) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text("Owner: " + PlayerManager.getPlayerName(claim.getOwner()), TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to invite player to " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> invitePlayer(executorPlayer, targetPlayer, claim))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }
    public void invitePlayer(Player executorPlayer, UUID targetPlayer, ClaimData claim) {
        String notifId = UUID.randomUUID().toString();
        String targetName = PlayerManager.getPlayerName(targetPlayer);
        Component msg = prefix.append(Component.text("You've been invited to the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)))
                .append(Component.text("\nACCEPT INVITE", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim accept invite " + notifId)))
                .append(Component.text("     "))
                .append(Component.text("DECLINE INVITE", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim decline invite " + notifId)));
        executorPlayer.sendMessage(prefix.append(Component.text("Successfully invited " +
                targetName + " to the claim!", TextColor.fromHexString("#20df80"))));
        NotificationsUtils.createNotification("claim-invite", claim.getId(), targetPlayer, msg, notifId, false);
        Player isOnline = Bukkit.getPlayer(targetPlayer);
        if (isOnline != null) {
            isOnline.sendMessage(msg);
        }
    }
    public void inviteDecline(Player player, ClaimData claim, String notifId) {
        if(NotificationsUtils.hasNotification(notifId, player).isEmpty()) return;
        List<ClaimMember> members = claim.getMembers().stream().filter(m -> !m.getRank().equalsIgnoreCase("member")).toList();

        NotificationsUtils.deleteNotification(notifId);

        player.sendMessage(prefix.append(Component.text("You've successfully declined the invite to join the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));

        Component msg = prefix.append(Component.text("Player ", TextColor.fromHexString("#20df80"))
                .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" has REJECTED the invite to join the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
        members.forEach(m -> PlayerManager.sendMessage(m.getUniqueId(), msg, "claim-invite-declined", claim.getId()));
    }
    public void inviteAccept(Player player, ClaimData claim, String notifId) {
        if(NotificationsUtils.hasNotification(notifId, player).isEmpty()) return;

        final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(claim.getWorld()))));
        assert regionManager != null;
        Objects.requireNonNull(regionManager.getRegion(claim.getId())).getMembers().addPlayer(player.getUniqueId());

        NotificationsUtils.deleteNotification(notifId);

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?)")) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, claim.getId());
            ps.setString(3, "member");
            ps.executeUpdate();
            claim.addMember(new ClaimMember(claim.getId(), player.getUniqueId(), "member"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<ClaimMember> members = claim.getMembers().stream().filter(m -> !m.getRank().equalsIgnoreCase("member")).toList();

        player.sendMessage(prefix.append(Component.text("You've successfully joined the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("Player ", TextColor.fromHexString("#20df80"))
                .append(Component.text(player.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" has accepted the invite to join the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
        members.forEach(m -> PlayerManager.sendMessage(m.getUniqueId(), msg, "claim-invite-accepted", claim.getId()));
    }
    public void kickPlayerMultiple(Player executorPlayer, UUID targetPlayer, List<ClaimData> claims) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Kick ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to kick player from: ", NamedTextColor.GRAY));


        for(ClaimData claim : claims) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text("Owner: " + PlayerManager.getPlayerName(claim.getOwner()), TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to kick player from " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> kickPlayer(executorPlayer, claim.getMember(targetPlayer), claim))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }
    public void kickPlayer(Player executorPlayer, ClaimMember targetPlayer, ClaimData claim) {
        final RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld(claim.getWorld()))));
        assert regionManager != null;
        Objects.requireNonNull(regionManager.getRegion(claim.getId())).getMembers().removePlayer(targetPlayer.getUniqueId());

        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM claims_members WHERE claim_id = ? AND user_id = ?")) {
            ps.setString(1, claim.getId());
            ps.setString(2, targetPlayer.getUniqueId().toString());
            ps.executeUpdate();
            claim.removeMember(targetPlayer);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        executorPlayer.sendMessage(prefix.append(Component.text("Successfully kicked ", TextColor.fromHexString("#20df80"))
                .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetPlayer.getUniqueId()), "COULDN'T GET NAME"),
                        TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" from the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("You've been kicked from the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));
        PlayerManager.sendMessage(targetPlayer.getUniqueId(), msg, "claim-kick", claim.getId());
    }
    public void promotePlayerMultiple(Player executorPlayer, UUID targetPlayer, List<ClaimData> claims) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Promote ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to promote player in: ", NamedTextColor.GRAY));

        for(ClaimData claim : claims) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text("Owner: " + PlayerManager.getPlayerName(claim.getOwner()), TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to promote player in " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> promotePlayer(executorPlayer, claim.getMember(targetPlayer), claim))));
        }
    }
    public void promotePlayer(Player executorPlayer, ClaimMember targetPlayer, ClaimData claim) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
            ps.setString(1, "co-owner");
            ps.setString(2, targetPlayer.getUniqueId().toString());
            ps.setString(3, claim.getId());
            ps.executeUpdate();
            targetPlayer.setRank("co-owner");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Component msg = prefix.append(Component.text("You've been promoted in the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));

        executorPlayer.sendMessage(prefix.append(Component.text("Successfully promoted ", TextColor.fromHexString("#20df80"))
                .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetPlayer.getUniqueId()), "COULDN'T GET NAME"),
                        TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" in the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        PlayerManager.sendMessage(targetPlayer.getUniqueId(), msg, "claim-promote", claim.getId());
    }
    public void demotePlayerMultiple(Player executorPlayer, UUID targetPlayer, List<ClaimData> claims) {
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Demote ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to demote player in: ", NamedTextColor.GRAY));

        for(ClaimData claim : claims) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text("Owner: " + PlayerManager.getPlayerName(claim.getOwner()), TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to demote player in " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> demotePlayer(executorPlayer, claim.getMember(targetPlayer), claim))));
        }
    }
    public void demotePlayer(Player executorPlayer, ClaimMember targetPlayer, ClaimData claim) {
        try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
            ps.setString(1, "member");
            ps.setString(2, targetPlayer.getUniqueId().toString());
            ps.setString(3, claim.getId());
            ps.executeUpdate();
            targetPlayer.setRank("member");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Component msg = prefix.append(Component.text("You've been demoted in the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" by ", TextColor.fromHexString("#20df80")))
                .append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)));

        executorPlayer.sendMessage(prefix.append(Component.text("Successfully demoted ", TextColor.fromHexString("#20df80"))
                .append(Component.text(Objects.requireNonNullElse(PlayerManager.getPlayerName(targetPlayer.getUniqueId()), "COULDN'T GET NAME"),
                        TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                .append(Component.text(" in the claim ", TextColor.fromHexString("#20df80")))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))));
        PlayerManager.sendMessage(targetPlayer.getUniqueId(), msg, "claim-demote", claim.getId());
    }
    public void transferClaim(Player executorPlayer, ClaimMember targetPlayer, ClaimData claim) {
        if(!Objects.equals(executorPlayer.getUniqueId(), claim.getOwner()) && !hasPerm(executorPlayer)) return;
        long claimBlocks = claim.getBlocks() + claim.getChildren().stream().mapToLong(ClaimData::getBlocks).sum();

        ClaimBlocks pBlocks = getPlayerBlocks(targetPlayer.getUniqueId());

        long pBlocksLeft = pBlocks.total - pBlocks.used;
        String targetName = PlayerManager.getPlayerName(targetPlayer.getUniqueId());
        if (claimBlocks <= 0 || claimBlocks > pBlocksLeft) {
            executorPlayer.sendMessage(prefix.append(Component.text(Objects.requireNonNullElse(targetName, "COULDN'T GET NAME"), NamedTextColor.RED, TextDecoration.BOLD)
                    .append(Component.text(" doesn't have enough claim blocks!", NamedTextColor.RED))));
            return;
        }
        String notifId = UUID.randomUUID().toString();
        Component msg = prefix.append(Component.text(executorPlayer.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD)
                        .append(Component.text(" wants to transfer the claim ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(claim.getName(), TextColor.fromHexString("#20df80"), TextDecoration.BOLD))
                        .append(Component.text(" to you!", TextColor.fromHexString("#20df80"))))
                .append(!claim.getChildren().isEmpty() ? Component.text("\nThis will also transfer all child claims to you!", NamedTextColor.GRAY) : Component.empty())
                .append(Component.text("\nACCEPT TRANSFER", NamedTextColor.GREEN, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim accept transfer " + notifId)))
                .append(Component.text("     "))
                .append(Component.text("DECLINE TRANSFER", NamedTextColor.RED, TextDecoration.BOLD).clickEvent(ClickEvent.runCommand("/claim decline transfer " + notifId)));
        NotificationsUtils.createNotification("claim-transfer", claim.getId(), targetPlayer.getUniqueId(), msg, notifId, false);
        Player isOnline = Bukkit.getPlayer(targetPlayer.getUniqueId());
        if (isOnline != null) {
            isOnline.sendMessage(msg);
        }
        executorPlayer.sendMessage(prefix.append(Component.text("Successfully sent a transfer request to " + targetName + "!", TextColor.fromHexString("#20df80"))));
    }
    public void transferDecline(Player transferPlayer, ClaimData claim, String notifId) {
        if(NotificationsUtils.hasNotification(notifId, transferPlayer).isEmpty()) return;

        NotificationsUtils.deleteNotification(notifId);

        transferPlayer.sendMessage(prefix.append(Component.text("You've successfully declined the transfer to become the owner of the claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(claim.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("Player ", NamedTextColor.RED)
                .append(Component.text(transferPlayer.getName(), NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text(" has declined the transfer request to become the owner of the claim ", NamedTextColor.RED))
                .append(Component.text(claim.getName(), NamedTextColor.RED, TextDecoration.BOLD)));

        List<ClaimMember> members = claim.getMembers().stream().filter(m -> !m.getRank().equalsIgnoreCase("member")).toList();
        members.forEach(m -> PlayerManager.sendMessage(m.getUniqueId(), msg, "claim-transfer-declined", claim.getId()));
    }
    public void transferAccept(Player transferPlayer, ClaimData claim, String notifId) {
        if(NotificationsUtils.hasNotification(notifId, transferPlayer).isEmpty()) return;

        NotificationsUtils.deleteNotification(notifId);

        long claimBlocks = claim.getBlocks() + claim.getChildren().stream().mapToLong(ClaimData::getBlocks).sum();

        long pBlocks = hasNeededBlocks(transferPlayer.getUniqueId(), claimBlocks);
        if (pBlocks < 0) {
            transferPlayer.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this claim transfer! Cancelling transfer..", NamedTextColor.RED)));
            return;
        }
        String oldName = claim.getName();
        HashMap<String, String> names = new HashMap<>();
        ClaimData nameTaken = getPlayerClaim(transferPlayer.getUniqueId(), claim.getName(), "owner");
        if (nameTaken != null) {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 10;
            Random random = new Random();
            String newName = random.ints(leftLimit, rightLimit + 1)
                    .limit(targetStringLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            names.put(claim.getName(), newName);
            updateClaimName(claim, newName);
        }

        List<ClaimData> childTaken = getPlayerClaims(transferPlayer.getUniqueId(), claim.getChildren().stream().map(ClaimData::getName).toList(), "owner");
        if(!childTaken.isEmpty()) {
            for(ClaimData child : childTaken) {
                int leftLimit = 97; // letter 'a'
                int rightLimit = 122; // letter 'z'
                int targetStringLength = 10;
                Random random = new Random();
                String newName = random.ints(leftLimit, rightLimit + 1)
                        .limit(targetStringLength)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();
                names.put(child.getName(), newName);
                updateClaimName(child, newName);
            }
        }

        updateUsedBlocks(transferPlayer.getUniqueId(), pBlocks);
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks_used = claim_blocks_used - ? WHERE user_id = ?")) {
            ps.setLong(1, claimBlocks);
            ps.setString(2, claim.getOwner().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
            ps.setString(1, "owner");
            ps.setString(2, transferPlayer.getUniqueId().toString());
            ps.setString(3, claim.getId());
            ps.executeUpdate();
            claim.getMembers().stream().filter(m -> m.getUniqueId().equals(transferPlayer.getUniqueId())).findFirst().ifPresent(m -> m.setRank("owner"));
            claim.setOwner(transferPlayer.getUniqueId());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
            ps.setString(1, "co-owner");
            ps.setString(2, claim.getOwner().toString());
            ps.setString(3, claim.getId());
            claim.getMembers().stream().filter(m -> m.getUniqueId().equals(claim.getOwner())).findFirst().ifPresent(m -> m.setRank("co-owner"));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for(ClaimData child : claim.getChildren()) {
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE user_rank = VALUE(user_rank)")) {
                ps.setString(1, transferPlayer.getUniqueId().toString());
                ps.setString(2, child.getId());
                ps.setString(3, "owner");
                ps.executeUpdate();
                child.getMembers().stream().filter(m -> m.getUniqueId().equals(transferPlayer.getUniqueId())).findFirst()
                        .ifPresentOrElse(m -> m.setRank("owner"), () -> child.addMember(new ClaimMember(child.getId(), transferPlayer.getUniqueId(), "owner")));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?")) {
                ps.setString(1, "co-owner");
                ps.setString(2, child.getOwner().toString());
                ps.setString(3, child.getId());
                ps.executeUpdate();
                child.getMembers().stream().filter(m -> m.getUniqueId().equals(child.getOwner())).findFirst().ifPresent(m -> m.setRank("co-owner"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        if (!names.isEmpty()) {
            Component msg = prefix.append(Component.text("Some of the names overlapped with your existing claims! The transferred claims have been renamed to: ",
                    TextColor.fromHexString("#20df80")));
            for(String prevName : names.keySet()) {
                msg = msg.append(Component.text("\n- ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(prevName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                        .append(Component.text(" ⇒ ", TextColor.fromHexString("#20df80")))
                        .append(Component.text(names.get(prevName), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));
            }
        }
        transferPlayer.sendMessage(prefix.append(Component.text("You're now the owner of the claim ", TextColor.fromHexString("#20df80"))
                        .append(Component.text(oldName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)))
                .append(!claim.getChildren().isEmpty() ? Component.text(" and all of its child claims!", TextColor.fromHexString("#ffba75")) : Component.empty()));

        Component msg = prefix.append(Component.text("Your claim ", TextColor.fromHexString("#20df80"))
                .append(Component.text(oldName, TextColor.fromHexString("#ffba75"), TextDecoration.BOLD))
                .append(!claim.getChildren().isEmpty() ? Component.text(" and all of its child claims", TextColor.fromHexString("#20df80")) : Component.empty())
                .append(Component.text(" was successfully transferred to ", TextColor.fromHexString("#20df80")))
                .append(Component.text(transferPlayer.getName(), TextColor.fromHexString("#ffba75"), TextDecoration.BOLD)));
        PlayerManager.sendMessage(claim.getOwner(), msg, "claim-transfer-accepted", claim.getId());
    }
    public void claimFlagsMultiple(Player executorPlayer, UUID targetPlayer, List<ClaimData> claims) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer) && !hasPerm(executorPlayer)) return;
        Component info = Component.text("");
        info = info.append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH))
                .append(Component.text(" Claim Flags ", TextColor.fromHexString("#0fc3ff"), TextDecoration.BOLD))
                .append(Component.text("⎯⎯⎯⎯⎯⎯", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to promote the player in: ", NamedTextColor.GRAY));


        for(ClaimData claim : claims) {
            info = info.append(Component.text("\n- ", NamedTextColor.WHITE).append(Component.text(claim.getName(), TextColor.fromHexString("#0fffc3")))
                    .append(claim.getParent() != null ? Component.text(" (Child)" , TextColor.fromHexString("#0fffc3")) : Component.text(""))
                    .append(Component.text(" ⇒ ", NamedTextColor.GRAY))
                    .append(Component.text("Owner: " + PlayerManager.getPlayerName(claim.getOwner()), TextColor.fromHexString("#ffba75")))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to view flags for " + claim.getName(), NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimFlags(executorPlayer, targetPlayer, claim))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        executorPlayer.sendMessage(info);
    }
    public void claimFlags(Player executorPlayer, UUID targetPlayer, ClaimData claim) {
        if(!Objects.equals(executorPlayer.getUniqueId(), targetPlayer) && !hasPerm(executorPlayer)) return;
        ClaimMember member = claim.getMember(targetPlayer);
        boolean canEdit = member != null && !member.getRank().equalsIgnoreCase("member");
        if(hasPerm(executorPlayer)) canEdit = true;
        boolean finalCanEdit = canEdit;
        ownerHasPurchasedFlags(claim).thenAcceptAsync(hasPurchasedFlags ->
                plugin.getServer().getScheduler().runTask(plugin, () ->
                        executorPlayer.openInventory(new ClaimFlags(plugin, claim, finalCanEdit, hasPurchasedFlags).getInventory())));
    }
    public List<ClaimData> getClaimsFromloc(Location loc, Player player) {
        List<String> regionIds = new ArrayList<>();
        ApplicableRegionSet regionList = getRegionManager(player).getApplicableRegions(BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        if (regionList.getRegions().isEmpty()) return new ArrayList<>();
        for (final ProtectedRegion rg : regionList) {
            if (rg.getId().startsWith("claim_")) {
                regionIds.add(rg.getId());
            }
        }
        return getClaims(regionIds);
    }
}
