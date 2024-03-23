package me.nvus.nvus_prison_setup.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import me.nvus.nvus_prison_setup.Ranks.RankManager;
import me.nvus.nvus_prison_setup.Ranks.Rank;

import java.text.DecimalFormat;

public class RankPlaceholders extends PlaceholderExpansion {

    private final RankManager rankManager;

    public RankPlaceholders(RankManager rankManager) {
        this.rankManager = rankManager;
    }

    @Override
    public String getIdentifier() {
        return "prison";
    }

    @Override
    public String getAuthor() {
        return "never2nv";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "rank_current":
                Rank currentRank = rankManager.getCurrentRank(player);
                return currentRank != null ? currentRank.getName() : "Unranked";

            case "rank_next":
                Rank nextRank = rankManager.getNextRank(player);
                return nextRank != null ? nextRank.getName() : "Max Rank";

            case "rank_cost":
                Rank rankForCost = rankManager.getNextRank(player);
                if (rankForCost != null) {
                    DecimalFormat decimalFormat = new DecimalFormat("$###,###.00");
                    return decimalFormat.format(rankForCost.getCost());
                } else {
                    return "N/A";
                }

//            case "rank_cost":
//                Rank rankForCost = rankManager.getNextRank(player);
//                return rankForCost != null ? String.format("$%.2f", rankForCost.getCost()) : "N/A";

            default:
                return null;
        }
    }
}