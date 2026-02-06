package cn.plumc.invrollback.ui;

import cn.plumc.invrollback.Config;
import cn.plumc.invrollback.PInvRollback;
import cn.plumc.invrollback.profile.RollbackProfile;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class ViewUI extends ChestUI{
    private enum Mode{
        INVENTORY,
        ENDER_CHEST
    }

    private static final HashMap<UUID, ViewUI> opened = new HashMap<>();

    private static final int PLAYER = 0;
    private static final int MODE = 1;
    private static final int BACK = 8;

    private static final int OFF_HAND = 2;
    private static final int HELMET = 3;
    private static final int CHESTPLATE = 4;
    private static final int LEGGINGS = 5;
    private static final int BOOTS = 6;

    private RollbackProfile profile = RollbackProfile.getLoading();
    private boolean loaded = false;
    private Mode mode;

    private int handSlot;
    private HashMap<String, ItemStack> inventoryData;
    private HashMap<String, ItemStack> enderChestData;

    public ViewUI(ChestUI parent, Player player, long id) {
        super(parent, player, 54, Config.i18n("ui.view.title"));
        opened.put(player.getUniqueId(), this);
        mode = Mode.INVENTORY;
        init();
        Bukkit.getScheduler().runTaskAsynchronously(PInvRollback.instance, ()->{
            profile = PInvRollback.rollbackManager.read(id);
            loaded = true;
            init();
        });
    }

    @Override
    public void init() {
        handSlot = profile.inventory.getHandSlot();
        inventoryData = profile.inventory.getInventoryData();
        enderChestData = profile.enderChest.getEnderChestData();

        if (player.hasPermission("commands.pinvrollback.ui.fetch")){
            for (Map.Entry<String, ItemStack> itemStackEntry : ImmutableMap.copyOf(inventoryData).entrySet()) {
                getFetchableItem(itemStackEntry, inventoryData);
            }
            for (Map.Entry<String, ItemStack> itemStackEntry : enderChestData.entrySet()) {
                getFetchableItem(itemStackEntry, enderChestData);
            }
        }

        super.init();
    }

    private void getFetchableItem(Map.Entry<String, ItemStack> itemStackEntry, HashMap<String, ItemStack> enderChestData) {
        ItemStack itemStack = itemStackEntry.getValue();
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = itemMeta.getLore() == null ? new ArrayList<>() : itemMeta.getLore();
        lore.add("");
        lore.add(Config.i18n("ui.view.fetch"));
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        enderChestData.put(itemStackEntry.getKey(), itemStack);
    }

    @Override
    public void update() {
        inventory.clear();
        ItemStack frame = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setHideTooltip(true);
        frameMeta.setDisplayName("");
        frame.setItemMeta(frameMeta);
        inventory.setItem(7, frame);
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, frame);
        }
        if (mode == Mode.ENDER_CHEST) {
            for (int i = 45; i < 54; i++) {
                inventory.setItem(i, frame);
            }
        }

        SimpleDateFormat format = new SimpleDateFormat("MM/dd HH:mm:ss");

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta headMetal = (SkullMeta) playerHead.getItemMeta();
        headMetal.setOwningPlayer(Bukkit.getOfflinePlayer(profile.player));
        headMetal.setDisplayName(Config.i18n("ui.rollback.player").formatted(Bukkit.getOfflinePlayer(profile.player).getName()));
        headMetal.setLore(List.of(
                Config.i18n("ui.view.player.date").formatted(format.format(new Date(profile.time)))
        ));
        playerHead.setItemMeta(headMetal);
        inventory.setItem(PLAYER, playerHead);

        ItemStack modeItem;
        if (mode == Mode.INVENTORY){
            modeItem = new ItemStack(Material.ENDER_CHEST);
            ItemMeta modeMeta = modeItem.getItemMeta();
            modeMeta.setDisplayName(Config.i18n("ui.view.mode.inventory"));
            modeMeta.setLore(List.of(Config.i18n("ui.view.mode.ender_chest.switch")));
            modeItem.setItemMeta(modeMeta);
        } else {
            modeItem = new ItemStack(Material.CHEST);
            ItemMeta modeMeta = modeItem.getItemMeta();
            modeMeta.setDisplayName(Config.i18n("ui.view.mode.ender_chest"));
            modeMeta.setLore(List.of(Config.i18n("ui.view.mode.inventory.switch")));
            modeItem.setItemMeta(modeMeta);
        }
        inventory.setItem(MODE, modeItem);

        if (mode == Mode.INVENTORY){
            ItemStack holdFrame = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta holdFrameMeta = holdFrame.getItemMeta();
            holdFrameMeta.setHideTooltip(true);
            holdFrameMeta.setDisplayName("");
            holdFrame.setItemMeta(holdFrameMeta);
            inventory.setItem(handSlot+9, holdFrame);
        }

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(Config.i18n("ui.confirm.back"));
        backItem.setItemMeta(backMeta);
        inventory.setItem(BACK, backItem);

        ItemStack nullItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta nullItemMeta = nullItem.getItemMeta();
        nullItemMeta.setDisplayName(Config.i18n("ui.view.item.null"));
        nullItem.setItemMeta(nullItemMeta);

        inventory.setItem(OFF_HAND, inventoryData.getOrDefault("off_hand", nullItem));
        inventory.setItem(HELMET, inventoryData.getOrDefault("helmet", nullItem));
        inventory.setItem(CHESTPLATE, inventoryData.getOrDefault("chestplate", nullItem));
        inventory.setItem(LEGGINGS, inventoryData.getOrDefault("leggings", nullItem));
        inventory.setItem(BOOTS, inventoryData.getOrDefault("boots", nullItem));

        if (mode == Mode.INVENTORY){
            for (Map.Entry<String, ItemStack> itemStackEntry : inventoryData.entrySet()) {
                if (NumberUtils.isDigits(itemStackEntry.getKey())) {
                    Integer inventoryIndex = NumberUtils.createInteger(itemStackEntry.getKey());
                    inventory.setItem(inventoryIndex+18, itemStackEntry.getValue());
                }
            }
        } else {
            for (Map.Entry<String, ItemStack> itemStackEntry : enderChestData.entrySet()) {
                if (NumberUtils.isDigits(itemStackEntry.getKey())) {
                    Integer inventoryIndex = NumberUtils.createInteger(itemStackEntry.getKey());
                    inventory.setItem(inventoryIndex+18, itemStackEntry.getValue());
                }
            }
        }
    }

    @Override
    public void onClick(ClickType clickType, InventoryAction action, int slot) {
        if (slot == BACK) {
            player.playSound(player, Sound.BLOCK_CHEST_CLOSE, 0.3F, 1F);
            Bukkit.getScheduler().runTask(PInvRollback.instance, parent::open);
            return;
        }
        if (!loaded) return;

        if (slot==MODE){
            if (mode == Mode.INVENTORY) mode = Mode.ENDER_CHEST;
            else mode = Mode.INVENTORY;
            update();
            return;
        }

        if (!player.hasPermission("commands.pinvrollback.ui.fetch")) return;
        boolean update = false;
        if (slot==OFF_HAND){
            if (inventoryData.containsKey("off_hand")) {
                player.getInventory().addItem(inventoryData.get("off_hand"));
                update = true;
            }
        }
        if (slot==HELMET){
            if (inventoryData.containsKey("helmet")) {
                player.getInventory().addItem(inventoryData.get("helmet"));
                update = true;
            }
        }
        if (slot==CHESTPLATE){
            if (inventoryData.containsKey("chestplate")) {
                player.getInventory().addItem(inventoryData.get("chestplate"));
                update = true;
            }

        }
        if (slot==LEGGINGS){
            if (inventoryData.containsKey("leggings")) {
                player.getInventory().addItem(inventoryData.get("leggings"));
                update = true;
            }
        }
        if (slot==BOOTS){
            if (inventoryData.containsKey("boots")) {
                player.getInventory().addItem(inventoryData.get("boots"));
                update = true;
            }
        }
        if (mode==Mode.INVENTORY&&18<=slot&&slot<=53){
            if (inventoryData.containsKey(String.valueOf(slot-18))){
                player.getInventory().addItem(inventoryData.get(String.valueOf(slot-18)));
                update = true;
            }
        } else if (mode==Mode.ENDER_CHEST&&18<=slot&&slot<=44) {
            if (enderChestData.containsKey(String.valueOf(slot-18))){
                player.getInventory().addItem(enderChestData.get(String.valueOf(slot-18)));
                update = true;
            }
        }
        if (update){
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3F, 1F);
            Bukkit.getScheduler().runTask(PInvRollback.instance, player::updateInventory);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        opened.remove(player.getUniqueId());
    }

    public static ViewUI get(Player player, Inventory inventory){
        if (isPlayerOpen(player.getUniqueId(), inventory)) {
            return opened.get(player.getUniqueId());
        }
        return null;
    }
}
