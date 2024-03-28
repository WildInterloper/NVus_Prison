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
        // Added additional permission check
        if ((sellManager.isAutoSellEnabled(player)) ) {
            sellManager.sellItems(player);
        }
    }

}
