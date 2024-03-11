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

    public enum GangRank {
        OWNER,
        CAPO,
        MEMBER
    }

    // Assuming a simplified model where the next rank is simply the next ordinal in the enum
    public String getNextRank(String currentRank) {
        try {
            GangRank current = GangRank.valueOf(currentRank.toUpperCase());
            int nextOrdinal = current.ordinal() + 1;
            if (nextOrdinal < GangRank.values().length) {
                return GangRank.values()[nextOrdinal].name();
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid rank input
        }
        return currentRank;
    }

    public String getPreviousRank(String currentRank) {
        try {
            GangRank current = GangRank.valueOf(currentRank.toUpperCase());
            int prevOrdinal = current.ordinal() - 1;
            if (prevOrdinal >= 0) {
                return GangRank.values()[prevOrdinal].name();
            }
        } catch (IllegalArgumentException e) {
            // Handle invalid rank input
        }
        return currentRank; // Return current rank if it's already the lowest or not found
    }

    public boolean createGang(String gangName, Player owner) {
        if (dbManager.getGangIdByName(gangName) != null) return false;
        dbManager.createGang(gangName, owner.getUniqueId().toString());
        return true;
    }

    public boolean addMemberToGang(String username, UUID uuid, String gangName, String rank) {
        Integer gangId = dbManager.getGangIdByName(gangName);
        if (gangId == null) return false;
        return dbManager.addMember(uuid.toString(), username, gangId, rank);
    }

    public boolean canInvite(UUID playerUuid) {
        String rank = dbManager.getMemberRank(playerUuid, getCurrentGangName(playerUuid));
        return GangRank.OWNER.name().equalsIgnoreCase(rank) || GangRank.CAPO.name().equalsIgnoreCase(rank);
    }

    public boolean canKickOrPromote(UUID playerUuid, String gangName) {
        String rank = dbManager.getMemberRank(playerUuid, gangName);
        return GangRank.OWNER.name().equalsIgnoreCase(rank) || GangRank.CAPO.name().equalsIgnoreCase(rank);
    }

    public boolean addInvitation(UUID playerUuid, String gangName) {
        Integer gangId = dbManager.getGangIdByName(gangName);
        if (gangId == null) return false;
        String username = resolveUsername(playerUuid);
        if (username == null) return false;
        return dbManager.addMember(playerUuid.toString(), username, gangId, "Pending");
    }

    public boolean acceptInvitation(UUID playerUuid, String gangName) {
        return dbManager.updateMemberRank(playerUuid, gangName, GangRank.MEMBER.name());
    }

    public boolean denyInvitation(UUID playerUuid, String gangName) {
        return dbManager.removeMember(playerUuid, gangName);
    }

    public boolean kickMember(UUID playerUuid, UUID targetUuid, String gangName) {
        if (canKickOrPromote(playerUuid, gangName)) {
            return dbManager.removeMember(targetUuid, gangName);
        }
        return false;
    }

    public boolean promoteMember(UUID playerUuid, UUID targetUuid, String gangName) {
        if (!canKickOrPromote(playerUuid, gangName)) return false;
        String currentRank = dbManager.getMemberRank(targetUuid, gangName);
        String newRank = getNextRank(currentRank);
        if (newRank.equals(currentRank)) return false;
        return dbManager.updateMemberRank(targetUuid, gangName, newRank);
    }

    public boolean demoteMember(UUID playerUuid, UUID targetUuid, String gangName) {
        if (!canKickOrPromote(playerUuid, gangName)) return false;
        String currentRank = dbManager.getMemberRank(targetUuid, gangName);
        String newRank = getPreviousRank(currentRank);
        if (newRank.equals(currentRank)) return false; // Prevent demotion if already at lowest rank
        return dbManager.updateMemberRank(targetUuid, gangName, newRank);
    }

    public boolean disbandGang(String gangName, UUID ownerUuid) {
        // Ensure the requester is the gang owner
        Integer gangId = dbManager.getGangIdByName(gangName);
        String ownerUuidString = ownerUuid.toString();
        if (gangId != null && dbManager.getGangIdByOwnerUuid(ownerUuidString).equals(gangId)) {
            // First, remove all members from the gang to maintain referential integrity
            dbManager.removeMembersByGangId(gangId);
            // Then, remove the gang itself
            return dbManager.removeGang(gangName);
        }
        return false;
    }


    public boolean leaveGang(UUID playerUuid, String gangName) {
        return dbManager.removeMember(playerUuid, gangName);
    }

    private String resolveUsername(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : null;
    }

    public String getCurrentGangName(UUID playerUuid) {
        return dbManager.getCurrentGangByPlayerUuid(playerUuid);
    }
}
