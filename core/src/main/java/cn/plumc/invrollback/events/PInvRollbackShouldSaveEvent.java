package cn.plumc.invrollback.events;

import cn.plumc.invrollback.profile.EnderChestProfile;
import cn.plumc.invrollback.profile.InventoryProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Optional;

public class PInvRollbackShouldSaveEvent extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final String type;
    private final String message;
    private final int maxProfiles;
    private boolean cached;
    private InventoryProfile inventoryProfile;
    private EnderChestProfile enderChestProfile;

    private boolean cancelled;

    public PInvRollbackShouldSaveEvent(Player player, String type, String message, int maxProfiles) {
        this.player = player;
        this.type = type;
        this.message = message;
        this.maxProfiles = maxProfiles;
        this.cached = false;
        this.inventoryProfile = null;
        this.enderChestProfile = null;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public void cacheProfile() {
        this.cached = true;
        this.inventoryProfile = InventoryProfile.parse(player.getInventory());
        this.enderChestProfile = EnderChestProfile.parse(player.getEnderChest());
    }

    public boolean isCached() {
        return cached;
    }

    public Optional<EnderChestProfile> getEnderChestProfile() {
        return Optional.of(enderChestProfile);
    }

    public Optional<InventoryProfile> getInventoryProfile() {
        return Optional.of(inventoryProfile);
    }

    public Player getPlayer() {
        return player;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getMaxProfiles() {
        return maxProfiles;
    }

    @Override
    public HandlerList getHandlers() {
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
