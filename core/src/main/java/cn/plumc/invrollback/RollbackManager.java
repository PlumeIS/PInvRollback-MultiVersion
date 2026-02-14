package cn.plumc.invrollback;

import cn.plumc.invrollback.events.PInvRollbackShouldSaveEvent;
import cn.plumc.invrollback.profile.EnderChestProfile;
import cn.plumc.invrollback.profile.InventoryProfile;
import cn.plumc.invrollback.profile.RollbackProfile;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class RollbackManager {
    private final static String CONFIG_PATH = "profile.json";
    private final static String PROFILE_PATH = "profiles";
    private final HashMap<UUID, List<ProfileView>> views = new HashMap<>();
    private final HashMap<UUID, HashMap<String, Integer>> counters = new HashMap<>();
    private JsonObject config = null;
    private Path dataPath = null;
    private long lastId = 0;

    public long getNewId() {
        lastId++;
        return lastId;
    }

    public void load(Path dataPath) {
        this.dataPath = dataPath;
        Path configPath = dataPath.resolve(CONFIG_PATH);
        File configFile = configPath.toFile();
        PInvRollback.instance.saveResource("profile.json", false);
        try {
            config = JsonParser.parseReader(new FileReader(configFile)).getAsJsonObject();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        lastId = config.get("id").getAsLong();
        for (JsonElement jsonElement : config.getAsJsonArray("profiles")) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
            if (!views.containsKey(uuid)) {
                views.put(uuid, new ArrayList<>());
            }
            long id = jsonObject.get("id").getAsLong();
            long time = jsonObject.get("time").getAsLong();
            String player = jsonObject.get("player").getAsString();
            String type = jsonObject.get("type").getAsString();
            String message = jsonObject.get("message").getAsString();
            views.get(uuid).add(new ProfileView(id, uuid, player, type, message, time, new Date(time)));
        }
        for (Map.Entry<String, JsonElement> entry : config.getAsJsonObject("counters").entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            if (!counters.containsKey(uuid)) {
                counters.put(uuid, new HashMap<>());
            }
            HashMap<String, Integer> counter = counters.get(uuid);
            JsonObject jsonObject = entry.getValue().getAsJsonObject();
            for (Map.Entry<String, JsonElement> type : jsonObject.entrySet()) {
                counter.put(type.getKey(), type.getValue().getAsInt());
            }
        }
    }

    public void save(Path dataPath) {
        Path configPath = dataPath.resolve(CONFIG_PATH);
        File configFile = configPath.toFile();
        PInvRollback.instance.saveResource("profile.json", false);
        Gson gson = new Gson();
        JsonArray jsonArray = new JsonArray();
        for (List<ProfileView> profileViews : views.values()) {
            for (ProfileView profileView : profileViews) {
                jsonArray.add(profileView.serialize());
            }
        }
        config.addProperty("id", lastId);
        config.add("profiles", jsonArray);
        config.add("counters", gson.toJsonTree(counters));

        try {
            JsonWriter writer = new JsonWriter(new FileWriter(configFile));
            writer.setIndent("  ");
            gson.toJson(config, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public long create(PInvRollbackShouldSaveEvent event) {
        Player player = event.getPlayer();
        String type = event.getType();
        String message = event.getMessage();
        int maxProfile = event.getMaxProfiles();
        InventoryProfile inventoryProfile = event.getInventoryProfile().orElse(InventoryProfile.parse(player.getInventory()));
        EnderChestProfile enderChestProfile = event.getEnderChestProfile().orElse(EnderChestProfile.parse(player.getEnderChest()));
        return create(player, type, message, maxProfile, inventoryProfile, enderChestProfile);
    }

    public long create(Player player, String type, String message, int maxProfile, InventoryProfile inventoryProfile, EnderChestProfile enderChestProfile) {
        UUID uuid = player.getUniqueId();

        HashMap<String, Integer> counter = counters.get(uuid);
        if (counter == null) {
            counters.put(uuid, new HashMap<>());
            counter = counters.get(uuid);
        }

        if (!counter.containsKey(type)) {
            counter.put(type, 0);
        }

        if (counter.get(type) >= maxProfile) {
            List<ProfileView> profileViews = views.get(uuid);
            ProfileView last = profileViews.stream().min(Comparator.comparingLong(ProfileView::time)).orElse(null);
            if (last != null) remove(last.id);
        }
        counter.put(type, counter.get(type) + 1);
        RollbackProfile profile = new RollbackProfile(player, type, message, inventoryProfile, enderChestProfile);
        if (!views.containsKey(uuid)) {
            views.put(uuid, new ArrayList<>());
        }
        views.get(uuid).add(new ProfileView(profile.id, player.getUniqueId(), player.getName(), profile.type, profile.message, profile.time, new Date(profile.time)));
        try {
            Path playerProfilePath = dataPath.resolve(PROFILE_PATH).resolve(uuid.toString());
            if (!playerProfilePath.toFile().exists()) {
                playerProfilePath.toFile().mkdirs();
            }

            JsonObject serialize = profile.serialize();
            Gson gson = new Gson();
            String serializedJson = gson.toJson(serialize);
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(serializedJson.getBytes(StandardCharsets.UTF_8));
            deflater.finish();

            Path profilePath = playerProfilePath.resolve("%d.pinv".formatted(profile.id));
            File profilePathFile = profilePath.toFile();
            profilePathFile.createNewFile();
            profilePathFile.setWritable(true);

            final byte[] bytes = new byte[512];
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(profilePathFile))) {
                while (!deflater.finished()) {
                    int length = deflater.deflate(bytes);
                    out.write(bytes, 0, length);
                }
                deflater.end();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return profile.id;
    }

    public RollbackProfile read(long id) {
        ProfileView view = null;
        for (List<ProfileView> profileViews : views.values()) {
            for (ProfileView profileView : profileViews) {
                if (profileView.id == id) {
                    view = profileView;
                }
            }
        }
        if (view == null) return null;

        Path filePath = dataPath.resolve(PROFILE_PATH).resolve(view.playerUUID.toString()).resolve("%d.pinv".formatted(view.id));
        Inflater inflater = new Inflater();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);

        try (InputStream in = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
            byte[] tempbytes = new byte[in.available()];
            for (int i = 0; (i = in.read(tempbytes)) != -1; ) {
                inflater.setInput(tempbytes, 0, i);
            }
            final byte[] bytes = new byte[512];
            while (!inflater.finished()) {
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (IOException | DataFormatException e) {
            throw new RuntimeException(e);
        } finally {
            inflater.end();
        }

        String serializedJson = outputStream.toString(StandardCharsets.UTF_8);
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(serializedJson, JsonObject.class);
        return new RollbackProfile(json);
    }

    public void remove(long id) {
        UUID uuid = null;
        ProfileView view = null;
        for (Map.Entry<UUID, List<ProfileView>> profileViews : views.entrySet()) {
            uuid = profileViews.getKey();
            for (ProfileView profileView : profileViews.getValue()) {
                if (profileView.id == id) {
                    HashMap<String, Integer> counter = counters.get(profileView.playerUUID);
                    Path playerPath = dataPath.resolve(PROFILE_PATH).resolve(profileView.playerUUID.toString());
                    if (playerPath.toFile().exists() && playerPath.resolve("%d.pinv".formatted(profileView.id)).toFile().exists()) {
                        playerPath.resolve("%d.pinv".formatted(profileView.id)).toFile().delete();
                    }
                    view = profileView;
                    counter.put(profileView.type, counter.get(profileView.type) - 1);
                    break;
                }
            }
        }
        if (view != null) views.get(uuid).remove(view);
    }

    public void rollback(Player player, long id, String reason) {
        Bukkit.getScheduler().runTaskAsynchronously(PInvRollback.instance, () -> {
            RollbackProfile rollbackProfile = read(id);
            Bukkit.getScheduler().runTask(PInvRollback.instance, () -> {
                PInvRollbackShouldSaveEvent saveEvent = new PInvRollbackShouldSaveEvent(player, DefaultType.ROLLBACK, reason, Config.maxCount("rollback"));
                Bukkit.getPluginManager().callEvent(saveEvent);
                if (saveEvent.isCancelled()) return;
                PInvRollback.instance.getLogger().info("Player %s rollback snapshot with id: %d".formatted(player.getName(), id));
                rollbackProfile.rollback(player);
            });
        });
    }

    public void rollback(Player player, RollbackProfile profile, String reason) {
        PInvRollbackShouldSaveEvent saveEvent = new PInvRollbackShouldSaveEvent(player, DefaultType.ROLLBACK, reason, Config.maxCount("rollback"));
        Bukkit.getPluginManager().callEvent(saveEvent);
        if (saveEvent.isCancelled()) return;
        PInvRollback.instance.getLogger().info("Player %s rollback snapshot with id: %d".formatted(player.getName(), profile.id));
        profile.rollback(player);
    }

    public List<ProfileView> getSortedViews(UUID uuid) {
        return views.get(uuid).stream().sorted(Comparator.comparing(ProfileView::time, Comparator.reverseOrder())).toList();
    }

    public UUID getOwner(long id) {
        for (Map.Entry<UUID, List<ProfileView>> profileViews : views.entrySet()) {
            UUID uuid = profileViews.getKey();
            for (ProfileView profileView : profileViews.getValue()) {
                if (profileView.id == id) {
                    return uuid;
                }
            }
        }
        return null;
    }

    public List<Long> getActiveId() {
        List<Long> activeIds = new ArrayList<>();
        for (List<ProfileView> profileViews : views.values()) {
            for (ProfileView profileView : profileViews) {
                activeIds.add(profileView.id);
            }
        }
        return activeIds;
    }

    public List<String> getTypes(UUID uuid) {
        List<String> types = new ArrayList<>();
        if (counters.containsKey(uuid)) {
            types.addAll(counters.get(uuid).keySet());
        }
        return types;
    }

    public void importProfile() {
    }

    public void exportProfile() {
    }

    public static class DefaultType {
        public static final String MANUAL = Config.i18n("type.manual");
        public static final String JOIN = Config.i18n("type.join");
        public static final String QUIT = Config.i18n("type.quit");
        public static final String DEATH = Config.i18n("type.death");
        public static final String WORLD_CHANGE = Config.i18n("type.worldChange");
        public static final String ROLLBACK = Config.i18n("type.rollback");
    }

    public record ProfileView(long id, UUID playerUUID, String player, String type, String message, long time,
                              Date date) {
        public JsonObject serialize() {
            JsonObject view = new JsonObject();
            view.addProperty("id", id);
            view.addProperty("uuid", playerUUID.toString());
            view.addProperty("player", player);
            view.addProperty("type", type);
            view.addProperty("message", message);
            view.addProperty("time", time);
            return view;
        }
    }
}
