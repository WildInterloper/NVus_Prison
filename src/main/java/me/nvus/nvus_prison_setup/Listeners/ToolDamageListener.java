package me.nvus.nvus_prison_setup.Listeners;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ToolDamageListener implements Listener {
    private final ConfigManager configManager;

    public ToolDamageListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (!(itemInHand.getItemMeta() instanceof Damageable)) return;

        FileConfiguration config = configManager.getConfig("config.yml");
        boolean toolDamageEnabled = config.getBoolean("ToolDamage", true);
        List<Material> prisonerTools = config.getStringList("PrisonerTools").stream().map(Material::valueOf).collect(Collectors.toList());

        if (!toolDamageEnabled && prisonerTools.contains(itemInHand.getType())) {
            Damageable itemMeta = (Damageable) itemInHand.getItemMeta();

            itemMeta.setDamage(0);

            itemInHand.setItemMeta((ItemMeta) itemMeta);

            // DEBUGGGGGGGGGGGG
            //player.sendMessage(ChatColor.GREEN + "Your tool's durability was preserved.");
        }
    }


}
