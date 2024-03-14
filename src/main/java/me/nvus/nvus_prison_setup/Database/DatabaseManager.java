package me.nvus.nvus_prison_setup.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.io.File;

import me.nvus.nvus_prison_setup.Gangs.GangInfo;
import me.nvus.nvus_prison_setup.Ranks.Rank;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import me.nvus.nvus_prison_setup.Configs.ConfigManager;
import org.bukkit.entity.Player;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DatabaseManager {
    private ConfigManager configManager;
    private String url;
    private String databaseType; // To store the database type (MySQL or SQLite)

    public DatabaseManager(ConfigManager configManager) {
        this.configManager = configManager;
        setupDatabase();
    }


    private void setupDatabase() {
        FileConfiguration config = configManager.getConfig("config.yml");
        this.databaseType = config.getString("Database.Type", "MySQL");

        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "nvus_prison");
        String username = URLEncoder.encode(config.getString("username", "username"), StandardCharsets.UTF_8);
        String password = URLEncoder.encode(config.getString("password", "password"), StandardCharsets.UTF_8);

        if ("SQLite".equalsIgnoreCase(databaseType)) {
            File dbFile = new File(configManager.getDataFolder(), "nvus_prison.db");
            this.url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        } else {
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + username + "&password=" + password;
        }
        // Init Gangs Database
        initializeGangDatabase();
        // Init Ranks Database
        initializeRanksDatabase();
    }


    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }




    // Public Accessor to initialize the database
    public void initGangDatabase() {
        initializeGangDatabase();
    }
    public void initRanksDatabase() {
        initializeRanksDatabase();
    }


