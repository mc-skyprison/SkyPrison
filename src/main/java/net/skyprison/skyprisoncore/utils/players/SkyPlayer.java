package net.skyprison.skyprisoncore.utils.players;

import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Modules.Ranks.CMIRank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.skyprison.skyprisoncore.utils.DailyMissions;
import net.skyprison.skyprisoncore.utils.NotificationsUtils;
import net.skyprison.skyprisoncore.utils.Tags;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkyPlayer {
    private Player player;
    private String name;
    private Component displayName;
    private final UUID uniqueId;
    private Long discordId;

    private String inventory;
    private String enderchest;

    private String logoutWorld;

    private final CMIUser cmiUser;

    private Tags.Tag tag;

    private List<PlayerManager.Ignore> ignores;

    private List<DailyMissions.PlayerMission> missions;

    public SkyPlayer(@NotNull String name, @NotNull UUID uniqueId, @Nullable Tags.Tag tag,
                     @NotNull List<DailyMissions.PlayerMission> missions, @NotNull List<PlayerManager.Ignore> ignores,
                     @Nullable Long discordId, @NotNull CMIUser cmiUser, @Nullable String logoutWorld, @Nullable Component displayName,
                     @Nullable String inventory, @Nullable String enderchest) {
        this.name = name;
        this.uniqueId = uniqueId;
        this.tag = tag;
        this.missions = missions;
        this.ignores = ignores;
        this.discordId = discordId;
        this.cmiUser = cmiUser;
        this.logoutWorld = logoutWorld;
        this.displayName = displayName;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    @NotNull
    public List<DailyMissions.PlayerMission> getMissions() {
        return missions;
    }

    @NotNull
    public List<DailyMissions.PlayerMission> getUncompletedMissions() {
        return missions.stream().filter(mission -> !mission.completed()).toList();
    }

    @NotNull
    public List<DailyMissions.PlayerMission> getCompletedMissions() {
        return missions.stream().filter(DailyMissions.PlayerMission::completed).toList();
    }

    public void setMissions(@NotNull List<DailyMissions.PlayerMission> missions) {
        this.missions = missions;
    }

    @Nullable
    public Tags.Tag getTag() {
        return tag;
    }

    public void setTag(@Nullable Tags.Tag tag) {
        this.tag = tag;
    }

    @NotNull
    public List<PlayerManager.Ignore> getIgnores() {
        return ignores;
    }

    public void setIgnores(@NotNull List<PlayerManager.Ignore> ignores) {
        this.ignores = ignores;
    }

    public void addIgnore(@NotNull PlayerManager.Ignore ignore) {
        ignores.add(ignore);
    }

    public void removeIgnore(@NotNull PlayerManager.Ignore ignore) {
        ignores.remove(ignore);
    }

    public boolean isIgnoring(@NotNull UUID uniqueId) {
        return ignores.stream().anyMatch(ignore -> ignore.targetId().equals(uniqueId));
    }

    public boolean isIgnoring(@NotNull Player player) {
        return isIgnoring(player.getUniqueId());
    }

    public boolean isIgnoringMsgs(@NotNull UUID uniqueId) {
        return ignores.stream().anyMatch(ignore -> ignore.targetId().equals(uniqueId) && ignore.ignorePrivate());
    }

    public boolean isIgnoringMsgs(@NotNull Player player) {
        return isIgnoringMsgs(player.getUniqueId());
    }

    public boolean isIgnoringTps(@NotNull UUID uniqueId) {
        return ignores.stream().anyMatch(ignore -> ignore.targetId().equals(uniqueId) && ignore.ignorePrivate());
    }

    public boolean isIgnoringTps(@NotNull Player player) {
        return isIgnoringTps(player.getUniqueId());
    }

    @Nullable
    public Long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(@Nullable Long discordId) {
        this.discordId = discordId;
    }

    public void sendMessage(@NotNull Component msg, @NotNull String notifType) {
        sendMessage(msg, notifType, null, null, true);
    }

    public void sendMessage(@NotNull Component msg, @NotNull String notifType, @NotNull String notifData) {
        sendMessage(msg, notifType, notifData, null, true);
    }

    public void sendMessage(@NotNull Component msg, @NotNull String notifType, boolean deleteOnView) {
        sendMessage(msg, notifType, null, null, deleteOnView);
    }

    public void sendMessage(@NotNull Component msg, @NotNull String notifType, @NotNull String notifData, boolean deleteOnView) {
        sendMessage(msg, notifType, notifData, null, deleteOnView);
    }

    public void sendMessage(@NotNull Component msg, @NotNull String notifType, @Nullable String notifData,
                            @Nullable String notifId, boolean deleteOnView) {
        if (player != null && player.isOnline()) {
            player.sendMessage(msg);
        } else {
            NotificationsUtils.createNotification(notifType, notifData, uniqueId, msg, notifId, deleteOnView);
        }
    }

    public CompletableFuture<Boolean> hasPermission(@NotNull String permission) {
        if (player != null) return CompletableFuture.completedFuture(player.hasPermission(permission));

        LuckPerms luckAPI = LuckPermsProvider.get();
        UserManager userManager = luckAPI.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uniqueId);
        return userFuture.thenApplyAsync(user -> {
            CachedPermissionData permissionData = user.getCachedData().getPermissionData();
            return permissionData.checkPermission(permission).asBoolean();
        });
    }

    public double getBalance() {
        return cmiUser.getBalance();
    }

    public long getPlaytime() {
        return cmiUser.getTotalPlayTime();
    }

    @NotNull
    public String getLastIp() {
        return cmiUser.getLastIp();
    }

    @NotNull
    public CMIRank getPrisonRank() {
        return cmiUser.getRank();
    }

    public boolean isInPrison() {
        String world = player.getWorld().getName();
        return world.equals("world_prison") || world.equals("world_free") || world.equals("world_free_nether")
                || world.equals("world_free_end") || world.equals("world_skycity") || world.equals("world_prison_tutorial");
    }

    @Nullable
    public String getLogoutWorld() {
        return logoutWorld;
    }

    public void setLogoutWorld(@NotNull String logoutWorld) {
        this.logoutWorld = logoutWorld;
    }

    @Nullable
    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable Component displayName) {
        this.displayName = displayName;

        if(player != null) {
            player.customName(displayName);
        } else {
            if(displayName != null) {
                NotificationsUtils.scheduleForOnline(uniqueId, "namecolour", GsonComponentSerializer.gson().serialize(displayName));
            } else {
                NotificationsUtils.scheduleForOnline(uniqueId, "namecolour", "remove");
            }
        }
    }

    @Nullable
    public String getInventory() {
        return inventory;
    }

    public void setInventory(@NotNull String inventory) {
        this.inventory = inventory;
    }

    public boolean updateInventory() {
        if (player == null) return false;
        setInventory(PlayerManager.toBase64(player.getInventory()));
        return true;
    }

    @Nullable
    public String getEnderchest() {
        return enderchest;
    }

    public void setEnderchest(@NotNull String enderchest) {
        this.enderchest = enderchest;
    }

    public boolean updateEnderchest() {
        if (player == null) return false;
        setEnderchest(PlayerManager.toBase64(player.getEnderChest()));
        return true;
    }
}
