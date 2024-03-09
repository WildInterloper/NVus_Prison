package me.nvus.nvus_prison_setup.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class PlayerArmor implements Listener {
    private final ConfigManager configManager;

    public PlayerArmor(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("nvus.prisoner") && configManager.getConfig("config.yml").getBoolean("PrisonerArmor")) {
            PlayerInventory inv = player.getInventory();
            inv.setArmorContents(new ItemStack[]{
                    createArmor(Material.LEATHER_BOOTS, "Prisoner Boots"),
                    createArmor(Material.LEATHER_LEGGINGS, "Prisoner Leggings"),
                    createArmor(Material.LEATHER_CHESTPLATE, "Prisoner Chestplate"),
                    createArmor(Material.LEATHER_HELMET, "Prisoner Helmet")
            });
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lYou have been equipped with prisoner armor!"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission("nvus.prisoner")) return;

        if (event.getClickedInventory() != null && (event.getClickedInventory().getType() == InventoryType.PLAYER || event.getClickedInventory().getType() == InventoryType.CRAFTING)) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR || isArmorItem(event.getCurrentItem()) || isArmorItem(event.getCursor())) {

                boolean restrictArmor = configManager.getConfig("config.yml").getBoolean("RestrictArmor");
                if (restrictArmor) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lSorry inmate! &cYou're a &6&lprisoner &cand cannot change your armor!"));
                }
                // If restrictArmor is false, allows the player to change armor freely.
            }
        }
    }

    // Checks if the given item is a piece of prisoner armor.
    private boolean isArmorItem(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material type = item.getType();
        return type == Material.LEATHER_HELMET || type == Material.LEATHER_CHESTPLATE ||
                type == Material.LEATHER_LEGGINGS || type == Material.LEATHER_BOOTS ||
                // Add checks for other armor materials if prisoners can have those
                type == Material.CHAINMAIL_BOOTS || type == Material.IRON_HELMET;
        // We can later add additional armor sets here if we allow customization of what is considered "prisoner armor".
    }

    private ItemStack createArmor(Material material, String name) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setColor(Color.ORANGE); // Set the color for leather armor
            item.setItemMeta(meta);
        }
        return item;
    }
}
