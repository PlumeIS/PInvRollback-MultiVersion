package cn.plumc.invrollback.profile;

import cn.plumc.invrollback.PInvRollback;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class RollbackProfile {
    public final UUID player;
    public final double[] pos;
    public final String world;
    public final String type;
    public final String message;
    public final long time;
    public final long id;
    public final InventoryProfile inventory;
    public final EnderChestProfile enderChest;

    public RollbackProfile(Player player, String type, String message, InventoryProfile inventory, EnderChestProfile enderChest) {
        this.player = player.getUniqueId();
        Location loc = player.getLocation();
        this.pos = new double[]{loc.getX(), loc.getY(), loc.getZ()};
        this.world = loc.getWorld().getName();
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
        JsonArray posArray = rollbackProfile.getAsJsonArray("pos");
        if (Objects.nonNull(posArray)) {
            this.pos = new double[]{posArray.get(0).getAsDouble(), posArray.get(1).getAsDouble(), posArray.get(2).getAsDouble()};
        } else {
            this.pos = new double[]{0.0, 0.0, 0.0};
        }
        this.world = rollbackProfile.get("world").getAsString();
        this.message = rollbackProfile.get("message").getAsString();
        this.time = rollbackProfile.get("time").getAsLong();
        this.id = rollbackProfile.get("id").getAsLong();
        this.inventory = InventoryProfile.read(rollbackProfile.get("inventory").getAsJsonObject());
        this.enderChest = EnderChestProfile.read(rollbackProfile.get("enderChest").getAsJsonObject());
    }

    public static RollbackProfile getLoading() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("player", "c1d3dcd0-5125-4910-9ac0-0b738ad39d5c");
        JsonArray pos = new JsonArray();
        pos.add(0.0);
        pos.add(0.0);
        pos.add(0.0);
        jsonObject.add("pos", pos);
        jsonObject.addProperty("world", "loading");
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
        jsonObject.add("pos", new Gson().toJsonTree(this.pos));
        jsonObject.addProperty("world", world);
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
        return "RollbackProfile[player=%s pos=[%.2f, %.2f, %.2f] world=%s id=%s type=%s message=%s time=%s]".formatted(
                player.toString(),
                pos[0],
                pos[1],
                pos[2],
                world,
                id,
                type,
                message,
                time);
    }
}
