package me.nvus.nvus_prison_setup;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.Listeners.CommandListener;
import me.nvus.nvus_prison_setup.Configs.SettingsMenu;
import me.nvus.nvus_prison_setup.Listeners.PlayerArmor;
import me.nvus.nvus_prison_setup.Listeners.PlayerItems;
import me.nvus.nvus_prison_setup.Listeners.PlayerSpawn;
import me.nvus.nvus_prison_setup.Listeners.BlockListener;
import me.nvus.nvus_prison_setup.Listeners.ToolSwitchListener;
import me.nvus.nvus_prison_setup.Updater.UpdateChecker;
import me.nvus.nvus_prison_setup.Listeners.ToolDamageListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class PrisonSetup extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        // Initialize the ConfigManager
        configManager = new ConfigManager(this);

        // Save the default configs, if they doesn't exist
        configManager.saveDefaultConfig("config.yml");
        configManager.saveDefaultConfig("banned_items.yml");
        configManager.saveDefaultConfig("auto_switch.yml");

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerSpawn(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerArmor(configManager), this);
        getServer().getPluginManager().registerEvents(new PlayerItems(configManager), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ToolSwitchListener(configManager), this);
        this.getCommand("nvus").setExecutor(new CommandListener(this, configManager));
        //new SettingsMenu(this, configManager);
        getServer().getPluginManager().registerEvents(new SettingsMenu(this, configManager), this);

        ToolDamageListener toolDamageListener = new ToolDamageListener(configManager);
        getServer().getPluginManager().registerEvents(toolDamageListener, this);

        getLogger().info(ChatColor.translateAlternateColorCodes('&',"&a&lNVus Prison Setup has been successfully enabled!"));

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
}
