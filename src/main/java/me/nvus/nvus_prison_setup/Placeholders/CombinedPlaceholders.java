package me.nvus.nvus_prison_setup.Placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class CombinedPlaceholders extends PlaceholderExpansion {
    private final PlaceholderManager placeholderManager;

    public CombinedPlaceholders(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
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
        return "1.2";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "rank_current":
                return placeholderManager.getCurrentRankName(player);
            case "rank_next":
                return placeholderManager.getNextRankName(player);
            case "rank_cost":
                double cost = placeholderManager.getNextRankCost(player);
                return cost >= 0 ? String.format("$%.2f", cost) : "N/A";
            case "gang_name":
                return placeholderManager.getCurrentGangName(player);
            case "gang_owner":
                String gangName = placeholderManager.getCurrentGangName(player);
                return placeholderManager.getGangOwnerName(gangName);
            case "gang_members":
                gangName = placeholderManager.getCurrentGangName(player);
                return String.valueOf(placeholderManager.getGangMemberCount(gangName));
            default:
                return null;
        }
    }
}
