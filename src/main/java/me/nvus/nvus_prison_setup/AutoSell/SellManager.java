package me.nvus.nvus_prison_setup.AutoSell;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.PrisonSetup;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellManager implements CommandExecutor {

    private final HashMap<UUID, Boolean> autoSellStatus = new HashMap<>();
    private final HashMap<Material, Double> prices = new HashMap<>();
    private final ConfigManager configManager;

    public SellManager(ConfigManager configManager) {
        this.configManager = configManager;
        loadPrices();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "autosell":
                toggleAutoSell(player);
                break;
            case "sellall":
                sellItems(player);
                break;
        }

        return true;
    }

    private void loadPrices() {
        FileConfiguration itemPricesConfig = configManager.getItemPricesConfig();
        for (String key : itemPricesConfig.getConfigurationSection("Prices").getKeys(false)) {
            String materialName = itemPricesConfig.getString("Prices." + key + ".Material");
            Material material = Material.matchMaterial(materialName);
            double price = itemPricesConfig.getDouble("Prices." + key + ".Sell");

            if (material != null) {
                prices.put(material, price);
            } else {
                System.err.println("Invalid material in item_prices.yml: " + materialName);
            }
        }
    }

    private void toggleAutoSell(Player player) {
        boolean currentStatus = autoSellStatus.getOrDefault(player.getUniqueId(), false);
        autoSellStatus.put(player.getUniqueId(), !currentStatus); // Toggle the status
        player.sendMessage("AutoSell is now " + (autoSellStatus.get(player.getUniqueId()) ? "enabled!" : "disabled!"));
    }

    public boolean isAutoSellEnabled(Player player) {
        // Get the AutoSell status for this player. Defaults to FALSE if not set!!!!!
        return autoSellStatus.getOrDefault(player.getUniqueId(), false);
    }

    public void sellItems(Player player) {
        if (!player.hasPermission("nvus.prisoner")) {
            player.sendMessage("You do not have permission to use this command.");
            return;
        }

        double totalSale = 0;
        ItemStack[] items = player.getInventory().getContents();
        for (ItemStack item : items) {
            if (item != null && prices.containsKey(item.getType())) {
                double pricePerItem = prices.get(item.getType());
                totalSale += pricePerItem * item.getAmount();
                player.getInventory().remove(item);
            }
        }

        if (totalSale > 0) {
            giveMoney(player, totalSale);
            // We send the message of amount give in the giveMoney method now!!
            //player.sendMessage(String.format("Sold items for $%.2f", totalSale));
        } else {
            player.sendMessage("No eligible items to sell.");
        }
    }

    private void giveMoney(Player player, double amount) {
        Economy economy = PrisonSetup.getEconomy();
        economy.depositPlayer(player, amount);
        player.sendMessage(String.format("You've been given $%.2f", amount));
    }

    public void sellBlockDrop(Player player, Material material, int amount) {
        if (!isSellable(material)) {
            return;
        }
        double price = getPrice(material) * amount;
        if (price > 0) {
            giveMoney(player, price);
            player.sendMessage(String.format("Sold %s x%d for $%.2f", material.toString(), amount, price));
        }
    }

    public boolean isSellable(Material material) {
        return prices.containsKey(material);
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }



}
