package cn.plumc.invrollback.events;

import cn.plumc.invrollback.profile.RollbackProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PInvRollbackStartEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled;

    private final Player player;
    private final String type;
    private final String reason;
    private final long time;
    private final long id;

    public PInvRollbackStartEvent(RollbackProfile profile) {
        this.player = Bukkit.getPlayer(profile.player);
        this.type = profile.type;
        this.reason = profile.message;
        this.time = profile.time;
        this.id = profile.id;
    }

    public Player getPlayer() {
        return player;
    }

    public String getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public long getTime() {
        return time;
    }

    public long getId() {
        return id;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
