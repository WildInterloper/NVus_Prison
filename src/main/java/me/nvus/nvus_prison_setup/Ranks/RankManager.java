package me.nvus.nvus_prison_setup.Ranks;

import me.nvus.nvus_prison_setup.Database.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RankManager {

    private final DatabaseManager dbManager;
    private final Economy economy; // Assuming you have a method to get the Economy service

    public RankManager(DatabaseManager dbManager, Economy economy) {
        this.dbManager = dbManager;
        this.economy = economy;
        syncRanksWithDatabase();
    }

    public void syncRanksWithDatabase() {
        dbManager.syncRanks();
    }

    public Rank getCurrentRank(Player player) {
        return dbManager.getCurrentPlayerRank(player);
    }

    public Rank getNextRank(Player player) {
        return dbManager.getPlayerNextRank(player);
    }

    public boolean rankUp(Player player) {
        Rank nextRank = getNextRank(player);
        if (nextRank == null) {
            player.sendMessage(ChatColor.RED + "You are already at the highest rank!");
            return false;
        }

        double balance = economy.getBalance(player);
        if (balance >= nextRank.getCost()) {
            EconomyResponse response = economy.withdrawPlayer(player, nextRank.getCost());
            if (response.transactionSuccess()) {
                dbManager.updatePlayerRankData(player, nextRank);
                executeRankCommands(player, nextRank.getCommands());
                //player.sendMessage(ChatColor.GREEN + "Congratulations! You've been ranked up to " + nextRank.getName() + ".");
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "Transaction failed: " + response.errorMessage);
                return false;
            }
        } else {
            player.sendMessage(ChatColor.RED + "You cannot afford to rank up. You need $" + nextRank.getCost() + ", but you only have $" + balance + ".");
            return false;
        }
    }

    private void executeRankCommands(Player player, List<String> commands) {
        if (commands == null || commands.isEmpty()) return;
        for (String command : commands) {
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }

    public String getRanksDisplay(Player player) {
        StringBuilder ranksMessage = new StringBuilder(ChatColor.GOLD + "Available Ranks:\n");
        Rank currentRank = getCurrentRank(player);
        // Assuming you have a method to get all ranks sorted by cost
        List<Rank> allRanks = dbManager.getAllRanksSorted();
        for (Rank rank : allRanks) {
            ranksMessage.append(ChatColor.YELLOW).append(rank.getName())
                    .append(ChatColor.WHITE).append(" - $")
                    .append(ChatColor.GREEN).append(rank.getCost()).append("\n");
        }
        ranksMessage.append(ChatColor.GOLD + "\nYour current rank: " + ChatColor.YELLOW + currentRank.getName());
        double balance = economy.getBalance(player);
        ranksMessage.append(ChatColor.GOLD + "\nYour balance: " + ChatColor.GREEN + "$" + balance);
        return ranksMessage.toString();
    }


    public void assignDefaultRank(Player player) {
        dbManager.assignPlayerToDefaultRank(player);
        // You might want to log this action or send a message to the player
        player.sendMessage(ChatColor.GREEN + "You have been assigned the default rank.");
    }




}

















