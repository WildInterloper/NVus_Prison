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

import java.util.HashMap;

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
            ItemStack[] currentArmor = inv.getArmorContents();

            for (int slot = 0; slot < currentArmor.length; slot++) {
                ItemStack armorPiece = currentArmor[slot];

                // Check if current armor piece is not a prisoner armor, if it's null or AIR (empty slot), or if it's already a prisoner armor piece
                if (armorPiece != null && armorPiece.getType() != Material.AIR && !isPrisonerArmorItem(armorPiece)) {
                    // Move the non-prisoner armor piece safely before replacing
                    moveArmorToAvailableSlot(player, armorPiece, slot);
                }
            }

            // After safely moving existing armor, equip new prisoner armor
            equipPrisonerArmor(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPer The Warden: &6&lYou have been equipped with standard issue prisoner armor!"));
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        if (!player.hasPermission("nvus.prisoner")) return;

        int slot = event.getSlot();
        // Correct the slot checks for the inventory interaction
        boolean isArmorInteraction = (slot >= 5 && slot <= 8) // Player inventory armor slots (1.8+)
                || (event.getClickedInventory() instanceof PlayerInventory && (slot == 39 || slot == 38 || slot == 37 || slot == 36)); // Correct slots for armor

        if (isArmorInteraction && (isPrisonerArmorItem(event.getCurrentItem()) || isPrisonerArmorItem(event.getCursor()))) {
            boolean restrictArmor = configManager.getConfig("config.yml").getBoolean("RestrictArmor");
            if (restrictArmor) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lPer The Warden: &c You cannot change your armor!"));
            }
            // If restrictArmor is false, allows the player to change armor freely.
        }
    }

    private boolean isArmorSlot(int slot) {
        // Correct the method to align with how armor slots are identified in an InventoryClickEvent
        return slot == 39 || slot == 38 || slot == 37 || slot == 36; // The slots for helmet, chestplate, leggings, and boots respectively.
    }


    // Checks if the given item is a piece of prisoner armor.
    private boolean isPrisonerArmorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        String itemName = item.getItemMeta().getDisplayName();
        // Adjust these checks based on how you identify prisoner armor items.
        return itemName != null && (itemName.contains("Prisoner Boots") || itemName.contains("Prisoner Leggings")
                || itemName.contains("Prisoner Chestplate") || itemName.contains("Prisoner Helmet"));
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

    private void equipPrisonerArmor(Player player) {
        ItemStack[] prisonerArmor = new ItemStack[]{
                createArmor(Material.LEATHER_BOOTS, "Prisoner Boots"),
                createArmor(Material.LEATHER_LEGGINGS, "Prisoner Leggings"),
                createArmor(Material.LEATHER_CHESTPLATE, "Prisoner Chestplate"),
                createArmor(Material.LEATHER_HELMET, "Prisoner Helmet")
        };
        player.getInventory().setArmorContents(prisonerArmor);
    }

    private void moveArmorToAvailableSlot(Player player, ItemStack armorPiece, int armorSlot) {
        // Try to add the non-prisoner armor piece to the main inventory
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(armorPiece);
        if (!overflow.isEmpty()) {
            // Inventory was full, try the Ender Chest next
            if (player.getEnderChest().firstEmpty() != -1) {
                player.getEnderChest().addItem(overflow.get(0));
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so your " + armorPiece.getType() + " was moved to your Ender Chest.");
            } else {
                // Ender Chest was also full, drop the item at the player's location
                player.getWorld().dropItemNaturally(player.getLocation(), overflow.get(0));
                player.sendMessage(ChatColor.RED + "Your inventory and Ender Chest were full, so your " + armorPiece.getType() + " was dropped on the ground.");
            }
        }
        // Clear the original armor slot now that we've moved the item
        player.getInventory().setItem(armorSlot + 36, new ItemStack(Material.AIR));
    }





}
