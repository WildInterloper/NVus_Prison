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
import me.nvus.nvus_prison_setup.Placeholders.GangPlaceholders;
import me.nvus.nvus_prison_setup.Updater.UpdateChecker;
import me.nvus.nvus_prison_setup.Listeners.ToolDamageListener;
import me.nvus.nvus_prison_setup.TreeFarm.TreeFarmListener;
import me.nvus.nvus_prison_setup.AutoSell.SellManager;
// Database
import me.nvus.nvus_prison_setup.Database.DatabaseManager;
// Gangs
import me.nvus.nvus_prison_setup.Gangs.GangCommands;
import me.nvus.nvus_prison_setup.Gangs.GangManager;

// Vault
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

// Bukkit
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
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

    private static Economy econ = null; // Vault / Economy

    @Override
    public void onEnable() {

        // Initialize the ConfigManager
        configManager = new ConfigManager(this);

        // Initialize the DatabaseManager with ConfigManager
        dbManager = new DatabaseManager(configManager); // Correctly assign to the class field

        // Initialize the GangManager with the DatabaseManager
        gangManager = new GangManager(dbManager); // Use the corrected dbManager

        // Check if SQLite DB Exists, if not init it
        File databaseFile = new File(getDataFolder(), "nvus_prison.db");
        if (!databaseFile.exists()) {
            dbManager.initDatabase(); // Correct use of dbManager after initialization
            getLogger().info("Database initialized successfully.");
        } else {
            getLogger().info("SQLite database already exists.");
        }

        // Save the default configs, if they don't exist
        configManager.saveDefaultConfig("config.yml");
        configManager.saveDefaultConfig("banned_items.yml");
        configManager.saveDefaultConfig("auto_switch.yml");
        configManager.saveDefaultConfig("item_prices.yml");

        // Check if Vault is installed, it's a hard dependency so disable plugin if not installed!
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //FileConfiguration config = configManager.getConfig("config.yml");

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new PlayerSpawn(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerArmor(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerItems(configManager), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolSwitchListener(configManager), this);
        this.getCommand("nvus").setExecutor(new CommandListener(this, configManager));

        // Gang Related... GANG, GANG #LOLOLOLOL
        this.getCommand("gang").setExecutor(new GangCommands(dbManager)); // Now correctly using initialized dbManager
        // Register the Gangs placeholder expansion
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GangPlaceholders(gangManager).register();
        }

        // Register the Auto Sell and Sell All Listeners
        boolean autoSellEnabled = configManager.getConfig("config.yml").getBoolean("AutoSell", true);
        boolean sellAllEnabled = configManager.getConfig("config.yml").getBoolean("SellAll", true);
        SellManager sellManager = new SellManager(configManager);

        // If they are true, register the commands.
        if (autoSellEnabled) {
            // Register the autosell command.
            this.getCommand("autosell").setExecutor(sellManager);
        }
        // If they are true, register the commands.
        if (sellAllEnabled) {
            // Register the sellall command.
            this.getCommand("sellall").setExecutor(sellManager);
        }

        // Settings Menu
        getServer().getPluginManager().registerEvents(new SettingsMenu(this, configManager), this);

        // Tool Damage
        ToolDamageListener toolDamageListener = new ToolDamageListener(configManager);
        getServer().getPluginManager().registerEvents(toolDamageListener, this);

        // TreeFarm Boolean Check
        if (configManager.getBoolean("config.yml", "TreeFarm", false)) {
            getServer().getPluginManager().registerEvents(new TreeFarmListener(this), this);
        }

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
//        configManager.saveConfig("config.yml");
//        configManager.saveConfig("banned_items.yml");
//        configManager.saveConfig("auto_switch.yml");

        // Log a success message
        getLogger().info(ChatColor.translateAlternateColorCodes('&',"&c&lNVus Prison Setup has been successfully disabled!"));
    }

    // Vault Stuff
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
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
