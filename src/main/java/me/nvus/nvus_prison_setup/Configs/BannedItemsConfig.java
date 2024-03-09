package me.nvus.nvus_prison_setup.Configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BannedItemsConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public BannedItemsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "banned_items.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "banned_items.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("banned_items.yml", false);
            plugin.getLogger().info("banned_items.yml has been created.");
        }
    }


    public List<String> getBannedItems() {
        return getConfig().getStringList("BannedItems");
    }
}
