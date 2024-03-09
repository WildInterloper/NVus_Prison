package me.nvus.nvus_prison_setup.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.PrisonSetup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerItems implements Listener {

    private final ConfigManager configManager;

    public PlayerItems(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());

        if (item != null && isBannedItem(item.getType())) {
            if (player.hasPermission("nvus.prisoner")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c&lSorry inmate! &cYou're a &6&lprisoner &cand cannot use this tool!"));
            }
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // Check if the clicked item is a banned item
        if (clickedItem != null && isBannedItem(clickedItem.getType())) {
            // Check if the player is a prisoner
            if (player.hasPermission("nvus.prisoner")) {
                // Cancel the event to prevent interaction with banned items
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&c&lSorry inmate! &cYou're a &6&lprisoner &cand cannot use this tool!"));
            }
        }
    }

    private boolean isBannedItem(Material itemType) {
        List<String> bannedItems = configManager.getConfig("banned_items.yml").getStringList("BannedItems");

        return bannedItems.contains(itemType.toString());
    }
}
