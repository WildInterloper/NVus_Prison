package me.nvus.nvus_prison_setup.AutoSell.Listeners;

import me.nvus.nvus_prison_setup.AutoSell.SellManager;
import org.bukkit.ChatColor;
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
        if (!player.hasPermission("nvus.prisoner") || !player.hasPermission("nvus.autosell") || !sellManager.isAutoSellEnabled(player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou do not have permission to use this command."));
            return;
        }
        sellManager.sellItems(player);
    }
}
