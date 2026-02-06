package cn.plumc.invrollback.nms.v1_21_1;

import cn.plumc.invrollback.nms.NMSHandler;
import com.google.common.base.Preconditions;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class NMSHandlerImpl implements NMSHandler {
    public byte[] serializeItem(ItemStack item) {
        Preconditions.checkNotNull(item, "null cannot be serialized");
        Preconditions.checkArgument(item.getType() != Material.AIR, "air cannot be serialized");

        return serializeNbtToBytes((net.minecraft.nbt.CompoundTag) CraftItemStack.asNMSCopy(item).save(MinecraftServer.getServer().registryAccess()));
    }

    public ItemStack deserializeItem(byte[] data) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");

        net.minecraft.nbt.CompoundTag compound = deserializeNbtFromBytes(data);
        final int dataVersion = compound.getInt("DataVersion");
        compound = (net.minecraft.nbt.CompoundTag) MinecraftServer.getServer().fixerUpper.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, compound), dataVersion, Bukkit.getUnsafe().getDataVersion()).getValue();
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.parse(MinecraftServer.getServer().registryAccess(), compound).orElseThrow());
    }

    private static byte[] serializeNbtToBytes(CompoundTag compound) {
        compound.putInt("DataVersion", Bukkit.getUnsafe().getDataVersion());
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try {
            net.minecraft.nbt.NbtIo.writeCompressed(
                    compound,
                    outputStream
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return outputStream.toByteArray();
    }

    private static net.minecraft.nbt.CompoundTag deserializeNbtFromBytes(byte[] data) {
        net.minecraft.nbt.CompoundTag compound;
        try {
            compound = net.minecraft.nbt.NbtIo.readCompressed(
                    new java.io.ByteArrayInputStream(data), net.minecraft.nbt.NbtAccounter.unlimitedHeap()
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        int dataVersion = compound.getInt("DataVersion");
        return compound;
    }
}
