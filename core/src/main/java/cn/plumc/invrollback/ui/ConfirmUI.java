package cn.plumc.invrollback.ui;

import cn.plumc.invrollback.Config;
import cn.plumc.invrollback.PInvRollback;
import cn.plumc.invrollback.profile.RollbackProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ConfirmUI extends ChestUI {
    private static final HashMap<UUID, ConfirmUI> opened = new HashMap<>();

    private static final int VIEW = 22;
    private static final int ACCEPT = 20;
    private static final int REJECT = 24;
    private static final int BACK = 40;

    private RollbackProfile profile = RollbackProfile.getLoading();
    private boolean loaded = false;

    public ConfirmUI(ChestUI parent, Player player, long id) {
        super(parent, player, 54, Config.i18n("ui.confirm.title"));
        init();
        Bukkit.getScheduler().runTaskAsynchronously(PInvRollback.instance, ()->{
            profile = PInvRollback.rollbackManager.read(id);
            loaded = true;
            update();
        });
    }

    @Override
    public void update() {
        ItemStack viewItem = new ItemStack(Material.CHEST);
        ItemMeta meta = viewItem.getItemMeta();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm:ss");
        meta.setDisplayName(Config.i18n("ui.rollback.view.type").formatted(profile.type));
        meta.setLore(List.of(
                Config.i18n("ui.rollback.view.id").formatted(profile.id),
                Config.i18n("ui.rollback.view.date").formatted(format.format(new Date(profile.time))),
                Config.i18n("ui.rollback.view.message").formatted("".equals(profile.message) ? Config.i18n("view.message.null") : profile.message),
                "",
                Config.i18n("ui.confirm.view.tip")
                )
        );
        viewItem.setItemMeta(meta);
        inventory.setItem(VIEW, viewItem);

        ItemStack acceptItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta acceptMeta = acceptItem.getItemMeta();
        acceptMeta.setDisplayName(Config.i18n("ui.confirm.accept"));
        acceptMeta.setLore(List.of(Config.i18n("ui.confirm.warning")));
        acceptItem.setItemMeta(acceptMeta);
        inventory.setItem(ACCEPT, acceptItem);

        ItemStack rejectItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta rejectMeta = rejectItem.getItemMeta();
        rejectMeta.setDisplayName(Config.i18n("ui.confirm.reject"));
        rejectItem.setItemMeta(rejectMeta);
        inventory.setItem(REJECT, rejectItem);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(Config.i18n("ui.confirm.back"));
        backItem.setItemMeta(backMeta);
        inventory.setItem(BACK, backItem);
    }

    @Override
    public void onClick(ClickType clickType, InventoryAction action, int slot) {
        if (slot == BACK || slot == REJECT) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.3F, 1F);
            Bukkit.getScheduler().runTask(PInvRollback.instance, ()-> {onClose();parent.open();});
        }
        if (!loaded) return;
        if (slot == VIEW) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(profile.player);
            if (offlinePlayer.isOnline()) {
                ViewUI viewUI = new ViewUI(this, player, profile.id);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.3F, 1F);
                Bukkit.getScheduler().runTask(PInvRollback.instance, ()->{onClose();viewUI.open();});
            } else {
                player.sendMessage(Config.i18n("command.player.offline"));
            }
            return;
        }
        if (slot == ACCEPT) {
            PInvRollback.rollbackManager.rollback(player, profile, "");
            player.sendMessage(Config.i18n("command.rollback.success"));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3F, 1F);
            Bukkit.getScheduler().runTask(PInvRollback.instance, ()->player.closeInventory());
            return;
        }
    }

    @Override
    public void open() {
        opened.put(player.getUniqueId(), this);
        super.open();
    }

    @Override
    public void onClose() {
        super.onClose();
        opened.remove(player.getUniqueId());
    }

    public static ConfirmUI get(Player player, Inventory inventory){
        if (isPlayerOpen(player.getUniqueId(), inventory)) {
            return opened.get(player.getUniqueId());
        }
        return null;
    }
}
