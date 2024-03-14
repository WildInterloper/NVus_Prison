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
import org.bukkit.inventory.meta.LeatherArmorMeta;

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
        // Create Prisoner Helmet
        ItemStack leatherHelmetPrison = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) leatherHelmetPrison.getItemMeta();
        if (helmetMeta != null) {
            helmetMeta.setColor(Color.ORANGE);
            helmetMeta.setDisplayName(ChatColor.GOLD + "Prisoner Helmet");
            leatherHelmetPrison.setItemMeta(helmetMeta);
        }

        // Create Prisoner Chestplate
        ItemStack leatherChestplatePrison = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) leatherChestplatePrison.getItemMeta();
        if (chestplateMeta != null) {
            chestplateMeta.setColor(Color.ORANGE);
            chestplateMeta.setDisplayName(ChatColor.GOLD + "Prisoner Chestplate");
            leatherChestplatePrison.setItemMeta(chestplateMeta);
        }

        // Create Prisoner Leggings
        ItemStack leatherLeggingsPrison = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leatherLeggingsPrison.getItemMeta();
        if (leggingsMeta != null) {
            leggingsMeta.setColor(Color.ORANGE);
            leggingsMeta.setDisplayName(ChatColor.GOLD + "Prisoner Leggings");
            leatherLeggingsPrison.setItemMeta(leggingsMeta);
        }

        // Create Prisoner Boots
        ItemStack leatherBootsPrison = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) leatherBootsPrison.getItemMeta();
        if (bootsMeta != null) {
            bootsMeta.setColor(Color.ORANGE);
            bootsMeta.setDisplayName(ChatColor.GOLD + "Prisoner Boots");
            leatherBootsPrison.setItemMeta(bootsMeta);
        }

        // Equip Prisoner Armor
        player.getInventory().setHelmet(leatherHelmetPrison);
        player.getInventory().setChestplate(leatherChestplatePrison);
        player.getInventory().setLeggings(leatherLeggingsPrison);
        player.getInventory().setBoots(leatherBootsPrison);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lYou're a prisoner! &6You've been given the default prisoner armor!"));
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
