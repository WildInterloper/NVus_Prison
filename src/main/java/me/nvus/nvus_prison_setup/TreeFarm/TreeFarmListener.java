package me.nvus.nvus_prison_setup.TreeFarm;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import me.nvus.nvus_prison_setup.PrisonSetup;

public class TreeFarmListener implements Listener {
    private PrisonSetup plugin;

    // Constructor that accepts the main plugin instance
    public TreeFarmListener(PrisonSetup plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        // Check if the block being broken is a sapling and protect it unless player has admin permission
        if (block.getType() == Material.OAK_SAPLING || block.getType() == Material.SPRUCE_SAPLING || block.getType() == Material.BIRCH_SAPLING || block.getType() == Material.JUNGLE_SAPLING || block.getType() == Material.ACACIA_SAPLING || block.getType() == Material.DARK_OAK_SAPLING) {
            if (!event.getPlayer().hasPermission("nvus.admin")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cYou do not have permission to break saplings!");
                return;
            }
        }

        // Check if the block being broken is a log
        if (TreeType.isLog(block.getType())) {
            // Get the block directly beneath the log block
            Block blockBelow = block.getRelative(0, -1, 0);
            // Check if the block below is either grass or dirt, indicating this could be the base of a tree
            if (blockBelow.getType() == Material.GRASS_BLOCK || blockBelow.getType() == Material.DIRT || blockBelow.getType() == Material.PODZOL || blockBelow.getType() == Material.MYCELIUM || (blockBelow.getType() == Material.SAND)) {
                // Check if the player has the required permission
                if (event.getPlayer().hasPermission("nvus.prisoner")) {
                    event.setCancelled(true); // Cancel the event to handle block breaking manually
                    TreeFarmManager.breakTree(block, event.getPlayer(), plugin);
                }
            }
        }
    }


}
