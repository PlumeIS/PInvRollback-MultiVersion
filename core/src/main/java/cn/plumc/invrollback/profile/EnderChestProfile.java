package cn.plumc.invrollback.profile;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

import static cn.plumc.invrollback.profile.InventoryProfile.getItemAsJson;
import static cn.plumc.invrollback.profile.InventoryProfile.getItemFromJson;

public class EnderChestProfile {
    private final JsonObject enderChest;

    public EnderChestProfile(JsonObject enderChest) {
        this.enderChest = enderChest;
    }

    public static EnderChestProfile parse(Inventory inventory){
        JsonObject enderChest = new JsonObject();
        for (int i = 0; i < 27; i++) {
            JsonObject item = getItemAsJson(inventory.getItem(i));
            if (item!=null) enderChest.add(String.valueOf(i), item);
        }
        return new EnderChestProfile(enderChest);
    }

    public static EnderChestProfile read(JsonObject inventory){
        return new EnderChestProfile(inventory.getAsJsonObject("enderChest") == null ? new JsonObject() : inventory.getAsJsonObject("enderChest"));
    }

    public JsonObject serialize(){
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("enderChest", enderChest);
        return jsonObject;
    }

    public void rollback(Inventory inventory){
        inventory.clear();
        for (Map.Entry<String, JsonElement> itemJson : enderChest.entrySet()) {
            int slot = Integer.parseInt(itemJson.getKey());
            inventory.setItem(slot, getItemFromJson(itemJson.getValue().getAsJsonObject()));
        }
    }

    public HashMap<String, ItemStack> getEnderChestData() {
        HashMap<String, ItemStack> enderChest = new HashMap<>();
        for (Map.Entry<String, JsonElement> itemJson : this.enderChest.entrySet()) {
            enderChest.put(itemJson.getKey(), getItemFromJson(itemJson.getValue().getAsJsonObject()));
        }
        return enderChest;
    }
}
