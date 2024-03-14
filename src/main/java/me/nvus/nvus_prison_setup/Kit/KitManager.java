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
import java.util.List;
import java.util.Map;

public class KitManager {

    private final ConfigManager configManager;

    public KitManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean isPrisonerKitItem(ItemStack item) {
        FileConfiguration config = configManager.getConfig("config.yml");
        List<Map<?, ?>> kitItems = config.getMapList("PrisonerKitItems");

        for (Map<?, ?> itemSpec : kitItems) {
            String itemName = (String) itemSpec.get("item");
            Material material = Material.matchMaterial(itemName);

            if (material != null && item.getType() == material) {
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

            // Set lore if available
            if (itemSpec.containsKey("lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : (List<String>) itemSpec.get("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(lore);
            }

            // Set enchantments if available
            if (itemSpec.containsKey("enchantments")) {
                Map<String, Integer> enchantments = (Map<String, Integer>) itemSpec.get("enchantments");
                for (Map.Entry<String, Integer> enchantmentEntry : enchantments.entrySet()) {
                    Enchantment enchantment = Enchantment.getByName(enchantmentEntry.getKey());
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, enchantmentEntry.getValue(), true);
                    }
                }
            }

            item.setItemMeta(meta);

            // Set item in specified quickbar slot, if available
            if (itemSpec.containsKey("slot")) {
                player.getInventory().setItem((Integer) itemSpec.get("slot"), item);
            } else {
                player.getInventory().addItem(item);
            }
        }

    }
}
