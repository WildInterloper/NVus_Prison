package me.nvus.nvus_prison_setup.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import me.nvus.nvus_prison_setup.Ranks.RankManager;
import me.nvus.nvus_prison_setup.Gangs.GangManager;
import me.nvus.nvus_prison_setup.Gangs.GangInfo;
import me.nvus.nvus_prison_setup.Ranks.Rank;

public class CombinedPlaceholders extends PlaceholderExpansion {

    private final GangManager gangManager;
    private final RankManager rankManager;

    public CombinedPlaceholders(GangManager gangManager, RankManager rankManager) {
        this.gangManager = gangManager;
        this.rankManager = rankManager;
    }

    @Override
    public String getIdentifier() {
        return "nvus";
    }

    @Override
    public String getAuthor() {
        return "never2nv";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "gang_name":
                String gangName = gangManager.getCurrentGangName(player.getUniqueId());
                return gangName != null ? gangName : "No Gang";
            case "gang_owner":
                GangInfo gangInfo = gangManager.getGangInfo(gangManager.getCurrentGangName(player.getUniqueId()));
                return gangInfo != null ? gangInfo.getOwnerName() : "No Gang";
            case "gang_members":
                GangInfo gangInfoMembers = gangManager.getGangInfo(gangManager.getCurrentGangName(player.getUniqueId()));
                return gangInfoMembers != null ? String.valueOf(gangInfoMembers.getMemberCount()) : "No Gang";
            case "rank_current":
                Rank currentRank = rankManager.getCurrentRank(player);
                return currentRank != null ? currentRank.getName() : "Unranked";
            case "rank_next":
                Rank nextRank = rankManager.getNextRank(player);
                return nextRank != null ? nextRank.getName() : "Highest Rank";
            case "rank_cost":
                Rank rankForCost = rankManager.getNextRank(player);
                return rankForCost != null ? String.format("$%.2f", rankForCost.getCost()) : "N/A";
            default:
                return null;
        }
    }
}
