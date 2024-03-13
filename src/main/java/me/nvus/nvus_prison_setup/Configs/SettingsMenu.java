package me.nvus.nvus_prison_setup.Configs;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SettingsMenu implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Consumer<Player>> playerTasks = new ConcurrentHashMap<>();

    public SettingsMenu(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openSettingsMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "NVus Prison Settings");
        FileConfiguration config = configManager.getConfig("config.yml");

        inv.setItem(0, createToggleItem(Material.LEATHER_CHESTPLATE, "Toggle PrisonerArmor", config.getBoolean("PrisonerArmor", true)));
        inv.setItem(1, createToggleItem(Material.IRON_CHESTPLATE, "Toggle RestrictArmor", config.getBoolean("RestrictArmor", true)));

        inv.setItem(3, createToggleItem(Material.HOPPER, "Toggle AutoPickup", config.getBoolean("AutoPickup", true)));
        inv.setItem(4, createToggleItem(Material.LEVER, "Toggle AutoSwitch", config.getBoolean("AutoSwitch", false)));

        inv.setItem(6, createToggleItem(Material.IRON_PICKAXE, "Toggle ToolDamage", config.getBoolean("ToolDamage", false)));
        inv.setItem(7, createToggleItem(Material.BOOK, "Reload Configs", false));

        player.openInventory(inv);
    }

    private ItemStack createToggleItem(Material material, String name, boolean isEnabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        // Non-Toggable Items
        if (name.equals("Reload Configs")) {
            meta.setDisplayName(ChatColor.GREEN + name);
        }
        // Toggable Items
        else {
            meta.setDisplayName(ChatColor.GREEN + name + ": " + (isEnabled ? ChatColor.BLUE + "Enabled" : ChatColor.RED + "Disabled"));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "NVus Prison Settings")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();

        // Execute any pending tasks for this player before processing a new one
        playerTasks.computeIfPresent(playerUUID, (uuid, task) -> {
            Bukkit.getScheduler().runTask(plugin, () -> task.accept(player));
            return null;
        });

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        scheduleConfigToggle(playerUUID, displayName);
    }

    private void scheduleConfigToggle(UUID playerUUID, String displayName) {
        Consumer<Player> task = player -> {
            if (displayName.contains("Toggle PrisonerArmor")) {
                toggleConfigOption(player, "PrisonerArmor");
            } else if (displayName.contains("Toggle RestrictArmor")) {
                toggleConfigOption(player, "RestrictArmor");
            } else if (displayName.contains("Toggle AutoPickup")) {
                toggleConfigOption(player, "AutoPickup");
            } else if (displayName.contains("Toggle AutoSwitch")) {
                toggleConfigOption(player, "AutoSwitch");
            } else if (displayName.contains("Reload Configs")) {
                reloadConfigs(player);
            }

        };

        playerTasks.put(playerUUID, task);
    }

    private void toggleConfigOption(final Player player, String configKey) {
        // Close the inventory to prevent further clicks
        player.closeInventory();
        // Re-open menu, 10 tick delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> openSettingsMenu(player), 10L);

        FileConfiguration config = configManager.getConfig("config.yml");
        boolean currentValue = config.getBoolean(configKey, false);

        config.set(configKey, !currentValue);

        configManager.saveConfig("config.yml");
        // Debug message
        //player.sendMessage(ChatColor.GREEN + configKey + " " + (!currentValue ? "enabled" : "disabled"));


    }


    private void reloadConfigs(final Player player) {
        configManager.reloadConfig("config.yml");
        configManager.reloadConfig("auto_switch.yml");
        configManager.reloadConfig("banned_items.yml");
        configManager.reloadConfig("item_prices.yml");
        //configManager.saveConfig("config.yml");
        player.sendMessage(ChatColor.GREEN + "[NVus Prison] : Configuration files reloaded!");
        player.closeInventory();
    }
}
