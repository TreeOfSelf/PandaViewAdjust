package me.TreeOfSelf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PandaViewConfig {
    private static final String CONFIG_FILE = "config/PandaViewConfig.json";
    private List<ConfigEntry> configEntries = new ArrayList<>();

    public PandaViewConfig() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }

        loadConfig();
    }

    private void createDefaultConfig(File configFile) {
        try (FileWriter writer = new FileWriter(configFile)) {
            JsonArray defaultConfig = new JsonArray();

            JsonObject entry1 = new JsonObject();
            entry1.addProperty("maxPlayerCount", 5);
            entry1.addProperty("maxMSPT", 30);
            entry1.addProperty("viewDistance", 32);
            entry1.addProperty("simulationDistance", 12);
            defaultConfig.add(entry1);

            JsonObject entry2 = new JsonObject();
            entry2.addProperty("maxPlayerCount", 10);
            entry2.addProperty("maxMSPT", 40);
            entry2.addProperty("viewDistance", 12);
            entry2.addProperty("simulationDistance", 6);
            defaultConfig.add(entry2);

            JsonObject entry3 = new JsonObject();
            entry3.addProperty("maxPlayerCount", 50);
            entry3.addProperty("maxMSPT", 50);
            entry3.addProperty("viewDistance", 6);
            entry3.addProperty("simulationDistance", 6);
            defaultConfig.add(entry3);

            JsonObject entry4 = new JsonObject();
            entry4.addProperty("maxPlayerCount", 0);
            entry4.addProperty("maxMSPT", 0);
            entry4.addProperty("viewDistance", 3);
            entry4.addProperty("simulationDistance", 3);
            defaultConfig.add(entry4);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(defaultConfig, writer);

            System.out.println("Created default PandaViewConfig.json");
        } catch (IOException ignored) {
        }
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(new File(CONFIG_FILE))) {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(reader, JsonArray.class);

            for (var jsonElement : jsonArray) {
                if (jsonElement.isJsonObject()) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    try {
                        ConfigEntry entry = new ConfigEntry(
                                jsonObject.get("maxPlayerCount").getAsInt(),
                                jsonObject.get("maxMSPT").getAsInt(),
                                jsonObject.get("viewDistance").getAsInt(),
                                jsonObject.get("simulationDistance").getAsInt()
                        );
                        configEntries.add(entry);
                    } catch (JsonParseException ignored) {
                    }
                }
            }

            if (configEntries.isEmpty()) {
                System.err.println("Warning: No valid entries found in PandaViewConfig.json");
            } else {
                System.out.println("Loaded " + configEntries.size() + " configuration entries");
            }
        } catch (IOException e) {
            File configFile = new File(CONFIG_FILE);
            createDefaultConfig(configFile);
            loadConfig();
        }
    }

    public List<ConfigEntry> getConfigEntries() {
        return configEntries;
    }

    public static class ConfigEntry {
        public final int maxPlayerCount;
        public final int maxMSPT;
        public final int viewDistance;
        public final int simulationDistance;

        public ConfigEntry(int maxPlayerCount, int maxMSPT, int viewDistance, int simulationDistance) {
            this.maxPlayerCount = maxPlayerCount;
            this.maxMSPT = maxMSPT;
            this.viewDistance = viewDistance;
            this.simulationDistance = simulationDistance;
        }
    }
}