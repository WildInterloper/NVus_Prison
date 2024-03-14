package me.nvus.nvus_prison_setup.Kit.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.Kit.KitManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class KitListener implements Listener {

    private final ConfigManager configManager;
    private final KitManager kitManager;

    public KitListener(ConfigManager configManager, KitManager kitManager) {
        this.configManager = configManager;
        this.kitManager = kitManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Check for permission first before giving kit
        if (!player.hasPermission("nvus.prisoner")) {
            return;
        }
        kitManager.givePrisonerKit(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Check for permission first before giving kit
        if (!player.hasPermission("nvus.prisoner")) {
            return;
        }
        kitManager.givePrisonerKit(player);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!shouldRestrictKitDrop()) return;

        Player player = event.getPlayer();
        // Permission check first before defining droppedItem etc
        if (!player.hasPermission("nvus.prisoner")) {
            return;
        }

        ItemStack droppedItem = event.getItemDrop().getItemStack();


        if (kitManager.isPrisonerKitItem(droppedItem)) {
            event.setCancelled(true);
            player.sendMessage("§cPER THE WARDEN: You cannot drop your prisoner kit items!");
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!shouldRestrictKitMove()) return;

        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // Permission Check first before defining clickedItem etc
        if (!player.hasPermission("nvus.prisoner")) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (kitManager.isPrisonerKitItem(clickedItem)) {
            event.setCancelled(true);
            player.sendMessage("§cPER THE WARDEN: You cannot move your prisoner kit items!");
        }
    }


    private boolean shouldRestrictKitDrop() {
        return configManager.getBoolean("config.yml", "RestrictKitDrop", false);
    }

    private boolean shouldRestrictKitMove() {
        return configManager.getBoolean("config.yml", "RestrictKitMove", false);
    }
}
