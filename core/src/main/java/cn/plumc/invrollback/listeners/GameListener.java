package cn.plumc.invrollback.listeners;

import cn.plumc.invrollback.Config;
import cn.plumc.invrollback.RollbackManager;
import cn.plumc.invrollback.events.PInvRollbackShouldSaveEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {
    @EventHandler
    public static void onPlayerJoin(PlayerJoinEvent event) {
        PInvRollbackShouldSaveEvent saveEvent = new PInvRollbackShouldSaveEvent(event.getPlayer(), RollbackManager.DefaultType.JOIN, "", Config.maxCount("join"));
        saveEvent.cacheProfile();
        Bukkit.getPluginManager().callEvent(saveEvent);
    }

    @EventHandler
    public static void onPlayerQuit(PlayerQuitEvent event) {
        PInvRollbackShouldSaveEvent saveEvent = new PInvRollbackShouldSaveEvent(event.getPlayer(), RollbackManager.DefaultType.QUIT, "", Config.maxCount("quit"));
        saveEvent.cacheProfile();
        Bukkit.getPluginManager().callEvent(saveEvent);
    }

    @EventHandler
    public static void onPlayerDeath(PlayerDeathEvent event) {
        PInvRollbackShouldSaveEvent saveEvent = new PInvRollbackShouldSaveEvent(event.getEntity(), RollbackManager.DefaultType.DEATH, "", Config.maxCount("death"));
        saveEvent.cacheProfile();
        Bukkit.getPluginManager().callEvent(saveEvent);
    }

    @EventHandler
    public static void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        PInvRollbackShouldSaveEvent saveEvent = new PInvRollbackShouldSaveEvent(event.getPlayer(), RollbackManager.DefaultType.WORLD_CHANGE, "", Config.maxCount("worldChange"));
        saveEvent.cacheProfile();
        Bukkit.getPluginManager().callEvent(saveEvent);
    }
}
