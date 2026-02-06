package cn.plumc.invrollback.nms.paper;

import cn.plumc.invrollback.nms.NMSHandler;
import org.bukkit.inventory.ItemStack;

public class NMSHandlerImpl implements NMSHandler {
    public byte[] serializeItem(ItemStack item) {
        return item.serializeAsBytes();
    }

    public ItemStack deserializeItem(byte[] data) {
        return ItemStack.deserializeBytes(data);
    }
}
