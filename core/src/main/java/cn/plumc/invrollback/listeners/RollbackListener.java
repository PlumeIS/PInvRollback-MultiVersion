package cn.plumc.invrollback.listeners;

import cn.plumc.invrollback.PInvRollback;
import cn.plumc.invrollback.events.PInvRollbackShouldSaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RollbackListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onRollbackSaving(PInvRollbackShouldSaveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(PInvRollback.instance, () -> {
            PInvRollback.rollbackManager.create(event);
        });
    }
}
