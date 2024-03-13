package me.nvus.nvus_prison_setup.AutoSell;

import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import me.nvus.nvus_prison_setup.PrisonSetup;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
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

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Admin Commands:
        if ("setprice".equalsIgnoreCase(command.getName())) {
            if (player.hasPermission("nvus.admin")) {
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /setprice <price>");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[0]);
                    setPrice(player, price);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Please provide a valid price.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to set prices.");
            }
            return true;
        }

        // Player/Prisoner Commands:
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
        ConfigurationSection pricesSection = itemPricesConfig.getConfigurationSection("Prices");
        if (pricesSection == null) {
            System.err.println("Prices section not found in item_prices.yml");
            return;
        }

        for (String key : pricesSection.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            double price = pricesSection.getDouble(key + ".Sell");
            if (material != null) {
                prices.put(material, price);
            } else {
                System.err.println("Invalid material in item_prices.yml: " + key);
            }
        }
    }

    public void reloadPrices() {
        prices.clear(); // Clear existing prices to avoid duplicates
        loadPrices(); // Reload prices
    }

    private void setPrice(Player player, double price) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to set its price!");
            return;
        }

        Material material = itemInHand.getType();
        String materialName = material.name();

        FileConfiguration itemPricesConfig = configManager.getItemPricesConfig();
        itemPricesConfig.set("Prices." + materialName + ".Sell", price);
        try {
            itemPricesConfig.save(new File(configManager.getDataFolder(), "item_prices.yml"));
            player.sendMessage(ChatColor.GREEN + "The price of " + materialName + " has been set to $" + price + ".");
            configManager.reloadConfig("item_prices.yml");
            configManager.reorderItemPrices();
            reloadPrices();
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "There was a problem saving the price.");
            e.printStackTrace();
        }
    }


    private void toggleAutoSell(Player player) {
        boolean currentStatus = autoSellStatus.getOrDefault(player.getUniqueId(), false);
        autoSellStatus.put(player.getUniqueId(), !currentStatus); // Toggle the status
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&a&lAutoSell is now %s!", (autoSellStatus.get(player.getUniqueId()) ? "&a&lenabled" : "&c&ldisabled"))));
        //player.sendMessage("AutoSell is now " + (autoSellStatus.get(player.getUniqueId()) ? "enabled!" : "disabled!"));
    }

    public boolean isAutoSellEnabled(Player player) {
        // Get the AutoSell status for this player. Defaults to FALSE if not set!!!!!
        return autoSellStatus.getOrDefault(player.getUniqueId(), false);
    }

    public void sellItems(Player player) {
        if (!player.hasPermission("nvus.prisoner")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lYou do not have permission to use this command."));
            return;
        }

        Map<Material, Integer> soldItems = new HashMap<>();

        // Loop through the player's inventory and collect sellable items
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && prices.containsKey(item.getType())) {
                soldItems.merge(item.getType(), item.getAmount(), Integer::sum);
                player.getInventory().remove(item);
            }
        }

        // Sell the items that we just removed from their inventory
        soldItems.forEach((material, quantity) -> {
            double totalSale = prices.get(material) * quantity;
            giveMoney(player, material, quantity, totalSale);
        });
    }

    private void giveMoney(Player player, Material material, int quantity, double amount) {
        Economy economy = PrisonSetup.getEconomy();
        economy.depositPlayer(player, amount);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                String.format("&a&lSold &d%dx &3%s &a&lfor $%.2f", quantity, material.toString().toLowerCase().replaceAll("_", " "), amount)));
    }



//    public void sellItems(Player player) {
//        if (!player.hasPermission("nvus.prisoner")) {
//            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&c&lYou do not have permission to use this command.")));
//            //player.sendMessage("You do not have permission to use this command.");
//            return;
//        }
//
//        double totalSale = 0;
//        ItemStack[] items = player.getInventory().getContents();
//        for (ItemStack item : items) {
//            if (item != null && prices.containsKey(item.getType())) {
//                double pricePerItem = prices.get(item.getType());
//                totalSale += pricePerItem * item.getAmount();
//                player.getInventory().remove(item);
//            }
//        }
//
//        if (totalSale > 0) {
//            giveMoney(player, totalSale);
//            // We send the message of the amount earned in the giveMoney method now!!
//            //player.sendMessage(String.format("Sold items for $%.2f", totalSale));
//        } else {
//            //player.sendMessage("No eligible items to sell."); // For debug purposes only. Comment out when done.
//        }
//    }
//
//    private void giveMoney(Player player, double amount) {
//        Economy economy = PrisonSetup.getEconomy();
//        economy.depositPlayer(player, amount);
//        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&a&lYou've earned: $%.2f", amount)));
//    }

    public boolean isSellable(Material material) {
        return prices.containsKey(material);
    }

    public double getPrice(Material material) {
        return prices.getOrDefault(material, 0.0);
    }



}