/* ================================================================================================================
                                            RANKS RELATED METHODS
==================================================================================================================*/

    private void initializeRanksDatabase() {
        String sqlRanks = "CREATE TABLE IF NOT EXISTS nvus_ranks ("
                + "rank_name VARCHAR(255) NOT NULL PRIMARY KEY,"
                + "cost DOUBLE NOT NULL,"
                + "commands TEXT NOT NULL"
                + ");";

        String sqlRanksPlayers = "CREATE TABLE IF NOT EXISTS nvus_ranks_players ("
                + "uuid VARCHAR(36) NOT NULL,"
                + "username VARCHAR(255) NOT NULL,"
                + "rank_name VARCHAR(255) NOT NULL,"
                + "FOREIGN KEY (rank_name) REFERENCES nvus_ranks(rank_name) ON DELETE CASCADE ON UPDATE CASCADE"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlRanks);
            stmt.execute(sqlRanksPlayers);
        } catch (SQLException e) {
            System.out.println("Error initializing Ranks database: " + e.getMessage());
        }

    }



    private void loadRanksFromConfig(FileConfiguration ranksConfig) {
        ConfigurationSection ranksSection = ranksConfig.getConfigurationSection("Ranks");
        if (ranksSection != null) {
            for (String rankKey : ranksSection.getKeys(false)) {
                String rankName = rankKey;
                double cost = ranksSection.getDouble(rankKey + ".Cost");
                List<String> commandsList = ranksSection.getStringList(rankKey + ".Commands");
                // Join the commands list into a single string with ";" as the delimiter
                String commands = String.join(";", commandsList);

                // Insert or update the ranks in the database with the commands as a single string
                upsertRankInDatabase(rankName, cost, commands);
            }
        }
    }


    public void syncRanks() {
        FileConfiguration ranksConfig = configManager.getConfig("ranks.yml");
        // Assuming you've moved loadRanksFromConfig method logic directly here or keep it separated as below
        loadRanksFromConfig(ranksConfig);
    }

    private void upsertRankInDatabase(String rankName, double cost, String commandsAsString) {
        // This method will check if the rank exists and update or insert accordingly
        String selectQuery = "SELECT COUNT(*) FROM nvus_ranks WHERE rank_name = ?";
        String insertQuery = "INSERT INTO nvus_ranks (rank_name, cost, commands) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE nvus_ranks SET cost = ?, commands = ? WHERE rank_name = ?";

        try (Connection conn = connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

            // Check if rank exists
            selectStmt.setString(1, rankName);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Rank exists, update it
                updateStmt.setDouble(1, cost);
                updateStmt.setString(2, commandsAsString);
                updateStmt.setString(3, rankName);
                updateStmt.executeUpdate();
            } else {
                // Rank doesn't exist, insert it
                insertStmt.setString(1, rankName);
                insertStmt.setDouble(2, cost);
                insertStmt.setString(3, commandsAsString);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error upserting rank: " + e.getMessage());
        }
    }

    public void initializeAndSyncRanks() {
        // Initialize the database (tables creation)
        initializeRanksDatabase();

        // Load ranks from the ranks.yml config and sync them with the database
        syncRanks();
    }



    public void reloadRanks() {
        syncRanks();
    }

    // Now it's a onstructor to re-build string comments from DB with ; as delimeter to a LIST/ARRAY :P
    public Rank getCurrentRank(Player player) {
        String playerRankSql = "SELECT rank_name FROM nvus_ranks_players WHERE uuid = ?";
        String rankDetailsSql = "SELECT * FROM nvus_ranks WHERE rank_name = ?";

        try (Connection conn = connect();
             PreparedStatement pstmtPlayerRank = conn.prepareStatement(playerRankSql)) {

            pstmtPlayerRank.setString(1, player.getUniqueId().toString());
            ResultSet rsPlayerRank = pstmtPlayerRank.executeQuery();

            if (rsPlayerRank.next()) {
                String rankName = rsPlayerRank.getString("rank_name");

                try (PreparedStatement pstmtRankDetails = conn.prepareStatement(rankDetailsSql)) {
                    pstmtRankDetails.setString(1, rankName);
                    ResultSet rsRankDetails = pstmtRankDetails.executeQuery();

                    if (rsRankDetails.next()) {
                        double cost = rsRankDetails.getDouble("cost");
                        String commandsAsString = rsRankDetails.getString("commands");
                        List<String> commands = new ArrayList<>();
                        if (commandsAsString != null && !commandsAsString.isEmpty()) {
                            commands = Arrays.asList(commandsAsString.split(";"));
                        }
                        return new Rank(rankName, cost, commands);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Rank getCurrentPlayerRank(Player player) {
        return getCurrentRank(player);
    }



    private Rank getNextRank(Player player) {
        Rank currentRank = getCurrentRank(player);
        if (currentRank == null) {
            return null; // Player has no current rank, handle as needed
        }

        final String query = "SELECT * FROM nvus_ranks WHERE cost > ? ORDER BY cost ASC LIMIT 1";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, currentRank.getCost());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String rankName = rs.getString("rank_name");
                double cost = rs.getDouble("cost");
                String commandsAsString = rs.getString("commands");
                List<String> commands = new ArrayList<>();
                if (commandsAsString != null && !commandsAsString.isEmpty()) {
                    commands = Arrays.asList(commandsAsString.split(";"));
                }
                return new Rank(rankName, cost, commands);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Player is at the highest rank or an error occurred
    }


    public Rank getPlayerNextRank(Player player) {
        return getNextRank(player);
    }

    private void updatePlayerRank(UUID uuid, String username, String newRankName) {
        // SQL query to update the player's rank
        String updateSql = "UPDATE nvus_ranks_players SET rank_name = ?, username = ? WHERE uuid = ?;";

        // SQL query to insert a new row if the player does not exist in the table
        String insertSql = "INSERT INTO nvus_ranks_players (uuid, username, rank_name) SELECT ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM nvus_ranks_players WHERE uuid = ?);";

        try (Connection conn = connect();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // First, try to update the player's rank
            updateStmt.setString(1, newRankName);
            updateStmt.setString(2, username);
            updateStmt.setString(3, uuid.toString());
            int rowsAffected = updateStmt.executeUpdate();

            // If the player was not already in the table, insert a new row
            if (rowsAffected == 0) {
                insertStmt.setString(1, uuid.toString());
                insertStmt.setString(2, username);
                insertStmt.setString(3, newRankName);
                insertStmt.setString(4, uuid.toString());
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error updating player rank: " + e.getMessage());
        }
    }


    public void updatePlayerRankData(Player player, Rank nextRank) {
        // Extract UUID and username from the Player object
        UUID playerUUID = player.getUniqueId();
        String username = player.getName();

        // Extract the new rank name from the Rank object
        String newRankName = nextRank.getName();

        // Update the player's rank in the database
        updatePlayerRank(playerUUID, username, newRankName);
    }

    public List<Rank> getAllRanksSorted() {
        List<Rank> sortedRanks = new ArrayList<>();
        final String query = "SELECT * FROM nvus_ranks ORDER BY cost ASC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String rankName = rs.getString("rank_name");
                double cost = rs.getDouble("cost");
                String commandsAsString = rs.getString("commands");
                List<String> commands = new ArrayList<>();
                if (commandsAsString != null && !commandsAsString.isEmpty()) {
                    commands = Arrays.asList(commandsAsString.split(";"));
                }
                Rank rank = new Rank(rankName, cost, commands);
                sortedRanks.add(rank);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error retrieving all ranks sorted: " + e.getMessage());
        }
        return sortedRanks;
    }



    private void setPlayerDefaultRank(UUID playerUuid, String playerName) {
        final String defaultRankQuery = "SELECT * FROM nvus_ranks ORDER BY cost ASC LIMIT 1";
        final String insertOrUpdatePlayerRankSql = "INSERT INTO nvus_ranks_players (uuid, username, rank_name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE rank_name = VALUES(rank_name), username = VALUES(username)";

        try (Connection conn = connect();
             PreparedStatement defaultRankStmt = conn.prepareStatement(defaultRankQuery);
             ResultSet rsDefaultRank = defaultRankStmt.executeQuery()) {

            if (rsDefaultRank.next()) {
                String defaultRankName = rsDefaultRank.getString("rank_name");

                try (PreparedStatement insertOrUpdateStmt = conn.prepareStatement(insertOrUpdatePlayerRankSql)) {
                    insertOrUpdateStmt.setString(1, playerUuid.toString());
                    insertOrUpdateStmt.setString(2, playerName);
                    insertOrUpdateStmt.setString(3, defaultRankName);
                    insertOrUpdateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error setting default rank for player: " + e.getMessage());
        }
    }

    public void assignPlayerToDefaultRank(Player player) {
        setPlayerDefaultRank(player.getUniqueId(), player.getName());
    }




















    /* ================================================================================================================
                                                GANG RELATED METHODS
    ==================================================================================================================*/

    private void initializeGangDatabase() {
        String sqlGangs = "CREATE TABLE IF NOT EXISTS nvus_gangs ("
                + "id INTEGER PRIMARY KEY " + (databaseType.equalsIgnoreCase("SQLite") ? "AUTOINCREMENT" : "AUTO_INCREMENT") + ","
                + "name TEXT NOT NULL,"
                + "owner_uuid VARCHAR(36) NOT NULL"
                + ");";

        String sqlMembers = "CREATE TABLE IF NOT EXISTS nvus_gangs_members ("
                + "uuid VARCHAR(36) PRIMARY KEY,"
                + "username TEXT NOT NULL,"
                + "gang_id INTEGER NOT NULL,"
                + "rank TEXT NOT NULL,"
                + "FOREIGN KEY (gang_id) REFERENCES nvus_gangs(id)"
                + ");";

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlGangs);
            stmt.execute(sqlMembers);
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
        }
    }



    public void createGang(String name, String ownerUuid, String ownerName) {
        String insertGangSQL = "INSERT INTO nvus_gangs(name, owner_uuid) VALUES(?,?)";
        String insertMemberSQL = "INSERT INTO nvus_gangs_members(uuid, username, gang_id, rank) VALUES(?,?,(SELECT id FROM nvus_gangs WHERE owner_uuid = ?),'Owner')";

        try (Connection conn = this.connect();
             PreparedStatement insertGangStmt = conn.prepareStatement(insertGangSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement insertMemberStmt = conn.prepareStatement(insertMemberSQL)) {

            // Insert the gang
            insertGangStmt.setString(1, name);
            insertGangStmt.setString(2, ownerUuid);
            insertGangStmt.executeUpdate();

            // Insert the owner as a member
            insertMemberStmt.setString(1, ownerUuid);
            insertMemberStmt.setString(2, ownerName);
            insertMemberStmt.setString(3, ownerUuid); // Re-use owner UUID to fetch the gang ID
            insertMemberStmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error creating gang and adding owner as member: " + e.getMessage());
        }
    }



//    public void createGang(String name, String ownerUuid) {
//        String sql = "INSERT INTO nvus_gangs(name, owner_uuid) VALUES(?,?)";
//        String sqlMember = "INSERT INTO nvus_gangs_members(name, owner_uuid) VALUES(?,?)";
//        try (Connection conn = this.connect();
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, name);
//            pstmt.setString(2, ownerUuid);
//            pstmt.executeUpdate();
//        }
//        catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//        try (Connection conn = this.connect();
//             PreparedStatement pstmt = conn.prepareStatement(sqlMember)) {
//            pstmt.setString(1, name);
//            pstmt.setString(2, ownerUuid);
//            pstmt.executeUpdate();
//        }
//        catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//    }

    public GangInfo getGangInfo(String gangName) {
        // Updated query to match gang_id from nvus_gangs_members with id in nvus_gangs, maybe a re-write is needed to match these id's as gangid??
        String gangInfoQuery = "SELECT g.name, " +
                "(SELECT username FROM nvus_gangs_members WHERE uuid = g.owner_uuid) AS ownerName, " +
                "COUNT(m.uuid) AS memberCount " +
                "FROM nvus_gangs g " +
                "JOIN nvus_gangs_members m ON g.id = m.gang_id " +
                "WHERE g.name = ? " +
                "GROUP BY g.name, g.owner_uuid";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(gangInfoQuery)) {
            pstmt.setString(1, gangName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String ownerName = rs.getString("ownerName");
                    int memberCount = rs.getInt("memberCount");
                    return new GangInfo(name, ownerName, memberCount);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching gang info: " + e.getMessage());
        }
        return null; // Return null if gang info could not be retrieved
    }




    public boolean removeGang(String gangName) {
        String sql = "DELETE FROM nvus_gangs WHERE name = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gangName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean addMember(String uuid, String username, int gangId, String rank) {
        String sql = "INSERT INTO nvus_gangs_members(uuid, username, gang_id, rank) VALUES(?,?,?,?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, username);
            pstmt.setInt(3, gangId);
            pstmt.setString(4, rank);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Return true if the row was added successfully
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false; // Return false if an error occurs
        }
    }

    public Integer getGangIdByName(String name) {
        String sql = "SELECT id FROM nvus_gangs WHERE name = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            pstmt.setString(1,name);
            ResultSet rs  = pstmt.executeQuery();

            // Only return the first id
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Gang not found
    }

    public Integer getGangIdByOwnerUuid(String ownerUuid) {
        String sql = "SELECT id FROM nvus_gangs WHERE owner_uuid = ?";

        try (Connection conn = this.connect();
             PreparedStatement pstmt  = conn.prepareStatement(sql)){

            pstmt.setString(1,ownerUuid);
            ResultSet rs  = pstmt.executeQuery();

            // Only return the first id
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Gang not found
    }

    public String getCurrentGangByPlayerUuid(UUID playerUuid) {
        String sql = "SELECT g.name FROM nvus_gangs g INNER JOIN nvus_gangs_members m ON g.id = m.gang_id WHERE m.uuid = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Return null if the player is not part of any gang or an error occurs
    }

    // GANG INVITES

    // Method to invite a player to a gang, limited to Gang Owner and Capo
    public boolean canInvite(String playerUuid) {
        // Example implementation
        String sql = "SELECT rank FROM nvus_gangs_members WHERE uuid = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String rank = rs.getString("rank");
                return "Owner".equalsIgnoreCase(rank) || "Capo".equalsIgnoreCase(rank);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false; // Player cannot invite or an error occurred
    }


    // Add Gang Invite
    public boolean addInvitation(String playerUuid, String gangName) {
        // This method would implement the logic to add an invitation.
        // For demonstration, let's assume we're directly adding them to the gang with a "Pending" rank.
        // You'll need to adjust this based on how you decide to manage invitations.
        Integer gangId = getGangIdByName(gangName);
        if (gangId == null) {
            return false; // Gang not found
        }
        // Assuming a "Pending" status for new invites
        addMember(playerUuid, "Pending", gangId, "Pending");
        return true;
    }

    public String getMemberRank(UUID playerUuid, String gangName) {
        String sql = "SELECT m.rank FROM nvus_gangs_members m INNER JOIN nvus_gangs g ON m.gang_id = g.id WHERE m.uuid = ? AND g.name = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, gangName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("rank");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Return null if the member's rank was not found or an error occurs
    }

    public boolean updateMemberRank(UUID playerUuid, String gangName, String newRank) {
        String sql = "UPDATE nvus_gangs_members SET rank = ? WHERE uuid = ? AND gang_id = (SELECT id FROM nvus_gangs WHERE name = ?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRank);
            pstmt.setString(2, playerUuid.toString());
            pstmt.setString(3, gangName);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean removeMember(UUID playerUuid, String gangName) {
        String sql = "DELETE FROM nvus_gangs_members WHERE uuid = ? AND gang_id = (SELECT id FROM nvus_gangs WHERE name = ?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, gangName);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean removeMembersByGangId(int gangId) {
        String sql = "DELETE FROM nvus_gangs_members WHERE gang_id = ?";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gangId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }



}
