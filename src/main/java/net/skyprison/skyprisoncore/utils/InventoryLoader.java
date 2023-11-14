package net.skyprison.skyprisoncore.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.skyprison.skyprisoncore.SkyPrisonCore.db;

public class InventoryLoader {
    private static final Gson gson = new Gson();

    public void loadInventories(String parentDirectory) {
        File parentDir = new File(parentDirectory);
        File[] playerFolders = parentDir.listFiles();
        if (playerFolders != null) {
            int total = playerFolders.length;
            int count = 0;
            for (File playerFolder : playerFolders) {
                if (playerFolder.isDirectory()) {
                    String playerId = playerFolder.getName();
                    File inventoryFile = new File(playerFolder, "default.json");
                    if (inventoryFile.exists()) {
                        processPlayerInventory(playerId, inventoryFile);
                        count++;
                        Bukkit.getLogger().info("(" + count + "/" + total + ") Loaded inventory for player " + PlayerManager.getPlayerName(UUID.fromString(playerId)));
                    } else {
                        Bukkit.getLogger().warning("Inventory file for player " + PlayerManager.getPlayerName(UUID.fromString(playerId)) + " does not exist!");
                    }
                }
            }
        }
    }
    static class MapDeserializerDoubleToInt implements JsonDeserializer<Map<String, Object>> {
        @Override
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return (Map<String, Object>) read(json);
        }

        public Object read(JsonElement in) {
            if(in.isJsonArray()){
                return StreamSupport.stream(in.getAsJsonArray().spliterator(), false).map(this::read).collect(Collectors.toList());
            } else if(in.isJsonObject()) {
                Map<String, Object> map = new HashMap<>();
                JsonObject obj = in.getAsJsonObject();
                obj.entrySet().forEach(entry -> map.put(entry.getKey(), read(entry.getValue())));
                return map;
            } else if( in.isJsonPrimitive()) {
                JsonPrimitive primitive = in.getAsJsonPrimitive();
                if(primitive.isNumber()) {
                    String numStr = primitive.getAsString();
                    if(numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                        return primitive.getAsDouble();
                    } else {
                        return primitive.getAsInt();
                    }
                } else if (primitive.isBoolean()) {
                    return primitive.getAsBoolean();
                } else {
                    return primitive.getAsString();
                }
            }
            return null;
        }
    }
    private String jsonToYaml(String jsonStr) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(),
                        new MapDeserializerDoubleToInt())
                .create();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> map = gson.fromJson(jsonStr, type);
        // Convert Map to YAML
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        return yaml.dump(map);
    }

    private ItemStack deserializeItemStackFromYaml(String yamlStr) {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        try {
            yamlConfiguration.loadFromString(yamlStr);
            Map<String, Object> map = yamlConfiguration.getValues(false);
            return ItemStack.deserialize(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processPlayerInventory(String playerId, File inventoryFile) {
        try {
            // Parse JSON file
            JsonObject jsonObject = gson.fromJson(new FileReader(inventoryFile), JsonObject.class);
            JsonObject stats = jsonObject.getAsJsonObject("stats");
            JsonObject inventoryObject = jsonObject.getAsJsonObject("inventory");
            JsonArray enderChestArray = jsonObject.getAsJsonArray("ender-chest");

            // Deserialize player stats
            float exp = stats.get("exp").getAsFloat();
            int level = stats.get("level").getAsInt();
            double health = stats.get("health").getAsDouble();
            int hunger = stats.get("food").getAsInt();

            // Deserialize inventory and ender chest
            Inventory inventory = deserializeInventory(inventoryObject.getAsJsonArray("contents"), InventoryType.PLAYER);
            Inventory enderChest = deserializeInventory(enderChestArray, InventoryType.ENDER_CHEST);

            // Serialize inventories to Base64
            String invSerialized = PlayerManager.toBase64(inventory);
            String enderSerialized = PlayerManager.toBase64(enderChest);

            // Prepare and execute SQL query
            try(Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO player_inventories (user_id, exp, level, health, hunger, inventory, ender_chest) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE exp = VALUE(exp), level = VALUE(level), health = VALUE(health), hunger = VALUE(hunger), " +
                            "inventory = VALUE(inventory), ender_chest = VALUE(ender_chest)")) {
                ps.setString(1, playerId);
                ps.setFloat(2, exp);
                ps.setInt(3, level);
                ps.setDouble(4, health);
                ps.setInt(5, hunger);
                ps.setString(6, invSerialized);
                ps.setString(7, enderSerialized);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Inventory deserializeInventory(JsonArray itemsArray, InventoryType type) {
        Inventory inventory = Bukkit.createInventory(null, type);

        for (int i = 0; i < itemsArray.size(); i++) {
            JsonObject itemObject = itemsArray.get(i).getAsJsonObject();
            JsonObject itemDetails = itemObject.getAsJsonObject("item");
            int index = itemObject.get("index").getAsInt();

            String yamlStr = jsonToYaml(itemDetails.toString());

            ItemStack itemStack = deserializeItemStackFromYaml(yamlStr);
            inventory.setItem(index, itemStack);
        }

        return inventory;
    }

}
