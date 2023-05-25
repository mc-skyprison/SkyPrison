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
    private final DatabaseHook hook;

    private final Component prefix = Component.text("Claims").color(TextColor.fromHexString("#0fc3ff")).append(Component.text(" | ").color(NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false);

    public Claim(SkyPrisonCore plugin, DatabaseHook hook) {
        this.plugin = plugin;
        this.hook = hook;
    }
    public HashMap<String, UUID> getClaimOwners(List<String> claimIds) {
        HashMap<String, UUID> userUUIDs = new HashMap<>();
        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id FROM claims_members WHERE claim_id IN ? AND user_rank = ?");
            ps.setArray(1, conn.createArrayOf("TEXT", claimIds.toArray()));
            ps.setString(2, "owner");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                userUUIDs.put(rs.getString(1), UUID.fromString(rs.getString(2)));
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }
        return userUUIDs;
    }
    public HashMap<String, HashMap<String, Object>> getClaimData(List<String> claimIds) {
        HashMap<String, HashMap<String, Object>> claimInfos = new HashMap<>();
        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT claim_id, claim_name, parent_id, world, blocks_used FROM claims WHERE claim_id IN ?");
            ps.setArray(1, conn.createArrayOf("TEXT", claimIds.toArray()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                HashMap<String, Object> claimInfo = new HashMap<>();
                claimInfo.put("name", rs.getString(2));
                claimInfo.put("parent", rs.getString(3));
                claimInfo.put("world", rs.getString(4));
                claimInfo.put("blocks", rs.getInt(5));
                claimInfos.put(rs.getString(1), claimInfo);
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }

        return claimInfos;
    }

    public HashMap<String, List<String>> getClaimIdsFromNames(List<OfflinePlayer> players, List<String> claimNames, List<String> ranks) {
        HashMap<String, List<String>> claimIds = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT claim_name, claim_id FROM claims WHERE claim_id IN (SELECT claim_id FROM claims_members WHERE user_id IN ? AND user_rank IN ?)");
            ps.setArray(1, conn.createArrayOf("TEXT", playerIds.toArray()));
            ps.setArray(2, conn.createArrayOf("TEXT", ranks.toArray()));
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
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }
        if(!claimIds.isEmpty()) {
            return claimIds;
        } else {
            return null;
        }
    }

    public HashMap<UUID, HashMap<String, Integer>> getPlayersBlocks(List<OfflinePlayer> players) {
        HashMap<UUID, HashMap<String, Integer>>  playerBlocks = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_blocks, claim_blocks_used FROM users WHERE user_id IN ?");
            ps.setArray(1, conn.createArrayOf("TEXT", playerIds.toArray()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID user = UUID.fromString(rs.getString(1));
                HashMap<String, Integer> blocks = new HashMap<>();
                blocks.put("total", rs.getInt(2));
                blocks.put("used", rs.getInt(3));
                playerBlocks.put(user, blocks);
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }
        return playerBlocks;
    }


    public boolean updateClaimName(String claimId, String newName) {
        String sql = "UPDATE claims SET claim_name = ? WHERE claim_id = ?";
        List<Object> params = new ArrayList<>() {{
            add(newName);
            add(claimId);
        }};
        return hook.sqlUpdate(sql, params);
    }
    public int hasNeededBlocks(OfflinePlayer player, int amount) {
        int pClaimBlocks = 0;
        int pClaimBlocksUsed = 0;

        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?");
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pClaimBlocks = rs.getInt(1);
                pClaimBlocksUsed = rs.getInt(2);
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }

        int pClaimBlocksLeft = pClaimBlocks - pClaimBlocksUsed;

        if(pClaimBlocksLeft < amount) {
            return -1;
        } else {
            return pClaimBlocksUsed + amount;
        }
    }
    public boolean hasClaimNamed(OfflinePlayer player, String claimName) {
        Connection conn = hook.getSQLConnection();
        boolean hasName = false;
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT claim_name FROM claims_members WHERE user_id = ? AND user_rank = ? AND claim_name = ?");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, "owner");
            ps.setString(3, claimName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                hasName = true;
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }
        return hasName;
    }

    public List<String> hasNotifications(String type, List<String> extraData, OfflinePlayer player) {
        List<String> notifications = new ArrayList<>();
        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT extra_data FROM notifcations WHERE type = ? AND user_id = ? AND extra_data IN ?");
            ps.setString(1, type);
            ps.setString(2, player.getUniqueId().toString());
            ps.setArray(3, conn.createArrayOf("TEXT", extraData.toArray()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notifications.add(rs.getString(1));
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }
        return notifications;
    }
    public void createNotification(String type, String extraData, OfflinePlayer player, Component msg) {
        String sql = "INSERT INTO notifications (type, extra_data, user_id, message) VALUES (?, ?, ?, ?)";
        List<Object> params = new ArrayList<>() {{
            add(type);
            add(extraData);
            add(player.getUniqueId().toString());
            add(GsonComponentSerializer.gson().serialize(msg));
        }};
        hook.sqlUpdate(sql, params);
    }
    public void deleteNotification(String type, String extraData, OfflinePlayer player) {
        String sql = "DELETE FROM notifications WHERE extra_data = ? AND user_id = ? AND type = ?";
        List<Object> params = new ArrayList<>() {{
            add(extraData);
            add(player.getUniqueId().toString());
            add(type);
        }};
        hook.sqlUpdate(sql, params);
    }
    public boolean removeClaim(Player player, String claimName, RegionManager regionManager) {
        String claimId = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(claimName), Collections.singletonList("owner")).get(claimName).get(0);
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        int claimBlocksUsed = (int) claimData.get("blocks");

        if (claimId != null && regionManager.hasRegion(claimId)) {
            String sql = "DELETE FROM claims WHERE claim_id = ? OR parent_id = ?";
            List<Object> params = new ArrayList<>() {{
                add(claimId);
                add(claimId);
            }};
            hook.sqlUpdate(sql, params);
            int playerClaimBlocksUsed = getPlayersBlocks(Collections.singletonList(player)).get(player.getUniqueId()).get("used");
            int newClaimBlocksUsed = playerClaimBlocksUsed - claimBlocksUsed;
            sql = "UPDATE users SET claim_blocks_used = ? WHERE user_id = ?";
            params = new ArrayList<>() {{
                add(newClaimBlocksUsed);
                add(player.getUniqueId().toString());
            }};
            hook.sqlUpdate(sql, params);

            regionManager.removeRegion(claimId);
            return true;
        } else {
            return false;
        }
    }

    public void createClaim(Player player, String claimName, RegionManager regionManager, RegionSelector regionSelector, int playerClaimBlocks, int playerClaimBlocksUsed) {
        try {
            boolean nameTaken = hasClaimNamed(player, claimName);
            if(!nameTaken) {
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
                    claimBlocks = (int) new Polygonal2DRegion(BukkitAdapter.adapt(player.getWorld()), regionSel.getPoints(), minY, maxY).getVolume();
                } else {
                    BlockVector3 p1 = regionSelector.getRegion().getMinimumPoint();
                    BlockVector3 p2 = regionSelector.getRegion().getMaximumPoint();
                    region = new ProtectedCuboidRegion(claimId, BlockVector3.at(p1.getBlockX(), minY, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), maxY, p2.getBlockZ()));
                    claimBlocks = (int) new CuboidRegion(BlockVector3.at(p1.getBlockX(), minY, p1.getBlockZ()), BlockVector3.at(p2.getBlockX(), maxY, p2.getBlockZ())).getVolume();
                }

                ProtectedRegion parentRegion = null;

                List<ProtectedRegion> overlapRegions = region.getIntersectingRegions(regionManager.getRegions().values());

                if (overlapRegions.size() != 0) {
                    for (ProtectedRegion overlapRegion : overlapRegions) {
                        String overlapId = overlapRegion.getId();
                        if (overlapId.contains(player.getUniqueId().toString())) {
                            if (overlapRegion.getParent() == null) {
                                BlockVector3 regionMin = region.getMinimumPoint();
                                BlockVector3 regionMax = region.getMaximumPoint();

                                if (overlapRegion.contains(regionMax) && overlapRegion.contains(regionMin)) {
                                    parentRegion = overlapRegion;
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Your selection is partially outside the parent claim!")).color(NamedTextColor.RED));
                                    return;
                                }
                            } else {
                                String[] overlapName = overlapId.split("_");
                                double overlapX = overlapRegion.getMaximumPoint().getBlockX();
                                double overlapZ = overlapRegion.getMaximumPoint().getBlockZ();
                                player.sendMessage(prefix.append(Component.text("Your selection is overlapping with this child claim: " + overlapName[2] + " ( X: " + overlapX + " | Z: " + overlapZ + ")")).color(NamedTextColor.RED));
                                return;
                            }
                        } else {
                            String[] overlapName = overlapId.split("_");
                            double overlapX = overlapRegion.getMaximumPoint().getBlockX();
                            double overlapZ = overlapRegion.getMaximumPoint().getBlockZ();
                            if (overlapName.length > 1) {
                                CMIUser user = CMI.getInstance().getPlayerManager().getUser(UUID.fromString(overlapName[1]));
                                player.sendMessage(prefix.append(Component.text("Your selection is overlapping a claim owned by " + user.getName() + "! The claim is: " + overlapName[2] + " ( X: " + overlapX + " | Z: " + overlapZ + ")")
                                        .color(NamedTextColor.RED)));
                            } else {
                                player.sendMessage(prefix.append(Component.text("Your selection is overlapping with this claim: " + overlapName[0] + " ( X: " + overlapX + " | Z: " + overlapZ + ")")
                                        .color(NamedTextColor.RED)));
                            }
                            return;
                        }
                    }
                }

                if (parentRegion != null) {
                    region.setParent(parentRegion);
                    region.setPriority(2);
                    region.setFlags(parentRegion.getFlags());
                } else {
                    region.setPriority(1);
                }

                if (claimBlocks >= 36) {
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
                        String sql = "UPDATE users SET claim_blocks_used = ? WHERE user_id = ?";
                        List<Object> params = new ArrayList<>() {{
                            add(newClaimBlocksUsed);
                            add(player.getUniqueId().toString());
                        }};
                        hook.sqlUpdate(sql, params);
                        sql = "INSERT INTO claims (claim_id, claim_name, parent_id, world, blocks_used) VALUES (?, ?, ?, ?, ?)";
                        ProtectedRegion finalParentRegion = parentRegion;
                        params = new ArrayList<>() {{
                            add(region.getId());
                            add(claimName);
                            add(finalParentRegion.getId());
                            add(player.getWorld().getName());
                            add(claimBlocks);
                        }};
                        hook.sqlUpdate(sql, params);
                        sql = "INSERT INTO claims_members (user_id, claimd_id, claim_name, user_rank) VALUES (?, ?, ?, ?)";
                        params = new ArrayList<>() {{
                            add(player.getUniqueId().toString());
                            add(region.getId());
                            add(claimName);
                            add("owner");
                        }};
                        hook.sqlUpdate(sql, params);

                        player.sendMessage(prefix.append(Component.text("Successfully created a claim with the name ").color(TextColor.fromHexString("#20df80"))
                                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));
                    } else {
                        player.sendMessage(prefix.append(Component.text("You don't have enough claim blocks! You need " + ((playerClaimBlocksUsed + claimBlocks) - playerClaimBlocks) + " blocks more.").color(NamedTextColor.RED)));
                    }
                } else {
                    player.sendMessage(prefix.append(Component.text("Selected area is too small! Claims must be atleast 6x6 blocks in size.").color(NamedTextColor.RED)));
                }
            } else {
                player.sendMessage(prefix.append(Component.text("You already have a claim with that name!").color(NamedTextColor.RED)));
            }
        } catch (final Exception e){
            e.printStackTrace();
        }
    }

    public void expandClaim(Player player, String claimId, int amount, boolean isChild) {

    }

    public void helpMessage(Player player, int page) {
        int totalPages = 3;
        if(!player.hasPermission("skyprisoncore.command.claim.admin")) {
            totalPages = 2;
            if(page > 2) {
                page = 1;
            }
        }
        Component msg = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("SkyPrison Claims").color(TextColor.fromHexString("#a49a2b")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
        int prevPage = page - 1;
        int nextPage = page + 1;
        if (page == 1) {
            msg = msg.append(Component.text("/claim list ").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("List of your claims").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim info (claimname)").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Info about the claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim blocks ").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Info about your claim blocks usage").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim buyblocks <amount>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Buy more claimblocks").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim create <claimname>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Create a new claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim remove <claimname>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Remove a claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim flags (claim)").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("View/edit flags").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim addmember <player>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Add member to your claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim removemember <player>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Remove member from your claim").color(TextColor.fromHexString("#dbb976"))))

                    .append(Component.text("\n" + page).color(TextColor.fromHexString("#266d27")).append(Component.text("/").color(NamedTextColor.GRAY)
                    .append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->").color(NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/claim help " + nextPage))));
        } else if (page == 2) {
            msg = msg.append(Component.text("/claim addadmin <player>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Add an admin to your claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim removeadmin <player>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Remove an admin from your claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim transfer <claim> <player>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Transfer claim to another player").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim rename <claimname> <newClaimName>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Rename a claim").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim expand <amount>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Expand a claim in the direction you are facing").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim customheight").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Enable/disable custom height on claim create").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim customshape").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Enable/disable custom shape on claim create.").color(TextColor.fromHexString("#dbb976"))))
                    .append(Component.text("\n/claim nearby <radius>").color(TextColor.fromHexString("#d58f5d")).append(Component.text(" - ").color(NamedTextColor.GRAY))
                    .append(Component.text("Get a list of nearby claims").color(TextColor.fromHexString("#dbb976"))));

            if(page == totalPages) {
                msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/claim help " + prevPage)).append(Component.text(page).color(TextColor.fromHexString("#266d27")).append(Component.text("/")
                    .color(NamedTextColor.GRAY).append(Component.text(totalPages).color(TextColor.fromHexString("#266d27"))))));
            } else {
                msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
                        .clickEvent(ClickEvent.runCommand("/claim help " + prevPage)).append(Component.text(page).color(TextColor.fromHexString("#266d27"))
                        .append(Component.text("/").color(NamedTextColor.GRAY).append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))))
                        .append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))))
                        .clickEvent(ClickEvent.runCommand("/claim help " + nextPage)));
            }
        } else {
            msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/claim help " + prevPage))
                    .append(Component.text(page).color(TextColor.fromHexString("#266d27"))
                    .append(Component.text("/").color(NamedTextColor.GRAY)
                    .append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))))
                    .append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))))
                    .clickEvent(ClickEvent.runCommand("/claim help " + nextPage)));
        }
        msg = msg.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(msg);
    }

    public void claimList(Player player, RegionManager regionManager, int playerBlocks, int playerBlocksUsed, int page) {
        HashMap<String, String> userClaims = getUserClaims(Collections.singletonList(player), new ArrayList<>(), null, Arrays.asList("owner", "co-owner", "member"), false).get(player.getUniqueId());
        // claim_name, parent_id, blocks_used, user_rank

        List<String> claimsIds = userClaims.keySet().stream().toList();

        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimsIds);

        int totalPages = (int) Math.ceil((double) claimsData.size() / 10);

        if(page > totalPages) {
            page = 1;
        }

        Component msg = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("Claims List").color(TextColor.fromHexString("#0fc3ff")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));

        msg = msg.append(Component.text("\nTotal Blocks").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ")
                .color(NamedTextColor.GRAY)).append(Component.text(playerBlocksUsed + "/" + playerBlocks).color(TextColor.fromHexString("#ffba75"))));

        for(String claimId : claimsData.keySet()) {
            String name = claimsData.get(claimId).get("name").toString();
            String userRank = WordUtils.capitalize(userClaims.get(claimsData.get(claimId).get("id").toString()));
            int claimX = Objects.requireNonNull(regionManager.getRegion(claimsData.get(claimId).get("id").toString())).getMaximumPoint().getBlockX();
            int claimZ = Objects.requireNonNull(regionManager.getRegion(claimsData.get(claimId).get("id").toString())).getMaximumPoint().getBlockZ();
            int blocksUsed = (int) claimsData.get(claimId).get("blocks");

            Component parentInfo = Component.text("");
            if(claimsData.get(claimId).get("parent") != null) {
                String parentId = (String) claimsData.get(claimId).get("parent");
                String parentName = getClaimData(Collections.singletonList(parentId)).get(parentId).get("name").toString();

                name = name + " (Child)";
                parentInfo = Component.text("\nParent").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ")
                        .color(NamedTextColor.GRAY)).append(Component.text(parentName).color(TextColor.fromHexString("#ffba75")));
            }
            msg = msg.append(Component.text("\n- ").color(NamedTextColor.WHITE).append(Component.text(name).color(TextColor.fromHexString("#0fffc3"))
                    .append(Component.text(" ⇒ ").color(NamedTextColor.GRAY)).append(Component.text(userRank).color(TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\n").color(NamedTextColor.WHITE).decorate(TextDecoration.STRIKETHROUGH)
                            .append(Component.text("\nYour Rank").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ")
                                    .color(NamedTextColor.GRAY)).append(Component.text(userRank).color(TextColor.fromHexString("#ffba75"))))
                            .append(parentInfo)
                            .append(Component.text("\nCoords").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ")
                                    .color(NamedTextColor.GRAY)).append(Component.text("X " + claimX + " Y " + claimZ).color(TextColor.fromHexString("#ffba75"))))
                            .append(Component.text("\nBlocks Used").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ")
                                    .color(NamedTextColor.GRAY)).append(Component.text(blocksUsed).color(TextColor.fromHexString("#ffba75"))))
                            .append(Component.text("⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯").color(NamedTextColor.WHITE).decorate(TextDecoration.STRIKETHROUGH)))));
        }

        int nextPage = page + 1;
        int prevPage = page - 1;

        if(page == 1) {
            msg = msg.append(Component.text("\n" + page).color(TextColor.fromHexString("#266d27")).append(Component.text("/").color(NamedTextColor.GRAY)
                    .append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))).append(Component.text(" Next --->").color(NamedTextColor.GRAY)
                    .hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))).clickEvent(ClickEvent.runCommand("/claim help " + nextPage))));
        }
        if(page == totalPages) {
            msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page).color(TextColor.fromHexString("#266d27")).append(Component.text("/")
                            .color(NamedTextColor.GRAY).append(Component.text(totalPages).color(TextColor.fromHexString("#266d27"))))));
        } else {
            msg = msg.append(Component.text("\n<--- Prev ").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text("<<<").color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.runCommand("/claim list " + prevPage)).append(Component.text(page).color(TextColor.fromHexString("#266d27"))
                            .append(Component.text("/").color(NamedTextColor.GRAY).append(Component.text(totalPages).color(TextColor.fromHexString("#266d27")))))
                    .append(Component.text(" Next --->").color(NamedTextColor.GRAY).hoverEvent(HoverEvent.showText(Component.text(">>>").color(NamedTextColor.GRAY))))
                    .clickEvent(ClickEvent.runCommand("/claim list " + nextPage)));
        }

        msg = msg.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(msg);
    }

    public void claimInfoMultiple(Player player, List<String> claimIds) {
        Component info = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("Claim Info").color(TextColor.fromHexString("#a49a2b")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ").color(TextColor.fromHexString("#a49a2b")));

        HashMap<String, UUID> ownersData = getClaimOwners(claimIds);
        HashMap<String, HashMap<String, Object>> claimsData = getClaimData(claimIds);

        for(String claimId : ownersData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(ownersData.get(claimId));
            info = info.append(Component.text("\n- ").color(NamedTextColor.WHITE).append(Component.text(claimsData.get(claimId).get("name").toString()).color(TextColor.fromHexString("#0fffc3"))
                    .append(Component.text(" ⇒ ").color(NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName()).color(TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to show info for " + claimsData.get(claimId).get("name").toString()).color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        claimInfo(player, claimId);
                    })));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void claimInfo(Player player, String claimId) {
        Component info = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("Claim Info").color(TextColor.fromHexString("#a49a2b")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        String claimName = (String) claimData.get("name");
        String parentId = (String) claimData.get("parent");
        int blocksUsed = (int) claimData.get("blocks");

        Connection conn = hook.getSQLConnection();

        info = info.append(Component.text("\nName").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ").color(NamedTextColor.GRAY))
                .append(Component.text(claimName).color(TextColor.fromHexString("#ffba75"))));

        if(parentId != null && !parentId.isEmpty()) {
            String parentName = (String) getClaimData(Collections.singletonList(parentId)).get(claimId).get("name");
            info = info.append(Component.text("\nParent").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ").color(NamedTextColor.GRAY))
                    .append(Component.text(parentName).color(TextColor.fromHexString("#ffba75"))).clickEvent(ClickEvent.runCommand("/claim info " + parentName)));
        }

        HashMap<UUID, String> members = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner", "member")).get(claimId);
        boolean canEdit = members.containsKey(player.getUniqueId()) && (members.get(player.getUniqueId()).equalsIgnoreCase("owner") || members.get(player.getUniqueId()).equalsIgnoreCase("co-owner"));

        hook.close(null, null, conn);
        OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(getClaimOwners(Collections.singletonList(claimId)).get(claimId));
        info = info.append(Component.text("\nBlocks Used").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ").color(NamedTextColor.GRAY))
                .append(Component.text(blocksUsed).color(TextColor.fromHexString("#ffba75"))));

        info = info.append(Component.text("\nOwner").color(TextColor.fromHexString("#0fffc3")).append(Component.text(" ⇒ ").color(NamedTextColor.GRAY))
                .append(Component.text(Objects.requireNonNull(oPlayer.getName())).color(TextColor.fromHexString("#ffba75"))));

        info = info.append(Component.text("\n\nVIEW MEMBERS").color(TextColor.fromHexString("#0fffc3")).clickEvent(ClickEvent.callback(audience -> {
            player.openInventory(new ClaimMembers(plugin, claimName, members, 1).getInventory());
        })));

        info = info.append(Component.text("\nVIEW FLAGS").color(TextColor.fromHexString("#0fffc3")).clickEvent(ClickEvent.callback(audience -> {
            player.openInventory(new ClaimFlags(plugin, claimId, claimData.get("world").toString(), canEdit, "", 1).getInventory());
        })));

        info = info.decoration(TextDecoration.ITALIC, false);

        player.sendMessage(info);
    }

    public void invitePlayerMultiple(Player player, CMIUser iUser, List<String> claimIds) {
        Component info = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("Claim Invite").color(TextColor.fromHexString("#a49a2b")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick one: ").color(TextColor.fromHexString("#a49a2b")));

        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);

        for(String claimId : claimData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(owners.get(claimId));
            info = info.append(Component.text("\n- ").color(NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString()).color(TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ").color(NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName()).color(TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to invite player to " + claimData.get(claimId).get("name").toString()).color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        invitePlayer(player, iUser, claimId);
                    })));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void invitePlayer(Player player, CMIUser iUser, String claimId) {
        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");
        Component msg = prefix.append(Component.text("You've been invited to the claim ").color(TextColor.fromHexString("#20df80"))
                        .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                        .append(Component.text(" by ").color(TextColor.fromHexString("#20df80")))
                        .append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)))
                .append(Component.text("\nACCEPT INVITE").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                    inviteAccept(iUser.getPlayer(), claimId);
                })))
                .append(Component.text("               "))
                .append(Component.text("\nDECLINE INVITE").color(NamedTextColor.RED).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                    inviteDecline(iUser.getPlayer(), claimId);
                })));

        if(iUser.isOnline()) {

            iUser.getPlayer().sendMessage(msg);
            player.sendMessage(prefix.append(Component.text("Successfully invited " + iUser.getName() + " to the claim!").color(TextColor.fromHexString("#20df80"))));
        } else {
            createNotification("claim-invite", claimId, iUser.getOfflinePlayer(), msg);
            player.sendMessage(prefix.append(Component.text("Successfully invited " + iUser.getName() + " to the claim! They'll get an invite once they're online.").color(TextColor.fromHexString("#20df80"))));
        }
    }

    public void inviteDecline(Player player, String claimId) {
        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");
        HashMap<UUID, String> toNotify = getClaimUsers(Collections.singletonList(claimId), Arrays.asList("owner", "co-owner")).get(claimId);

        deleteNotification("claim-invite", claimId, player);

        player.sendMessage(prefix.append(Component.text("You've successfully declined the invite to join the claim ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));

        for(UUID pUUID : toNotify.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(pUUID);
            Component msg = prefix.append(Component.text("Player ").color(TextColor.fromHexString("#20df80"))
                    .append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                    .append(Component.text(" has REJECTED the invite to join the claim ").color(TextColor.fromHexString("#20df80")))
                    .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)));
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


        String sql = "INSERT INTO claims_members (user_id, claimd_id, user_rank) VALUES (?, ?, ?)";
        List<Object> params = new ArrayList<>() {{
            add(player.getUniqueId().toString());
            add(claimId);
            add("member");
        }};
        hook.sqlUpdate(sql, params);

        player.sendMessage(prefix.append(Component.text("You've successfully joined the claim ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));

        for(UUID pUUID : toNotify.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(pUUID);
                Component msg = prefix.append(Component.text("Player ").color(TextColor.fromHexString("#20df80"))
                        .append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                        .append(Component.text(" has accepted the invite to join the claim ").color(TextColor.fromHexString("#20df80")))
                        .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)));
            if(oPlayer.isOnline()) {
                Objects.requireNonNull(oPlayer.getPlayer()).sendMessage(msg);
            } else {
                createNotification("claim-invite-accepted", claimId, oPlayer, msg);
            }
        }
    }
    public void kickPlayerMultiple(Player player, CMIUser kickedPlayer, List<String> claimIds) {
        Component info = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("Claim Kick").color(TextColor.fromHexString("#a49a2b")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to kick player from: ").color(TextColor.fromHexString("#a49a2b")));

        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);


        for(String claimId : claimData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(owners.get(claimId));
            info = info.append(Component.text("\n- ").color(NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString()).color(TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ").color(NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName()).color(TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to kick player from " + claimData.get(claimId).get("name")).color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> {
                        kickPlayer(player, kickedPlayer, claimId);
                    })));
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

        String sql = "DELETE FROM claims_members WHERE claim_id = ? AND user_id = ?";
        List<Object> params = new ArrayList<>() {{
            add(claimId);
            add(kickedPlayer.getUniqueId().toString());
        }};
        hook.sqlUpdate(sql, params);

        player.sendMessage(prefix.append(Component.text("Successfully kicked ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(kickedPlayer.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                .append(Component.text(" from the claim ").color(TextColor.fromHexString("#20df80")))
                .append(Component.text(claimData.get("name").toString()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("You've been kicked from the claim ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(claimData.get("name").toString()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                .append(Component.text(" by ").color(TextColor.fromHexString("#20df80")))
                .append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)));
        if(kickedPlayer.isOnline()) {
            kickedPlayer.getPlayer().sendMessage(msg);
        } else {
            createNotification("claim-kick", claimId, kickedPlayer.getOfflinePlayer(), msg);
        }
    }

    public void promotePlayer(Player player, CMIUser promotedPlayer, String claimId) {
        String sql = "UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?";
        List<Object> params = new ArrayList<>() {{
            add("co-owner");
            add(promotedPlayer.getUniqueId().toString());
            add(claimId);
        }};
        hook.sqlUpdate(sql, params);
        String claimName = getClaimData(Collections.singletonList(claimId)).get(claimId).get("name").toString();
        Component msg = prefix.append(Component.text("You've been promoted in the claim ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                .append(Component.text(" by ").color(TextColor.fromHexString("#20df80")))
                .append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)));

        player.sendMessage(prefix.append(Component.text("Successfully promoted ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(promotedPlayer.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                .append(Component.text(" in the claim ").color(TextColor.fromHexString("#20df80")))
                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));
        if(promotedPlayer.isOnline()) {
            promotedPlayer.getPlayer().sendMessage(msg);
        } else {
            createNotification("claim-promote", claimId, promotedPlayer.getOfflinePlayer(), msg);
        }
    }

    public void demotePlayer(Player player, CMIUser demotedPlayer, String claimId) {
        String sql = "UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?";
        List<Object> params = new ArrayList<>() {{
            add("member");
            add(demotedPlayer.getUniqueId().toString());
            add(claimId);
        }};
        hook.sqlUpdate(sql, params);
        String claimName = getClaimData(Collections.singletonList(claimId)).get(claimId).get("name").toString();
        Component msg = prefix.append(Component.text("You've been demoted in the claim ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                .append(Component.text(" by ").color(TextColor.fromHexString("#20df80")))
                .append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)));

        player.sendMessage(prefix.append(Component.text("Successfully demoted ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(demotedPlayer.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                .append(Component.text(" in the claim ").color(TextColor.fromHexString("#20df80")))
                .append(Component.text(claimName).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));
        if(demotedPlayer.isOnline()) {
            demotedPlayer.getPlayer().sendMessage(msg);
        } else {
            createNotification("claim-demote", claimId, demotedPlayer.getOfflinePlayer(), msg);
        }
    }

    public void transferClaim(Player player, CMIUser transferPlayer, String claimId) {
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        int claimBlocks = (int) claimData.get("blocks");

        int pClaimBlocks = 0;
        int pClaimBlocksUsed = 0;
        Connection conn = hook.getSQLConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?");
            ps.setString(1, transferPlayer.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pClaimBlocks = rs.getInt(1);
                pClaimBlocksUsed = rs.getInt(2);
            }
            hook.close(ps, rs, conn);
        } catch (SQLException e) {
            hook.close(null, null, conn);
            e.printStackTrace();
        }

        int pClaimBlocksLeft = pClaimBlocks - pClaimBlocksUsed;

        if(claimBlocks > 0 && claimBlocks < pClaimBlocksLeft) {
            Component msg = prefix.append(Component.text(player.getName()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD)
                            .append(Component.text(" wants to transfer the claim ").color(TextColor.fromHexString("#20df80")))
                            .append(Component.text(claimData.get("name").toString()).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))
                            .append(Component.text(" to you!").color(TextColor.fromHexString("#20df80"))))
                    .append(Component.text("\nACCEPT TRANSFER").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                        transferAccept(player, transferPlayer.getPlayer(), claimId);
                    })))
                    .append(Component.text("               "))
                    .append(Component.text("\nDECLINE TRANSFER").color(NamedTextColor.RED).decorate(TextDecoration.BOLD).clickEvent(ClickEvent.callback(audience -> {
                        transferDecline(player, transferPlayer.getPlayer(), claimId);
                    })));

            if (transferPlayer.isOnline()) {
                transferPlayer.getPlayer().sendMessage(msg);
                player.sendMessage(prefix.append(Component.text("Successfully sent a transfer request to " + transferPlayer.getName() + "!").color(TextColor.fromHexString("#20df80"))));
            } else {
                createNotification("claim-transfer", claimId, transferPlayer.getOfflinePlayer(), msg);
                player.sendMessage(prefix.append(Component.text("Successfully created transfer request! " + transferPlayer.getName() + " will receive the request when they log on. ").color(TextColor.fromHexString("#20df80"))));
            }
        } else {
            player.sendMessage(prefix.append(Component.text(transferPlayer.getName()).color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                    .append(Component.text(" doesn't have enough claim blocks!").color(NamedTextColor.RED))));
        }
    }

    public void transferDecline(OfflinePlayer player, Player transferPlayer, String claimId) {
        deleteNotification("claim-transfer", claimId, transferPlayer);

        String claimName = (String) getClaimData(Collections.singletonList(claimId)).get(claimId).get("name");

        transferPlayer.sendMessage(prefix.append(Component.text("You've successfully declined the transfer to become the owner of the claim ").color(TextColor.fromHexString("#20df80"))
                .append(Component.text(claimName).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))));
        Component msg = prefix.append(Component.text("Player ").color(NamedTextColor.RED)
                .append(Component.text(transferPlayer.getName()).color(NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .append(Component.text(" has declined the transfer request to become the owner of the claim ").color(NamedTextColor.RED))
                .append(Component.text(claimName).color(NamedTextColor.RED).decorate(TextDecoration.BOLD)));
        if (player.isOnline()) {
            Objects.requireNonNull(player.getPlayer()).sendMessage(msg);
        } else {
            createNotification("claim-transfer-declined", claimId, player, msg);
        }
    }

    public void transferAccept(OfflinePlayer player, Player transferPlayer, String claimId) {
        HashMap<String, Object> claimData = getClaimData(Collections.singletonList(claimId)).get(claimId);
        int claimBlocks = (int) claimData.get("blocks");
        String claimName = (String) claimData.get("name");
        int pBlocks = hasNeededBlocks(transferPlayer, claimBlocks);
        if(pBlocks != -1) {
            String oldClaimName = claimName;
            boolean hasName = hasClaimNamed(transferPlayer, claimName);

            if (hasName) {
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

            String sql = "UPDATE users SET blocks_used = ? WHERE user_id = ?";
            List<Object> params = new ArrayList<>() {{
                add(pBlocks);
                add(transferPlayer.getUniqueId().toString());
            }};
            hook.sqlUpdate(sql, params);

            sql = "UPDATE users SET blocks_used = blocks_used - ? WHERE user_id = ?";
            params = new ArrayList<>() {{
                add(claimBlocks);
                add(player.getUniqueId().toString());
            }};
            hook.sqlUpdate(sql, params);

            sql = "UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?";
            params = new ArrayList<>() {{
                add("owner");
                add(transferPlayer.getUniqueId().toString());
                add(claimId);
            }};
            hook.sqlUpdate(sql, params);

            sql = "UPDATE claims_members SET user_rank = ? WHERE user_id = ? AND claim_id = ?";
            params = new ArrayList<>() {{
                add("co-owner");
                add(player.getUniqueId().toString());
                add(claimId);
            }};
            hook.sqlUpdate(sql, params);


            if (hasName) {
                transferPlayer.sendMessage(prefix.append(Component.text("You already own a claim with the name ").color(TextColor.fromHexString("#20df80"))
                        .append(Component.text(oldClaimName).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))
                        .append(Component.text("! Renaming transferred claim to ").color(TextColor.fromHexString("#20df80")))
                        .append(Component.text(claimName).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))));
            }
            transferPlayer.sendMessage(prefix.append(Component.text("You're now the owner of the claim ").color(TextColor.fromHexString("#20df80"))
                    .append(Component.text(claimName).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))));

            Component msg = prefix.append(Component.text("Your claim ").color(TextColor.fromHexString("#20df80"))
                    .append(Component.text(oldClaimName).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))
                    .append(Component.text(" was successfully transferred to ").color(TextColor.fromHexString("#20df80")))
                    .append(Component.text(transferPlayer.getName()).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD)));
            if (player.isOnline()) {
                Objects.requireNonNull(player.getPlayer()).sendMessage(msg);
            } else {
                createNotification("claim-transfer-accepted", claimId, player, msg);
            }
        } else {
            transferPlayer.sendMessage(prefix.append(Component.text("You don't have enough claim blocks for this claim transfer! Cancelling transfer..").color(NamedTextColor.RED)));
        }
        deleteNotification("claim-transfer", claimId, transferPlayer);
    }

    public void claimFlagsMultiple(Player player, HashMap<String, String> userRanks) {
        Component info = Component.text("⎯⎯⎯⎯⎯⎯ ").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("Claim Promote").color(TextColor.fromHexString("#a49a2b")).decorate(TextDecoration.BOLD))
                .append(Component.text(" ⎯⎯⎯⎯⎯⎯").color(NamedTextColor.GRAY).decorate(TextDecoration.STRIKETHROUGH));
        info = info.append(Component.text("\nMore than 1 claim found! Please pick the one to promote the player in: ").color(TextColor.fromHexString("#a49a2b")));
        List<String> claimIds = userRanks.keySet().stream().toList();
        HashMap<String, HashMap<String, Object>> claimData = getClaimData(claimIds);
        HashMap<String, UUID> owners = getClaimOwners(claimIds);

        for(String claimId : claimData.keySet()) {
            OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(owners.get(claimId));
            info = info.append(Component.text("\n- ").color(NamedTextColor.WHITE).append(Component.text(claimData.get(claimId).get("name").toString()).color(TextColor.fromHexString("#0fffc3"))
                            .append(Component.text(" ⇒ ").color(NamedTextColor.GRAY)).append(Component.text("Owned By " + oPlayer.getName()).color(TextColor.fromHexString("#ffba75"))))
                    .hoverEvent(HoverEvent.showText(Component.text("Click here to view flags for " + claimData.get(claimId).get("name").toString()).color(NamedTextColor.GRAY)))
                    .clickEvent(ClickEvent.callback(audience -> claimFlags(player, claimId, claimData.get("world").toString(), userRanks.get(claimId)))));
        }
        info = info.decoration(TextDecoration.ITALIC, false);
        player.sendMessage(info);
    }

    public void claimFlags(Player player, String claimId, String world, String userRank) {
        boolean canEdit = userRank.equalsIgnoreCase("owner") || userRank.equalsIgnoreCase("co-owner");
        ClaimFlags claimFlags = new ClaimFlags(plugin, claimId, world, canEdit, "", 1);
        player.openInventory(claimFlags.getInventory());
    }

    public HashMap<String, HashMap<UUID, String>> getClaimUsers(List<String> claimIds, List<String> ranks) {
        HashMap<String, HashMap<UUID, String>> userClaims = new HashMap<>();
        Connection conn = hook.getSQLConnection();
        if(claimIds != null) {
            if(!claimIds.isEmpty()) {
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members WHERE claim_id IN ? AND user_rank IN ?");
                    ps.setArray(1, conn.createArrayOf("TEXT", claimIds.toArray()));
                    ps.setArray(2, conn.createArrayOf("TEXT", ranks.toArray()));
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String claimId = rs.getString(1);
                        UUID user = UUID.fromString(rs.getString(2));
                        HashMap<UUID, String> userData = new HashMap<>();
                        if(userClaims.containsKey(claimId)) userData = userClaims.get(claimId);
                        userData.put(user, rs.getString(3));
                        userClaims.put(claimId, userData);
                    }
                    hook.close(ps, rs, null);
                } catch (SQLException e) {
                    hook.close(null, null, conn);
                    e.printStackTrace();
                }
            } else {
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT claim_id, user_id, user_rank FROM claims_members WHERE user_rank IN ?");
                    ps.setArray(1, conn.createArrayOf("TEXT", ranks.toArray()));
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String claimId = rs.getString(1);
                        UUID user = UUID.fromString(rs.getString(2));
                        HashMap<UUID, String> userData = new HashMap<>();
                        if(userClaims.containsKey(claimId)) userData = userClaims.get(claimId);
                        userData.put(user, rs.getString(3));
                        userClaims.put(claimId, userData);
                    }
                    hook.close(ps, rs, null);
                } catch (SQLException e) {
                    hook.close(null, null, conn);
                    e.printStackTrace();
                }
            }
        }
        hook.close(null, null, conn);
        return userClaims;
    }
    public HashMap<UUID, HashMap<String, String>> getUserClaims(List<OfflinePlayer> players, List<String> claimIds, RegionManager regionManager, List<String> ranks, boolean getLocationRegardless) {
        HashMap<UUID, HashMap<String, String>> userClaims = new HashMap<>();
        List<String> playerIds = new ArrayList<>();
        players.forEach(i -> playerIds.add(i.getUniqueId().toString()));
        Connection conn = hook.getSQLConnection();
        if(claimIds != null) {
            if(!claimIds.isEmpty()) {
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN ? AND claim_id IN ? AND user_rank IN ?");
                    ps.setArray(1, conn.createArrayOf("TEXT", playerIds.toArray()));
                    ps.setArray(2, conn.createArrayOf("TEXT", claimIds.toArray()));
                    ps.setArray(3, conn.createArrayOf("TEXT", ranks.toArray()));
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        UUID user = UUID.fromString(rs.getString(1));
                        HashMap<String, String> claims = new HashMap<>();
                        if(userClaims.containsKey(user)) claims = userClaims.get(user);
                        claims.put(rs.getString(2), rs.getString(3));
                        userClaims.put(user, claims);
                    }
                    hook.close(ps, rs, null);
                } catch (SQLException e) {
                    hook.close(null, null, conn);
                    e.printStackTrace();
                }
            } else {
                try {
                    PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN ? AND user_rank IN ?");
                    ps.setArray(1, conn.createArrayOf("TEXT", playerIds.toArray()));
                    ps.setArray(2, conn.createArrayOf("TEXT", ranks.toArray()));
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        UUID user = UUID.fromString(rs.getString(1));
                        HashMap<String, String> claims = new HashMap<>();
                        if(userClaims.containsKey(user)) claims = userClaims.get(user);
                        claims.put(rs.getString(2), rs.getString(3));
                        userClaims.put(user, claims);
                    }
                    hook.close(ps, rs, null);
                } catch (SQLException e) {
                    hook.close(null, null, conn);
                    e.printStackTrace();
                }
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
                            try {
                                PreparedStatement ps = conn.prepareStatement("SELECT user_id, claim_id, user_rank FROM claims_members WHERE user_id IN ? AND claim_id IN ? AND user_rank IN ?");
                                ps.setArray(1, conn.createArrayOf("TEXT", playerIds.toArray()));
                                ps.setArray(2, conn.createArrayOf("TEXT", regionIds.toArray()));
                                ps.setArray(3, conn.createArrayOf("TEXT", ranks.toArray()));
                                ResultSet rs = ps.executeQuery();
                                while (rs.next()) {
                                    UUID user = UUID.fromString(rs.getString(1));
                                    HashMap<String, String> claims = new HashMap<>();
                                    if (userClaims.containsKey(user)) claims = userClaims.get(user);
                                    claims.put(rs.getString(2), rs.getString(3));
                                    userClaims.put(user, claims);
                                }
                                hook.close(ps, rs, null);
                            } catch (SQLException e) {
                                hook.close(null, null, conn);
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
        hook.close(null, null, conn);
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

            List<String> claimIds = null;

            try {
                Connection conn = hook.getSQLConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT claim_blocks, claim_blocks_used FROM users WHERE user_id = ?");
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    claimBlocks = rs.getInt(1);
                    claimBlocksUsed = rs.getInt(2);
                }
                hook.close(ps, rs, conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "remove" -> { // /claim remove <claimname>
                        if (args.length > 1) {
                            if (removeClaim(player, args[1], regionManager)) {
                                player.sendMessage(prefix.append(Component.text("Successfully removed the claim with the name ").color(TextColor.fromHexString("#20df80"))
                                        .append(Component.text(args[1]).color(TextColor.fromHexString("#20df80")).decorate(TextDecoration.BOLD))));
                            } else {
                                player.sendMessage(prefix.append(Component.text("You don't have any claims with that name!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Correct usage: /claim remove <claimname>").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false)));
                        }
                    }
                    case "create" -> {
                        if (args.length >= 2 && args[1] != null) {
                            if (player.getWorld().getName().equalsIgnoreCase("world_free")) {
                                if (regionSelector.isDefined()) {
                                    createClaim(player, args[1], regionManager, regionSelector, claimBlocks, claimBlocksUsed);
                                } else {
                                    player.sendMessage(prefix.append(Component.text("You havn't created a selection!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Claiming is not allowed in this world!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Correct usage: /claim create <claimname>").color(NamedTextColor.RED)));
                        }
                    }
                    case "list" -> {
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
                            player.sendMessage(prefix.append(Component.text("Custom height claiming disabled!").color(NamedTextColor.RED)));
                        } else {
                            plugin.customClaimHeight.put(player.getUniqueId(), true);
                            player.sendMessage(prefix.append(Component.text("Custom height claiming enabled!").color(NamedTextColor.GREEN)));
                        }
                    }
                    case "customshape" -> {
                        LocalSession session = WorldEdit.getInstance().getSessionManager().get(BukkitAdapter.adapt(player));
                        final RegionSelector newSelector;
                        if(plugin.customClaimShape.containsKey(player.getUniqueId())) {
                            plugin.customClaimShape.remove(player.getUniqueId());
                            newSelector = new CuboidRegionSelector(regionSelector);
                            player.sendMessage(prefix.append(Component.text("Custom shape claiming disabled!").color(NamedTextColor.RED)));
                        } else {
                            plugin.customClaimShape.put(player.getUniqueId(), true);
                            newSelector = new Polygonal2DRegionSelector(regionSelector);
                            player.sendMessage(prefix.append(Component.text("Custom shape claiming enabled!").color(NamedTextColor.GREEN)));
                        }
                        session.setRegionSelector(BukkitAdapter.adapt(player.getWorld()), newSelector);
                    }
                    case "info" -> { // claim info (claim)
                        if(args.length > 1) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), Arrays.asList("owner", "co-owner", "member")).get(args[2]);
                        List<String> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, Arrays.asList("owner", "co-owner", "member"), true).get(player.getUniqueId()).keySet().stream().toList();
                        if(!userClaims.isEmpty()) {
                            if(userClaims.size() == 1) {
                                claimInfo(player, userClaims.get(0));
                            } else {
                                claimInfoMultiple(player, userClaims);
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                        }
                    }
                    case "invite" -> { // claim invite <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), Arrays.asList("owner", "co-owner")).get(args[2]);
                                List<String> userClaims = new ArrayList<>(getUserClaims(Collections.singletonList(player), claimIds, regionManager, Arrays.asList("owner", "co-owner"), false).get(player.getUniqueId()).keySet().stream().toList());

                                HashMap<String, String> alreadyMember = getUserClaims(Collections.singletonList(iUser.getOfflinePlayer()), claimIds, null,
                                        Arrays.asList("owner", "co-owner", "member", "banned"), false).get(iUser.getUniqueId());

                                if (!userClaims.isEmpty()) {
                                    userClaims.removeAll(alreadyMember.keySet());
                                    if (!userClaims.isEmpty()) {
                                        List<String> alreadyInvited = hasNotifications("claim-invite", userClaims, iUser.getOfflinePlayer());
                                        userClaims.removeAll(alreadyInvited);
                                        if(!userClaims.isEmpty()) {
                                            if (userClaims.size() == 1) {
                                                invitePlayer(player, iUser, userClaims.get(0));
                                            } else {
                                                invitePlayerMultiple(player, iUser, userClaims);
                                            }
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("The player already has an invite to this claim!").color(NamedTextColor.RED)));
                                        }
                                    } else {
                                        if(alreadyMember.size() == 1) {
                                            if (alreadyMember.containsValue("banned")) {
                                                player.sendMessage(prefix.append(Component.text("The player is banned from this claim!").color(NamedTextColor.RED)));
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("The player is already in this claim!").color(NamedTextColor.RED)));
                                            }
                                        }
                                        player.sendMessage(prefix.append(Component.text("The player is already in this claim!").color(NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim invite <player> (claim)").color(NamedTextColor.RED)));
                        }
                    }
                    case "kick" -> { // claim kick <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), Arrays.asList("owner", "co-owner")).get(args[2]);
                                HashMap<String, String> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, Arrays.asList("owner", "co-owner"), false).get(player.getUniqueId());

                                HashMap<String, String> isMember = getUserClaims(Collections.singletonList(iUser.getOfflinePlayer()), claimIds, null,
                                        Arrays.asList("owner", "co-owner", "member"),false).get(iUser.getUniqueId());

                                if(!userClaims.isEmpty()) {
                                    if(!isMember.isEmpty()) {
                                        if (isMember.size() == 1) {
                                            String claimId = isMember.keySet().stream().toList().get(0);
                                            if(isMember.containsValue("member") || userClaims.get(claimId).equalsIgnoreCase("owner")) {
                                                kickPlayer(player, iUser, claimId);
                                            } else {
                                                player.sendMessage(prefix.append(Component.text("You can't kick this player!").color(NamedTextColor.RED)));
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
                                                player.sendMessage(prefix.append(Component.text("You can't kick this player!").color(NamedTextColor.RED)));
                                            }
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!").color(NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim kick <player> (claim)").color(NamedTextColor.RED)));
                        }
                    }
                    case "promote" -> { // claim promote <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), List.of("owner")).get(args[2]);
                                HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, List.of("owner"), false);


                                if(!userClaims.isEmpty()) {
                                    String claimId = userClaims.get(player.getUniqueId()).keySet().stream().toList().get(0);
                                    HashMap<UUID, HashMap<String, String>> promoteClaims = getUserClaims(Collections.singletonList(iUser.getOfflinePlayer()), Collections.singletonList(claimId),
                                            regionManager, Arrays.asList("owner", "co-owner", "member"), false);
                                    if(!promoteClaims.isEmpty()) {
                                        String userRank = promoteClaims.get(iUser.getUniqueId()).get(claimId);
                                        if (userRank.equalsIgnoreCase("member")) {
                                            promotePlayer(player, iUser, claimId);
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("This player has already been promoted!").color(NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!").color(NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim promote <player> (claim)").color(NamedTextColor.RED)));
                        }
                    }
                    case "demote" -> { // claim demote <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), List.of("owner")).get(args[2]);
                                HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    String claimId = userClaims.get(player.getUniqueId()).keySet().stream().toList().get(0);
                                    HashMap<UUID, HashMap<String, String>> demoteClaims = getUserClaims(Collections.singletonList(iUser.getOfflinePlayer()), Collections.singletonList(claimId),
                                            regionManager, Arrays.asList("owner", "co-owner", "member"), false);
                                    if(!demoteClaims.isEmpty()) {
                                        String userRank = demoteClaims.get(iUser.getUniqueId()).get(claimId);
                                        if (userRank.equalsIgnoreCase("co-owner")) {
                                            demotePlayer(player, iUser, claimId);
                                        } else {
                                            player.sendMessage(prefix.append(Component.text("This player can't be demoted!").color(NamedTextColor.RED)));
                                        }
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player isn't a member of this claim!").color(NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim demote <player> (claim)").color(NamedTextColor.RED)));
                        }
                    }
                    case "transfer" -> { // claim transfer <player> (claim)
                        if(args.length > 1) {
                            if(CMI.getInstance().getPlayerManager().getUser(args[1]) != null) {
                                CMIUser iUser = CMI.getInstance().getPlayerManager().getUser(args[1]);
                                if(args.length > 2) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), List.of("owner")).get(args[2]);
                                HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, List.of("owner"), false);

                                if(!userClaims.isEmpty()) {
                                    String claimId = userClaims.get(player.getUniqueId()).keySet().stream().toList().get(0);
                                    HashMap<UUID, HashMap<String, String>> transferClaims = getUserClaims(Collections.singletonList(iUser.getOfflinePlayer()), Collections.singletonList(claimId),
                                            regionManager, List.of("co-owner"), false);
                                    if(!transferClaims.isEmpty()) {
                                        transferClaim(player, iUser, claimId);
                                    } else {
                                        player.sendMessage(prefix.append(Component.text("The player must be a promoted member of the claim!").color(NamedTextColor.RED)));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("You don't have any claims with that name!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No such player exists!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim demote <player> (claim)").color(NamedTextColor.RED)));
                        }
                    }
                    case "flags" -> { // claim flags (claim)
                            if(args.length > 1) claimIds = getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[2]), List.of("owner", "co-owner", "member")).get(args[2]);
                            HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(Collections.singletonList(player), claimIds, regionManager, List.of("owner", "co-owner", "member"), true);

                            if(!userClaims.isEmpty()) {
                                HashMap<String, String> userRank = userClaims.get(player.getUniqueId());

                                if(userRank.size() == 1) {
                                    String claimId = userRank.keySet().stream().toList().get(0);
                                    String world = getClaimData(Collections.singletonList(claimId)).get(claimId).get("world").toString();
                                    claimFlags(player, claimId, world, userRank.get(claimId));
                                } else {
                                    claimFlagsMultiple(player, userRank);
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                            }
                    }
                    case "wand" -> player.performCommand("//wand");
                    case "buyclaimblocks", "buyblocks" -> { // claim buyblocks <amount>
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                int blocks = Integer.parseInt(args[1]);
                                double price = 40 * blocks;
                                if(user.getBalance() >= price) {
                                    user.withdraw(price);
                                    String sql = "UPDATE users SET claim_blocks = claim_blocks + ? WHERE user_id = ?";
                                    List<Object> params = new ArrayList<>() {{
                                        add(blocks);
                                        add(user.getUniqueId().toString());
                                    }};
                                    hook.sqlUpdate(sql, params);
                                    player.sendMessage(prefix.append(Component.text("Successfully bought ").color(TextColor.fromHexString("#20df80"))
                                            .append(Component.text(plugin.formatNumber(blocks)).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))
                                            .append(Component.text(" blocks for $").color(TextColor.fromHexString("#20df80")))
                                            .append(Component.text(plugin.formatNumber(price)).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))));
                                } else {
                                    int needed = (int) (price - user.getBalance());
                                    player.sendMessage(prefix.append(Component.text("You don't have enough money! You need $" + plugin.formatNumber(needed) + " more..").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim buyblocks <amount>").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim buyblocks <amount>").color(NamedTextColor.RED)));
                        }
                    }
                    case "expand" -> { // claim expand <amount>
                        if(args.length > 1) {
                            if(plugin.isInt(args[1])) {
                                HashMap<UUID, HashMap<String, String>> userClaims = getUserClaims(Collections.singletonList(player), null, regionManager, List.of("owner"), false);

                                if (!userClaims.isEmpty()) { // .get(player.getUniqueId()).keySet().stream().toList();
                                    List<String> claims = userClaims.get(player.getUniqueId()).keySet().stream().toList();

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
                                    player.sendMessage(prefix.append(Component.text("No claims were found!").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! Amount must be a number!").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim expand <amount>").color(NamedTextColor.RED)));

                        }
                    }
                    case "rename" -> { // claim rename <current> <new>
                        if(args.length > 2) {
                            if(hasClaimNamed(player, args[1])) {
                                if(updateClaimName(getClaimIdsFromNames(Collections.singletonList(player), Collections.singletonList(args[1]), Collections.singletonList("owner")).get(args[1]).get(0), args[2])) {
                                    player.sendMessage(prefix.append(Component.text("Successfully renamed the claim from ").color(TextColor.fromHexString("#20df80"))
                                            .append(Component.text(args[1]).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD))
                                            .append(Component.text(" to ").color(TextColor.fromHexString("#20df80")))
                                            .append(Component.text(args[2])).color(TextColor.fromHexString("#ffba75")).decorate(TextDecoration.BOLD)));
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Something went wrong during renaming! Try again later..").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("You don't own any claims with the name ").color(NamedTextColor.RED)
                                        .append(Component.text(args[1]).color(NamedTextColor.RED).decorate(TextDecoration.BOLD))));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim rename <curren namet> <new name>").color(NamedTextColor.RED)));
                        }
                    }
                    case "nearbyclaims", "nearby" -> { // claim nearby <radius>
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
                                        player.sendMessage(prefix.append(Component.text("No claims found!").color(TextColor.fromHexString("#20df80"))));
                                    }
                                } else {
                                    player.sendMessage(prefix.append(Component.text("Incorrect Usage! Max radius is 200 blocks.").color(NamedTextColor.RED)));
                                }
                            } else {
                                player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim nearby <radius>").color(NamedTextColor.RED)));
                            }
                        } else {
                            player.sendMessage(prefix.append(Component.text("Incorrect Usage! /claim nearby <radius>").color(NamedTextColor.RED)));
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
