package me.nvus.nvus_prison_setup.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.stream.Collectors;

public class ToolSwitchListener implements Listener {
    private final ConfigManager configManager;

    public ToolSwitchListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        FileConfiguration mainConfig = configManager.getConfig("config.yml");
        FileConfiguration autoSwitchConfig = configManager.getConfig("auto_switch.yml");

        if (!player.hasPermission("nvus.prisoner") || !mainConfig.getBoolean("AutoSwitch", true)) return;

        Material blockType = event.getClickedBlock().getType();

        Material bestTool = determineBestTool(blockType, player, mainConfig, autoSwitchConfig);
        if (bestTool != null) {
            switchToTool(player, bestTool);
        }
    }

    private Material determineBestTool(Material blockType, Player player, FileConfiguration mainConfig, FileConfiguration autoSwitchConfig) {
        List<String> prisonTools = mainConfig.getStringList("PrisonerTools");

        // Fetch block materials for tools from auto_switch.yml
        List<Material> pickaxeMaterials = convertStringListToMaterial(autoSwitchConfig.getStringList("PickaxeMaterials"));
        List<Material> axeMaterials = convertStringListToMaterial(autoSwitchConfig.getStringList("AxeMaterials"));
        List<Material> shovelMaterials = convertStringListToMaterial(autoSwitchConfig.getStringList("ShovelMaterials"));

        Material requiredTool = getRequiredToolForBlock(blockType, pickaxeMaterials, axeMaterials, shovelMaterials);

        if (requiredTool != null && prisonTools.contains(requiredTool.toString())) {
            return findBestToolInInventory(requiredTool, player);
        }

        return null;
    }

    private Material getRequiredToolForBlock(Material blockType, List<Material> pickaxeMaterials, List<Material> axeMaterials, List<Material> shovelMaterials) {
        if (pickaxeMaterials.contains(blockType)) {
            return Material.IRON_PICKAXE;
        } else if (axeMaterials.contains(blockType)) {
            return Material.IRON_AXE;
        } else if (shovelMaterials.contains(blockType)) {
            return Material.IRON_SHOVEL;
        }
        return null;
    }

    private Material findBestToolInInventory(Material toolType, Player player) {
        PlayerInventory inventory = player.getInventory();
        return inventory.all(toolType).values().stream()
                .findFirst()
                .map(ItemStack::getType)
                .orElse(null);
    }

    private List<Material> convertStringListToMaterial(List<String> stringList) {
        return stringList.stream().map(Material::valueOf).collect(Collectors.toList());
    }

    private void switchToTool(Player player, Material tool) {
        PlayerInventory inventory = player.getInventory();
        int toolSlot = inventory.first(tool);
        if (toolSlot >= 0 && toolSlot < 9) { // If the tool is in the quickbar
            inventory.setHeldItemSlot(toolSlot);
        }
    }
}
