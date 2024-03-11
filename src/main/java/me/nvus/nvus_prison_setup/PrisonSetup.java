package me.nvus.nvus_prison_setup;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.Listeners.CommandListener;
import me.nvus.nvus_prison_setup.Configs.SettingsMenu;
// Listeners
import me.nvus.nvus_prison_setup.Listeners.PlayerArmor;
import me.nvus.nvus_prison_setup.Listeners.PlayerItems;
import me.nvus.nvus_prison_setup.Listeners.PlayerSpawn;
import me.nvus.nvus_prison_setup.Listeners.BlockListener;
import me.nvus.nvus_prison_setup.Listeners.ToolSwitchListener;
import me.nvus.nvus_prison_setup.Updater.UpdateChecker;
import me.nvus.nvus_prison_setup.Listeners.ToolDamageListener;
// Database
import me.nvus.nvus_prison_setup.Database.DatabaseManager;
// Gangs
import me.nvus.nvus_prison_setup.Gangs.GangCommands;
import me.nvus.nvus_prison_setup.Gangs.GangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class PrisonSetup extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager dbManager;
    private GangManager gangManager; // Added reference to GangManager
    // Initialize the DatabaseManager

    @Override
    public void onEnable() {
        // Initialize the ConfigManager
        configManager = new ConfigManager(this);

        // Get the plugin's data folder
        File dataFolder = getDataFolder();

        // Initialize the DatabaseManager with the plugin's data folder
        DatabaseManager databaseManager = new DatabaseManager(configManager);

        // Initialize the GangManager with the DatabaseManager
        gangManager = new GangManager(dbManager);

        // Check if SQLite DB Exists, if not init it
        File databaseFile = new File(dataFolder, "nvus_prison.db");
        if (!databaseFile.exists()) {
            // If the database file doesn't exist, initialize the database
            dbManager.initDatabase();
            getLogger().info("SQLite database initialized successfully.");
        } else {
            getLogger().info("SQLite database already exists.");
        }


        // Save the default configs, if they don't exist
        configManager.saveDefaultConfig("config.yml");
        configManager.saveDefaultConfig("banned_items.yml");
        configManager.saveDefaultConfig("auto_switch.yml");

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerSpawn(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerArmor(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerItems(configManager), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolSwitchListener(configManager), this);
        this.getCommand("nvus").setExecutor(new CommandListener(this, configManager));

        // Gang Related...... GANG, GANG #LOLOLOLOL
        this.getCommand("gang").setExecutor(new GangCommands(dbManager));

        // Settings Menu
        getServer().getPluginManager().registerEvents(new SettingsMenu(this, configManager), this);

        // Tool Damage
        ToolDamageListener toolDamageListener = new ToolDamageListener(configManager);
        getServer().getPluginManager().registerEvents(toolDamageListener, this);

        // Successful Startup/Enable
        getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a&lNVus Prison Setup has been successfully enabled!"));

        // UPDATE CHECKER
        new UpdateChecker(this, 12345).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().info("There is a new update available for NVus Prison Setup! Grab it from SpigotMC here: https://www.spigotmc.org/resources/nvus-prison-setup.115441/");

                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.isOp() || player.hasPermission("nvus.admin")) {
                            player.sendMessage(ChatColor.RED + "=====================================================");
                            player.sendMessage(ChatColor.YELLOW + "An update for NVus Prison Setup is available! Grab it from SpigotMC here: https://www.spigotmc.org/resources/nvus-prison-setup.115441/");
                            player.sendMessage(ChatColor.RED + "=====================================================");
                        }
                    }
                }, 20L * 60);
            }
        });

    }


    @Override
    public void onDisable() {
        // Save the config when disabling the plugin
        configManager.saveConfig("config.yml");
        configManager.saveConfig("banned_items.yml");
        configManager.saveConfig("auto_switch.yml");

        // Log a success message
        getLogger().info(ChatColor.translateAlternateColorCodes('&',"&c&lNVus Prison Setup has been successfully disabled!"));
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
    }

    public GangManager getGangManager() {
        return gangManager;
    }
}
