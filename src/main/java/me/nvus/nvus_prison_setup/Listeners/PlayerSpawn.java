package me.nvus.nvus_prison_setup.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;

public class PlayerSpawn implements Listener {
    private final ConfigManager configManager;

    public PlayerSpawn(ConfigManager configManager) {
        this.configManager = configManager;
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();

        // Check if the player has the permission and if PrisonerArmor is enabled in config
        if (joinedPlayer.hasPermission("nvus.prisoner") && configManager.getConfig("config.yml").getBoolean("PrisonerArmor", true)) {
            equipPrisonerArmor(joinedPlayer);
        }


    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();

        if (deadPlayer.hasPermission("nvus.prisoner") && configManager.getConfig("config.yml").getBoolean("PrisonerArmor", false)) {
            // Remove prisoner armor from the list of dropped items
            event.getDrops().removeIf(drop -> isPrisonerArmor(drop.getType()));
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player respawnedPlayer = event.getPlayer();

        if (respawnedPlayer.hasPermission("nvus.prisoner") && configManager.getConfig("config.yml").getBoolean("PrisonerArmor", true)) {
            equipPrisonerArmor(respawnedPlayer);
        }
    }

    private void equipPrisonerArmor(Player player) {
        handleNonPrisonerArmorItems(player); // Ensure non-prisoner armor is handled before equipping new armor

        // Define and equip standard prisoner armor
        ItemStack[] standardPrisonerArmor = new ItemStack[]{
                createArmor(Material.LEATHER_BOOTS, "Prisoner Boots"),
                createArmor(Material.LEATHER_LEGGINGS, "Prisoner Leggings"),
                createArmor(Material.LEATHER_CHESTPLATE, "Prisoner Chestplate"),
                createArmor(Material.LEATHER_HELMET, "Prisoner Helmet")
        };

        player.getInventory().setArmorContents(standardPrisonerArmor);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPer The Warden: &6&lYou have been equipped with standard issue prisoner armor!"));
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

    private void handleNonPrisonerArmorItems(Player player) {
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        for (int i = 0; i < armorContents.length; i++) {
            ItemStack armorPiece = armorContents[i];
            // Check if the armor piece is not part of the prisoner kit
            if (armorPiece != null && armorPiece.getType() != Material.AIR && !isPrisonerArmorItem(armorPiece)) {
                moveArmorToAvailableSlot(player, armorPiece);
                armorContents[i] = new ItemStack(Material.AIR); // Remove the non-prisoner armor from the armor slot
            }
        }

        // Update the player's armor contents after removals
        player.getInventory().setArmorContents(armorContents);
    }

    private boolean isPrisonerArmorItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        String itemName = meta.getDisplayName();
        // Adjust the check based on your naming convention for prisoner armor
        return itemName != null && itemName.contains("Prisoner");
    }

    private void moveArmorToAvailableSlot(Player player, ItemStack armorPiece) {
        // Attempt to move the existing armor piece to an available slot
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(armorPiece);
        if (!overflow.isEmpty()) {
            // Check Ender Chest
            if (player.getEnderChest().firstEmpty() != -1) {
                player.getEnderChest().addItem(overflow.get(0));
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so your armor was moved to your Ender Chest.");
            } else {
                // Drop the item at the player's location
                player.getWorld().dropItemNaturally(player.getLocation(), overflow.get(0));
                player.sendMessage(ChatColor.RED + "Your inventory and Ender Chest were full, so your armor was dropped on the ground.");
            }
        }
    }



    // Destroy armor upon death etc.
    private void destroyDefaultPrisonerArmor(Player player) {
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Using this as a debug/check, will comment out later!
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lYour default prisoner armor has been destroyed!"));
    }

    private boolean isPrisonerArmor(Material material) {
        return material == Material.LEATHER_HELMET ||
                material == Material.LEATHER_CHESTPLATE ||
                material == Material.LEATHER_LEGGINGS ||
                material == Material.LEATHER_BOOTS;
    }




    /*
    private void destroyDefaultPrisonerArmor(Player player) {
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Using this as a debug/check, will comment out later!
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lYour default prisoner armor has been destroyed!"));
    }
    */

    /*
    // Destroy armor upon death etc.
    private void destroyDefaultPrisonerArmor(Player player) {
        // Loop through the armor slots and remove any armor pieces
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                player.getInventory().remove(armor);
            }
        }

        // Using this as a debug/check, will comment out later!
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lYour default prisoner armor has been destroyed!"));
    }
    */
}
