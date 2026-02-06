package cn.plumc.invrollback.listeners;

import cn.plumc.invrollback.ui.ChestUI;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!(human instanceof Player player)) return;
        if (ChestUI.isPlayerOpen(human.getUniqueId(), event.getClickedInventory())) {
            event.setCancelled(true);
            ChestUI ui = ChestUI.getUI(player, event.getClickedInventory());
            if (ui == null) return;
            ui.onClick(event.getClick() ,event.getAction() ,event.getSlot());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity human = event.getPlayer();
        if (!(human instanceof Player player)) return;
        if (ChestUI.isPlayerOpen(event.getPlayer().getUniqueId(), event.getInventory())) {
            ChestUI.getUI(player, event.getInventory());
        }
    }
}
