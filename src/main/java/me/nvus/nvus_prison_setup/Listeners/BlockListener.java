package me.nvus.nvus_prison_setup.Listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.nvus.nvus_prison_setup.PrisonSetup;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockListener implements Listener {
    private final PrisonSetup plugin;

    private final Set<Material> storageBlocks = new HashSet<>(Arrays.asList(
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ACACIA_CHEST_BOAT,
            Material.BIRCH_CHEST_BOAT,
            Material.DARK_OAK_CHEST_BOAT,
            Material.JUNGLE_CHEST_BOAT,
            Material.OAK_CHEST_BOAT,
            Material.SPRUCE_CHEST_BOAT,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.BARREL,
            Material.DISPENSER,
            Material.DROPPER,
            Material.HOPPER,
            Material.CAULDRON,
            Material.SHULKER_BOX,
            Material.CHEST_MINECART,
            Material.BARREL,
            Material.CHISELED_BOOKSHELF,
            Material.BUNDLE
            // Add more!
    ));

    public BlockListener(PrisonSetup plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!isBlockBreakAllowed(player, block.getLocation())) {
            event.setCancelled(true); // Prevent the block from being broken
            event.setDropItems(false); // Prevent the block from dropping items
            //player.sendMessage("You cannot break blocks in this area.");


            return;
        }

        if (storageBlocks.contains(block.getType())) {
            return;
        }

        if (player.hasPermission("nvus.prisoner") && plugin.getConfigManager().getConfig("config.yml").getBoolean("AutoPickup")) {
            List<ItemStack> drops = block.getDrops().stream().toList();
            for (ItemStack drop : drops) {
                if (player.getInventory().addItem(drop).isEmpty()) {

                    event.setDropItems(false);
                } else {
                    // Inventory is full, drop the item on the ground
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    player.sendMessage("Your inventory is currently full. The resource has been dropped on the ground!");
                }
            }
        }
    }

    private boolean isBlockBreakAllowed(Player player, org.bukkit.Location bukkitLocation) {

        if (player.hasPermission("worldguard.region.bypass." + bukkitLocation.getWorld().getName()) || player.hasPermission("worldguard.region.bypass.*")) {
            return true;
        }

        com.sk89q.worldedit.util.Location location = BukkitAdapter.adapt(bukkitLocation);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        com.sk89q.worldguard.LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        if (query.testBuild(location, localPlayer)) {
            return true;
        }

        // region membership, ownership, and specific flags set on the region
        return query.testState(location, localPlayer, Flags.BLOCK_BREAK);
    }


}
