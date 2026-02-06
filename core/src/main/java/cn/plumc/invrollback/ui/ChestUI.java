package cn.plumc.invrollback.ui;

import cn.plumc.invrollback.PInvRollback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.UUID;

public abstract class ChestUI {
    public static HashMap<UUID, Inventory> players = new HashMap<>();

    public Player player;
    public Inventory inventory;
    public ChestUI parent;

    public ChestUI(ChestUI parent, Player player, int size, String title) {
        this.parent = parent;
        this.player = player;
        this.inventory = Bukkit.createInventory(player, size, title);
    }

    public ChestUI(Player player, int size, String title) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, size, title);
    }

    public void init(){
        update();
    }

    public void update(){}

    public void open(){
        player.openInventory(inventory);
        player.updateInventory();
        players.put(player.getUniqueId(), inventory);
    }

    public void onClick(ClickType clickType, InventoryAction action, int slot){}

    public void onClose(){
        players.remove(player.getUniqueId());
        Bukkit.getScheduler().runTask(PInvRollback.instance, ()->{if (hasParent() && parent != null) parent.open();});
    }

    public boolean hasParent(){
        return false;
    }

    public static boolean isPlayerOpen(UUID player, Inventory inventory){
        return players.containsKey(player) && players.get(player).equals(inventory);
    }

    public static ChestUI getUI(Player player, Inventory inventory){
        ChestUI ui;
        if ((ui = RollbackUI.get(player, inventory))!=null) return ui;
        if ((ui = ConfirmUI.get(player, inventory))!=null) return ui;
        if ((ui = ViewUI.get(player, inventory))!=null) return ui;
        return null;
    }
}
