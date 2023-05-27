package net.skyprison.skyprisoncore.utils.claims;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.FlagValueChangeHandler;
import com.sk89q.worldguard.session.handler.Handler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.skyprison.skyprisoncore.SkyPrisonCore;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class FlyFlagHandler extends FlagValueChangeHandler<State> {
    public static final Factory FACTORY = new Factory();
    public static class Factory extends Handler.Factory<FlyFlagHandler> {
        @Override
        public FlyFlagHandler create(Session session) {
            return new FlyFlagHandler(session);
        }
    }
    public FlyFlagHandler(Session session) {
        super(session, SkyPrisonCore.FLY);
    }

    @Override
    protected void onInitialValue(LocalPlayer player, ApplicableRegionSet set, State value) {
        Player p = BukkitAdapter.adapt(player);
        if(!p.getGameMode().equals(GameMode.CREATIVE) && !p.getGameMode().equals(GameMode.SPECTATOR)) {
            if (value == State.ALLOW) {
                p.setAllowFlight(true);
                p.sendMessage(Component.text("You can fly now!").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            } else if (value == State.DENY && p.getAllowFlight()) {
                p.setAllowFlight(false);
                p.sendMessage(Component.text("You can no longer fly!").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            }
        }
    }

    @Override
    protected boolean onSetValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {
        Player p = BukkitAdapter.adapt(player);
        if(!p.getGameMode().equals(GameMode.CREATIVE) && !p.getGameMode().equals(GameMode.SPECTATOR)) {
            if (currentValue == State.ALLOW && (lastValue == State.DENY || lastValue == null)) {
                p.setAllowFlight(true);
                p.sendMessage(Component.text("You can fly now!").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            } else if (currentValue == State.DENY && lastValue == State.ALLOW) {
                p.setAllowFlight(false);
                p.sendMessage(Component.text("You can no longer fly!").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            }
        }
        return true;
    }

    @Override
    protected boolean onAbsentValue(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
        Player p = BukkitAdapter.adapt(player);
        if(!p.getGameMode().equals(GameMode.CREATIVE) && !p.getGameMode().equals(GameMode.SPECTATOR)) {
            if (lastValue == State.ALLOW) {
                p.setAllowFlight(false);
                p.sendMessage(Component.text("You can no longer fly!").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            }
        }
        return true;
    }
}