//package me.nvus.nvus_prison_setup.Ranks;
//
//import com.google.gson.Gson;
//import me.nvus.nvus_prison_setup.PrisonSetup;
//import net.milkbowl.vault.economy.Economy;
//import net.milkbowl.vault.economy.EconomyResponse;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.entity.Player;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.stream.Collectors;
//import com.google.gson.GsonBuilder;
//import com.google.gson.reflect.TypeToken;
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//public class RankManager {
//
//    private JavaPlugin plugin;
//    private List<Rank> ranks = new ArrayList<>();
//    private Map<UUID, String> playerRanks = new HashMap<>();
//
//
//    public RankManager(JavaPlugin plugin) {
//        this.plugin = plugin;
//        ensureRankDataFile();
//        loadRanksFromConfig();
//        loadPlayerRanks();
//        loadRanksFromRankDataFile();
//    }
//
//    private void ensureRankDataFile() {
//        File rankDataFile = new File(plugin.getDataFolder(), "rank_data.json");
//        if (!rankDataFile.exists()) {
//            try {
//                plugin.saveResource("ranks.yml", false);
//                File ranksYmlFile = new File(plugin.getDataFolder(), "ranks.yml");
//                FileConfiguration ranksConfig = YamlConfiguration.loadConfiguration(ranksYmlFile);
//                updateRankDataFromYml(ranksConfig, rankDataFile);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void updateRankDataFromYml(FileConfiguration ranksConfig, File rankDataFile) {
//        try {
//            Set<String> rankKeys = ranksConfig.getConfigurationSection("Ranks").getKeys(false);
//            List<Rank> ranks = rankKeys.stream().map(rankKey -> {
//                String path = "Ranks." + rankKey;
//                String name = rankKey;
//                double cost = ranksConfig.getDouble(path + ".Cost");
//                List<String> commands = ranksConfig.getStringList(path + ".Commands");
//                return new Rank(name, cost, commands);
//            }).collect(Collectors.toList());
//
//            Map<String, Object> rankData = new HashMap<>();
//            rankData.put("ranks", ranks);
//            // Add a dummy player data for demonstration. Replace with actual player data logic.
//            rankData.put("players", Collections.singletonList(new PlayerRankData(UUID.randomUUID(), "DummyPlayer", "Default")));
//
//            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rankDataFile), StandardCharsets.UTF_8))) {
//                new Gson().toJson(rankData, writer);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void loadRanksFromConfig() {
//        File ranksYmlFile = new File(plugin.getDataFolder(), "ranks.yml");
//        if (ranksYmlFile.exists()) {
//            FileConfiguration ranksConfig = YamlConfiguration.loadConfiguration(ranksYmlFile);
//            File rankDataFile = new File(plugin.getDataFolder(), "rank_data.json");
//            updateRankDataFromYml(ranksConfig, rankDataFile);
//            // Debug log to check ranks loading
//            System.out.println("Loaded ranks: " + ranks.size());
//            ranks.forEach(rank -> System.out.println(rank.getName() + ": $" + rank.getCost()));
//        }
//    }
//
//    private void loadRanksFromRankDataFile() {
//        File rankDataFile = new File(plugin.getDataFolder(), "rank_data.json");
//        if (rankDataFile.exists()) {
//            try (Reader reader = new FileReader(rankDataFile)) {
//                Gson gson = new Gson();
//                Type type = new TypeToken<Map<String, Object>>() {}.getType();
//                Map<String, Object> rankDataMap = gson.fromJson(reader, type);
//
//                List<Map<String, Object>> ranksList = (List<Map<String, Object>>) rankDataMap.get("ranks");
//                if (ranksList != null) {
//                    this.ranks.clear(); // Clear existing ranks before loading new ones
//                    for (Map<String, Object> rankMap : ranksList) {
//                        String name = (String) rankMap.get("name");
//                        double cost = ((Number) rankMap.get("cost")).doubleValue();
//                        List<String> commands = (List<String>) rankMap.get("commands");
//                        Rank rank = new Rank(name, cost, commands);
//                        this.ranks.add(rank);
//                    }
//                }
//                System.out.println("Loaded ranks from rank_data.json: " + ranks.size());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    public Rank getCurrentRank(Player player) {
//        String currentRankName = playerRanks.get(player.getUniqueId());
//        if (currentRankName == null) {
//            // Reload ranks from config to ensure they are up to date
//            loadRanksFromConfig();
//            loadPlayerRanks();
//
//            // Assign default rank if the player has no current rank
//            assignDefaultRank(player);
//            currentRankName = playerRanks.get(player.getUniqueId());
//        }
//
//        // Use a final variable for the lambda expression
//        final String rankNameForLambda = currentRankName;
//
//        return ranks.stream()
//                .filter(rank -> rank.getName().equals(rankNameForLambda))
//                .findFirst()
//                .orElse(null); // Consider handling the default rank if not found.
//    }
//
//
//    public Rank getNextRank(Player player) {
//        Rank currentRank = getCurrentRank(player);
//        if (currentRank == null) {
//            return null; // Handle appropriately, maybe return the first rank if implementing a default catch-up mechanism.
//        }
//        int currentIndex = ranks.indexOf(currentRank);
//        if (currentIndex < ranks.size() - 1) {
//            return ranks.get(currentIndex + 1);
//        }
//        return null; // Player is at the highest rank.
//    }
//
//    public boolean rankUp(Player player) {
//        // Directly retrieve the next rank using the player object
//        Rank nextRank = getNextRank(player);
//
//        if (nextRank == null) {
//            player.sendMessage(ChatColor.RED + "You are already at the highest rank!");
//            return false;
//        }
//
//        Economy economy = PrisonSetup.getEconomy(); // Assuming this static method access is correct
//        double balance = economy.getBalance(player);
//
//        if (balance < nextRank.getCost()) {
//            player.sendMessage(ChatColor.RED + "You cannot afford to rank up. You have " + balance + ", but need " + nextRank.getCost() + ".");
//            return false;
//        }
//
//        EconomyResponse response = economy.withdrawPlayer(player, nextRank.getCost());
//        if (!response.transactionSuccess()) {
//            player.sendMessage(ChatColor.RED + "Transaction failed: " + response.errorMessage);
//            return false;
//        }
//
//        // Update the player's rank in memory and any persistent storage
//        playerRanks.put(player.getUniqueId(), nextRank.getName());
//        saveRankData(); // Ensure to implement this method to persist changes to 'rank_data.json'
//
//        // Execute rank-up commands
//        executeRankCommands(player, nextRank.getCommands());
//
//        player.sendMessage(ChatColor.GREEN + "You've been ranked up to " + nextRank.getName() + "!");
//        return true;
//    }
//
//
//    private void updatePlayerRank(Player player, Rank nextRank) {
//        // Update the player's current rank in memory (if you're keeping a cache)
//        playerRanks.put(player.getUniqueId(), nextRank.getName());
//        // Save the player's current rank to persistent storage
//        saveRankData();
//    }
//
//
//
//    private void executeRankCommands(Player player, List<String> commands) {
//        if (commands == null || commands.isEmpty()) return;
//        String playerName = player.getName();
//        commands.forEach(command ->
//                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.replace("%player%", playerName))
//        );
//    }
//
//
//    private void saveRankData() {
//        File rankDataFile = new File(plugin.getDataFolder(), "rank_data.json");
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        // Load existing rank data to merge with updates
//        Map<String, Object> existingData = new HashMap<>();
//        try (Reader reader = new FileReader(rankDataFile)) {
//            Type type = new TypeToken<Map<String, Object>>() {}.getType();
//            existingData = gson.fromJson(reader, type);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        // If there's no existing data or it's the first time, initialize structures
//        if (existingData == null) {
//            existingData = new HashMap<>();
//        }
//
//        // Prepare rank data to be saved
//        List<Map<String, Object>> serializedRanks = ranks.stream().map(rank -> {
//            Map<String, Object> rankMap = new HashMap<>();
//            rankMap.put("name", rank.getName());
//            rankMap.put("cost", rank.getCost());
//            rankMap.put("commands", rank.getCommands());
//            return rankMap;
//        }).collect(Collectors.toList());
//
//        // Prepare player rank data to be saved
//        List<Map<String, Object>> serializedPlayerRanks = playerRanks.entrySet().stream().map(entry -> {
//            Map<String, Object> playerRankMap = new HashMap<>();
//            playerRankMap.put("uuid", entry.getKey().toString());
//            playerRankMap.put("rankName", entry.getValue());
//            return playerRankMap;
//        }).collect(Collectors.toList());
//
//        // Update the existing data map
//        existingData.put("ranks", serializedRanks);
//        existingData.put("players", serializedPlayerRanks);
//
//        // Save the updated data back to the file
//        try (Writer writer = new FileWriter(rankDataFile)) {
//            gson.toJson(existingData, writer);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public String getRanksDisplay(Player player) {
//        StringBuilder ranksMessage = new StringBuilder(ChatColor.GOLD + "Available Ranks:\n");
//        for (Rank rank : ranks) {
//            ranksMessage.append(ChatColor.YELLOW).append(rank.getName())
//                    .append(ChatColor.WHITE).append(" - $")
//                    .append(ChatColor.GREEN).append(rank.getCost()).append("\n");
//        }
//
//        // Fetch the player's current rank from the stored data
//        String currentRankName = playerRanks.getOrDefault(player.getUniqueId(), "Unranked");
//        // Using Vault to get the player's balance
//        Economy economy = PrisonSetup.getEconomy();
//        double balance = economy.getBalance(player);
//
//        // Append the player's current rank and balance to the message
//        ranksMessage.append(ChatColor.GOLD + "\nYour current rank: " + ChatColor.YELLOW + currentRankName);
//        ranksMessage.append(ChatColor.GOLD + "\nYour balance: " + ChatColor.GREEN + "$" + balance);
//
//        return ranksMessage.toString();
//    }
//
//    public void assignDefaultRank(Player player) {
//        if (!playerRanks.containsKey(player.getUniqueId())) {
//            // Ensure this method successfully finds and assigns the default rank
//            Rank defaultRank = ranks.stream().filter(r -> r.getName().equalsIgnoreCase("Default")).findFirst().orElse(null);
//            if (defaultRank != null) {
//                playerRanks.put(player.getUniqueId(), defaultRank.getName());
//                // Debug log
//                System.out.println("Assigning default rank to " + player.getName());
//                saveRankData();
//                player.sendMessage(ChatColor.GREEN + "You've been assigned the default rank: " + defaultRank.getName());
//            } else {
//                System.out.println("Default rank not found in loaded ranks.");
//            }
//        }
//    }
//
//
//    private void loadPlayerRanks() {
//        File rankDataFile = new File(plugin.getDataFolder(), "rank_data.json");
//        if (rankDataFile.exists()) {
//            try (Reader reader = new FileReader(rankDataFile)) {
//                Type type = new TypeToken<Map<String, List<PlayerRankData>>>() {}.getType();
//                Map<String, List<PlayerRankData>> data = new Gson().fromJson(reader, type);
//                List<PlayerRankData> playerRankDataList = data.get("players");
//                if (playerRankDataList != null) {
//                    playerRankDataList.forEach(playerRankData -> playerRanks.put(playerRankData.getUuid(), playerRankData.getRankName()));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//
//
//
//
//
//
//
//}
