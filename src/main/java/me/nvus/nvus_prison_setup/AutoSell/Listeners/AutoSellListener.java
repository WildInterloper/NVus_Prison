package me.nvus.nvus_prison_setup.AutoSell.Listeners;

import me.nvus.nvus_prison_setup.AutoSell.SellManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AutoSellListener implements Listener {
    private final SellManager sellManager;

    public AutoSellListener(SellManager sellManager) {
        this.sellManager = sellManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("nvus.prisoner") || !sellManager.isAutoSellEnabled(player)) {
            return;
        }

        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        block.getDrops(tool).forEach(drop -> {
            Material dropType = drop.getType();
            if (sellManager.isSellable(dropType)) {
                sellManager.sellBlockDrop(player, dropType, drop.getAmount());
                block.setType(Material.AIR); // Remove the block after "selling" its drop
                event.setDropItems(false); // Prevent dropping the item
            }
        });
    }
}
