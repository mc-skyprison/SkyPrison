package net.skyprison.skyprisoncore.commands;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import net.skyprison.skyprisoncore.utils.DatabaseHook;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseImport implements CommandExecutor {
    private final SkyPrisonCore plugin;
    private final DatabaseHook db;

    public DatabaseImport(SkyPrisonCore plugin, DatabaseHook db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        World world = Bukkit.getWorld("world_free");
        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = regionContainer.get(BukkitAdapter.adapt(Objects.requireNonNull(world)));

        List<ProtectedRegion> claims = new ArrayList<>();

        assert regionManager != null;
        for(ProtectedRegion region : regionManager.getRegions().values()) {
            if(region.getId().startsWith("claim_")) {
                claims.add(region);
            }
        }

        HashMap<String, String> parents = new HashMap<>();

        for(ProtectedRegion claim : claims) {
            UUID ownerUUID = UUID.fromString(claim.getId().split("_")[1]);
            ProtectedRegion newClaim;
            if(claim instanceof ProtectedCuboidRegion) {
                newClaim = new ProtectedCuboidRegion("claim_" + UUID.randomUUID(), claim.getMinimumPoint(), claim.getMaximumPoint());
            } else {
                newClaim = new ProtectedPolygonalRegion("claim_" + UUID.randomUUID(), claim.getPoints(), claim.getMinimumPoint().getBlockY(), claim.getMaximumPoint().getBlockY());
            }
            newClaim.copyFrom(claim);


            if(newClaim.getParent() != null) {
                if(newClaim.getParent().getId().split("_").length > 2) {
                    parents.put(newClaim.getId(), newClaim.getParent().getId());
                }
            }

            String claimName = claim.getId().substring(43);

            CuboidRegion volume = new CuboidRegion(newClaim.getMinimumPoint().withY(0), newClaim.getMaximumPoint().withY(0));

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims (claim_id, claim_name, parent_id, world, blocks_used) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, newClaim.getId());
                ps.setString(2, claimName);
                ps.setString(3, null);
                ps.setString(4, world.getName());
                ps.setLong(5, volume.getVolume());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Set<UUID> admins = newClaim.getOwners().getUniqueIds();

            for(UUID admin : admins) {
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?)")) {
                    ps.setString(1, admin.toString());
                    ps.setString(2, newClaim.getId());
                    ps.setString(3, !admin.equals(ownerUUID) ? "co-owner" : "owner");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            List<UUID> members = new ArrayList<>(newClaim.getMembers().getUniqueIds());

            members.removeAll(admins);

            for(UUID member : members) {
                try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO claims_members (user_id, claim_id, user_rank) VALUES (?, ?, ?)")) {
                    ps.setString(1, member.toString());
                    ps.setString(2, newClaim.getId());
                    ps.setString(3, "member");
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            for(UUID admin : admins) {
                newClaim.getMembers().addPlayer(admin);
                plugin.tellConsole("ADDED " + admin);
            }
            newClaim.getOwners().removeAll();
            regionManager.addRegion(newClaim);

            if(parents.containsValue(claim.getId())) {
                Set<String> childClaims = parents.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), claim.getId())).map(Map.Entry::getKey).collect(Collectors.toSet());

                for(String childClaim : childClaims) {
                    try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE claims SET parent_id = ? WHERE claim_id = ?")) {
                        ps.setString(1, newClaim.getId());
                        ps.setString(2, childClaim);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    ProtectedRegion child = regionManager.getRegion(childClaim);
                    assert child != null;
                    try {
                        child.setParent(newClaim);
                    } catch (ProtectedRegion.CircularInheritanceException e) {
                        throw new RuntimeException(e);
                    }
                    parents.remove(childClaim);
                }
            }
        }

        claims.forEach(claim -> regionManager.removeRegion(claim.getId(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN));


        File folder = new File(plugin.getDataFolder() + File.separator + "players");

        for(File pFile : Objects.requireNonNull(folder.listFiles())) {
            YamlConfiguration pConf = YamlConfiguration.loadConfiguration(pFile);
            String pUUID = pFile.getName().split("\\.")[0];
            long totalBlocks = pConf.getInt("player.totalClaimBlocks");
            long totalInUse = pConf.getInt("player.totalClaimBlocksInUse");
            plugin.tellConsole(pUUID + " " + totalBlocks + " " + totalInUse);

            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET claim_blocks = ?, claim_blocks_used = ? WHERE user_id = ?")) {
                ps.setLong(1, totalBlocks);
                ps.setLong(2, totalInUse);
                ps.setString(3, pUUID);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
