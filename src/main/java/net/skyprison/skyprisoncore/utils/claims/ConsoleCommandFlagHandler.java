package net.skyprison.skyprisoncore.utils.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;

import java.util.Objects;

public class ConsoleCommandFlagHandler extends FlagValueChangeHandler<String> {
    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<ConsoleCommandFlagHandler> {
        @Override
        public ConsoleCommandFlagHandler create(Session session) {
            return new ConsoleCommandFlagHandler(session);
        }
    }
    public ConsoleCommandFlagHandler(Session session) {
        super(session, ClaimUtils.CONSOLECMD);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, String value) {
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String currentValue, String lastValue, MoveType moveType) {
        boolean isCitizensNPC = BukkitAdapter.adapt(player).hasMetadata("NPC");
        if(!Objects.equals(lastValue, currentValue) && !isCitizensNPC) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), currentValue.replaceAll("<player>", player.getName()));
        }
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, String lastValue, MoveType moveType) {
        return true;
    }
}

