package cn.plumc.invrollback.profile;

import cn.plumc.invrollback.nms.NMSUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InventoryProfile {
    private final JsonObject helmet;
    private final JsonObject chestplate;
    private final JsonObject leggings;
    private final JsonObject boots;

    private final JsonObject offHand;
    private final int handSlot;

    private final JsonObject mainInventory;

    private InventoryProfile(JsonObject helmet, JsonObject chestplate, JsonObject leggings, JsonObject boots, JsonObject offHand, int handSlot, JsonObject mainInventory) {
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
        this.offHand = offHand;
        this.handSlot = handSlot;
        this.mainInventory = mainInventory;
    }

    public static InventoryProfile parse(PlayerInventory inventory){
        JsonObject helmet = getItemAsJson(inventory.getHelmet());
        JsonObject chestplate = getItemAsJson(inventory.getChestplate());
        JsonObject leggings = getItemAsJson(inventory.getLeggings());
        JsonObject boots = getItemAsJson(inventory.getBoots());

        JsonObject offHand = getItemAsJson(inventory.getItemInOffHand());
        JsonObject mainInventory = new JsonObject();
        for (int i = 0; i < 36; i++) {
            JsonObject item = getItemAsJson(inventory.getItem(i));
            if (item!=null) mainInventory.add(String.valueOf(i), item);
        }
        return new InventoryProfile(helmet, chestplate, leggings, boots, offHand, inventory.getHeldItemSlot(), mainInventory);
    }

    public static JsonObject getItemAsJson(ItemStack item){
        if (item == null || item.getType()== Material.AIR) return null;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("item", item.getType().getKey().toString());
        jsonObject.addProperty("nbt", new String(Base64.getEncoder().encode(NMSUtils.getNMSHandler().serializeItem(item)), StandardCharsets.UTF_8));
        return jsonObject;
    }

    public static ItemStack getItemFromJson(JsonObject jsonObject){
        if (jsonObject == null) return null;
        return NMSUtils.getNMSHandler().deserializeItem(Base64.getDecoder().decode(jsonObject.get("nbt").getAsString()));
    }

    public static InventoryProfile read(JsonObject inventory){
        return new InventoryProfile(inventory.getAsJsonObject("helmet"),
                inventory.getAsJsonObject("chestplate"),
                inventory.getAsJsonObject("leggings"),
                inventory.getAsJsonObject("boots"),
                inventory.getAsJsonObject("offHand"),
                inventory.get("handSlot") == null ? 0 : inventory.get("handSlot").getAsInt(),
                inventory.get("mainInventory") == null ? new JsonObject() : inventory.get("mainInventory").getAsJsonObject());
    }

    public JsonObject serialize(){
        JsonObject jsonObject = new JsonObject();
        if (helmet!=null) jsonObject.add("helmet", helmet);
        if (chestplate!=null) jsonObject.add("chestplate", chestplate);
        if (leggings!=null) jsonObject.add("leggings", leggings);
        if (boots!=null) jsonObject.add("boots", boots);
        if (offHand!=null) jsonObject.add("offHand", offHand);
        jsonObject.addProperty("handSlot", handSlot);
        jsonObject.add("mainInventory", mainInventory);
        return jsonObject;
    }

    public void rollback(PlayerInventory inventory){
        inventory.clear();
        if (helmet!=null) inventory.setHelmet(getItemFromJson(helmet));
        if (chestplate!=null) inventory.setChestplate(getItemFromJson(chestplate));
        if (leggings!=null) inventory.setLeggings(getItemFromJson(leggings));
        if (boots!=null) inventory.setBoots(getItemFromJson(boots));
        if (offHand!=null) inventory.setItemInOffHand(getItemFromJson(offHand));
        inventory.setHeldItemSlot(handSlot);
        for (Map.Entry<String, JsonElement> itemJson : mainInventory.entrySet()) {
            int slot = Integer.parseInt(itemJson.getKey());
            inventory.setItem(slot, getItemFromJson(itemJson.getValue().getAsJsonObject()));
        }
    }

    public HashMap<String, ItemStack> getInventoryData() {
        HashMap<String, ItemStack> inventory = new HashMap<>();
        if (helmet != null) inventory.put("helmet", getItemFromJson(helmet));
        if (chestplate != null) inventory.put("chestplate", getItemFromJson(chestplate));
        if (leggings != null) inventory.put("leggings", getItemFromJson(leggings));
        if (boots != null) inventory.put("boots", getItemFromJson(boots));
        if (offHand != null) inventory.put("offHand", getItemFromJson(offHand));
        for (Map.Entry<String, JsonElement> itemJson : mainInventory.entrySet()) {
            inventory.put(itemJson.getKey(), getItemFromJson(itemJson.getValue().getAsJsonObject()));
        }
        return inventory;
    }

    public int getHandSlot(){
        return handSlot;
    }
}
