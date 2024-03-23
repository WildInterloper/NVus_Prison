package me.nvus.nvus_prison_setup.Placeholders;

import me.nvus.nvus_prison_setup.Gangs.GangInfo;
import me.nvus.nvus_prison_setup.Gangs.GangManager;
import me.nvus.nvus_prison_setup.Ranks.Rank;
import me.nvus.nvus_prison_setup.Ranks.RankManager;

import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.entity.Player;

public class PlaceholderManager {
    private final GangManager gangManager;
    private final RankManager rankManager;

    public PlaceholderManager(GangManager gangManager, RankManager rankManager) {
        this.gangManager = gangManager;
        this.rankManager = rankManager;
    }

    public String getCurrentGangName(Player player) {
        return gangManager.getCurrentGangName(player.getUniqueId());
    }

    public String getGangOwnerName(String gangName) {
        GangInfo gangInfo = gangManager.getGangInfo(gangName);
        return gangInfo != null ? gangInfo.getOwnerName() : "No Gang";
    }

    public int getGangMemberCount(String gangName) {
        GangInfo gangInfo = gangManager.getGangInfo(gangName);
        return gangInfo != null ? gangInfo.getMemberCount() : 0;
    }

    public String getCurrentRankName(Player player) {
        Rank currentRank = rankManager.getCurrentRank(player);
        return currentRank != null ? currentRank.getName() : "Unranked";
    }

    public String getNextRankName(Player player) {
        Rank nextRank = rankManager.getNextRank(player);
        return nextRank != null ? nextRank.getName() : "Max Rank";
    }

    public String getNextRankCost(Player player) {
        Rank nextRank = rankManager.getNextRank(player);
        if (nextRank != null) {
            double cost = nextRank.getCost();
            // Using the US locale as an example for currency formatting
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            return currencyFormat.format(cost);
        }
        return "$0.00"; // or however you wish to indicate a null or zero value
    }

//    public double getNextRankCost(Player player) {
//        Rank nextRank = rankManager.getNextRank(player);
//        return nextRank != null ? nextRank.getCost() : -1;
//    }
}
