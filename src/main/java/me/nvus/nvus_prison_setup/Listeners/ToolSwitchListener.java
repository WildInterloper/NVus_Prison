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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToolSwitchListener implements Listener {
    private final ConfigManager configManager;

    public ToolSwitchListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        FileConfiguration config = configManager.getConfig("config.yml");
        FileConfiguration autoSwitchConfig = configManager.getConfig("auto_switch.yml");

        if (!player.hasPermission("nvus.prisoner") || !config.getBoolean("AutoSwitch", true)) return;

        Material blockType = event.getClickedBlock().getType();
        Material bestTool = determineBestToolForBlock(blockType, player, config, autoSwitchConfig);
        if (bestTool != null) {
            switchToTool(player, bestTool);
        }
    }

    private Material determineBestToolForBlock(Material blockType, Player player, FileConfiguration config, FileConfiguration autoSwitchConfig) {
        List<Material> prisonerTools = config.getStringList("PrisonerTools").stream()
                .map(Material::valueOf)
                .collect(Collectors.toList());


        Map<Material, List<Material>> toolEffectivenessMap = new HashMap<>();
        for (Material tool : prisonerTools) {

            if (tool.toString().endsWith("_PICKAXE")) {
                toolEffectivenessMap.put(tool, convertStringListToMaterial(autoSwitchConfig.getStringList("PickaxeMaterials")));
            } else if (tool.toString().endsWith("_AXE")) {
                toolEffectivenessMap.put(tool, convertStringListToMaterial(autoSwitchConfig.getStringList("AxeMaterials")));
            } else if (tool.toString().endsWith("_SHOVEL")) {
                toolEffectivenessMap.put(tool, convertStringListToMaterial(autoSwitchConfig.getStringList("ShovelMaterials")));
            }
        }

        for (Material tool : prisonerTools) {
            List<Material> effectiveBlocks = toolEffectivenessMap.getOrDefault(tool, List.of());
            if (effectiveBlocks.contains(blockType) && player.getInventory().contains(tool)) {
                return tool;
            }
        }

        return null; // No suitable tool found
    }


    private void switchToTool(Player player, Material tool) {
        PlayerInventory inventory = player.getInventory();
        int toolSlot = inventory.first(tool);
        if (toolSlot >= 0 && toolSlot < 9) { // If the tool is in the quickbar
            inventory.setHeldItemSlot(toolSlot);
        }
    }

    private List<Material> convertStringListToMaterial(List<String> stringList) {
        return stringList.stream().map(Material::valueOf).collect(Collectors.toList());
    }
}
