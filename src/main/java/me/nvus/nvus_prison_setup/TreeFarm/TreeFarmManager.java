package me.nvus.nvus_prison_setup.TreeFarm;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


public class TreeFarmManager {

    public static void breakTree(Block block, Player player, Plugin plugin) {
        if (!TreeType.isLog(block.getType())) return; // Early exit if not a log

        // Determine the tree type from the block
        TreeType treeType = getTreeTypeByLog(block.getType());
        if (treeType == null) return; // Early exit if tree type is not recognized

        // Break the tree starting from the bottom block
        Block currentBlock = block;
        while (currentBlock != null && treeType.isLog(currentBlock.getType())) {
            // Add the log to the player's inventory
            player.getInventory().addItem(new ItemStack(currentBlock.getType(), 1));
            currentBlock.setType(Material.AIR); // Remove the log block
            currentBlock = currentBlock.getRelative(0, 1, 0); // Move up
        }

        // Replace the base of the tree with a sapling
        block.setType(treeType.getSaplingMaterial());

        // Collect any disconnected logs
        collectDisconnectedLogs(block, player, plugin);
    }

    private static TreeType getTreeTypeByLog(Material logMaterial) {
        for (TreeType treeType : TreeType.values()) {
            if (treeType.getLogMaterial() == logMaterial) {
                return treeType;
            }
        }
        return null; // Return null if no matching TreeType is found
    }

    public static void collectDisconnectedLogs(Block startBlock, Player player, Plugin plugin) {
        Set<Block> checkedBlocks = new HashSet<>();
        Queue<Block> toCheck = new LinkedList<>();
        toCheck.add(startBlock);

        while (!toCheck.isEmpty()) {
            Block block = toCheck.poll();
            if (!checkedBlocks.add(block)) continue; // Skip if already checked

            if (TreeType.isLog(block.getType())) {
                // Add the log to the player's inventory and remove it
                player.getInventory().addItem(new ItemStack(block.getType(), 1));
                block.setType(Material.AIR);

                // Check surrounding blocks
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            Block relative = block.getRelative(x, y, z);
                            if (TreeType.isLog(relative.getType()) && !checkedBlocks.contains(relative)) {
                                toCheck.add(relative);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void accelerateLeafDecay(Block startBlock, Plugin plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int x = -5; x <= 5; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -5; z <= 5; z++) {
                        Block block = startBlock.getRelative(x, y, z);
                        if (block.getType() == Material.OAK_LEAVES || block.getType() == Material.SPRUCE_LEAVES // Add other leaf types
                        ) {
                            block.breakNaturally();
                        }
                    }
                }
            }
        }, 20L); // Based on 20TPS
    }


}

