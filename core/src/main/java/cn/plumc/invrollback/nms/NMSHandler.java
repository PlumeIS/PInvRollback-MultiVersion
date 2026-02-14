package cn.plumc.invrollback.nms;

import org.bukkit.inventory.ItemStack;

public interface NMSHandler {
    byte[] serializeItem(ItemStack item);

    ItemStack deserializeItem(byte[] data);
}
