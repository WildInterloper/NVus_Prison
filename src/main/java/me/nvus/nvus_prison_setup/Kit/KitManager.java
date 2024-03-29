package me.nvus.nvus_prison_setup.Kit;
import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitManager {

    private final ConfigManager configManager;

    public KitManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean isPrisonerKitItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        FileConfiguration config = configManager.getConfig("config.yml");
        List<Map<?, ?>> kitItems = config.getMapList("PrisonerKitItems");

        ItemMeta meta = item.getItemMeta();
        String itemName = meta.hasDisplayName() ? meta.getDisplayName() : "";
        List<String> itemLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        for (Map<?, ?> itemSpec : kitItems) {
            String configItemName = ChatColor.translateAlternateColorCodes('&', (String) itemSpec.get("name"));
            List<String> configItemLore = new ArrayList<>();
            if (itemSpec.get("lore") != null) {
                for (String line : (List<String>) itemSpec.get("lore")) {
                    configItemLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
            }

            // Check if the item matches the config item name ANNNND lore
            // Just to be double sure we're saying yes this is a prioner kit item on the right items!
            if (itemName.equals(configItemName) && itemLore.equals(configItemLore)) {
                return true;
            }
        }

        return false;
    }

    public void givePrisonerKit(Player player) {
        if (!configManager.getBoolean("config.yml", "PrisonerKit", false)) {
            return;
        }

        FileConfiguration config = configManager.getConfig("config.yml");
        List<Map<?, ?>> kitItems = config.getMapList("PrisonerKitItems");

        for (Map<?, ?> itemSpec : kitItems) {
            Material material = Material.matchMaterial((String) itemSpec.get("item"));
            if (material == null) continue;

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            // Configure item meta (name, lore, enchantments)...
            if (itemSpec.containsKey("name")) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (String) itemSpec.get("name")));
            }
            if (itemSpec.containsKey("lore")) {
                List<String> lore = new ArrayList<>();
                ((List<String>) itemSpec.get("lore")).forEach(line -> lore.add(ChatColor.translateAlternateColorCodes('&', line)));
                meta.setLore(lore);
            }
            if (itemSpec.containsKey("enchantments")) {
                ((Map<String, Integer>) itemSpec.get("enchantments")).forEach((enchant, level) -> meta.addEnchant(Enchantment.getByName(enchant.toUpperCase()), level, true));
            }
            item.setItemMeta(meta);

            if (itemSpec.containsKey("slot")) {
                int slot = (Integer) itemSpec.get("slot");
                ItemStack existingItem = player.getInventory().getItem(slot);

                if (existingItem != null && existingItem.getType() != Material.AIR && !isPrisonerKitItem(existingItem)) {
                    moveItemToAvailableSlot(player, existingItem);
                }

                player.getInventory().setItem(slot, item);
            } else {
                player.getInventory().addItem(item);
            }
        }
    }

    private void moveItemToAvailableSlot(Player player, ItemStack item) {
        // Attempt to move the existing item to an available slot
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
        if (!overflow.isEmpty()) {
            // Check Ender Chest
            if (player.getEnderChest().firstEmpty() != -1) {
                player.getEnderChest().addItem(overflow.get(0));
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so an item was moved to your Ender Chest.");
            } else {
                // Drop the item at the player's location
                player.getWorld().dropItemNaturally(player.getLocation(), overflow.get(0));
                player.sendMessage(ChatColor.RED + "Your inventory and Ender Chest were full, so an item was dropped on the ground.");
            }
        }
    }






//    public void givePrisonerKit(Player player) {
//        if (!configManager.getBoolean("config.yml", "PrisonerKit", false)) {
//            return;
//        }
//
//        FileConfiguration config = configManager.getConfig("config.yml");
//        List<Map<?, ?>> kitItems = config.getMapList("PrisonerKitItems");
//
//        for (Map<?, ?> itemSpec : kitItems) {
//            Material material = Material.matchMaterial((String) itemSpec.get("item"));
//            if (material == null) continue;
//
//            ItemStack item = new ItemStack(material);
//            ItemMeta meta = item.getItemMeta();
//
//            // Configure item meta (name, lore, enchantments)...
//            // This part remains the same as in your original method
//
//            item.setItemMeta(meta);
//
//            // Attempt to set the item in the specified slot
//            if (itemSpec.containsKey("slot")) {
//                int slot = (Integer) itemSpec.get("slot");
//                ItemStack existingItem = player.getInventory().getItem(slot);
//
//                // If the slot is occupied, try to find a new place for the existing item
//                if (existingItem != null && existingItem.getType() != Material.AIR) {
//                    HashMap<Integer, ItemStack> failedItems = player.getInventory().addItem(existingItem);
//
//                    // If inventory is full, try ender chest or drop to the ground
//                    if (!failedItems.isEmpty()) {
//                        failedItems.forEach((integer, itemStack) -> {
//                            if (player.getEnderChest().firstEmpty() != -1) {
//                                player.getEnderChest().addItem(itemStack);
//                                player.sendMessage(ChatColor.YELLOW + "Some items were moved to your Ender Chest due to a full inventory.");
//                            } else {
//                                player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
//                                player.sendMessage(ChatColor.RED + "Your inventory and Ender Chest are full. Some items were dropped on the ground.");
//                            }
//                        });
//                    }
//
//                    // Now we can safely place the kit item in the intended slot
//                    player.getInventory().setItem(slot, item);
//                } else {
//                    // Slot is empty, just place the item there
//                    player.getInventory().setItem(slot, item);
//                }
//            } else {
//                // No specific slot defined, just add to inventory
//                HashMap<Integer, ItemStack> failedItems = player.getInventory().addItem(item);
//
//                // Handle full inventory as above
//                if (!failedItems.isEmpty()) {
//                    failedItems.forEach((integer, itemStack) -> {
//                        if (player.getEnderChest().firstEmpty() != -1) {
//                            player.getEnderChest().addItem(itemStack);
//                            player.sendMessage(ChatColor.YELLOW + "Some items were moved to your Ender Chest due to a full inventory.");
//                        } else {
//                            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
//                            player.sendMessage(ChatColor.RED + "Your inventory and Ender Chest are full. Some items were dropped on the ground.");
//                        }
//                    });
//                }
//            }
//        }
//    }




}
