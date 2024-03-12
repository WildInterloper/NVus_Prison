package me.nvus.nvus_prison_setup.Configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    private FileConfiguration itemPricesConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
    }

    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public void reloadConfig(String configName) {
        File configFile = getConfigFile(configName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configs.put(configName, config);
    }

    public FileConfiguration getConfig(String configName) {
        if (!configs.containsKey(configName)) {
            reloadConfig(configName);
        }
        return configs.get(configName);
    }

    public boolean getBoolean(String configName, String path, boolean defaultValue) {
        FileConfiguration config = getConfig(configName);
        if (config == null) {
            return defaultValue;
        }
        return config.getBoolean(path, defaultValue);
    }

    public void saveConfig(String configName) {
        FileConfiguration config = getConfig(configName);
        File configFile = getConfigFile(configName);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig(String configName) {
        File configFile = getConfigFile(configName);
        if (!configFile.exists()) {
            plugin.saveResource(configName, false);
            plugin.getLogger().info(configName + " has been created.");
        }
    }

    private File getConfigFile(String configName) {
        if (!configFiles.containsKey(configName)) {
            File configFile = new File(plugin.getDataFolder(), configName);
            configFiles.put(configName, configFile);
        }
        return configFiles.get(configName);
    }

    // ITEM CONFIGURATION
    public void loadItemPricesConfig(File dataFolder) {
        File file = new File(dataFolder, "item_prices.yml");
        if (!file.exists()) {
            try {
                file.createNewFile(); // Create the file if it doesn't exist
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        itemPricesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getItemPricesConfig() {
        return itemPricesConfig;
    }
}
