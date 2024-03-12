package me.nvus.nvus_prison_setup.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.io.File;

import me.nvus.nvus_prison_setup.Gangs.GangInfo;
import org.bukkit.configuration.file.FileConfiguration;
import me.nvus.nvus_prison_setup.Configs.ConfigManager;
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

        initializeDatabase();
    }


    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void initializeDatabase() {
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




    // Public Accessor to initialize the database
    public void initDatabase() {
        initializeDatabase();
    }

    public void createGang(String name, String ownerUuid) {
        String sql = "INSERT INTO nvus_gangs(name, owner_uuid) VALUES(?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ownerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public GangInfo getGangInfo(String gangName) {
        String gangInfoQuery = "SELECT g.name, (SELECT username FROM members WHERE uuid = g.owner_uuid) AS ownerName, COUNT(m.uuid) AS memberCount " +
                "FROM nvus_gangs g " +
                "LEFT JOIN nvus_gangs_members m ON g.id = m.gang_id " +
                "WHERE g.name = ? " +
                "GROUP BY g.name";

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
