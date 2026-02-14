package cn.plumc.invrollback.profile;

import cn.plumc.invrollback.PInvRollback;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RollbackProfile {
    public final UUID player;
    public final String type;
    public final String message;
    public final long time;
    public final long id;
    public final InventoryProfile inventory;
    public final EnderChestProfile enderChest;

    public RollbackProfile(Player player, String type, String message, InventoryProfile inventory, EnderChestProfile enderChest) {
        this.player = player.getUniqueId();
        this.type = type;
        this.message = message;
        this.time = System.currentTimeMillis();
        this.id = PInvRollback.rollbackManager.getNewId();
        this.inventory = inventory;
        this.enderChest = enderChest;
    }

    public RollbackProfile(JsonObject rollbackProfile) {
        this.player = UUID.fromString(rollbackProfile.get("player").getAsString());
        this.type = rollbackProfile.get("type").getAsString();
        this.message = rollbackProfile.get("message").getAsString();
        this.time = rollbackProfile.get("time").getAsLong();
        this.id = rollbackProfile.get("id").getAsLong();
        this.inventory = InventoryProfile.read(rollbackProfile.get("inventory").getAsJsonObject());
        this.enderChest = EnderChestProfile.read(rollbackProfile.get("enderChest").getAsJsonObject());
    }

    public static RollbackProfile getLoading() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("player", "c1d3dcd0-5125-4910-9ac0-0b738ad39d5c");
        jsonObject.addProperty("type", "loading");
        jsonObject.addProperty("message", "loading");
        jsonObject.addProperty("time", System.currentTimeMillis());
        jsonObject.addProperty("id", 0);
        jsonObject.add("inventory", new JsonObject());
        jsonObject.add("enderChest", new JsonObject());
        return new RollbackProfile(jsonObject);
    }

    public void rollback(Player serverPlayer) {
        inventory.rollback(serverPlayer.getInventory());
        enderChest.rollback(serverPlayer.getEnderChest());
    }

    public JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("player", player.toString());
        jsonObject.addProperty("type", type);
        jsonObject.addProperty("message", message);
        jsonObject.addProperty("time", time);
        jsonObject.addProperty("id", id);
        jsonObject.add("inventory", inventory.serialize());
        jsonObject.add("enderChest", enderChest.serialize());
        return jsonObject;
    }

    @Override
    public String toString() {
        return "RollbackProfile[player=%s id=%s type=%s message=%s time=%s]".formatted(player.toString(), id, type, message, time);
    }
}
