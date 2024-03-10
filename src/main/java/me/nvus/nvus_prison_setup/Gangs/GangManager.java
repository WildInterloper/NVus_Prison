package me.nvus.nvus_prison_setup.Gangs;

import java.util.UUID;
import me.nvus.nvus_prison_setup.Database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GangManager {

    private final DatabaseManager dbManager;

    public GangManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public boolean createGang(String gangName, Player owner) {
        UUID ownerUuid = owner.getUniqueId();
        String ownerUuidString = ownerUuid.toString();

        // Check if the gang already exists
        if (dbManager.getGangIdByName(gangName) != null) {
            return false;
        }

        // Create the gang in the database
        dbManager.createGang(gangName, ownerUuidString);
        return true;
    }


    // Method to add a member to a gang
    public boolean addMemberToGang(String username, UUID uuid, String gangName, String rank) {
        Integer gangId = dbManager.getGangIdByName(gangName);
        if (gangId == null) {
            // Gang does not exist
            return false;
        }
        // Add the member to the gang with the specified rank
        dbManager.addMember(uuid.toString(), username, gangId, rank);
        return true;
    }

    // Check if a player can invite others to the gang
    public boolean canInvite(UUID playerUuid) {
        // Implement logic to check if the player has the rank of Owner or Capo
        // This requires a method in DatabaseManager to check the player's rank in their gang
        return dbManager.canInvite(playerUuid.toString());
    }

    // Add an invitation to the gang
    public boolean addInvitation(UUID playerUuid, String gangName) {
        // For simplicity, this example directly adds the player to the gang
        // You might want to implement a more complex invitation system
        Integer gangId = dbManager.getGangIdByName(gangName);
        if (gangId == null) {
            // Gang does not exist
            return false;
        }
        // Assume a default rank for new invites, e.g., "Pending" or "Member"
        String username = resolveUsername(playerUuid); // You need to implement this method
        return dbManager.addMember(playerUuid.toString(), username, gangId, "Pending");
    }

    // Method for a player to accept a gang invitation
    public boolean acceptInvitation(UUID playerUuid, String gangName) {
        return dbManager.updateMemberRank(playerUuid, gangName, "Member");
    }

    // Method for a player to deny a gang invitation or to be removed from the gang
    public boolean denyInvitation(UUID playerUuid, String gangName) {
        return dbManager.removeMember(playerUuid, gangName);
    }


    private String resolveUsername(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        } else {
            // The player might be offline or the UUID might not correspond to a known player.
            // Decide how you want to handle this case. For now, returning null or a placeholder.
            return null; // Consider an alternative approach based on your plugin's requirements.
        }
    }

    public String getCurrentGangName(UUID playerUuid) {
        // Call the DatabaseManager method to get the current gang name by player's UUID
        return dbManager.getCurrentGangByPlayerUuid(playerUuid);
    }

    public boolean leaveGang(UUID playerUuid, String gangName) {
        // Before removing a member, you may need to get the gang's ID based on its name
        Integer gangId = dbManager.getGangIdByName(gangName);
        if (gangId == null) {
            // This means the gang doesn't exist, which could indicate an issue with the provided gangName
            return false;
        }
        // Proceed to remove the player from the gang
        return dbManager.removeMember(playerUuid, gangName);
    }


}
