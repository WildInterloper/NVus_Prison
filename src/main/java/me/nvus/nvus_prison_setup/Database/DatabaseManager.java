package me.nvus.nvus_prison_setup.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DatabaseManager {

    private String url = "jdbc:sqlite:/plugins/NVus_Prison/nvus_prison.db";

    public DatabaseManager() {
        initializeDatabase();
    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private void initializeDatabase() {
        // SQL statement for creating gangs table
        String sqlGangs = "CREATE TABLE IF NOT EXISTS gangs ("
                + " id INTEGER PRIMARY KEY,"
                + " name TEXT NOT NULL,"
                + " owner_uuid TEXT NOT NULL"
                + ");";

        // SQL statement for creating members table
        String sqlMembers = "CREATE TABLE IF NOT EXISTS members ("
                + " uuid TEXT PRIMARY KEY,"
                + " username TEXT NOT NULL,"
                + " gang_id INTEGER NOT NULL,"
                + " rank TEXT NOT NULL,"
                + " FOREIGN KEY (gang_id) REFERENCES gangs(id)"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            // Create the gangs and members tables
            stmt.execute(sqlGangs);
            stmt.execute(sqlMembers);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createGang(String name, String ownerUuid) {
        String sql = "INSERT INTO gangs(name, owner_uuid) VALUES(?,?)";

        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ownerUuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean addMember(String uuid, String username, int gangId, String rank) {
        String sql = "INSERT INTO members(uuid, username, gang_id, rank) VALUES(?,?,?,?)";
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
        String sql = "SELECT id FROM gangs WHERE name = ?";

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
        String sql = "SELECT id FROM gangs WHERE owner_uuid = ?";

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
        String sql = "SELECT g.name FROM gangs g INNER JOIN members m ON g.id = m.gang_id WHERE m.uuid = ?";
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
        String sql = "SELECT rank FROM members WHERE uuid = ?";
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

    public boolean updateMemberRank(UUID playerUuid, String gangName, String newRank) {
        String sql = "UPDATE members SET rank = ? WHERE uuid = ? AND gang_id = (SELECT id FROM gangs WHERE name = ?)";

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
        String sql = "DELETE FROM members WHERE uuid = ? AND gang_id = (SELECT id FROM gangs WHERE name = ?)";

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




}
